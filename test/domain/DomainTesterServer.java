package org.jacorb.orb.domain.test;  
  
import org.jacorb.orb.domain.*;
import org.jacorb.util.Debug;
import java.io.*;
import org.omg.CosNaming.*;

/**
 * The server for the domain test environment. 
 * 
 * @author Herbert Kiefer
 * @version $Revision$
 */
public class DomainTesterServer
{
    final static int n= 10;
  /** if started with any arguments this program registers an instance of a
   *  domain server at the name service, otherwise it assumes the domain
   *  server is already started and registerered at the name service 
   */
    public static void main( String[] args )
    {
       org.omg.CORBA.Policy[] pols= new org.omg.CORBA.Policy[n];
       org.omg.CORBA.Object[] objs= new org.omg.CORBA.Object[n];
       org.omg.CORBA.Object o= null;
       Domain ds= null;
       boolean domainServerAlreadyStarted= true;
       java.util.Properties props = new java.util.Properties();

       if (args.length > 0) 
	 { // Herb: startup of domain server disabled:
	   // two different ways of doing the same thing is bad design
	   // domainServerAlreadyStarted= false;
	   // props.put("jacorb.orb_domain.mount","off");
	 }
    
       org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, props);
       try
	 {
	   org.omg.PortableServer.POA poa = 
	     org.omg.PortableServer.POAHelper.narrow
	     (orb.resolve_initial_references("RootPOA"));
	    	    
	   poa.the_POAManager().activate();

	   ((org.jacorb.orb.ORB) orb).mountORBDomain("orb domain of tester server");

	   TestPolicyPOA servant= null;
	   // create test policies
	   TestPolicy pol[]= new TestPolicy[n];
	   for (int k= 0; k < n; k++) 
	     {
	       pol[k]= TestPolicyHelper.narrow(poa.servant_to_reference(new TestPolicyImpl(k))) ;
	     }

	   if (! domainServerAlreadyStarted) 
	     { // start one
	       DomainFactoryImpl factoryImpl= new DomainFactoryImpl();
	       DomainFactory     factory    = 
		 DomainFactoryHelper.narrow(poa.servant_to_reference(factoryImpl));
	       
	       // policy factory
	       PolicyFactoryImpl polFactoryImpl = new PolicyFactoryImpl();
	       PolicyFactory     polFactory     =
		 PolicyFactoryHelper.narrow(poa.servant_to_reference(polFactoryImpl));

	       // create policies by the help of policy factory
	       org.omg.CORBA.Policy policies[]= new org.omg.CORBA.Policy[1];
	       policies[0]= polFactory.createConflictResolutionPolicy
		 (ConflictResolutionPolicy.PARENT_RULES);

	       // use factory to get a domain service	
	       ds= factory.createDomain(null, policies, "domain server");
	      

	       if (args.length > 0) 
		 { // write ior to file
		   try 
		     {
		       FileOutputStream out = new FileOutputStream(args[0]);
		       PrintWriter pw = new PrintWriter(out);
		       pw.println(orb.object_to_string(ds));
		       pw.flush();
		       out.close();
		       org.jacorb.util.Debug.output(2, " wrote IOR of domain server to file" + args[0]);
		     }
		   catch (IOException e) 
		     {
		       System.err.println
			 ("Unable to write IOR to file " + args[0]);
		       System.exit(1);
		     }
		 }
	       else 
		 {
		   System.out.println("domain service:");
		   System.out.println(ds);
		 }
	     }
	   NamingContextExt nc = 
	     NamingContextExtHelper.narrow(
					   orb.resolve_initial_references
					   ("NameService"));

	   if (! domainServerAlreadyStarted) 
	     {
	       nc.bind(nc.to_name("Domain.service"), ds);
	     }

	   // bind policy objects
	   for (int k =0; k < n; k++)
	     {
	       nc.bind(nc.to_name("TestPolicy"+k+".whatever"), pol[k]);
	     }
	 } 
       catch ( Exception e )
	 {
	   e.printStackTrace();
	 }
       org.jacorb.util.Debug.output(Debug.DOMAIN | Debug.INFORMATION | 2, 
				"domain test server up.");
       orb.run();
    }
}






