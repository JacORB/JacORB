package org.jacorb.test.poa;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.Servant;

public class BiDirDelegateTestServerRunner
{
    /**
     * <code>main</code> is reimplemented here so that we can start
     * the server with a child POA and use servant_to_id.
     *
     * @param args a <code>String[]</code> value
     */
    public static void main (String[] args)
    {
        try
        {
            //init ORB
            ORB orb = ORB.init( args, null );

            //init POA
            POA rootPoa =
                POAHelper.narrow( orb.resolve_initial_references( "RootPOA" ));
            rootPoa.the_POAManager().activate();

            // create POA
            Policy policies[] = new Policy[2];
            policies[0] = rootPoa.create_id_uniqueness_policy(
                org.omg.PortableServer.IdUniquenessPolicyValue.MULTIPLE_ID);
            policies[1] = rootPoa.create_implicit_activation_policy(
                ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION );

            POA poa = rootPoa.create_POA
                ("childPOA1", rootPoa.the_POAManager(), policies);
            poa.the_POAManager().activate();

            String className = args[0];
            Class servantClass = Class.forName (className);
            Servant servant = ( Servant ) servantClass.newInstance();

            // Get the id
            byte []oid = poa.servant_to_id (servant);

            // create the object reference
            org.omg.CORBA.Object obj = poa.id_to_reference (oid);

            System.out.println ("SERVER IOR: " + orb.object_to_string(obj));
            System.out.flush();

            // wait for requests
            orb.run();
        }
        catch( Exception e )
        {
            e.printStackTrace();
            System.out.println ("Caught error " + e);
        }
    }
}
