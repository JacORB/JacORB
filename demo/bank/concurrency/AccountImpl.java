package demo.bank.concurrency;

/**
 * Simple Transaction Service example, code taken and adapted 
 * from http://www.wiley.com/compbooks/vogel/ejb/code.html
 */

import org.omg.CORBA.*;
import org.omg.CosTransactions.*;
import org.omg.CosConcurrencyControl.*;
import org.omg.CosConcurrencyControl.*;

public class AccountImpl
    extends AccountPOA
{
    private float balance;
    private float newBalance;
    private TransactionalLockSet lock_set;
    private String name;
    private int id;
    private Coordinator current_transaction = null;
    private boolean prepared = false;

    public AccountImpl( TransactionalLockSet lock_set, String name, float deposit, int id ) 
    {
	this.name = name;
        this.lock_set = lock_set;
        this.id = id;
	balance = deposit;
        newBalance = balance;
    }

    public synchronized float get_balance( Control control ){
        try {
            Coordinator coord = control.get_coordinator();
            // If my transaction change account or nobody change account
            if( lock_set.try_lock( coord, lock_mode.read ) ) {
                float r = newBalance;
                lock_set.unlock( coord, lock_mode.read );
                return r;
            } else {
                // Anybody change account get_last valid data
                return balance;
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            throw new org.omg.CORBA.INTERNAL();
        }
    }

    public synchronized void debit( float amount, Control control ) 
	throws InsufficientFunds 
    {
	try 
	{
            Coordinator coord = control.get_coordinator();

	    // lock account for write 
	    while( ! lock_set.try_lock( coord, lock_mode.write ) ){
                try {
                    wait();
                } catch ( Exception e ){
                    e.printStackTrace();
                }
            }

	    // check founds
	    if( amount > newBalance ) {
		lock_set.unlock( coord, lock_mode.write );
		System.out.println("Resource: " + name + " account unlocked");
		throw new InsufficientFunds();
	    }
	    newBalance -= amount;
            if( current_transaction == null ){
                current_transaction = coord;
                coord.register_resource( _this() );
            };
	} catch( Unavailable u ) {
	    System.err.println("Account " + name + "::debit: exception: " + u );
	} catch( LockNotHeld h ) {
	    System.err.println("Account " + name + "::debit: exception: " + h );
	} catch( Inactive i ) {
	    System.err.println("Account " + name + "::debit: exception: " + i );
	} catch( SystemException se ) {
	    System.err.println("Account " + name + "::debit: exception: " + se );
	};
    }

    public synchronized void credit( float amount, Control control )
    {
	try 
	{
            Coordinator coord = control.get_coordinator();
	    // lock account for write 
	    while( ! lock_set.try_lock( coord, lock_mode.write ) ){
                try {
                    wait();
                } catch ( Exception e ){
                    e.printStackTrace();
                }
            }

	    newBalance += amount;
            if( current_transaction == null ){
                current_transaction = coord;
                coord.register_resource( _this() );
            };
	}
	catch( Unavailable u ) {
	    System.err.println("Account " + name + "::credit: exception: " + u );
	}
	catch( Inactive i ) {
	    System.err.println("Account " + name + "::credit: exception: " + i );
	}
	catch( SystemException se ) {
	    System.err.println("Account " + name + "::credit: exception: " + se );
	}
    }

    public TransactionalLockSet get_lock_set(){
        return lock_set;
    };

    public java.lang.String owner(){
        return name;
    };

    public int account_code(){
        return id;
    };

    // implement methods of the Resource interface

    public synchronized Vote prepare() 
    {
	if( balance == newBalance )
	    return Vote.VoteReadOnly;
	return Vote.VoteCommit;
    }

    public synchronized void rollback() {
        newBalance = balance;
        current_transaction = null;
        notifyAll();
    }

    public synchronized void commit() {
	balance = newBalance;
        current_transaction = null;
        notifyAll();
    }

    public synchronized void commit_one_phase() 
    {
	if(prepare() == Vote.VoteCommit) {
	    commit();
	}
    }

    public synchronized void forget() 
    {
        System.out.println("Resource " + name + " : forget()");
    }
}
