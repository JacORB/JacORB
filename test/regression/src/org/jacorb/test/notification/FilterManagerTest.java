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

package org.jacorb.test.notification;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.easymock.MockControl;
import org.jacorb.notification.FilterManager;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterNotFound;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class FilterManagerTest extends TestCase
{
    private FilterManager objectUnderTest_;

    private Filter mockFilter_;

    private MockControl controlFilter_;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();

        objectUnderTest_ = new FilterManager();

        controlFilter_ = MockControl.createControl(Filter.class);
        mockFilter_ = (Filter) controlFilter_.getMock();

        controlFilter_.replay();
    }

    protected void tearDown() throws Exception
    {
        controlFilter_.verify();
        super.tearDown();
    }

    /**
     * Constructor for FilterManagerTest.
     * 
     * @param name
     */
    public FilterManagerTest(String name)
    {
        super(name);
    }

    public void testAdd_filter() throws Exception
    {
        assertEquals(0, objectUnderTest_.get_all_filters().length);

        objectUnderTest_.add_filter(mockFilter_);

        assertEquals(1, objectUnderTest_.get_all_filters().length);
    }

    public void testRemove_filter() throws Exception
    {
        int id = objectUnderTest_.add_filter(mockFilter_);

        assertEquals(1, objectUnderTest_.get_all_filters().length);

        objectUnderTest_.remove_filter(id);

        assertEquals(0, objectUnderTest_.get_all_filters().length);

        try
        {
            objectUnderTest_.remove_filter(id);
            fail();
        } catch (FilterNotFound e)
        {
        }

        assertEquals(0, objectUnderTest_.get_all_filters().length);
    }

    public void testGet_filter() throws Exception
    {
        int id = objectUnderTest_.add_filter(mockFilter_);

        assertEquals(mockFilter_, objectUnderTest_.get_filter(id));

        objectUnderTest_.remove_all_filters();

        try
        {
            objectUnderTest_.get_filter(id);
            fail();
        } catch (FilterNotFound e)
        {
        }
    }

    public void testGet_all_filters()
    {
        assertEquals(0, objectUnderTest_.get_all_filters().length);

        int id = objectUnderTest_.add_filter(mockFilter_);

        int[] all = objectUnderTest_.get_all_filters();

        assertEquals(1, all.length);

        assertEquals(id, all[0]);
    }

    public void testRemove_all_filters()
    {
        objectUnderTest_.add_filter(mockFilter_);

        assertEquals(1, objectUnderTest_.get_all_filters().length);

        objectUnderTest_.remove_all_filters();

        assertEquals(0, objectUnderTest_.get_all_filters().length);
    }

    public void testGetFilters()
    {
        assertTrue(objectUnderTest_.getFilters().isEmpty());
        
        objectUnderTest_.add_filter(mockFilter_);
        assertEquals(1, objectUnderTest_.getFilters().size());
        assertEquals(mockFilter_, objectUnderTest_.getFilters().get(0));
        
        try {
            objectUnderTest_.getFilters().clear();
            fail();
        } catch (Exception e)
        {    
        }
        
        objectUnderTest_.remove_all_filters();
        
        assertTrue(objectUnderTest_.getFilters().isEmpty());
    }

    
    public static Test suite()
    {
        return new TestSuite(FilterManagerTest.class);
    }

}