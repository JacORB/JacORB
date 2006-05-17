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
package org.jacorb.orb.standardInterceptors;

import org.apache.avalon.framework.logger.*;

import org.omg.CORBA.INTERNAL;
import org.omg.PortableInterceptor.*;

import org.jacorb.orb.*;
import org.jacorb.config.Configuration;

/**
 * This class initializes the default IOR interceptors
 * used by JacORB.
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class IORInterceptorInitializer
    extends org.omg.CORBA.LocalObject
    implements ORBInitializer
{
    /**
     * Adds the SSLComponentInterceptor and the CodeSetInfoInterceptor
     * to the set of IORInterceptors.
     *
     * @param info the info object.
     */
    public void post_init(ORBInitInfo info)
    {
        final ORB orb = ((org.jacorb.orb.portableInterceptor.ORBInitInfoImpl) info).getORB();
        final Configuration config = orb.getConfiguration();
        final Logger logger = config.getNamedLogger("org.jacorb.interceptors.ior_init");

        try
        {
            int giop_minor =
                config.getAttributeAsInteger("jacorb.giop_minor_version",2);

            if( giop_minor > 0 )
            {
                info.add_ior_interceptor(new CodeSetInfoInterceptor(orb));
            }
        }
        catch (Exception e)
        {
            logger.error("unexpected exception", e);
            throw new INTERNAL(e.getMessage());
        }
    }

    public void pre_init(ORBInitInfo info)
    {
        // do nothing
    }

} // IORInterceptorInitializer
