package demo.bank.transaction.explicit;

import java.io.*;
import org.omg.CosNaming.*;

public class Client 
{
    public static void main( String[] args )
    {
	try
	{
	    TheBank bank;
	    AccountManager acc_mgr;
	    org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args,null);
		
	    NamingContextExt nc = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
	    NameComponent [] name = new NameComponent[1];
	    name[0] = new NameComponent("DigiBank", "server");

            acc_mgr = AccountManagerHelper.narrow( nc.resolve(name));

	    System.out.println("> Opening Accounts Acc1 and Acc 2.");

	    Account a1 = acc_mgr.open("Acc1", (float)199.99);
	    Account a2 = acc_mgr.open("Acc2", (float)50.08);

	    System.out.println("\n--- Balances ---");
	    System.out.println("Acc1 : " + a1.balance() );
	    System.out.println("Acc2 : " + a2.balance() );

	    if( ((org.omg.CORBA.Object)acc_mgr)._is_a(TheBankHelper.id())) 
	    {
		bank = TheBankHelper.narrow(acc_mgr );		
	
		System.out.println("> Transfer 100,- from Acc 1 to Acc 2.");
		bank.transfer(a1, a2, 100);
	
		System.out.println("\n--- Balances ---");
		System.out.println("Acc1 : " + a1.balance() );
		System.out.println("Acc2 : " + a2.balance() );
	    }

	} 
	catch ( Exception e )
	{
	    e.printStackTrace();
	}
    }  
}


