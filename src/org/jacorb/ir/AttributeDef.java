/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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
 */

package org.jacorb.ir;

import org.jacorb.util.Debug;

import java.lang.reflect.*;
import java.util.*;

import org.omg.CORBA.TypeCode;

public class AttributeDef 
    extends Contained
    implements org.omg.CORBA.AttributeDefOperations
{
    private org.omg.CORBA.TypeCode typeCode = null;
    private org.omg.CORBA.IDLType type_def = null;
    private org.omg.CORBA.AttributeMode mode = null;
    private Method method = null;
    private boolean defined = false;

    public AttributeDef( java.lang.reflect.Method m, 
                         String attrTypeName,
                         org.omg.CORBA.AttributeMode mode,
                         org.omg.CORBA.Container _defined_in,
                         org.omg.CORBA.Repository _containing_repository )
    {
        def_kind = org.omg.CORBA.DefinitionKind.dk_Attribute;
        method = m;
        this.mode = mode;
        name( m.getName() );
        version( "1.0" );

        try
        {
            typeCode = 
                TypeCodeUtil.getTypeCode( m.getReturnType(), 
                                          RepositoryImpl.loader, 
                                          null,
                                          attrTypeName );            
        }
        catch( ClassNotFoundException cnfe )
        {
            org.jacorb.util.Debug.output( 0, "Error: TypeCode for AttributeDef  could not be created!");
            cnfe.printStackTrace();
        }

        defined_in = _defined_in;
        containing_repository = _containing_repository;

        Debug.myAssert( containing_repository != null, 
                      "containing_repository null!");
        Debug.myAssert( defined_in != null, "Defined?in null!");

        org.omg.CORBA.Contained myContainer = 
            org.omg.CORBA.ContainedHelper.narrow( defined_in );
        String interface_id = myContainer.id();

        id = interface_id.substring(interface_id.lastIndexOf(':')-1)  + 
            name() + ":" + version();
        absolute_name = myContainer.absolute_name() + "::" + name();

        org.jacorb.util.Debug.output(2, "New AttributeDef, name: " + name() +  
                                 " " + absolute_name);

    }

    public org.omg.CORBA.TypeCode type()
    {
        return typeCode;
    }

    public org.omg.CORBA.IDLType type_def()
    {
        return type_def;
    }

    public void type_def(org.omg.CORBA.IDLType a)
    {
        Debug.myAssert( defined == true, "Attribute not defined" );
        type_def = a;
    }

    public org.omg.CORBA.AttributeMode mode()
    {
        return mode;
    }

    public void mode(org.omg.CORBA.AttributeMode a)
    {
        mode = a;
    }

    org.omg.CORBA.AttributeDescription describe_attribute()
    {
        return new org.omg.CORBA.AttributeDescription(
                                        name(), 
                                        id(), 
                                        org.omg.CORBA.ContainedHelper.narrow(defined_in).id(), 
                                        version(), 
                                        type(), 
                                        mode());
    }

    public void define()
    {
        type_def = IDLType.create(typeCode, containing_repository  );
        defined = true;
    }

    // from Contained

    public org.omg.CORBA.ContainedPackage.Description describe()
    {
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.CORBA.AttributeDescriptionHelper.insert( a, describe_attribute() );
        return new org.omg.CORBA.ContainedPackage.Description( 
              org.omg.CORBA.DefinitionKind.dk_Attribute, a);
    }

    // from IRObject

    public void destroy()
    {}


}



