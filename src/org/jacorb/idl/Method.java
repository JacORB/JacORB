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
 *
 * This class is used to represent accessor operations
 */

import java.io.PrintWriter;

class Method
        implements Operation
{

    private TypeSpec resultType;
    private TypeSpec parameterType;
    private String name;
    private boolean pseudo;


    public Method( TypeSpec res, TypeSpec params, String name, boolean pseudo )
    {
        resultType = res;
        parameterType = params;
        this.name = name;
        this.pseudo = pseudo;
    }

    public String name()
    {
        return name;
    }

    public String opName()
    {
        if( resultType != null )
            return "_get_" + name;
        else
            return "_set_" + name;
    }

    public String signature()
    {
        StringBuffer sb = new StringBuffer();
        sb.append( name + "(" );
        if( parameterType != null )
        {
            sb.append( parameterType.toString() );
        }
        sb.append( ")" );
        return sb.toString();
    }

    public void printSignature( PrintWriter ps )
    {
        printSignature( ps, pseudo );
    }

    /**
     * @param printModifiers whether "public abstract" should be added
     */
    public void printSignature( PrintWriter ps, boolean printModifiers )
    {
        ps.print( "\t" );
        if( printModifiers )
            ps.print( "public abstract " );

        if( resultType != null )
        {
            ps.print( resultType.toString() );
            ps.println( " " + name + "();" );
        }
        else
        {
            ps.print( "void " + name + "(" );
            ps.print( parameterType.toString() );
            ps.println( " arg);" );
        }
    }


    public void printMethod( PrintWriter ps, String classname, boolean is_local, boolean is_abstract )
    {
        ps.print( "\tpublic " );

        if( resultType != null )
        {
            // accessor method
            ps.print( resultType.toString() );
            ps.println( " " + name + "()" );
            ps.println( "\t{" );
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
                ps.println( "\t\t\t\torg.omg.CORBA.portable.OutputStream _os = _request(\"_get_" + name + "\",true);" );
                ps.println( "\t\t\t\t_is = _invoke(_os);" );
                TypeSpec ts = resultType.typeSpec();
                ps.println( "\t\t\t\treturn " + ts.printReadExpression( "_is" ) + ";" );
                ps.println( "\t\t\t}" );
                ps.println( "\t\t\tcatch( org.omg.CORBA.portable.RemarshalException _rx ){}" );
                ps.println( "\t\t\tcatch( org.omg.CORBA.portable.ApplicationException _ax )" );
                ps.println( "\t\t\t{" );
                ps.println( "\t\t\t\tString _id = _ax.getId();" );
                ps.println( "\t\t\t\tthrow new RuntimeException(\"Unexpected exception \" + _id );" );
                ps.println( "\t\t\t}" );
                ps.println( "\t\t\tfinally" );
                ps.println( "\t\t\t{" );
                ps.println( "\t\t\t\tthis._releaseReply(_is);" );
                ps.println( "\t\t\t}" );
                ps.println( "\t\t}\n" );

                // local part
                ps.println( "\t\telse" );
                ps.println( "\t\t{" );
            }

            ps.println( "\t\torg.omg.CORBA.portable.ServantObject _so = _servant_preinvoke( \"_get_" + name + "\", _opsClass);" );

            ps.println( "\t\tif( _so == null )" );
            ps.println( "\t\t\tthrow new org.omg.CORBA.UNKNOWN(\"local invocations not supported!\");" );
            ps.println( "\t\t" + classname + "Operations _localServant = (" + classname + "Operations)_so.servant;" );
            ps.println( "\t\t\t" + resultType + " _result;" );

            ps.println( "\t\ttry" );
            ps.println( "\t\t{" );
            ps.println( "\t\t\t_result = _localServant." + name + "();" );
            ps.println( "\t\t}" );
            ps.println( "\t\tfinally" );
            ps.println( "\t\t{" );
            ps.println( "\t\t\t_servant_postinvoke(_so);" );
            ps.println( "\t\t}" );
            ps.println( "\t\treturn _result;" );
            ps.println( "\t\t}" );
            if( !is_local ) ps.println( "\t\t}\n" );
            ps.println( "\t}\n" );
        }
        else
        {
            /** modifier */

            ps.print( "void " + name + "(" + parameterType.toString() );
            ps.println( " a)" );
            ps.println( "\t{" );
            ps.println( "\t\twhile(true)" );
            ps.println( "\t\t{" );
            // remote part not for locality constrained objects
            //
            if( !is_local )
            {
                ps.println( "\t\tif(! this._is_local())" );
                ps.println( "\t\t{" );
                ps.println( "\t\t\torg.omg.CORBA.portable.InputStream _is = null;" );

                ps.println( "\t\t\ttry" );
                ps.println( "\t\t\t{" );
                ps.println( "\t\t\t\torg.omg.CORBA.portable.OutputStream _os = _request(\"_set_" + name + "\",true);" );
                ps.println( "\t\t\t\t" + parameterType.typeSpec().printWriteStatement( "a", "_os" ) );
                ps.println( "\t\t\t\t_is = _invoke(_os);" );
                ps.println( "\t\t\t\treturn;" );
                ps.println( "\t\t\t}" );
                ps.println( "\t\t\tcatch( org.omg.CORBA.portable.RemarshalException _rx ){}" );
                ps.println( "\t\t\tcatch( org.omg.CORBA.portable.ApplicationException _ax )" );
                ps.println( "\t\t\t{" );
                ps.println( "\t\t\t\tString _id = _ax.getId();" );
                ps.println( "\t\t\t\tthrow new RuntimeException(\"Unexpected exception \" + _id );" );
                ps.println( "\t\t\t}" );
                ps.println( "\t\t\tfinally" );
                ps.println( "\t\t\t{" );
                ps.println( "\t\t\t\tthis._releaseReply(_is);" );
                ps.println( "\t\t\t}" );
                ps.println( "\t\t}\n" );

                // local part
                ps.println( "\t\telse" );
                ps.println( "\t\t{" );

            }
            ps.println( "\t\t\torg.omg.CORBA.portable.ServantObject _so = _servant_preinvoke( \"_set_" + name + "\", _opsClass);" );

            ps.println( "\t\t\tif( _so == null )" );
            ps.println( "\t\t\t\tthrow new org.omg.CORBA.UNKNOWN(\"local invocations not supported!\");" );
            ps.println( "\t\t\t" + classname + "Operations _localServant = (" + classname + "Operations)_so.servant;" );

            ps.println( "\t\t\t\ttry" );
            ps.println( "\t\t\t\t{" );
            ps.println( "\t\t\t\t\t_localServant." + name + "(a);" );
            ps.println( "\t\t\t\t}" );
            ps.println( "\t\t\t\tfinally" );
            ps.println( "\t\t\t\t{" );
            ps.println( "\t\t\t\t\t_servant_postinvoke(_so);" );
            ps.println( "\t\t\t\t}" );
            ps.println( "\t\t\t\treturn;" );
            ps.println( "\t\t\t}" );
            if( !is_local ) ps.println( "\t\t}\n" );
            ps.println( "\t}\n" );
        }
    }


    public void print_sendc_Method( PrintWriter ps,
                                    String classname )
    {
        ps.print( "\tpublic void sendc_" );

        if( resultType != null )
        {
            // accessor method
            ps.print  ( "get_" + name );
            ps.println( "(AMI_" + classname + "Handler ami_handler)" );
            ps.println( "\t{" );
            ps.println( "\t\twhile(true)" );
            ps.println( "\t\t{" );

            ps.println( "\t\t\ttry" );
            ps.println( "\t\t\t{" );
            ps.println( "\t\t\t\torg.omg.CORBA.portable.OutputStream _os = _request(\"_get_" + name + "\",true);" );
            //ps.println( "\t\t\t\t_invoke(_os, ami_handler);" );
            ps.println( "\t\t\t\t((org.jacorb.orb.Delegate)_get_delegate()).invoke(this, _os, ami_handler);" );
            ps.println( "\t\t\t\treturn;" );
            ps.println( "\t\t\t}" );
            ps.println( "\t\t\tcatch( org.omg.CORBA.portable.RemarshalException _rx ){}" );
            ps.println( "\t\t\tcatch( org.omg.CORBA.portable.ApplicationException _ax )" );
            ps.println( "\t\t\t{" );
            ps.println( "\t\t\t\tString _id = _ax.getId();" );
            ps.println( "\t\t\t\tthrow new RuntimeException(\"Unexpected exception \" + _id );" );
            ps.println( "\t\t\t}" );
            ps.println( "\t\t}" );
            ps.println( "\t}\n" );
        }
        else
        {
            // modifier
            ps.print   ( "set_" + name );
            ps.print   ( "(AMI_" + classname + "Handler ami_handler, " );
            ps.println ( parameterType.toString() + " attr_" + name + ")");
            ps.println( "\t{" );
            ps.println( "\t\twhile(true)" );
            ps.println( "\t\t{" );
            ps.println( "\t\t\ttry" );
            ps.println( "\t\t\t{" );
            ps.println( "\t\t\t\torg.omg.CORBA.portable.OutputStream _os = _request(\"_set_" + name + "\",true);" );
            ps.println( "\t\t\t\t" + parameterType.typeSpec().printWriteStatement( "attr_" + name, "_os" ) );
            //ps.println( "\t\t\t\t_invoke(_os, ami_handler);" );
            ps.println( "\t\t\t\t((org.jacorb.orb.Delegate)_get_delegate()).invoke(this, _os, ami_handler);" );
            ps.println( "\t\t\t\treturn;" );
            ps.println( "\t\t\t}" );
            ps.println( "\t\t\tcatch( org.omg.CORBA.portable.RemarshalException _rx ){}" );
            ps.println( "\t\t\tcatch( org.omg.CORBA.portable.ApplicationException _ax )" );
            ps.println( "\t\t\t{" );
            ps.println( "\t\t\t\tString _id = _ax.getId();" );
            ps.println( "\t\t\t\tthrow new RuntimeException(\"Unexpected exception \" + _id );" );
            ps.println( "\t\t\t}" );
            ps.println( "\t\t}" );
            ps.println( "\t}\n" );
        }
    }

    public void printDelegatedMethod( PrintWriter ps )
    {
        ps.print( "\tpublic " );
        if( resultType != null )
        {
            ps.print( resultType.toString() );
            ps.println( " " + name + "()" );
            ps.println( "\t{" );
            ps.println( "\t\treturn _delegate." + name + "();" );
            ps.println( "\t}\n" );
        }
        else
        {
            /** modifier */

            ps.print( "void " + name + "(" + parameterType.toString() );
            ps.println( " a)" );
            ps.println( "\t{" );
            ps.println( "\t\t_delegate." + name + "(a);" );
            ps.println( "\t}\n" );
        }
    }

    public void printInvocation( PrintWriter ps )
    {

        ps.println( "\t\t\t_out = handler.createReply();" );
        ps.print( "\t\t\t" );

        if( resultType != null )
        {
            ps.println( resultType.typeSpec().printWriteStatement( name + "()", "_out" ) );
        }
        else
        {
            ps.println( name + "(" + parameterType.printReadExpression( "_input" ) + ");" );
        }
    }


}
