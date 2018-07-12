package com.brightcove.ddmetrics.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import org.checkerframework.checker.nullness.qual.EnsuresNonNull;

import com.brightcove.ddmetrics.Reporter;

import com.timgroup.statsd.StatsDClient;

/**
 * Report some standard JVM memory usage metrics ('jvm.memory.*').
 *
 * Reads metrics from the 'MemoryMXBean' provided by
 * by the JMX management interface.
 *
 * Currently reports:
 *
 * - 'jvm.memory.heap.used'
 * - 'jvm.memory.heap.committed'
 * - 'jvm.memory.heap.max'
 * - 'jvm.memory.non_heap.used'
 * - 'jvm.memory.non_heap.committed'
 * - 'jvm.memory.non_heap.max'
 */
public class MemoryReporter implements Reporter {

    private final MemoryMXBean memBean;

    public MemoryReporter() {
        this(ManagementFactory.getMemoryMXBean());
    }

    @EnsuresNonNull({"#1"})
    MemoryReporter(MemoryMXBean memBean) {
        if (memBean == null)
            throw new NullPointerException("memBean must not be null");
        this.memBean = memBean;
    }

    public void report(StatsDClient client) {
        client.recordGaugeValue("jvm.memory.heap.used",
                                memBean.getHeapMemoryUsage().getUsed());
        client.recordGaugeValue("jvm.memory.heap.committed",
                                memBean.getHeapMemoryUsage().getCommitted());
        client.recordGaugeValue("jvm.memory.heap.max",
                                memBean.getHeapMemoryUsage().getMax());
        client.recordGaugeValue("jvm.memory.non_heap.used",
                                memBean.getNonHeapMemoryUsage().getUsed());
        client.recordGaugeValue("jvm.memory.non_heap.committed",
                                memBean.getNonHeapMemoryUsage().getCommitted());
        client.recordGaugeValue("jvm.memory.non_heap.max",
                                memBean.getNonHeapMemoryUsage().getMax());
    }
}
