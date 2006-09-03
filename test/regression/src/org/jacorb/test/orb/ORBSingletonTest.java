package org.jacorb.test.orb;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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
 */

import junit.framework.*;

import org.jacorb.orb.*;
import org.jacorb.test.common.*;

/**
 * A very simple test that checks whether some methods in the singleton ORB
 * are correctly flagged as forbidden (NO_IMPLEMENT).
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id$
 */
public class ORBSingletonTest extends JacORBTestCase
{
    
    public ORBSingletonTest (String name)
    {
        super (name);
    }
    
    public static Test suite()
    {
        return new TestSuite (ORBSingletonTest.class, "ORBSingleton tests");
    }
    
    public void test_disallowed_methods()
    {
        ORBSingleton orbs = new org.jacorb.orb.ORBSingleton();
        try
        {
            orbs.create_exception_list();
            fail ("should have raised NO_IMPLEMENT");
        }
        catch (org.omg.CORBA.NO_IMPLEMENT ex)
        {
            // ok
        }

        try
        {
            orbs.create_list(77);
            fail ("should have raised NO_IMPLEMENT");
        }
        catch (org.omg.CORBA.NO_IMPLEMENT ex)
        {
            // ok
        }

        try
        {
            orbs.create_named_value("", null, 0);
            fail ("should have raised NO_IMPLEMENT");
        }
        catch (org.omg.CORBA.NO_IMPLEMENT ex)
        {
            // ok
        }

        try
        {
            orbs.create_operation_list((org.omg.CORBA.Object)null);
            fail ("should have raised NO_IMPLEMENT");
        }
        catch (org.omg.CORBA.NO_IMPLEMENT ex)
        {
            // ok
        }

        try
        {
            orbs.create_operation_list((org.omg.CORBA.OperationDef)null);
            fail ("should have raised NO_IMPLEMENT");
        }
        catch (org.omg.CORBA.NO_IMPLEMENT ex)
        {
            // ok
        }

        try
        {
            orbs.string_to_object("");
            fail ("should have raised NO_IMPLEMENT");
        }
        catch (org.omg.CORBA.NO_IMPLEMENT ex)
        {
            // ok
        }

        try
        {
            orbs.create_environment();
            fail ("should have raised NO_IMPLEMENT");
        }
        catch (org.omg.CORBA.NO_IMPLEMENT ex)
        {
            // ok
        }

        try
        {
            orbs.create_context_list();
            fail ("should have raised NO_IMPLEMENT");
        }
        catch (org.omg.CORBA.NO_IMPLEMENT ex)
        {
            // ok
        }

        try
        {
            orbs.create_output_stream();
            fail ("should have raised NO_IMPLEMENT");
        }
        catch (org.omg.CORBA.NO_IMPLEMENT ex)
        {
            // ok
        }

        try
        {
            orbs.get_current();
            fail ("should have raised NO_IMPLEMENT");
        }
        catch (org.omg.CORBA.NO_IMPLEMENT ex)
        {
            // ok
        }

        try
        {
            orbs.get_default_context();
            fail ("should have raised NO_IMPLEMENT");
        }
        catch (org.omg.CORBA.NO_IMPLEMENT ex)
        {
            // ok
        }

        try
        {
            orbs.get_next_response();
            fail ("should have raised NO_IMPLEMENT");
        }
        catch (org.omg.CORBA.NO_IMPLEMENT ex)
        {
            // ok
        }

        try
        {
            orbs.list_initial_services();
            fail ("should have raised NO_IMPLEMENT");
        }
        catch (org.omg.CORBA.NO_IMPLEMENT ex)
        {
            // ok
        }

        try
        {
            orbs.object_to_string(null);
            fail ("should have raised NO_IMPLEMENT");
        }
        catch (org.omg.CORBA.NO_IMPLEMENT ex)
        {
            // ok
        }

        try
        {
            orbs.poll_next_response();
            fail ("should have raised NO_IMPLEMENT");
        }
        catch (org.omg.CORBA.NO_IMPLEMENT ex)
        {
            // ok
        }

        try
        {
            orbs.resolve_initial_references("");
            fail ("should have raised NO_IMPLEMENT");
        }
        catch (org.omg.CORBA.NO_IMPLEMENT ex)
        {
            // ok
        }
        catch (Exception ex)
        {
            fail ("should have raised NO_IMPLEMENT");
        }
            
        try
        {
            orbs.send_multiple_requests_deferred(null);
            fail ("should have raised NO_IMPLEMENT");
        }
        catch (org.omg.CORBA.NO_IMPLEMENT ex)
        {
            // ok
        }

        try
        {
            orbs.send_multiple_requests_oneway(null);
            fail ("should have raised NO_IMPLEMENT");
        }
        catch (org.omg.CORBA.NO_IMPLEMENT ex)
        {
            // ok
        }

        try
        {
            orbs.run();
            fail ("should have raised NO_IMPLEMENT");
        }
        catch (org.omg.CORBA.NO_IMPLEMENT ex)
        {
            // ok
        }

        try
        {
            orbs.shutdown(true);
            fail ("should have raised NO_IMPLEMENT");
        }
        catch (org.omg.CORBA.NO_IMPLEMENT ex)
        {
            // ok
        }

        try
        {
            orbs.work_pending();
            fail ("should have raised NO_IMPLEMENT");
        }
        catch (org.omg.CORBA.NO_IMPLEMENT ex)
        {
            // ok
        }

        try
        {
            orbs.perform_work();
            fail ("should have raised NO_IMPLEMENT");
        }
        catch (org.omg.CORBA.NO_IMPLEMENT ex)
        {
            // ok
        }        

    }
}
