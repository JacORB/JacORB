package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.StructuredEvent;
import org.jacorb.notification.node.ComponentName;
import org.jacorb.notification.node.EvaluationResult;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.Poolable;
import org.jacorb.notification.evaluate.EvaluationException;
import org.jacorb.notification.FilterUtils;
import org.apache.log.Logger;
import org.apache.log.Hierarchy;

/**
 * NotificationEvent.java
 *
 *
 * Created: Tue Oct 22 20:16:33 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public abstract class NotificationEvent extends Poolable
{

    public static final int TYPE_ANY = 0;
    public static final int TYPE_STRUCTURED = 1;
    public static final int TYPE_TYPED = 2;

    ////////////////////////////////////////

    static protected Logger logger_ = 
	Hierarchy.getDefaultHierarchy().getLoggerFor(NotificationEvent.class.getName());

    ////////////////////////////////////////

    protected ApplicationContext applicationContext_;
    protected boolean proxyConsumerFiltered_;
    protected boolean supplierAdminFiltered_;
    protected boolean consumerAdminFiltered_;
    protected boolean proxySupplierFiltered_;
    protected FilterStage currentFilterStage_;

    ////////////////////////////////////////

    protected NotificationEvent( ApplicationContext appContext )
    {
        applicationContext_ = appContext;
    }

    ////////////////////////////////////////

    public abstract EventTypeIdentifier getEventTypeIdentifier();

    /**
     * Extracts a value out of this NotificationEvent. Extract the
     * Value denoted by the ComponentOperator Node out of this
     * NotificationEvent. The EvaluationContext is used to cache and
     * lookup results.
     *
     * @param evaluationContext an <code>EvaluationContext</code> value
     * @param c a <code>ComponentOperator</code> value
     * @return an <code>EvaluationResult</code> value
     * @exception EvaluationException if an error occurs
     */
    public abstract EvaluationResult evaluate( EvaluationContext evaluationContext,
					       ComponentName c )
	throws EvaluationException;

    
    /**
     * Check if the denoted Union value has a Default
     * Discriminator. Check if the Union value denoted by the
     * ComponentOperator Nodes value has a Default Discriminator.
     *
     * @param evaluationContext an <code>EvaluationContext</code> value
     * @param c a <code>ComponentOperator</code> value
     * @return an <code>EvaluationResult</code> value
     * @exception EvaluationException if an error occurs
     */
    public abstract EvaluationResult hasDefault( EvaluationContext evaluationContext, 
						 ComponentName c )
	throws EvaluationException;


    /**
     * Check if a Value exists in this Event. Check if the Value
     * denoted by the ComponentOperator Node 
     * exists in this Event.
     *
     * @param component a <code>ComponentOperator</code> that denotes
     * a Path within an Event.
     *
     * @return an boolean <code>EvaluationResult</code> value.
     *
     * @exception EvaluationException if an error occurs during Evaluation.
     */
    public abstract EvaluationResult testExists( EvaluationContext evaluationContext,
						 ComponentName component )
	throws EvaluationException;

    /**
     * get the Constraint Key for this Event. The Constraint Key is
     * used to fetch the Filter Constraints that must be evaluated for
     * this Event. The Constraint Key consists of domain_name and
     * type_name of the Event.
     * Within this Implementation the Operation 
     * {@link FilterUtils#calcConstraintKey(String, String)} 
     * is used to provide a uniform
     * Mapping from domain_name and type_name to a Constraint Key.
     *
     * @return a <code>String</code> value
     */
    public abstract String getConstraintKey();

    /**
     * Access this NotificationEvent as Any.
     *
     * @return an <code>Any</code> value
     */
    public abstract Any toAny();

    /**
     * Access this NotificationEvent as StructuredEvent.
     *
     * @return a <code>StructuredEvent</code> value
     */
    public abstract StructuredEvent toStructuredEvent();

    /**
     * get the Type of this NotificationEvent. The value is one of
     * {@link #TYPE_ANY TYPE_ANY}, {@link #TYPE_STRUCTURED
     * TYPE_STRUCTURED} or {@link #TYPE_TYPED TYPE_TYPED}.
     *
     * @return the Type of this NotificationEvent.
     */
    public abstract int getType();

    /**
     * Internal Reference Counter.
     */
    private int referenced_ = 0;

    public void reset()
    {
        currentFilterStage_ = null;
    }

    /**
     * Add a reference on this NotificationEvent. After Usage release
     * must be called.
     */
    synchronized public void addReference()
    {
        ++referenced_;
    }

    /**
     * release this NotificationEvent. If the
     * internal Refcounter is zero the NotificationEvent is returned
     * to its pool.
     */
    synchronized public void release()
    {
        if ( referenced_ > 0 )
        {
            --referenced_;
        }

        if ( referenced_ == 0 )
        {
            super.release();
        }
    }

    public void setFilterStage( FilterStage node )
    {
        currentFilterStage_ = node;
    }

    public FilterStage getFilterStage()
    {
        return currentFilterStage_;
    }

} // NotificationEvent

