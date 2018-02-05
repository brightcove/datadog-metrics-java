package com.brightcove.ddmetrics.jvm;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

import org.checkerframework.checker.nullness.qual.EnsuresNonNull;

import com.brightcove.ddmetrics.Reporter;

import com.timgroup.statsd.StatsDClient;

/**
 * Report metrics around standard JVM buffers.
 *
 * Reads metrics from 'BufferPoolMXBean's provided by
 * the JMX management interface.
 *
 * Current reports:
 *
 * - 'jvm.buffers.direct.count'
 */
public class BufferReporter implements Reporter {

    private final BufferPoolMXBean directPool;
    
    public BufferReporter() {
	this(ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class));
    }

    @EnsuresNonNull({"#1"})
    BufferReporter(List<BufferPoolMXBean> bufferBeans) {
	if (bufferBeans == null)
	    throw new NullPointerException("bufferBeans must not be null");
	BufferPoolMXBean directPool = null;
	for (BufferPoolMXBean bufferBean : bufferBeans) {
	    if (bufferBean == null)
		throw new NullPointerException("bufferBeans must not have nulls");
	    if (bufferBean.getName() == "direct") {
		directPool = bufferBean;
		// could break...but don't to complete null checking
	    }
	}
	if (directPool == null)
	    throw new IllegalArgumentException("direct pool must be present");
	this.directPool = directPool;
    }

    public void report(StatsDClient client) {
	client.recordGaugeValue("jvm.buffers.direct.count",
				directPool.getCount());
    }
}
