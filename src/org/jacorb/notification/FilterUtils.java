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

/**
 * Collection of Utility methods used within the Filterimplementation.
 *
 * Created: Fri Nov 01 17:19:54 2002
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class FilterUtils {

    /**
     * Provide a Uniform Mapping from domain_name and type_name to a
     * Unique Key that can be used to put EventTypes into a Map.
     * if (d1 == d2) AND (t1 == t2) => calcConstraintKey(d1, t1) ==
     * calcConstraintKey(d2, t2).
     * 
     * @param domain_name a <code>String</code> value
     * @param type_name a <code>String</code> value
     * @return an Unique Constraint Key.
     */
    public static String calcConstraintKey(String domain_name, String type_name) {
	StringBuffer _b = new StringBuffer(domain_name);
	_b.append("__%%__");
	_b.append(type_name);

	return _b.toString();
    }    

}// FilterUtils
