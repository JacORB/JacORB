package test.idl.OBV.Map;

// Server.java

import java.io.*;
import org.omg.PortableServer.*;
import org.omg.CosNaming.*;

public class Server
{

    public static void main(String[] args)
        throws Exception
    {
        // Initialize ORB
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init( args , null );

        // Resolve Root POA
        POA rootPoa = POAHelper.narrow( orb.resolve_initial_references("RootPOA"));
        rootPoa.the_POAManager().activate();

        // Create policies for our persistent POA
        org.omg.CORBA.Policy[] policies = {
            rootPoa.create_lifespan_policy(LifespanPolicyValue.PERSISTENT)
       };

        // Create managerPOA with the right policies
        POA serverPoa =
            rootPoa.create_POA("serverPoa", rootPoa.the_POAManager(), policies );

        PointManagerImpl pointManager =
            new PointManagerImpl("PointManager1", true);

        // Export the newly created object.
        byte [] oid = serverPoa.activate_object(pointManager);
        org.omg.CORBA.Object o = serverPoa.id_to_reference( oid );

        // use the naming service

        NamingContextExt nc =
            NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
        nc.rebind( nc.to_name("PointManager1.example"), o);

        System.out.println("PointManager is ready.");

        // Wait for incoming requests
        orb.run();

    }

}
