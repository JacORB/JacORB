package org.jacorb.orb.standardInterceptors;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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

import org.jacorb.config.Configuration;
import org.jacorb.orb.ORB;
import org.omg.CORBA.INTERNAL;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.slf4j.Logger;

/**
 * This class initializes the default IOR interceptors
 * used by JacORB.
 *
 * @author Nicolas Noffke
 */

public class IORInterceptorInitializer
    extends org.omg.CORBA.LocalObject
    implements ORBInitializer
{
    /**
     * Adds the CodeSetInfoInterceptor
     * to the set of IORInterceptors.
     *
     * @param info the info object.
     */
    public void post_init(ORBInitInfo info)
    {
        final ORB orb = ((org.jacorb.orb.portableInterceptor.ORBInitInfoImpl) info).getORB();
        final Configuration config = orb.getConfiguration();
        final Logger logger = config.getLogger("org.jacorb.interceptors.ior_init");

        try
        {
            int giop_minor =
                config.getAttributeAsInteger("jacorb.giop_minor_version", 2);

            if( giop_minor > 0 )
            {
                info.add_ior_interceptor(new CodeSetInfoInterceptor(orb));
            }
        }
        catch (Exception e)
        {
            logger.error ("Unexpected exception in IORInterceptorInitializer", e);
            throw new INTERNAL(e.toString());
        }
    }

    public void pre_init(ORBInitInfo info)
    {
        // do nothing
    }

} // IORInterceptorInitializer
