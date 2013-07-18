package org.jacorb.test.bugs.bug956;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2013 Gerald Brose / The JacORB Team.
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

import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ORBTestCase;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.PortableServer.POAManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Properties;

import org.omg.BiDirPolicy.BIDIRECTIONAL_POLICY_TYPE;
import org.omg.BiDirPolicy.BOTH;
import org.omg.BiDirPolicy.BidirectionalPolicyValueHelper;
import org.omg.CORBA.Any;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Properties;

import org.omg.BiDirPolicy.BIDIRECTIONAL_POLICY_TYPE;
import org.omg.BiDirPolicy.BOTH;
import org.omg.BiDirPolicy.BidirectionalPolicyValueHelper;
import org.omg.CORBA.Any;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;


/**
 * @author Nick Cross
 *
 * Verify built-in interceptors work with local calls.
 */
public class LocalInterceptorTest extends ORBTestCase
{
    protected  void patchORBProperties(String testName, Properties props) throws Exception
    {
        props.put ("org.omg.PortableInterceptor.ORBInitializerClass.bidir_init",
                   "org.jacorb.orb.giop.BiDirConnectionInitializer");
    }

    public void testLocalCall() throws Exception
    {
        POA bidir_poa;
        Server server;

        Any any = orb.create_any ();
        BidirectionalPolicyValueHelper.insert (any, BOTH.value);

        Policy[] policies = new Policy[4];
        policies[0] = rootPOA.create_lifespan_policy (LifespanPolicyValue.TRANSIENT);

        policies[1] = rootPOA.create_id_assignment_policy (IdAssignmentPolicyValue.SYSTEM_ID);

        policies[2] = rootPOA.create_implicit_activation_policy (ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION);

        policies[3] = orb.create_policy (BIDIRECTIONAL_POLICY_TYPE.value, any);

        bidir_poa = rootPOA.create_POA ("BiDirPOA",
                                         rootPOA.the_POAManager (),
                                         policies);

        bidir_poa.the_POAManager ().activate ();

        org.omg.CORBA.Object o = bidir_poa.servant_to_reference (new ServerImpl ());
        server = ServerHelper.narrow (o);

        ClientCallback ccb = ClientCallbackHelper.narrow (bidir_poa.servant_to_reference (new Client ()));

        server.register_callback (ccb);

        server.callback_hello ("A test string");
    }
}


class Client extends ClientCallbackPOA
{
    public void hello (String message)
    {
        System.out.println ("Client callback object received hello message >"
                + message + '<');
    }
}

class ServerImpl extends ServerPOA
{
    private ClientCallback ccb = null;

    public void register_callback (ClientCallback ccb)
    {
        this.ccb = ccb;
    }

    public void callback_hello (String message)
    {
        System.out.println ("Server object received hello message >" + message
                + '<');

        ccb.hello (message);
    }
}
