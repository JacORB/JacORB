/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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
package org.jacorb.util;

/**
 * This class provides thread specific system
 * properties, i.e., a call to System.setProperty()
 * does not alter the properties accessible by
 * System.getProperty() from any other thread. <br>
 *
 * Properties are inherited downwards, i.e., if a
 * thread creates a new thread, that child will get
 * a copy of its parents threads properties. <br>
 *
 * WARNING: The use of any other methods than getProperty(),
 * setProperty() and propertyNames(), may result in 
 * unintended behavior. On the other hand, put() and get()
 * might be used for a set of properties, common to all
 * threads.
 *
 * @author Nicolas Noffke
 * $Id$
 */
import java.util.*;

public class ThreadSystemProperties 
    extends Properties 
{
    private InheritableThreadLocal delegate_props = null;

    public ThreadSystemProperties( Properties initial ) 
    {
        super();

        delegate_props = new TSPITL();
        delegate_props.set( initial );
    }

    public String getProperty( String key )
    {
        return ((Properties) delegate_props.get()).getProperty( key ); 
    }

    public Object setProperty( String key, String value )
    {
        return ((Properties) delegate_props.get()).setProperty( key, value ); 
    }

    public Enumeration propertyNames()
    {   
        return ((Properties) delegate_props.get()).propertyNames(); 
    }

    /**
     * This must be overridden, because jdk1.2 uses this method
     * instead of the "correct" setProperty().
     */
    public Object put( Object key, Object value )
    {
        return ((Properties) delegate_props.get()).setProperty( (String) key, 
                                                                (String) value ); 
    }

    private class TSPITL
        extends InheritableThreadLocal
    {
        public TSPITL()
        {
            super();
        }

        protected Object childValue( Object parentValue )
        {
            return ((Properties) parentValue).clone();
        }
    }
    
} // ThreadSystemProperties






