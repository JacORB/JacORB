package org.jacorb.naming;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2000  Gerald Brose.
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

import  java.util.*;
import  org.omg.CosNaming.*;
import  org.omg.CosNaming.NamingContextPackage.*;

/**
 * A convenience class for names and converting
 * between Names and their string representation
 *
 * @author Gerald Brose, FU Berlin
 * @version $Id$ 
 */

public class Name
    implements java.io.Serializable
{
    private NameComponent[] fullName;
    private NameComponent baseName;

    /** context part of this Name */
    private NameComponent[] ctxName; 

    public Name()
    {
	fullName = null;
	baseName = null;
	ctxName = null;
    }

    /**
     *	create a name from an array of NameComponents
     *	@param org.omg.CosNaming.NameComponent[] n
     */

    public Name(NameComponent[] n)
	throws InvalidName
    {
	if( n == null || n.length == 0 )
	    throw new InvalidName();

	fullName = n;
	baseName = n[ n.length-1 ];
	if( n.length > 1 )
	{
	    ctxName = new NameComponent[n.length-1];
	    for( int i = 0; i< n.length-1; i++ )
		ctxName[i] = n[i];
	} 
	else
	    ctxName = null;
    }

    /**
     *	create a name from a stringified name
     *	@param String structured_name
     */ 

    public Name(String string_name)
	throws org.omg.CosNaming.NamingContextPackage.InvalidName
    {
	this( toName( string_name) );
    }

    /**
     *	create a name from a singleNameComponent
     *	@param org.omg.CosNaming.NameComponent n
     * 
     */

    public Name(org.omg.CosNaming.NameComponent n)
	throws org.omg.CosNaming.NamingContextPackage.InvalidName
    {
	if( n == null )
	    throw new org.omg.CosNaming.NamingContextPackage.InvalidName();
	baseName = n;
	fullName = new org.omg.CosNaming.NameComponent[1];
	fullName[0] = n;
	ctxName = null;
    }

    /**
     *	@returns a NameComponent object representing the unstructured 
     *	base name of this structured name 
     */

    public org.omg.CosNaming.NameComponent baseNameComponent()
    {
	return baseName;
    }


    public String kind()
    {
	return baseName.kind;
    }

    /**
     *	@returns this name as an array of org.omg.CosNaming.NameComponent, 
     *	neccessary for a number of operations on naming context
     */

    public org.omg.CosNaming.NameComponent[] components()
    {
	return fullName;
    }

    /**
     *	@returns a Name object representing the name of the enclosing context 
     */

    public Name ctxName()
    {
	// null if no further context 
	if( ctxName != null )
	{
	    try 
	    {
		return new Name(ctxName);
	    } 
	    catch ( org.omg.CosNaming.NamingContextPackage.InvalidName im)
	    {
		im.printStackTrace();
		return null;
	    }
	}
	else 
	    return null;
    }

    public boolean equals( Object obj )
    {
	if( obj == null ) return false;
	if( !(obj instanceof Name) ) return false;
	return( toString().equals( obj.toString() ));
    }


    public Name fullName()
	throws org.omg.CosNaming.NamingContextPackage.InvalidName
    {
	return new Name(fullName);
    }

    public int hashCode()
    {
	return toString().hashCode();
    }

    /**
     * @returns  the string representation of this name
     */

    public String toString()
    {
	try
	{
	    return toString(fullName);
	}
	catch( InvalidName in )
	{
	    return "<invalid>";
	}
    }

    /**
     * @returns a single NameComponent, parsed from sn
     */

    private static org.omg.CosNaming.NameComponent getComponent(String sn)
	throws org.omg.CosNaming.NamingContextPackage.InvalidName
    {
	org.omg.CosNaming.NameComponent result = new org.omg.CosNaming.NameComponent("","");
	int start = 0;
	for( int i = 0; i < sn.length(); i++ )
	{
	    if( sn.charAt(i) == '.' ) 
	    {
		if( i == sn.length()-1 && i > 0)
		    throw new InvalidName();

		if( i > 0 && sn.charAt(i-1) != '\\')
		{
		    /* not an escaped dot */
		    if( start < i )
			result.id = sn.substring(start,i);

		    if( i < sn.length() )
			result.kind = sn.substring(i+1);
		    return result;
		}
		else
		{
		    /* leading '.' */
		    if( i < sn.length() )
			result.kind = sn.substring(i+1);
		    return result;
		}
	    }	    
	}
	/* no dot found */
	result.id = sn;
	return result;
    }

    /**
     * @returns an a array of NameComponents
     */

    public static org.omg.CosNaming.NameComponent[] toName(String sn) 
	throws org.omg.CosNaming.NamingContextPackage.InvalidName
    {
	if( sn.startsWith("/"))
	    throw new InvalidName();

	Vector v = new Vector();

	int start = 0;
	int i = 0;
	for( ; i < sn.length(); i++ )
	{
	    if( sn.charAt(i) == '/' && sn.charAt(i-1) != '\\')
	    {
		if( i-start == 0 )
		    throw new InvalidName();
		v.addElement( getComponent( sn.substring( start, i )));
		start = i+1;
	    }
	}
	if( start < i )
	    v.addElement( getComponent( sn.substring( start, i )));
	
	org.omg.CosNaming.NameComponent[] result = 
	new org.omg.CosNaming.NameComponent[v.size()];
	
	for( int j = 0; j < result.length; j++ )
	{	
	    result[j] = (org.omg.CosNaming.NameComponent)v.elementAt(j);
	}
	return result;
    }

    /**
     * @returns the string representation of this NameComponent array
     */

    public static String toString( org.omg.CosNaming.NameComponent[] n)
	throws org.omg.CosNaming.NamingContextPackage.InvalidName
    {
	if( n == null || n.length == 0 )
	    throw new org.omg.CosNaming.NamingContextPackage.InvalidName();

	StringBuffer b = new StringBuffer();
	for( int i = 0; i < n.length; i++ )
	{
	    if( i > 0 )
		b.append("/");

	    if( n[i].id.length() > 0 )
		b.append( escape(n[i].id) );

	    if( n[i].kind.length() > 0 || 
		n[i].id.length() == 0 )
		b.append(".");

	    if( n[i].kind.length() > 0 )
		b.append( escape(n[i].kind) );
	}
	return b.toString();
    }

    /**
     * escape any occurrence of "/", "." and "\"
     */

    private static String escape(String s)
    {
	StringBuffer sb = new StringBuffer(s);
	for( int i = 0; i < sb.length(); i++ )
	{
	    if( sb.charAt(i) == '/' || 
		sb.charAt(i) == '\\' || 
		sb.charAt(i) == '.' )
	    {
		sb.insert(i, '\\');
		i++;
	    }
	}
	return sb.toString();
    }


}









