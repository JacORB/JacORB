package org.jacorb.test.bugs.bugjac662;

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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jacorb.orb.Delegate;
import org.jacorb.orb.ReplyGroup;
import org.jacorb.orb.giop.ReplyPlaceholder;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.IMRExcludedClientServerCategory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.omg.CORBA.COMM_FAILURE;


/**
 * <code>BugJac662Test</code> verifies that if an exception occurs while
 * waiting for a reply, the Delegate pending_replies map is correctly cleared.
 *
 * @author <a href="mailto:Nick.Cross@prismtech.com">Nick Cross</a>
 * @version 1.0
 */
@Category(IMRExcludedClientServerCategory.class)
public class BugJac662Test extends ClientServerTestCase
{
    private PingReceiver server;


    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {

        Properties props = new Properties();
        props.put("jacorb.use_imr", "off");

        setup = new ClientServerSetup("org.jacorb.test.bugs.bugjac662.PingReceiverImpl", props, props);
    }


    @Before
    public void setUp() throws Exception
    {
        server = PingReceiverHelper.narrow(setup.getServerObject());
    }

    @After
    public void tearDown() throws Exception
    {
        server._release();
        server = null;
    }

    /**
     * <code>testReplyReceiverCount</code> uses reflection to test the
     * value of the private groups field to ensure that it is
     * always cleared after an exception happens.
     *
     * @exception Exception if an error occurs
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testReplyReceiverCount () throws Exception
    {
        PingReceiver pr = PingReceiverHelper.narrow(server);
        pr.ping ();

        // Now the test...
        Delegate d = (Delegate)((org.omg.CORBA.portable.ObjectImpl)pr)._get_delegate();
        Field fields[] = Delegate.class.getDeclaredFields();
        Set<ReplyPlaceholder> pendingReplies = null;
        ConcurrentHashMap<org.omg.ETF.Profile, ReplyGroup> groups = null;

        for (int i = 0; i < fields.length; ++i)
        {
            if ("groups".equals(fields[i].getName()))
            {
                Field f = fields[i];
                f.setAccessible(true);
                groups = (ConcurrentHashMap<org.omg.ETF.Profile, ReplyGroup>)f.get (d);
                break;
            }
        }
        if (groups == null)
        {
            fail ("Unable to find pending_replies in Delegate");
        }

        assertTrue ("Groups does not have only one entry", groups.size() == 1);

        Enumeration<ReplyGroup> elements = groups.elements();
        if (elements.hasMoreElements())
        {
            pendingReplies = elements.nextElement().getReplies();
        }
        if (pendingReplies == null)
        {
            fail ("Unable to get replies from ReplyGroup");
        }

        assertTrue ("Should be no replies pending: " + pendingReplies.toString(), pendingReplies.size() == 0);

        try
        {
            pr.shutdown ();
        }
        catch (COMM_FAILURE e)
        {
        }
        pr._release();
        assertTrue ("Should be no replies pending: " + pendingReplies.toString(), pendingReplies.size() == 0);
    }
}
