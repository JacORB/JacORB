/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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

import org.jacorb.notification.engine.AbstractRetryStrategy;
import org.jacorb.notification.engine.AlwaysDisposeRetryStrategy;
import org.junit.Test;

/**
 * @author Alphonse Bendt
 */
public class AlwaysDisposeRetryStrategyTest extends AbstractRetryStrategyTestCase
{
    @Test
    public void testRetryDisposesOperationAndConsumer() throws Exception
    {
        mockConsumer_.isRetryAllowed();
        controlConsumer_.setDefaultReturnValue(true);

        mockConsumer_.destroy();
        controlConsumer_.replay();

        mockPushOperation_.dispose();
        controlPushOperation_.replay();

        objectUnderTest_.retry();

        controlConsumer_.verify();
        controlPushOperation_.verify();
    }
    
    @Test
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

    protected void setUpTest()
    {
        // no op
    }

    protected AbstractRetryStrategy newRetryStrategy()
    {
        return new AlwaysDisposeRetryStrategy(mockConsumer_, mockPushOperation_);
    }
}
