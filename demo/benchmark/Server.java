package demo.benchmark;

import org.omg.CosNaming.*;

public class Server
{
    public static void main( String[] args )
    {
	try
	{
	    org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);
	    org.omg.PortableServer.POA poa = 
		org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));		

	    poa.the_POAManager().activate();

	    org.omg.CORBA.Object o = poa.servant_to_reference(new benchImpl());
		 
	    if( args.length == 0 )
	    {
		NamingContextExt nc = 
		    NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
		nc.bind(nc.to_name("benchmark"), o);

	    }
	    else
	    {
                try
                {
                    String ref = orb.object_to_string( o );
                    String refFile = args[0];
                    java.io.PrintWriter out =
                        new java.io.PrintWriter(new java.io.FileOutputStream(refFile));
                    out.println(ref);
                    out.flush();
                }
                catch(java.io.IOException ex)
                {
                    System.err.println("Server: can't write to `" +
                                       ex.getMessage() + "'");
                    System.exit( 1 );
                }
	    }
	    orb.run();			
	} 
	catch (Exception e )
	{
	    e.printStackTrace();
	}
    }
}




