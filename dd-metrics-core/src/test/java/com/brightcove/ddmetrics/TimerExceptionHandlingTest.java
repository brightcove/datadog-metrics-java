package com.brightcove.ddmetrics;

import com.timgroup.statsd.NonBlockingStatsDClient;
import java.util.concurrent.TimeoutException;
import org.junit.Test;

/* Use Java for this test to make sure reference types are resolved representatively. */

public class TimerExceptionHandlingTest {

    @Test
    public void checkedExceptionsNeedIntrospection() {
        Timer timer = new Timer(new NonBlockingStatsDClient("test","localhost",0));
        try {
            timer.time("metric", () -> {
                throw new TimeoutException("Too long!!");
            });
        } catch(Exception ex) {
            if (!(ex instanceof TimeoutException)) throw ex;
        }
    }

    @Test
    public void runtimeExceptionsCanBeHandledCanBeHandled() {
        Timer timer = new Timer(new NonBlockingStatsDClient("test","localhost",0));
        try {
            timer.time("metric", () -> {
                throw new IllegalArgumentException("Too long!!");
            });
        } catch(IllegalArgumentException ex) {
            // Exception swallowed.
        }
    }
}
