package test.servantscaling;

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
            // create a IDiacosFactory object
            SessionFactoryServant sessionFactoryServant =
              new SessionFactoryServant(orb);

            // create the object reference
            org.omg.CORBA.Object sessionFactory =
              rootPOA.servant_to_reference( sessionFactoryServant );

            //register IDiacosFactory with the naming service
            NamingContextExt nc =
              NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
            try {
                nc.bind_new_context (nc.to_name("ServantScaling"));
            } catch (Exception e) {}
            nc.rebind(nc.to_name("ServantScaling/SessionFactory"), sessionFactory);

            // wait for requests
            System.out.println("CORBA Server ready");
            rootPOA.the_POAManager().activate();
            orb.run();
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
}
