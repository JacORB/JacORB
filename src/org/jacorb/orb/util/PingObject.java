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

package org.jacorb.orb.util;

/**
 * @version $Id$
 */

import org.jacorb.orb.ParsedIOR;
import java.io.*;

public class PingObject 
{
    public static void main( String args[] )
    {
	org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args,null);
	String iorString = null;

	if( args.length < 1 || args.length > 2 )
	{
	    System.err.println("Usage: java org.jacorb.orb.util.PingObject [ ior_str | -f filename ]");
	    System.exit( 1 );
	}

	if( args[0].equals("-f"))
	{
	    try
	    {
		BufferedReader br = new BufferedReader( new FileReader( args[1] ));
		iorString = br.readLine();
	    } 
	    catch ( IOException ioe )
	    {
		ioe.printStackTrace();
		System.exit(1);
	    }
	}
	else
	{
	    iorString = args[0];
	}

	org.omg.CORBA.Object o = orb.string_to_object( iorString );

	if( o._non_existent())
	{
	    System.out.println("Object not reachable!");
	}
	else
	{
	    System.out.println("Object is alive! ");
	}
	
    }
}






