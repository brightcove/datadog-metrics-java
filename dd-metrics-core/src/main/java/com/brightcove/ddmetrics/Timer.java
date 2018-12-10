package com.brightcove.ddmetrics;

import com.timgroup.statsd.StatsDClient;
import java.util.concurrent.Callable;
import java.util.Set;
import java.util.HashSet;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper around the StatsDClient which provides the ability to
 * conveniently time a block of code.
 */
public final class Timer {
    static final Logger LOG = LoggerFactory.getLogger(Timer.class);

    private final StatsDClient client;

    /**
     * Construct a Timer for the provided client.
     *
     * @param client The statsd client which will be used to publish timing metrics.
     */
    @EnsuresNonNull({"#1"})
    public Timer(StatsDClient client) {
        if (client == null) throw new NullPointerException("client must not be null.");
        this.client = client;
    }

    /**
     * Time the passed block and publish using the named metric and tags.
     *
     * @param metric The name of the metric under which to publish the time.
     * @param tags Any tags to associate with this published metric.
     * @param funk The block which will be timed. Will be expected to be passed
     * as a lambda or similar.
     * @returns The return value of the `funk` block.
     */
    @EnsuresNonNull({"#1", "#2", "#3"})
    public <T> T time(String metric, Set<String> tags, Callable<T> funk) {
        if (metric == null) throw new NullPointerException("metric name must not be null.");
        if (funk == null) throw new NullPointerException("function must not be null.");
        if (tags == null) throw new NullPointerException("tags must not be null.");
        long start = System.currentTimeMillis();
        T result;
        try {
            result = funk.call();
        } catch (Exception ex) {
            LOG.info("Exception thrown from timed callable.", ex);
            throw new RuntimeException(ex);
        }
        long time = System.currentTimeMillis() - start;
        client.recordExecutionTime(metric, time, tags.toArray(new String[tags.size()]));
        return result;
    }

    /**
     * Time the passed block and publish using the named metric (with no tags).
     *
     * @param metric The name of the metric under which to publish the time.
     * @param funk The block which will be timed. Will be expected to be passed
     * as a lambda or similar.
     * @returns The return value of the `funk` block.
     */
    @EnsuresNonNull({"#1", "#2"})
    public <T> T time(String metric, Callable<T> funk) {
        return time(metric, new HashSet<>(), funk);
    }
}
