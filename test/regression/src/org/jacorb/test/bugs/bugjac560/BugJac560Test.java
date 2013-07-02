/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

package org.jacorb.test.bugs.bugjac560;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.omg.Messaging.ExceptionHolder;
import cerent.cms.idl.Node.AMI_BugJac560ServiceHandlerPOA;
import cerent.cms.idl.Node.BugJac560Service;
import cerent.cms.idl.Node.BugJac560ServiceHelper;
import cerent.cms.idl.Node._BugJac560ServiceStub;
import cerent.cms.idl.Node.xNoSuchDefault;

/**
 * @author Alphonse Bendt
 */
public class BugJac560Test extends ClientServerTestCase
{
    private BugJac560Service server;

    public BugJac560Test(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite(BugJac560Test.class.getName());
        ClientServerSetup setup = new ClientServerSetup
            (suite, BugJac560ServiceImpl.class.getName(), null, null);
        TestUtils.addToSuite(suite, setup, BugJac560Test.class);

        return setup;
    }

    protected void setUp() throws Exception
    {
        server = BugJac560ServiceHelper.narrow(setup.getServerObject());
    }

    public void testNoException() throws Exception
    {
        server.exc(false);
    }

    public void testException()
    {
        try
        {
            server.exc(true);
            fail();
        }
        catch(xNoSuchDefault e)
        {
        }
    }


    class MyHandler extends AMI_BugJac560ServiceHandlerPOA
    {
        private boolean done;
        private Exception exception;

        public synchronized void exc()
        {
            done = true;
            notifyAll();
        }

        public synchronized void exc_excep(ExceptionHolder excep_holder)
        {
            done = true;
            try
            {
                excep_holder.raise_exception();
            }
            catch(Exception e)
            {
                exception = e;
            }

            notifyAll();
        }

        private synchronized void untilDone(long timeout)
        {
            long until = System.currentTimeMillis() + timeout;
            while(!done && System.currentTimeMillis() < until)
            {
                try
                {
                    wait();
                }
                catch (InterruptedException e)
                {
                }
            }
        }

        public boolean isDone(long timeout)
        {
            untilDone(timeout);
            return done;
        }

        public Exception getException(long timeout)
        {
            untilDone(timeout);
            return exception;
        }
    }

    public void testAMINoException()
    {
        MyHandler handler = new MyHandler();

        ((_BugJac560ServiceStub)server).sendc_exc(handler._this(setup.getClientOrb()), false);

        assertTrue(handler.isDone(TestUtils.getMediumTimeout()));
        assertNull(handler.getException(0));
    }

    public void testAMIWithException()
    {
        MyHandler handler = new MyHandler();

        ((_BugJac560ServiceStub)server).sendc_exc(handler._this(setup.getClientOrb()), true);

        assertTrue(handler.isDone(TestUtils.getMediumTimeout()));
        final Exception exception = handler.getException(0);
        assertEquals(xNoSuchDefault.class, exception.getClass());
    }
}
