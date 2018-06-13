package com.brightcove.ddmetrics.jvm;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;

import com.brightcove.ddmetrics.Reporter;

import org.checkerframework.checker.nullness.qual.EnsuresNonNull;

import com.timgroup.statsd.StatsDClient;

/**
 * Report metrics related to garbage collection.
 *
 * Reads metrics from 'GarbageCollectorMXBean's provided by
 * the JMX management interface.
 *
 * Currently reports:
 *
 * - 'jvm.gc.count'
 * - 'jvm.gc.time'
 *
 * The metrics will be tagged by the name of the collector as provided by the bean name.
 */
public class GarbageCollectionReporter implements Reporter {
    private static final String TAG_PREFIX = "collector:";

    private final Iterable<GarbageCollectorMXBean> gcBeans;

    public GarbageCollectionReporter() {
        this(ManagementFactory.getGarbageCollectorMXBeans());
    }

    @EnsuresNonNull({"#1"})
    public GarbageCollectionReporter(Iterable<GarbageCollectorMXBean> gcBeans) {
        if (gcBeans == null)
            throw new NullPointerException("gcBeans must not be null.");
        for (final GarbageCollectorMXBean gcBean: gcBeans) {
            if (gcBean == null)
                throw new NullPointerException("gcBeans must not contain any nulls.");
        }
        this.gcBeans = gcBeans;
    }

    public void report(StatsDClient client) {
        for (final GarbageCollectorMXBean gcBean: gcBeans) {
            String gcName = gcBean.getName();
            client.recordGaugeValue("jvm.gc.count",
                                    gcBean.getCollectionCount(),
                                    TAG_PREFIX + gcName);
            client.recordGaugeValue("jvm.gc.time",
                                    gcBean.getCollectionTime(),
                                    TAG_PREFIX + gcName);
        }
    }
}
