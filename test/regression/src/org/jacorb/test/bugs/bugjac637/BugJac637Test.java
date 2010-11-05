/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package org.jacorb.test.bugs.bugjac637;

import java.util.Properties;
import java.applet.Applet;
import junit.framework.TestCase;
import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.OperationDef;
import org.omg.CORBA.ORB;

/**
 * <code>BugJac637Test</code> tests that calling operations on
 * a destroyed ORB will throw BAD_INV_ORDER.
 *
 * @author <a href="mailto:Nick.Cross@prismtech.com">Nick Cross</a>
 * @version 1.0
 */
public class BugJac637Test extends TestCase
{
    public void testShutdown() throws Exception
    {
        Properties props = new Properties();
        props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        props.put("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");

        ORB orb = ORB.init(new String[0], props);

        orb.destroy();

        try
        {
            orb.connect (null);
            fail ("Should have thrown not exist");
        }
        catch (BAD_INV_ORDER e)
        {
            // Pass
        }
        try
        {
            orb.disconnect (null);
            fail ("Should have thrown not exist");
        }
        catch (BAD_INV_ORDER e)
        {
            // Pass
        }
        try
        {
            orb.list_initial_services ();
            fail ("Should have thrown not exist");
        }
        catch (BAD_INV_ORDER e)
        {
            // Pass
        }
        try
        {
            orb.resolve_initial_references (null);
            fail ("Should have thrown not exist");
        }
        catch (BAD_INV_ORDER e)
        {
            // Pass
        }
        try
        {
            orb.object_to_string (null);
            fail ("Should have thrown not exist");
        }
        catch (BAD_INV_ORDER e)
        {
            // Pass
        }
        try
        {
            orb.string_to_object (null);
            fail ("Should have thrown not exist");
        }
        catch (BAD_INV_ORDER e)
        {
            // Pass
        }
        try
        {
            orb.create_list (0);
            fail ("Should have thrown not exist");
        }
        catch (BAD_INV_ORDER e)
        {
            // Pass
        }
        try
        {
            orb.create_operation_list ((org.omg.CORBA.Object)null);
            fail ("Should have thrown not exist");
        }
        catch (BAD_INV_ORDER e)
        {
            // Pass
        }
        try
        {
            orb.create_operation_list ((OperationDef)null);
            fail ("Should have thrown not exist");
        }
        catch (BAD_INV_ORDER e)
        {
            // Pass
        }
        try
        {
            orb.create_named_value (null, null, 0);
            fail ("Should have thrown not exist");
        }
        catch (BAD_INV_ORDER e)
        {
            // Pass
        }
        try
        {
            orb.create_exception_list ();
            fail ("Should have thrown not exist");
        }
        catch (BAD_INV_ORDER e)
        {
            // Pass
        }
        try
        {
            orb.create_context_list ();
            fail ("Should have thrown not exist");
        }
        catch (BAD_INV_ORDER e)
        {
            // Pass
        }
        try
        {
            orb.get_default_context ();
            fail ("Should have thrown not exist");
        }
        catch (BAD_INV_ORDER e)
        {
            // Pass
        }
        try
        {
            orb.create_environment();
            fail ("Should have thrown not exist");
        }
        catch (BAD_INV_ORDER e)
        {
            // Pass
        }
        try
        {
            orb.create_output_stream ();
            fail ("Should have thrown not exist");
        }
        catch (BAD_INV_ORDER e)
        {
            // Pass
        }
        try
        {
            orb.send_multiple_requests_oneway (null);
            fail ("Should have thrown not exist");
        }
        catch (BAD_INV_ORDER e)
        {
            // Pass
        }
        try
        {
            orb.send_multiple_requests_deferred (null);
            fail ("Should have thrown not exist");
        }
        catch (BAD_INV_ORDER e)
        {
            // Pass
        }
        try
        {
            orb.poll_next_response ();
            fail ("Should have thrown not exist");
        }
        catch (BAD_INV_ORDER e)
        {
            // Pass
        }
        try
        {
            orb.get_next_response ();
            fail ("Should have thrown not exist");
        }
        catch (BAD_INV_ORDER e)
        {
            // Pass
        }
    }
}
