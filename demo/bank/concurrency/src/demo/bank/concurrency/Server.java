package demo.bank.concurrency;

import org.omg.CORBA.*;
import org.omg.CORBA.ORBPackage.*;
import org.omg.CosTransactions.*;
import org.omg.CosNaming.*;
import java.io.*;

import org.jacorb.concurrency.*;
import org.jacorb.transaction.*;

public class Server 
{
    public static void main( String[] args ) throws Exception
    {
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);
        
        org.omg.PortableServer.POA poa = 
                org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

	    poa.the_POAManager().activate();
            
        NamingContextExt nc =
            NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));

        NameComponent [] name = new NameComponent[1];

        TransactionService.start( poa, 100 );
        name[0] = new NameComponent( "TransactionService", "service");
        
        nc.bind(name, TransactionService.get_reference());
        
        LockSetFactoryImpl lsf = new LockSetFactoryImpl( poa );
        name[0] = new NameComponent( "ConcurrencyControlService", "service");

        nc.bind(name, poa.servant_to_reference( lsf ));

        org.omg.CORBA.Object o = 
            poa.servant_to_reference( new BankImpl(orb,poa));
        name[0] = new NameComponent( "DigiBank", "server");
        nc.bind(name, o);

        if (args.length == 2)
        {
            File killFile = new File(args[1]);
            while(!killFile.exists())
            {
                Thread.sleep(1000);
            }
            orb.shutdown(true);
        }
        else
        {
            orb.run();
        }
    }
}



