package demo.domain.printer;

import java.io.*;
import org.omg.CosNaming.*;
import jacorb.orb.domain.*;

public class Server
{
    public static void main( String[] args )
    {

      java.util.Properties props = new java.util.Properties();
      props.put("jacorb.orb_domain.mount","off"); // use only local domain service
      props.put("jacorb.orb_domain.filename","printerDemo_ORBDomain"); // handle

	org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, props);
	try
	{
       	    org.omg.PortableServer.POA poa = 
		org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
	   

	    poa.the_POAManager().activate();
	    
	    org.omg.CORBA.Object p = poa.servant_to_reference(new PrinterImpl());
	    org.omg.CORBA.Object s = poa.servant_to_reference(new SpoolerImpl());


	    // domain stuff
	    Domain domain= DomainHelper.narrow( orb.resolve_initial_references
						("LocalDomainService"));
	    domain.insertMemberWithName("printer object", p);
	    
	    // create and insert domain policy
	    PropertyPolicy pricePolicy= domain.createPropertyPolicy();
	    pricePolicy.name("Price for printing");
	    pricePolicy.setPolicyType(300); // proprietary
	    pricePolicy.put("Page", "3200");
	    pricePolicy.put("Line", "80");
	    pricePolicy.put("Byte", "1");

	    domain.set_domain_policy(pricePolicy);

	    if( args.length == 1 ) 
	    {
	       	// write the object reference to args[0]

		PrintWriter ps = new PrintWriter(new FileOutputStream(new File( args[0] )));
		ps.println( orb.object_to_string( p ) );
		ps.close();
	    } 
	    else
	    {
		NamingContextExt nc = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
		nc.bind( nc.to_name("printer.example"), p);
		nc.bind( nc.to_name("spooler.example"), s);
	    }
	} 
	catch ( Exception e )
	{
	    e.printStackTrace();
	}
	orb.run();
    if (orb.get_service_information(org.omg.CORBA.Security.value,
                                    new org.omg.CORBA.ServiceInformationHolder()))
        System.out.println ( "ssl sec trans support with sec services" );
    }
}


