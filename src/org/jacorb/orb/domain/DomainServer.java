
package org.jacorb.orb.domain;  

import java.io.*;  
import org.omg.CosNaming.*;
import org.jacorb.orb.domain.*;
import org.jacorb.util.Debug;

/**
 * This class implements the (global) domain server. The 
 * supplied parameter indicates the URL where to store 
 * the IOR of the domain server. The domain server consists
 * of a (global) domain with an empty member set and the 
 * "ParentRules" conflict resolution policy.
 *
 * @author Herbert Kiefer
 * @version $Revision$
 * @see org.jacorb.orb.domain.ParentRulesPolicy
 */

public class DomainServer
{
    private static void usage()
    {
	System.err.println
            ("Usage: java org.jacorb.orb.domain.DomainServer <ior_filename>");
	System.exit(1);
    }

    public static void main( String[] args )
    {
        if (args.length < 1 ) 
            usage(); 

        Domain domain;
        org.omg.CORBA.Object o;

        String ior_filename = args[0];
        org.jacorb.util.Debug.output( Debug.DOMAIN | 2, 
                                  "starting domain server ... ");

        java.util.Properties props = new java.util.Properties();
        props.put("jacorb.orb_domain.mount", "off");
        props.put("jacorb.use_domain", "on");

        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init( args, props);

        try
        {
             org.omg.PortableServer.POA poa = 
                org.omg.PortableServer.POAHelper.narrow
                (orb.resolve_initial_references("RootPOA"));
	    
            poa.the_POAManager().activate();
	
            // domain factory
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
            domain = factory.createDomain(null, policies, "domain server");

            Debug.output(Debug.DOMAIN | Debug.INFORMATION, 
                         "writing IOR of domain service to file "
                         + ior_filename);
            try 
            {

                FileOutputStream out = new FileOutputStream(args[0]);
                PrintWriter pw = new PrintWriter(out);
                pw.println(orb.object_to_string(domain));
                pw.flush();
                out.close();
            }
            catch (IOException e) 
            {
                System.err.println("Unable to write IOR to file " + args[0]);
                System.exit(1);
            }
	
            try 
            {
                NamingContextExt nc = 
                    NamingContextExtHelper.narrow(
                          orb.resolve_initial_references("NameService"));

                nc.bind(nc.to_name("Domain.service"), domain);
            }
            catch (org.omg.CORBA.COMM_FAILURE failure)
            {
                Debug.output(Debug.DOMAIN | 1,
                             " name server not available, "
                             +" don't register at name server, but continue.");
            }

            // insert orb domain of domain server as child into domain server
            Domain orb_domain = 
                DomainHelper.narrow( 
                      orb.resolve_initial_references("LocalDomainService"));

            orb_domain.name("domain server orb domain");

            try  
            { 
                domain.insertChild(orb_domain); 
            }
            catch(jacorb.orb.domain.GraphNodePackage.ClosesCycle cc) 
            {} // never happens

        } 
        catch ( Exception e )
        {
            Debug.output(1, e);
        }
        org.jacorb.util.Debug.output(Debug.DOMAIN | 2, "domain server up.");
        orb.run();
    }
} // DomainServer


