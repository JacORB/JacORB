package org.jacorb.trading.util;

import org.omg.CosTrading.*;
import org.omg.CosTrading.LookupPackage.*;
import org.omg.CORBA.*;
import java.lang.*;

/**
 * This class bundles the parameters to Lookup.query(). QueryPropagator
 * accesses the attributes directly, while Lookup.query() has to go
 * via the getXXX()-methods. The synchronization with the executing
 * QueryThread is done via a binary semaphore.
 *
 * @author Nicolas Noffke
 * $Log$
 * Revision 1.3  2002/03/19 11:10:13  brose
 * *** empty log message ***
 *
 * Revision 1.2  2002/03/17 18:45:24  brose
 * *** empty log message ***
 *
 * Revision 1.4  1999/11/25 16:08:19  brose
 * cosmetics
 *
 * Revision 1.3  1999/11/08 08:11:52  brose
 * *** empty log message ***
 *
 * Revision 1.2  1999/11/03 18:04:06  brose
 * *** empty log message ***
 *
 * Revision 1.1  1999-10-06 12:06:59+02  brose
 * *** empty log message ***
 *
 * Revision 1.2  1999-10-05 16:08:49+02  brose
 * New directory structure for trading service
 *
 * Revision 1.1  1999-10-04 10:51:26+02  brose
 * *** empty log message ***
 *
 */

public class QueryContainer  {
    protected String m_type;
    protected String m_constr;
    protected String m_pref;
    protected org.omg.CosTrading.Policy[] m_policies;
    protected SpecifiedProps m_desired_props;
    protected int m_how_many;
    protected OfferSeqHolder m_offers;
    protected OfferIteratorHolder m_offer_itr;
    protected PolicyNameSeqHolder m_limits_applied;
    protected UserException m_exception = null;
    protected Lookup m_target;
    protected Semaphore m_mutex;

    public int no = 0;
    public static int count = 0;

    /**
     * The constructor. takes all "in"-Parameters from Lookup.query()
     *
     * @param type The ServiceType
     * @param contr The Constraints
     * @param pref The Preferences
     * @param policies  An array holding the queries policies
     * @param desired_props The desired properties
     * @param how_many  No. of offers to be returned
     * @param target  The lookup-interface of the queries target trader
     * @author Nicolas Noffke
     */
    public QueryContainer(String type,
			  String constr,
			  String pref,
			  org.omg.CosTrading.Policy[] policies,
			  SpecifiedProps desired_props,
			  int how_many,
			  Lookup target) {
	m_type = type;
	m_constr = constr;
	m_pref = pref;
	m_policies = policies;
	m_desired_props = desired_props;
	m_how_many = how_many;
	m_target = target;

	no = count++;

	// The "out"-Parameters of query are instanciated here, 
	// for lookup.query() does not have to store them as well.
	m_offers = new OfferSeqHolder();
	m_offer_itr = new OfferIteratorHolder();
	m_limits_applied = new PolicyNameSeqHolder();

	m_mutex = new Semaphore(0);
	
    }


   /**
     * Convenience constructor, for keeping the interface small. Takes
     * another QueryContainer to get the "in"-Parameters of query().
     *
     * @param templ The template to take the values from
     * @param target The lookup-interface of the queries target trader
     * @author Nicolas Noffke
     */
    public QueryContainer(QueryContainer templ, Lookup target){
	this(templ.m_type, templ.m_constr, templ.m_pref,
	     templ.m_policies, templ.m_desired_props,
	     templ.m_how_many, target);
    }

    /**
     * This method blocks until the result is ready.
     * @author Nicolas Noffke
     */
    public void resultReady(){
	m_mutex.P(); // Blocks, until QueryThread issues V()
	//	m_mutex.V(); // not really necessary since object is never used again
    }

    /**
     * Returns any UserException (i.e. the ones that query() explicitly throws)
     * that was caught executing this query.<br>
     * *Not* safe to call until resultReady returned.
     *
     * @return Null, if query returned correctly.
     * @author Nicolas Noffke
     */
    public UserException getException(){
	return m_exception;
    }
    
    /**
     * Returnes the Offers returned by the remote query().<br>
     * *Not* safe to call until resultReady returned.
     *
     * @return The offers
     * @author Nicolas Noffke
     */
    public OfferSeqHolder getOffers(){
	return m_offers;
    }

    /**
     * Returnes the OfferIterator returned by the remote query().<br>
     * *Not* safe to call until resultReady returned.
     *
     * @return The offer iterator
     * @author Nicolas Noffke
     */
    public OfferIteratorHolder getItr(){
	return m_offer_itr;
    }

    /**
     * Returnes the policies_applied-sequence returned by the remote query.<br>
     * *Not* safe to call until resultReady returned.
     *
     * @return The limits_applied policies
     * @author Nicolas Noffke
     */
    public PolicyNameSeqHolder getLimits(){
	return m_limits_applied;
    }
    
} // QueryContainer










