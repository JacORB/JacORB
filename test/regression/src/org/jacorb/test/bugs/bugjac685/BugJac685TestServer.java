package org.jacorb.test.bugs.bugjac685;


import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class BugJac685TestServer
{
    public static void main (String[] args)
    {
        try
        {
            //init ORB
            ORB orb = ORB.init( args, null );

            //init POA
            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

            // create a Factory object
           SessionFactoryServant sessionFactoryServant =
              new SessionFactoryServant(orb);

            // create the object reference
            org.omg.CORBA.Object sessionFactory =
              rootPOA.servant_to_reference( sessionFactoryServant );

            //register Factory with the naming service
            NamingContextExt nc =
              NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));

            try
            {
                nc.bind_new_context (nc.to_name("ServantScaling"));
            }
            catch (Exception e) {}

            nc.rebind(nc.to_name("ServantScaling/SessionFactory"), sessionFactory);

            // wait for requests
            rootPOA.the_POAManager().activate();

            System.out.println ("BugJac685TestServer starting");
            orb.run();
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
}
