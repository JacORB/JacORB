package org.jacorb.test.bugs.bug923;

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
        ServantLocatorImpl servantLocatorImpl = new ServantLocatorImpl(orb, persistent_poa);

        // Set servant manager
        persistent_poa.set_servant_manager(servantLocatorImpl);

        // create a GoodDayImpl
        GoodDayImpl goodDayImpl = new GoodDayImpl();
        org.omg.CORBA.Object obj = servantLocatorImpl.registerObject(GoodDayHelper.id(), GoodDayHelper.id(), goodDayImpl);

        // create a DayFactoryImpl
        DayFactoryImpl dayFactoryImpl = new DayFactoryImpl(persistent_poa);
        obj = servantLocatorImpl.registerObject(DayFactoryHelper.id(), DayFactoryHelper.id(), dayFactoryImpl);

        poa_manager.activate();

        System.out.println ("SERVER IOR: " + orb.object_to_string(obj));
        System.out.flush();

        // wait for requests
        orb.run();
    }
}
