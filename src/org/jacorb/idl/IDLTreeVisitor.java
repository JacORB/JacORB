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


import java.io.PrintWriter;
import java.util.*;

import org.apache.log.*;


/**
 * Generic API used by the IDL compiler to call backend code
 * generators. Extend this interface for your own code
 * generator backends. To plug into the compiler, use the 
 * IDL compiler option "-backend <classname>"
 *
 * @author Gerald Brose, XTRADYNE Technologies.
 * @version $Id$ 
 */

public interface IDLTreeVisitor
{
    /**
     * Visit an IDL specification. This method is the top-level entry point
     * called by the IDL compiler for a single compiler run.
     *
     * @param spec The spec node from the compiler's AST
     */

    void visitSpec( Spec spec );

    /**
     * Visit a module
     *
     * @param spec The module node from the compiler's AST
     */

    void visitModule( Module mod );


    void visitInterface( Interface intf  );

    void visitInterfaceBody( InterfaceBody body  );

    void visitDefinitions( Definitions defs  );

    void visitDefinition( Definition def  );

    void visitDeclaration( Declaration decl );

    void visitOpDecl( OpDecl decl );

    void visitMethod( Method m );

    void visitParamDecl( ParamDecl param );

    void visitStruct( StructType struct );

    void visitUnion( UnionType union );

    void visitEnum( EnumType enumType );


    void visitNative( NativeType _native );

    void visitTypeDef( TypeDef typedef );

    void visitAlias( AliasTypeSpec alias );

    void visitValue( Value value );

    void visitTypeDeclaration( TypeDeclaration typeDecl );

    void visitConstrTypeSpec( ConstrTypeSpec typeDecl );

}

