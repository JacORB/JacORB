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

import org.jacorb.notification.evaluate.EvaluationException;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.AbstractPoolable;
import org.jacorb.notification.node.ComponentName;
import org.jacorb.notification.node.DynamicTypeException;
import org.jacorb.notification.node.EvaluationResult;
import org.jacorb.notification.node.RuntimeVariableNode;

import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.CosNotifyFilter.UnsupportedFilterableData;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AbstractMessage extends AbstractPoolable
{
    /**
     * Instead of directly using an instance of AbstractMessage an
     * indirection via MessageHandle is used. This way the Data in
     * AbstractMessage is kept only once. MessageHandle realizes
     * reference counting. As the last reference is disposed the
     * instance of AbstractMessage can be disposed.
     * MessageHandle also allows its priority and timeout to be
     * changed (Note: The real Message isn't modified by this).
     */
    class MessageHandle implements Message, Disposable {

        MessageHandle() {
            addReference();
        }

        private MessageHandle(int priority,
                              boolean priorityOverride,
                              long timeout,
                              boolean timeoutOverride) {

            // i would like to write this() here to call the no-args
            // construtor.
            // this compiles but gives some rare java.lang.VerifyError
            // exceptions at runtime
            addReference();

            priority_ = priority;
            priorityOverride_ = priorityOverride;
            timeOut_ = timeout;
            timeoutOverride_ = timeoutOverride;
        }

        private Message.MessageStateListener eventStateListener_;

        private boolean inValid_ = false;

        private boolean priorityOverride_ = false;
        private int priority_;
        private boolean timeoutOverride_ = false;
        private long timeOut_;

        public void setInitialFilterStage(FilterStage s) {
            AbstractMessage.this.setFilterStage(s);
        }

        public String getConstraintKey() {
            return AbstractMessage.this.getConstraintKey();
        }

        public Any toAny() {
            return AbstractMessage.this.toAny();
        }

        public StructuredEvent toStructuredEvent() {
            return AbstractMessage.this.toStructuredEvent();
        }

        public int getType() {
            return AbstractMessage.this.getType();
        }

        public FilterStage getInitialFilterStage() {
            return AbstractMessage.this.getFilterStage();
        }

        public EvaluationResult extractValue( EvaluationContext context,
                                              ComponentName componentName,
                                              RuntimeVariableNode runtimeVariable )
            throws EvaluationException,
                   DynamicTypeException
        {
            return AbstractMessage.this.extractValue(context,
                                                           componentName,
                                                           runtimeVariable);
        }

        public EvaluationResult extractValue( EvaluationContext context,
                                              ComponentName componentName)
            throws EvaluationException
        {
            return AbstractMessage.this.extractValue(context, componentName);
        }

        public  EvaluationResult extractFilterableData( EvaluationContext context,
                                                        ComponentName componentRootNode,
                                                        String variable)
            throws EvaluationException {

            return AbstractMessage.this.extractFilterableData(context,
                                                                    componentRootNode,
                                                                    variable);
        }

        public EvaluationResult extractVariableHeader( EvaluationContext context,
                                                       ComponentName componentName,
                                                       String s ) throws EvaluationException {

            return AbstractMessage.this.extractVariableHeader(context,
                                                                    componentName,
                                                                    s);
        }

        public boolean hasStartTime() {
            return AbstractMessage.this.hasStartTime();
        }

        public Date getStartTime() {
            return AbstractMessage.this.getStartTime();
        }

        public boolean hasStopTime() {
            return AbstractMessage.this.hasStopTime();
        }

        public Date getStopTime() {
            return AbstractMessage.this.getStopTime();
        }

        public boolean hasTimeout() {
            return AbstractMessage.this.hasTimeout();
        }

        public long getTimeout() {
            if (timeoutOverride_) {
                return timeOut_;
            } else {
                return AbstractMessage.this.getTimeout();
            }
        }

        public void setTimeout(long timeout) {
            timeOut_ = timeout;

            if (eventStateListener_ != null) {
                eventStateListener_.actionLifetimeChanged(timeout);
            }
        }

        public void setPriority(int priority) {
            priorityOverride_ = true;
            priority_ = priority;
        }

        public int getPriority() {
            if (priorityOverride_) {
                return priority_;
            } else {
                return AbstractMessage.this.getPriority();
            }
        }

        public boolean match(FilterStage s) {
            return AbstractMessage.this.match(s);
        }

        public boolean match(MappingFilter m,
                             AnyHolder r) throws UnsupportedFilterableData {

            return AbstractMessage.this.match(m, r);
        }

        public Object clone() {
            try {
                checkInvalid();

                return new MessageHandle(priority_,
                                              priorityOverride_,
                                              timeOut_,
                                              timeoutOverride_);
            } catch (InterruptedException e) {
                return null;
            }
        }

        public void dispose() {
            removeReference();
        }

        public synchronized boolean isInvalid() {
            return inValid_;
        }

        public void setMessageStateListener(Message.MessageStateListener l) {
            eventStateListener_ = l;
        }

        public synchronized void actionTimeout() {
            inValid_ = true;
        }

        public String toString() {
            return "-->" + AbstractMessage.this.toString();
        }

        private void checkInvalid() throws InterruptedException {
            if (isInvalid()) {
                throw new InterruptedException("This Notification has been invalidated");
            }
        }
    }

    ////////////////////////////////////////

    static protected Logger logger_ =
        Hierarchy.getDefaultHierarchy().getLoggerFor(Message.class.getName());

    protected static ORB sOrb = ORB.init();

    protected boolean proxyConsumerFiltered_;
    protected boolean supplierAdminFiltered_;
    protected boolean consumerAdminFiltered_;
    protected boolean proxySupplierFiltered_;
    private FilterStage currentFilterStage_;

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
    protected int referenced_ = 0;

    public void reset()
    {
        referenced_ = 0;
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
    synchronized public void removeReference()
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
        throws EvaluationException,
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
        throws EvaluationException

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

    public Message getHandle() {
        return new MessageHandle();
    }

    public abstract boolean hasStartTime();

    public abstract Date getStartTime();

    public abstract boolean hasStopTime();

    public abstract Date getStopTime();

    public abstract boolean hasTimeout();

    public abstract long getTimeout();

    public abstract int getPriority();

    public abstract boolean match(FilterStage filterStage);

    public abstract boolean match(MappingFilter filter,
                                  AnyHolder value) throws UnsupportedFilterableData;
}

