package  org.jacorb.test.bugs.bug927;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManager;

public class Server
{
    public static void main(String[] args) throws Exception
    {
        //init ORB
        ORB orb = ORB.init( args, null );

        org.omg.CORBA.Object obj = orb.resolve_initial_references( "PICurrent" );
        org.omg.PortableInterceptor.Current piCurrent = org.omg.PortableInterceptor.CurrentHelper.narrow(obj);

        //init POA
        POA poa =
            POAHelper.narrow( orb.resolve_initial_references( "RootPOA" ));

        POAManager poa_manager = poa.the_POAManager();

        // Create a PERSISTENT POA named 'simple_persistent', beneath the root
        //
        org.omg.CORBA.Policy[] policies=new org.omg.CORBA.Policy[4];

        policies[0]=poa.create_lifespan_policy(org.omg.PortableServer.LifespanPolicyValue.PERSISTENT);
        policies[1]=poa.create_id_assignment_policy(org.omg.PortableServer.IdAssignmentPolicyValue.USER_ID);
        policies[2]=poa.create_servant_retention_policy(org.omg.PortableServer.ServantRetentionPolicyValue.NON_RETAIN);
        policies[3]=poa.create_request_processing_policy(
                        org.omg.PortableServer.RequestProcessingPolicyValue.USE_SERVANT_MANAGER);

        org.omg.PortableServer.POA persistent_poa=
               poa.create_POA("simple_persistent",
                                    poa_manager,
                                    policies);

        // ServantLocator
        ServantLocatorImpl servantLocatorImpl = new ServantLocatorImpl(persistent_poa, piCurrent);

      // Set servant manager
      persistent_poa.set_servant_manager(servantLocatorImpl);

        // create a TestObjectImpl
        System.out.println("Creating TestObjectImpl");
        TestObjectImpl testObjectImpl = new TestObjectImpl(orb);
        obj = servantLocatorImpl.registerObject(TestObjectHelper.id(), TestObjectHelper.id(), testObjectImpl);

        poa_manager.activate();

        System.out.println ("SERVER IOR: " + orb.object_to_string(obj));
        System.out.flush();

        // wait for requests
        orb.run();
    }
}
