/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

package org.jacorb.orb.iiop;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kevin Conner (Kevin.Conner@arjuna.com)
 * @version $Id$
 */
public class IIOPLoopbackRegistry
{
    private static final IIOPLoopbackRegistry REGISTRY = new IIOPLoopbackRegistry() ;
    
    private final Map loopbackMap = new HashMap() ;
    
    private IIOPLoopbackRegistry()
    {
    }
    
    public static IIOPLoopbackRegistry getRegistry()
    {
        return REGISTRY ;
    }
    
    public synchronized void register(final IIOPAddress address,
                                      final IIOPLoopback loopback)
    {
        loopbackMap.put(address, loopback);
    }
    
    public synchronized void unregister(final IIOPAddress address)
    {
        loopbackMap.remove(address);
    }
    
    public synchronized IIOPLoopback getLoopback(final IIOPAddress address)
    {
        return (IIOPLoopback)loopbackMap.get(address);
    }
}
