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

package org.jacorb.test.notification.filter;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.easymock.MockControl;
import org.jacorb.notification.filter.FilterUsageDecorator;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosNotifyFilter.FilterOperations;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class FilterUsageDecoratorTest extends TestCase
{
    private FilterUsageDecorator objectUnderTest_;

    private MockControl controlFilterOperations_;

    private FilterOperations mockFilterOperations_;

    private Any any_;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();

        any_ = ORB.init().create_any();

        controlFilterOperations_ = MockControl.createControl(FilterOperations.class);
        mockFilterOperations_ = (FilterOperations) controlFilterOperations_.getMock();

        objectUnderTest_ = new FilterUsageDecorator(mockFilterOperations_);
    }

    /**
     * Constructor for FilterUsageDecoratorTest.
     * 
     * @param name
     */
    public FilterUsageDecoratorTest(String name)
    {
        super(name);
    }

    public void testFilterOperationsAreDelegated() throws Exception
    {
        mockFilterOperations_.match(any_);
        controlFilterOperations_.setReturnValue(true);

        controlFilterOperations_.replay();

        FilterOperations handle = objectUnderTest_.getFilterOperations();

        assertTrue(handle.match(any_));

        controlFilterOperations_.verify();
    }

    public void testGetLastUsage() throws Exception
    {
        mockFilterOperations_.match(any_);
        controlFilterOperations_.setReturnValue(true, 2);

        controlFilterOperations_.replay();

        FilterOperations handle = objectUnderTest_.getFilterOperations();

        handle.match(any_);

        Date usage1 = objectUnderTest_.getLastUsage();

        assertTrue(usage1.getTime() <= System.currentTimeMillis());

        Thread.sleep(1000);

        handle.match(any_);

        Date usage2 = objectUnderTest_.getLastUsage();

        assertTrue(usage2.getTime() <= System.currentTimeMillis());

        assertTrue(usage1.getTime() < usage2.getTime());

        controlFilterOperations_.verify();
    }

    public static Test suite()
    {
        return new TestSuite(FilterUsageDecoratorTest.class);
    }
}
