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
import java.lang.Class;
import java.util.Collections;

/**
 * provides a simple wrapper around java.util.Collections. Notification
 * Service uses the Method Collections.singletonList. This method is not
 * available in a pre 1.3 JDK.
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class CollectionsWrapper {

    interface CollectionsOperations {
	public List singletonList(Object o);
    }
 
    private static CollectionsOperations delegate_;

    static {
	boolean jdk13available;

	String className;
	
	try {
	    Collections.class.getMethod("singletonList", new Class[] {Object.class});

	    className = "org.jacorb.notification.JDK13Collections";
	} catch (Exception e) {
	    className = "org.jacorb.notification.JacORBCollections";
	}
	
	try {
	    Class clazz = Class.forName(className);
	    delegate_ = (CollectionsOperations)clazz.newInstance();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static List singletonList(Object o) {
	return delegate_.singletonList(o);
    }

} 
