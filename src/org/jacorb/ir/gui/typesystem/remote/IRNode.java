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


import org.omg.CORBA.Contained;
import org.omg.CORBA.ContainedHelper;
import org.omg.CORBA.IDLType;
import org.omg.CORBA.IDLTypeHelper;
import org.omg.CORBA.IRObject;
import org.omg.CORBA.TypeCode;

public abstract class IRNode
    extends org.jacorb.ir.gui.typesystem.TypeSystemNode
{
    protected org.omg.CORBA.IRObject irObject;
    private TypeCode typeCode;
    private String versionString = "";
    protected String repositoryID = "";

    /**
     * Dient nur dem Durchreichen des Konstruktor-Aufrufs an Oberklasse
     */

    protected IRNode ( ) {
    super();
    }

    /**
     * @param irObject org.omg.CORBA.IRObject
     */

    protected IRNode ( IRObject irObject)
    {
    super();
    setIRObject(irObject);
        //	System.out.println(this.toString()); System.out.flush();
    }

    /**
     * @return java.lang.String[]
     */

    public String[] allowedToAdd()
    {
    return null;
    }

    /**
     * @return java.lang.String
     */

    public  String description()
    {
    String result = super.description();
    result = result + "\nVersion:\t" + versionString + "\nRepository ID:\t" + repositoryID;
    return result;

    }

    /**
     * @return java.lang.String
     */

    public String getAbsoluteName()
    {
    if (absoluteName!=null && !absoluteName.equals(""))
        {
            return absoluteName;
    }
    else
        {
            return name;
    }
    }

    /**
     * @return org.omg.CORBA.TypeCode
     */
    public TypeCode getTypeCode()
    {
    return typeCode;
    }

    /**
     * Referenz auf dazugehöriges IRObject setzen.(kann null sein, z.B. bei StructMember)
     * Holt außerdem den name() des IRObject, wenn es ein Contained Objekt ist
     * @param irobj org.omg.CORBA.IRObject
     */

    protected void setIRObject(org.omg.CORBA.IRObject irobj)
    {
    this.irObject = irobj;
    Contained contained;
        try
        {
            contained = ContainedHelper.narrow((org.omg.CORBA.Object)irobj);
            setName(contained.name());
            setAbsoluteName(contained.absolute_name());
            versionString = contained.version();
            repositoryID = contained.id();
        }
        catch( org.omg.CORBA.BAD_PARAM bp )
        {
            // narrow failed
        }

        try
        {
            IDLType idlType = IDLTypeHelper.narrow((org.omg.CORBA.Object)irobj);
            typeCode = idlType.type();

            // mithilfe des TypeCodes könnten wir uns bei IDLTypes eigentlich ein
            // paar Remote Method Invocations sparen (es steckt einiges schon im TypeCode);
            // außerdem ließe sich vielleicht der Code zum Auslesen
            // der members bei struct etc. vereinfachen.
            // Alle anderen Klassen haben übrigens auch eine TypeCode
            // type() oder result() Operation!
        }
        catch( org.omg.CORBA.BAD_PARAM bp )
        {
            // narrow failed
    }
    }
}











