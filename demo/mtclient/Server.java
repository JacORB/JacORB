package demo.mtclient;

//
// Server for multi-threaded client
//

import org.omg.CosNaming.*;
import org.omg.PortableServer.*;

public class Server
{
    public static void main( String[] args )
    {
	org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);
	try
	{
	    POA poa = 
		POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
	
	    poa.the_POAManager().activate();

	    org.omg.CORBA.Object o = poa.servant_to_reference(new serverImpl());
	    // register server with naming context

	    NamingContextExt nc = 
		NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));

	    nc.bind(nc.to_name("Thread.example"), o);

	}
	catch ( Exception e )
	{
	    e.printStackTrace();
	}
	orb.run();
    }
}


