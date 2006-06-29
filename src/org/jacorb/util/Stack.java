/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) The JacORB project, 1997-2006.
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

package org.jacorb.util;

import java.util.EmptyStackException;
import java.util.LinkedList;

/**
 * unsynchronized implementation of a stack (LIFO queue)
 *
 * @see java.util.Stack
 * @author Alphonse Bendt
 * @version $Id$
 */
public class Stack
{
    private final LinkedList elements = new LinkedList();

    public boolean empty()
    {
        return elements.isEmpty();
    }

    public Object push(Object element)
    {
        elements.add(element);

        return element;
    }

    public Object pop()
    {
        if (empty())
        {
            throw new EmptyStackException();
        }

        return elements.removeLast();
    }

    public Object peek()
    {
        if (empty())
        {
            throw new EmptyStackException();
        }

        return elements.getLast();
    }

    public int search(Object value)
    {
        final int result;

        if (empty())
        {
            result =  -1;
        }
        else
        {
            result = elements.size() - elements.lastIndexOf(value);
        }

        return result;
    }
}
