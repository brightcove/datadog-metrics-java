package com.brightcove.ddmetrics

import com.timgroup.statsd.StatsDClient

import spock.lang.Specification
import spock.thencleanup.ThenkfulSpecification

class TimerNullnessSpecification extends Specification {

    StatsDClient client = Mock(StatsDClient)
    Timer timer = new Timer(client)

    def 'construction throws NPE on null client'() {
        when:
        new Timer(null)

        then:
        thrown(NullPointerException)
    }

    def 'time throws NPE on null metric name'() {
        when:
        timer.time(null, {})

        then:
        thrown(NullPointerException)
    }

    def 'time throws NPE on null function'() {
        when:
        timer.time('myFunk', null)

        then:
        thrown(NullPointerException)
    }

    def 'time throws NPE on null tags'() {
        when:
        timer.time('myFunk', null, {})

        then:
        thrown(NullPointerException)
    }

}

class TimerSpecification extends ThenkfulSpecification {

    StatsDClient client = Mock(StatsDClient)
    Timer timer = new Timer(client)

    def 'time sends the execution time metric to the client'() {
        when:
        timer.time('myMethod') {
            def somethingDone = true
        }

        then:
        1 * client.recordExecutionTime(*_) >> { args ->
            thenk { assert args[0] == 'myMethod' }
            thenk { assert args[1] < 2 }
            thenk { assert args[2] == [] }
        }
    }

    def 'time sends the execution time metric to the client for a slow block'() {
        when:
        timer.time('myMethod') {
            sleep 500
        }

        then:
        1 * client.recordExecutionTime(*_) >> { args ->
            thenk { assert args[0] == 'myMethod' }
            thenk { assert args[1] >= 500 }
            thenk { assert args[2] == [] }
        }
    }

    def 'time rethrows any exceptions from callable and does not log time'() {
        when:
        timer.time('anything') {
            throw new IOException('blah')
        }

        then:
        thrown(Exception)
        0 * client.recordExecutionTime(*_)
    }

    def 'nested calls are supported'() {
        given:
        def calls = [:]
        client.recordExecutionTime(*_) >> { args ->
            calls[args[0]] = args[1]
        }

        when:
        timer.time('meth1') {
            sleep 1
            timer.time('meth2') {
                sleep 1
                timer.time('meth3') {
                    sleep 1
                }
            }
        }

        then:
        calls.meth3
        calls.meth2 > calls.meth3
        calls.meth1 > calls.meth2
    }

    def 'provided tags are used'() {
        when:
        timer.time('myMethod', ['foo:bar'] as Set) {
            def somethingDone = true
        }

        then:
        1 * client.recordExecutionTime(*_) >> { args ->
            thenk { assert args[0] == 'myMethod' }
            thenk { assert args[1] < 2 }
            thenk { assert args[2] == ['foo:bar'] }
        }
    }

    def 'provided tags are mutable within the block'() {
        when:
        def tags = ['foo:bar'] as Set
        timer.time('myMethod', tags) {
            tags.add('blah')
        }

        then:
        1 * client.recordExecutionTime(*_) >> { args ->
            thenk { assert args[0] == 'myMethod' }
            thenk { assert args[2] == ['foo:bar', 'blah'] }
        }
    }

}

class TimerExceptionSpecification extends Specification {
    StatsDClient client = Mock(StatsDClient)
    Timer timer = new Timer(client)

    def 'when the timed code throws a RuntimeException, that exception escapes'() {
        given:
        Exception ex = new IllegalArgumentException('Sample runtime exception')

        when:
        timer.time('rtEx') {
            throw ex
        }

        then:
        Exception actual = thrown()
	actual == ex
    }

    def 'when the timed code throws a checked exception, that exception escapes'() {
        given:
        Exception ex = new java.util.concurrent.TimeoutException('Sample checked exception')

        when:
        timer.time('rtEx') {
            throw ex
        }

        then:
        Exception actual = thrown()
	actual == ex
    }

    def 'exception handling in client code functions properly'() {
        given:
        Exception ex = new java.util.concurrent.TimeoutException('Sample checked exception')

        when:
        try {
            timer.time('rtEx') {
                throw ex
            }
        } catch (java.util.concurrent.TimeoutException e) {
            // Exception swallowed
        }

        then:
        notThrown()
    }
}
