package com.brightcove.ddmetrics;

import com.timgroup.statsd.StatsDClient;

import org.checkerframework.checker.nullness.qual.EnsuresNonNull;

/**
 * A function which should report current metric values.
 *
 * These can be attached to a 'ScheduledReporterPoller' so that metrics can
 * be periodically sent to statsd. In cases where metrics can be sent
 * immediately, the statsd client can and should be called directly.
 * This interface serves as a lightweight an unopinionated
 * wrapper around statsd calls. It is expected but not
 * enforced that an implementation will send metrics using
 * the provided client. It may be nice to ensure that each
 * reporter only sends a single metric...but multiple
 * metrics could also be send in the same function.
 */
@FunctionalInterface
public interface Reporter {

    /**
     * Send data using the provided 'client' (ostensibly).
     *
     * @param client Statsd client provided by the caller of this function.
     * @throws NullPointerException if 'client' is null.
     */
    @EnsuresNonNull({"#1"})
    public void report(StatsDClient client);
}
