/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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
import java.util.*;

/**
 * @author Gerald Brose
 * @version $Id$
 */

class OpDecl
    extends Declaration
    implements Operation
{
    public static final int NO_ATTRIBUTE = 0;
    public static final int ONEWAY       = 1;
    
    public int opAttribute; // either NO_ATTRIBUTE or ONEWAY
    public TypeSpec opTypeSpec;
    public Vector paramDecls;
    public RaisesExpr raisesExpr;
    public IdlSymbol myInterface;

    public OpDecl( int num )
    {
        super( num );
        paramDecls = new Vector();
    }

    /**
     *  Constructs a new OpDecl with the given characteristics.
     */    
    public OpDecl (IdlSymbol myInterface, 
                   int opAttribute, 
                   TypeSpec opTypeSpec,
                   String name,
                   List paramDecls,
                   RaisesExpr raisesExpr)
    {
        super (new_num());
        this.myInterface = myInterface;
        this.opAttribute = opAttribute;
        this.opTypeSpec  = opTypeSpec;
        this.name        = name;
        this.paramDecls  = new Vector (paramDecls);
        this.raisesExpr  = raisesExpr;
        setEnclosingSymbol (myInterface);
        setPackage (myInterface.full_name());    
    } 

    /**
     *  Constructs a normal (not oneway) operation with void return type
     *  and no raises-Expression.
     */
    public OpDecl (IdlSymbol myInterface,
                   String    name,
                   List      paramDecls)
    {
        this (myInterface, 
              NO_ATTRIBUTE, 
              new VoidTypeSpec (new_num()),
              name, 
              paramDecls, 
              new RaisesExpr (new_num()));
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );

        if( pack_name.length() > 0 )
            pack_name = new String( s + "." + pack_name );
        else
            pack_name = s;
        opTypeSpec.setPackage( s );

        for( Enumeration e = paramDecls.elements();
             e.hasMoreElements();
             ( (ParamDecl)e.nextElement() ).setPackage( s )
                )
            ;
        raisesExpr.setPackage( s );
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
            throw new RuntimeException( "Compiler Error: trying to reassign container for "
                    + name );
        enclosing_symbol = s;
        raisesExpr.setEnclosingSymbol( s );
    }

    public void parse()
    {
        myInterface = enclosing_symbol;

        //        escapeName();
        if( opAttribute == ONEWAY )
        {
            if( !raisesExpr.empty() )
                parser.error( "Oneway operation " + full_name() +
                        " may not define a raises clases.", token );

            if( !( opTypeSpec.typeSpec() instanceof VoidTypeSpec ) )
                parser.error( "Oneway operation " + full_name() +
                        " may only define void as return type.", token );
        }

        try
        {
            NameTable.define( full_name(), "operation" );
        }
        catch( NameAlreadyDefined nad )
        {
            parser.error( "Operation " + full_name() + " already defined", token );
        }

        for( Enumeration e = paramDecls.elements(); e.hasMoreElements(); )
        {
            ParamDecl param = (ParamDecl)e.nextElement();
            param.parse();
            try
            {
                NameTable.define( full_name() + "." +
                        param.simple_declarator.name(),
                        "argument" );
            }
            catch( NameAlreadyDefined nad )
            {
                parser.error( "Argument " + param.simple_declarator.name() +
                        " already defined in operation " + full_name(),
                        token );
            }

            if( param.paramAttribute != ParamDecl.MODE_IN )
            {
                // for out and inout params
                myInterface.addImportedNameHolder( param.paramTypeSpec.holderName() );
            }
//              else
//              {
//                  Environment.output( 2, "addImportedName " + param.paramTypeSpec.toString()  );
//                  myInterface.addImportedName( param.paramTypeSpec.toString() );
//              }

            if( !(param.paramTypeSpec.typeSpec() instanceof BaseType ))
            {
                
                Environment.output( 2, param.paramTypeSpec.typeSpec().getClass().getName() );

                myInterface.addImportedName( param.paramTypeSpec.typeSpec().full_name(), 
                                             param.paramTypeSpec.typeSpec() );
            }
        }

        if( opTypeSpec.typeSpec() instanceof ScopedName )
        {
            TypeSpec ts =
                    ( (ScopedName)opTypeSpec.typeSpec() ).resolvedTypeSpec();

            if( ts != null )
                opTypeSpec = ts;

            myInterface.addImportedName( opTypeSpec.typeName() );
        }

        if( ( !NameTable.defined( opTypeSpec.typeName(), "type" ) ) &&
                ( !NameTable.defined( opTypeSpec.typeName(), "interface" ) ) )
        {
            //parser.error("Not a type: "+opTypeSpec.typeName(), token );
        }

        raisesExpr.parse();
    }


    public void print( PrintWriter ps )
    {
        if( is_pseudo )
            ps.print( "\tpublic abstract " + opTypeSpec.toString() );
        else
            ps.print( "\t" + opTypeSpec.toString() );
        ps.print( " " );
        ps.print( name );

        ps.print( "(" );
        Enumeration e = paramDecls.elements();
        if( e.hasMoreElements() )
            ( (ParamDecl)e.nextElement() ).print( ps );

        for( ; e.hasMoreElements(); )
        {
            ps.print( ", " );
            ( (ParamDecl)e.nextElement() ).print( ps );
        }
        ps.print( ")" );
        raisesExpr.print( ps );
        ps.println( ";" );
    }


    public void printMethod( PrintWriter ps,
                             String classname,
                             boolean is_local )
    {
        /* in some cases generated name have an underscore prepended for the
           mapped java name. On the wire, we must use the original name */

        String idl_name = ( name.startsWith( "_" ) ? name.substring( 1 ) : name );

        ps.print( "\tpublic " + opTypeSpec.toString() + " " + name + "(" );

        Enumeration e = paramDecls.elements();
        if( e.hasMoreElements() )
            ( (ParamDecl)e.nextElement() ).print( ps );

        for( ; e.hasMoreElements(); )
        {
            ps.print( ", " );
            ( (ParamDecl)e.nextElement() ).print( ps );
        }

        ps.print( ")" );
        raisesExpr.print( ps );
        ps.println( "\n\t{" );
        ps.println( "\t\twhile(true)" );
        ps.println( "\t\t{" );
        // remote part, not for locality constrained objects
        //
        if( !is_local )
        {
            ps.println( "\t\tif(! this._is_local())" );
            ps.println( "\t\t{" );
            ps.println( "\t\t\torg.omg.CORBA.portable.InputStream _is = null;" );
            ps.println( "\t\t\ttry" );
            ps.println( "\t\t\t{" );
            ps.print( "\t\t\t\torg.omg.CORBA.portable.OutputStream _os = _request( \"" + idl_name + "\"," );

            if( opAttribute == NO_ATTRIBUTE )
                ps.println( " true);" );
            else
                ps.println( " false);" );

            //  arguments..

            for( e = paramDecls.elements(); e.hasMoreElements(); )
            {
                ParamDecl p = ( (ParamDecl)e.nextElement() );
                if( p.paramAttribute != ParamDecl.MODE_OUT ) 
                    ps.println( "\t\t\t\t" + p.printWriteStatement( "_os" ) );
            }

            ps.println( "\t\t\t\t_is = _invoke(_os);" );

            if( opAttribute == 0 &&
                    !( opTypeSpec.typeSpec() instanceof VoidTypeSpec ) )
            {
                ps.println( "\t\t\t\t" + opTypeSpec.toString() + " _result = " +
                        opTypeSpec.typeSpec().printReadExpression( "_is" ) + ";" );
            }

            for( Enumeration e2 = paramDecls.elements(); e2.hasMoreElements(); )
            {
                ParamDecl p = (ParamDecl)e2.nextElement();
                if( p.paramAttribute != ParamDecl.MODE_IN )
                {
                    ps.println( "\t\t\t\t" + p.simple_declarator + ".value = " +
                            p.printReadExpression( "_is" ) + ";" );
                }
            }

            if( opAttribute == NO_ATTRIBUTE &&
                    !( opTypeSpec.typeSpec() instanceof VoidTypeSpec ) )
            {
                ps.println( "\t\t\t\treturn _result;" );
            }
            else
                ps.println( "\t\t\t\treturn;" );

            /* catch exceptions */

            ps.println( "\t\t\t}" );
            ps.println( "\t\t\tcatch( org.omg.CORBA.portable.RemarshalException _rx ){}" );
            ps.println( "\t\t\tcatch( org.omg.CORBA.portable.ApplicationException _ax )" );
            ps.println( "\t\t\t{" );
            ps.println( "\t\t\t\tString _id = _ax.getId();" );

            if( !raisesExpr.empty() )
            {
                String[] exceptIds = raisesExpr.getExceptionIds();
                String[] classNames = raisesExpr.getExceptionClassNames();
                ps.print( "\t\t\t\t" );
                for( int i = 0; i < exceptIds.length; i++ )
                {
                    ps.println( "if( _id.equals(\"" + exceptIds[ i ] + "\"))" );
                    ps.println( "\t\t\t\t{" );
                    ps.println( "\t\t\t\t\tthrow " + classNames[ i ] + "Helper.read(_ax.getInputStream());" );
                    ps.println( "\t\t\t\t}" );
                    ps.print( "\t\t\t\telse " );
                }
                ps.print( "\n\t" );
            }
            ps.println( "\t\t\t\tthrow new RuntimeException(\"Unexpected exception \" + _id );" );
            ps.println( "\t\t\t}" );
            ps.println( "\t\t\tfinally" );
            ps.println( "\t\t\t{" );
            ps.println( "\t\t\t\tthis._releaseReply(_is);" );
            ps.println( "\t\t\t}" );

            ps.println( "\t\t}" );
            // local part
            ps.println( "\t\telse" );
            ps.println( "\t\t{" );
        }

        ps.println( "\t\t\torg.omg.CORBA.portable.ServantObject _so = _servant_preinvoke( \"" + idl_name + "\", _opsClass );" );

        ps.println( "\t\t\tif( _so == null )" );
        ps.println( "\t\t\t\tthrow new org.omg.CORBA.UNKNOWN(\"local invocations not supported!\");" );

        ps.println( "\t\t\t" + classname + "Operations _localServant = (" +
                classname + "Operations)_so.servant;" );

        if( opAttribute == 0 &&
                !( opTypeSpec.typeSpec() instanceof VoidTypeSpec ) )
        {
            ps.print( "\t\t\t" + opTypeSpec.toString() + " _result;" );
        }

        ps.println( "\t\t\ttry" );
        ps.println( "\t\t\t{" );

        if( opAttribute == 0 &&
                !( opTypeSpec.typeSpec() instanceof VoidTypeSpec ) )
        {
            ps.print( "\t\t\t_result = " );
        }
        else
            ps.print( "\t\t\t" );

        ps.print( "_localServant." + name + "(" );

        for( e = paramDecls.elements(); e.hasMoreElements(); )
        {
            ParamDecl p = ( (ParamDecl)e.nextElement() );
            ps.print( p.simple_declarator.toString() );
            if( e.hasMoreElements() )
                ps.print( "," );
        }
        ps.println( ");" );

        ps.println( "\t\t\t}" );
        ps.println( "\t\t\tfinally" );
        ps.println( "\t\t\t{" );
        ps.println( "\t\t\t\t_servant_postinvoke(_so);" );
        ps.println( "\t\t\t}" );

        if( opAttribute == 0 && !( opTypeSpec.typeSpec() instanceof VoidTypeSpec ) )
        {
            ps.println( "\t\t\treturn _result;" );
        }
        else
            ps.println( "\t\t\treturn;" );


        if( !is_local ) ps.println( "\t\t}\n" );

        ps.println( "\t\t}\n" ); // end while
        ps.println( "\t}\n" ); // end method
    }

    public void print_sendc_Method( PrintWriter ps,
                                    String classname )
    {
        /* in some cases generated name have an underscore prepended for the
           mapped java name. On the wire, we must use the original name */

        String idl_name = ( name.startsWith( "_" ) ? name.substring( 1 ) : name );

        ps.print( "\tpublic void sendc_" + name + "(" );
        
        ps.print( "AMI_" + classname + "Handler ami_handler" );

        for ( Iterator i = paramDecls.iterator(); i.hasNext(); )
        {
            ParamDecl p = ( ParamDecl ) i.next();
            if ( p.paramAttribute != ParamDecl.MODE_OUT )
            {
                ps.print( ", " );
                p.print( ps );
            }
        }

        ps.print( ")" );
        ps.println( "\n\t{" );
        ps.println( "\t\twhile(true)" );
        ps.println( "\t\t{" );
        ps.println( "\t\t\ttry" );
        ps.println( "\t\t\t{" );
        ps.print( "\t\t\t\torg.omg.CORBA.portable.OutputStream _os = _request( \"" + idl_name + "\"," );

        if( opAttribute == NO_ATTRIBUTE )
            ps.println( " true);" );
        else
            ps.println( " false);" );

        //  arguments..

        for( Iterator i = paramDecls.iterator(); i.hasNext(); )
        {
            ParamDecl p = ( (ParamDecl)i.next() );
            if( p.paramAttribute != ParamDecl.MODE_OUT ) 
                ps.println( "\t\t\t\t" + p.printWriteStatement( "_os" ) );
        }

        //ps.println( "\t\t\t\t_invoke(_os, ami_handler);" );
        ps.println( "\t\t\t\t((org.jacorb.orb.Delegate)_get_delegate()).invoke(this, _os, ami_handler);" );
        ps.println( "\t\t\t\treturn;");

        /* catch exceptions */

        ps.println( "\t\t\t}" );
        ps.println( "\t\t\tcatch( org.omg.CORBA.portable.RemarshalException _rx ){}" );
        ps.println( "\t\t\tcatch( org.omg.CORBA.portable.ApplicationException _ax )" );
        ps.println( "\t\t\t{" );
        ps.println( "\t\t\t\tString _id = _ax.getId();" );
        ps.println( "\t\t\t}" );

        ps.println( "\t\t}\n" ); // end while
        ps.println( "\t}\n" ); // end method
    }

    public void printDelegatedMethod( PrintWriter ps )
    {
        ps.print( "\tpublic " + opTypeSpec.toString() + " " + name + "(" );

        Enumeration e = paramDecls.elements();
        if( e.hasMoreElements() )
            ( (ParamDecl)e.nextElement() ).print( ps );

        for( ; e.hasMoreElements(); )
        {
            ps.print( ", " );
            ( (ParamDecl)e.nextElement() ).print( ps );
        }

        ps.print( ")" );
        raisesExpr.print( ps );
        ps.println( "\n\t{" );


        if( opAttribute == NO_ATTRIBUTE && 
            !( opTypeSpec.typeSpec() instanceof VoidTypeSpec ) )
        {
            ps.print( "\t\treturn " );
        }

        ps.print( "_delegate." + name + "(" );
        e = paramDecls.elements();
        if( e.hasMoreElements() )
            ps.print( ( (ParamDecl)e.nextElement() ).simple_declarator );

        for( ; e.hasMoreElements(); )
        {
            ps.print( "," );
            ps.print( ( (ParamDecl)e.nextElement() ).simple_declarator );
        }
        ps.println( ");" );
        ps.println( "\t}\n" );
    }

    public void printInvocation( PrintWriter ps )
    {
        if( !raisesExpr.empty() )
        {
            ps.println( "\t\t\ttry" );
            ps.println( "\t\t\t{" );
        }

        /* read args */

        int argc = 0;
        boolean holders = false;

        for( Enumeration e = paramDecls.elements(); e.hasMoreElements(); )
        {
            ParamDecl p = (ParamDecl)e.nextElement();
            TypeSpec ts = p.paramTypeSpec.typeSpec();

            boolean is_wstring = 
                ( ( ts instanceof StringType ) && ( ( (StringType)ts ).isWide() ) );

            boolean is_wchar = 
                ( ( ts instanceof CharType ) && ( ( (CharType)ts ).isWide() ) );

            if( p.paramAttribute == ParamDecl.MODE_IN )
            {
                ps.println( "\t\t\t\t" + ts.toString() + " _arg" + ( argc++ ) +
                        "=" + ts.printReadExpression( "_input" ) + ";" );
            }
            else
            {
                holders = true;
                ps.println( "\t\t\t\t" + ts.holderName() + " _arg" + ( argc++ ) +
                        "= new " + ts.holderName() + "();" );
                if( p.paramAttribute == ParamDecl.MODE_INOUT )
                {
                    // wchars and wstrings are contained in CharHolder and
                    // StringHolder and so cannot be inserted via _read operation
                    // on holder. Instead value of holder needs to be set directly
                    // from correct type explicitly read from stream.

                    if( is_wchar )
                    {
                        ps.println( "\t\t\t\t_arg" + ( argc - 1 ) + ".value = _input.read_wchar ();" );
                    }
                    else if( is_wstring )
                    {
                        ps.println( "\t\t\t\t_arg" + ( argc - 1 ) + ".value = _input.read_wstring ();" );
                    }
                    else
                    {
                        ps.println( "\t\t\t\t_arg" + ( argc - 1 ) + "._read (_input);" );
                    }
                }
            }
        }


        boolean complex =
                ( opTypeSpec.typeSpec() instanceof ArrayTypeSpec ) ||
                ( opTypeSpec.typeSpec() instanceof FixedPointType );

        String write_str = null,
                write_str_prefix = null,
                write_str_suffix = null;

//	if( (!(opTypeSpec.typeSpec() instanceof VoidTypeSpec ))    || holders )
//	{
        ps.println( "\t\t\t\t_out = handler.createReply();" );
        if( !( opTypeSpec.typeSpec() instanceof VoidTypeSpec ) && !complex )
        {
            write_str = opTypeSpec.typeSpec().printWriteStatement( "**", "_out" );
            int index = write_str.indexOf( "**" );
            write_str_prefix = write_str.substring( 0, index );
            write_str_suffix = write_str.substring( index + 2 );
            ps.print( "\t\t\t\t" + write_str_prefix );
        }
        else
            ps.print( "\t\t\t\t" );
//	}


        if( complex )
            ps.print( opTypeSpec.typeSpec().typeName() + " _result = " );

        ps.print( name + "(" );

        for( int i = 0; i < argc; i++ )
        {
            ps.print( "_arg" + i );
            if( i < argc - 1 )
                ps.print( "," );
        }

        /*

          Enumeration e = paramDecls.elements();
          if(e.hasMoreElements())
          {
          TypeSpec ts = ((ParamDecl)e.nextElement()).paramTypeSpec;
          ps.print(ts.printReadExpression("input"));
          }

          for(; e.hasMoreElements();)
          {
          TypeSpec ts = ((ParamDecl)e.nextElement()).paramTypeSpec;
          ps.print("," + ts.printReadExpression("input"));
          }
        */

        if( !( opTypeSpec.typeSpec() instanceof VoidTypeSpec ) )
            ps.print( ")" );

        if( !complex )
        {
            if( opTypeSpec.typeSpec() instanceof VoidTypeSpec )
                ps.println( ");" );
            else
                ps.println( write_str_suffix );
        }
        else
        {
            ps.println( ";" );
            ps.println( opTypeSpec.typeSpec().printWriteStatement( "_result", "_out" ) );
        }

        /* write holder values */

        argc = 0;
        for( Enumeration e = paramDecls.elements(); e.hasMoreElements(); )
        {
            ParamDecl p = (ParamDecl)e.nextElement();
            TypeSpec ts = p.paramTypeSpec;
            if( p.paramAttribute != ParamDecl.MODE_IN )
            {
                ps.println( "\t\t\t\t" + p.printWriteStatement( ( "_arg" + ( argc ) ), "_out" ) );
                //		ps.println("\t\t\t\t_arg" + (argc) + "._write(_out);");
            }
            argc++;
        }

        if( !raisesExpr.empty() )
        {
            ps.println( "\t\t\t}" );
            String[] excepts = raisesExpr.getExceptionNames();
            String[] classNames = raisesExpr.getExceptionClassNames();
            for( int i = 0; i < excepts.length; i++ )
            {
                ps.println( "\t\t\tcatch(" + excepts[ i ] + " _ex" + i + ")" );
                ps.println( "\t\t\t{" );
                ps.println( "\t\t\t\t_out = handler.createExceptionReply();" );
                ps.println( "\t\t\t\t" + classNames[ i ] + "Helper.write(_out, _ex" + i + ");" );
                ps.println( "\t\t\t}" );
            }
        }

    }

    public String signature()
    {
        StringBuffer sb = new StringBuffer();
        sb.append( name + "(" );

        Enumeration e = paramDecls.elements();
        if( e.hasMoreElements() )
            sb.append( ( (ParamDecl)e.nextElement() ).paramTypeSpec.toString() );

        for( ; e.hasMoreElements(); )
        {
            sb.append( "," + ( (ParamDecl)e.nextElement() ).paramTypeSpec.toString() );
        }
        sb.append( ")" );
        return sb.toString();
    }


    public String name()
    {
        return name;
    }


    public String opName()
    {
        return name();
    }

    public void printSignature( PrintWriter ps )
    {
        printSignature( ps, false );
    }

    /**
     * @param printModifiers whether "public abstract" should be added
     */
    public void printSignature( PrintWriter ps, boolean printModifiers )
    {
        ps.print( "\t" );
        if( printModifiers ) ps.print( "public abstract " );

        ps.print( opTypeSpec.toString() + " " + name + "(" );

        for( Enumeration e = paramDecls.elements(); e.hasMoreElements(); )
        {
            ( (ParamDecl)e.nextElement() ).print( ps );
            if( e.hasMoreElements() ) ps.print( ", " );
        }

        ps.print( ")" );
        raisesExpr.print( ps );
        ps.println( ";" );
    }


    /**
     *    collect Interface Repository information in the argument hashtable
     */

    public void getIRInfo( Hashtable irInfoTable )
    {
        StringBuffer sb = new StringBuffer();
        boolean enter = false;

        TypeSpec ts = opTypeSpec.typeSpec();

        if( ts instanceof AliasTypeSpec )
        {
            //             if( ((AliasTypeSpec)ts).originalType.typeSpec() instanceof FixedPointType )
//              {
            sb.append( ts.full_name() );
            enter = true;
            //             }
        }
        sb.append( "(" );

        for( Enumeration e = paramDecls.elements(); e.hasMoreElements(); )
        {
            ParamDecl param = (ParamDecl)e.nextElement();
            if( param.paramAttribute == ParamDecl.MODE_INOUT )
            {
                sb.append( "inout:" + param.simple_declarator.name + " " );
                enter = true;
            }
            else if( param.paramAttribute == ParamDecl.MODE_OUT )
            {
                sb.append( "out:" + param.simple_declarator.name + " " );
                enter = true;
            }
            else // MODE_IN
                sb.append( "in:" + param.simple_declarator.name + " " );

            ts = param.paramTypeSpec.typeSpec();

            if( ts instanceof AliasTypeSpec )
            {
                sb.append( ts.full_name() );
                enter = true;
            }

            sb.append( "," );
        }

        if( paramDecls.size() > 0 )
        {
            // remove extra trailing ","
            //sb.deleteCharAt( sb.length()-1);
            // ugly workaround for non exisitng delete in jdk1.1
            sb = new StringBuffer( sb.toString().substring( 0, sb.length() - 1 ) );
        }
        sb.append( ")" );

        if( opAttribute == ONEWAY )
            sb.append( "-oneway" );

        //       if( enter )
        irInfoTable.put( name, sb.toString() );

        Environment.output( 2, "OpInfo for " + name + " : " + sb.toString() );
    }


}




