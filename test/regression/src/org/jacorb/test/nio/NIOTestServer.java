package org.jacorb.test.nio;

import java.util.Properties;
import org.jacorb.orb.util.CorbaLoc;
import org.jacorb.test.common.TestUtils;

import org.jacorb.test.TestIfPOA;


import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManager;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ImplicitActivationPolicyValue;

public class NIOTestServer extends TestIfPOA
{

    public void op()
    {
        System.out.println ("TestIf::op called, waiting 3000ms");
        try
        {
            Thread.currentThread().sleep (3000);
        } 
        catch (InterruptedException e)
        {
            // ignore
        }

    }

    public void onewayOp()
    {
        // ignore
    }

    public static void main (String[] args)
    {
        try
        {
            Properties props = new Properties();
            props.setProperty ("jacorb.implname","NIOTestServer");
            props.setProperty ("OAPort", "6969");
            String objID = "ObjectID";

            ORB orb = ORB.init(args, props);
            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

            Policy[] policies = new Policy[1];
            policies[0] = rootPOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID);
            POAManager poa_manager = rootPOA.the_POAManager();
            POA child = rootPOA.create_POA ("thePOA", poa_manager, policies);

            poa_manager.activate();
            
            Servant impl = new NIOTestServer();
            child.activate_object_with_id(objID.getBytes(), impl);

            // Manually create a persistent based corbaloc.
            String corbalocStr =
                "corbaloc::localhost:"
                + props.getProperty("OAPort") + "/"
                + props.getProperty("jacorb.implname") + "/"
                + child.the_name() + "/" + objID;

            System.out.println ("Server IOR: " + corbalocStr);
            orb.run(); 
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
