/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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
package org.jacorb.security.level2;

import org.omg.PortableInterceptor.*;
import org.omg.SecurityLevel2.*;
import org.jacorb.util.*;
import org.jacorb.orb.portableInterceptor.ServerRequestInfoImpl;
import java.util.*;

/**
 * ServerAccessDecisionInterceptor.java
 *
 *
 * Created: Wed Jul  5 14:31:30 2000
 *
 * @author Nicolas Noffke
 * $Id$
 */

public class ServerAccessDecisionInterceptor 
    extends org.omg.CORBA.LocalObject 
    implements ServerRequestInterceptor
{
    public static final String DEFAULT_NAME = 
        "ServerAccessDecisionInterceptor";

    private String name = null;

    private AccessDecision access_decision = null;
    private org.omg.SecurityLevel2.Current current = null;
    private Hashtable special_operations = null;

    public ServerAccessDecisionInterceptor
        (org.omg.SecurityLevel2.Current current) 
    {
        this( current, DEFAULT_NAME );
    }

    public ServerAccessDecisionInterceptor
        (org.omg.SecurityLevel2.Current current, 
         String name ) 
    {
        this.current = current;
        this.name = name;

        access_decision = current.access_decision();

        special_operations = new Hashtable();
        special_operations.put("_is_a", "");
        special_operations.put("_get_interface", "");
        special_operations.put("_non_existent", "");

        special_operations.put("_get_policy", "");
        special_operations.put("_get_domain_managers", "");
        special_operations.put("_set_policy_overrides", "");
    }

    // InterceptorOperations interface
    public String name()
    {
        return name;
    }

    public void destroy()
    {
    } 

    /**
     * Put the propagation context from the service context
     * into the PICurrent.
     */
    public void receive_request_service_contexts(ServerRequestInfo ri) 
        throws ForwardRequest
    {
    }

    public void receive_request(ServerRequestInfo ri)
        throws ForwardRequest
    {
        //Debug.output(1,"Call to: " + ri.target_most_derived_interface() );

        if (special_operations.containsKey(ri.operation()))
        {
            //System.out.println("Ignoring op " + ri.operation());            
            return;
        }
//         else if (ri.target_most_derived_interface().
//                  startsWith("IDL:jacorb/orb/domain"))
//         {
//             //System.out.println("Ignoring call to domain object");            
//             return;
//         }
        else
        {
            //System.out.println("(ServerAccessDecInterc.)Controlling operation: " + ri.operation());
        }

        //proprietary call!!
        org.omg.CORBA.Object target = 
            ((ServerRequestInfoImpl) ri).target();

            if (! access_decision.access_allowed( 
                       new Credentials[] { current.received_credentials() },
                       target,
                       ri.operation(),
                       ri.target_most_derived_interface())
                )
            {
                throw new org.omg.CORBA.NO_PERMISSION();
            }
            else
            {
                //System.out.println("Access allowed!!");
            }
    }

    public void send_reply(ServerRequestInfo ri)
    {
    }

    public void send_exception(ServerRequestInfo ri)
        throws ForwardRequest
    {
    }

    public void send_other(ServerRequestInfo ri) 
        throws ForwardRequest
    {
    }

} // ServerAccessDecisionInterceptor






