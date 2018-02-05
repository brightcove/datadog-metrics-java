package com.brightcove.ddmetrics.jvm

import java.lang.management.BufferPoolMXBean

import spock.lang.Specification

class BufferReporterSpecification extends Specification {

  def 'Throws NullPointerException if bufferBeans is null.'() {
    when:
    new BufferReporter(null)

    then:
    thrown(NullPointerException)
  }
}
