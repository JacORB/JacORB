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

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class PackageTest extends TestCase {

    public PackageTest(String name) {
        super(name);
    }

    public static Test suite() throws Exception {
        TestSuite _suite =
            new TestSuite("Tests in Package org.jacorb.test.notification");

        _suite.addTest(EventChannelFactoryTest.suite());
        _suite.addTest(TimeoutTest.suite());
        _suite.addTest(StopTimeTest.suite());
        _suite.addTest(StartTimeTest.suite());
        _suite.addTest(EventChannelTest.suite());
        _suite.addTest(CosEventChannelTest.suite());
        _suite.addTest(StructuredEventChannelTest.suite());
        _suite.addTest(SequenceEventChannelTest.suite());
        _suite.addTest(InterFilterGroupOperatorTest.suite());
        _suite.addTest(AdminLimitTest.suite());
        _suite.addTest(FilterImplTest.suite());
        _suite.addTest(FilterTest.suite());
        _suite.addTest(MappingFilterTest.suite());
        _suite.addTest(MessageFactoryTest.suite());
        _suite.addTest(MessageUtilsTest.suite());
        _suite.addTest(NotificationEventTest.suite());
        _suite.addTest(CollectionsWrapperTest.suite());
        _suite.addTest(PropertyValidatorTest.suite());
        _suite.addTest(OfferManagerTest.suite());

        return _suite;
    }

    public static void main(String[] args) throws Exception {
        junit.textui.TestRunner.run(suite());
    }
}
