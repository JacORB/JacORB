package org.jacorb.test.util;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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

import java.util.Properties;

import org.jacorb.util.Environment;
import org.jacorb.util.LogKitLoggerFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *  Unit Test for class LogKitLoggerFactory
 * @author Alphonse Bendt
 * @version $Id$
 */

public class LogKitLoggerFactoryTest extends TestCase
{
    static int defaultPriority;

    static {
        String defaultPriorityString =
            Environment.getProperty("jacorb.log.default.verbosity");

        if (defaultPriorityString != null)
        {
            try
            {
                defaultPriority = Integer.parseInt(defaultPriorityString);
            }
            catch (NumberFormatException nfe)
            {
                defaultPriority = 0;
            }
        }
    }

    ////////////////////////////////////////

    LogKitLoggerFactory factory;

    ////////////////////////////////////////

    public LogKitLoggerFactoryTest (String name)
    {
        super(name);
    }

    ////////////////////////////////////////

    public void setUp() throws Exception
    {
        Properties props = new Properties();

        props.setProperty("jacorb.log.verbosity", "2");
        props.setProperty("jacorb.component.log.verbosity", "3");
        props.setProperty("jacorb.component.subcomponent.log.verbosity", "4");

        props.setProperty("jacorb.trailingspace.test1", "INFO ");
        props.setProperty("jacorb.trailingspace.test2", "INFO");

        Environment.addProperties(props);

        factory = new LogKitLoggerFactory();
    }


    public void testGetPriorityForNamedLogger() throws Exception
    {
        assertEquals(defaultPriority, factory.getPriorityForNamedLogger("foologger"));

        assertEquals(2, factory.getPriorityForNamedLogger("jacorb"));

        assertEquals(2, factory.getPriorityForNamedLogger("jacorb.other_component"));

        assertEquals(2, factory.getPriorityForNamedLogger("jacorb.other_component.sub"));


        assertEquals(3, factory.getPriorityForNamedLogger("jacorb.component"));

        assertEquals(3, factory.getPriorityForNamedLogger("jacorb.component.subcomponent2"));

        assertEquals(4, factory.getPriorityForNamedLogger("jacorb.component.subcomponent"));

        assertEquals(4, factory.getPriorityForNamedLogger("jacorb.component.subcomponent.sub"));

        assertEquals(factory.getPriorityForNamedLogger("jacorb.trailingspace.test1"),
                     factory.getPriorityForNamedLogger("jacorb.trailingspace.test2"));
    }


    public static TestSuite suite()
    {
        TestSuite suite = new TestSuite(LogKitLoggerFactoryTest.class);

        return suite;
    }


    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

}
