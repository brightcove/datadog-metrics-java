package com.brightcove.ddmetrics;

import java.time.Duration
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

import com.timgroup.statsd.StatsDClient

import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class ScheduledReporterPollerNonNullSpecification extends Specification {

    StatsDClient client = Mock(StatsDClient)
    ScheduledReporterPoller srp = ScheduledReporterPoller.forClient(client)

    def 'forClient constructor throws NPE if client is null.'() {
        when:
	ScheduledReporterPoller.forClient(null)

	then:
	thrown(NullPointerException)
    }

    def 'registerReporter throws NPE if reporter is null.'() {
    	when:
	srp.registerReporter(null)

	then:
	thrown(NullPointerException)
    }
}

class ScheduledReporterPollerPollingSpecification extends Specification {

    StatsDClient client = Mock(StatsDClient)
    ScheduledReporterPoller srp = ScheduledReporterPoller.forClient(client)

    def cleanup() {
        srp.close()
    }

    def 'Starts without error (doing nothing) if no Reporters registered.'() {
    	when:
    	srp.start(Duration.ofSeconds(1))
	TimeUnit.SECONDS.sleep 2

	then:
	notThrown(Exception)
    }

    def 'Attempting to start when already started throws an Exception.'() {
        given:
	srp.start(Duration.ofSeconds(1))

	when:
	srp.start(Duration.ofSeconds(1))

	then:
	thrown(IllegalStateException)
    }

    def 'If never started, Reporter is never polled.'() {
        given:
	CountDownLatch l = new CountDownLatch(1)
	Reporter reporter = Mock() { report(*_) >> { l.countDown() } }

	when:
	srp.registerReporter(reporter)
	Boolean polled = l.await(5, TimeUnit.SECONDS)

	then:
	!polled
    }

    def 'Reporter registered before startup is polled.'() {
        given:
	CountDownLatch l = new CountDownLatch(2)
	Reporter reporter = Mock() {
            report(*_) >> { args ->
	        assert(args[0] == client)
                l.countDown()
	    }
	}
	srp.registerReporter(reporter)

	when:
	srp.start(Duration.ofSeconds(1))
	Boolean polled = l.await(5, TimeUnit.SECONDS)

	then:
	polled
    }

    def 'Reporter registered after startup is polled.'() {
        given:
	CountDownLatch l = new CountDownLatch(2)	
	Reporter reporter = Mock(Reporter) {
	    report(*_) >> { args -> 
	        assert(args[0] == client)
		l.countDown()
            }
	}
	srp.start(Duration.ofSeconds(1))

	when:
	srp.registerReporter(reporter)
	Boolean polled = l.await(5, TimeUnit.SECONDS)

	then:
	polled
    }

    def 'Multiple registered reporters are polled in order of registration.'() {
        given:
	ConcurrentLinkedQueue<Integer> results = new ConcurrentLinkedQueue<>()
	List<Reporter> rs = (0..2).collect{ ix ->
	    Mock(Reporter) { report(*_) >> { results.add(ix) } }
	}
	rs.each(srp.&registerReporter)

	when:
	srp.start(Duration.ofSeconds(1))
	TimeUnit.SECONDS.sleep 3

	then:
	results.size() > 3
	results.eachWithIndex { it, ix -> assert ix % 3 == it }
    }

    def 'An exception thrown in the Reporter is swallowed'() {
        given:
	CountDownLatch l = new CountDownLatch(2)
	Reporter bad = Mock(Reporter) {
	    report(*_) >> { throw new AssertionError("I'm bad") }
	}
	Reporter reporter = Mock(Reporter) { report(*_) >> { l.countDown() } }
	srp.registerReporter(bad)
	srp.registerReporter(reporter)

	when:
	srp.start(Duration.ofSeconds(1))
	Boolean polled = l.await(5, TimeUnit.SECONDS)

	then:
	polled
    }
}

// The stopping/closing introduces a lot of corners which are just going to be
// ignored for now. They are not simple to test, unlikely to occur, and the
// logic was stolen from Dropwizard so it should be suitably trustworthy.
// Tests for corner cases should be added if there's a reason to suspect
// they're relevant.
class ScheduledReporterPollerCloseSpecification extends Specification {

    StatsDClient client = Mock(StatsDClient)
    ScheduledReporterPoller srp = ScheduledReporterPoller.forClient(client)

    def 'If never started, close successfully.'() {
    	when:
	srp.close()

	then:
	notThrown(Exception)
    }

    def 'If started, background thread is stopped.'() {
        given:
        AtomicReference<Thread> t = new AtomicReference<>();
	Reporter reporter = Mock(Reporter) {
	    report(*_) >> { t.compareAndSet(null, Thread.currentThread()) }
        }
	srp.registerReporter(reporter)
	def conditions = new PollingConditions(timeout: 3)

	when:
	srp.start(Duration.ofSeconds(2))

	then:
	conditions.eventually {
            assert t.get() != null
	}

	when:
	srp.close()

	then:
	conditions.eventually {
	    assert t.get().state == Thread.State.TERMINATED
	}
    }
}

