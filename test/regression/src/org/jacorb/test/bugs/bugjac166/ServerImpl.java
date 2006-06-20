package org.jacorb.test.bugs.bugjac166;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.jacorb.test.bugs.bugjac74.Jac074ServerPOA;
import org.omg.CORBA.Any;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.InvalidSlot;


/**
 * <code>ServerImpl</code> is a simple server to extract the data from the
 * Interceptor and return it to the client.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class ServerImpl extends Jac074ServerPOA implements Configurable
{
    private ORB orb;

    /**
     * Describe <code>ping</code> method here.
     *
     * @return a <code>String</code> value
     */
    public String ping()
    {
        String result = "";

        try
        {
            Current current = (Current)orb.resolve_initial_references("PICurrent");

            Any anyName = current.get_slot( IPInitializer.slotID );

            result = anyName.extract_string();
         }
         catch (InvalidSlot e)
         {
             throw new INTERNAL(e.toString());
         }
         catch (InvalidName e)
         {
             throw new INTERNAL(e.toString());
         }
        return result;
    }

    public void configure(Configuration arg0) throws ConfigurationException
    {
        orb = ((org.jacorb.config.Configuration)arg0).getORB();
    }
}
