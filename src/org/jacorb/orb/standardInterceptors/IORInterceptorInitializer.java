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

    public IORInterceptorInitializer() 
    {
    }

    // implementation of org.omg.PortableInterceptor.ORBInitializerOperations interface

    /**
     * Adds the SSLComponentInterceptor and the CodeSetInfoInterceptor
     * to the set of IORInterceptors.
     *
     * @param info the info object.
     */
    public void post_init(ORBInitInfo info)
    {
        Configuration config = null;
        try
        {
            ORB orb = ((org.jacorb.orb.portableInterceptor.ORBInitInfoImpl) info).getORB();
            config = orb.getConfiguration();

//             String supportedOptions = 
//                 config.getAttribute("jacorb.security.ssl.server.supported_options",null);
//             String requiredOptions = 
//                 config.getAttribute("jacorb.security.ssl.server.required_options", null);

//             if( config.getAttribute("jacorb.security.support_ssl","off").equals("on") &&
//                 supportedOptions != null &&
//                 requiredOptions != null
//                 )
//             {
//                 info.add_ior_interceptor(new SSLComponentInterceptor(orb));
//             }

            int giop_minor =
                config.getAttributeAsInteger("jacorb.giop_minor_version",2);

            if( giop_minor > 0 )
            {
                info.add_ior_interceptor(new CodeSetInfoInterceptor(orb));
            }
        }
        catch (Exception e)
        {
            if (config!= null)
            {
                Logger logger = 
                    config.getNamedLogger("org.jacorb.interceptors.ior_init");
                if (logger.isErrorEnabled())
                    logger.error("During IORInterceptor.post_init(): " + e.getMessage());
            }
            else
                e.printStackTrace(); // last resort...
        }
    }

    /**
     *
     * @param info <description>
     */

    public void pre_init(ORBInitInfo info)
    {
        // do nothing
    }

} // IORInterceptorInitializer
