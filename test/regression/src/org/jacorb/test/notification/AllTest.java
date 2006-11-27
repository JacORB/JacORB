package org.jacorb.test.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.common.JacORBTestSuite;

/**
 * @jacorb-since cvs
 * @author Alphonse Bendt
 */

public class AllTest extends JacORBTestSuite
{
    public AllTest(String name)
    {
        super(name);
    }

    public static Test suite() throws Exception
    {
        TestSuite _suite = new AllTest("Notification Service Test Suite");
        _suite.addTest(org.jacorb.test.notification.PackageTest.suite());

        _suite.addTest(org.jacorb.test.notification.container.PackageTest.suite());

        _suite.addTest(org.jacorb.test.notification.node.PackageTest.suite());
        _suite.addTest(org.jacorb.test.notification.util.PackageTest.suite());
        _suite.addTest(org.jacorb.test.notification.queue.PackageTest.suite());
        _suite.addTest(org.jacorb.test.notification.evaluate.PackageTest.suite());
        _suite.addTest(org.jacorb.test.notification.engine.PackageTest.suite());
        _suite.addTest(org.jacorb.test.notification.bugs.PackageTest.suite());
        _suite.addTest(org.jacorb.test.notification.filter.PackageTest.suite());
        _suite.addTest(org.jacorb.test.notification.servant.PackageTest.suite());
        _suite.addTest(org.jacorb.test.notification.lifecycle.PackageTest.suite());
        _suite.addTest(org.jacorb.test.notification.typed.PackageTest.suite());

        return _suite;
    }

    public static void main(String[] args) throws Exception
    {
        junit.textui.TestRunner.run(suite());
    }
}