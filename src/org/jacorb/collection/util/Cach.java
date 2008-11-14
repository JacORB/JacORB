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
package org.jacorb.collection.util;

import java.util.Enumeration;
import java.util.Vector;

public class Cach{
/* ------------------------------------------------------------------------- */
    class Node {
        Object key;
        Object element;
        Node( Object key, Object element ){
            this.key =key;
            this.element = element;
        }
    }
/* ------------------------------------------------------------------------- */
    private Vector data;
    private int capacity;
/* ------------------------------------------------------------------------- */
    public Cach( int capacity ){
        data = new Vector( capacity );
        this.capacity = capacity;
    }
/* ------------------------------------------------------------------------- */
    public Object getElement( Object key ){
        Enumeration enumeration = data.elements();
        while( enumeration.hasMoreElements() ){
            Node n =(Node)enumeration.nextElement();
            if( n.key == key ){
                data.removeElement( n );
                data.insertElementAt( n, 0 );
                return n.element;
            }
        }
        return null;
    }
/* ------------------------------------------------------------------------- */
    public void putElement( Object key, Object element ){
        if( data.size() >= capacity ){
            data.removeElementAt( data.size()-1 );
        }
        Node n = new Node( key, element );
        data.insertElementAt( n, 0 );
    }
/* ------------------------------------------------------------------------- */
    public void clear(){
        data.removeAllElements();
    }

}





