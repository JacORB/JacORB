/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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

import org.omg.CORBA.ParameterDescription;
import org.omg.CORBA.ParameterMode;
import org.omg.CORBA.TypeCode;

/**
 * This class was generated by a SmartGuide.
 *
 */
public class IRParameter extends IRNodeWithType
{
    private ParameterDescription parDesc;

    /**
     * IRParameter constructor comment.
     */
    protected IRParameter() {
    super();
    }
    /**
     * IRParameter constructor comment.
     */
    protected IRParameter(org.omg.CORBA.ParameterDescription parDesc) {
        setName(parDesc.name);
        setAssociatedTypeSystemNode(RemoteTypeSystem.createTypeSystemNode(parDesc.type_def));
        this.parDesc = parDesc;
    }

    public ParameterMode getMode() {
        return parDesc.mode;
    }

    /**
     * This method was created by a SmartGuide.
     * @return org.omg.CORBA.TypeCode
     */
    public TypeCode getTypeCode() {
    return parDesc.type;
    }

    public String getInstanceNodeTypeName ( ) {
        String result = super.getInstanceNodeTypeName();
        String suffix = null;
        switch (parDesc.mode.value()) {
            case ParameterMode._PARAM_IN:
                suffix = "in";
                break;
            case ParameterMode._PARAM_OUT:
                suffix = "out";
                break;
            case ParameterMode._PARAM_INOUT:
                suffix = "inout";
                break;
            default:
        }
        if (suffix!=null) {
            result = suffix + " " + result;
        }
        return result;
    }

    public static String nodeTypeName() {
        return "";
    }
}











