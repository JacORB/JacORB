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

import junit.framework.Test;

import org.jacorb.test.notification.NotificationTestCase;
import org.jacorb.test.notification.NotificationTestCaseSetup;
import org.omg.CORBA.Any;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.CosNotifyFilter.InvalidGrammar;
import org.omg.CosNotifyFilter.MappingFilter;

public class MappingFilterFactoryTest extends NotificationTestCase
{
    private FilterFactory filterFactory_;
    
    public MappingFilterFactoryTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    public void setUpTest() throws Exception
    {
        filterFactory_ = (FilterFactory) getPicoContainer().getComponentInstanceOfType(
                FilterFactory.class);
    }

    public void testFilterFactory() throws Exception
    {
        Any _defaultValue = getORB().create_any();

        MappingFilter _filter = filterFactory_.create_mapping_filter("EXTENDED_TCL", _defaultValue);

        assertEquals("EXTENDED_TCL", _filter.constraint_grammar());

        try
        {
            filterFactory_.create_mapping_filter("SOMETHING_ELSE", _defaultValue);

            fail();
        } catch (InvalidGrammar e)
        {
            // expected
        }
    }
    
    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite(MappingFilterFactoryTest.class);
    }
}
