package demo.hello;

import java.io.*;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;


public class Server 
{
    public static void main(String[] args) 
    {
        if( args.length != 2 ) 
	{
            System.out.println(
                "Usage: java demo.hello.Server <ior_file> <location>");
            System.exit( 1 );
        }

        try 
        {            
            //init ORB
	    ORB orb = ORB.init( args, null );

	    //init POA
	    POA poa = 
                POAHelper.narrow( orb.resolve_initial_references( "RootPOA" ));

	    poa.the_POAManager().activate();

            // create a GoodDay object
            GoodDayImpl goodDayImpl = new GoodDayImpl( args[1] );	
    
            // create the object reference
            org.omg.CORBA.Object obj = 
                poa.servant_to_reference( goodDayImpl );

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
