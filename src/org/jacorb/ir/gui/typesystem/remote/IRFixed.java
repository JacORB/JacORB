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

import org.omg.CORBA.FixedDef;
import org.omg.CORBA.FixedDefHelper;

/**
 * 
 */

public class IRFixed 
    extends IRNode
{
    short digits;
    short scale;

   /**
     * IRAliasDef constructor comment.
     */
    protected IRFixed() 
    {
	super();
    
    }

    /**
     * IRAliasDef constructor comment.
     * @param irObject org.omg.CORBA.IRObject
     */

    protected IRFixed(org.omg.CORBA.IRObject irObject) 
    {
	super(irObject);
	FixedDef fixedDef = FixedDefHelper.narrow((org.omg.CORBA.Object)irObject);
        digits = fixedDef.digits();
        scale = fixedDef.scale();
        //	setAssociatedTypeSystemNode(RemoteTypeSystem.createTypeSystemNode(aliasDef.original_type_def()));
    }

    /**
     * @return java.lang.String
     */

    public static String nodeTypeName() 
    {
	return "fixed";
    }

    /**
     * @return java.lang.String
     */

    public String description() 
    {
	String result = "fixed\ndigits:\t" + digits + "\nscale:\t" + scale;
	return result;
    }

}











