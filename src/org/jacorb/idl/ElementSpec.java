package org.jacorb.idl;

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

/**
 * @author Gerald Brose
 * @version $Id$
 */

public class ElementSpec
    extends IdlSymbol
{
    public TypeSpec typeSpec = new TypeSpec( new_num() );
    public Declarator declarator = null;
    private UnionType containingUnion;

    public ElementSpec( int num )
    {
        super( num );
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        if( pack_name.length() > 0 )
            pack_name = s + "." + pack_name;
        else
            pack_name = s;
        typeSpec.setPackage( s );
        declarator.setPackage( s );
    }

    public void setUnion( UnionType ut )
    {
        containingUnion = ut;

        // If its a constrType and is a pseudoscope add union name
        if (typeSpec.typeSpec () instanceof ConstrTypeSpec)
        {
           String tmpRef = ((ConstrTypeSpec)typeSpec.typeSpec ()).c_type_spec.pack_name;

           if (tmpRef.endsWith ("PackagePackage") || ! tmpRef.startsWith ("_") && tmpRef.endsWith ("Package"))
           {
              tmpRef = tmpRef.substring( 0, tmpRef.lastIndexOf( "Package" ) );
           }
           if (ScopedName.isPseudoScope (tmpRef))
           {
              ((ConstrTypeSpec)typeSpec.typeSpec ()).c_type_spec.pack_name =
                 ((ConstrTypeSpec)typeSpec.typeSpec ()).c_type_spec.pack_name + "." + ut.name + "Package";
           }
        }
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        typeSpec.setEnclosingSymbol( s );
        declarator.setEnclosingSymbol( s );
    }

    public void parse()
    {
        if( logger.isDebugEnabled() )
        {
            logger.debug("EelementSpec.parse(): element_spec is " +  typeSpec.typeSpec().getClass().getName());
        }

        if( typeSpec.typeSpec() instanceof TemplateTypeSpec ||
            typeSpec.typeSpec() instanceof ConstrTypeSpec )
        {
            typeSpec.parse();
            if( typeSpec.typeSpec() instanceof SequenceType )
            {
                TypeSpec ts = ( (SequenceType)typeSpec.typeSpec() ).elementTypeSpec().typeSpec();
                SequenceType seqTs = (SequenceType)typeSpec.typeSpec();
                while( ts instanceof SequenceType )
                {
                    seqTs = (SequenceType)ts;
                    ts = ( (SequenceType)ts.typeSpec() ).elementTypeSpec().typeSpec();
                }

                //                if( ts.typeName().equals( containingUnion.typeName() ) ||
                if( ScopedName.isRecursionScope( ts.typeName() ) )
                {
                    ( (SequenceType)seqTs.typeSpec() ).setRecursive();
                }
            }
        }
        else if( typeSpec.typeSpec() instanceof ScopedName )
        {
            TypeSpec ts = ( (ScopedName)typeSpec.typeSpec() ).resolvedTypeSpec();
            if( ts.typeName().equals( containingUnion.typeName() ) )
            {
                parser.error( "Illegal recursion in union " + containingUnion.full_name(), token );
            }

            containingUnion.addImportedName( ts.typeName() );

            // if( ts != null )

            // fix for bug#115: only set the element spec's type spec to the resolved
            // type if it is not an Interface! Otherwise the compile may loop!
            if( ! ( ts instanceof ConstrTypeSpec &&
                    ((ConstrTypeSpec)ts).declaration() instanceof Interface) )
            {
                typeSpec = ts;
            }
        }

        try
        {
            NameTable.define( containingUnion.full_name() + "." + declarator.name(), "declarator" );
        }
        catch( NameAlreadyDefined nad )
        {
            parser.error( "Declarator " + declarator.name() +
                    " already defined in union " + containingUnion.full_name(), token );
        }

        if( logger.isDebugEnabled() )
        {
            logger.debug("ElementSpec.parse-end(): element_spec is " +  typeSpec.typeSpec().getClass().getName());
        }
    }

    public void print( java.io.PrintWriter ps )
    {
        if( typeSpec.typeSpec() instanceof TemplateTypeSpec ||
                typeSpec.typeSpec() instanceof ConstrTypeSpec )
        {
            typeSpec.print( ps );
        }
    }
}
