package org.jacorb.test.notification.engine;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import org.jacorb.notification.engine.WaitRetryStrategy;
import org.jacorb.notification.engine.PushOperation;
import org.jacorb.notification.engine.RetryException;

import org.omg.CORBA.TRANSIENT;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jacorb.notification.engine.MessagePushOperation;

/**
 * @author Alphonse Bendt
 */
public class WaitRetryStrategyTest extends TestCase {

    MockEventConsumer messageConsumer = new MockEventConsumer();

    PushOperation pushOperation_;

    boolean pushAllowed;

    int pushInvoked_;

    public WaitRetryStrategyTest(String name){
        super(name);
    }


    public void setUp() throws Exception {
        pushAllowed = true;

        pushOperation_ = new PushOperation() {
                public void invokePush()  {
                    if (pushAllowed) {
                        ++pushInvoked_;
                    } else {
                        throw new TRANSIENT();
                    }
                }

                public void dispose() {}
            };
    }


    public void testRetryTerminates() throws Exception {
        try {
            WaitRetryStrategy retry =
                new WaitRetryStrategy(messageConsumer, pushOperation_);

            pushAllowed = false;

            retry.retry();

            fail();
        } catch (RetryException e) {}

        assertEquals(0, pushInvoked_);
        assertEquals(1, messageConsumer.disposeCalled);
    }


    public void testRetrySucceeds() throws Exception {
        try {
            WaitRetryStrategy retry =
                new WaitRetryStrategy(messageConsumer, pushOperation_);

            retry.retry();
        } catch (RetryException e) {}

        assertEquals(1, pushInvoked_);
    }


    public void testRetryRetries() throws Exception {
        final WaitRetryStrategy retry =
            new WaitRetryStrategy(messageConsumer, pushOperation_);

        final Object notify = new Object();

        pushAllowed = false;

        new Thread() {
            public void run() {
                try {
                    retry.retry();
                    synchronized(notify) {
                        notify.notifyAll();
                    }
                } catch (RetryException e) {
                }
            }
        }.start();

        Thread.sleep(1000);

        pushAllowed = true;

        synchronized(notify) {
            notify.wait(5000);
        }

        assertEquals(1, pushInvoked_);
    }


    public static Test suite(){
        TestSuite suite = new TestSuite(WaitRetryStrategyTest.class);

        return suite;
    }
}
