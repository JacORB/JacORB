package demo.benchmark;

import org.omg.CosNaming.*;

public class OctetServer
{
    public static void main( String[] args )
    {


	try
	{
	    org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);
	    org.omg.PortableServer.POA poa = 
		org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));		

	    poa.the_POAManager().activate();

	    org.omg.CORBA.Object o = poa.servant_to_reference(new octetBenchImpl());
		 
            NamingContextExt nc = 
                NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
            nc.bind(nc.to_name("octet_benchmark"), o);


	    orb.run();			
	} 
	catch (Exception e )
	{
	    e.printStackTrace();
	}
    }
}




