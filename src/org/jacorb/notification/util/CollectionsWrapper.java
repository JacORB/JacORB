package org.jacorb.notification.util;

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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

/**
 * provides a simple wrapper around java.util.Collections. Notification Service uses the Method
 * Collections.singletonList. This method is not available in a pre 1.3 JDK.
 * 
 * @author Alphonse Bendt
 * @author Marc Heide
 * 
 * @version $Id$
 */

public class CollectionsWrapper
{
    private static Method singletonListMethod;

    static
    {
        try
        {
            singletonListMethod = Collections.class.getMethod("singletonList",
                    new Class[] { Object.class });
        } catch (Exception e)
        {
            singletonListMethod = null;
        }
    }

    public static List singletonList(Object o)
    {
        if (singletonListMethod != null)
        {
            try
            {
                return (List) (singletonListMethod.invoke(null, new Object[] { o }));
            } catch (Exception e)
            {
                // ignore. return out implementation. should not happen.
            }
        }
        return new SingletonList(o);
    }

    private static class SingletonList extends AbstractList implements Serializable
    {
        private static final long serialVersionUID = 1L;
        private final Object singletonElement_;

        SingletonList(Object element)
        {
            singletonElement_ = element;
        }

        public int size()
        {
            return 1;
        }

        public boolean contains(Object object)
        {
            if (singletonElement_ == null)
            {
                if (object == null)
                {
                    return true;
                }
                return false;
            }

            return object.equals(singletonElement_);
        }

        public Object get(int index)
        {
            if (index != 0)
            {
                throw new IndexOutOfBoundsException("Index: " + index);
            }

            return singletonElement_;
        }
    }
}