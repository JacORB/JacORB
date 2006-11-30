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
import junit.framework.TestSuite;

import org.jacorb.notification.engine.RetryException;
import org.jacorb.notification.engine.AbstractRetryStrategy;
import org.jacorb.notification.engine.WaitRetryStrategy;
import org.omg.CORBA.TRANSIENT;

/**
 * @author Alphonse Bendt
 */
    
public class WaitRetryStrategyTest extends AbstractRetryStrategyTestCase
{
    public void testRetryTerminatesAndThrowsException() throws Exception
    {
        mockConsumer_.isRetryAllowed();
        controlConsumer_.setReturnValue(true);

        mockConsumer_.isRetryAllowed();
        controlConsumer_.setReturnValue(true);

        mockConsumer_.isRetryAllowed();
        controlConsumer_.setDefaultReturnValue(false);

        mockConsumer_.incErrorCounter();
        controlConsumer_.setReturnValue(0);

        mockConsumer_.destroy();

        controlConsumer_.replay();

        mockPushOperation_.invokePush();
        controlPushOperation_.setThrowable(new TRANSIENT());

        mockPushOperation_.dispose();

        controlPushOperation_.replay();

        try
        {
            objectUnderTest_.retry();

            fail();
        } catch (RetryException e)
        {
            // expected
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

        objectUnderTest_.retry();

        controlConsumer_.verify();
        controlPushOperation_.verify();
    }

    public void testSuccessfulRetryDisposesPushOperation() throws Exception
    {
        mockPushOperation_.invokePush();
        mockPushOperation_.dispose();
        
        controlPushOperation_.replay();
        
        mockConsumer_.isRetryAllowed();
        controlConsumer_.setDefaultReturnValue(true);
        
        controlConsumer_.replay();
      
        objectUnderTest_.retry();
        
        controlPushOperation_.verify();
    }
    
    public void testNotSuccessfulRetryDisposes() throws Exception
    {
        mockPushOperation_.dispose();

        mockConsumer_.isRetryAllowed();
        controlConsumer_.setDefaultReturnValue(false);
        
        controlPushOperation_.replay();
        
        controlConsumer_.replay();      
        
        objectUnderTest_.retry();
        
        controlPushOperation_.verify();
    }

    
    public void testRetryAllowedDisposesPushOperation() throws Exception
    {
        mockPushOperation_.dispose();
        
        controlPushOperation_.replay();
        
        mockConsumer_.isRetryAllowed();
        controlConsumer_.setDefaultReturnValue(true);
        
        controlConsumer_.replay();
      
        objectUnderTest_.retry();
        
        controlPushOperation_.verify();
    }
    
    public static Test suite()
    {
        TestSuite suite = new TestSuite(WaitRetryStrategyTest.class);

        return suite;
    }

    
    protected void setUpTest()
    {
       // no op
    }

    
    protected AbstractRetryStrategy newRetryStrategy()
    {
        return new WaitRetryStrategy(mockConsumer_, mockPushOperation_);
    }
}