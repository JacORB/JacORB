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

package org.jacorb.test.notification.common;

import java.util.Properties;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.TestUtils;
import org.jacorb.test.ir.IFRServerSetup;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelFactory;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelFactoryHelper;

/**
 * base class for TypedEventChannel integration tests.
 *
 * @author Alphonse Bendt
 */
public abstract class TypedServerTestCase extends ClientServerTestCase
{
    protected static IFRServerSetup ifrSetup;

    @BeforeClass
    public static void beforeClassSetup() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);

        ifrSetup = new IFRServerSetup(TestUtils.testHome() + "/src/test/idl/TypedNotification.idl", null, null);
        Properties props = new Properties();
        props.setProperty("ORBInitRef.InterfaceRepository", ifrSetup.getRepository().toString());
        setup = new TypedServerTestSetup(props);
    }

    @AfterClass
    public static void afterClassSetup() throws Exception
    {
        if (ifrSetup != null)
        {
            ifrSetup.tearDown();
        }
    }

    /**
     * access the ChannelFactory
     */
    protected final TypedEventChannelFactory getChannelFactory()
    {
        TypedEventChannelFactory channelFactory = TypedEventChannelFactoryHelper.narrow(setup.getServerObject());

        return channelFactory;
    }
}
