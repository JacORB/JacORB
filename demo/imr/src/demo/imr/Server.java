package demo.imr;

import java.io.*;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;

import org.jacorb.util.*;

public class Server 
    extends SomeIfPOA
{
    public Server()
    {
    }

    public void op()
    {
        System.out.println("Server: Received call from client");        
    }
        
    public static void main(String[] args) 
    {
        if( args.length != 2 ) 
	{
            System.out.println(
                "Usage: jaco demo.imr.Server <ior_file> <timeout in secs>");
            System.exit( 1 );
        }

        System.setProperty( "jacorb.implname", "imr_demo" );
        System.setProperty( "jacorb.use_imr", "on" );

        try 
        {   
            long timeout = Integer.parseInt( args[1] ) * 1000;
            //init ORB
	    ORB orb = ORB.init( args, null );

            //get root POA
       	    org.omg.PortableServer.POA root_poa = 
		org.omg.PortableServer.POAHelper.narrow(
                    orb.resolve_initial_references("RootPOA"));

            //create necessary policies
	    org.omg.CORBA.Policy[] policies = new org.omg.CORBA.Policy[2];

	    policies[0] = root_poa.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);
	    policies[1] = root_poa.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID);
            
            //create user POA with these policies
	    POA demo_poa = root_poa.create_POA( "ImRDemoServerPOA", 
                                                root_poa.the_POAManager(), 
                                                policies );
            
            //destroy policies
	    for (int i=0; i<policies.length; i++) 
		policies[i].destroy();			

            //instanciate implementation
            Server s = new Server();

            //create object id
            byte[] id = "imr_demo".getBytes();

            //activate object
            demo_poa.activate_object_with_id( id, s );

            //make POA accept requests
	    root_poa.the_POAManager().activate();

            
            // create the object reference
            org.omg.CORBA.Object obj = 
                demo_poa.servant_to_reference( s );

            PrintWriter pw = 
                new PrintWriter( new FileWriter( args[ 0 ] ));

            // print stringified object reference to file
            pw.println( orb.object_to_string( obj ));
            
            pw.flush();
            pw.close();
    
            // wait for requests
	    Thread.sleep( timeout );

            orb.shutdown( true );
        }
        catch( Exception e ) 
        {
            System.out.println( e );
        }
    }
}


