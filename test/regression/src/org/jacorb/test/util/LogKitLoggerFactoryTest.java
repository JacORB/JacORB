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

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.avalon.framework.logger.Logger;
import org.jacorb.config.Configuration;

/**
 *  Unit Test for class LogKitLoggerFactory
 * @author Alphonse Bendt
 * @version $Id$
 */

public class LogKitLoggerFactoryTest 
    extends TestCase
{
    ////////////////////////////////////////

    private Configuration config;
    private int defaultPriority = 0;

    ////////////////////////////////////////

    public LogKitLoggerFactoryTest(String name)
    {
        super(name);
    }

    ////////////////////////////////////////

    public void setUp()
        throws Exception
    {
        Properties props = new Properties();

        props.setProperty("jacorb.log.verbosity", "2");
        props.setProperty("jacorb.component.log.verbosity", "3");
        props.setProperty("jacorb.component.subcomponent.log.verbosity", "4");

        props.setProperty("jacorb.trailingspace.test1", "INFO ");
        props.setProperty("jacorb.trailingspace.test2", "INFO");

        config =  Configuration.getConfiguration(props, null, false);

        defaultPriority = config.getAttributeAsInteger("jacorb.log.default.verbosity",0);
    }

    private int priorityFor(Logger l)
    {
        if (l.isDebugEnabled())
            return 4;
        else if (l.isInfoEnabled())
            return 3;
        else if (l.isWarnEnabled())
            return 2;
        else if (l.isErrorEnabled())
            return 1;
        return 0;
    }

    public void testGetPriorityForNamedLogger() 
        throws Exception
    {
        assertEquals(defaultPriority, priorityFor(config.getNamedLogger("foologger")));

        assertEquals(2, priorityFor(config.getNamedLogger("jacorb")));

        assertEquals(2, priorityFor(config.getNamedLogger("jacorb.other_component")));

        assertEquals(2, priorityFor(config.getNamedLogger("jacorb.other_component.sub")));


        assertEquals(3, priorityFor(config.getNamedLogger("jacorb.component")));

        assertEquals(3, priorityFor(config.getNamedLogger("jacorb.component.subcomponent2")));

        assertEquals(4, priorityFor(config.getNamedLogger("jacorb.component.subcomponent")));

        assertEquals(4, priorityFor(config.getNamedLogger("jacorb.component.subcomponent.sub")));

        assertEquals(priorityFor(config.getNamedLogger("jacorb.trailingspace.test1")),
                     priorityFor(config.getNamedLogger("jacorb.trailingspace.test2")));
    }


    public static TestSuite suite()
    {
        TestSuite suite = new TestSuite(LogKitLoggerFactoryTest.class);

        return suite;
    }
}
