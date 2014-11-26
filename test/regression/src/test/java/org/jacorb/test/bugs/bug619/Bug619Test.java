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

package org.jacorb.test.bugs.bug619;

import static org.junit.Assert.fail;
import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.IMRExcludedClientServerCategory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.omg.CORBA.NO_MEMORY;

@Category(IMRExcludedClientServerCategory.class)
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

        @Override
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
            }
            catch (Exception e)
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
                    }
                    catch (InterruptedException e)
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

    @Test
    public void testOutOfMemoryShouldFail() throws Exception
    {
        int[] data = new int[10000000];

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
            pusher.join();
            Thread.sleep(5000);
        }
        // Try a succesful run.
        pusher = new Pusher(new int [1]);
        pusher.run();

        // Try another failure run.
        pusher = new Pusher(data);
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

    @Before
    public void setUp() throws Exception
    {
        server = OutOfMemoryHelper.narrow(setup.getServerObject());
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Properties clientProps = new Properties();
        Properties serverProps = new Properties();

        serverProps.put("jacorb.test.maxheapsize", "64m");

        setup = new ClientServerSetup(OutOfMemoryImpl.class.getName(),
                clientProps, serverProps);
    }
}
