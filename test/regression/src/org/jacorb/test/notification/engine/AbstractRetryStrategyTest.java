/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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
 *
 */

package org.jacorb.test.notification.engine;

import org.easymock.MockControl;
import org.jacorb.notification.engine.PushOperation;
import org.jacorb.notification.engine.AbstractRetryStrategy;
import org.jacorb.notification.interfaces.MessageConsumer;

import junit.framework.TestCase;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public abstract class AbstractRetryStrategyTest extends TestCase
{
    protected MessageConsumer mockConsumer_;
    protected MockControl controlConsumer_;
    protected PushOperation mockPushOperation_;
    protected MockControl controlPushOperation_;

    protected AbstractRetryStrategy objectUnderTest_;

    /**
     * Constructor for AbstractRetryStrategyTest.
     * @param name
     */
    public AbstractRetryStrategyTest(String name)
    {
        super(name);
    }

    protected final void setUp() throws Exception
    {
        super.setUp();
        
        controlConsumer_ = MockControl.createNiceControl(MessageConsumer.class);
        mockConsumer_ = (MessageConsumer) controlConsumer_.getMock();
    
        controlPushOperation_ = MockControl.createNiceControl(PushOperation.class);
        mockPushOperation_ = (PushOperation) controlPushOperation_.getMock();
        
        setUpTest();
        
        objectUnderTest_ = newRetryStrategy();
    }

    protected abstract void setUpTest();
    
    protected abstract AbstractRetryStrategy newRetryStrategy();

    public void testNoRetryAllowedDisposesPushOperation() throws Exception
    {
        mockConsumer_.isRetryAllowed();
        controlConsumer_.setDefaultReturnValue(false);
        
        controlConsumer_.replay();
        
        mockPushOperation_.dispose();
        controlPushOperation_.replay();
        
        objectUnderTest_.retry();
        
        controlConsumer_.verify();
        controlPushOperation_.verify();
    }
}
