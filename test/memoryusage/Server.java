package test.memoryusage;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.CosNaming.*;

public class Server
{
    public static void main(String[] args)
    {
        try
        {
            //init ORB
            ORB orb = ORB.init( args, null );
	
            //init POA
            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();
            POAManager poaManager = rootPOA.the_POAManager();
	
            // create a IDiacosFactory object
            SessionFactoryServant sessionFactoryServant = 
              new SessionFactoryServant(orb, rootPOA);
	
            // create the object reference
            org.omg.CORBA.Object sessionFactory = 
              rootPOA.servant_to_reference( sessionFactoryServant );
	
            //register IDiacosFactory with the naming service
            NamingContextExt nc =
              NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));

            nc.bind(nc.to_name("SessionFactory"), sessionFactory);
	
            // wait for requests
            System.out.println("CORBA Server ready");
            orb.run();
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
}
