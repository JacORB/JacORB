/*
 * JacORB - a free Java ORB
 * 
 * Copyright (C) 1999-2004 Gerald Brose
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Library General Public License as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139,
 * USA.
 *  
 */

package org.jacorb.notification.container;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoInitializationException;
import org.picocontainer.defaults.AbstractComponentAdapter;

class POAComponentAdapter extends AbstractComponentAdapter
{
    private static final long serialVersionUID = 1L;
    
    public POAComponentAdapter()
    {
        super(POA.class, POA.class);
    }

    public Object getComponentInstance(PicoContainer container)
    {
        try
        {
            ORB orb = (ORB) container.getComponentInstance(ORB.class);

            POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

            return poa;
        } catch (InvalidName e)
        {
            throw new PicoInitializationException("could not resolve RootPOA", e);
        }
    }

    public void verify(PicoContainer container)
    {
        // no op
    }
}