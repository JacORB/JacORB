package org.jacorb.ir;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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

import java.io.*;

import org.jacorb.orb.TypeCode;
import org.omg.CORBA.TCKind;

/**
 * This class prints IDL from IR-Descriptions to PrintStreams
 *
 * @author (c) Gerald Brose, FU Berlin 2000
 * @version $Id$
 */

public class IdlWriter 
{
    private PrintStream ps; 
    private int indent = 0;
    private org.omg.CORBA.ORB orb ;
    private org.omg.CORBA.Repository ir = null;

    /**
     *  create a new IdlWriter for the default JacORB IR 
     *  which writes to a specific PrintStream
     * 
     *  @param _ps	a PrintStream
     */

    public IdlWriter( PrintStream _ps )
    {
        ps = _ps;
        try
        {
            orb = org.omg.CORBA.ORB.init((String[])null, null);
            ir = org.omg.CORBA.RepositoryHelper.narrow( 
                     orb.resolve_initial_references("InterfaceRepository"));
        }
        catch( org.omg.CORBA.ORBPackage.InvalidName e )
        {}

        if( ir == null )
        {
            System.err.println("No IR configured! Exiting..");
            System.exit(1);
        }
    }

    /**
     *  create a new IdlWriter for a specific IR which writes 
     *  to a specific PrintStream
     * 
     *  @param _ps	a PrintStream
     *  @param _ir	a Repository
     */

    public IdlWriter( PrintStream _ps, org.omg.CORBA.Repository _ir ){
        ps = _ps;
        ir = _ir;
    }

    public void close()
    {
        ps.flush();
        ps.close();
    }

    private void indent(int indentation)
    {
        indent = indentation;
    }

    private void print( String s )
    {
        for( int i = 0; i < indent; i++ )
            ps.print(" ");
        ps.print(s);
    }

    /**
     *  print the IDL definition for a contained objec
     * 
     *  @param c	the contained object 
     *  @param indentation	how many spaces to use for indentation
     */

    public void printContained( org.omg.CORBA.Contained c, 
                                int indentation )
    {
        org.omg.CORBA.ContainedPackage.Description descr = c.describe();

        switch (descr.kind.value()) 
        {
        case org.omg.CORBA.DefinitionKind._dk_Module:
            {
                printModule( org.omg.CORBA.ModuleDescriptionHelper.extract( descr.value ),
                             indentation+3 );
                break;
            }
        case org.omg.CORBA.DefinitionKind._dk_Interface:
            {
                org.omg.CORBA.InterfaceDef idef = 
                    org.omg.CORBA.InterfaceDefHelper.narrow(  
                        ir.lookup_id(  
                            org.omg.CORBA.InterfaceDescriptionHelper.extract(descr.value).id ));
                printInterface( idef,
                                indentation+3 );
                break;
            }
        case org.omg.CORBA.DefinitionKind._dk_Attribute:
            {
                printAttribute( org.omg.CORBA.AttributeDescriptionHelper.extract( descr.value ),
                                indentation+3 );
                break;
            }
        case org.omg.CORBA.DefinitionKind._dk_Operation:
            {
                printOperation( org.omg.CORBA.OperationDescriptionHelper.extract( descr.value ),
                                indentation+3 );
                break;
            }
        case org.omg.CORBA.DefinitionKind._dk_Exception:
            {
                printException( org.omg.CORBA.ExceptionDescriptionHelper.extract( descr.value ),
                                indentation+3 );
                break;
            }
        case org.omg.CORBA.DefinitionKind._dk_Constant:
            {
                printConstant( org.omg.CORBA.ConstantDescriptionHelper.extract( descr.value ),
                               indentation+3 );
                break;
            }
        case org.omg.CORBA.DefinitionKind._dk_Struct:
            {
                printStruct( org.omg.CORBA.TypeDescriptionHelper.extract( descr.value ),
                             indentation+3 );
                break;
            }
        case org.omg.CORBA.DefinitionKind._dk_Enum:
            {
                printEnum( org.omg.CORBA.TypeDescriptionHelper.extract( descr.value ),
                           indentation+3 );
                break;
            }
        case org.omg.CORBA.DefinitionKind._dk_Union:
            {
                printUnion( org.omg.CORBA.TypeDescriptionHelper.extract( descr.value ),
                            indentation+3 );
                break;
            }
        case org.omg.CORBA.DefinitionKind._dk_Alias:
            {
                printAlias( org.omg.CORBA.TypeDescriptionHelper.extract( descr.value ),
                            indentation+3 );
                break;
            }
        }
    }

    /**
     *  print the IDL definition for a module
     * 
     *  @param mdes	the module description 
     *  @param indentation	how many spaces to use for indentation
     */

    public void printModule( org.omg.CORBA.ModuleDescription mdes, 
                             int indentation )
    {
        indent( indentation );

        org.omg.CORBA.ModuleDef mdef = 
            org.omg.CORBA.ModuleDefHelper.narrow( ir.lookup_id( mdes.id ));
        print("module " + mdef.name() + "\n" );
        print("{\n");
        org.omg.CORBA.Contained[] contents = 
            mdef.contents( org.omg.CORBA.DefinitionKind.dk_all, true );

        for( int x = 0; x < contents.length; x++){
            printContained( contents[x], indentation );
        }

        indent( indentation );
        print("};" + "\n\n");
    }


    /** 
     * print an IDL interface
     */

    public void printInterface( org.omg.CORBA.InterfaceDef idef, 
                                int indentation )
    {

        org.omg.CORBA.InterfaceDefPackage.FullInterfaceDescription idfid = 
            idef.describe_interface();
        org.omg.CORBA.Contained[] contents = 
            idef.contents( org.omg.CORBA.DefinitionKind.dk_all, true );

        indent( indentation );

        StringBuffer inheritanceSb = new StringBuffer();

        if( idfid.base_interfaces.length > 0 )
            inheritanceSb.append(" : " + idfid.base_interfaces[0] );

        for( int b = 1; b < idfid.base_interfaces.length; b++){
            inheritanceSb.append(", " + idfid.base_interfaces[b]);
        }

        print("interface " + idfid.name + inheritanceSb.toString() + "\n");
        print("{" + "\n");

        for( int x = 0; x < contents.length; x++){
            printContained( contents[x], indentation );
        }

        indent( indentation );
        print("};" + "\n\n" );
    }


    /** print an IDL exception def
     */

    public void printException( org.omg.CORBA.ExceptionDescription e, 
                                int indentation )
    {
        org.omg.CORBA.ExceptionDef e_def = 
            org.omg.CORBA.ExceptionDefHelper.narrow( ir.lookup_id( e.id )); 

        if( e_def != null )
        {
            org.omg.CORBA.StructMember [] members = e_def.members();
            indent( indentation );
            print( "exception " + e.name + " {" + "\n" );
            indent( indentation + 3 );
            for( int i = 0; i< members.length; i++){
                print( idlTypeName( members[i].type ) + " " + members[i].name + 
                       ";" + "\n" );
            }
            indent( indentation );
            print( "};" + "\n\n" );
        } 
        else 
            System.err.println("Error, could not find excpetion " + e.id + " in IR ");
    }

    /** print an IDL struct def
     */

    public void printStruct( org.omg.CORBA.TypeDescription t, int indentation )
    {
        org.omg.CORBA.StructDef s_def = 
            org.omg.CORBA.StructDefHelper.narrow(ir.lookup_id( t.id )); 

        if( s_def != null )
        {
            org.omg.CORBA.StructMember [] members = s_def.members();
            org.omg.CORBA.Contained [] contents = 
                s_def.contents(org.omg.CORBA.DefinitionKind.dk_all, false);

            indent( indentation );
            print( "struct " + s_def.name() + " {" + "\n" );
            indent( indentation + 3 );
            for( int i = 0; i< members.length; i++){
                print( idlTypeName( members[i].type ) + " " +
                       members[i].name + ";" + "\n" );
            }
            for( int i = 0; i< contents.length; i++){
                printContained( contents[i], indentation  );
            }			
            indent( indentation );
            print( "};" + "\n\n" );
        } 
        else 
            System.err.println("Error, could not find struct " + s_def.id() + " in IR ");

    }


    /** print an IDL const
     */

    public void printConstant( org.omg.CORBA.ConstantDescription c, int indentation )
    {
        indent( indentation );
        StringBuffer sb = new StringBuffer (  "const " + idlTypeName( c.type ) 
                                              + " " + c.name + " = " );
        switch ( c.type.kind().value() )
        {
        case org.omg.CORBA.TCKind._tk_string: sb.append( c.value.extract_string() ); break;
        case org.omg.CORBA.TCKind._tk_boolean: sb.append( c.value.extract_boolean() ); break;
        case org.omg.CORBA.TCKind._tk_long: sb.append( c.value.extract_long() ); break;
        case org.omg.CORBA.TCKind._tk_ulong: sb.append( c.value.extract_ulong() ); break;
        case org.omg.CORBA.TCKind._tk_longlong: sb.append( c.value.extract_longlong() ); break;
        case org.omg.CORBA.TCKind._tk_ulonglong: sb.append( c.value.extract_ulonglong() ); break;
        case org.omg.CORBA.TCKind._tk_short: sb.append( c.value.extract_short() ); break;
        case org.omg.CORBA.TCKind._tk_float: sb.append( c.value.extract_float() ); break;
        case org.omg.CORBA.TCKind._tk_octet: sb.append( c.value.extract_octet() ); break;
        case org.omg.CORBA.TCKind._tk_char: sb.append( c.value.extract_char() ); break;
        case org.omg.CORBA.TCKind._tk_fixed: sb.append( c.value.extract_fixed() ); break;
        }

        print( sb.toString()+";\n\n");
    }


    /** print an IDL attribute
     */

    public void printAttribute( org.omg.CORBA.AttributeDescription a, int indentation )
    {
        indent( indentation );
        String mode = "";
        if( a.mode.equals( org.omg.CORBA.AttributeMode.ATTR_READONLY ))
            mode = "readonly ";
        print( mode + "attribute " + idlTypeName(a.type)
               + " " + a.name + ";" +  "\n" );
    }


    /** print an IDL Enum
     */

    public void printEnum( org.omg.CORBA.TypeDescription t, int indentation )
    {
        org.omg.CORBA.EnumDef e_def =
            org.omg.CORBA.EnumDefHelper.narrow( ir.lookup_id( t.id )); 
        if( e_def != null )
        {
            String [] members = e_def.members();
            indent( indentation );
            StringBuffer vals = new StringBuffer();
            if( members.length > 0 )
                vals.append( members[0] );
            for( int i = 1; i< members.length; i++){
                vals.append( "," + members[i] );
            }
            print( "enum " + e_def.name() + " {" + vals + "};" + "\n\n" );
        } 
        else 
            System.err.println("Error, could not find enum " + e_def.id() + " in IR ");

    }

    /** print an IDL Union
     */

    public void printUnion( org.omg.CORBA.TypeDescription t, int indentation )
    {
        org.omg.CORBA.UnionDef u_def = 
            org.omg.CORBA.UnionDefHelper.narrow( ir.lookup_id( t.id )); 
        if( u_def != null )
        {
            org.omg.CORBA.UnionMember [] members = u_def.members();
            indent( indentation );
            print( "union " + u_def.name() + " switch ( " + 
                   idlTypeName(u_def.discriminator_type())
                   +  " )\n");
            print("{\n" );
            indent( indentation + 4 );
            int def_idx = -1;
            for( int i = 0; i < members.length; i++ )
            {
                if( members[i].label.type().kind() == org.omg.CORBA.TCKind.tk_octet &&
                    ( members[i].label.extract_octet() == (byte)0 ))
                {
                    def_idx = i;
                }
                else if( members[i].label.type().kind() == org.omg.CORBA.TCKind.tk_char )
                {
                    print("case \'" + members[i].label.extract_char() + "\' : " +  
                          idlTypeName(members[i].type) + " " 
                          + members[i].name + ";" + "\n");
                }
                else if( members[i].label.type().kind() == org.omg.CORBA.TCKind.tk_enum )
                {
                    int val = members[i].label.extract_long();
                    try 
                    {
                        print("case " + members[i].label.type().member_name(val) + " : " +  
                              idlTypeName(members[i].type) + " " + 
                              members[i].name + ";" + "\n");
                    }
                    catch( Exception bk )
                    {
                        bk.printStackTrace();
                    }
                }
                else
                    print("case " + members[i].label.type() + " : " +  
                          idlTypeName(members[i].type) + " " +
                          members[i].name + ";" + "\n");
            }
            if( def_idx != -1 )
            {
                print("default : " +  idlTypeName(members[def_idx].type) + " " + 
                      members[def_idx].name +";" + "\n" );
            }
            indent( indentation );
            print( "};" + "\n\n");
        } 
        else 
            System.err.println("Error, could not find union " + 
                               u_def.id() + " in IR ");
    }

    /** 
     * print an IDL alias
     */

    public void printAlias( org.omg.CORBA.TypeDescription t, int indentation )
    {
        org.omg.CORBA.AliasDef adef = 
            org.omg.CORBA.AliasDefHelper.narrow( ir.lookup_id( t.id ));
        indent( indentation );

        String originalTypeName = idlTypeName( adef.original_type_def().type());

        print("typedef " + originalTypeName + 
                  " " + adef.name() + ";\n\n");

    }

    /** 
     * print an IDL operation
     */

    public void printOperation(org.omg.CORBA.OperationDescription op, 
                               int indentation )
    {
        indent( indentation );

        String mode = "";
        if( op.mode.equals(org.omg.CORBA.OperationMode.OP_ONEWAY ))
            mode = "oneway ";
        print( mode +  idlTypeName(op.result) + " " + op.name + "(");

        indent(0);

        for( int i = 0; i < op.parameters.length-1; i++){
            printParameter(op.parameters[i], ",");
        }

        if( op.parameters.length > 0 )
            printParameter(op.parameters[op.parameters.length -1], "");
        print(")");

        if( op.exceptions.length > 0 ){
            print(" raises (");
            print( idlTypeName(op.exceptions[0].type ) );
            for( int i = 1; i < op.exceptions.length; i++){
                print( idlTypeName(op.exceptions[0].type ) + ",");
            }
            print(")");
        }
        print( ";" + "\n" );
        indent( indentation );
    }


    public void printParameter( org.omg.CORBA.ParameterDescription p, String separator )
    {
        if( p.mode.equals( org.omg.CORBA.ParameterMode.PARAM_OUT) )
            print("out ");
        else if ( p.mode.equals( org.omg.CORBA.ParameterMode.PARAM_INOUT ))
            print("inout ");
        else
            print("in ");
        print( idlTypeName(p.type) + " " + p.name );
        print( separator );
    }

    private String idlTypeName( org.omg.CORBA.TypeCode tc )
    {
        return ((jacorb.orb.TypeCode)tc).idlTypeName();
    }


}




