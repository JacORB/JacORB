package org.jacorb.test.bugs.bugjac488;

/*
 * JacORB - a free Java ORB
 *
 * Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import static org.junit.Assert.fail;
import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.IMRExcludedClientServerCategory;
import org.jacorb.test.harness.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TIMEOUT;

/**
 * <code>TestCase</code> verifies that calling a non-existent server
 * correctly receives a transient and not a nullpointer exception.
 *
 * @author <a href="mailto:Nick.Cross@prismtech.com">Nick Cross</a>
 */
@Category(IMRExcludedClientServerCategory.class)
public class BugJac488Test extends ClientServerTestCase
{
    private PingReceiver server;
    private ORB orb;

    // setup is done in @before as we need a fresh server for each test.
    @Before
    public void setUp() throws Exception
    {
        Properties props = new Properties();
        props.put("jacorb.use_imr", "off");
        setup = new ClientServerSetup (PingReceiverImpl.class.getName(), props, props);
        server = PingReceiverHelper.narrow(setup.getServerObject());
        orb = setup.getClientOrb();
    }

    @After
    public void tearDown() throws Exception
    {
        setup.tearDown();
        server._release();
    }

    @Test
    public void testOnewayPingNone () throws Exception
    {
        testPingInternal ("SYNC_NONE");
    }

    @Test
    public void testOnewayPingTransport () throws Exception
    {
        testPingInternal ("SYNC_WITH_TRANSPORT");
    }

    @Test
    public void testOnewayPingServer () throws Exception
    {
        testPingInternal ("SYNC_WITH_SERVER");
    }

    @Test
    public void testOnewayPingTarget () throws Exception
    {
        testPingInternal ("SYNC_WITH_TARGET");
    }

    private void testPingInternal (String policyType) throws Exception
    {
        org.omg.CORBA.Object obj = null;
        PingReceiver pr = null;

        org.omg.CORBA.Policy syncScopePolicy = null;
        org.omg.CORBA.Any syncScopePolicyAny = orb.create_any ();

        if (policyType.equalsIgnoreCase ("SYNC_NONE"))
        {
            TestUtils.getLogger().debug ("Setting SyncScope Policy to SYNC_NONE");
            syncScopePolicyAny.insert_short (org.omg.Messaging.SYNC_NONE.value);
            syncScopePolicy = orb.create_policy (org.omg.Messaging.SYNC_SCOPE_POLICY_TYPE.value,
                                                 syncScopePolicyAny);
            obj = server._set_policy_override (new org.omg.CORBA.Policy[] { syncScopePolicy },
                                               org.omg.CORBA.SetOverrideType.ADD_OVERRIDE);
        }
        else if (policyType.equalsIgnoreCase ("SYNC_WITH_SERVER"))
        {
            TestUtils.getLogger().debug ("Setting SyncScope Policy to SYNC_WITH_SERVER");
            syncScopePolicyAny.insert_short (org.omg.Messaging.SYNC_WITH_SERVER.value);
            syncScopePolicy = orb.create_policy (org.omg.Messaging.SYNC_SCOPE_POLICY_TYPE.value,
                                                 syncScopePolicyAny);
            obj = server._set_policy_override (new org.omg.CORBA.Policy[] { syncScopePolicy },
                                               org.omg.CORBA.SetOverrideType.ADD_OVERRIDE);
        }
        else if (policyType.equalsIgnoreCase ("SYNC_WITH_TARGET"))
        {
            TestUtils.getLogger().debug ("Setting SyncScope Policy to SYNC_WITH_TARGET");
            syncScopePolicyAny.insert_short (org.omg.Messaging.SYNC_WITH_TARGET.value);
            syncScopePolicy = orb.create_policy (org.omg.Messaging.SYNC_SCOPE_POLICY_TYPE.value,
                                                 syncScopePolicyAny);
            obj = server._set_policy_override (new org.omg.CORBA.Policy[] { syncScopePolicy },
                                               org.omg.CORBA.SetOverrideType.ADD_OVERRIDE);
        }
        else if (policyType.equalsIgnoreCase ("SYNC_WITH_TRANSPORT"))
        {
            TestUtils.getLogger().debug ("Setting SyncScope Policy to SYNC_WITH_TRANSPORT");
            syncScopePolicyAny.insert_short (org.omg.Messaging.SYNC_WITH_TRANSPORT.value);
            syncScopePolicy = orb.create_policy (org.omg.Messaging.SYNC_SCOPE_POLICY_TYPE.value,
                                                 syncScopePolicyAny);
            obj = server._set_policy_override (new org.omg.CORBA.Policy[] { syncScopePolicy },
                                               org.omg.CORBA.SetOverrideType.ADD_OVERRIDE);
        }
        else
        {
            TestUtils.getLogger().debug ("Received Unexpected SyncScope Policy");
        }


        pr = PingReceiverHelper.narrow(obj);
        obj._release();

        pr.ping ();
        pr.shutdown();
        Thread.sleep (10000);

        try
        {
            pr.ping ();

            if ( ! policyType.equalsIgnoreCase ("SYNC_NONE"))
            {
                fail ("No exception thrown");
            }
        }
        catch (org.omg.CORBA.TRANSIENT e)
        {
            // Pass
        }
        catch(TIMEOUT e)
        {
            fail ("Did not expect timeout");
        }
        finally
        {
            pr._release();
        }
    }
}
