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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.easymock.MockControl;
import org.jacorb.notification.engine.PushOperation;
import org.jacorb.notification.engine.RetryException;
import org.jacorb.notification.engine.WaitRetryStrategy;
import org.jacorb.notification.interfaces.MessageConsumer;
import org.omg.CORBA.TRANSIENT;

/**
 * @author Alphonse Bendt
 */
public class WaitRetryStrategyTest extends TestCase
{
    private MessageConsumer mockConsumer_;

    private MockControl controlConsumer_;

    private PushOperation mockPushOperation_;

    private MockControl controlPushOperation_;

    public WaitRetryStrategyTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        controlConsumer_ = MockControl.createControl(MessageConsumer.class);
        mockConsumer_ = (MessageConsumer) controlConsumer_.getMock();

        controlPushOperation_ = MockControl.createControl(PushOperation.class);
        mockPushOperation_ = (PushOperation) controlPushOperation_.getMock();
    }

    public void testRetryTerminates() throws Exception
    {
        mockConsumer_.isRetryAllowed();
        controlConsumer_.setReturnValue(true);

        mockConsumer_.isRetryAllowed();
        controlConsumer_.setReturnValue(true);

        mockConsumer_.isRetryAllowed();
        controlConsumer_.setDefaultReturnValue(false);

        mockConsumer_.incErrorCounter();
        controlConsumer_.setReturnValue(0);

        mockConsumer_.dispose();

        controlConsumer_.replay();

        mockPushOperation_.invokePush();
        controlPushOperation_.setThrowable(new TRANSIENT());

        mockPushOperation_.dispose();

        controlPushOperation_.replay();

        try
        {
            WaitRetryStrategy retry = new WaitRetryStrategy(mockConsumer_, mockPushOperation_);

            retry.retry();

            fail();
        } catch (RetryException e)
        {
        }

        controlPushOperation_.verify();
        controlConsumer_.verify();
    }

    public void testRetrySucceeds() throws Exception
    {
        mockConsumer_.isRetryAllowed();
        controlConsumer_.setDefaultReturnValue(true);

        mockPushOperation_.invokePush();
        mockPushOperation_.dispose();
        controlPushOperation_.replay();

        controlConsumer_.replay();

        WaitRetryStrategy retry = new WaitRetryStrategy(mockConsumer_, mockPushOperation_);

        retry.retry();

        controlConsumer_.verify();
        controlPushOperation_.verify();
    }


    public static Test suite()
    {
        TestSuite suite = new TestSuite(WaitRetryStrategyTest.class);

        return suite;
    }
}