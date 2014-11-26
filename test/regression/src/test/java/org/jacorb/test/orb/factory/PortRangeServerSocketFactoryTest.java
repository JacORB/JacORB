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

package org.jacorb.test.orb.factory;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import org.easymock.MockControl;
import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.orb.factory.PortRangeServerSocketFactory;
import org.jacorb.orb.factory.ServerSocketFactory;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

public class PortRangeServerSocketFactoryTest
{
    private PortRangeServerSocketFactory objectUnderTest;

    private MockControl configurationControl;
    private Configuration configurationMock;
    private MockControl factoryDelegateControl;
    private ServerSocketFactory factoryDelegateMock;
    private MockControl loggerControl;
    private Logger loggerMock;

    @Before
    public void setUp() throws Exception
    {
        configurationControl = MockControl.createControl(Configuration.class);
        configurationMock = (Configuration) configurationControl.getMock();

        factoryDelegateControl = MockControl.createControl(ServerSocketFactory.class);
        factoryDelegateMock = (ServerSocketFactory) factoryDelegateControl.getMock();

        loggerControl = MockControl.createNiceControl(Logger.class);
        loggerMock = (Logger) loggerControl.getMock();

        configurationMock.getLogger(null);
        configurationControl.setMatcher(MockControl.ALWAYS_MATCHER);
        configurationControl.setReturnValue(loggerMock);

        objectUnderTest = new PortRangeServerSocketFactory(factoryDelegateMock);
    }

    @Test
    public void testFactoryIsConfigurable()
    {
        assertTrue(objectUnderTest instanceof Configurable);
    }

    /*
     * Test method for 'org.jacorb.orb.factory.PortRangeServerSocketFactory.createServerSocket(int,
     * int)'
     */
    @Test
    public void testCreateServerSocketIntInt() throws Exception
    {

        factoryDelegateMock.createServerSocket(5, 10);
        factoryDelegateControl.setReturnValue(new ServerSocket());

        configurationControl.expectAndReturn(configurationMock.getAttributeAsInteger(PortRangeServerSocketFactory.MIN_PROP), 1);
        configurationControl.expectAndReturn(configurationMock.getAttributeAsInteger(PortRangeServerSocketFactory.MAX_PROP), 10);

        replayAll();

        objectUnderTest.configure(configurationMock);
        objectUnderTest.createServerSocket(5, 10);

        verifyAll();
    }

    /*
     * Test method for 'org.jacorb.orb.factory.PortRangeServerSocketFactory.createServerSocket(int,
     * int, InetAddress)'
     */
    @Test
    public void testCreateServerSocketIntIntInetAddress() throws Exception
    {
        final InetAddress localhost = InetAddress.getByName("localhost");
        factoryDelegateMock.createServerSocket(5, 10, localhost);
        factoryDelegateControl.setReturnValue(new ServerSocket());

        configurationControl.expectAndReturn(configurationMock.getAttributeAsInteger(PortRangeServerSocketFactory.MIN_PROP), 1);
        configurationControl.expectAndReturn(configurationMock.getAttributeAsInteger(PortRangeServerSocketFactory.MAX_PROP), 10);

        replayAll();

        objectUnderTest.configure(configurationMock);
        objectUnderTest.createServerSocket(5, 10, localhost);

        verifyAll();
    }

    @Test
    public void testCreateServerSocketIntPortWithinBounds() throws Exception
    {
        factoryDelegateMock.createServerSocket(5);
        factoryDelegateControl.setReturnValue(new ServerSocket());

        configurationControl.expectAndReturn(configurationMock.getAttributeAsInteger(PortRangeServerSocketFactory.MIN_PROP), 1);
        configurationControl.expectAndReturn(configurationMock.getAttributeAsInteger(PortRangeServerSocketFactory.MAX_PROP), 10);

        replayAll();

        objectUnderTest.configure(configurationMock);
        objectUnderTest.createServerSocket(5);

        verifyAll();
    }

    @Test
    public void testCreateServerSocketIntPortOutsideBounds() throws Exception
    {
        factoryDelegateMock.createServerSocket(1);
        factoryDelegateControl.setReturnValue(new ServerSocket());

        configurationControl.expectAndReturn(configurationMock.getAttributeAsInteger(PortRangeServerSocketFactory.MIN_PROP), 1);
        configurationControl.expectAndReturn(configurationMock.getAttributeAsInteger(PortRangeServerSocketFactory.MAX_PROP), 10);

        replayAll();

        objectUnderTest.configure(configurationMock);
        objectUnderTest.createServerSocket(15);

        verifyAll();
    }

    @Test
    public void testCreateFailsIfCannotFindFreePort() throws Exception
    {
        factoryDelegateMock.createServerSocket(1);
        factoryDelegateControl.setMatcher(MockControl.ALWAYS_MATCHER);
        factoryDelegateControl.setThrowable(new IOException(), 5, 6);

        configurationControl.expectAndReturn(configurationMock.getAttributeAsInteger(PortRangeServerSocketFactory.MIN_PROP), 1);
        configurationControl.expectAndReturn(configurationMock.getAttributeAsInteger(PortRangeServerSocketFactory.MAX_PROP), 5);

        replayAll();

        objectUnderTest.configure(configurationMock);

        try
        {
            objectUnderTest.createServerSocket(1);
            fail();
        } catch (BindException e)
        {
            // expected
        }
        verifyAll();
    }

    private void replayAll()
    {
        factoryDelegateControl.replay();
        configurationControl.replay();
        loggerControl.replay();
    }

    private void verifyAll()
    {
        factoryDelegateControl.verify();
        configurationControl.verify();
        loggerControl.verify();
    }
}
