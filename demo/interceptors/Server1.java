package demo.interceptors;

import java.io.*;
import org.omg.CosNaming.*;

public class Server1
{
    public static void main( String[] args )
    {
	org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);
	try
	{
       	    org.omg.PortableServer.POA poa = 
		org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

	    poa.the_POAManager().activate();
	    
	    org.omg.CORBA.Object o = poa.servant_to_reference(new gridImpl());

	    if( args.length == 1 ) 
	    {
	       	// write the object reference to args[0]

		PrintWriter ps = new PrintWriter(new FileOutputStream(new File( args[0] )));
		ps.println( orb.object_to_string( o ) );
		ps.close();
	    } 
	    else
	    {
		NamingContextExt nc = 
                    NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
		nc.bind( nc.to_name("grid1.example"), o);
	    }
	} 
	catch ( Exception e )
	{
	    e.printStackTrace();
	}
	orb.run();
    }
}


