
// Copyright (C) 1998-1999
// Object Oriented Concepts, Inc.

// **********************************************************************
//
// Copyright (c) 1997
// Mark Spruiell (mark@intellisoft.com)
//
// See the COPYING file for more information
//
// **********************************************************************

package org.jacorb.trading.client.dynprop;

import java.io.*;
import java.util.*;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;

public class DynPropDemo
{
    public static void main(String[] args)
    {
        if( args.length != 1 )
        {
            usage();
        }

	String iorfile = args[0];

	// initialize the ORB
	ORB orb = org.omg.CORBA.ORB.init(args, null);

        try
        {
            POA poa = 
                POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

            poa.the_POAManager().activate();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

	// create the dynamic property evaluation implementation
	DynamicPropEvalImpl impl = new DynamicPropEvalImpl();
	impl._this_object( orb );
                
	// write the IOR of the object (if necessary)
        try {
            FileOutputStream out = new FileOutputStream(iorfile);
            PrintWriter pw = new PrintWriter(out);
            pw.println(orb.object_to_string(impl._this()));
            pw.flush();
            out.close();
        }
        catch (IOException e) {
            System.err.println("Unable to write IOR to file " + iorfile);
            System.exit(1);
        }
	

	// GB:
	orb.run();

	System.exit(0);
    }


    protected static void usage()
    {
	System.err.println("Usage: org.jacorb.trading.client.dynprop.DynPropDemo <iorfile>");
	System.exit(1);
    }
}




