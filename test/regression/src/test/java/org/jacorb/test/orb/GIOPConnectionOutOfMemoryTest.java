package org.jacorb.test.orb;

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

import java.io.IOException;
import java.util.Properties;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMRules;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.omg.CORBA.COMM_FAILURE;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertTrue;

/**
 * @author Nick Cross
 *
 * Verify the GIOPConnection::receiveMessagesLoop handles OutOfMemory. This
 * test uses ByteMan to inject an OutOfMemory condition in the code where
 * it was reported:
 * <pre>
 * Exception in thread "ClientMessageReceptor0" java.lang.OutOfMemoryError: Java heap space
 *	at java.util.Arrays.copyOf(Arrays.java:2271)
 *	at java.io.ByteArrayOutputStream.grow(ByteArrayOutputStream.java:113)
 *	at java.io.ByteArrayOutputStream.ensureCapacity(ByteArrayOutputStream.java:93)
 *	at java.io.ByteArrayOutputStream.write(ByteArrayOutputStream.java:140)
 *	at org.jacorb.orb.giop.GIOPConnection.receiveMessagesLoop(GIOPConnection.java:641)
 *</pre>
 * This then allows verification that the code can handle the fault condition and the client
 * receives an exception and doesn't hang.
 *
 */
@RunWith(BMUnitRunner.class)
public class GIOPConnectionOutOfMemoryTest extends ClientServerTestCase
{
    private BasicServer server;

    @Before
    public void setUp() throws Exception
    {
        server = BasicServerHelper.narrow( setup.getServerObject() );
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Properties client_props = new Properties();
        client_props.setProperty
            ("jacorb.connection.client.pending_reply_timeout", "5000");

        setup = new ClientServerSetup (
            "org.jacorb.test.orb.BasicServerImpl",
            client_props,
            null);

    }

    @Test(expected=COMM_FAILURE.class)
    @BMRule(name="outofmemory-injection",
        targetClass="GIOPConnection",
        targetMethod="getMessage()",
        targetLocation = "AT EXIT",
        action="throw new java.lang.OutOfMemoryError(\"OutOfMemory\")")
    public void testOutOfMemory() throws Exception
    {
        server.bounce_short((short) 14);
    }

    @Test
    @BMRule(name = "outofmemory-injection",
            targetClass = "GIOPConnection",
            targetMethod = "getMessage()",
            targetLocation = "AT EXIT",
            action = "throw new java.lang.OutOfMemoryError(\"OutOfMemory\")")
    public void testOutOfMemoryCaused() throws Exception
    {
        try
        {
            server.bounce_short((short) 14);
            fail("Should have thrown an error.");
        }
        catch(COMM_FAILURE ex)
        {
            assertTrue (ex.getCause() instanceof OutOfMemoryError);
        }
    }


    @Test(expected=COMM_FAILURE.class)
    @BMRule(name="outofmemory-injection",
        targetClass="GIOPConnection",
        targetMethod="getMessage()",
        targetLocation = "AT EXIT",
        action="throw new org.omg.CORBA.NO_MEMORY(\"OutOfMemory\")")
    public void testNoMemory() throws Exception
    {
        server.bounce_short( ( short ) 14 );
    }

    @Test
    @BMRules ( rules = {
        @BMRule(name = "toggle-msg-type",
            targetClass="Messages",
            targetMethod="getMsgType",
            targetLocation = "AT ENTRY",
            condition = "callerEquals(\"GIOPConnection.receiveMessagesLoop\", true)",
            action = "return 7" ),
        @BMRule(name = "toggle-minor-type",
                    targetClass="Messages",
                    targetMethod="getGIOPMinor",
                    targetLocation = "AT ENTRY",
                    condition = "callerEquals(\"GIOPConnection.receiveMessagesLoop\", true)",
                    action = "return 0" ),
        @BMRule(name="ioexception-injection",
            targetClass="GIOPConnection",
            targetMethod="sendMessage",
            targetLocation = "AT ENTRY",
            condition = "callerEquals(\"GIOPConnection.receiveMessagesLoop\", true)",
            action="throw new java.io.IOException(\"DummyException\")")
    })
    public void testCapturedIOException() throws Exception
    {
        try
        {
            server.bounce_short((short) 14);
            fail("Should have thrown an error.");
        }
        catch(COMM_FAILURE ex)
        {
            assertTrue (ex.getCause() instanceof IOException);
        }
    }
}
