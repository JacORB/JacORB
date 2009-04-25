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
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoIntrospectionException;
import org.picocontainer.defaults.AbstractComponentAdapter;
import org.jacorb.config.*;

class ConfigurationComponentAdapter extends AbstractComponentAdapter
{
    private static final long serialVersionUID = 1L;

    public ConfigurationComponentAdapter()
    {
        super(Configuration.class, Configuration.class);
    }
    
    public Object getComponentInstance(PicoContainer container)
    {
        ORB orb = (ORB) container.getComponentInstance(ORB.class);

        Configuration config = ((org.jacorb.orb.ORB) orb).getConfiguration();

        return config;
    }
    
    public void verify(PicoContainer container)
    {
        org.jacorb.orb.ORB jorb = 
            (org.jacorb.orb.ORB) container.getComponentInstance(ORB.class);
        
        if (jorb == null)
        {
            throw new PicoIntrospectionException("Need JacORB ORB");
        }
    }
}