package demo.grid;

import java.io.*;
import org.omg.CosNaming.*;

public class TieServer
{
    public static void main( String[] args )
    {
	org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);
	try
	{
	    org.omg.PortableServer.POA poa = 
		org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

	    org.omg.CORBA.Object o =
                poa.servant_to_reference( new MyServerPOATie(new gridOperationsImpl()) );

	    poa.the_POAManager().activate();

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
		NameComponent [] name = new NameComponent[1];
		name[0] = new NameComponent("grid", "example");
		nc.bind( name, o );
	    }
	} 
	catch ( Exception e )
	{
	    e.printStackTrace();
	}
	orb.run();
    }
}




