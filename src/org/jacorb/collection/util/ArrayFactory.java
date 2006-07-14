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

/**
 * @author Gerald Brose
 * @version $Id$
 */
public class ArrayFactory
{
    private static final int MAX = 16;
    private static final int MIN_OFFSET = 4;
    private static final int THREASHOLD = 16;
    private static DynArray [] pool = new DynArray[ MAX ];

    static
    {
        for( int i = MAX; i > 0;)
        {
            pool[--i]=new DynArray();
        }
    }

    private final static int log2(int n){
        int l =0;
        int nn = n-1;
        while( (nn >>l) != 0 )
        {
            l++;
        }

        return l;
    }

    public static void release()
    {
        for( int i= MAX; i > 0; )
        {
            i--;
            pool[i].removeAllElements();
        }
    }

    public static synchronized Object [] get_array( int capacity ){
        int log = 0;
        if( capacity != 0 )
        {
            log = log2(capacity);
        }
        DynArray v= pool[log > MIN_OFFSET ? log-MIN_OFFSET : 0 ];

        if( ! v.isEmpty() )
        {
            Object o = v.lastElement();
            v.removeElementAt(v.size()-1);
            return (Object [])o;
        }
        return new Object[log > MIN_OFFSET ? 1<<log : 1 << MIN_OFFSET ];
    }

    public static synchronized void free_array( Object [] current ){
        int log_curr = log2(current.length);
        if( log_curr-MIN_OFFSET < pool.length )
        {
            clear_array( current );
            DynArray v = pool[ log_curr-MIN_OFFSET ];
            if( v.size() < THREASHOLD )
            {
                v.addElement( current );
            }
        }
    }

    private static void clear_array( Object [] current ){
        for( int i=current.length;i>0;)
        {
            current[--i] = null;
        }
    }
}
