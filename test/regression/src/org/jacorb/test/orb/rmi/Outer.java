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

package org.jacorb.test.orb.rmi;

import java.io.Serializable;

public class Outer implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final String id;
    
    class Inner implements Serializable
    {
        private static final long serialVersionUID = 1L;
        private final String innerId;
        public Inner(String id)
        {
            innerId = id;
        }
        
        public boolean equals(Object obj)
        {
            return innerId.equals(((Inner)obj).innerId);
        }
    }
    
    static class StaticInner implements Serializable
    {
        private static final long serialVersionUID = 1L;
        private final String innerId;
        public StaticInner(String id)
        {
            innerId = id;
        }
        
        public boolean equals(Object obj)
        {
            return innerId.equals(((StaticInner)obj).innerId);
        }
    }
    
    public Inner inner;
    public StaticInner staticInner;
    
    public Outer(String id)
    {
        inner = new Inner(id);
        staticInner = new StaticInner(id);
        this.id = id;
    }
    
    public boolean equals(Object obj)
    {
        Outer other = (Outer) obj;
        
        return id.equals(other.id) && inner.equals(other.inner) && staticInner.equals(other.staticInner);
    }
}
