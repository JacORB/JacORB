package org.jacorb.test.orb.connection;

import org.jacorb.test.BiDirServerPOA;
import org.jacorb.test.ClientCallback;
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
 * @author Andre Spiegel
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
        try
        {
            Thread.sleep (5000);
        }
        catch (InterruptedException ex)
        {
            // ignore
        }
        int count = org.jacorb.orb.iiop.ClientIIOPConnection.openTransports;
        callback._release();
        return count;
    }

    public static void main (String[] args) throws Exception
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

        System.out.println ("SERVER IOR: " + orb.object_to_string(o));
        System.out.flush();

        orb.run();
    }

}
