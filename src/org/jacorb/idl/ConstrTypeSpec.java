
/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

import java.io.PrintWriter;
import java.util.*;

public class ConstrTypeSpec
    extends TypeSpec
{
    public TypeDeclaration c_type_spec;

    public ConstrTypeSpec( int num )
    {
        super( num );
    }

    public ConstrTypeSpec( TypeDeclaration c )
    {
        super( new_num() );
        c_type_spec = c;
    }

    public void set_name( String n )
    {
        c_type_spec.set_name( n );
    }

    public Object clone()
    {
        ConstrTypeSpec cts = new ConstrTypeSpec( new_num() );
        cts.c_type_spec = (TypeDeclaration)c_type_spec.clone();
        return cts;
    }

    public TypeDeclaration declaration()
    {
        return c_type_spec;
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
            throw new RuntimeException( "Compiler Error: trying to reassign container for " + name );
        enclosing_symbol = s;
        c_type_spec.setEnclosingSymbol( s );
    }

    public String toString()
    {
        String n = typeName();
        if( !n.startsWith( "org.omg" ) )
        {
            return omgPrefix() + n;
        }
        else
            return n;
    }

    public String typeName()
    {
        return c_type_spec.typeName();
    }

    public String full_name()
    {
        return c_type_spec.full_name();
    }

    /**
     * @returns "org.omg." if the symbol has been declare inside a
     * scope with a pragma prefix of "omg.org"
     */

    public String omgPrefix()
    {
        return c_type_spec.omg_package_prefix;
    }

    public TypeSpec typeSpec()
    {
        return this;
    }

    public boolean basic()
    {
        return c_type_spec.basic();
    }

    public void parse()
    {
        c_type_spec.parse();
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        c_type_spec.setPackage( s );
    }


    public String getTypeCodeExpression( Set knownTypeSpecs )
    {
        return c_type_spec.getTypeCodeExpression( knownTypeSpecs );
    }

    /**
     * @returns a string for an expression of type TypeCode
     * 			that describes this type
     */
    public String getTypeCodeExpression()
    {
        return c_type_spec.getTypeCodeExpression();
    }


    public void print( PrintWriter ps )
    {
        c_type_spec.print( ps );
    }

    public String holderName()
    {
        return c_type_spec.holderName();
    }

    public String printReadExpression( String streamname )
    {
        return c_type_spec.printReadExpression( streamname );
    }

    public String printWriteStatement( String var_name, String streamname )
    {
        return c_type_spec.printWriteStatement( var_name, streamname );
    }

    public String printInsertExpression()
    {
        throw new RuntimeException( "Should not be called" );
    }

    public String printExtractExpression()
    {
        throw new RuntimeException( "Should not be called" );
    }

    public String id()
    {
        return c_type_spec.declaration().id();
    }

    /**
     * @overrides accept in IdlSymbol
     */ 

    public void accept( IDLTreeVisitor visitor )
    {
        c_type_spec.declaration().accept( visitor );
    }


}

