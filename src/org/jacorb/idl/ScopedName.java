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

package org.jacorb.idl;

/**
 * IDL scoped names
 * 
 * @author Gerald Brose
 * @version $Id$
 *
 */

import java.util.*;
import java.io.*;

class ScopedName 
    extends SimpleTypeSpec 
    implements SwitchTypeSpec 
{
    private static Hashtable pseudoScopes = 
	new Hashtable();

    private static Hashtable enumMap = 
	new Hashtable();

    private static Stack recursionStack = new Stack();

    /** 
     *  Interfaces define a new scope, but since we can't do that 
     *	in Java, this kind of scope is called a 'pseudo scope' and
     *	is just prepended to the interface name 
     */

    public static void definePseudoScope( String name )  
    { 
	pseudoScopes.put( name, "" );
    }

    private static boolean isPseudoScope( String name )
    {
	return( pseudoScopes.containsKey( name ));
    }

    /**
     * unPseudo transforms scoped names like 
     * module.Interface1.Interface2.Type_name to
     * module.Interface1Package.Interface2Package.Type_name
    */

    public static String unPseudoName( String name )
    {
	String n = unPseudo( name );
	if( n.endsWith("PackagePackage") || !n.startsWith("_") && n.endsWith("Package"))
	    n = n.substring( 0, n.lastIndexOf( "Package") );
	return n;
    }

    private static String unPseudo ( String name )
    {
	if( name.charAt(0) == '.' )
	{
	    name = name.substring(1);
	}

	String head = name;
	String tail = null;
	int lastDot = name.lastIndexOf('.');

	if( lastDot < 0 )
            return name;

	while( ! isPseudoScope( head ) )
	{ 
	    // search for longest tail in scope name which 
	    // does not contain a pseudo scope

	    lastDot = head.lastIndexOf('.');
	    if( lastDot < 0 ) return name;
	    head = name.substring( 0, lastDot );
	    tail = name.substring(lastDot+1);
	}

	java.util.StringTokenizer strtok = 
            new java.util.StringTokenizer( head, "." );
	String scopes[] = new String[strtok.countTokens()];

	for( int i = 0; strtok.hasMoreTokens(); scopes[i++] = strtok.nextToken() )
            ;

	StringBuffer newHead = new StringBuffer();
	int j = 1;

	newHead.append(scopes[0]);

	while( !isPseudoScope( newHead.toString() ))
        {
	    if( j == scopes.length )
		return( name );
	    newHead.append(".");
	    newHead.append(scopes[j++]);
	}

	StringBuffer copy = new StringBuffer(newHead.toString()); 
	// we have to remember this...

	newHead.append("Package");

	while( j < scopes.length )
        {
	    newHead.append("."+scopes[j]) ;
	    copy.append( "." + scopes[j] );
	    if( isPseudoScope( copy.toString()) )
		newHead.append("Package");
	    j++;
	    // old: newHead.append(scopes[j++]);
	}

	if( tail != null )
	    newHead.append( "."+tail );
	// debug: System.out.println("Unpseudo returns: " + 
	// 	newHead.toString());
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

    public ScopedName(int num) 
    {
	super(num);
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
	typeName = _id;
        escapeName();
    }

    /**
     * @overrides escapeName in IdlSymbol
     */

    public void escapeName()
    {
        if( ! name.startsWith("_") &&
            lexer.strictJavaEscapeCheck( typeName ))
        {
            typeName = "_" +typeName;
        }
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
	if( enclosing_symbol != null && enclosing_symbol != s )
	    throw new RuntimeException("Compiler Error: trying to reassign container for " + name );
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
	return NameTable.defined( resolvedName(), "pseudo interface");
    } 

    public TypeSpec resolvedTypeSpec()
    {
	if( !resolved )
	    resolvedName = resolvedName();
	if( resolvedSpec == null )
	    parser.fatal_error( "Not a type: " + resolvedName , token);
	return resolvedSpec;
    }


    public String resolvedName()
    {
	if( !resolved )
	    resolvedName = resolvedName( pack_name, typeName);

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
     * This is the main name resoltion algorithm. It
     * resolves a qualified name s by replacing it by a fully
     * qualified one, (watch out for typedef'd names!)
     *
     * @returns a fully qualified IDL identifier
     */

    private String resolvedName( String pack_name, String s )
    {
	Environment.output(3, "Resolve " + pack_name + ":" +  s);
        boolean global = false;

	if( s == null )
	    throw new RuntimeException("Parser Error: null string in ScopedName (pack_name: " + pack_name + ") !");

	String result = null;
	String suffix="";

	if( s.charAt(0) == '.' )
	{
            s = s.substring(1);
            global = true;
	}

	if( s.endsWith("[]"))
	{
	    result = s.substring(0, s.indexOf("["));
	    suffix = "[]";
	} 
        else
	    result = s;

	if( NameTable.defined( ( !global? pack_name + "." : "" ) + result ))
	{
            String unmap = unMap(( !global? pack_name + "." : "" ) + result);
	    Environment.output( 3, "resolve, " + ( !global? pack_name + "." : "" ) + result + 
                                " was in name table, returning " + unmap + 
                                " suffix: " + suffix );

	    return unmap + suffix ;
	}

	java.util.StringTokenizer strtok = 
	    new java.util.StringTokenizer( s, "." );
	String s_scopes[] = new String[strtok.countTokens()];

	for( int i = 0; strtok.hasMoreTokens(); i++)
	{
	    s_scopes[i] = strtok.nextToken();
	}

        // ADDED (Don Busch )
	if ( s_scopes.length > 0 ) 
        {
	    // Try to get help from the package map
	    String pack_replace = parser.pack_replace( s_scopes[0] );
	            
	    if ( ! pack_replace.equals( s_scopes[0] ) )
	    {
	        // rebuild the fully scoped name
	        StringBuffer t = new StringBuffer();
	        t.append( pack_replace );
	        for ( int i = 1; i < s_scopes.length; ++i ) {
	            t.append(".");
	            t.append( s_scopes[i] );
	        }

	        result = t.toString();
	        //System.out.println("ScopedName: checking " + result );
	        if( NameTable.defined( result ))
                {
                    String unmap2 = unMap(result);
                    Environment.output(3, "resolve b, " + result + 
                                       " was in name table, returning " + 
                                       unmap2 + " suffix: " + suffix );
                    return unmap2 + suffix ;	
	        }
	    }
        }
        // END ADDED

        // MOVED (Don Busch)

	java.util.StringTokenizer p_strtok = 
	    new java.util.StringTokenizer( pack_name, "." );
	String p_scopes[] = new String[p_strtok.countTokens()];

	for( int i = 0; p_strtok.hasMoreTokens(); i++)
	{
	    p_scopes[i] = p_strtok.nextToken();
	}
        // END MOVED

	/* If the  simple name was not known and we  have no scopes at
	   all, try the global scope. If the name's not found, emit an
	   error message */

	if( s_scopes.length == 0 || p_scopes.length == 0)
	{
            if( NameTable.defined(  result ))
            {            
                return unMap( result ) + suffix ;
            }
            // else 
	    parser.fatal_error( "Undefined name: " + s + " .", token );
	}

	// if package name and the name s, which is to be resolved, begin
	// with the same scoping qualifiers, strip these from s

	if( s_scopes[0].equals(p_scopes[0]))
	{
	    StringBuffer t = new StringBuffer();
	    int i;

	    int m = s_scopes.length < p_scopes.length ? 
                s_scopes.length : p_scopes.length;

	    if( m > 1 )
	    {
		for( i = 1; i < m -1 ; i++ )
                {
		    if( ! ( s_scopes[i].equals( p_scopes[i] )))
			break;
                }
		t.append( s_scopes[i] );

		for( int k = i+1; k < s_scopes.length; k++ )
		{
		    t.append(".");
		    t.append( s_scopes[k] );
		}
		s = t.toString();
	    }
	}

	String prefix = "";
	int start_index = 0;
	
	if( parser.package_prefix != null )
	{
	    prefix = parser.package_prefix + ".";
	    java.util.StringTokenizer prefix_strtok = 
		new java.util.StringTokenizer( prefix, "." );
	    String prefix_scopes[] = new String[prefix_strtok.countTokens()];

	    for( int i = 0; prefix_strtok.hasMoreTokens(); i++)
		prefix_scopes[i] = prefix_strtok.nextToken();

	    while( start_index < prefix_scopes.length &&
		   prefix_scopes[start_index].equals(p_scopes[start_index] ))
		start_index++;
	}

	StringBuffer buf = new StringBuffer();
	int k = p_scopes.length - start_index;

        if( k > 0 )
            buf.append( p_scopes[start_index] + ".");
	    
        for( int j= start_index+1 ; j < p_scopes.length ; j++)
        {
            buf.append( p_scopes[j] );
            buf.append(".");
        }

	buf.append( s );

	int sub = start_index+1;

	while( !NameTable.defined( prefix + buf.toString() ) )
	{
	    //System.out.println("sub = " + sub + ", Looking at " +  prefix +  buf.toString()  + " hash: " + (new String (prefix + buf.toString())).hashCode() );
	    if( sub > p_scopes.length )
	    {
		//    if( NameTable.defined( "Global." + s ))
		//    return unMap( "Global." + s ) + suffix ;
				// else
				// 	System.out.println("Global." + s +" not found.");
                //	new RuntimeException().printStackTrace();
                parser.fatal_error( "Undefined name: " + pack_name + "." + s , token);
		return "/* unresolved name */";
	    }
	    buf = new StringBuffer();
	    k = p_scopes.length -  sub++ ;
	    if( k > 0)
	    {
		buf.append( p_scopes[start_index] + ".");
		for( int j=start_index+1; j < k+start_index  ; j++)
		{
		    buf.append(p_scopes[j]);
		    buf.append(".");

		}
	    }
	    buf.append( s );
	}
	String res = unMap( prefix + buf.toString() ) + suffix ;
	Environment.output(4, "ScopedName.resolve (at end) returns: " + res);
	//System.out.println("Return! resolved name is " + res);
	return res;
    }

    public TypeSpec typeSpec()
    {
	return this;
    }

    public void setPackage( String s)
    {
        s = parser.pack_replace(s);
	set = true;
	if( pack_name.length() > 0 )
	    pack_name = new String( s + "." + pack_name );
	else
	    pack_name = s;
    }

    private String qualify( String str )
    {
	if( str.charAt(0) == '.' )
	{
	    return (str.substring( 1 ));
	} 
	else 
	{
	    if( !pack_name.equals("") )
		return (pack_name + "." + str);
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
	String tmp = null;
	TypeSpec y = TypeMap.map( _name );
	TypeSpec x = null;
	while( y != null && !(y instanceof ScopedName) 
	       && !(y instanceof ConstrTypeSpec) )
	{
	    x = y;
	    y = y.typeSpec();
	    if( x.equals(y) ) break; // necessary?
	}
	if( y == null )
	{
	    if( x != null )
	    {
		resolvedSpec = x;
		return x.typeName();
	    } else
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
     * @returns the fully qualified and resolved name in an intermediate
     * format, i.e. with "Package" suffixes but without potential "omg.org"
     * scopes
     */

    public String typeName()
    {
	String n = unPseudo( resolvedName( pack_name, typeName ));

	if( n.endsWith("PackagePackage") || !n.startsWith("_") && n.endsWith("Package"))
	    n = n.substring( 0, n.lastIndexOf( "Package") );

	int i =  n.indexOf('.');
	if( i > 0 && parser.hasImports())
	    n = n.substring(i+1);
	return n;
    }

    public String signature()
    {
	return resolvedTypeSpec().signature();
    }

    public String holderName()
    {
	return resolvedTypeSpec().holderName();
    }

    public String printReadExpression(String streamname)
    {
	return resolvedTypeSpec().printReadExpression( streamname);
    }

    public String printWriteStatement(String var_name, String streamname)
    {
	return resolvedTypeSpec().printWriteStatement(var_name, streamname);
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
     * @returns a string for an expression of type TypeCode that describes this type
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
	if( resolvedTypeSpec() != null && (! n.startsWith( "org.omg")))
	    n = resolvedTypeSpec().omgPrefix() + n;
	return n;
    }

    public void print(PrintWriter ps) 
    {
	//throw new RuntimeException("Compiler Error, don't call print() on ScopedName!");
	// ps.print(toString());
    }

    public String IDLName()
    {
        String n = toString();
        StringBuffer sb = new StringBuffer();
        int from = 0;
        while( n.substring( from ).indexOf('.') > 0 )
        {
            int to = from + n.substring( from ).indexOf('.') ;
            sb.append( n.substring( from, to ) + "::" );
            from = to + 1;
        }
        sb.append( n.substring( from ));

        return sb.toString();
    }

    public static void addRecursionScope(String typeName)
    {
        Environment.output(2 ,"addRecursionScope " +  typeName  );
        recursionStack.push( typeName );
    }

    public static void removeRecursionScope( String typeName )
    {
        String check = (String)recursionStack.pop();
        if( typeName != null && ( check == null || !check.equals( typeName )))
        {
            throw new RuntimeException("RecursionScope Error, expected " + 
                                       typeName + ", got " + check);
        }
    }

    public static boolean isRecursionScope(String typeName)
    {
        Environment.output(2 ,"Check isRecursionScope " +  typeName  +
                           " " + recursionStack.search( typeName ));

        return (recursionStack.search( typeName ) != -1);
    }


}













