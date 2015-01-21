
package org.jacorb.test.orb.policies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.jacorb.test.SyncScopeServer;
import org.jacorb.test.SyncScopeServerHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.IMRExcludedClientServerCategory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.omg.CORBA.Policy;
import org.omg.CORBA.PolicyError;
import org.omg.CORBA.SetOverrideType;
import org.omg.Messaging.SYNC_NONE;
import org.omg.Messaging.SYNC_SCOPE_POLICY_TYPE;
import org.omg.Messaging.SYNC_WITH_SERVER;
import org.omg.Messaging.SYNC_WITH_TARGET;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

/**
 * Tests for SyncScopePolicy.
 *
 * @author Andre Spiegel &lt;spiegel@gnu.org&gt;
 */
@Category(IMRExcludedClientServerCategory.class)
public class SyncScopeTest extends ClientServerTestCase
{
    private static final int TIME = 300;

    private SyncScopeServer server;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        setup = new ClientServerSetup (SyncScopeServerImpl.class.getName());
    }

    @Before
    public void setUp() throws Exception
    {
        server = SyncScopeServerHelper.narrow (setup.getClientOrb().string_to_object(setup.getServerIOR()));
    }

    @After
    public void tearDown() throws Exception
    {
        server._release();
        server = null;
    }

    @Test
    public void test_warm_up()
    {
        server.operation (50);
        server.oneway_op (50);
    }

    @Test
    public void test_sync_none() throws Exception
    {
        server = setSyncScope (server, SYNC_NONE.value);

        int beforeCount = server.get_oneway_count();

        long start = System.currentTimeMillis();
        server.oneway_op (TIME);
        long time = System.currentTimeMillis() - start;
        assertTrue ("return too late", time < TIME);

        verifyOnewayWasReceived(beforeCount + 1);
    }

    @Test
    public void test_sync_with_transport() throws Exception
    {
        int beforeCount = server.get_oneway_count();

        server = setSyncScope (server, SYNC_WITH_TRANSPORT.value);
        long start = System.currentTimeMillis();
        server.oneway_op (TIME);
        long time = System.currentTimeMillis() - start;
        assertTrue ("return too late", time < TIME);

        verifyOnewayWasReceived(beforeCount + 1);
    }

    @Test
    public void test_sync_with_server() throws Exception
    {
        int beforeCount = server.get_oneway_count();

        server = setSyncScope (server, SYNC_WITH_SERVER.value);
        long start = System.currentTimeMillis();
        server.oneway_op (TIME);
        long time = System.currentTimeMillis() - start;
        assertTrue ("return too late", time < TIME);

        verifyOnewayWasReceived(beforeCount + 1);
    }

    @Test
    public void test_sync_with_target() throws Exception
    {
        int beforeCount = server.get_oneway_count();

        server = setSyncScope (server, SYNC_WITH_TARGET.value);
        long start = System.currentTimeMillis();
        server.oneway_op (TIME);
        long time = System.currentTimeMillis() - start;
        assertTrue ("return too early", time >= TIME);

        verifyOnewayWasReceived(beforeCount + 1);
    }

    private SyncScopeServer setSyncScope (SyncScopeServer server, short syncScope)
    {
        org.omg.CORBA.Any a   = setup.getClientOrb().create_any();
        a.insert_short (syncScope);
        try
        {
            Policy policy =
                setup.getClientOrb().create_policy(SYNC_SCOPE_POLICY_TYPE.value, a);
            org.omg.CORBA.Object r = server._set_policy_override (new Policy[]{ policy },
                                         SetOverrideType.ADD_OVERRIDE);
            return SyncScopeServerHelper.narrow (r);
        }
        catch (PolicyError e)
        {
            throw new RuntimeException ("policy error: " + e);
        }
    }

    private void verifyOnewayWasReceived(int expected) throws Exception
    {
        final long waitUntil = System.currentTimeMillis() + 10000;

        while( (server.get_oneway_count() != expected) && (System.currentTimeMillis() < waitUntil) )
        {
            Thread.sleep(1000);
        }

        assertEquals(expected, server.get_oneway_count());
    }
}
