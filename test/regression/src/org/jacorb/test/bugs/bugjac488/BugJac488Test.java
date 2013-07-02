package org.jacorb.test.bugs.bugjac488;

/*
 * JacORB - a free Java ORB
 *
 * Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

import java.util.Properties;
import junit.framework.TestCase;
import org.jacorb.test.common.ORBSetup;
import org.jacorb.test.common.ServerSetup;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TIMEOUT;

/**
 * <code>TestCase</code> verifies that calling a non-existent server
 * correctly receives a transient and not a nullpointer exception.
 *
 * @author <a href="mailto:Nick.Cross@prismtech.com">Nick Cross</a>
 */
public class BugJac488Test extends TestCase
{
    private PingReceiver server;
    private ServerSetup serverSetup;
    private ORBSetup orbSetup;
    private ORB orb;


    protected void setUp() throws Exception
    {
        Properties props = new Properties();
        props.put("jacorb.use_imr", "off");

        orbSetup = new ORBSetup(this, props);
        orbSetup.setUp();
        orb = orbSetup.getORB();

        serverSetup = new ServerSetup(this, null, PingReceiverImpl.class.getName(), props);
        serverSetup.setUp();
        server = PingReceiverHelper.narrow(orb.string_to_object(serverSetup.getServerIOR()));
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

    public void testOnewayPingNone () throws Exception
    {
        testPingInternal ("SYNC_NONE");
    }

    public void testOnewayPingTransport () throws Exception
    {
        testPingInternal ("SYNC_WITH_TRANSPORT");
    }

    public void testOnewayPingServer () throws Exception
    {
        testPingInternal ("SYNC_WITH_SERVER");
    }

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
            System.out.println ("Setting SyncScope Policy to SYNC_NONE");
            syncScopePolicyAny.insert_short (org.omg.Messaging.SYNC_NONE.value);
            syncScopePolicy = orb.create_policy (org.omg.Messaging.SYNC_SCOPE_POLICY_TYPE.value,
                                                 syncScopePolicyAny);
            obj = server._set_policy_override (new org.omg.CORBA.Policy[] { syncScopePolicy },
                                               org.omg.CORBA.SetOverrideType.ADD_OVERRIDE);
        }
        else if (policyType.equalsIgnoreCase ("SYNC_WITH_SERVER"))
        {
            System.out.println ("Setting SyncScope Policy to SYNC_WITH_SERVER");
            syncScopePolicyAny.insert_short (org.omg.Messaging.SYNC_WITH_SERVER.value);
            syncScopePolicy = orb.create_policy (org.omg.Messaging.SYNC_SCOPE_POLICY_TYPE.value,
                                                 syncScopePolicyAny);
            obj = server._set_policy_override (new org.omg.CORBA.Policy[] { syncScopePolicy },
                                               org.omg.CORBA.SetOverrideType.ADD_OVERRIDE);
        }
        else if (policyType.equalsIgnoreCase ("SYNC_WITH_TARGET"))
        {
            System.out.println ("Setting SyncScope Policy to SYNC_WITH_TARGET");
            syncScopePolicyAny.insert_short (org.omg.Messaging.SYNC_WITH_TARGET.value);
            syncScopePolicy = orb.create_policy (org.omg.Messaging.SYNC_SCOPE_POLICY_TYPE.value,
                                                 syncScopePolicyAny);
            obj = server._set_policy_override (new org.omg.CORBA.Policy[] { syncScopePolicy },
                                               org.omg.CORBA.SetOverrideType.ADD_OVERRIDE);
        }
        else if (policyType.equalsIgnoreCase ("SYNC_WITH_TRANSPORT"))
        {
            System.out.println ("Setting SyncScope Policy to SYNC_WITH_TRANSPORT");
            syncScopePolicyAny.insert_short (org.omg.Messaging.SYNC_WITH_TRANSPORT.value);
            syncScopePolicy = orb.create_policy (org.omg.Messaging.SYNC_SCOPE_POLICY_TYPE.value,
                                                 syncScopePolicyAny);
            obj = server._set_policy_override (new org.omg.CORBA.Policy[] { syncScopePolicy },
                                               org.omg.CORBA.SetOverrideType.ADD_OVERRIDE);
        }
        else
        {
            System.out.println ("Received Unexpected SyncScope Policy");
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
