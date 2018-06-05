package com.brightcove.ddmetrics;

import java.io.Closeable;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.timgroup.statsd.StatsDClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * Much of this will be stolen from
 * `ScheduledReporter` in dropwizard metrics.
 */
public final class ScheduledReporterPoller implements Closeable {

    private static final Logger LOG =
        LoggerFactory.getLogger(ScheduledReporterPoller.class);

    /**
     * A simple named thread factory.
     *
     * Stolen from Drowizard.
     */
    private static class NamedThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        private NamedThreadFactory(String name) {
            final SecurityManager s = System.getSecurityManager();

            // @Nonnull check
            @Nullable ThreadGroup group = (s != null) ? s.getThreadGroup()
                : Thread.currentThread().getThreadGroup();
            if (group == null)
                throw new NullPointerException("ThreadGroup must not be null");
            this.group = group;

            this.namePrefix = "metrics-" + name + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            final Thread t =
                new Thread(group, r,
                           namePrefix + threadNumber.getAndIncrement(), 0);
            t.setDaemon(true);
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

    private static final AtomicInteger FACTORY_ID = new AtomicInteger();

    /**
     * The client to pass to each 'Reporter'
     */
    private final StatsDClient client;

    /**
     * The collection of Reporters that this poller will poll.
     */
    private final List<Reporter> reporters;

    /**
     * The executor service which will initiate the polling.
     */
    private final ScheduledExecutorService executor;

    /**
     * A handle for the currently scheduled polling, if any.
     */
    private @Nullable ScheduledFuture<?> scheduledFuture;

    /**
     * Return a unique 'ScheduledReporterPoller' using provided client.
     *
     * @param client The client which will be passed to polled 'Reporter's.
     * @returns ScheduledReporterPoller instance using 'client'.
     * @throws NullPointerException if client is null.
     */
    @EnsuresNonNull({"#1"})
    public static ScheduledReporterPoller forClient(StatsDClient client) {
        if (client == null)
            throw new NullPointerException("client must not be null");
        return new ScheduledReporterPoller(client);
    }

    private ScheduledReporterPoller(StatsDClient client) {
        this.client = client;
        reporters = new LinkedList<>();
        this.executor = Executors.newSingleThreadScheduledExecutor(
            new NamedThreadFactory("metrics-poller-" +
                                   FACTORY_ID.incrementAndGet()));
    }

    /**
     * Add a `Reporter` to poll.
     *
     * @param reporter The 'Reporter' to poll.
     * @throws NullPointerException if 'reporter' is null.
     */
    @EnsuresNonNull({"#1"})
    public void registerReporter(Reporter reporter) {
        if (reporter == null)
            throw new NullPointerException("reporter must not be null.");
        reporters.add(reporter);
    }

    /**
     * Poll all registered 'Reporter's.
     *
     * This performs a simple iteration in order of insertion/registration.
     * More interesting alternatives could be implemented through
     * composed 'Reporter's.
     */
    private void poll() {
        // This is synchronized for DropWizard...but simplicity and optimism
        // makes me think it isn't needed here (specifically with UDP).
        for (Reporter r : reporters) {
            try {
                r.report(this.client);
            } catch (Throwable t) {
                LOG.error("Reporter {} threw error.", r, t);
            }
        }
    }

    /**
     * Start the poller on the provided schedule.
     *
     * @param period The period between any two polls.
     * @throws NullPointerException if 'initialDelay' or 'period' is null.
     * @throws IllegalStateException if already in started state.
     */
    @EnsuresNonNull({"#1"})
    synchronized public void start(Duration period) {
        if (period == null)
            throw new NullPointerException("period must not be null.");
        if (scheduledFuture != null)
            throw new IllegalStateException("Already started!.");
        this.scheduledFuture = executor.scheduleAtFixedRate(() -> {
                try {
                    poll();
                } catch (Throwable ex) {
                    LOG.error("Exception thrown, will continue polling.", ex);
                }
            }, 0, period.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        executor.shutdown(); // Disable new tasks from being submitted.
        try {
            // Wait a while for existing tasks to terminate.
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Cancel currently executing tasks.
                // Wait a while for tasks to respond to being cancelled.
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    LOG.error("Could not terminate");
                }
            }
        } catch (InterruptedException e) {
	    // (Re-)Cancel if current thread also interrupted.
	    executor.shutdownNow();
	    // Preserve interrupt status.
	    Thread.currentThread().interrupt();
	}
    }
}
