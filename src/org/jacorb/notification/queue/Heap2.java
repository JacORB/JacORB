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

package org.jacorb.notification.queue;

import java.util.Arrays;
import java.util.Comparator;

import EDU.oswego.cs.dl.util.concurrent.Heap;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
class Heap2 extends Heap
{
    /**
     * @param capacity
     * @param cmp
     * @throws IllegalArgumentException
     */
    public Heap2(int capacity, Comparator cmp) throws IllegalArgumentException
    {
        super(capacity, cmp);
    }

    /**
     * @param capacity
     */
    public Heap2(int capacity)
    {
        super(capacity);
    }
    
    /* (non-Javadoc)
     * @see EDU.oswego.cs.dl.util.concurrent.Heap#clear()
     */
    public synchronized void clear()
    {
        super.clear();
        
        for (int i = 0; i < nodes_.length && nodes_[i] != null; ++i)
        {
            nodes_[i] = null;
        }
    }
    
    public String toString()
    {
        return Arrays.asList(nodes_).toString();
    }
}
