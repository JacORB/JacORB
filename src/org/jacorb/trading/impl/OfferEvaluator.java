
// Copyright (C) 1998-2001
// Object Oriented Concepts, Inc.

// **********************************************************************
//
// Copyright (c) 1997
// Mark Spruiell (mark@intellisoft.com)
//
// See the COPYING file for more information
//
// **********************************************************************

package org.jacorb.trading.impl;

import java.util.*;
import org.omg.CORBA.*;
import org.omg.CosTrading.*;
import org.omg.CosTrading.LookupPackage.*;
import org.omg.CosTrading.RegisterPackage.*;
import org.omg.CosTrading.ProxyPackage.*;
import org.omg.CosTradingDynamic.*;
import org.jacorb.trading.constraint.Constraint;
import org.jacorb.trading.util.*;

/**
 * Used to receive offers after they've been evaluated
 */
interface OfferListener
{
    /**
     * Called when a matching offer has been found; returns true if
     * more offers can be accepted, false otherwise
     */
    public boolean offerNotify(SourceAdapter source);
	
    /**
     * Informs the listener that a source has been processed
     */
    public void sourceNotify(SourceAdapter source);
}

/**
 * This class is responsible for evaluating all "local" service offers.
 * Two queues are used to feed service offers requiring special handling
 * (i.e. offers containing dynamic properties and proxy offers) to
 * independent threads for simultaneous evaluation.
 *
 * The final result is a Vector of SourceAdapter objects that have
 * matched the constraint.
 */
public class OfferEvaluator 
	implements Runnable, OfferListener
{


    // the maxmimum number of helper threads created to evaluate offers
  private static final int MAX_DYNAMIC_THREADS = 10;
  private static final int MAX_PROXY_THREADS = 10;

  private String m_type;
  private Constraint m_constraint;
  private String m_preference;
  private org.omg.CosTrading.Policy[] m_policies;
  private SpecifiedProps m_desiredProps;
  private Vector m_sources;
  private int m_matchCard;
  private int m_matchCount;
  private Vector m_results;
  private int m_numProcessed;
  private int m_threadPriority;
  private int m_numDynamicThreads;
  private int m_numProxyThreads;
  private MessageQueue m_proxyQueue;
  private MessageQueue m_dynamicQueue;


  public OfferEvaluator(
    String type,
    Constraint constraint,
    String preference,
    org.omg.CosTrading.Policy[] policies,
    SpecifiedProps desiredProps,
    Vector sources,
    int matchCard)
  {
      // save arguments
    m_type              = type;
    m_constraint        = constraint;
    m_preference        = preference;
    m_policies          = policies;
    m_desiredProps      = desiredProps;
    m_sources           = sources;
    m_matchCard         = matchCard;

    m_matchCount        = 0;
    m_results           = new Vector();
    m_numProcessed      = 0;
    m_threadPriority    = Thread.currentThread().getPriority();
    if( m_threadPriority < Thread.MAX_PRIORITY )
	m_threadPriority++;

    m_numDynamicThreads = 0;
    m_numProxyThreads   = 0;
    m_proxyQueue        = new MessageQueue();
    m_dynamicQueue      = new MessageQueue();

      // start a thread to evaluate the offers
    new Thread(this).start();
  }


  public synchronized Vector getResults()
  {
    while (! getDone()) {
      try {
        wait();  // wait until we are notified and check again
      }
      catch (InterruptedException e) {
      }
    }

    return m_results;
  }


  public synchronized boolean getDone()
  {
      // we're done when we've considered all the offers or
      // when m_matchCount == m_matchCard
    boolean result =
      (m_matchCard == m_matchCount || m_numProcessed == m_sources.size());

    return result;
  }


  /**
   * Inherited from Listener; called when an offer has been evaluated
   */
  public synchronized boolean offerNotify(SourceAdapter source)
  {
      // add source to the results
    addSource(source);

    if (getDone())
      notifyAll();

      // return true if we can accept more offers
    return (! getDone());
  }


  /**
   * Inherited from Listener; called when a source has been processed
   */
  public synchronized void sourceNotify(SourceAdapter source)
  {
      // we keep track of the number of original SourceAdapter objects
      // we've processed, so that we know when we've evaluated all of
      // them;  a single SourceAdapter object may result in numerous
      // calls to offerNotify (for proxy offers)
    m_numProcessed++;

    if (getDone())
      notifyAll();
  }


  /**
   * The main dispatch thread; we iterate over all of the given sources,
   * dispatching them to the appropriate thread queue or evaluating the
   * simple offers immediately.
   *
   * Offers with dynamic properties and proxy offers are handled in separate
   * threads.  We give a higher priority to these threads; if we didn't, the
   * loop in this method might starve the threads that are evaluating offers,
   * forcing evaluation to occur after all other offers.  We at least want to
   * give the threads a chance of being included, if our match cardinality
   * is limited.
   */
  public void run()  // cannot be synchronized
  {
    Enumeration e = m_sources.elements();
    while (e.hasMoreElements() && ! getDone()) {
      SourceAdapter source = (SourceAdapter)e.nextElement();

      if (source instanceof ProxySourceAdapter)
        scheduleProxy(source);
      else if (PropUtil.hasDynamicProperties(source.getProperties()))
        scheduleDynamic(source);
      else {  // simple offer
        if (m_constraint.evaluate(source))
          offerNotify(source);
        sourceNotify(source);
      }
    }

      // wait until we're done, and then deactivate the queues
    synchronized (this) {
      while (! getDone()) {
        try {
          wait();  // wait until we are notified and check again
        }
        catch (InterruptedException ex) {
        }
      }

        // deactivating the queues signals the threads to terminate
      m_dynamicQueue.deactivate();
      m_proxyQueue.deactivate();
    }
  }


  /**
   * The source's offer has dynamic properties, so schedule a
   * thread to evaluate it
   */
  protected synchronized void scheduleDynamic(SourceAdapter source)
  {
      // create new DynEval objects until we reach our maximum
    if (m_numDynamicThreads < MAX_DYNAMIC_THREADS) {
      DynEval d =
        new DynEval(m_dynamicQueue, m_constraint, m_desiredProps, this);
      m_numDynamicThreads++;
      d.setPriority(m_threadPriority);
      d.start();
    }

      // let the next available thread process this offer
    m_dynamicQueue.enqueue(source);
  }


  /** The source is a proxy offer, so schedule a helper thread to evaluate it */
  protected synchronized void scheduleProxy(SourceAdapter source)
  {
      // create new ProxyEval objects until we reach our maximum
    if (m_numProxyThreads < MAX_PROXY_THREADS) {
      ProxyEval p = new ProxyEval(m_proxyQueue, m_type, m_constraint,
        m_preference, m_policies, m_desiredProps, this);
      m_numProxyThreads++;
      p.setPriority(m_threadPriority);
      p.start();
    }

      // let the next available thread process this offer
    m_proxyQueue.enqueue(source);
  }


  /** Adds another match to our list of results */
  protected synchronized void addSource(SourceAdapter source)
  {
      // we may be restricted by m_matchCard, therefore we check
      // to make sure we're not done before adding more matching
      // offers
    if (m_matchCount < m_matchCard) {
      m_results.addElement(source);
      m_matchCount++;
    }
  }



  /**
   * DynEval is responsible for evaluating offers with dynamic properties
   */
  protected static class DynEval extends Thread
  {
    private MessageQueue m_queue;
    private Constraint m_constraint;
    private SpecifiedProps m_desiredProps;
    private OfferListener m_listener;


    public DynEval(
      MessageQueue queue,
      Constraint constraint,
      SpecifiedProps desiredProps,
      OfferListener listener)
    {
      m_queue = queue;
      m_constraint = constraint;
      m_desiredProps = desiredProps;
      m_listener = listener;
    }


    /**
     * We remove offers from our queue and evaluate them against the
     * constraint until the queue is deactivated
     */
    public void run()
    {
      SourceAdapter source;

      while ((source = (SourceAdapter)m_queue.dequeue()) != null) {
          // first evaluate the constraint with this source

        if (m_constraint.evaluate(source)) {
          Property[] props = source.getProperties();

            // the constraint succeeded, now we need to ensure that
            // the SourceAdapter has evaluated all of the "desired"
            // properties

          if (m_desiredProps.discriminator() == HowManyProps.all) {
            for (int i = 0; i < props.length; i++)
              source.getPropertyValue(props[i].name);
          }
          else if (m_desiredProps.discriminator() == HowManyProps.some) {
            String[] names = m_desiredProps.prop_names();
            for (int i = 0; i < names.length; i++)
              source.getPropertyValue(names[i]);
          }

          m_listener.offerNotify(source);
        }

        m_listener.sourceNotify(source);
      }
    }
  }



  /**
   * ProxyEval is responsible for evaluating proxy offers
   */
  protected static class ProxyEval extends Thread
  {
    private MessageQueue m_queue;
    private String m_type;
    private Constraint m_constraint;
    private String m_preference;
    private org.omg.CosTrading.Policy[] m_policies;
    private SpecifiedProps m_desiredProps;
    private OfferListener m_listener;


    public ProxyEval(
      MessageQueue queue,
      String type,
      Constraint constraint,
      String preference,
      org.omg.CosTrading.Policy[] policies,
      SpecifiedProps desiredProps,
      OfferListener listener)
    {
      m_queue        = queue;
      m_type         = type;
      m_constraint   = constraint;
      m_preference   = preference;
      m_policies     = policies;
      m_desiredProps = desiredProps;
      m_listener     = listener;
    }


    /**
     * We remove offers from our queue and evaluate them
     * until the queue is deactivated
     */
    public void run()
    {
      ProxySourceAdapter source;

      while ((source = (ProxySourceAdapter)m_queue.dequeue()) != null) {
        ProxyInfo info = source.getInfo();

        boolean match = false;

          // if if_match_all is true, then type conformance is all that
          // is required for this proxy offer to be considered a match
        if (info.if_match_all)
          match = true;
        else  // otherwise evaluate the offer against the constraint
          match = m_constraint.evaluate(source);

        if (match) {
            // rewrite the constraint; this may fail if the constraint
            // uses a dynamic property for which a value could not be
            // obtained
          String primary = m_constraint.getConstraint();
          String constraint = Recipe.rewrite(info.recipe, source, primary);

          if (constraint != null) {
              // build a new array of policies, appending the "policies
              // to pass on"
            org.omg.CosTrading.Policy[] policies =
              new org.omg.CosTrading.Policy[
                m_policies.length + info.policies_to_pass_on.length];

            int count = 0;
            while (count < m_policies.length) {
              policies[count] = m_policies[count];
              count++;
            }

            for (int i = 0; i < info.policies_to_pass_on.length; i++)
              policies[count++] = info.policies_to_pass_on[i];

            try {
              OfferSeqHolder offers = new OfferSeqHolder();
              OfferIteratorHolder iter = new OfferIteratorHolder();
              PolicyNameSeqHolder limits = new PolicyNameSeqHolder();

                // call the target trader
              info.target.query(m_type, constraint, m_preference, policies,
                m_desiredProps, 0, offers, iter, limits);

                // process any offers we received
              if (iter.value != null) {
                OfferSeqHolder seq = new OfferSeqHolder();
                boolean more;
                do {
                  more = iter.value.next_n(20, seq);
                  for (int i = 0; i < seq.value.length; i++) {
                    Offer o = seq.value[i];
                      // create a new SourceAdapter object for each offer
                    SourceAdapter src =
                      new SourceAdapter(o.reference, o.properties);

                    if (! m_listener.offerNotify(src))
                      break;
                  }
                }
                while (more);

                iter.value.destroy();
              }
            }
            catch (org.omg.CORBA.UserException e) {
              // ignore
            }
            catch (org.omg.CORBA.SystemException e) {
              // ignore
            }
          }
        }

          // we've processed the source
        m_listener.sourceNotify(source);
      }
    }
  }
}










