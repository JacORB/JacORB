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

package org.jacorb.idl.javamapping;

import org.jacorb.idl.*;

import java.util.Enumeration;

/*
 * The compiler backend for the IDL/Java mapping
 *
 * @author Copyright (c) 1999-2004, Gerald Brose
 * @version $Id$
 *
 */

public class JavaMappingGeneratingVisitor
    implements IDLTreeVisitor
{
    /**
     * used by the IDL compiler
     */

    public JavaMappingGeneratingVisitor()
    {
    }

    /**
     * entry point for the code generation, called from
     * the parser.
     *
     */

    public void visitSpec( Spec spec )
    {

        Enumeration e = spec.definitions.elements();
        while( e.hasMoreElements() )
        {
            IdlSymbol s = (IdlSymbol)e.nextElement();
            s.accept( this );
        }

        // PrintStream ps = new PrintStream( System.out );
    }

    public void visitDefinitions( Definitions defs )
    {
        Enumeration e = defs.getElements();
        while( e.hasMoreElements() )
        {
            IdlSymbol s = (IdlSymbol)e.nextElement();
            s.accept( this );
        }
    }

    public void visitDefinition( Definition def  )
    {
        def.get_declaration().accept( this );
    }

    public void visitDeclaration( Declaration declaration )
    {
        // should not be needed, but to make sure we see something
        // if we ever get here...
        System.out.println("Unimplemented behavior: visitDeclaration");
    }

    public void visitModule( Module module )
    {
        module.getDefinitions().accept( this );
    }

    public void visitInterface( Interface interfce )
    {
        // forward declared interface, code will be generated
        // when the defininition is encountered later
        if( interfce.body == null )
            return;

        // list super interfaces
        String[] superInts = interfce.get_ids();

        for( int i = 1; i < superInts.length; i++ )
        {
            // skip index 0, which contains the current interface id

        }

        if( interfce.body != null )
            interfce.body.accept( this );

    }

    public void visitInterfaceBody( InterfaceBody body )
    {
         Operation[] ops = body.getMethods();


         for( int i = 0; i < ops.length; i++ )
         {
             ops[ i ].accept( this );
         }


    }

    public void visitMethod( Method m )
    {

        if( m.isGetter() )
        {
            ;
        }
        else
        {
            ;
        }
    }

    public void visitOpDecl( OpDecl op )
    {
        //    op.opAttribute == OpDecl.ONEWAY ? "true" : "false") );


        // descend....
        for( Enumeration e = op.paramDecls.elements(); e.hasMoreElements(); )
        {
            ParamDecl param = (ParamDecl)e.nextElement();
            param.accept( this );
        }

    }

    public void visitParamDecl( ParamDecl param )
    {

        String direction = "in";

        if( param.paramAttribute == ParamDecl.MODE_OUT )
            direction = "out";
        else if( param.paramAttribute == ParamDecl.MODE_INOUT )
            direction = "inout";


    }


    public void visitTypeDeclaration( TypeDeclaration typeDecl )
    {
    }

    public void visitConstrTypeSpec( ConstrTypeSpec typeDecl )
    {
        //
    }

    public void visitStruct( StructType struct )
    {
        int length = -1;
        boolean isSeq = false;
        boolean isArray = false;

        MemberList members = struct.memberlist;
        if( members != null )
        {

            for( Enumeration e = members.elements(); e.hasMoreElements(); )
            {
                Member m = (Member)e.nextElement();
                String memberType = typeSpecDesignator( m.type_spec );


                if( m.type_spec instanceof VectorType )
                {
                    memberType =
                        typeSpecDesignator( ((VectorType)m.type_spec).elementTypeSpec() );
                    length = ((VectorType)m.type_spec).length();

                    if(  m.type_spec instanceof SequenceType )
                    {
                        isSeq = true;
                    }
                    else if( m.type_spec  instanceof ArrayTypeSpec )
                    {
                        isArray = true;
                    }
                    else
                    {
                        throw new RuntimeException("Internal Error: encountered vector that is neither array nor sequence!");
                    }
                }
            }
        }
    }

    /**
     *
     */

    public void visitEnum( EnumType enumType )
    {

        for( Enumeration e = enumType.enumlist.elements(); e.hasMoreElements(); )
        {
            //
        }

    }

    public void visitUnion( UnionType union )
    {

    }

    public void visitSequence( SequenceType seq )
    {
        // nothing here, all work done in visitAlias()
    }

    public void visitNative( NativeType _native )
    {

    }

    public void visitTypeDef( TypeDef typedef )
    {
        // nothing here, all work done in visitAlias()
    }


    public void visitAlias( AliasTypeSpec alias )
    {
        boolean isSeq = false;
        boolean isArray = false;
        int length = -1;
        String aliasedType = typeSpecDesignator( alias.originalType() );



        if(  alias.originalType() instanceof VectorType )
        {
            aliasedType =
                typeSpecDesignator( ((VectorType)alias.originalType()).elementTypeSpec());
            length = ((VectorType)alias.originalType()).length();

            if( alias.originalType() instanceof SequenceType )
            {
                isSeq = true;
            }
            else if( alias.originalType() instanceof ArrayTypeSpec )
            {
                isArray = true;
            }
            else
            {
                throw new RuntimeException("Internal Error: encountered vector that is neither array nor sequence!");
            }
        }

    }

    public void visitValue( Value value )
    {

    }

    /**
     * Type ids
     * @return a string describing a type
     */

    private String typeSpecDesignator( TypeSpec ts )
    {
        if( ! ts.basic() )
        {
            if( ts.typeSpec() instanceof AnyType )
            {
                return "any";
            }
            else
            {
                return ts.id();
            }
        }
        else
        {
            if( ts.typeSpec() instanceof ObjectTypeSpec )
            {
                return ts.id();
            }
            else if( ts.typeSpec() instanceof ConstrTypeSpec )
            {
                return  ((ConstrTypeSpec)ts.typeSpec()).id();
            }
            else if( ts.typeSpec() instanceof ScopedName )
            {
                return typeSpecDesignator ( ((ScopedName)ts.typeSpec()).resolvedTypeSpec() );
            }
            else if( ts.typeSpec() instanceof StringType )
            {
                return "string";
            }
            else
            {
                // debug:
                // System.out.println("typeSpecDesignator for " +
                // ts.typeSpec().getClass().getName());
                return ts.toString();
            }
        }
    }
}
