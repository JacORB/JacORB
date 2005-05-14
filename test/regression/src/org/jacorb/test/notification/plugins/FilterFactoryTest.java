package org.jacorb.test.notification.plugins;

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

import org.jacorb.notification.filter.bsh.BSHFilter;
import org.jacorb.test.notification.common.NotifyServerTestCase;
import org.jacorb.test.notification.common.NotifyServerTestSetup;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterFactory;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class FilterFactoryTest extends NotifyServerTestCase
{
    private FilterFactory objectUnderTest_;

    public void setUpTest() throws Exception
    {
        objectUnderTest_ = getDefaultChannel().default_filter_factory();
    }

    public FilterFactoryTest(String name, NotifyServerTestSetup setup)
    {
        super(name, setup);
    }

    public void testCreateBSHFilter() throws Exception
    {
        Filter _filter = objectUnderTest_.create_filter(BSHFilter.CONSTRAINT_GRAMMAR);

        assertEquals(BSHFilter.CONSTRAINT_GRAMMAR, _filter.constraint_grammar());
    }

    public void testCreateNonExisting() throws Exception
    {
        try
        {
            objectUnderTest_.create_filter("ACME");
            fail();
        } catch (Exception e)
        {
            // expected
        }
    }

    public static Test suite() throws Exception
    {
        return NotifyServerTestCase.suite(FilterFactoryTest.class);
    }
}