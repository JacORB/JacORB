package demo.poa_monitor.root_poa;

import demo.poa_monitor.foox.*;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.*;
import org.omg.CosNaming.*;
import java.io.*;

public class Server 
{
    public static String description = "Root-POA only";

    public static void main(String[] args) 
    {
        try 
        {			
            ORB        orb     = org.omg.CORBA.ORB.init(args, null);
            POA        rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            POAManager poaMgr  = rootPOA.the_POAManager();

            FooFactoryImpl servant   = new FooFactoryImpl();
            FooFactory     reference = servant._this(orb);

            /*
            PrintWriter pw = new PrintWriter(new FileWriter("../fooFactory.ior"));
            pw.println(orb.object_to_string(reference));
            pw.close(); 
            */
            // CORBA compliant:			
            NamingContextExt nc = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
            NameComponent [] name = new NameComponent[1];
            name[0] = new NameComponent("FooFactory", "service");
            nc.bind(name, reference);

            poaMgr.activate();
            System.out.println("[ Server ready ]");
            orb.run();
									
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
}


