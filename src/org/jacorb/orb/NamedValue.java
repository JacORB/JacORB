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
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.jacorb.orb;

/**
 * @author Gerald Brose, FU Berlin 
 * @version $Id$	
 */

public class NamedValue 
    extends org.omg.CORBA.NamedValue
{
    private org.omg.CORBA.Any value;
    private String name;
    private int arg_modes;

    public NamedValue(int modes)
    {
	arg_modes = modes;
    }

    public NamedValue( String n, org.omg.CORBA.Any a, int modes )
    {
	name = n;
	value = a;
	arg_modes = modes;
    }

    public NamedValue( String n,  int modes )
    {
	name = n;
	arg_modes = modes;
    }
    
    public java.lang.String name()
    {
	return name;
    }

    public org.omg.CORBA.Any value()
    {
	return value;
    }
    
    public int flags()
    {
	return arg_modes;
    }

    public void set_value( org.omg.CORBA.Any v )
    {
	value = v;
    }

    /** JacORB-specific */

    public void send(org.omg.CORBA.portable.OutputStream out )
    {
	value().write_value(out);
    }

    public void receive(org.omg.CORBA.portable.InputStream in )
    { 
	value().read_value(in, value().type());
    }


}








