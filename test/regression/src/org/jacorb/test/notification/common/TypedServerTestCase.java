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

package org.jacorb.test.notification.common;

import junit.framework.Test;

import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.ORB;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelFactory;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelFactoryHelper;

/**
 * base class for TypedEventChannel integration tests.
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */
public abstract class TypedServerTestCase extends ClientServerTestCase
{
    public TypedServerTestCase(String name, TypedServerTestSetup setup)
    {
        super(name, setup);
    }

    /**
     * access the ChannelFactory 
     */
    protected final TypedEventChannelFactory getChannelFactory()
    {
        TypedEventChannelFactory channelFactory = TypedEventChannelFactoryHelper.narrow(setup.getServerObject());

        return channelFactory;
    }
    
    protected ORB getClientORB()
    {
        return setup.getClientOrb();
    }
    
    public static Test suite(Class clazz) throws Exception
    {
        return suite(clazz, "TestSuite defined in Class " + clazz.getName());
    }

    public static Test suite(Class clazz, String suiteName) throws Exception
    {
        return suite(clazz, suiteName, "test");
    }

    public static Test suite(Class clazz, String suiteName, String testMethodPrefix)
            throws Exception
    {
        return TestUtils.suite(clazz, TypedServerTestSetup.class, suiteName, testMethodPrefix);
    }
}
