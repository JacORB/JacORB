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
 * Common super class for arrays and sequences
 *
 *
 * @author Gerald Brose
 * @version $Id$
 */


public abstract class VectorType
    extends TemplateTypeSpec
{
    TypeSpec type_spec;

    public VectorType( int num )
    {
        super( num );
    }

    /**
     * @return the TypeSpec for the sequence's element type
     */

    public TypeSpec elementTypeSpec()
    {
        TypeSpec t = type_spec.typeSpec();

        /* if the element type is scoped name that refers to another
           type spec, we have to retrieve that. If that type spec is
           a base type or a string, we have to return it, otherwise
           we return the scoped type name.
        */

        if( t instanceof ScopedName )
        {
            t = ( (ScopedName)t ).resolvedTypeSpec().typeSpec();
        }
        return t;
    }


    public void setTypeSpec( SimpleTypeSpec sts )
    {
        type_spec = sts;
    }

    /**
     * @return this sequences Java type name, i.e., the element type with
     * "[]" appended.
     */

    public String typeName()
    {
        String name;

        if( type_spec.typeSpec() instanceof ScopedName )
        {
            name =
                    ( (ScopedName)type_spec.typeSpec() ).resolvedTypeSpec().toString();
        }
        else
        {
            name = type_spec.toString();
        }

        return name + "[]";
    }

    boolean typedefd()
    {
        return typedefd;
    }

    public String printReadExpression( String streamname )
    {
        if( typedefd() )
            return helperName() + ".read(" + streamname + ")";
       else
            return "*****";
    }


    protected String elementTypeExpression()
    {
        TypeSpec ts = type_spec.typeSpec();

        if( ts instanceof AliasTypeSpec )
        {
            return type_spec.full_name() + "Helper.type()";
        }
        else if( ts instanceof BaseType ||
                ts instanceof TypeCodeTypeSpec ||
                ts instanceof ConstrTypeSpec || // for value types
                ts instanceof TemplateTypeSpec )
        {
            return ts.getTypeCodeExpression();
        }
        else
        {
            return ts.typeName() + "Helper.type()";
        }
    }

    public String elementTypeName()
    {
        TypeSpec ts = type_spec;
        if( ts instanceof ScopedName )
        {
            if( logger.isFatalErrorEnabled() )
                logger.fatalError( "elementTypeName is outer ScopedName" );
            ts = ( (ScopedName)type_spec.type_spec ).resolvedTypeSpec();

            while( ts instanceof ScopedName || ts instanceof AliasTypeSpec )
            {
                if( ts instanceof ScopedName )
                {
                    if( logger.isFatalErrorEnabled() )
                        logger.fatalError( "elementTypeName is inner Alias" );
                    ts = ( (ScopedName)ts ).resolvedTypeSpec();
                }
                if( ts instanceof AliasTypeSpec )
                {
                    if( logger.isFatalErrorEnabled() )
                        logger.fatalError( "elementTypeName is inner Alias" );
                    ts = ( (AliasTypeSpec)ts ).originalType();
                }
            }
        }
        return ts.typeName();
    }

    /**
     * @return the length of this array or sequence.  For
     * multi-dimensional vectors, this is the outermost dimension.
     * For open sequences, this length is 0.
     */

    public abstract int length();

    public abstract String holderName();

    public abstract String helperName();


    public String toString()
    {
        return typeName();
    }


}
