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

package org.jacorb.idl;

/**
 * @author Gerald Brose
 * @version $Id$
 */

class ValueBase
    extends BaseType
{
    public ValueBase( int num )
    {
        super( num );
    }

    public Object clone()
    {
        return this;
    }

    public String typeName()
    {
        return "java.io.Serializable";
    }

    public TypeSpec typeSpec()
    {
        return this;
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
    }

    public boolean basic()
    {
        return true;
    }

    public void set_constr( TypeDeclaration td )
    {
    }

    public void parse()
    {
    }

    public String toString()
    {
        return typeName();
    }

    /**
     * @return a string for an expression of type TypeCode that describes this type
     */
    public String getTypeCodeExpression()
    {
        return "org.omg.CORBA.ORB.init().create_value_tc(\"" + id() + 
            "\",\"ValueBase\", org.omg.CORBA.VM_NONE.value, org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_null), new org.omg.CORBA.ValueMember[]{} )";
    }

    public String id()
    {
        return "IDL:omg.org/CORBA/ValueBase:1.0";
    }

    public void print( java.io.PrintWriter ps )
    {
    }

    public String holderName()
    {
        return typeName() + "Holder";
    }

    public String printReadExpression( String streamname )
    {
        return "((org.omg.CORBA_2_3.portable.InputStream)" + streamname + ").read_value()";
    }

    public String printWriteStatement( String var_name, String streamname )
    {
        return "((org.omg.CORBA_2_3.portable.OutputStream)" + streamname + 
            ").write_value(" + var_name + ");";
    }


}



