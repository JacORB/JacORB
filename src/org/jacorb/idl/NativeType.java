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

import java.io.PrintWriter;

public class NativeType
    extends TypeDeclaration
{
    SimpleDeclarator declarator;

    public NativeType( int num )
    {
        super( num );
        pack_name = "";
    }

    public Object clone()
    {
        NativeType nt = new NativeType( new_num() );
        nt.declarator = this.declarator;
        nt.pack_name = this.pack_name;
        return nt;
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
            throw new RuntimeException( "Compiler Error: trying to reassign container for " + name );
        enclosing_symbol = s;
    }

    public TypeDeclaration declaration()
    {
        return this;
    }

    public String typeName()
    {
        if( pack_name.length() > 0 )
            return ScopedName.unPseudoName( pack_name + "." + name );
        else
            return ScopedName.unPseudoName( name );
    }


    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        if( pack_name.length() > 0 )
            pack_name = s + "." + pack_name;
        else
            pack_name = s;
    }

    public boolean basic()
    {
        return true;
    }

    public String toString()
    {
        return typeName();
    }

    public void set_included( boolean i )
    {
        included = i;
    }

    public void parse()

    {
        // don't parse the declarator as that would define its
        // name which is to be defined as part of this type name

        name = declarator.name();
        is_pseudo = true;

        ConstrTypeSpec ctspec = new ConstrTypeSpec( new_num() );
        try
        {
            ctspec.c_type_spec = this;
            NameTable.define( full_name(), "native" );
            TypeMap.typedef( full_name(), ctspec );
        }
        catch( NameAlreadyDefined n )
        {
            parser.fatal_error( "Name already defined", token );
        }
    }

    public String holderName()
    {
        return typeName() + "Holder";
    }


    public String printReadExpression( String Streamname )
    {
        return full_name() + "Helper.read(" + Streamname + ")";
    }

    public String printWriteStatement( String var_name, String Streamname )
    {
        return full_name() + "Helper.write(" + Streamname + "," + var_name + ");";
    }

    public void print( PrintWriter ps )
    {
    }

    /**
     */ 

    public void accept( IDLTreeVisitor visitor )
    {
        visitor.visitNative( this );
    }


}





















