package org.jacorb.security.ssl.sun_jsse;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2002-2012 Gerald Brose / The JacORB Team.
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

import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;
import org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName;

public class SecurityServiceInitializer
    extends org.omg.CORBA.LocalObject
    implements ORBInitializer
{
    /**
    * This method registers the interceptors.
    */
    public void post_init( ORBInitInfo info )
    {
        try
        {
            org.omg.SecurityLevel2.Current current = null;
            try
            {
                org.omg.CORBA.Object sc =
                    info.resolve_initial_references("SecurityCurrent");

                current = org.omg.SecurityLevel2.CurrentHelper.narrow(sc);
            }
            catch (InvalidName in)
            {
            }

            info.add_server_request_interceptor(
                new ServerInvocationInterceptor(
                         current,
                         ((org.jacorb.orb.portableInterceptor.ORBInitInfoImpl)info).getORB()
                         )
                );
        }
        catch (DuplicateName duplicateName)
        {
            duplicateName.printStackTrace();
        }
        catch (Exception ce)
        {
            throw new org.omg.CORBA.INITIALIZE(ce.getMessage());
        }
    }

    public void pre_init(ORBInitInfo info)
    {
        // we don't to define initial references
        // because the security current if already defined.

        // we reserve slots for the security contexts

    }
}    // SSL setup Initializer






