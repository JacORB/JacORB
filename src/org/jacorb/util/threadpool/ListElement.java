/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2002 Gerald Brose
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
package org.jacorb.util.threadpool;
/**
 * ListElement.java
 *
 *
 * Created: Thu Jan  4 14:33:48 2002
 *
 * @author Nicolas Noffke
 * $Id$
 */

public class ListElement  
{
    private ListElement next = null;
    private Object data = null;

    public ListElement() 
    {        
    }
        
    
    /**
     * Get the value of data.
     * @return Value of data.
     */
    public Object getData() 
    {
        return data;
    }
    
    /**
     * Set the value of data.
     * @param v  Value to assign to data.
     */
    public void setData( Object v ) 
    {
        this.data = v;
    }
    
    /**
     * Get the value of next.
     * @return Value of next.
     */
    public ListElement getNext() 
    {
        return next;
    }
    
    /**
     * Set the value of next.
     * @param v  Value to assign to next.
     */
    public void setNext( ListElement v ) 
    {
        this.next = v;
    }       
} // ListElement






