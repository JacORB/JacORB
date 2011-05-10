package org.jacorb.test.bugs.bugjac662;

/*
 * JacORB - a free Java ORB
 *
 * Copyright (C) 1997-2011 Gerald Brose / The JacORB Team.
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

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jacorb.orb.Delegate;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.ORB;


/**
 * <code>BugJac662Test</code> verifies that if an exception occurs while
 * waiting for a reply, the Delegate pending_replies map is correctly cleared.
 *
 * @author <a href="mailto:Nick.Cross@prismtech.com">Nick Cross</a>
 * @version 1.0
 */
public class BugJac662Test extends ClientServerTestCase
{
    private PingReceiver server;


    public BugJac662Test (String name, ClientServerSetup setup)
    {
        super (name, setup);
    }


    /**
     * Junit <code>suite</code>.
     *
     * @return a <code>Test</code> value
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite ("ReplyReceiver Memory Leak");
        Properties props = new Properties();
        props.put("jacorb.use_imr", "off");

        ClientServerSetup setup =
            new ClientServerSetup
                (suite,
                 "org.jacorb.test.bugs.bugjac662.PingReceiverImpl", props, props);

        suite.addTest (new BugJac662Test ("testReplyReceiverCount", setup));

        return setup;
    }


    protected void setUp() throws Exception
    {
        server = PingReceiverHelper.narrow(setup.getServerObject());
    }

    protected void tearDown() throws Exception
    {
        server._release();
        server = null;
    }

    /**
     * <code>testReplyReceiverCount</code> uses reflection to test the
     * value of the private pending_replies field to ensure that it is
     * always cleared after an exception happens.
     *
     * @exception Exception if an error occurs
     */
    public void testReplyReceiverCount () throws Exception
    {
        PingReceiver pr = PingReceiverHelper.narrow(server);
        pr.ping ();

        // Now the test...
        Delegate d = (Delegate)((org.omg.CORBA.portable.ObjectImpl)pr)._get_delegate();
        Field fields[] = Delegate.class.getDeclaredFields();
        HashSet pendingReplies = null;

        for (int i = 0; i < fields.length; ++i)
        {
            if ("pending_replies".equals(fields[i].getName()))
            {
                Field f = fields[i];
                f.setAccessible(true);
                pendingReplies = (HashSet)f.get (d);
                break;
            }
        }
        if (pendingReplies == null)
        {
            fail ("Unable to find pending_replies in Delegate");
        }

        assertTrue ("Should be no replies pending", pendingReplies.size() == 0);
        try
        {
            pr.shutdown ();
        }
        catch (COMM_FAILURE e)
        {
        }
        pr._release();
        System.err.println ("replies pending: " + pendingReplies.toString());
        assertTrue ("Should be no replies pending", pendingReplies.size() == 0);
    }
}
