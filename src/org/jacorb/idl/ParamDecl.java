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

public class ParamDecl
    extends IdlSymbol
{
    public static final int MODE_IN    = 1;
    public static final int MODE_OUT   = 2;
    public static final int MODE_INOUT = 3;

    public int paramAttribute;
    public TypeSpec paramTypeSpec;
    public SimpleDeclarator simple_declarator;

    public ParamDecl( int num )
    {
        super( num );
    }

    /**
     *  Constructs a new parameter declaration with the given characteristics.
     */
    public ParamDecl( int paramAttribute,
                      TypeSpec paramTypeSpec,
                      SimpleDeclarator simple_declarator )
    {
        super( new_num());
        this.paramAttribute = paramAttribute;
        this.paramTypeSpec  = paramTypeSpec;
        this.simple_declarator = simple_declarator;
    }

    /**
     *  Constructs a new parameter declaration with the given characteristics.
     */
    public ParamDecl( int paramAttribute,
                      TypeSpec paramTypeSpec,
                      String name)
    {
        super( new_num() );
        this.paramAttribute = paramAttribute;
        this.paramTypeSpec  = paramTypeSpec;
        this.simple_declarator = new SimpleDeclarator( new_num() );
        this.simple_declarator.name = name;
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        if( pack_name.length() > 0 )
            pack_name = new String( s + "." + pack_name );
        else
            pack_name = s;
        paramTypeSpec.setPackage( s );
    }

    /**
     * Returns a new ParamDecl with the same characteristics as this one,
     * except that its mode is changed to 'in'.
     */
    public ParamDecl asIn()
    {
        return new ParamDecl( MODE_IN, 
                              this.paramTypeSpec, 
                              this.simple_declarator) ;
    }

    public void parse()
    {
        while( paramTypeSpec.typeSpec() instanceof ScopedName )
        {
            TypeSpec ts = ( (ScopedName)paramTypeSpec.typeSpec() ).resolvedTypeSpec();
            if( ts != null )
                paramTypeSpec = ts;
        }

        if( paramTypeSpec == null )
        {
            throw new ParseException("parameter TypeSpec is null " + name, this.myPosition );
        }

        //simple_declarator.print(ps);
    }

    public void print( PrintWriter ps )
    {
        switch( paramAttribute )
        {
            case MODE_IN:
                //    if( paramTypeSpec instanceof ConstrTypeSpec )
                //ps.print( paramTypeSpec.typeName() );
                //else
                ps.print( paramTypeSpec.toString() );
                break;
            case MODE_OUT:
            case MODE_INOUT:
                ps.print( paramTypeSpec.holderName() );
                break;
        }
        ps.print( " " + simple_declarator );
        //simple_declarator.print(ps);
    }

    public String printWriteStatement( String ps )
    {
        return printWriteStatement( simple_declarator.toString(), ps );
    }

    public String printWriteStatement( String name, String ps )
    {
        if( paramAttribute != ParamDecl.MODE_IN )
            return paramTypeSpec.typeSpec().printWriteStatement( name + ".value", ps );
        else
            return paramTypeSpec.typeSpec().printWriteStatement( name, ps );
    }

    public String printReadExpression( String ps )
    {
        return paramTypeSpec.typeSpec().printReadExpression( ps );
    }

    public void accept( IDLTreeVisitor visitor )
    {
        visitor.visitParamDecl( this );
    }


}


