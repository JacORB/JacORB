package org.jacorb.notification;

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


import java.util.List;
import java.util.AbstractList;
import java.util.RandomAccess;
import java.io.Serializable;

/**
 * JacORBCollections.java
 *
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class JacORBCollections implements CollectionsWrapper.CollectionsOperations
{
    public JacORBCollections()
    {
	
    }

    public List singletonList(Object o) {
	return new SingletonList(o);
    }
    
    private static class SingletonList extends AbstractList
	implements RandomAccess, Serializable {

	private final Object singletonElement_;

	SingletonList(Object element) {
	    singletonElement_ = element;
	}

	public int size() {
	    return 1;
	}

	public boolean contains(Object object) {
	    if (object == null && singletonElement_ == null) {
		return true;
	    }
	    return object.equals(singletonElement_);
	}

	public Object get(int index) {
	    if (index != 0) {
		throw new IndexOutOfBoundsException("Index: " + index);
	    }
	    return singletonElement_;
	}
    }
}
