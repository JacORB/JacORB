package org.jacorb.ir;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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

public class ArrayDef
    extends IDLType
    implements org.omg.CORBA.ArrayDefOperations
{
    int size = -1;
    org.omg.CORBA.TypeCode element_type;
    org.omg.CORBA.IDLType element_type_def;
    private org.omg.CORBA.Repository ir;
 
    public ArrayDef( org.omg.CORBA.TypeCode tc, org.omg.CORBA.Repository ir )
    {
         org.jacorb.util.Debug.myAssert( tc.kind() == org.omg.CORBA.TCKind.tk_array, 
                                  "Precondition volation: TypeCode must be of kind arry");
         def_kind = org.omg.CORBA.DefinitionKind.dk_Array;
         this.ir = ir;
         type = tc;
         try
         {
             size = tc.length();
             element_type = tc.content_type();
             element_type_def = IDLType.create(  element_type, ir );
         }
         catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
         {
             // cannot happen because of myAssertion
         }
         
         org.jacorb.util.Debug.myAssert( element_type_def != null, "Element type null in sequence def");
         org.jacorb.util.Debug.output(2, "New ArrayDef");
         
    }
    
    public int length()
    {
        return size;
    }

    public void length(int a)
    {
        size = a;
    }

    public org.omg.CORBA.TypeCode element_type()
    {
        return element_type;
	}
    
    public org.omg.CORBA.IDLType element_type_def()
    {
        return element_type_def;
    }
    
    public void element_type_def(org.omg.CORBA.IDLType a)
    {
        element_type_def = a;
    }
    
    public void destroy()
    {
        type = null;
        element_type = null;
        element_type_def = null;
    }
    public void define()
    {

    }
}










