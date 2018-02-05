package com.brightcove.ddmetrics.jvm

import java.lang.management.MemoryMXBean
import java.lang.management.MemoryUsage

import spock.lang.Specification

import com.timgroup.statsd.StatsDClient

class MemoryReporterSpecification extends Specification {

  StatsDClient client = Mock(StatsDClient)  
  MemoryMXBean memBean = Mock(MemoryMXBean)
  MemoryReporter reporter = new MemoryReporter(memBean)

  def setup() {
    memBean.getHeapMemoryUsage() >> new MemoryUsage(123L, 234L, 345L, 456L)
    memBean.getNonHeapMemoryUsage() >> new MemoryUsage(1123L, 1234L, 1345L, 1456L)
  }

  def 'Throws NullPointerException if memBean is null.'() {
    when:
    new MemoryReporter(null)

    then:
    thrown(NullPointerException)
  }

  def 'Reports used heap space as java.memory.heap.used.'() {
    when:
    reporter.report(client)

    then:
    1 * client.recordGaugeValue('jvm.memory.heap.used', 234L, [])
  }

  def 'Reports committed heap space as java.memory.heap.committed.'() {
    when:
    reporter.report(client)

    then:
    1 * client.recordGaugeValue('jvm.memory.heap.committed', 345L, [])
  }

  def 'Reports max heap space as java.memory.heap.max.'() {
    when:
    reporter.report(client)

    then:
    1 * client.recordGaugeValue('jvm.memory.heap.max', 456L, [])
  }

  def 'Reports used non-heap space as java.memory.non_heap.used.'() {
    when:
    reporter.report(client)

    then:
    1 * client.recordGaugeValue('jvm.memory.non_heap.used', 1234L, [])
  }

  def 'Reports committed non-heap space as java.memory.non_heap.committed.'() {
    when:
    reporter.report(client)

    then:
    1 * client.recordGaugeValue('jvm.memory.non_heap.committed', 1345L, [])
  }

  def 'Reports max non-heap space as java.memory.non_heap.max.'() {
    when:
    reporter.report(client)

    then:
    1 * client.recordGaugeValue('jvm.memory.non_heap.max', 1456, [])
  }


}
