package org.jacorb.test.common;

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

import java.util.Vector;
import java.lang.reflect.Method;

/**
 * Utility class used to setup JUnit-TestSuite 
 * see file
 * ../notification/org/jacorb/test/notification/EventChannelTest.java
 * for an usage example.
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class TestUtils 
{
    
    private static final String[] STRING_ARRAY_TEMPLATE = new String[0];
    
    /**
     * this method returns a List of all public Methods which Names
     * start with the Prefix "test" and accept no Parameters e.g:
     *
     * <ul>
     * <li>testOperation
     * <li>testSomething
     * </ul>
     *
     */
    public static String[] getTestMethods(Class clazz) {
	return getTestMethods(clazz, "test");
    }

    public static String[] getTestMethods(Class clazz, 
					  String prefix) {

	Method[] methods = clazz.getMethods();

	Vector result = new Vector();

	for (int x=0; x<methods.length; ++x) {
	    if (methods[x].getName().startsWith(prefix)) {
		if (methods[x].getParameterTypes().length == 0) {
		    result.add(methods[x].getName());
		}
	    }
	}

	return (String[])result.toArray(STRING_ARRAY_TEMPLATE);
    }    
}
