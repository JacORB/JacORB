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

package org.jacorb.test.bugs.bugjac511;

import java.util.Properties;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.common.CallbackTestCase;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.UserException;
import org.omg.Messaging.ExceptionHolder;
import bugjac511a.bugjac511b.AMI_BugJac511ServerHandler;
import bugjac511a.bugjac511b.AMI_BugJac511ServerHandlerOperations;
import bugjac511a.bugjac511b.AMI_BugJac511ServerHandlerPOATie;
import bugjac511a.bugjac511b.BugJac511Server;
import bugjac511a.bugjac511b.BugJac511ServerHelper;
import bugjac511a.bugjac511b.TestException;
import bugjac511a.bugjac511b._BugJac511ServerStub;

/**
 * @author Alphonse Bendt
 */
public class BugJac511Test extends CallbackTestCase
{
    private class ReplyHandler
        extends CallbackTestCase.ReplyHandler
        implements AMI_BugJac511ServerHandlerOperations
    {
        public void request_sending_exception()
        {
            wrong_reply("request_sending_exception");
        }

        public void request_sending_exception_excep(ExceptionHolder excep_holder)
        {
            wrong_exception("request_sending_exception_excep", excep_holder);
        }
    }

    private BugJac511Server server;

    public BugJac511Test(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    protected void setUp() throws Exception
    {
        server = BugJac511ServerHelper.narrow(setup.getServerObject());
    }

    public static Test suite()
    {
        Properties props = new Properties();

        TestSuite suite = new TestSuite(BugJac511Test.class.getName());
        ClientServerSetup setup = new ClientServerSetup(suite, BugJac511ServerImpl.class.getName(), props, props);
        TestUtils.addToSuite(suite, setup, BugJac511Test.class);
        return setup;
    }

    public void testExceptionDuringAMIInvocation() throws Exception
    {
        ReplyHandler handler = new ReplyHandler()
        {
            public void request_sending_exception_excep(ExceptionHolder excep_holder)
            {
                try
                {
                    excep_holder.raise_exception();
                    fail("should raise exception");
                }
                catch (TestException e)
                {
                    pass();
                }
                catch(UserException e)
                {
                    fail("unexpected exception: " + e);
                }
            }
        };
        AMI_BugJac511ServerHandler handlerRef = ref(handler);
        ((_BugJac511ServerStub)server).sendc_request_sending_exception(handlerRef);

        handler.wait_for_reply(TestUtils.getMediumTimeout());
    }

    private AMI_BugJac511ServerHandler ref ( AMI_BugJac511ServerHandlerOperations handler )
    {
        AMI_BugJac511ServerHandlerPOATie tie =
            new AMI_BugJac511ServerHandlerPOATie( handler )
            {
                public org.omg.CORBA.portable.OutputStream
                    _invoke( String method,
                             org.omg.CORBA.portable.InputStream _input,
                             org.omg.CORBA.portable.ResponseHandler handler )
                    throws org.omg.CORBA.SystemException
                {
                    try
                    {
                        return super._invoke( method, _input, handler );
                    }
                    catch( AssertionFailedError e )
                    {
                        return null;
                    }
                }
            };
        return tie._this( setup.getClientOrb() );
    }
}
