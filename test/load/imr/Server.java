package test.load.imr;

import java.io.*;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;


public class Server 
{
    public static void main(String[] args) 
    {
        if( args.length != 1 ) 
	{
            System.out.println(
                "Usage: jaco test.load.imr.Server <ior_file>");
            System.exit( 1 );
        }

        try 
        {            
            System.setProperty( "jacorb.implname", "loadtest" );
            System.setProperty( "jacorb.use_imr", "on" );

            //init ORB
	    ORB orb = ORB.init( args, null );

	    //init POA
	    POA root_poa = 
                POAHelper.narrow( orb.resolve_initial_references( "RootPOA" ));

	    root_poa.the_POAManager().activate();
	    org.omg.CORBA.Policy[] policies = new org.omg.CORBA.Policy[2];

	    policies[0] = root_poa.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);
	    policies[1] = root_poa.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID);
	    POA test_poa = root_poa.create_POA( "TestServerPOA", 
                                                root_poa.the_POAManager(), 
                                                policies );
	    for (int i=0; i<policies.length; i++) 
		policies[i].destroy();			

            // create a GoodDay object
            GoodDayImpl goodDayImpl = new GoodDayImpl( "" );	

            byte[] id = "test".getBytes();

            test_poa.activate_object_with_id( id, goodDayImpl );

    
            // create the object reference
            org.omg.CORBA.Object obj = 
                test_poa.servant_to_reference( goodDayImpl );

            PrintWriter pw = 
                new PrintWriter( new FileWriter( args[ 0 ] ));

            // print stringified object reference to file
            pw.println( orb.object_to_string( obj ));
            
            pw.flush();
            pw.close();
    
            // wait for requests
	    orb.run();
        }
        catch( Exception e ) 
        {
            System.out.println( e );
        }
    }
}
