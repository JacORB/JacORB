package demo.bank.transaction.implicit;

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

    private boolean nasty = false;
    private int nasty_count = 0;

    public AccountImpl( ORB orb, String name, float deposit ){
        this(orb, name, deposit, false);
    }

    public AccountImpl( ORB orb, String name, float deposit, boolean nasty ) 
    {
        this.name = name;
        this.orb = orb;
        this.nasty = nasty;

        balance = deposit;
        lock = new Lock();
    }

    public float balance()
    {
        return balance;
    }

    public synchronized void credit( float amount ) 
    {
        try 
        {
            if (nasty){
                switch (nasty_count++){
                case 0 : 
                    break;
                case 1 :
                    System.out.println("Step 1 nastyness: error");
                    throw new Error("Bank Holidays");
                case 2:
                    System.out.println("Step 2 nastyness: COMM_FALIURE");
                    throw new org.omg.CORBA.COMM_FAILURE();
                }
            }

            // lock account
            lock.lock();

            Control control = 
                org.omg.CosTransactions.CurrentHelper.narrow(
		orb.resolve_initial_references("TransactionCurrent")).
		get_control();

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
	    throw new org.omg.CORBA.TRANSACTION_ROLLEDBACK();
        };
    }

    public synchronized void debit( float amount )
        throws InsufficientFunds 
    {
        try 
        {

            if (nasty){
                switch (nasty_count++){
                case 0 : 
                    System.out.println("Step 0 nastyness: InsufficientFunds");
                    throw new InsufficientFunds();
                case 1 :
                    System.out.println("Step 1 nastyness: error");
                    throw new Error("Bank Holidays");
                case 2:
                    System.out.println("Step 2 nastyness: COMM_FALIURE");
                    throw new org.omg.CORBA.COMM_FAILURE();
                }
            }
            // lock account
            lock.lock();

            Control control = 
                org.omg.CosTransactions.CurrentHelper.narrow(
		orb.resolve_initial_references("TransactionCurrent")).
		get_control();

            // memorize current activitity
            this.amount = amount;
            credit = false;
	
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
        catch( org.omg.CORBA.ORBPackage.InvalidName in ) {
            System.err.println("Account " + name + "::debit: exception: " +in);
	    throw new org.omg.CORBA.TRANSACTION_ROLLEDBACK();
        }
        catch( Unavailable u ) {
            System.err.println("Account " + name + "::debit: exception: " + u );
	    throw new org.omg.CORBA.TRANSACTION_ROLLEDBACK();
        }
        catch( Inactive i ) {
            System.err.println("Account " + name + "::debit: exception: " + i );
	    throw new org.omg.CORBA.TRANSACTION_ROLLEDBACK();
        }
        catch( SystemException se ) {
            System.err.println("Account " + name + "::debit: exception: " + se );
	    throw new org.omg.CORBA.TRANSACTION_ROLLEDBACK();
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


