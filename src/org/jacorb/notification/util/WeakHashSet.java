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

package org.jacorb.notification.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class WeakHashSet implements Set
{
    private final Map entries_ = new WeakHashMap();

    private static final Object PRESENT = new Object();

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#size()
     */
    public int size()
    {
        return entries_.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#isEmpty()
     */
    public boolean isEmpty()
    {
        return entries_.isEmpty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(Object o)
    {
        return PRESENT == entries_.get(o);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#iterator()
     */
    public Iterator iterator()
    {
        return entries_.keySet().iterator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#toArray()
     */
    public Object[] toArray()
    {
        return entries_.keySet().toArray();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#toArray(java.lang.Object[])
     */
    public Object[] toArray(Object[] a)
    {
        return entries_.keySet().toArray(a);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#add(java.lang.Object)
     */
    public boolean add(Object o)
    {
        return entries_.put(o, PRESENT) == null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#remove(java.lang.Object)
     */
    public boolean remove(Object o)
    {
        return entries_.remove(o) != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection c)
    {
        Iterator i = c.iterator();
        while (i.hasNext())
        {
            if (!(PRESENT == entries_.get(i.next())))
            {
                return false;
            }
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#addAll(java.util.Collection)
     */
    public boolean addAll(Collection c)
    {
        boolean modified = false;
        Iterator i = c.iterator();
        while (i.hasNext())
        {
            modified |= add(i.next());
        }
        return modified;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection c)
    {
        boolean modified = false;
        Iterator i = entries_.keySet().iterator();
        while(i.hasNext())
        {
            if (!c.contains(i.next()))
            {
                i.remove();
                modified = true;
            }
        }
        return modified;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection c)
    {
        boolean modified = false;
        Iterator i = c.iterator();
        while (i.hasNext())
        {
            modified |= remove(i.next());
        }
        return modified;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#clear()
     */
    public void clear()
    {
        entries_.clear();
    }
}