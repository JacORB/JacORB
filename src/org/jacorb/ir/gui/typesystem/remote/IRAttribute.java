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
 * 
 */
 
import org.omg.CORBA.AttributeDef;
import org.omg.CORBA.AttributeDefHelper;
import org.omg.CORBA.AttributeMode;
import org.omg.CORBA.IRObject;
 
public class IRAttribute 
	extends IRNodeWithType 
{
	/**
	 * Default constructor, called from  TypeSystem.createNode(...)
	 */
	public IRAttribute ( ) {
		super();
	}

	/**
	 * @param irObject org.omg.CORBA.IRObject
	 */

	public IRAttribute( IRObject irObject) 
	{
		super(irObject);
		AttributeDef attributeDef = 
			AttributeDefHelper.narrow(irObject);
		setAssociatedTypeSystemNode(RemoteTypeSystem.createTypeSystemNode(attributeDef.type_def()));
	}

	/**
	 * @return java.lang.String
	 */

	public String getInstanceNodeTypeName ( ) 
	{
		String result = super.getInstanceNodeTypeName();
		if ( AttributeDefHelper.narrow((org.omg.CORBA.Object)irObject).mode().value() ==
			 AttributeMode._ATTR_READONLY) 
		{
			result = "readonly" + " " + result;
		}	
		return result;
	}

	/**
	 * @return java.lang.String
	 */
	public static String nodeTypeName() 
	{
		return "attribute";
	}
}











