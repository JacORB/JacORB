/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

package org.jacorb.orb.giop;

import org.jacorb.orb.ORB;
import org.omg.BiDirPolicy.BIDIRECTIONAL_POLICY_TYPE;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.slf4j.Logger;

/**
 * @author Nicolas Noffke
 */

public class BiDirConnectionInitializer
  extends org.omg.CORBA.LocalObject
  implements ORBInitializer
{
    public void post_init(ORBInitInfo info)
    {
        final ORB orb = ((org.jacorb.orb.portableInterceptor.ORBInitInfoImpl) info).getORB();
        final Logger logger = orb.getConfiguration().getLogger("org.jacorb.interceptors.ior_init");

        try
        {
            info.add_client_request_interceptor( new BiDirConnectionClientInterceptor( orb ));
            info.add_server_request_interceptor( new BiDirConnectionServerInterceptor( orb ));

            info.register_policy_factory( BIDIRECTIONAL_POLICY_TYPE.value,
                                          new BiDirPolicyFactory() );
        }
        catch (Exception e)
        {
            logger.error("BiDirConnectionInitializer.post_init", e);
        }
    }

    public void pre_init(ORBInitInfo info)
    {
        // do nothing
    }
} // BiDirConnectionInitializer
