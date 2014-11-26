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

package org.jacorb.test.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.easymock.MockControl;
import org.jacorb.notification.FilterManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterNotFound;

/**
 * @author Alphonse Bendt
 */
public class FilterManagerTest
{
    private FilterManager objectUnderTest_;

    private Filter mockFilter_;

    private MockControl controlFilter_;

    @Before
    public void setUp() throws Exception
    {
        objectUnderTest_ = new FilterManager();

        controlFilter_ = MockControl.createControl(Filter.class);
        mockFilter_ = (Filter) controlFilter_.getMock();

        controlFilter_.replay();
    }

    @After
    public void tearDown() throws Exception
    {
        controlFilter_.verify();
    }

    @Test
    public void testAdd_filter() throws Exception
    {
        assertEquals(0, objectUnderTest_.get_all_filters().length);

        objectUnderTest_.add_filter(mockFilter_);

        assertEquals(1, objectUnderTest_.get_all_filters().length);
    }

    @Test
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
            // expected
        }

        assertEquals(0, objectUnderTest_.get_all_filters().length);
    }

    @Test
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
            // expected
        }
    }

    @Test
    public void testGet_all_filters()
    {
        assertEquals(0, objectUnderTest_.get_all_filters().length);

        int id = objectUnderTest_.add_filter(mockFilter_);

        int[] all = objectUnderTest_.get_all_filters();

        assertEquals(1, all.length);

        assertEquals(id, all[0]);
    }

    @Test
    public void testRemove_all_filters()
    {
        objectUnderTest_.add_filter(mockFilter_);

        assertEquals(1, objectUnderTest_.get_all_filters().length);

        objectUnderTest_.remove_all_filters();

        assertEquals(0, objectUnderTest_.get_all_filters().length);
    }

    @Test
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
            // expected
        }

        objectUnderTest_.remove_all_filters();

        assertTrue(objectUnderTest_.getFilters().isEmpty());
    }

    }