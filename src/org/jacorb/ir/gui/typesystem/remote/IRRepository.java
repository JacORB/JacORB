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


/**
 * IRRepository wird gebraucht, um GUI erlaubte Child-Klassen mitzuteilen
 * 
 */
 
import org.omg.CORBA.IRObject;

public class IRRepository 
    extends IRContainer 
{       
    
    /**
     * Default-Konstruktor: wird von TypeSystem.createNode(...) benutzt
     */

    public IRRepository ( ) {
	super();
    }

    /**
     * @param irObject org.omg.CORBA.IRObject
     */

    public IRRepository ( IRObject irObject) 
    {
	super(irObject);
    }

    /**
     * @return java.lang.String[]
     */

    public String[] allowedToAdd() {
	String[] result = {	IRModule.nodeTypeName(),
                                IRInterface.nodeTypeName(),
                                IRConstant.nodeTypeName(),
                                IRTypedef.nodeTypeName(),
                                IRException.nodeTypeName()};
	return result;
    }

    /**
     * @return java.lang.String
     */

    public static String nodeTypeName() 
    {
	return "Repository";
    }
}











