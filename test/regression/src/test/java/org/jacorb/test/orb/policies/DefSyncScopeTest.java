
package org.jacorb.test.orb.policies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Properties;
import org.jacorb.test.SyncScopeServer;
import org.jacorb.test.SyncScopeServerHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.After;
import org.junit.Test;

/**
 * Tests for SyncScopePolicy.
 *
 * @author Andre Spiegel &lt;spiegel@gnu.org&gt;
 */
public class DefSyncScopeTest extends ClientServerTestCase
{
    private static final int TIME = 300;
    private Properties props;
    private SyncScopeServer server;

    @After
    public void tearDown() throws Exception
    {
        server._release();
        server = null;
    }

    private void init_server () throws Exception
    {
        setup = new ClientServerSetup (SyncScopeServerImpl.class.getName(),
                                       props, null);
        server = SyncScopeServerHelper.narrow (setup.getClientOrb().string_to_object(setup.getServerIOR()));
    }

    @Test
    public void test_warm_up() throws Exception
    {
        props = null;
        init_server ();
        server.operation (50);
        server.oneway_op (50);
    }

    @Test
    public void test_def_sync_none() throws Exception
    {
        props = new Properties();
        props.put("jacorb.default_sync_scope","None");
        init_server ();
        int beforeCount = server.get_oneway_count();

        long start = System.currentTimeMillis();
        server.oneway_op (TIME);
        long time = System.currentTimeMillis() - start;
        assertTrue ("return too late", time < TIME);

        verifyOnewayWasReceived(beforeCount + 1);
    }

    @Test
    public void test_def_sync_with_transport() throws Exception
    {
        props = new Properties();
        props.put("jacorb.default_sync_scope","Transport");
        init_server ();
        int beforeCount = server.get_oneway_count();

        long start = System.currentTimeMillis();
        server.oneway_op (TIME);
        long time = System.currentTimeMillis() - start;
        assertTrue ("return too late", time < TIME);

        verifyOnewayWasReceived(beforeCount + 1);
    }

    @Test
    public void test_def_sync_with_server() throws Exception
    {
        props = new Properties();
        props.put("jacorb.default_sync_scope","Server");
        init_server ();
        int beforeCount = server.get_oneway_count();

        long start = System.currentTimeMillis();
        server.oneway_op (TIME);
        long time = System.currentTimeMillis() - start;
        assertTrue ("return too late", time < TIME);

        verifyOnewayWasReceived(beforeCount + 1);
    }

    @Test
    public void test_def_sync_with_target() throws Exception
    {
        props = new Properties();
        props.put("jacorb.default_sync_scope","Target");
        init_server ();
        int beforeCount = server.get_oneway_count();

        long start = System.currentTimeMillis();
        server.oneway_op (TIME);
        long time = System.currentTimeMillis() - start;
        assertTrue ("return too early", time >= TIME);

        verifyOnewayWasReceived(beforeCount + 1);
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
