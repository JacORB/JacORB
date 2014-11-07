/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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

package org.jacorb.test.bugs.bugjac417;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.util.Properties;
import org.easymock.MockControl;
import org.jacorb.config.Configuration;
import org.jacorb.orb.factory.DefaultServerSocketFactory;
import org.jacorb.orb.factory.SocketFactoryManager;
import org.jacorb.orb.listener.NullTCPConnectionListener;
import org.jacorb.test.harness.ORBTestCase;
import org.jacorb.test.harness.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.INITIALIZE;

/**
 * @author Alphonse Bendt
 */
public class BugJac417Test extends ORBTestCase
{
    private SocketFactoryManager objectUnderTest;
    private MockControl configControl;
    private Configuration configMock;

    @Override
    protected void patchORBProperties(Properties properties)
    {
        properties.setProperty(SocketFactoryManager.SUPPORT_SSL, "false");
    }

    @Before
    public void setUp() throws Exception
    {
        objectUnderTest = new SocketFactoryManager();
        configControl = MockControl.createNiceControl(Configuration.class);
        configMock = (Configuration) configControl.getMock();

        configControl.expectAndReturn(configMock.getORB(), orb );
        configControl.expectAndReturn(configMock.getLogger("org.jacorb.orb.factory"), TestUtils.getLogger() );
        configControl.expectAndReturn(configMock.getAttributeAsObject(SocketFactoryManager.TCP_LISTENER, NullTCPConnectionListener.class.getName()), new NullTCPConnectionListener());
        configControl.expectAndReturn(configMock.getAttributeAsBoolean(SocketFactoryManager.SUPPORT_SSL, false), false);
    }

    @Test
    public void testNoArg() throws Exception
    {
        configControl.expectAndReturn(configMock.getAttribute(SocketFactoryManager.SOCKET_FACTORY, ""), SocketFactoryNoArgsCtor.class.getName());
        configControl.expectAndReturn(configMock.getAttribute(SocketFactoryManager.SERVER_SOCKET_FACTORY, DefaultServerSocketFactory.class.getName()), SocketFactoryNoArgsCtor.class.getName());

        configControl.replay();

        objectUnderTest.configure(configMock);

        assertEquals(SocketFactoryNoArgsCtor.class, objectUnderTest.getSocketFactory().getClass());

        configControl.verify();
    }

    @Test
    public void testOMGArg() throws Exception
    {
        configControl.expectAndReturn(configMock.getAttribute(SocketFactoryManager.SOCKET_FACTORY, ""), SocketFactoryOMG.class.getName());
        configControl.expectAndReturn(configMock.getAttribute(SocketFactoryManager.SERVER_SOCKET_FACTORY, DefaultServerSocketFactory.class.getName()), SocketFactoryNoArgsCtor.class.getName());

        configControl.replay();

        objectUnderTest.configure(configMock);

        assertEquals(SocketFactoryOMG.class, objectUnderTest.getSocketFactory().getClass());
        assertEquals(orb, ((SocketFactoryOMG)objectUnderTest.getSocketFactory()).orb );

        configControl.verify();
    }

    @Test
    public void testJacORBArg() throws Exception
    {
        configControl.expectAndReturn(configMock.getAttribute(SocketFactoryManager.SOCKET_FACTORY, ""), SocketFactoryJacORB.class.getName());
        configControl.expectAndReturn(configMock.getAttribute(SocketFactoryManager.SERVER_SOCKET_FACTORY, DefaultServerSocketFactory.class.getName()), SocketFactoryNoArgsCtor.class.getName());

        configControl.replay();

        objectUnderTest.configure(configMock);

        assertEquals(SocketFactoryJacORB.class, objectUnderTest.getSocketFactory().getClass());
        assertEquals(orb, ((SocketFactoryJacORB)objectUnderTest.getSocketFactory()).orb );

        configControl.verify();
    }

    @Test
    public void testConfigurable() throws Exception
    {
        configControl.expectAndReturn(configMock.getAttribute(SocketFactoryManager.SOCKET_FACTORY, ""), ConfigurableFactory.class.getName());
        configControl.expectAndReturn(configMock.getAttribute(SocketFactoryManager.SERVER_SOCKET_FACTORY, DefaultServerSocketFactory.class.getName()), SocketFactoryNoArgsCtor.class.getName());

        configControl.replay();

        objectUnderTest.configure(configMock);

        assertEquals(ConfigurableFactory.class, objectUnderTest.getSocketFactory().getClass());
        assertNotNull(((ConfigurableFactory)objectUnderTest.getSocketFactory()).config );

        configControl.verify();
    }

    @Test
    public void testWrongType() throws Exception
    {
        configControl.expectAndReturn(configMock.getAttribute(SocketFactoryManager.SOCKET_FACTORY, ""), WrongFactory.class.getName());
        configControl.expectAndReturn(configMock.getAttribute(SocketFactoryManager.SERVER_SOCKET_FACTORY, DefaultServerSocketFactory.class.getName()), SocketFactoryNoArgsCtor.class.getName());

        configControl.replay();

        objectUnderTest.configure(configMock);

        try
        {
            objectUnderTest.getSocketFactory();
            fail();
        }
        catch(INITIALIZE e)
        {
            // expected
        }

        configControl.verify();
    }

    @Test
    public void testNoPublicCtor() throws Exception
    {
        configControl.expectAndReturn(configMock.getAttribute(SocketFactoryManager.SOCKET_FACTORY, ""), NoPublicCtor.class.getName());
        configControl.expectAndReturn(configMock.getAttribute(SocketFactoryManager.SERVER_SOCKET_FACTORY, DefaultServerSocketFactory.class.getName()), SocketFactoryNoArgsCtor.class.getName());

        configControl.replay();

        objectUnderTest.configure(configMock);

        try
        {
            objectUnderTest.getSocketFactory();
            fail();
        }
        catch(INITIALIZE e)
        {
            // expected
        }

        configControl.verify();
    }

    @Test
    public void testFailingFactory() throws Exception
    {
        configControl.expectAndReturn(configMock.getAttribute(SocketFactoryManager.SOCKET_FACTORY, ""), FailingFactory.class.getName());
        configControl.expectAndReturn(configMock.getAttribute(SocketFactoryManager.SERVER_SOCKET_FACTORY, DefaultServerSocketFactory.class.getName()), SocketFactoryNoArgsCtor.class.getName());

        configControl.replay();

        objectUnderTest.configure(configMock);

        try
        {
            objectUnderTest.getSocketFactory();
            fail();
        }
        catch(INITIALIZE e)
        {
            // expected
        }

        configControl.verify();
    }
}
