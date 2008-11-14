package org.jacorb.ir;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

import java.lang.reflect.Field;

public class EnumDef 
    extends TypedefDef
    implements org.omg.CORBA.EnumDefOperations
{
    /** enum member sequence */
    private String []                     members;
    /* reference to my container as a contained object */
    private org.omg.CORBA.Contained       myContainer;

    public EnumDef( Class c, 
                    org.omg.CORBA.Container _defined_in,
                    org.omg.CORBA.Repository ir,
                    ClassLoader loader)
    {
        def_kind = org.omg.CORBA.DefinitionKind.dk_Enum;
        defined_in = _defined_in;
        containing_repository = ir;
        version = "1.0";
        String classId = c.getName();
        myContainer = org.omg.CORBA.ContainedHelper.narrow( defined_in );

        if( classId.indexOf('.') > 0 ) 
        {
            name( classId.substring( classId.lastIndexOf('.')+1));
            String path = classId.substring( 0, classId.lastIndexOf('.'));

            if( path.endsWith("Package"))
            {
                id( RepositoryID.toRepositoryID( path.substring( 0, path.lastIndexOf("Package")) + "." + name, loader));
            }
            else 
            {
                id( RepositoryID.toRepositoryID( path + "." + name, loader));
            }

            absolute_name = myContainer.absolute_name() + "::" + name;
        } 
        else 
        {
            name( classId );
            defined_in = containing_repository;
            id( RepositoryID.toRepositoryID(name, loader));
            absolute_name = "::" + name;
        }	

        Field memberFields[] = c.getDeclaredFields(); 
        int member_size = (memberFields.length - 1 ) / 2;
        members = new String [member_size];
        // only every second field denotes an original enum member
        for( int i = 0; i < member_size; i++ )
        {
            members[ i ] = memberFields[2+(2*i)].getName(); 
        }
        type = org.omg.CORBA.ORB.init().create_enum_tc( id, name, members );
    }

    public String[] members()
    {
        return members;
    }

    public void members(String[] m)
    {
        members = m;
    }

    public void define()
    {
    }

    // from Contained

    public org.omg.CORBA.ContainedPackage.Description describe()
    {
        org.omg.CORBA.Any a =  orb.create_any();


        String def_in_name;
        if( myContainer != null )
            def_in_name = myContainer.id();
        else
            def_in_name = "IDL:/:1.0";

        org.omg.CORBA.TypeDescriptionHelper.insert( a, 
                       new org.omg.CORBA.TypeDescription(name(), 
                                                         id(),
                                                         def_in_name, 
                                                         version(),
                                                         type()
                                                         ) );
        return new org.omg.CORBA.ContainedPackage.Description( org.omg.CORBA.DefinitionKind.dk_Enum, a);
    }

    // from IRObject

    public void destroy(){}


}





