package org.jacorb.test.orb.connection;

import java.util.Properties;

import org.jacorb.test.*;
import org.omg.CORBA.*;
import org.omg.GIOP.*;
import org.omg.BiDirPolicy.*;
import org.omg.PortableServer.*;

/**
 * @author Andre Spiegel
 * @version $Id$
 */
public class BiDirServerImpl extends BiDirServerPOA
{
    private ClientCallback callback = null;

    public void register_callback(ClientCallback cc)
    {
        this.callback = cc;
    }

    public void callback_hello(String message)
    {
        final String msg = message;
        new Thread (new Runnable()
        {
            public void run()
            {
                try
                {
                    Thread.sleep (200);
                }
                catch (InterruptedException ex)
                {
                    // ignore
                }
                callback.hello (msg);
            }
        }).start();
    }

    public int get_open_client_transports()
    {
        return org.jacorb.orb.connection.Client_TCP_IP_Transport.openTransports;
    }

    public static void main (String[] args)
    {
        try
        {
            org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);

            Any any = orb.create_any();
            BidirectionalPolicyValueHelper.insert(any, BOTH.value);

            POA root_poa = (POA) orb.resolve_initial_references("RootPOA");

            Policy[] policies = new Policy[4];
            policies[0] =
                root_poa.create_lifespan_policy(LifespanPolicyValue.TRANSIENT);

            policies[1] =
                root_poa.create_id_assignment_policy(
                    IdAssignmentPolicyValue.SYSTEM_ID);

            policies[2] =
                root_poa.create_implicit_activation_policy(
                    ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION);

            policies[3] =
                orb.create_policy(BIDIRECTIONAL_POLICY_TYPE.value, any);

            POA bidir_poa =
                root_poa.create_POA(
                    "BiDirPOA",
                    root_poa.the_POAManager(),
                    policies);
            bidir_poa.the_POAManager().activate();

            org.omg.CORBA.Object o =
                bidir_poa.servant_to_reference(new BiDirServerImpl());
                
            System.out.println (orb.object_to_string(o));
            System.out.flush();
            
            orb.run();
        }
        catch (Exception e)
        {
            System.out.println ("ERROR: " + e);
        }
        
    }

}
