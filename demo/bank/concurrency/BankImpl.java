package demo.bank.concurrency;

import org.omg.CORBA.*;
import org.omg.CORBA.ORBPackage.*;
import org.omg.CosTransactions.*;
import org.omg.CosConcurrencyControl.*;
import org.omg.CosNaming.*;
import java.io.*;

public class BankImpl 
    extends TheBankPOA
{
    private ORB orb;
    private org.omg.PortableServer.POA poa;
    private TransactionFactory transactionFactory;
    private LockSetFactory lockSetFactory;

    public BankImpl( ORB orb, org.omg.PortableServer.POA poa ) 
    {
	this.orb = orb;
	this.poa = poa;
        try 
        {
            // obtain transaction factory object from naming service
            NamingContextExt nc = 
                NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
            NameComponent [] name = new NameComponent[1];
            name[0] = 
                new NameComponent( "TransactionService", "service");
            transactionFactory = 
                TransactionFactoryHelper.narrow( nc.resolve(name));

            // obtain LockSet factory object from naming service
            name[0] = 
                new NameComponent( "ConcurrencyControlService", 
                                   "service");
            lockSetFactory = 
                LockSetFactoryHelper.narrow( nc.resolve(name) );
        } 
        catch( Exception n ) 
        {
            n.printStackTrace();
            System.exit(1);
        } 
    }

    public Account open(String name, float initial_deposit)
    {
	try
	{
            TransactionalLockSet lock_set = 
                lockSetFactory.create_transactional();
	    AccountImpl acc = 
                  new AccountImpl( lock_set, name, 
                        initial_deposit, 0);
	    org.omg.CORBA.Object o = poa.servant_to_reference(acc);
	    return acc._this(orb);
	}
	catch( Exception e )
	{
            e.printStackTrace();
	    throw new org.omg.CORBA.UNKNOWN();
	}
    }

    public void transfer( Account source, Account destination, float amount )
	throws InsufficientFunds 
    {
        Control control = null;
	try 
	{
	    // start a new transaction
	    System.err.println("begin transaction");

	    // obtain control object
	    control = transactionFactory.create(20);	        
            Coordinator coord = control.get_coordinator();

            // Lock in upgrade mode for prevent dead lock
            TransactionalLockSet lock_set = source.get_lock_set();
            lock_set.lock( coord, lock_mode.upgrade );
            lock_set = destination.get_lock_set();
            lock_set.lock( coord, lock_mode.upgrade );

	    source.debit( amount, control );

	    System.err.println("debited");

	    destination.credit( amount, control );

	    System.err.println("credited");

	    // commit the transaction
	    System.err.println("commit transaction");
	    control.get_terminator().commit( true );
	    System.err.println("transaction comitted");
	}
	catch( InsufficientFunds isf ) 
	{
	    try 
	    {
		control.get_terminator().rollback();
	    }
	    catch(  org.omg.CosTransactions.Unavailable  nt ) 
	    {
		System.err.println("No transaction - give up: " + nt );
		System.exit( 1 );
	    }
	    throw( isf );
	}
	catch( UserException ue ) 
	{
	    System.err.println("transactional failure - give up: " + ue );
	    System.exit( 1 );
	}
	catch( SystemException se ) 
	{
	    System.err.println("system exception - rollback transaction: " + se );
	    try 
	    {
		control.get_terminator().rollback();
	    }
	    catch(  org.omg.CosTransactions.Unavailable  nt ) 
	    {
		System.err.println("No transaction - give up: " + nt );
		System.exit( 1 );
	    }
	    throw( se );
	}
    }


}



