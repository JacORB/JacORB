package org.jacorb.idl;

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


import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Base class for all classes of the abstract syntax tree
 *
 * @author Gerald Brose
 * @version $Id$
 */

class IdlSymbol
        extends java_cup.runtime.symbol
        implements java.io.Serializable
{

    private static int num = 10000;
    public String pack_name = "";
    String name = "";
    protected boolean is_pseudo = false; // is this a PIDL spec.?
    protected boolean included = false;
    protected boolean inhibitionFlag = false;
    str_token token;
    protected String _id;
    private String _version;
    protected IdlSymbol enclosing_symbol;
    protected String omg_package_prefix = "";
    private Hashtable imports = new Hashtable();

    String typeName;

    protected static final char fileSeparator =
            System.getProperty( "file.separator" ).charAt( 0 );

    public IdlSymbol( int num )
    {
        super( num );
        inhibitionFlag = parser.getInhibitionState();
    }

    public void set_included( boolean i )
    {
        included = i;
    }

    public boolean is_included()
    {
        return included;
    }

    public void set_pseudo()
    {
        is_pseudo = true;
    }

    public boolean is_pseudo()
    {
        return is_pseudo;
    }

    public void set_token( str_token i )
    {
        token = i;
        if( token != null )
        {
            if( token.pragma_prefix.equals( "omg.org" ) )
            {
                omg_package_prefix = "org.omg.";
            }
            set_name( token.str_val );
        }
    }

    public str_token get_token()
    {
        return token;
    }

    public String name()
    {
        return name;
    }

    /**
     * A number of IDL constructs need to have their names
     * checked for clashes with name reserved by Java or
     * the Java Language Mapping.
     */

    public void escapeName()
    {
        if( !name.startsWith( "_" ) &&
                lexer.strictJavaEscapeCheck( name ) )
        {
            name = "_" + name;
        }
    }

    public boolean isEscaped()
    {
        return ( name().startsWith( "_" ) );
    }

    public String deEscapeName()
    {
        String tmp = name();

        if( tmp.startsWith( "_" ) )
        {
            tmp = tmp.substring( 1 );
        }

        return tmp;
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        if( pack_name.length() > 0 )
            pack_name = s + "." + pack_name;
        else
            pack_name = s;
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
            throw new RuntimeException( "Compiler Error: trying to reassign container for "
                    + name );

        Environment.output( 5, "Symbol " + name + " of type " +
                getClass().getName() + " enclosed by symbol " +
                s.getClass().getName() );

        enclosing_symbol = s;
    }

    public IdlSymbol getEnclosingSymbol()
    {
        return enclosing_symbol;
    }

    public static int new_num()
    {
        return num++;
    }

    /** the name of this symbol */

    public void set_name( String n )
    {
        name = n;
    }

    /**
     * @returns fully scoped IDL identifier
     */

    String full_name()
    {
        if( name.length() == 0 )
        {
//              IdlSymbol enc = enclosing_symbol;
//              lexer.emit_warn( "Empty full_name for " + this.getClass().getName() +
//                               ( enc != null ? 
//                                 " at " + enc.get_token() :
//                                 " in pack " + pack_name ));

            return null;
        }

        if( pack_name.length() > 0 )
        {
            return pack_name + "." + name;
        }
        else
            return name;
    }

    /**
     * @returns fully scoped Java identifier, only used in
     * code generation phase
     */

    String javaName()
    {
        if( name.length() == 0 )
            return null;
        if( pack_name.length() > 0 )
        {
            if( !pack_name.startsWith( "org.omg" ) )
            {
                return omg_package_prefix + pack_name + "." + name;
            }
            else
                return pack_name + "." + name;
        }
        else
            return name;
    }

    /**
     * @returns "org.omg." if the symbol has been declared inside a
     * scope with a pragma prefix of "omg.org".
     */

    public String omgPrefix()
    {
        return omg_package_prefix;
    }


    /** empty parse */

    public void parse()
            throws ParseException
    {
    }

    public void print( PrintWriter ps )
    {
        throw new java.lang.RuntimeException( "--abstract--!" );
    }

    public void printImport( PrintWriter ps )
    {
        if( !pack_name.equals( "" ) )
        {
//              for( Enumeration e = parser.import_list.elements(); e.hasMoreElements(); )
//              {
//                  ps.println( "import " + (String)e.nextElement() + ";" );
//              }

//              ps.println();

            for( Enumeration e = imports.keys(); e.hasMoreElements(); )
            {
                String name = (String)e.nextElement();
                ps.println( "import " + name + ";" );
            }
            ps.println();
        }
    }

    /**
     * Called  by derived classes  to potentially add  the aliasHelper
     * name to  the  generated  Java class's  import  list, which  is
     * necessary in case the mapped code is in the unnamed package.
     *
     * @param alias the name of the alias
     */

    public void addImportedAlias( String alias )
    {
        Environment.output( 2, "addImportedAlias " + alias );
        if( alias.indexOf( '.' ) < 0 && !BaseType.isBasicName( alias ) )
        {
            imports.put( alias + "Helper", "" );
        }
    }

    /**
     * Called by  derived classes to potentially add  the name and the
     * nameHelper to the generated  Java class's import list, which is
     * necessary in case the mapped code is in the unnamed package.
     *
     * @param name
     */

    public void addImportedName( String name )
    {
        if( name != null && name.indexOf( '.' ) < 0 && !BaseType.isBasicName( name ) )
        {
            addImportedName( name, null );
        }
    }

    /**
     * Called by  derived classes to potentially add  the name and the
     * nameHelper to the generated  Java class's import list, which is
     * necessary in case the mapped code is in the unnamed package.
     *
     * @param name
     * @param type
     */

    public void addImportedName( String name, TypeSpec type )
    {
        if( name != null && name.indexOf( '.' ) < 0 && !BaseType.isBasicName( name ) )
        {
            Environment.output( 2, "addImportedName " + name );

            // If we have a typedef for a basic type we only want
            // to import the helper class.

            if( ( type == null ) || !BaseType.isBasicName( type.toString() ) )
            {
                imports.put( name, "" );
            }
            imports.put( name + "Helper", "" );
        }
    }

    /**
     * Called by  derived classes  to potentially  add the  name, the
     * nameHelper and nameHolder  to the generated Java class's import
     * list,  which is  necessary in  case the mapped  code is  in the
     * unnamed package.
     *
     * @param name
     */

    public void addImportedNameHolder( String name )
    {
        if( name.indexOf( '.' ) < 0 && !BaseType.isBasicName( name ) )
        {
            Environment.output( 2, "addImportedNameHolder " + name );

            imports.put( name, "" );
//              imports.put( name + "Helper", "" );
//              imports.put( name + "Holder", "" );
        }
    }

    public void setPrintPhaseNames()
    {
//          Environment.doAssert( parser.done_parsing,
//                                "Parser not done parsing yet.");

        if( pack_name.length() > 0 )
        {
            typeName = ScopedName.unPseudoName( pack_name + "." + name );
            if( !typeName.startsWith( "org.omg" ) )
            {
                typeName = omg_package_prefix + typeName;
            }
            pack_name = typeName.substring( 0, typeName.lastIndexOf( "." ) );
        }
        else
            typeName = ScopedName.unPseudoName( name );

        Environment.output( 2, "setPrintPhaseNames: pack_name " +
                pack_name + ", name " + name +
                " typename " + typeName );
    }

    public void printIdMethod( PrintWriter ps )
    {
        //System.out.println("Symbol " + full_name() + " (" + this.hashCode() + ") has prefix: " + pragmaPrefix );

        ps.println( "\tpublic static String id()" );
        ps.println( "\t{" );
        ps.println( "\t\treturn \"" + id() + "\";" );
        ps.println( "\t}" );
    }

    /**
     * @return this symbol's repository Id
     */

    String id()
    {
        Environment.output( 2, "Id for name " + name );
        IdlSymbol enc = enclosing_symbol;
        StringBuffer sb = new StringBuffer();

        if( _id == null )
        {
            //	    while( enc != null && enc.getEnclosingSymbol() != null )
            while( enc != null )
            {
                str_token t = enc.get_token();
                if( t == null )
                {
                    enc = enc.getEnclosingSymbol();
                    continue;
                }

                if( token != null )
                {
                    if( t.pragma_prefix.equals( token.pragma_prefix ) )
                    {
                        String enclosingName = enc.name;
                        // if the enclosing symbol is a module, its name
                        // is a package name and might have been modified
                        // by the -i2jpackage switch. We want its unchanged
                        // name as part of the RepositoryId, however.
                        if( enc instanceof Module )
                        {
                            String enclosingModuleName =
                                    ( (Module)enc ).originalModuleName();

                            if( !enclosingModuleName.startsWith( "org" ) )
                                enclosingName = ( (Module)enc ).originalModuleName();

                            // remove leading "_" in repository Ids
                            if( enc.isEscaped() )
                                enclosingName = enclosingName.substring( 1 );
                        }
                        sb.insert( 0, enclosingName + "/" );
                        enc = enc.getEnclosingSymbol();
                    }
                    else
                        break;
                }
                else
                {
                    break;
                }
            }

            if( isEscaped() )
                sb.append( name.substring( 1 ) );
            else
                sb.append( name );


            if( token != null && token.pragma_prefix.length() > 0 )
            {
                _id = "IDL:" + token.pragma_prefix + "/" + sb.toString().replace( '.', '/' ) + ":" + version();
            }
            else
            {
                _id = "IDL:" + sb.toString().replace( '.', '/' ) + ":" + version();
                //		_id = org.jacorb.orb.ir.RepositoryID.toRepositoryID( full_name());
            }
        }
        Environment.output( 2, "Id for name " + name + " is " + _id );
        return _id;
    }

    private String version()
    {
        IdlSymbol enc = this;
        String tmp;

        if( _version == null )
        {
            while( true )
            {
                while( enc != null && !( enc instanceof Scope ) )
                {
                    enc = enc.getEnclosingSymbol();
                }
                if( enc != null )
                {
                    ScopeData sd = ( (Scope)enc ).getScopeData();
                    if( sd == null )
                    {
                        org.jacorb.idl.parser.fatal_error( "ScopeDate null for " + name + " " +
                                this.getClass().getName(), null );
                    }
                    Hashtable h = sd.versionMap;

                    // check for version settings in this sope
                    tmp = (String)h.get( name );
                    if( tmp != null )
                    {
                        _version = tmp;
                        break;
                    }
                    enc = enc.getEnclosingSymbol();
                }
                else
                {
                    _version = "1.0";
                    break;
                }
            }

            // check for further versions (which would be an error!)

            if( enc != null )
                enc = enc.getEnclosingSymbol();

            while( true )
            {
                while( enc != null && !( enc instanceof Scope ) )
                {
                    enc = enc.getEnclosingSymbol();
                }
                if( enc != null )
                {
                    // check for version settings in this sope
                    Hashtable h = ( (Scope)enc ).getScopeData().versionMap;
                    tmp = (String)h.get( name );

                    if( tmp != null )
                    {
                        lexer.emit_error( "Version for " + name +
                                " already declared!", enc.get_token() );
                        break;
                    }
                    else
                        enc = enc.getEnclosingSymbol();
                }
                else
                {
                    break;
                }
            }

        }
        return _version;
    }


    /**
     * access to parser state (e.g. options)
     */

    protected boolean generateIncluded()
    {
        return parser.generateIncluded() && !( inhibitionFlag );
    }


}





