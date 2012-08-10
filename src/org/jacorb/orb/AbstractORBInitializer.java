/*
 *        JacORB  - a free Java ORB
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

package org.jacorb.orb;

import org.jacorb.config.Configuration;
import org.jacorb.orb.portableInterceptor.ORBInitInfoImpl;
import org.omg.CORBA.INITIALIZE;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.slf4j.Logger;

/**
 * @author Alphonse Bendt
 */
public class AbstractORBInitializer extends org.omg.CORBA.LocalObject implements ORBInitializer
{
    protected ORB orb;
    protected Configuration config;

    public final void pre_init(ORBInitInfo info)
    {
        orb = ((ORBInitInfoImpl)info).getORB();
        config = orb.getConfiguration();

        final Logger logger = config.getLogger("jacorb.orb.interceptor.pre_init");

        try
        {
            doPreInit(info);
        }
        catch(Exception e)
        {
            logger.error("unexpected exception during pre_init", e);

            throw new INITIALIZE(e.toString());
        }

        orb = null;
        config = null;
    }

    protected void doPreInit(ORBInitInfo info) throws Exception
    {
    }

    public final void post_init(ORBInitInfo info)
    {
        orb = ((ORBInitInfoImpl)info).getORB();
        config = orb.getConfiguration();

        final Logger logger = config.getLogger("jacorb.orb.interceptor.post_init");

        try
        {
            doPostInit(info);
        }
        catch(Exception e)
        {
            logger.error("unexpected exception during post_init", e);

            throw new INITIALIZE(e.toString());
        }

        orb = null;
        config = null;
    }

    protected void doPostInit(ORBInitInfo info) throws Exception
    {
    }
}
