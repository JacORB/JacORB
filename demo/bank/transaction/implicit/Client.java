package demo.bank.transaction.implicit;

import java.io.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

public class Client 
{

    public static Account open(String name, 
                               float initial_deposit,
                               POA poa, 
                               ORB orb, boolean nasty)
    {
        try
        {
            AccountImpl acc = 
                new AccountImpl(orb, name, initial_deposit, nasty);

            org.omg.CORBA.Object o = poa.servant_to_reference(acc);
            return acc._this(orb);
        }
        catch( Exception e )
        {
            e.printStackTrace();
            throw new org.omg.CORBA.UNKNOWN();
        }
    }

    public static void main( String[] args )
    {
        try
        {
            TheBank bank;
            AccountManager acc_mgr;
            java.util.Properties props = 
                new java.util.Properties();
            props.put("org.omg.PortableInterceptor.ORBInitializerClass.TSServerInit",
                               "org.jacorb.transaction.TransactionInitializer");

            ORB orb = ORB.init(args,props);
		
            NamingContextExt nc = NamingContextExtHelper.narrow
                (orb.resolve_initial_references("NameService"));
            NameComponent [] name = 
                new NameComponent[1];
            name[0] = 
                new NameComponent("DigiBank", "server");

            acc_mgr = AccountManagerHelper.narrow( nc.resolve(name));
	    
            POA poa = (POA) orb.resolve_initial_references("RootPOA");
            poa.the_POAManager().activate();

            System.out.println("> Opening Accounts Acc1 and Acc 2.");

            Account a1 = open("Acc1", (float)199.99, poa, orb, true);
            Account a2 = open("Acc2", (float)50.08, poa, orb, false);

            System.out.println("\n--- Balances ---");
            System.out.println("Acc1 : " + a1.balance() );
            System.out.println("Acc2 : " + a2.balance() );

            bank = TheBankHelper.narrow(acc_mgr );		
		
            boolean ok = false;
            do{
                System.out.println("> Transfer 100,- from Acc 1 to Acc 2.");
                try{
                    bank.transfer(a1, a2, 100);
                    ok = true;
                }catch (Throwable e){
                    System.out.println("Got: " + e);
                }
            }while (! ok);
	
            System.out.println("\n--- Balances ---");
            System.out.println("Acc1 : " + a1.balance() );
            System.out.println("Acc2 : " + a2.balance() );
	
            do
            {
                System.out.println("> Transfer 50,- from Acc 2 to Acc 1.");
                try
                {
                    bank.transfer(a2, a1, 50);
                    ok = true;
                }
                catch (Throwable e){
                    System.out.println("Got: " + e);
                }
            }
            while (! ok);

            System.out.println("\n--- Balances ---");
            System.out.println("Acc1 : " + a1.balance() );
            System.out.println("Acc2 : " + a2.balance() );
        } 
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }  
}
