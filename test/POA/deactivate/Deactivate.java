package test.POA.deactivate;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POAPackage.*;


public class Deactivate
{

    public static void main(String[] args)
    {
        ORB orb = null;
        POA root, system = null;
        byte[] id1, id2;

        try
        {
            orb = orb.init( args, null );
            root = POAHelper.narrow( orb.resolve_initial_references( "RootPOA" ));

            // create POA
            Policy policies[] = new Policy[3];
            policies[0] = root.create_id_assignment_policy(
                                                           org.omg.PortableServer.IdAssignmentPolicyValue.SYSTEM_ID);
            policies[1] = root.create_id_uniqueness_policy(
                                                           org.omg.PortableServer.IdUniquenessPolicyValue.UNIQUE_ID);
            policies[2] = root.create_servant_retention_policy(
                                                               org.omg.PortableServer.ServantRetentionPolicyValue.RETAIN);

            system = root.create_POA("system_id", root.the_POAManager(), policies);
        }
        catch(AdapterAlreadyExists ex)
        {
            throw new RuntimeException();
        }
        catch(InvalidPolicy ex)
        {
            throw new RuntimeException();
        }
        catch(Exception  ex)
        {
            ex.printStackTrace();
        }

        // create Servants
        Test_impl servant1 = new Test_impl();
        Test_impl servant2 = new Test_impl();
        // first activate servants
        try
        {
            id1 = system.activate_object(servant1);
            id2 = system.activate_object(servant2);
        }
        catch(ServantAlreadyActive ex)
        {
            throw new RuntimeException();
        }
        catch(WrongPolicy ex)
        {
            throw new RuntimeException();
        }
        // deactivate the servants now
        // no request is pending 
        try
        {
            system.deactivate_object(id2);
            system.deactivate_object(id1);
        }
        catch(ObjectNotActive ex)
        {
            throw new RuntimeException();
        }
        catch(WrongPolicy ex)
        {
            throw new RuntimeException();
        }
        // now again try to deactivate 
        // I would expect ObjectNotActive Exception but didn't get one
        try
        {
            system.deactivate_object(id1);
            System.out.println("deactivate_object called twice, expecting ObjectNotActive exception, but didn't. Test not ok!"); 
        }
        catch(ObjectNotActive ex)
        {
            // expected
            ex.printStackTrace();
            System.out.println("Got what I wanted! Test ok!");
        }
        catch(WrongPolicy ex)
        {
            throw new RuntimeException();
        }
    }
}
