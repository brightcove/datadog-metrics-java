package com.brightcove.ddmetrics.jvm;

import java.lang.management.GarbageCollectorMXBean;

import com.brightcove.ddmetrics.Reporter;
import com.timgroup.statsd.StatsDClient

import spock.lang.Specification

public class GarbageCollectionReporterSpecification extends Specification {
    StatsDClient client = Mock(StatsDClient)

    def 'Throws NullPointerException if gcBeans is null.'() {
        when:
        new GarbageCollectionReporter(null)

        then:
        thrown(NullPointerException)
    }

    def 'Throws NullPointerException if gcBeans contains any null values.'() {
        when:
        new GarbageCollectionReporter([null])

        then:
        thrown(NullPointerException)
    }

    def 'Does nothing if gcBeans is empty.'() {
        given:
        Reporter rep = new GarbageCollectionReporter([])

        when:
        rep.report(client)

        then:
        0 * client.recordGaugeValue(*_)
    }

    def 'Reports time and count for gcBeans tagged by name'() {
        given:
        Reporter rep = new GarbageCollectionReporter([
            Mock(GarbageCollectorMXBean) {
                getName() >> 'CMS'
                getCollectionTime() >> 500
                getCollectionCount() >> 3
           },
           Mock(GarbageCollectorMXBean) {
                getName() >> 'STW'
                getCollectionTime() >> 10
                getCollectionCount() >> 1
           }
        ])

        when:
        rep.report(client)

        then:
        1 * client.recordGaugeValue('jvm.gc.count', 3, ['collector:CMS'])
        1 * client.recordGaugeValue('jvm.gc.time', 500, ['collector:CMS'])
        1 * client.recordGaugeValue('jvm.gc.count', 1, ['collector:STW'])
        1 * client.recordGaugeValue('jvm.gc.time', 10, ['collector:STW'])
    }
}
