package org.jacorb.demo.bidir;

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

/**
 * ServerImpl.java
 *
 *
 * Created: Mon Sep 3 19:28:34 2001
 *
 * @author Nicolas Noffke
 */

public class ServerImpl extends ServerPOA
{
    private ClientCallback ccb = null;
    private boolean shutdown = false;

    public ServerImpl ()
    {
    }

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


    public void shutdown ()
    {
        shutdown = true;
    }

    public static void main (String[] args) throws Exception
    {
        Properties props = new Properties ();
        props.put ("org.omg.PortableInterceptor.ORBInitializerClass.bidir_init",
                   "org.jacorb.orb.giop.BiDirConnectionInitializer");

        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);

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

        ServerImpl s = new ServerImpl();

        org.omg.CORBA.Object o = bidir_poa.servant_to_reference (s);

        PrintWriter ps = new PrintWriter (new FileOutputStream (new File (args[0])));
        ps.println (orb.object_to_string (o));
        ps.close ();

        if (args.length == 1)
        {
            while ( ! s.shutdown )
            {
                Thread.sleep(1000);
            }
            orb.shutdown(true);
        }
        else
        {
            orb.run ();
        }
    }
}
