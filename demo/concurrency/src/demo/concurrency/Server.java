package demo.concurrency;

import org.jacorb.concurrency.*;
import org.jacorb.transaction.*;
import org.omg.CosNaming.*;

import org.omg.CosConcurrencyControl.*;

public class Server
{
    public static void main (String[] args)
    {
        try
        {
            org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, null);
            org.omg.PortableServer.POA poa = 
                org.omg.PortableServer.POAHelper.narrow (orb.resolve_initial_references ("RootPOA"));

            poa.the_POAManager ().activate ();

            TransactionService.start (poa, 100);

            // SessionService.start(poa, 5);
            // org.omg.CosNaming.NamingContextExt nc =
            // SessionService._get_naming();
            
            NamingContextExt nc = 
                NamingContextExtHelper.narrow (orb.resolve_initial_references ("NameService"));
            NameComponent[] name = new NameComponent[1];
            name[0] = new NameComponent ("LogicLand", "transaction");

            if (nc == null)
            {
                System.out.println ("null");
                System.exit (0);
            }

            nc.bind (name, TransactionService.get_reference ());

            LockSetFactoryImpl lsf = new LockSetFactoryImpl (poa);

            name[0] = new NameComponent ("LogicLand", "lock");
            nc.bind (name, poa.servant_to_reference (lsf));

            TransactionalLockSet ls = lsf.create_transactional ();
            name[0] = new NameComponent ("LogicLand", "lockset");
            nc.bind (name, ls);
            /*
             * Session ss = SessionService.get_reference(); ReferenceServer
             * ref_server = new ReferenceServer(ss, 9000); Thread refsrv_thr =
             * new Thread(ref_server); refsrv_thr.start();
             */
            System.out.println ("Server is ready");
            System.out.println ("Do with client program instructions.");
            System.out.println ("Print result will displayed on this screen.");
            orb.run ();

        }
        catch (Exception e)
        {
            e.printStackTrace ();
            System.exit (0);
        }
    }
}

