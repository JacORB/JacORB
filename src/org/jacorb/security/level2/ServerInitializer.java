/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2002 Gerald Brose
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

import org.omg.SecurityLevel2.Current;
import org.jacorb.util.Debug;
/**
 *
 * @author Nicolas Noffke
 * $Id$
 */

public class ServerInitializer 
  extends org.omg.CORBA.LocalObject 
    implements org.omg.PortableInterceptor.ORBInitializer
{

    public ServerInitializer() 
    {
    }

    // implementation of ORBInitializerOperations interface
    /**
     * Registers the Interceptor with a codec and a slot id.
     */
    public void post_init(org.omg.PortableInterceptor.ORBInitInfo info) 
    {
        try
        {
            Current current = 
                (Current) info.resolve_initial_references("SecurityCurrent");

            info.add_server_request_interceptor
                (new ServerAccessDecisionInterceptor(current));
        }catch (Exception e)
        {
            Debug.output(Debug.SECURITY | Debug.IMPORTANT, e);
        }
    }

    /**
     *
     * @param info <description>
     */
    public void pre_init(org.omg.PortableInterceptor.ORBInitInfo info)
    {
    
    }

} // ServerInitializer






