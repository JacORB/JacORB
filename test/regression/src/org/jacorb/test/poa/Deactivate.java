package org.jacorb.test.poa;

import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ORBTestCase;
import org.jacorb.test.orb.BasicServerImpl;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POAPackage.*;
import junit.framework.*;

public class Deactivate extends ORBTestCase
{
    public static Test suite()
    {
        return new TestSuite (Deactivate.class);
    }

    public void test_deactivate () throws Exception
    {
        byte[] id1, id2;

        // create POA
        Policy policies[] = new Policy[3];
        policies[0] = rootPOA.create_id_assignment_policy(
                org.omg.PortableServer.IdAssignmentPolicyValue.SYSTEM_ID);
        policies[1] = rootPOA.create_id_uniqueness_policy(
                org.omg.PortableServer.IdUniquenessPolicyValue.UNIQUE_ID);
        policies[2] = rootPOA.create_servant_retention_policy(
                org.omg.PortableServer.ServantRetentionPolicyValue.RETAIN);

        POA system = rootPOA.create_POA("system_id", rootPOA.the_POAManager(), policies);

        // create Servants
        Test_impl servant1 = new Test_impl();
        Test_impl servant2 = new Test_impl();
        // first activate servants

        id1 = system.activate_object(servant1);
        id2 = system.activate_object(servant2);

        // deactivate the servants now
        // no request is pending
        system.deactivate_object(id2);
        system.deactivate_object(id1);

        // now again try to deactivate
        // I would expect ObjectNotActive Exception but didn't get one
        try
        {
            system.deactivate_object(id1);
            fail( "deactivate_object called twice, expecting ObjectNotActive exception, but didn't");
        }
        catch(ObjectNotActive ex)
        {
            // expected
        }
    }

    /**
     * <code>test_deactivate_activator</code> is a test for JAC2 - When
     * ServantActivators with a RETAIN policy are used local object
     * invocations do not activate the object so a deactivate fails.
     */
    public void test_deactivate_activator () throws Exception
    {
        rootPOA.the_POAManager().activate();

        // create POA
        Policy policies[] = new Policy[2];
        policies[0] = rootPOA.create_servant_retention_policy(
                org.omg.PortableServer.ServantRetentionPolicyValue.RETAIN);
        policies[1] = rootPOA.create_request_processing_policy(
                RequestProcessingPolicyValue.USE_SERVANT_MANAGER);

        POA system = rootPOA.create_POA("system_id_deactivate_2", rootPOA.the_POAManager(), policies);
        system.the_POAManager().activate();
        system.set_servant_manager(new PoaServantActivator());

        org.omg.CORBA.Object objectRef = system.create_reference(BasicServerHelper.id());
        BasicServer ref = BasicServerHelper.narrow(objectRef);
        // Local op; will incarnate the object.
        ref.ping();

        // Now try deactivating it.
        system.deactivate_object (system.reference_to_id(ref));
    }

    /**
     * <code>PoaServantActivator</code> is a ServantActivator for
     * test_deactivate_activator.
     */
    static class PoaServantActivator extends LocalObject implements ServantActivator
    {
        // The incarnate operation is invoked by the POA whenever the POA receives
        // a request for an object that is not currently active, assuming the POA
        // has the RETAIN and USE_SERVANT_MANAGER policies
        public Servant incarnate(byte[] oid, POA adapter) throws ForwardRequest
        {
            return new BasicServerImpl();
        }

        public void etherealize(byte[] oid, POA adapter, Servant serv,
                boolean cleanup_in_progress,
                boolean remaining_activations)
        {
            // nothing to do
        }
    }
}
