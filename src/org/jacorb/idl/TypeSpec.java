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

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Set;

/**
 * @author Gerald Brose
 */


public class TypeSpec
    extends IdlSymbol
{
    protected String alias = null;
    public TypeSpec type_spec;

    /**
     * Used to translate between Java value and CORBA key for Literal.parse error messages
     * e.g. org.omg.CORBA.Any --> any or
     */
    private static final HashMap<String, String> typeMap = new HashMap<String,String>();

    static
    {
       typeMap.put ("byte", "byte");
       typeMap.put ("org.omg.CORBA.Any", "any");
       typeMap.put ("char", "char");
       typeMap.put ("string", "string");
       typeMap.put ("java.math.BigDecimal", "fixed");
       typeMap.put ("float", "float");
       typeMap.put ("long", "long long");
       typeMap.put ("int", "int");
       typeMap.put ("byte", "byte");
       typeMap.put ("short", "short");

    }

    protected static String getIDLType (TypeSpec ts)
    {
       String key = ts.typeName ();
       if (ts instanceof CharType && ((CharType)ts).isWide ())
       {
          return "wchar";
       }
       else if (ts instanceof StringType && ((StringType)ts).isWide ())
       {
          return "wstring";
       }
       else if (ts instanceof IntType && ((IntType)ts).unsigned)
       {
          return "unsigned " + typeMap.get (key);
       }
       else if (ts instanceof LongType && ((LongType)ts).unsigned)
       {
          if (((LongType)ts).unsigned)
          {
             return "unsigned long";
          }
          else
          {
             return "long";
          }
       }
       else if ( ! typeMap.containsKey (key))
       {
          parser.error ("TypeSpec typeMap does not contain mapping for " + key + " - please report this bug");
          return key;
       }
       else
       {
          return typeMap.get (key);
       }
    }

    public TypeSpec( int num )
    {
        super( num );
    }

    public Object clone()
    {
        TypeSpec ts = new TypeSpec( new_num() );
        ts.type_spec = (TypeSpec)type_spec.clone();
        return ts;
    }

    public String typeName()
    {
        return type_spec.typeName();
    }

    public TypeSpec typeSpec()
    {
        return type_spec.typeSpec();
    }

    public int getTCKind()
    {
    	return type_spec.getTCKind();
    }

    public void accept( IDLTreeVisitor visitor )
    {
    	type_spec.accept(visitor);
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        type_spec.setPackage( s );
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
            throw new RuntimeException( "Compiler Error: trying to reassign container for " + name );
        enclosing_symbol = s;
        type_spec.setEnclosingSymbol( s );
    }

    /**
     * @return true if this is a basic type
     */

    public boolean basic()
    {
        if( type_spec == null )
            logger.warn("Typespec null " + getClass().getName() );
        return type_spec.basic();
    }

    public void set_constr( TypeDeclaration td )
    {
        ConstrTypeSpec c = new ConstrTypeSpec( new_num() );
        c.c_type_spec = td;
        type_spec = c;
    }

    public void parse()
    {
        type_spec.parse();
    }

    public String toString()
    {
        try
        {
            return type_spec.toString();
        }
        catch( NullPointerException np )
        {
            org.jacorb.idl.parser.fatal_error( "Compiler Error for " +
                                               type_spec + " " + typeName() + " " + np.getMessage(), null );
        }
        return null;
    }

    public String getTypeCodeExpression( Set knownTypes )
    {
        System.out.println("Add TypeSpec.GetTypeCodeExpression for " + this.getClass());
        return type_spec.getTypeCodeExpression( knownTypes );
    }

    /**
     * @return a string for an expression of type TypeCode
     *     that describes this type
     */
    public String getTypeCodeExpression()
    {
        return type_spec.getTypeCodeExpression();
    }

    public void print( PrintWriter ps )
    {
        if( !included )
            type_spec.print( ps );
    }

    public String holderName()
    {
        return type_spec.holderName();
    }

    /* helpers are not generated for base types, so
       there is no equivalent method to return helper
       names here. Such an operation is really only
       necessary for sequence types as a sequence's
       helper is named according to the sequence's
       element type. Therefore, the method helperName()
       throws an exception. Upon extending this class,
       the method has to be overwritten, if an helper class
       exists.
    */

    public String helperName() throws NoHelperException {
        throw new NoHelperException();
    }


    public String printReadExpression( String streamname )
    {
        return type_spec.printReadExpression( streamname );
    }

    public String printReadStatement( String var_name, String streamname )
    {
        return var_name + "=" + printReadExpression( streamname ) + ";";
    }

    public String printWriteStatement( String var_name, String streamname )
    {
        return type_spec.printWriteStatement( var_name, streamname );
    }

    public String printInsertExpression()
    {
        return type_spec.printInsertExpression();
    }

    public String printExtractExpression()
    {
        return type_spec.printExtractExpression();
    }

    /**
     * for use by subclasses when generating helper classes. Writes
     * common methods for all helpers to the helper class file. Must
     * be called after beginning the class definition itself
     */

    static void printHelperClassMethods( PrintWriter ps, String type )
    {
        printInsertExtractMethods( ps, type );

        ps.println( "\tpublic static org.omg.CORBA.TypeCode type()" );
        ps.println( "\t{" );
        ps.println( "\t\treturn _type;" );
        ps.println( "\t}" );
    }

    static void printInsertExtractMethods( PrintWriter ps, String type )
    {
        ps.println( "\tpublic static void insert (final org.omg.CORBA.Any any, final " + type + " s)" );
        ps.println( "\t{" );
        ps.println( "\t\tany.type(type());" );
        ps.println( "\t\twrite( any.create_output_stream(),s);" );
        ps.println( "\t}" + Environment.NL );

        ps.println( "\tpublic static " + type + " extract (final org.omg.CORBA.Any any)" );
        ps.println( "\t{" );

        ps.println( "\t\torg.omg.CORBA.portable.InputStream in = any.create_input_stream();" );
        ps.println( "\t\ttry" );
        ps.println( "\t\t{" );
        ps.println( "\t\t\treturn read (in);" );
        ps.println( "\t\t}" );
        ps.println( "\t\tfinally" );
        ps.println( "\t\t{" );

        ps.println( "\t\t\ttry");
        ps.println( "\t\t\t{");
        ps.println( "\t\t\t\tin.close();" );
        ps.println( "\t\t\t}");
        ps.println( "\t\t\tcatch (java.io.IOException e)");
        ps.println( "\t\t\t{" );
        ps.println( "\t\t\tthrow new RuntimeException(\"Unexpected exception \" + e.toString() );" );
        ps.println( "\t\t\t}" );
        ps.println( "\t\t}" );
        ps.println( "\t}" + Environment.NL );
    }

    /**
     * Prints the java-commands to insert the variable varname into
     * the Any anyname
     *
     * @param ps Stream, the commands shall be written to
     * @param anyname Name of the Any into which the variable shall be inserted
     * @param varname Name of the variable which shall be inserted
     */
    public void printInsertIntoAny(PrintWriter ps,
                                   String anyname,
                                   String varname)
    {
        type_spec.printInsertIntoAny(ps, anyname, varname );
    }

    /**
     * Prints the java-commands to extract the contents of the Any anyname
     * into a variable resultname with the type resulttype
     *
     * @param ps Stream, the commands shall be written to
     * @param resultname Name of the result variable
     * @param anyname Name of the Any holding the result
     * @param resulttype Type of the result variable
     */
    public void printExtractResult(PrintWriter ps,
                                   String resultname,
                                   String anyname,
                                   String resulttype)
    {
        type_spec.printExtractResult(ps, resultname, anyname, resulttype);
    }


}
