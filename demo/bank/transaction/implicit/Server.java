package demo.bank.transaction.implicit;

import org.omg.CORBA.*;
import org.omg.CORBA.ORBPackage.*;
import org.omg.CosTransactions.*;
import org.omg.CosNaming.*;
import java.io.*;

public class Server
{
    public static void main( String[] args )
    {
        java.util.Properties props = new java.util.Properties();
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.TSClientInit",
                  "org.jacorb.transaction.TransactionInitializer");

        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, props);
        try
        {
            org.omg.PortableServer.POA poa =
                org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

	    poa.the_POAManager().activate();

            org.omg.CORBA.Object o = poa.servant_to_reference(new BankImpl(orb,poa));

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
                name[0] = new NameComponent( "DigiBank", "server");
                nc.rebind(name, o);
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        orb.run();
    }


}
