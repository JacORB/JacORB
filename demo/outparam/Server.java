package demo.outparam;

import org.omg.CosNaming.*;

public class Server
{
    public static void main( String[] args )
    {
	org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);
	try
	{
	    org.omg.PortableServer.POA poa = 
		org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
	    poa.the_POAManager().activate();
	    org.omg.PortableServer.Servant servant = new serverImpl();
	    org.omg.CORBA.Object o = poa.servant_to_reference(servant );

	    // register server with naming context
	    NamingContextExt nc = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
	    NameComponent [] name = new NameComponent[]{ new NameComponent( "ParamServer", "service")};
	    nc.bind(name, o);
	} 
	catch ( Exception e )
	{
	    e.printStackTrace();
	}
	orb.run();
    }
}




