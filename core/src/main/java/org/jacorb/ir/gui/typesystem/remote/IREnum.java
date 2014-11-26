/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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
import org.omg.CORBA.EnumDef;
import org.omg.CORBA.EnumDefHelper;

public class IREnum 
    extends IRNode 
    implements AbstractContainer 
{

    /**
     * IRStruct constructor comment.
     */
    protected IREnum() {
        super();
    }
    /**
     * IRStruct constructor comment.
     * @param irObject org.omg.CORBA.IRObject
     */
    protected IREnum(org.omg.CORBA.IRObject irObject) {
        super(irObject);
    }
    /**
     * contents method comment.
     */
    public org.jacorb.ir.gui.typesystem.ModelParticipant[] contents() 
    {
        EnumDef enumDef =
            EnumDefHelper.narrow((org.omg.CORBA.Object)this.irObject);
        String[] contents = enumDef.members();  
        org.jacorb.ir.gui.typesystem.TypeSystemNode[] result = 
            new org.jacorb.ir.gui.typesystem.TypeSystemNode[contents.length];
        for (int i=0; i<contents.length; i++) 
        {
            result[i] = RemoteTypeSystem.createTypeSystemNode(contents[i]);
        } // for
        return result;  
    }
    /**
     * This method was created by a SmartGuide.
     * @return java.lang.String
     */
    public static String nodeTypeName() {
        return "enum";
    }
}











