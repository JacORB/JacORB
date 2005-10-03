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
 * @author Andre Spiegel <spiegel@gnu.org>
 * @version $Id$
 */

import java.io.PrintWriter;

public class ExceptionHolderTypeSpec
    extends TypeSpec
{

    public ExceptionHolderTypeSpec( int num )
    {
        super( num );
    }

    public Object clone()
    {
        return this;
    }

    public String typeName()
    {
        return "org.omg.Messaging.ExceptionHolder";
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

    public String id()
    {
        return "IDL:omg.org/Messaging/ExceptionHolder:1.0";
    }

    public String toString()
    {
        return "org.omg.Messaging.ExceptionHolder";
    }

    /**
     * @return a string for an expression of type TypeCode that describes this type
     */
    public String getTypeCodeExpression()
    {
        return "org.omg.CORBA.ORB.init().create_value_tc"
          + "(\"IDL:omg.org/Messaging/ExceptionHolder:1.0\","
          + "\"ExceptionHolder\", (short)0, null,"
          + "new org.omg.CORBA.ValueMember[] {"
          + "new org.omg.CORBA.ValueMember (\"\", \"IDL:*primitive*:1.0\","
          + "\"ExceptionHolder\", \"1.0\", "
          + "org.omg.CORBA.ORB.init().get_primitive_tc("
          + "org.omg.CORBA.TCKind.from_int(8)), null, (short)0),"
          + "new org.omg.CORBA.ValueMember (\"\", \"IDL:*primitive*:1.0\","
          + "\"ExceptionHolder\", \"1.0\", "
          + "org.omg.CORBA.ORB.init().get_primitive_tc("
          + "org.omg.CORBA.TCKind.from_int(8)), null, (short)0),"
          + "new org.omg.CORBA.ValueMember (\"\", \"IDL:marshaled_exception:1.0\","
          + "\"ExceptionHolder\", \"1.0\", "
          + "org.omg.CORBA.ORB.init().create_sequence_tc("
          + "0, org.omg.CORBA.ORB.init().get_primitive_tc("
          + "org.omg.CORBA.TCKind.from_int(10)) ), null, (short)0)});";
    }

    public void print( PrintWriter ps )
    {
    }

    public String holderName()
    {
        return typeName() + "Holder";
    }

    public String printWriteStatement( String var_name, String streamname )
    {
        return "((org.omg.CORBA_2_3.portable.OutputStream)" + streamname + ")"
                + ".write_value (" + var_name + " );";
    }

    public String printReadExpression( String streamname )
    {
        return "(" + typeName() + ")"
                + "((org.omg.CORBA_2_3.portable.InputStream)" + streamname + ")"
                + ".read_value (\"" + id() + "\")";
    }

    public void printInsertIntoAny(PrintWriter ps,
                                   String anyname,
                                   String varname)
    {
        throw new RuntimeException( "Should not be called" );
    }


    public void printExtractResult(PrintWriter ps,
                                   String resultname,
                                   String anyname,
                                   String resulttype)
    {
        throw new RuntimeException( "Should not be called" );
    }

}
