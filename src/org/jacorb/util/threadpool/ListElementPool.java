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
package org.jacorb.util.threadpool;
/**
 * ElementPool.java
 *
 *
 * Created: Thu Jan  4 14:56:29 2002
 *
 * @author Nicolas Noffke
 * $Id$
 */
import java.util.Stack;

public class ListElementPool  
{
    private Stack pool = null;
    private int max_size = 0;
    
    public ListElementPool( int max_size ) 
    {
        pool = new Stack();
        this.max_size = max_size;
    }

    public ListElement getElement()
    {
        if( pool.size() == 0 )
        {
            return new ListElement();
        }
        else
        {
            return (ListElement) pool.pop();
        }
    }
    
    public void returnElement( ListElement el )
    {
        if( pool.size() < max_size )
        {
            //clean up
            el.setNext( null );
            el.setData( null );
            
            pool.push( el );
        }
        //else throw away        
    }
} // ListElementPool






