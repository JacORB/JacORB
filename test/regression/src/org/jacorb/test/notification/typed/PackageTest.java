package org.jacorb.test.notification.typed;

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

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class PackageTest extends TestCase
{
    public PackageTest(String name)
    {
        super(name);
    }

    public static TestSuite suite() throws Exception
    {
        TestSuite suite = new TestSuite("TypedEventChannel Tests");

        suite.addTest(TypedProxyPushConsumerImplTest.suite());
        suite.addTest(TypedProxyPushSupplierImplTest.suite());
        suite.addTest(TypedProxyPullSupplierImplTest.suite());
        suite.addTest(TypedProxyPullConsumerImplTest.suite());
        suite.addTest(TypedConsumerAdminImplTest.suite());
        suite.addTest(TypedSupplierAdminImplTest.suite());
        suite.addTest(TypedEventChannelTest.suite());
        suite.addTest(TypedEventChannelFactoryIntegrationTest.suite());
        suite.addTest(TypedEventChannelIntegrationTest.suite());
        
        return suite;
    }
}
