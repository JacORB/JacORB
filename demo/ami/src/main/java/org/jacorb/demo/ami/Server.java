package demo.ami;

import java.io.*;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;

public class Server
{
    public static void main(String[] args) throws Exception 
    {
        if( args.length != 1 && args.length != 2) 
        {
            System.out.println(
                "Usage: java demo.ami.Server <ior_file>");
            System.exit( 1 );
        }

        //init ORB
	    ORB orb = ORB.init( args, null );

	    //init POA
	    POA poa = 
                POAHelper.narrow( orb.resolve_initial_references( "RootPOA" ));

	    poa.the_POAManager().activate();

        AsyncServerImpl s = new AsyncServerImpl();
        
        // create the object reference
        org.omg.CORBA.Object obj = poa.servant_to_reference( s );
        
        PrintWriter pw =
            new PrintWriter( new FileWriter( args[ 0 ] ));

        // print stringified object reference to file
        pw.println( orb.object_to_string( obj ));
            
        pw.flush();

        pw.close();
    
        // wait for requests
        if (args.length == 2)
        {
            File killFile = new File(args[1]);
            
            while(!killFile.exists())
            {
                Thread.sleep(1000);
            }
            orb.shutdown(true);
        }
        else
        {
            orb.run();
        }
    }
}
