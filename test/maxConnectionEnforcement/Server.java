package test.maxConnectionEnforcement;

import java.io.*;
import java.util.*;

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
        System.out.println("op called");
    }
        
    public static void main(String[] args) 
    {
        if( args.length != 2 ) 
	{
            System.out.println(
                "Usage: jaco test.maxConnectionEnforcement.Server <ior_file> <max transports>");
            System.exit( 1 );
        }

        try 
        {         
            Properties props = new Properties();
            props.put( "jacorb.connection.max_server_transports",
                       args[1] );
            props.put( "jacorb.connection.selection_strategy_class",
                       "org.jacorb.orb.connection.LRUSelectionStrategyImpl" );
            props.put( "jacorb.connection.statistics_provider_class",
                       "org.jacorb.orb.connection.LRUStatisticsProviderImpl" );

            //init ORB
	    ORB orb = ORB.init( args, props );

	    //init POA
	    POA poa = 
                POAHelper.narrow( orb.resolve_initial_references( "RootPOA" ));

	    poa.the_POAManager().activate();

            Server s = new Server();
            
            // create the object reference
            org.omg.CORBA.Object obj = 
                poa.servant_to_reference( s );

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


