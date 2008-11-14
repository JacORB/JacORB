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

import org.jacorb.ir.gui.typesystem.AbstractContainer;
import org.omg.CORBA.ExceptionDef;
import org.omg.CORBA.ExceptionDefHelper;
import org.omg.CORBA.IRObject;
import org.omg.CORBA.StructMember;

public class IRException 
    extends IRNode 
    implements AbstractContainer 
{
    /**
     * Default-Constructor used by TypeSystem.createNode(...)
     */

    public IRException () 
    {
	super();
    }

    /**
     * @param irObject org.omg.CORBA.IRObject
     */

    public IRException ( IRObject irObject) 
    {
	super(irObject);
    }

    /**
     * contents method comment.
     */

    public org.jacorb.ir.gui.typesystem.ModelParticipant[] contents() 
    {
	ExceptionDef exceptionDef = ExceptionDefHelper.narrow((org.omg.CORBA.Object)this.irObject);
	StructMember[] contents = exceptionDef.members();	

	org.jacorb.ir.gui.typesystem.TypeSystemNode[] result = 
            new org.jacorb.ir.gui.typesystem.TypeSystemNode[contents.length];

	for (int i=0; i<contents.length; i++) 
        {
            result[i] = RemoteTypeSystem.createTypeSystemNode(contents[i]);
	}

	return result;	
    }

    /**
     * This method was created by a SmartGuide.
     * @return java.lang.String
     */

    public static String nodeTypeName() {
	return "exception";
    }
}











