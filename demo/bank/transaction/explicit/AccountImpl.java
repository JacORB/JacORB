package demo.bank.transaction.explicit;

/**
 * Simple Transaction Service example, code taken and adapted 
 * from http://www.wiley.com/compbooks/vogel/ejb/code.html
 */

import org.omg.CORBA.*;
import org.omg.CosTransactions.*;

public class AccountImpl
    extends AccountPOA
{
    private float balance;
    private float newBalance;
    private float amount;
    private boolean credit;
    private ORB orb;
    private String name;
    private Lock lock;

    public AccountImpl( ORB orb, String name, float deposit ) 
    {
	this.name = name;
	this.orb = orb;

	balance = deposit;
	lock = new Lock();
    }

    public float balance()
    {
	return balance;
    }

    public synchronized void credit( float amount, Control control ) 
    {
	try 
	{
	    // lock account
	    lock.lock();

	    // memorize current activitity
	    this.amount = amount;
	    credit = true;

	    System.err.println("Account " + name + "::credit: get coordinator");
	    Coordinator coordinator = control.get_coordinator();

	    // register resource
	    System.out.println("Account " + name +
			       "::credit: register resource (Account) with ITS");
	    RecoveryCoordinator recCoordinator =
		coordinator.register_resource( _this() );

	    newBalance = balance + amount;
	    System.out.println(" credit $" + amount );
	    System.out.println(" new balance is $" + newBalance );
	}
	catch( Exception ex ) 
	{
	    System.err.println("Account " + name + "::credit: exception: " + ex );
	};
    }

    public synchronized void debit( float amount, Control control )
	throws InsufficientFunds 
    {
	try 
	{

	    // lock account
	    lock.lock();

	    // memorize current activitity
	    this.amount = amount;
	    credit = false;

	    // obtain current object
	    System.err.println("Accont::debit: resolve transaction current");
	
	    System.err.println("Account " + name + "::debit: get coordinator");
	    Coordinator coordinator = control.get_coordinator();

	    // register resource
	    System.out.println("Account " + name + 
			       "::debit: register resource (Account) with ITS");
	    RecoveryCoordinator recCoordinator =
		coordinator.register_resource( _this() );
	    System.out.println("Account " + name + "::debit: resource registered");

	    if( amount > balance ) 
	    {
		System.out.println("no sufficient funds");
		lock.unlock();
		System.out.println("Resource: " + name + " account unlocked");
		throw new InsufficientFunds();
	    }
	    newBalance = balance - amount;
	    System.out.println(" debit $" + amount );
	    System.out.println(" new balance is $" + newBalance );

	}
	catch( Unavailable u ) {
	    System.err.println("Account " + name + "::debit: exception: " + u );
	}
	catch( Inactive i ) {
	    System.err.println("Account " + name + "::debit: exception: " + i );
	}
	catch( SystemException se ) {
	    System.err.println("Account " + name + "::debit: exception: " + se );
	}
    }

    // implement methods of the Resource interface

    public Vote prepare() 
    {
	System.out.println("Resource " + name + " : prepare()");
	if( balance == newBalance )
	    return Vote.VoteReadOnly;
	return Vote.VoteCommit;
    }

    public void rollback() 
    {
	// remove data from temporary storage
	System.out.println("Resource " + name + " : rollback()");
	System.out.println("Resource " + name +
			   " : original balance restored: $" + balance);
	lock.unlock();
	System.out.println("Resource " + name + " account unlocked");
    }

    public void commit() 
    {
	// move data to final storage
	System.out.println("Resource " + name + " : commit()");
	balance = newBalance;
	lock.unlock();
	System.out.println("Resource " + name + " account unlocked");
    }

    public void commit_one_phase() 
    {
	// store data immediately at final destination
	System.out.println("Resource " + name + " : commit_one_phase()");
	if(prepare() == Vote.VoteCommit) 
	{
	    commit();
	}
    }

    public void forget() 
    {
        System.out.println("Resource " + name + " : forget()");
    }
}
