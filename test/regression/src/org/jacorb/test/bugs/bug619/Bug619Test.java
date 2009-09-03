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

package org.jacorb.test.bugs.bug619;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.CommonSetup;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.NO_MEMORY;

public class Bug619Test extends ClientServerTestCase
{
    private OutOfMemory server;

    private class Pusher extends Thread
    {
        private boolean success;

        private Exception exception;

        private final int[] data;

        public Pusher(int[] data)
        {
            this.data = data;
        }

        public void run()
        {
            try
            {
                server.push(data);
                synchronized (this)
                {
                    success = true;
                    notifyAll();
                }
            } catch (Exception e)
            {
                synchronized (this)
                {
                    exception = e;
                    notifyAll();
                }
            }
        }

        public void verify(long timeout) throws Exception
        {
            long waitUntil = System.currentTimeMillis() + timeout;

            synchronized (this)
            {
                while (!success && exception == null && System.currentTimeMillis() < waitUntil)
                {
                    try
                    {
                        wait(timeout);
                    } catch (InterruptedException e)
                    {
                        // ignore
                    }
                }

                if (exception != null)
                {
                    throw exception;
                }

                if (!success)
                {
                    throw new RuntimeException("No response within " + timeout);
                }
            }
        }
    }

    public Bug619Test(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public void testOutOfMemoryShouldFail() throws Exception
    {
        int[] data = new int[10000000];
        for (int i = 0; i < 10; i++)
        {
            Pusher pusher = new Pusher(data);
            pusher.run();

            try
            {
                pusher.verify(2000);
                fail();
            } 
            catch (NO_MEMORY e)
            {
                // expected
            }
            finally
            {
                pusher.interrupt();
            }
       }
    }

    protected void setUp() throws Exception
    {
        server = OutOfMemoryHelper.narrow(setup.getServerObject());
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        Properties clientProps = new Properties();
        Properties serverProps = new Properties();
        serverProps.put("jacorb.test.maxmemory", "64m");
        serverProps.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_IMR, "true");
        clientProps.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_IMR, "true");

        ClientServerSetup setup = new ClientServerSetup(suite, OutOfMemoryImpl.class.getName(),
                clientProps, serverProps);

        TestUtils.addToSuite(suite, setup, Bug619Test.class);

        return setup;
    }
}
