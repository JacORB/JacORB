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
 * IDL scoped names
 *
 * @author Gerald Brose
 * @version $Id$
 *
 */

import java.io.PrintWriter;
import java.util.*;

public class ScopedName
    extends SimpleTypeSpec
    implements SwitchTypeSpec
{
    private static Hashtable pseudoScopes = new Hashtable();

    private static Hashtable enumMap = new Hashtable();

    private static Stack recursionStack = new Stack();

    /**
     * Interfaces define a new scope, but since we can't do that
     * in Java, this kind of scope is called a 'pseudo scope' and
     * is just prepended to the interface name
     */

    public static void definePseudoScope( String name )
    {
        pseudoScopes.put( name, "" );
    }

    public static boolean isPseudoScope( String name )
    {
        return ( pseudoScopes.containsKey( name ) );
    }

    /**
     * unPseudo transforms scoped names like
     * module.Interface1.Interface2.Type_name to
     * module.Interface1Package.Interface2Package.Type_name
     */

    public static String unPseudoName( String name )
    {
        String n = unPseudo( name );
        if( n.endsWith( "PackagePackage" ) || !n.startsWith( "_" ) && n.endsWith( "Package" ) )
            n = n.substring( 0, n.lastIndexOf( "Package" ) );
        return n;
    }

    private static String unPseudo( String name )
    {
        if( name.charAt( 0 ) == '.' )
        {
            name = name.substring( 1 );
        }

        String head = name;
        String tail = null;
        int lastDot = name.lastIndexOf( '.' );

        if( lastDot < 0 )
            return name;

        while( !isPseudoScope( head ) )
        {
            // search for longest tail in scope name which
            // does not contain a pseudo scope

            lastDot = head.lastIndexOf( '.' );
            if( lastDot < 0 ) return name;
            head = name.substring( 0, lastDot );
            tail = name.substring( lastDot + 1 );
        }

        java.util.StringTokenizer strtok =
        new java.util.StringTokenizer( head, "." );
        String scopes[] = new String[ strtok.countTokens() ];

        for( int i = 0; strtok.hasMoreTokens(); scopes[ i++ ] = strtok.nextToken() )
            ;

        StringBuffer newHead = new StringBuffer();
        int j = 1;

        newHead.append( scopes[ 0 ] );

        while( !isPseudoScope( newHead.toString() ) )
        {
            if( j == scopes.length )
                return ( name );
            newHead.append( "." );
            newHead.append( scopes[ j++ ] );
        }

        StringBuffer copy = new StringBuffer( newHead.toString() );
        // we have to remember this...

        newHead.append( "Package" );

        while( j < scopes.length )
        {
            newHead.append( "." + scopes[ j ] );
            copy.append( "." + scopes[ j ] );
            if( isPseudoScope( copy.toString() ) )
                newHead.append( "Package" );
            j++;
        }

        if( tail != null )
            newHead.append( "." + tail );
        return newHead.toString();

    }

    /**
     * enumerations don't define new scopes in IDL, but their
     * mapping to Java introduces a new scope by generating
     * a new class for the enum's type. Thus, enumeration values
     * have to be additionally scoped in Java.
     */

    public static void enumMap( String n, String m )
    {
        enumMap.put( n, m );
    }

    private static String unEnum( String _name )
    {
        String n = (String)enumMap.get( _name );
        if( n != null )
            return n;
        else
            return _name;
    }

    /* end of static part */

    /* instance part */

    private TypeSpec resolvedSpec = null;
    private String resolvedName = null;
    private boolean resolved = false;
    private Interface resolvedInterface = null;
    boolean set = false;
    public String typeName = null;

    public ScopedName( int num )
    {
        super( num );
    }

    public Object clone()
    {
        ScopedName sn = new ScopedName( new_num() );
        sn.resolvedSpec = this.resolvedSpec;
        sn.resolvedName = this.resolvedName;
        sn.resolved = this.resolved;
        sn.typeName = this.typeName;
        sn.token = this.token;
        sn.set = this.set;

        /* superclass instance vars */
        sn.pack_name = this.pack_name;
        sn.name = this.name;
        sn.is_pseudo = this.is_pseudo;
        sn.included = this.included;
        sn.inhibitionFlag = this.inhibitionFlag;
        return sn;
    }

    public void setId( String _id )
    {
        if( logger.isInfoEnabled() )
            logger.info( "ScopedName.setId " + _id );

        typeName = _id;
        escapeName();
    }

    /**
     */

    public void escapeName()
    {
        if( !name.startsWith( "_" ) )
        {
            // if the type name is not a simple name, then insert the escape
            // char after the last dot
            if( typeName.indexOf( '.' ) > 0 )
            {
                if( lexer.strictJavaEscapeCheck( typeName.substring( typeName.lastIndexOf( '.' ) + 1 )))
                {
                    typeName =
                    typeName.substring( 0, typeName.lastIndexOf( '.' ) + 1 ) +
                    "_" + typeName.substring( typeName.lastIndexOf( '.' ) + 1 );
                }
            }
            else
            {
                if( lexer.strictJavaEscapeCheck( typeName ))
                    typeName = "_" + typeName;
            }
            if( logger.isInfoEnabled() )
                logger.info( "ScopedName.escapeName " + typeName );
        }
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
            throw new RuntimeException( "Compiler Error: trying to reassign container for " + name );
        enclosing_symbol = s;
    }

    public void parse()
    {
    }

    public boolean resolved()
    {
        return resolved;
    }

    public boolean basic()
    {
        TypeSpec t = resolvedTypeSpec();
        return t.basic();
    }

    public boolean is_pseudo()
    {
        return NameTable.isDefined( resolvedName(), "pseudo interface" );
    }

    public TypeSpec resolvedTypeSpec()
    {
        if( !resolved )
            resolvedName = resolvedName();
        if( resolvedSpec == null )
            parser.fatal_error( "Not a type: " + resolvedName, token );
        return resolvedSpec;
    }


    public String resolvedName()
    {
        if( !resolved )
            resolvedName = resolvedName( pack_name, typeName );

        ConstDecl constDecl = ConstDecl.getDeclaration( resolvedName );

        if( constDecl != null )
        {
            if( !constDecl.contained() )
            {
                resolvedName += ".value";
            }
        }
        resolved = true;
        return resolvedName;
    }


    /**
	 * This is the main name resolution algorithm. It resolves a qualified name
	 * s by replacing it by a fully qualified one, (watch out for typedef'd
	 * names!)
	 * 
	 * @param name
	 *            the name that is to be resolved. This may be a simple name, 
	 *            a partially qualified name, or even a fully qualifed name. 
	 * @param scopeOfOrigin
	 * 			  the scope from within which the resolution starts
	 * @return a fully qualified IDL identifier
	 */

    private String resolvedName( String scopeOfOrigin, String name )
    {
        if( logger.isInfoEnabled() )
            logger.info( "Resolve " + scopeOfOrigin + ":" + name );

        if( name == null )
            throw new RuntimeException( "Parser Error: null string in ScopedName (pack_name: " + scopeOfOrigin + ") !" );

        String result = null;

        // a fully qualified name starts with a '.'
        boolean global = false;
        if( name.charAt( 0 ) == '.' )
        {
        	// strip the dot
            name = name.substring( 1 );
            global = true;
        }

        // strip any "[]" but remember them for later
        String bracketSuffix = "";
        if( name.endsWith( "[]" ) )
        {
            result = name.substring( 0, name.indexOf( "[" ) );
            bracketSuffix = "[]";
        }
        else
            result = name;

        // see if "scope.name" is a defined name. If so, return that 
        // definition. First, try to form a combined name
        if( !global && 
        		NameTable.isDefined( scopeOfOrigin + "." + result ))
        {
            String unmappedResult = 
            	unMap( scopeOfOrigin + "."  + result );

            if( logger.isInfoEnabled() )
                logger.info( "resolve, " + scopeOfOrigin + "."  + result +
                             " was in name table, returning " + unmappedResult +
                             " suffix: " + bracketSuffix );

            return unmappedResult + bracketSuffix;
        }

        // now, check if name is known by itself (either because it is global
        // or it is a qualified name) 
        if( (global || result.indexOf('.') > 0 ) 
        		&& NameTable.isDefined(result) )
        {
            String unmappedResult = unMap( result );

            if( logger.isInfoEnabled() )
                logger.info( "resolve, found " + result +
                             " in name table, returning " + unmappedResult +
                             " suffix: " + bracketSuffix );

            return unmappedResult + bracketSuffix;
        }

        
        // split up all scopes contained in the name
        java.util.StringTokenizer strtok =
        	new java.util.StringTokenizer( name, "." );
        String nameScopes[] = new String[ strtok.countTokens() ];
        for( int i = 0; strtok.hasMoreTokens(); i++ ) {
            nameScopes[ i ] = strtok.nextToken();
        }

        // if there are scopes in the name itself...
        if( nameScopes.length > 0 )
        {
            // see if the compiler has registerd replacement names for
        	// our scopes
            String replacedPackageName = parser.pack_replace( nameScopes[ 0 ] );
            if( !replacedPackageName.equals( nameScopes[ 0 ] ) )
            {
                // okay, it has, so rebuild the fully scoped name 
                StringBuffer tmpString = new StringBuffer();
                tmpString.append( replacedPackageName );
                for( int i = 1; i < nameScopes.length; ++i )
                {
                    tmpString.append( "." );
                    tmpString.append( nameScopes[ i ] );
                }

                // check if this is a defined name now
                result = tmpString.toString();
                if( NameTable.isDefined( result ) )
                {
                    String unmappedResult = unMap( result );

                    if( logger.isInfoEnabled() )
                        logger.info( "resolve b, " + result + " was in name table, returning " +  unmappedResult + " suffix: " + bracketSuffix );
                    return unmappedResult + bracketSuffix;
                }
            }
        }

        // split up the individual scopes in the scopeOfOrigin
        java.util.StringTokenizer p_strtok =
        	new java.util.StringTokenizer( scopeOfOrigin, "." );
        String packageScopes[] = new String[ p_strtok.countTokens() ];
        for( int i = 0; p_strtok.hasMoreTokens(); i++ )
        {
            packageScopes[ i ] = p_strtok.nextToken();
        }

        // If the simple name was not known and we have no scopes at
		// all, try the global scope. 
        if( nameScopes.length == 0 || packageScopes.length == 0 )
        {
            if( NameTable.isDefined( result ) )
            {
                return unMap( result ) + bracketSuffix;
            }
            // else: the name is not found, emit an error message
            parser.fatal_error( "Undefined name: " + name + " .", token );
        }

        // if package name and the name which is to be resolved begin
        // with the same scoping qualifiers, strip these from name
        if( nameScopes[ 0 ].equals( packageScopes[ 0 ] ) )
        {
            StringBuffer tmpString = new StringBuffer();
            int minScopesLength = nameScopes.length < packageScopes.length ?
            		nameScopes.length : packageScopes.length;

            if( minScopesLength > 1 )
            {
                int i;
                for( i = 1; i < minScopesLength - 1; i++ )
                {
                    if( !( nameScopes[ i ].equals( packageScopes[ i ] ) ) )
                        break;
                }
                tmpString.append( nameScopes[ i ] );

                for( int k = i + 1; k < nameScopes.length; k++ )
                {
                    tmpString.append( "." );
                    tmpString.append( nameScopes[ k ] );
                }
                name = tmpString.toString();
            }
        }

        String prefix = "";
        int start_index = 0;

        if( parser.package_prefix != null )
        {
            prefix = parser.package_prefix + ".";
            java.util.StringTokenizer prefix_strtok =
            	new java.util.StringTokenizer( prefix, "." );
            String prefix_scopes[] = new String[ prefix_strtok.countTokens() ];

            for( int i = 0; prefix_strtok.hasMoreTokens(); i++ )
                prefix_scopes[ i ] = prefix_strtok.nextToken();

            while( start_index < prefix_scopes.length &&
                   prefix_scopes[ start_index ].equals( packageScopes[ start_index ] ) )
                start_index++;
        }

        StringBuffer buf = new StringBuffer();
        int k = packageScopes.length - start_index;

        if( k > 0 )
            buf.append( packageScopes[ start_index ] + "." );

        for( int j = start_index + 1; j < packageScopes.length; j++ )
        {
            buf.append( packageScopes[ j ] );
            buf.append( "." );
        }

        buf.append( name );

        int sub = start_index + 1;

        while( !NameTable.isDefined( prefix + buf.toString() ) )
        {
            if( sub > packageScopes.length )
            {
                parser.fatal_error( "Undefined name: " + scopeOfOrigin + "." + name, token );
                return "/* unresolved name */";
            }
            buf = new StringBuffer();
            k = packageScopes.length - sub++;
            if( k > 0 )
            {
                buf.append( packageScopes[ start_index ] + "." );
                for( int j = start_index + 1; j < k + start_index; j++ )
                {
                    buf.append( packageScopes[ j ] );
                    buf.append( "." );

                }
            }
            buf.append( name );
        }
        String res = unMap( prefix + buf.toString() ) + bracketSuffix;
        if( logger.isDebugEnabled() )
            logger.debug( "ScopedName.resolve (at end) returns: " + res );
        return res;
    }

    public TypeSpec typeSpec()
    {
        return this;
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        set = true;
        if( pack_name.length() > 0 )
            pack_name = s + "." + pack_name;
        else
            pack_name = s;
    }

    private String qualify( String str )
    {
        if( str.charAt( 0 ) == '.' )
        {
            return ( str.substring( 1 ) );
        }
        else
        {
            if( !pack_name.equals( "" ) )
                return ( pack_name + "." + str );
            else
                return str;
        }
    }

    /**
     * replace _name by the type name it stands for  (through
     * as many levels of typedef's as necessary) As a side effect,
     * set resolvedSpec to point to type spec object if the name
     * resolved was defined as a type name
     */

    private String unMap( String _name )
    {
        if( logger.isDebugEnabled() )
            logger.debug( "ScopedName.unmap: " + _name );

        String tmp = null;
        TypeSpec y = TypeMap.map( _name );

        if( logger.isDebugEnabled() )
            logger.debug( "ScopedName.unmap: " + _name + ", Type.map( " + _name + " ) is : " + y);

        TypeSpec x = null;

        while( y != null && !( y instanceof ScopedName )
               && !( y instanceof ConstrTypeSpec ) )
        {
            x = y;
            y = y.typeSpec();
            if( x.equals( y ) )
                break; // necessary?
        }

        if( y == null )
        {
            if( x != null )
            {
                resolvedSpec = x;
                return x.typeName();
            }
            else
                resolvedSpec = y;
            return unEnum( _name );
        }

        if( y instanceof ConstrTypeSpec )
        {
            resolvedSpec = y;
            return y.typeName();
        }

        if( y instanceof ScopedName && y != null && x != y )
            return unMap( y.typeName() );

        if( y == null )
        {
            resolvedSpec = x;
            return x.typeName();
        }
        else
        {
            resolvedSpec = y;
            return _name;
        }
    }


    /**
     * @return the fully qualified and resolved name in an intermediate
     * format, i.e. with "Package" suffixes but without potential "omg.org"
     * scopes
     */

    public String typeName()
    {
        String n = unPseudo( resolvedName( pack_name, typeName ) );

        if( n.endsWith( "PackagePackage" ) || !n.startsWith( "_" ) && n.endsWith( "Package" ) )
            n = n.substring( 0, n.lastIndexOf( "Package" ) );

        return n;
    }


    public String holderName()
    {
        return resolvedTypeSpec().holderName();
    }

    public String printReadExpression( String streamname )
    {
        return resolvedTypeSpec().printReadExpression( streamname );
    }

    public String printWriteStatement( String var_name, String streamname )
    {
        return resolvedTypeSpec().printWriteStatement( var_name, streamname );
    }

    public String printInsertExpression()
    {
        return resolvedTypeSpec().printInsertExpression();
    }

    public String printExtractExpression()
    {
        return resolvedTypeSpec().printExtractExpression();
    }

    /**
     * @return a string for an expression of type TypeCode that describes this type
     */

    public String getTypeCodeExpression()
    {
        return resolvedTypeSpec().getTypeCodeExpression();
    }

    public String id()
    {
        return resolvedTypeSpec().id();
    }

    public String toString()
    {
        String n = typeName();
        if( resolvedTypeSpec() != null && ( !n.startsWith( "org.omg" ) ) )
            n = resolvedTypeSpec().omgPrefix() + n;
        return n;
    }

    public void print( PrintWriter ps )
    {
    }

    public String IDLName()
    {
        String n = toString();
        StringBuffer sb = new StringBuffer();
        int from = 0;
        while( n.substring( from ).indexOf( '.' ) > 0 )
        {
            int to = from + n.substring( from ).indexOf( '.' );
            sb.append( n.substring( from, to ) + "::" );
            from = to + 1;
        }
        sb.append( n.substring( from ) );

        return sb.toString();
    }

    public static void addRecursionScope( String typeName )
    {
        recursionStack.push( typeName );
    }

    public static void removeRecursionScope( String typeName )
    {
        String check = (String)recursionStack.pop();
        if( typeName != null && ( check == null || !check.equals( typeName ) ) )
        {
            throw new RuntimeException( "RecursionScope Error, expected " +
                                        typeName + ", got " + check );
        }
    }

    public static boolean isRecursionScope( String typeName )
    {
        return ( recursionStack.search( typeName ) != -1 );
    }

    public boolean isSwitchable()
    {
        TypeSpec t = resolvedTypeSpec();
        return ( ( t instanceof SwitchTypeSpec ) &&
                 ( (SwitchTypeSpec)t ).isSwitchable() );
    }
}
