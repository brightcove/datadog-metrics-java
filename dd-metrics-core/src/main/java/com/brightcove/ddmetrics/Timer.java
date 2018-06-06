package com.brightcove.ddmetrics;

import java.util.function.Supplier;
import java.util.Set;
import java.util.HashSet;
import com.timgroup.statsd.StatsDClient;

import org.checkerframework.checker.nullness.qual.EnsuresNonNull;

/**
 * A wrapper around the StatsDClient which provides the ability to
 * conveniently time a block of code.
 */
public final class Timer {
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
    public <T> T time(String metric, Set<String> tags, Supplier<T> funk) {
        if (metric == null) throw new NullPointerException("metric name must not be null.");
        if (funk == null) throw new NullPointerException("function must not be null.");
        if (tags == null) throw new NullPointerException("tags must not be null.");
        long start = System.currentTimeMillis();
        T result = funk.get();
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
    public <T> T time(String metric, Supplier<T> funk) {
        return time(metric, new HashSet<>(), funk);
    }
}
