/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

/**
 * @author Gerald Brose
 */
public class Member
    extends Declaration
{
    public TypeSpec type_spec;

    /**
        this list initially set by the parser but later flattened so that
        each member only has a single declarator. The list will be null after
        calling parse()!
     */
    SymbolList declarators;

    public Vector extendVector;
    public TypeDeclaration containingType;

    public Declarator declarator;

    public Member( int num )
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

        type_spec.setPackage( s );
        if ( declarators != null )
            declarators.setPackage( s );
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        enclosing_symbol = s;
        type_spec.setEnclosingSymbol( s );
        for( Enumeration e = declarators.v.elements(); e.hasMoreElements(); )
            ( (Declarator)e.nextElement() ).setEnclosingSymbol( s );
    }


    public void setContainingType( TypeDeclaration t )
    {
        containingType = t;
    }

    /**
     * must be set by MemberList before parsing
     */
    public void setExtendVector( Vector v )
    {
        extendVector = v;
    }

    /**
     * Creates a new Member that is similar to this one,
     * but only for declarator d.
     */
    public Member extractMember( Declarator d )
    {
        Member result = new Member( new_num() );
        result.declarator = d;
        return result;
    }

    /**
     * Parsing members means creating new members for definitions
     * with more than one declarator.
     */
    public void parse()
    {
        boolean clone_and_parse = true;

        if( extendVector == null )
        {
            lexer.restorePosition(myPosition);
            parser.fatal_error ("Internal Compiler Error: extendVector not set.", token);
        }

        if( type_spec.typeSpec() instanceof ScopedName )
        {
            token = type_spec.typeSpec().get_token();
            String name = type_spec.typeSpec().toString();

            type_spec = ( (ScopedName)type_spec.typeSpec() ).resolvedTypeSpec();
            enclosing_symbol.addImportedName( name, type_spec );

            if( type_spec instanceof AliasTypeSpec )
            {
                AliasTypeSpec aliasTS = (AliasTypeSpec)type_spec;
                TypeSpec originalType = aliasTS.originalType ();
                if( originalType instanceof SequenceType )
                {
                    SequenceType sequenceTS = (SequenceType)originalType;
                    if( sequenceTS.elementTypeSpec().typeName().equals( containingType.typeName()) )
                    {
                        sequenceTS.setRecursive ();
                    }
                }
            }

            clone_and_parse = false;
            if( type_spec instanceof ConstrTypeSpec )
            {
                if( ( (ConstrTypeSpec)type_spec.typeSpec() ).c_type_spec instanceof StructType )
                {
                    if(
                       ( (ConstrTypeSpec)type_spec.typeSpec() ).c_type_spec.typeName().equals( containingType.typeName() )
                       )
                    {
                        parser.fatal_error( "Illegal type recursion (use sequence<" +
                                            containingType.typeName() + "> instead)", token );
                    }
                }
            }
        }
        else if( type_spec.typeSpec() instanceof SequenceType )
        {
            TypeSpec ts = ( (SequenceType)type_spec.typeSpec() ).elementTypeSpec().typeSpec();
            SequenceType seqTs = (SequenceType)type_spec.typeSpec();
            while( ts instanceof SequenceType )
            {
                seqTs = (SequenceType)ts;
                ts = ( (SequenceType)ts.typeSpec() ).elementTypeSpec().typeSpec();
            }

            if( ScopedName.isRecursionScope( ts.typeName() ) )
            {
                seqTs.setRecursive();
            }
        }
        else if( type_spec instanceof ConstrTypeSpec )
        {
            type_spec.parse();
        }

        String tokName = null;
        if( token != null && token.line_val != null )
        {
            tokName = token.line_val.trim();
            if( tokName.length() == 0 )
            {
                tokName = null;
            }
        }

        for( Enumeration e = declarators.v.elements(); e.hasMoreElements(); )
        {
            Declarator declarator = (Declarator)e.nextElement();
            String declaratorName = declarator.name();

            if( tokName != null )
            {
                if (org.jacorb.idl.parser.strict_names)
                {
                    // check for name clashes strictly (i.e. case insensitive)
                    if( declaratorName.equalsIgnoreCase( tokName ) )
                    {
                        parser.fatal_error( "Declarator " + declaratorName +
                                            " already defined in scope.", token );
                    }
                }
                else
                {
                    // check for name clashes only loosely (i.e. case sensitive)
                    if( declaratorName.equals( tokName ) )
                    {
                        parser.fatal_error( "Declarator " + declaratorName +
                                            " already defined in scope.", token );
                    }
                }
            }

            // we don't parse the declarator itself
            // as that would result in its name getting defined
            // we define the declarator's name as a type name indirectly
            // through the cloned type specs.

            Member m = extractMember( declarator );

            TypeSpec ts = type_spec.typeSpec();

            /* create a separate type spec copy for every declarator
               if the type spec is a new type definition, i.e. a
               struct, enum, union, sequence or the declarator is
               an array declarator
            */

            if( clone_and_parse || declarator.d instanceof ArrayDeclarator )
            {
                /* arrays need special treatment */

                if( declarator.d instanceof ArrayDeclarator )
                {
                    ts = new ArrayTypeSpec( new_num(), ts, (ArrayDeclarator)declarator.d, pack_name );
                    ts.parse();
                }
                else if( !( ts instanceof BaseType ) )
                {
                    ts = (TypeSpec)ts.clone();
                    if( !( ts instanceof ConstrTypeSpec ) )
                    {
                        ts.set_name( declarator.name() );
                    }

                    /* important: only parse type specs once (we do it for the last
                       declarator only) */
                    if( !e.hasMoreElements() )
                    {
                        ts.parse();
                    }
                }
            }

            //            else
            if( !( declarator.d instanceof ArrayDeclarator ) )
            {
                try
                {
                    NameTable.define( containingType + "." + declarator.name(),
                                      IDLTypes.DECLARATOR );
                }
                catch( NameAlreadyDefined nad )
                {
                    parser.fatal_error( "Declarator " + declarator.name() +
                            " already defined in scope.", token );
                }
            }

            /* if the type spec is a scoped name, it is already parsed and
             * the type name is defined
             */
            m.type_spec = ts;
            m.pack_name = this.pack_name;
            m.name = declaratorName;
            extendVector.addElement( m );
        }
        declarators = null;
    }

    /**
     *
     */

    public void print( PrintWriter ps )
    {
        member_print( ps, "\tpublic " );
    }

    /**
     *
     */

    public void member_print( PrintWriter ps, String prefix )
    {
        /* only print members that are not interfaces */

        if( ( type_spec.typeSpec() instanceof ConstrTypeSpec &&
                !( ( ( (ConstrTypeSpec)type_spec.typeSpec() ).c_type_spec.declaration()
                instanceof Interface ) ||
                ( ( (ConstrTypeSpec)type_spec.typeSpec() ).c_type_spec.declaration()
                instanceof Value ) ) ) ||
                type_spec.typeSpec() instanceof SequenceType ||
                type_spec.typeSpec() instanceof ArrayTypeSpec )
        {
            type_spec.print( ps );
        }

        if( type_spec.typeSpec().toString().equals("java.lang.String"))
        {
            ps.print( prefix + type_spec.toString() + " " + declarator.toString() + " = \"\";" );
        }
        else
        {
            ps.print( prefix + type_spec.toString() + " " + declarator.toString() + ";" );
        }
    }

    public TypeSpec typeSpec()
    {
        return type_spec.typeSpec();
    }
}
