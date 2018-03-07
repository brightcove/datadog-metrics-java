package com.brightcove.ddmetrics.jvm

import java.lang.management.BufferPoolMXBean
import javax.management.ObjectName

import spock.lang.Specification

import com.timgroup.statsd.StatsDClient

class BufferReporterSpecification extends Specification {

  StubBufferPoolMXBean directMBean = new StubBufferPoolMXBean(name: 'direct', count: 23L, totalCapacity: 1024L)
  BufferReporter reporter = new BufferReporter([directMBean])  
  StatsDClient client = Mock(StatsDClient)

  def 'Throws NullPointerException if bufferBeans is null.'() {
    when:
    new BufferReporter(null)

    then:
    thrown(NullPointerException)
  }

  def 'Throws NullPointerException if bufferBeans contains any null values.'() {
    when:
    new BufferReporter(bufferBeans)

    then:
    thrown(NullPointerException)

    where:
    bufferBeans << [
      [null],
      [new StubBufferPoolMXBean(name: 'direct'), null],
      [null, new StubBufferPoolMXBean(name: 'direct')]
    ]
  }

  def 'Throws IllegalArgumentException if bufferBeans is empty.'() {
    when:
    new BufferReporter([])

    then:
    thrown(IllegalArgumentException)
  }

  def 'Throws IllegalArgumentException if bufferBeans does not contain direct pool.'() {
    when:
    new BufferReporter([new StubBufferPoolMXBean(name: 'indirect'),
    	                new StubBufferPoolMXBean(name: 'transitive')])

    then:
    thrown(IllegalArgumentException)
  }

  def 'Reports count of direct buffers as jvm.buffers.direct.count.'() {
    when:
    reporter.report(client)

    then:
    1 * client.recordGaugeValue('jvm.buffers.direct.count', 23L)
  }

  def 'Reports count of direct buffers as jvm.buffers.direct.count.'() {
    when:
    reporter.report(client)

    then:
    1 * client.recordGaugeValue('jvm.buffers.direct.capacity', 1024L)
  }

}

class StubBufferPoolMXBean implements BufferPoolMXBean {
      long count
      long memoryUsed
      String name
      long totalCapacity
      ObjectName objectName
}
