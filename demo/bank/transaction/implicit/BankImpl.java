package demo.bank.transaction.implicit;

import org.omg.CORBA.*;
import org.omg.CORBA.ORBPackage.*;
import org.omg.CosTransactions.*;
import org.omg.CosNaming.*;
import java.io.*;

public class BankImpl 
    extends TheBankPOA
{
    private ORB orb;
    private org.omg.PortableServer.POA poa;
  
    private org.omg.CosTransactions.Current ts_current = null;

    public BankImpl( ORB orb, org.omg.PortableServer.POA poa ) 
    {
        this.orb = orb;
        this.poa = poa;

        try
        {
            ts_current =  org.omg.CosTransactions.CurrentHelper.narrow(
                orb.resolve_initial_references("TransactionCurrent"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public Account open(String name, float initial_deposit)
    {
        try
        {
            AccountImpl acc = new AccountImpl(orb, name, initial_deposit);
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
        throws InsufficientFunds, TransferFailed 
    {
        try 
        {

            // start a new transaction
            System.err.println("begin transaction");

            // obtain control object
            //control = transactionFactory.create(20);
            ts_current.set_timeout(20);
            ts_current.begin();

            source.debit( amount );

            System.err.println("debited");

            destination.credit( amount );

            System.err.println("credited");

            // commit the transaction
            System.err.println("commit transaction");
            //control.get_terminator().commit( true );
            ts_current.commit(true);
            System.err.println("transaction comitted");
        }
        catch( InsufficientFunds isf ) 
        {
            System.err.println("insufficient funds - rollback transaction: " + isf );
            try 
            {
                //control.get_terminator().rollback();
                ts_current.rollback();
            }
            catch(  org.omg.CosTransactions.NoTransaction  nt ) 
            {
                System.err.println("No transaction - give up: " + nt );
                System.exit( 1 );
            }
            throw( isf );
        }

        catch( UserException ue ) 
        {
            System.err.println("user exception - rollback transaction: " + ue );
            try 
            {
                //control.get_terminator().rollback();
                ts_current.rollback();
            }
            catch(  org.omg.CosTransactions.NoTransaction  nt ) 
            {
                System.err.println("No transaction - give up: " + nt );
                System.exit( 1 );
            }
            throw new TransferFailed();	    
        }
        catch( SystemException se ) 
        {
            System.err.println("system exception - rollback transaction: " + se );
            try 
            {
                //control.get_terminator().rollback();
                ts_current.rollback();
            }
            catch(  org.omg.CosTransactions.NoTransaction  nt ) 
            {
                System.err.println("No transaction - give up: " + nt );
                System.exit( 1 );
            }
            throw( se );
        }
    }
}




