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
 * LinkedListQueue.java
 *
 *
 * Created: Thu Dec 21 11:35:52 2000
 *
 * @author Nicolas Noffke
 * $Id$
 */
import java.util.Vector;

public class LinkedListQueue
    implements ThreadPoolQueue
{
    private ListElement first = null;
    private ListElement last = null;

    private ListElementPool element_pool = null;

    public LinkedListQueue( int pool_size )
    {
        element_pool = new ListElementPool( pool_size );
    }

    public LinkedListQueue()
    {
        this( 100 );
    }
    
    public synchronized boolean add( Object job )
    {
        ListElement el = element_pool.getElement();

        if( first == null )
        {
            first = el;
            last = el;
        }
        else
        {
            last.setNext( el );
            last = el;
        }
        
        el.setData( job );
        
        return true;
    }

    public Object removeFirst()
    {
        ListElement el = first;
        Object data = el.getData();

        if( first == last )
        {
            first = null;
            last = null;
        }
        else
        {
            first = first.getNext();
        }

        element_pool.returnElement( el );        
        return data;
    }

    public boolean isEmpty()
    {
        return first == null;
    }
} // LinkedListQueue






