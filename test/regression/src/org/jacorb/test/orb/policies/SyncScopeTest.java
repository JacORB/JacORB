
package org.jacorb.test.orb.policies;

import junit.framework.TestCase;
import org.jacorb.test.SyncScopeServer;
import org.jacorb.test.SyncScopeServerHelper;
import org.jacorb.test.common.ORBSetup;
import org.jacorb.test.common.ServerSetup;
import org.omg.CORBA.ORB;
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
 * @author Andre Spiegel <spiegel@gnu.org>
 */
public class SyncScopeTest extends TestCase
{
    private final int TIME = 300;
    private SyncScopeServer server;
    private ServerSetup serverSetup;
    private ORBSetup orbSetup;
    private ORB orb;

    public static void main(String args[]) throws Exception
    {
        SyncScopeTest s = new SyncScopeTest();
        s.orb = org.omg.CORBA.ORB.init(args, null);
        s.server = SyncScopeServerHelper.narrow (s.orb.string_to_object(args[0]));
        s.test_sync_none();
    }


    protected void setUp() throws Exception
    {
        orbSetup = new ORBSetup(this);
        orbSetup.setUp();
        orb = orbSetup.getORB();

        serverSetup = new ServerSetup(this, SyncScopeServerImpl.class.getName());
        serverSetup.setUp();
        server = SyncScopeServerHelper.narrow (orb.string_to_object(serverSetup.getServerIOR()));
    }

    protected void tearDown() throws Exception
    {
        orbSetup.tearDown();
        orbSetup = null;
        server._release();
        server = null;
        serverSetup.tearDown();
        serverSetup = null;
    }

    public void test_warm_up()
    {
        server.operation (50);
        server.oneway_op (50);
    }

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
        org.omg.CORBA.Any a   = this.orb.create_any();
        a.insert_short (syncScope);
        try
        {
            Policy policy =
                this.orb.create_policy(SYNC_SCOPE_POLICY_TYPE.value, a);
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
