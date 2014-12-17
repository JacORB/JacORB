package demo.any;

import java.io.*;
import org.omg.CosNaming.*;

public class Server
{
    public static void main( String[] args ) throws Exception
    {
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);
        
	    org.omg.PortableServer.POA poa = 
		org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

	    poa.the_POAManager().activate();

	    org.omg.CORBA.Object o = poa.servant_to_reference(new AnyServerImpl());

	    if ( args.length == 1 || args.length == 2) 
	    {
	        // write the object reference to args[0]

	        PrintWriter ps = new PrintWriter(new FileOutputStream(new File( args[0] )));
	        ps.println( orb.object_to_string( o ) );
	        ps.close();
	    } 
	    else
	    {
	        // register server with naming context
	        NamingContextExt nc = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
	        nc.bind( nc.to_name("AnyServer.service"), o);
	    }

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


