package org.jacorb.naming;

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

import java.net.*;
import java.io.*;
import org.omg.PortableServer.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.jacorb.orb.*;
import org.jacorb.imr.util.ImRManager;
import org.jacorb.util.*;

/**
 *	The name server application
 * 
 *	@author Gerald Brose, FU Berlin
 *	@version $Id$
 */


public class NameServer 
{
    private static org.omg.CORBA.ORB orb = null;
    public static String name_delimiter = "/";

    private static String filePrefix = "_nsdb";

    private static void usage()
    {
	System.err.println("Usage: java org.jacorb.naming.NameServer <ior_filename> [ <time_out> [imr_register] ]");
	System.exit(1);
    }

    /**
     * The servant manager (servant activator) for the name server POA
     */


    static class NameServantActivatorImpl 
        extends ServantActivatorPOA 
    {
	private org.omg.CORBA.ORB orb = null;
 
	public NameServantActivatorImpl(org.omg.CORBA.ORB orb) 
	{      
	    this.orb = orb;
	}

	/**
	 * @returns - a servant initialized from a file
	 */

	public Servant incarnate(byte[] oid, POA adapter) 
	    throws ForwardRequest 
	{
	    String oidStr = new String(oid);

	    NamingContextImpl n = null;		
	    try
	    {
		File f = new File(filePrefix + oidStr );
		if( f.exists() )
		{
		    org.jacorb.util.Debug.output(2,"Reading in  context state from file");
		    FileInputStream f_in = new FileInputStream(f);
		    
		    if( f_in.available() > 0 )
		    {
			ObjectInputStream in = new ObjectInputStream(f_in);
			n = (NamingContextImpl)in.readObject();
			in.close();
		    }
		    f_in.close();
		}
		else
		    org.jacorb.util.Debug.output(2,"No naming context state, starting empty");

	    }
	    catch( IOException io )
	    {
		org.jacorb.util.Debug.output(2,"File seems corrupt, starting empty");
	    }
	    catch( java.lang.ClassNotFoundException c )
	    {
		System.err.println("Could not read object from file, class not found!");
		System.exit(1);
	    }
	    if( n == null )
	    {
		n = new NamingContextImpl();
	    }

	    n.init( orb, adapter);
	    return n;
	}

	/** 
	 * Saves the servant's  state in a file
	 */

	public void etherealize(byte[] oid, POA adapter, 
                                Servant servant, 
				boolean cleanup_in_progress, boolean remaining_activations) 
	{
	    String oidStr = new String(oid);

	    try
	    {
		File f = new File(filePrefix + oidStr);
                FileOutputStream fout = new FileOutputStream(f);
                
		ObjectOutputStream out = 
		    new ObjectOutputStream(fout);

		/* save state */
                out.writeObject((NamingContextImpl)servant);
		org.jacorb.util.Debug.output(2,"Saved state for servant " + oidStr);
	    }
	    catch( IOException io )
	    {
		io.printStackTrace();
		System.err.println("Error opening output file " + filePrefix + oidStr );
		//		System.exit(1);
	    }
  	}
    }


    /** Main */

    public static void main( String args[] )  
    {
	try
	{
	    /* get time out value if any */
	    int time_out = 0;
	    if( args.length < 1)
	    {
		usage();
	    }
	    
	    if( args.length >= 2 )
	    {
		try
		{
		    time_out = Integer.parseInt( args[1] );
		} 
		catch( NumberFormatException nf )
		{
		    usage();
		}
	    }

	    java.util.Properties props = new java.util.Properties();
	    props.put("jacorb.implname","StandardNS");

	    // because domain server starts after ns the 
            // orb domain of the ns can't be inserted
	    props.put("jacorb.orb_domain.mount","off");

	    /* 
             * set a connection time out : after 30 secs. idle time,
             * the adapter will close connections 
             */
            props.put( "jacorb.connection.server_timeout", "10000" );


	    /* which directory to store/load in? */

	    String directory = org.jacorb.util.Environment.getProperty("jacorb.naming.db_dir");

	    if( directory != null )
		filePrefix = directory + File.separatorChar + filePrefix;

	    /* intialize the ORB and Root POA */

	    orb = org.omg.CORBA.ORB.init(args, props);

	    if (org.jacorb.util.Environment.useImR() && (args.length == 3) &&
		args[2].equals("imr_register"))
            {
	      
                // don't supply "imr_register", so a ns started by an imr_ssd
                // won't try to register himself again.
                String command = Environment.getProperty("jacorb.java_exec") +
                    " org.jacorb.naming.NameServer " + args[0] + " " + args[1];
	      
                ImRManager.autoRegisterServer(orb, "StandardNS", command,
                                              ImRManager.getLocalHostName(),
                                              true); //edit existing
	    }

	    org.omg.PortableServer.POA rootPOA = 
		org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

	    /* create a user defined poa for the naming contexts */

	    org.omg.CORBA.Policy [] policies = new org.omg.CORBA.Policy[3];

	    policies[0] = rootPOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID);
	    policies[1] = rootPOA.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);
	    policies[2] = rootPOA.create_request_processing_policy(
				       RequestProcessingPolicyValue.USE_SERVANT_MANAGER);

	    POA nsPOA = rootPOA.create_POA("NameServer-POA", 
                                           rootPOA.the_POAManager(), 
                                           policies);

	    NameServer.NameServantActivatorImpl servantActivator = 
		new NameServer.NameServantActivatorImpl( orb );

	    nsPOA.set_servant_manager( servantActivator._this(orb) );
	    nsPOA.the_POAManager().activate();

	    for (int i=0; i<policies.length; i++) 
		policies[i].destroy();			


	    /* export the root context's reference to a file */

	    byte[] oid = ( new String("_root").getBytes() );
	    try
	    {
		org.omg.CORBA.Object obj = 
		    nsPOA.create_reference_with_id(oid, "IDL:omg.org/CosNaming/NamingContextExt:1.0");
						
		PrintWriter out = new PrintWriter( new FileOutputStream( args[0] ), true);

		out.println( orb.object_to_string(obj) );
		out.close();
	    }
	    catch ( Exception e )
	    {	    
		e.printStackTrace();
		throw new RuntimeException(e.getMessage());
	    }

	    org.jacorb.util.Debug.output(2,"NS up");
 
	    /* either block indefinitely or time out */
	    
	    if( time_out == 0 )
		orb.run();
	    else
		Thread.sleep(time_out);


	    /* shutdown. This will etherealize all servants, thus saving their state */
	    orb.shutdown(true);
            //	    System.exit(0);
	} 
	catch( Exception e )
	{
	    e.printStackTrace();
	    System.exit(1);
	} 
    }



}



















