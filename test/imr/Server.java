package test.imr;

import java.io.*;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;

import org.jacorb.util.*;

public class Server 
    extends TestIfPOA
{
    public Server()
    {
    }

    public void op()
    {
        //todo: implement
    }
        
    public static void main(String[] args) 
    {
        if( args.length != 2 ) 
	{
            System.out.println(
                "Usage: jaco test.imr.Server <ior_file> <timeout in secs>");
            System.exit( 1 );
        }

        System.setProperty( "jacorb.implname", "imr_test" );
        System.setProperty( "jacorb.use_imr", "on" );

        try 
        {   
            long timeout = Integer.parseInt( args[1] ) * 1000;
            //init ORB
	    ORB orb = ORB.init( args, null );

       	    org.omg.PortableServer.POA root_poa = 
		org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

	    root_poa.the_POAManager().activate();

	    org.omg.CORBA.Policy[] policies = new org.omg.CORBA.Policy[2];

	    policies[0] = root_poa.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);
	    policies[1] = root_poa.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID);
	    POA test_poa = root_poa.create_POA( "ImRTestServerPOA", 
                                                root_poa.the_POAManager(), 
                                                policies );
	    for (int i=0; i<policies.length; i++) 
		policies[i].destroy();			


            Server s = new Server();

            byte[] id = "imr_test".getBytes();

            test_poa.activate_object_with_id( id, s );
            
            // create the object reference
            org.omg.CORBA.Object obj = 
                test_poa.servant_to_reference( s );

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


