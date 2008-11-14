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
 
import org.omg.CORBA.IRObject;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.omg.CORBA.ValueMemberDef;
import org.omg.CORBA.ValueMemberDefHelper;
 
public class IRValueMember 
    extends IRNodeWithType 
{
    /**
     * Default constructor, called from  TypeSystem.createNode(...)
     */
    public IRValueMember()
    {
        super();
    }

    /**
     * @param irObject org.omg.CORBA.IRObject
     */
    public IRValueMember(IRObject irObject) 
    {
        super(irObject);
        ValueMemberDef valueMemberDef = ValueMemberDefHelper.narrow(irObject);
        setAssociatedTypeSystemNode(RemoteTypeSystem.createTypeSystemNode(valueMemberDef.type_def()));
    }

    /**
     * @return java.lang.String
     */
    public String getInstanceNodeTypeName() 
    {
        String access;
        short visibility = ValueMemberDefHelper.narrow((org.omg.CORBA.Object)irObject).access();

        switch (visibility) {
            case PUBLIC_MEMBER.value:
                access = "public ";
                break;
            case PRIVATE_MEMBER.value:
                access = "private ";
                break;
            default:
                access = "<unknown visibility> ";
                break;
        }

        return access + super.getInstanceNodeTypeName();
    }

    /**
     * @return A string denoting the node type implemented here.
     */
    public static String nodeTypeName() 
    {
        return "valuemember";
    }
}
