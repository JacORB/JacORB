/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

package org.jacorb.idl;

import java.util.Hashtable;

/**
 * A table of defined typeprefixes
 * 
 * @author Alexander Birchenko
 */

public class TypePrefixes
{
    private static final Hashtable/*<String,String>*/ typePrefixes = new Hashtable/*<String,String>*/();

    public static void define(String moduleName, String prefix)
    {
        typePrefixes.put(moduleName, prefix);
    }

    public static boolean isDefined(String moduleName)
    {
        return typePrefixes.containsKey(moduleName);
    }

    public static String getDefined(String moduleName)
    {
        return (String)typePrefixes.get(moduleName);
    }

    public static Hashtable/*<String,String>*/ getTypePrefixes()
    {
        return typePrefixes;
    }
}
