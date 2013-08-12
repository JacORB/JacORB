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
     * Only for passing the constructor call through to the superclass.
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
     * Set reference to corresponding IROBject (could be null, e.g. with StructMember).
     * Also fetches the name() of the IRObject, if it is a Contained object.
     * @param irobj
     */
    protected void setIRObject(org.omg.CORBA.IRObject irobj)
    {
    this.irObject = irobj;
    Contained contained;
        try
        {
            contained = ContainedHelper.narrow(irobj);
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
            IDLType idlType = IDLTypeHelper.narrow(irobj);
            typeCode = idlType.type();

            /*
             * Using the TypeCode we could get rid of a few remote method invocations
             * (a lot is already contained in the TypeCode); also perhaps the code for
             * reading the members of struct etc. could be simplified. BTW, all other
             * classes also have a TypeCode type() or result() operation!
             */
        }
        catch( org.omg.CORBA.BAD_PARAM bp )
        {
            // narrow failed
    }
    }
}
