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

import java.util.Date;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.jacorb.notification.evaluate.EvaluationException;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.AbstractPoolable;
import org.jacorb.notification.node.ComponentName;
import org.jacorb.notification.node.DynamicTypeException;
import org.jacorb.notification.node.EvaluationResult;
import org.jacorb.notification.node.RuntimeVariableNode;
import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.CosNotifyFilter.UnsupportedFilterableData;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;

/**
 * NotificationEvent.java
 *
 *
 * Created: Tue Oct 22 20:16:33 2002
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class NotificationEvent extends AbstractPoolable
{

    public interface NotificationEventStateListener {
        public void actionLifetimeChanged(long lifetime);
    }

    private boolean disposed_ = false;

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

    protected NotificationEventStateListener notificationEventStateListener_;

    private long timeOut_ = -1;

    ////////////////////////////////////////

    protected NotificationEvent( ApplicationContext appContext )
    {
        applicationContext_ = appContext;
    }

    ////////////////////////////////////////

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
        disposed_ = false;
        currentFilterStage_ = null;
        timeOut_ = -1;
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

    public EvaluationResult extractValue(EvaluationContext context,
                                         ComponentName componentRootNode,
                                         RuntimeVariableNode runtimeVariable )
        throws InconsistentTypeCode,
               TypeMismatch,
               EvaluationException,
               InvalidValue,
               DynamicTypeException
    {
        EvaluationResult _ret = null;
        String _completePath = componentRootNode.getComponentName();

        if (logger_.isDebugEnabled()) {
            logger_.debug("Extract " + _completePath);
        }

        _ret = context.lookupResult( _completePath );

        if ( _ret == null )
        {
            _ret = runtimeVariable.evaluate(context);

            if (componentRootNode.right() != null) {
                _ret = MessageUtils.extractFromAny(componentRootNode.right(),
                                                             _ret.getAny(),
                                                             context,
                                                             runtimeVariable.toString());
            }
            context.storeResult( _completePath, _ret);
        }

        return _ret;
    }

    public abstract EvaluationResult extractFilterableData(EvaluationContext context,
                                                           ComponentName componentRootNode,
                                                           String variable)
        throws EvaluationException;

    public abstract EvaluationResult extractVariableHeader(EvaluationContext context,
                                                           ComponentName componentRootNode,
                                                           String variable)
        throws EvaluationException;


    public EvaluationResult extractValue( EvaluationContext evaluationContext,
                                          ComponentName componentRootNode )
        throws InconsistentTypeCode,
               TypeMismatch,
               EvaluationException,
               InvalidValue
    {
        EvaluationResult _ret = null;
        String _completePath = componentRootNode.getComponentName();

        if ( logger_.isDebugEnabled() )
        {
            logger_.debug( "extractValue path: " + componentRootNode.toStringTree() );
            logger_.debug( "complete path is: " + _completePath );
        }

        _ret = evaluationContext.lookupResult( _completePath );

        logger_.debug("Cache lookup: " + _ret);

        if ( _ret == null )
        {
            _ret = MessageUtils.extractFromAny(componentRootNode.left(),
                                                         toAny(),
                                                         evaluationContext,
                                                         componentRootNode.toString());

            // Cache the EvaluationResult
            if ( _ret != null )
            {
                if ( logger_.isDebugEnabled() )
                {
                    logger_.debug( "Cache Result: " + _completePath + " => " + _ret );
                }

                evaluationContext.storeResult( _completePath, _ret );
            }
        }
        else
        {
            logger_.debug( "Result Cache HIT" + _ret);
        }

        return _ret;
    }

    public boolean hasStartTime() {
        return false;
    }

    public Date getStartTime() {
        return null;
    }

    public boolean hasStopTime() {
        return false;
    }

    public Date getStopTime() {
        return null;
    }

    public boolean hasTimeout() {
        return timeOut_ != -1;
    }

    public long getTimeout() {
        return timeOut_;
    }

    public void setTimeout(long timeout) {
        timeOut_ = timeout;

        if (notificationEventStateListener_ != null) {
            notificationEventStateListener_.actionLifetimeChanged(timeout);
        }
    }

    public void setPriority(long priority) {
    }

    public void setNotificationEventStateListener(NotificationEventStateListener l) {
        notificationEventStateListener_ = l;
    }

    public synchronized boolean isDisposable() {
        return disposed_;
    }

    public synchronized void setDisposable() {
        disposed_ = true;
    }

    public abstract boolean match(FilterStage filterStage);

    public abstract boolean match(MappingFilter filter, AnyHolder value) throws UnsupportedFilterableData;
}

