
package org.jacorb.test.orb.policies;

import junit.framework.*;

import org.omg.CORBA.*;
import org.omg.Messaging.*;

import org.jacorb.Tests.*;
import org.jacorb.test.common.*;

/**
 * Tests for SyncScopePolicy.
 *
 * @author Andre Spiegel <spiegel@gnu.org>
 * @version $Id$
 */
public class SyncScopeTest extends ClientServerTestCase 
{
    private SyncScopeServer server;
    
    public SyncScopeTest (String name, ClientServerSetup setup)
    {
        super (name, setup); 
    }

    protected void setUp() throws Exception
    {
        server = SyncScopeServerHelper.narrow (setup.getServerObject());
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite ("Sync Scope");
        ClientServerSetup setup = 
            new ClientServerSetup 
                (suite,
                 "org.jacorb.test.orb.policies.SyncScopeServerImpl");
                  
        suite.addTest (new SyncScopeTest ("test_warm_up", setup));
        suite.addTest (new SyncScopeTest ("test_sync_none", setup));
        suite.addTest (new SyncScopeTest ("test_sync_with_transport", setup));
        suite.addTest (new SyncScopeTest ("test_sync_with_server", setup));
        suite.addTest (new SyncScopeTest ("test_sync_with_target", setup));
        
        return setup;  
    }
    
    public void test_warm_up()
    {
        server.operation (50);
        server.oneway_op (50);
    }

    public void test_sync_none()
    {
        setSyncScope (server, SYNC_NONE.value);
        long start = System.currentTimeMillis();
        server.oneway_op (100);
        long time = System.currentTimeMillis() - start;
        assertTrue ("return too late", time < 100);    
    }

    public void test_sync_with_transport()
    {
        setSyncScope (server, SYNC_WITH_TRANSPORT.value);
        long start = System.currentTimeMillis();
        server.oneway_op (100);
        long time = System.currentTimeMillis() - start;
        assertTrue ("return too late", time < 100);    
    }

    public void test_sync_with_server()
    {
        setSyncScope (server, SYNC_WITH_SERVER.value);
        long start = System.currentTimeMillis();
        server.oneway_op (100);
        long time = System.currentTimeMillis() - start;
        assertTrue ("return too late", time < 100);    
    }

    public void test_sync_with_target()
    {
        setSyncScope (server, SYNC_WITH_TARGET.value);
        long start = System.currentTimeMillis();
        server.oneway_op (100);
        long time = System.currentTimeMillis() - start;
        assertTrue ("return too early", time > 100);    
    }

    private void setSyncScope (SyncScopeServer server, short syncScope)
    {
        org.omg.CORBA.ORB orb = setup.getClientOrb();
        org.omg.CORBA.Any a   = orb.create_any();
        a.insert_short (syncScope);
        try
        {
            Policy p =
                orb.create_policy(SYNC_SCOPE_POLICY_TYPE.value, a);
            server._set_policy_override (new Policy[]{ p }, 
                                         SetOverrideType.ADD_OVERRIDE);   
        }
        catch (PolicyError e)
        {
            throw new RuntimeException ("policy error: " + e);
        }        
        
    }

}
