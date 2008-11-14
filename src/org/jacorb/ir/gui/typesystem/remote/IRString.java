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
package org.jacorb.ir.gui.typesystem.remote;

import org.omg.CORBA.StringDef;
import org.omg.CORBA.StringDefHelper;

/**
 * 
 */

public class IRString 
    extends IRNode
{
    int bound;

   /**
     * IRAliasDef constructor comment.
     */
    protected IRString() 
    {
	super();
    }

    /**
     * IRAliasDef constructor comment.
     * @param irObject org.omg.CORBA.IRObject
     */

    protected IRString(org.omg.CORBA.IRObject irObject) 
    {
	super(irObject);
	StringDef stringDef = StringDefHelper.narrow(irObject);
        bound = stringDef.bound();
		setName("string");
		setAbsoluteName("string");
    }

    /**
     * @return java.lang.String
     */

    public static String nodeTypeName() 
    {
	return "string";
    }

    /**
     * @return java.lang.String
     */

    public String description() 
    {
	String result = "string\nbound:\t" + bound;
	return result;
    }

}











