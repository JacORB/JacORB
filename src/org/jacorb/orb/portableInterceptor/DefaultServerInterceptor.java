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
package org.jacorb.orb.portableInterceptor;

import org.omg.PortableInterceptor.*;

/**
 * DefaultServerInterceptor.java
 *
 * A simple base class for user-defined server interceptors
 *
 * @author Gerald Brose.
 * @version $Id$
 */

public abstract class DefaultServerInterceptor
    extends org.omg.CORBA.LocalObject 
    implements ServerRequestInterceptor
{

    // InterceptorOperations interface
    public abstract String name();

    public void destroy()
    {
    }

    public void receive_request_service_contexts( ServerRequestInfo ri ) 
        throws ForwardRequest
    {
    }

    public void receive_request( ServerRequestInfo ri )
        throws ForwardRequest
    {
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

}






