/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */
package org.jacorb.transaction;

import java.util.Hashtable;
import org.slf4j.Logger;
import org.jacorb.orb.ORB;
import org.omg.CORBA.Any;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.ControlHelper;
import org.omg.CosTransactions.Current;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.InvalidControl;
import org.omg.CosTransactions.NoTransaction;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.PropagationContextHelper;
import org.omg.CosTransactions.Status;
import org.omg.CosTransactions.SubtransactionsUnavailable;
import org.omg.CosTransactions.TransIdentity;
import org.omg.CosTransactions.TransactionFactory;
import org.omg.CosTransactions.TransactionFactoryHelper;
import org.omg.CosTransactions.otid_t;

/**
 * This class represents the transaction current.
 * It is a very simple implementation wich mostly
 * maps to the methods in the control. 
 *
 * @author Nicolas Noffke
 * @author Vladimir Mencl
 * @version $Id$
 */

public class TransactionCurrentImpl 
    extends org.omg.CORBA.LocalObject 
    implements Current
{
    private static final int DEFAULT_TIMEOUT = 30;
  
    private Hashtable contexts = null;
    private Hashtable timeouts = null;
    private ORB orb = null;
    private static int slot_id = -1; /* used from static getControl */

    /** there will only be ony logger instance for all TX services in a process...*/
    private static Logger logger;

    private TransactionFactory factory = null;
  
    public TransactionCurrentImpl(ORB orb, int slot_id) 
    {
        this.orb = orb;
        this.slot_id = slot_id;
        logger =    
            orb.getConfiguration().getLogger("jacorb.tx_service.current");

        contexts = new Hashtable();
        timeouts = new Hashtable();

        try
        {
            NamingContextExt nc = 
                NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
            NameComponent [] name = new NameComponent[1];
            name[0] = new NameComponent( "TransactionService", "service");
            factory = TransactionFactoryHelper.narrow(nc.resolve(name));    
        }
        catch (Exception e)
        {
            if (logger.isErrorEnabled())
                logger.error("Unable to obtain Transaction Service reference. Giving up.", e );
            throw new Error(e.getMessage());
        }
    }

    /**
     * Creates a non-functional current.
     */
    public TransactionCurrentImpl()
    {
        contexts = new Hashtable();
        timeouts = new Hashtable();
    }

    /**
     * This method is a convenience method for the server
     * programmer the exctract the Control from the
     * PICurrent.
     */
    public static Control getControl(org.omg.CORBA.ORB orb)
    {
        try
        {
            org.omg.PortableInterceptor.Current pi_current =
                (org.omg.PortableInterceptor.Current)orb.resolve_initial_references("PICurrent");

            PropagationContext context = PropagationContextHelper.extract
                (pi_current.get_slot(slot_id));

            return ControlHelper.extract(context.implementation_specific_data);
        }
        catch(Exception e)
        {
            if (logger.isDebugEnabled())
                logger.debug("Unable to obtain Transaction Service reference. Giving up.", e );
        }

        return null;
    }

    // implementation of org.omg.CosTransactions.CurrentOperations interface
    /**
     * Start a new transaction. The propagation context will be transfered
     * on ALL communication (~1k extra data) from now on, until
     * the transaction is committed or rolled back. <br>
     * NOTICE: the PropagationContext might not be set up fully
     * compliant to the Spec.
     */
    public void begin() 
        throws SubtransactionsUnavailable 
    {
        Thread thread = Thread.currentThread();

        if (contexts.containsKey(thread))
            throw new SubtransactionsUnavailable();

        int timeout = (timeouts.containsKey(thread))? 
            ((Integer) timeouts.get(thread)).intValue() : DEFAULT_TIMEOUT;

        Control control = factory.create(timeout);
        contexts.put(thread, control);
    
        try
        {
            org.omg.PortableInterceptor.Current pi_current =
                (org.omg.PortableInterceptor.Current) orb.resolve_initial_references("PICurrent");

            // the info inserted here is actually never needed and mostly a waste of
            // space/bandwidth, since the control itself is transfered also.
            TransIdentity id = new TransIdentity(control.get_coordinator(), 
                                                 control.get_terminator(),
                                                 new otid_t(0, 0, new byte[0]));
      
            Any control_any = orb.create_any();
            ControlHelper.insert(control_any, control);

            PropagationContext context = new PropagationContext(timeout,
                                                                id, new TransIdentity[0],
                                                                control_any);
            Any context_any = orb.create_any();
            PropagationContextHelper.insert(context_any, context);

            pi_current.set_slot(slot_id, context_any);
        }
        catch (Exception e)
        {
            if (logger.isDebugEnabled())
                logger.debug("Exception: ", e);
        }
    }

    public void commit(boolean report_heuristics) 
        throws NoTransaction, HeuristicMixed, HeuristicHazard 
    {
        Thread current = Thread.currentThread();

        if (! contexts.containsKey(current))
            throw new NoTransaction();

	Control control = null;
        try
        {
            control = (Control) contexts.get(current);
            control.get_terminator().commit(report_heuristics);    
            control._release();

            removeContext(current);
	}
        catch (org.omg.CORBA.TRANSACTION_ROLLEDBACK tr) 
        {
	    // Transaction was rolledback.
            if (logger.isDebugEnabled())
                logger.debug("TRANSACTION_ROLLEDBACK: ", tr);

	    control._release();
	    removeContext(current);
	    throw tr; // re-throw the exception
        }
        catch (Exception e)
        {
            if (logger.isDebugEnabled())
                logger.debug("Exception: ", e);
        }
    }

    /**
     * This and the following method should actually throw 
     * NoTransaction, but that is against the spec.
     */
    public Control get_control() 
    {
        return (Control) contexts.get(Thread.currentThread());
    }

    public Status get_status()
    {
        Thread current = Thread.currentThread();

        if (! contexts.containsKey(current))
            return null;

        try
        {
            return ((Control) contexts.get(current)).get_coordinator().get_status();
        }
        catch (Exception e)
        {
            if (logger.isDebugEnabled())
                logger.debug("Exception: ", e);
        }
        return null;
    }

    public String get_transaction_name()
    {
        Thread current = Thread.currentThread();

        if (! contexts.containsKey(current))
            return null;

        try
        {            
            return ((Control) contexts.get(current)).get_coordinator().get_transaction_name();
        }
        catch (Exception e)
        {
            if (logger.isDebugEnabled())
                logger.debug("Exception: ", e);
        }
        return null;
    }

    public void resume(Control which) 
        throws InvalidControl 
    {
	setCurrentThreadContext(which);
    }

    public void rollback() 
        throws NoTransaction 
    {
        Thread current = Thread.currentThread();

        if (! contexts.containsKey(current))
            throw new NoTransaction();
        try
        {
            Control control = (Control) contexts.get(current);
            control.get_terminator().rollback();
   
            control._release();

            removeContext(current);
        }
        catch (Exception e)
        {
            if (logger.isDebugEnabled())
                logger.debug("Exception: ", e);
        }
    }

    public void rollback_only() 
        throws NoTransaction 
    {
        Thread current = Thread.currentThread();

        if (! contexts.containsKey(current))
            throw new NoTransaction();
        try
        {
            Control control = (Control) contexts.get(current);
            control.get_coordinator().rollback_only();       
            control._release();

            removeContext(current);
        }
        catch (Exception e){
            if (logger.isDebugEnabled())
                logger.debug("Exception: ", e);
        }
    }

    public void set_timeout(int seconds) 
    {
        timeouts.put(Thread.currentThread(), new Integer(seconds));
    }

    public Control suspend() 
    {
        Control result = get_control();
        removeContext(Thread.currentThread());
        return result;
    }

    public void setCurrentThreadContext(Control control) 
    {
        Thread thread = Thread.currentThread();

        contexts.put(thread, control);
    
        try
        {
            org.omg.PortableInterceptor.Current pi_current =
                (org.omg.PortableInterceptor.Current)orb.resolve_initial_references("PICurrent");

            // the info inserted here is actually never needed and mostly a waste of
            // space/bandwidth, since the control itself is transfered also.
            TransIdentity id = 
                new TransIdentity(control.get_coordinator(), 
                                  control.get_terminator(),
                                  new otid_t(0, 0, new byte[0]));
      
            Any control_any = orb.create_any();
            ControlHelper.insert(control_any, control);

            int timeout = 
                (timeouts.containsKey(thread))? 
                ((Integer) timeouts.get(thread)).intValue() : 
                DEFAULT_TIMEOUT;

            PropagationContext context = 
                new PropagationContext(timeout,
                                       id, 
                                       new TransIdentity[0],
                                       control_any);

            Any context_any = orb.create_any();
            PropagationContextHelper.insert(context_any, context);
            pi_current.set_slot(slot_id, context_any);
        }
        catch (Exception e)
        {
            if (logger.isDebugEnabled())
                logger.debug("Exception: ", e);
        }
    } 

    private void removeContext(Thread current)
    {
        //remove control from Hashtable
        contexts.remove(current);

        try
        {
            org.omg.PortableInterceptor.Current pi_current =
                (org.omg.PortableInterceptor.Current)orb.resolve_initial_references("PICurrent");

            //remove control from PICurrent by overwriting it with
            //an empty any
            Any empty = orb.create_any();     
            pi_current.set_slot(slot_id, empty);
        }
        catch (Exception e)
        {
            if (logger.isDebugEnabled())
                logger.debug("Exception: ", e);
        }
    } 
} // TransactionCurrentImpl






