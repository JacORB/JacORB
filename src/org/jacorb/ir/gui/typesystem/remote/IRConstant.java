/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2002 Gerald Brose
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

import java.util.*;
import org.omg.CORBA.*;
import javax.swing.tree.*;
 
public class IRConstant extends IRNodeWithType 
{
    protected java.lang.Object value;

    /**
     * Default-Konstruktor: wird von TypeSystem.createNode(...) benutzt
     */
    public IRConstant ( ) {
	super();
    }

    /**
     * This method was created by a SmartGuide.
     * @param irObject org.omg.CORBA.IRObject
     */

    public IRConstant ( IRObject irObject) 
    {
	super(irObject);
	ConstantDef constantDef = ConstantDefHelper.narrow((org.omg.CORBA.Object)irObject);
	setAssociatedTypeSystemNode(RemoteTypeSystem.createTypeSystemNode(constantDef.type_def()));
	Any any = constantDef.value();
	this.value = org.jacorb.ir.gui.remoteobject.ObjectRepresentantFactory.objectFromAny(any);
    }

    /**
     * This method was created by a SmartGuide.
     * @return java.lang.String
     */

    public  String description() {
	String result = super.description();
	result = result + "\nConstant value =\t" + value;
	return result;
    }

    /**
     * This method was created by a SmartGuide.
     * @return java.lang.String
     */
    public static String nodeTypeName() {
	return "const";
    }
}











