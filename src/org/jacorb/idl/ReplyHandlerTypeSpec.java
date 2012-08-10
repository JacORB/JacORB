/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

public class ReplyHandlerTypeSpec
    extends TypeSpec
{

    public ReplyHandlerTypeSpec( int num )
    {
        super( num );
    }

    public Object clone()
    {
        return this;
    }

    public String typeName()
    {
        return "org.omg.Messaging.ReplyHandler";
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
        return "org.omg.Messaging.ReplyHandler";
    }

    /**
     * @return a string for an expression of type TypeCode that describes this type
     */
    public String getTypeCodeExpression()
    {
        return "org.omg.CORBA.ORB.init().create_interface_tc(\"IDL:omg.org/Messaging/ReplyHandler:1.0\",\"ReplyHandler\")";
    }

    public void print( PrintWriter ps )
    {
    }

    public String holderName()
    {
        return typeName() + "Holder";
    }

    public String printReadExpression( String streamname )
    {
        // this is not currently used and certainly not correct
        return streamname + ".read_Object()";
    }

    public String printWriteStatement( String var_name, String streamname )
    {
        // this is not currently used and certainly not correct
        return streamname + ".write_Object(" + var_name + ");";
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
