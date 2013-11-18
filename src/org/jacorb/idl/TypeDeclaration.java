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
 * @author Gerald Brose
 */

import java.io.PrintWriter;
import java.util.Set;
import java.util.logging.Level;

public class TypeDeclaration
    extends Declaration
{
    public TypeDeclaration type_decl;

    public TypeDeclaration( int num )
    {
        super( num );
        pack_name = "";
    }

    public Object clone()
    {
        return type_decl.clone();
    }


    public TypeDeclaration declaration()
    {
        return type_decl;
    }

    public String typeName()
    {
        return type_decl.typeName();
    }

    /**
     *  we have to be able to distinguish between explicitly typedef'd
     *  type names and anonymously defined type names
     */

    public void markTypeDefd( String alias )
    {
        type_decl.markTypeDefd( alias );
    }

    public String getRecursiveTypeCodeExpression()
    {
        if( type_decl == null )
        {
            return "org.omg.CORBA.ORB.init().create_recursive_tc "
            + "(\"" + id() + "\")";
        }
        return type_decl.getRecursiveTypeCodeExpression();
    }

    /**
     * Returns a type code expression (for use in generated code) for
     * this type.  If `knownTypes' contains this type,
     * then a recursive type code is returned.
     */

    public String getTypeCodeExpression( Set knownTypes )
    {
        if( type_decl instanceof Value )
        {
            return type_decl.getTypeCodeExpression( knownTypes );
        }
        return type_decl.getTypeCodeExpression();
    }

    /**
     * @return a string for an expression of type TypeCode
     *     that describes this type
     */
    public String getTypeCodeExpression()
    {
        return type_decl.getTypeCodeExpression();
    }

    public boolean basic()
    {
        return type_decl.basic();
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        type_decl.setPackage( s );
    }

    public void set_included( boolean i )
    {
        included = i;
        type_decl.set_included( i );
    }

    public void parse()
    {
        type_decl.parse();
    }

    public String holderName()
    {
        return type_decl.holderName();
    }

    public String helperName() throws NoHelperException {
        if (type_decl == null)
        {
            throw new NoHelperException();
        }

        return type_decl.helperName();
    }

    public void print( PrintWriter ps )
    {
        type_decl.print( ps );
    }

    public String toString()
    {
        return type_decl.toString();
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
        {
            parser.logger.log(Level.SEVERE, "was " + enclosing_symbol.getClass().getName() + " now: " + s.getClass().getName());
            throw new RuntimeException( "Compiler Error: trying to reassign container for " + name );
        }
        enclosing_symbol = s;
        type_decl.setEnclosingSymbol( s );
    }

    public String printReadExpression( String streamname )
    {
        return type_decl.printReadExpression( streamname );
    }

    public String printReadStatement( String var_name, String streamname )
    {
        return var_name + "=" + printReadExpression( streamname ) + ";";
    }

    public String printWriteStatement( String var_name, String streamname )
    {
        return type_decl.printWriteStatement( var_name, streamname );
    }

    public void accept( IDLTreeVisitor visitor )
    {
        type_decl.accept( visitor );
    }

    public void printInsertIntoAny(PrintWriter ps,
                                   String anyname,
                                   String varname)
    {
        type_decl.printInsertIntoAny(ps, anyname, varname);
    }

    public void printExtractResult(PrintWriter ps,
                                   String resultname,
                                   String anyname,
                                   String resulttype)
    {
        type_decl.printExtractResult(ps, resultname, anyname, resulttype);
    }

    public void printSerialVersionUID(PrintWriter ps)
    {
        ps.println("\t/** Serial version UID. */");
        ps.println("\tprivate static final long serialVersionUID = 1L;");
    }
}
