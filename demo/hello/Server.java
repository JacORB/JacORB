package demo.hello;

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
                "Usage: java demo.hello.Server <location>");
            System.exit( 1 );
        }

        try {

	    /* turn off any output, otherwise it will garble up our
	       IOR file */

	    java.util.Properties props = new java.util.Properties();
	    props.put("jacorb.verbosity","0");

            //init ORB
	    ORB orb = ORB.init( args, props );

	    //init POA
	    POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

	    poa.the_POAManager().activate();

            // create a GoodDay object
            GoodDayImpl goodDayImpl = new GoodDayImpl( args[0] );	
    
            // create the object reference
            org.omg.CORBA.Object obj = poa.servant_to_reference( goodDayImpl );

            // print stringified object reference
            System.out.println( orb.object_to_string( obj ) );
    
            // wait for requests
	    orb.run();
        }
        catch(SystemException e) {
	    e.printStackTrace();
        }
        catch(java.lang.Exception ie) {
	    ie.printStackTrace();
        }
    }
}
