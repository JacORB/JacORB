package org.jacorb.orb;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-98  Gerald Brose.
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
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139,
 *   USA.  
*/


public class Principal 
    extends org.omg.CORBA.Principal 
{
    private byte[] name = new byte[0];

    public Principal(){}

    public Principal( byte[] a ) 
    {
	name = new byte [ a.length];
	System.arraycopy(a,0,name,0,a.length);
    }

    public boolean equals( Object obj )
    {
	if( obj instanceof org.jacorb.orb.Principal )
	{
	    String myname = new String ( name );
	    return ( myname.equals( new String(  ((org.jacorb.orb.Principal)obj).name() )));
	} 
	else
	    return false;
    }

    public int hashCode()
    {
	if( name ==  null ) 
	    return 0;
	else
	    return (new String ( name )).hashCode();
    }

    public byte[] name() 
    {
	if( name == null )
	    return null;
	else 
	{
	    // copy, 
	    byte [] result = new byte [name.length];
	    System.arraycopy(name,0,result,0,name.length);
	    return result;
	}
    }

    public void name(byte[] a) 
    {
	name = new byte [ a.length];
	System.arraycopy(a,0,name,0,a.length);
    }

    public String toString()
    {
	return new String(name);
    }
}








