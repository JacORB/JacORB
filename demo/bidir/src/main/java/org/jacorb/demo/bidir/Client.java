package org.jacorb.demo.bidir;

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
 * Client.java
 *
 *
 * Created: Mon Sep 3 19:28:34 2001
 *
 * @author Nicolas Noffke
 */

public class Client extends ClientCallbackPOA
{
    public Client ()
    {
    }

    public void hello (String message)
    {
        System.out.println ("Client callback object received hello message >"
                + message + '<');
    }

    public static void main (String[] args) throws Exception
    {
        Properties props = new Properties ();
        props.put ("org.omg.PortableInterceptor.ORBInitializerClass.bidir_init",
                   "org.jacorb.orb.giop.BiDirConnectionInitializer");

        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);

        BufferedReader br = new BufferedReader (new FileReader (args[0]));

        org.omg.CORBA.Object o = orb.string_to_object (br.readLine ());

        Server server = ServerHelper.narrow (o);

        Any any = orb.create_any ();
        BidirectionalPolicyValueHelper.insert (any, BOTH.value);

        POA root_poa = (POA) orb.resolve_initial_references ("RootPOA");

        Policy[] policies = new Policy[4];
        policies[0] = root_poa.create_lifespan_policy (LifespanPolicyValue.TRANSIENT);

        policies[1] = root_poa.create_id_assignment_policy (IdAssignmentPolicyValue.SYSTEM_ID);

        policies[2] = root_poa.create_implicit_activation_policy (ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION);

        policies[3] = orb.create_policy (BIDIRECTIONAL_POLICY_TYPE.value, any);

        POA bidir_poa = root_poa.create_POA ("BiDirPOA",
                                             root_poa.the_POAManager (),
                                             policies);
        bidir_poa.the_POAManager ().activate ();

        ClientCallback ccb = ClientCallbackHelper.narrow (bidir_poa.servant_to_reference (new Client ()));

        server.register_callback (ccb);

        server.callback_hello ("A test string");

        server.shutdown();
    }
}// Client
