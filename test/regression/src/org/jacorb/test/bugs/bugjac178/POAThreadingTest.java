package org.jacorb.test.bugs.bugjac178;

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

import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;

/**
 * <code>POAThreadingTest</code> is a test for JAC178; test interleaving
 * of calls in single and multiple thread child POAs
 *
 * @author Nick Cross
 * @version $Id$
 */
public class POAThreadingTest extends ClientServerTestCase
{
    /**
     * <code>server</code> is the server reference.
     */
    private JAC178 server;

    /**
     * Creates a new <code>POAThreadingTest</code> instance.
     *
     * @param name a <code>String</code> value
     * @param setup a <code>ClientServerSetup</code> value
     */
    public POAThreadingTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    /**
     * <code>setUp</code> is the JUnit setup code.
     *
     * @exception Exception if an error occurs
     */
    public void setUp() throws Exception
    {
        server = JAC178Helper.narrow( setup.getServerObject() );
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite( "JAC178 client/server tests" );

        ClientServerSetup setup = new ClientServerSetup( suite, "org.jacorb.test.bugs.bugjac178.JAC178Impl");

        TestUtils.addToSuite(suite, setup, POAThreadingTest.class);

        return setup;
    }


    /**
     * <code>test_single</code> tests the single thread child poa.
     */
    public void test_single() throws Exception
    {
        String[] strings = runThreadTest("Single");

        verifyResult(true, strings);
    }

    private void verifyResult(boolean checkSingleThreadedExcution, String[] strings)
    {
        assertEquals(0, strings.length % 2);

        boolean beginLongOpSeen = false;
        HashSet beginShortOpSeen = new HashSet();

        for (int x=0; x < strings.length; ++x)
        {
            if (strings[x].startsWith("begin-longOp"))
            {
                assertFalse("there should only be one begin-longOp", beginLongOpSeen);
                beginLongOpSeen = true;
            }
            else if (strings[x].startsWith("end-longOp"))
            {
                assertTrue("end should be after begin", beginLongOpSeen);
            }
            else if (strings[x].startsWith("begin-shortOp"))
            {
                final String opID = strings[x].substring(12);
                assertTrue(!checkSingleThreadedExcution || beginShortOpSeen.isEmpty());
                assertFalse(beginShortOpSeen.contains(opID));
                beginShortOpSeen.add(opID);
            }
            else if (strings[x].startsWith("end-shortOp"))
            {
                assertNotNull(beginShortOpSeen);
                final String opID = strings[x].substring(10);
                assertTrue(beginShortOpSeen.contains(opID));
                beginShortOpSeen.remove(opID);
            }
            else
            {
                throw new IllegalArgumentException();
            }
        }
    }


    /**
     * <code>test_multiple</code> tests the multiple thread child poa.
     */
    public void test_multiple() throws Exception
    {
        String[] result = runThreadTest("Multiple");

        verifyResult(false, result);
    }

    /**
     * <code>runThreadTest</code> is the internal code used to test the poas.
     *
     * @param prefix a <code>String</code> value which is the sessionID prefix passed
     *               to getObject. This denotes to the server whether to create a
     *               child POA with single thread or multiple thread policy.
     * @return an <code>int</code> value
     */
    private String[] runThreadTest (String prefix) throws Exception
    {
        org.omg.CORBA.Object childObj1 = server.getObject (prefix + "Session1");
        org.omg.CORBA.Object childObj2 = server.getObject (prefix + "Session2");

        final JAC178 child1 = JAC178Helper.narrow (childObj1);
        final JAC178 child2 = JAC178Helper.narrow (childObj2);

        final Exception[] exceptions = new Exception[3];

        // Create a thread.
        Thread thread1 = new Thread("LongOpThread")
        {
            public void run()
            {
                try
                {
                    child1.longOp ();
                }
                catch (Exception e)
                {
                    exceptions[0] = e;
                }
            }
        };

        // Create a thread.
        Thread thread2 = new Thread("ShortOpThread")
        {
            public void run()
            {
                try
                {
                    child2.shortOp ("1");
                }
                catch (Exception e)
                {
                    exceptions[1] = e;
                }
            }
        };
        Thread thread3 = new Thread("ShortOpThread2")
        {
            public void run()
            {
                try
                {
                    child2.shortOp ("2");
                }
                catch (Exception e)
                {
                    exceptions[2] = e;
                }
            }
        };

        assertNull(exceptions[0]);
        assertNull(exceptions[1]);
        assertNull(exceptions[2]);

        thread1.start();
        thread2.start();
        thread3.start();
        child2.shortOp ("3");

        // Wait for everything to complete...
        thread1.join();
        thread2.join();
        thread3.join();

        // Get the result...
        String resultStr = server.getResult().replaceAll("\\[|\\]", "");

        return resultStr.split("\\s*,\\s*");
    }
}
