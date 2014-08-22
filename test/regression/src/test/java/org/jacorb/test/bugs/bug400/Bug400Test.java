package org.jacorb.test.bugs.bug400;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.harness.ORBTestCase;
import org.jacorb.test.orb.BasicServerImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAManager;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;

/**
 * @author Alphonse Bendt
 * @author Alex Shebunyaev
 */
public class Bug400Test extends ORBTestCase
{
    private POAManager poaManager;

    @Before
    public void setUp() throws Exception
    {
        poaManager = rootPOA.the_POAManager();
    }

    @After
    public void tearDown() throws Exception
    {
        poaManager = null;
    }

    @Test
    public void testSimpleShutdown() throws Exception
    {
        poaManager.activate();

        BasicServerImpl servant = new BasicServerImpl();

        rootPOA.activate_object(servant);

        BasicServer server = BasicServerHelper.narrow(rootPOA.servant_to_reference(servant));

        assertEquals(42, server.bounce_long(42));

        rootPOA.deactivate_object(rootPOA.servant_to_id(servant));

        Thread.sleep(1000);

        try
        {
            server.bounce_long(43);
            fail();
        }
        catch (OBJECT_NOT_EXIST e)
        {
            // expected
        }

        assertTrue(destroyPOA(rootPOA));
    }

    @Test
    public void testShutdownWithServantLocator() throws Exception
    {
        Policy[] policies = new Policy[]
                                       {
                                           rootPOA.create_id_assignment_policy(
                                               IdAssignmentPolicyValue.USER_ID),
                                           rootPOA.create_servant_retention_policy(
                                               ServantRetentionPolicyValue.NON_RETAIN),
                                           rootPOA.create_request_processing_policy(
                                               RequestProcessingPolicyValue.USE_SERVANT_MANAGER)
                                       };

        POA poa = rootPOA.create_POA("MyPOA", poaManager, policies);

        poa.set_servant_manager(new MyServantLocator());

        poaManager.activate();

        org.omg.CORBA.Object ref = poa.create_reference_with_id(
                "some_oid".getBytes(),
                BasicServerHelper.id());

        BasicServer bs = BasicServerHelper.narrow(ref);

        try
        {
            bs.bounce_string("test-string");
            fail();
        }
        catch (OBJECT_NOT_EXIST ex)
        {
        }

        assertTrue(destroyPOA(poa));
    }

    private boolean destroyPOA(final POA rootPOA) throws InterruptedException
    {
        final boolean[] success = new boolean[1];

        Thread thread = new Thread()
        {
            public void run()
            {
                rootPOA.destroy(true, true);
                success[0] = true;
            }
        };

        thread.start();
        thread.join(2000);

        return success[0];
    }

    private static class MyServantLocator
        extends LocalObject implements ServantLocator
    {
        public Servant preinvoke(
                byte[] poaObjectId,
                POA poa,
                String operation,
                CookieHolder cookie)
        {
            throw new OBJECT_NOT_EXIST();
        }

        public void postinvoke(
                byte[] poaObjectId,
                POA poa,
                String operation,
                java.lang.Object cookie,
                Servant servant)
        {
        }
    }
}
