package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

import java.util.Iterator;
import java.util.List;

import org.jacorb.notification.filter.ComponentName;
import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.filter.EvaluationException;
import org.jacorb.notification.filter.EvaluationResult;
import org.jacorb.notification.filter.RuntimeVariable;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.util.AbstractPoolable;
import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.CosNotifyFilter.UnsupportedFilterableData;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AbstractMessage extends AbstractPoolable
{
    /**
     * Instead of directly using an instance of AbstractMessage an indirection via MessageHandle is
     * used. This way the Data in AbstractMessage is kept only once. MessageHandle uses reference
     * counting. As the last MessageHandle is disposed the AbstractMessage containing the data can
     * be disposed. MessageHandle also allows the priority and timeout to be changed on a per handle
     * base (the underlying AbstractMessage isn't affected by this)
     */
    class MessageHandle implements Message, Disposable
    {
        /**
         * Listener that gets notified if the State of this MessageHandle changes.
         */
        private MessageStateListener eventStateListener_;

        private boolean isInvalid_ = false;

        /**
         * flag to indicate that the Priority has been changed for this Messagehandle.
         */
        private boolean isPriorityOverridden = false;

        /**
         * if isPriorityOverridden_ is true priority_ contains the Priority for this MessageHandle.
         */
        private int priority_;

        /**
         * flag to indicate that the Timeout has been changed for this Messagehandle.
         */
        private boolean isTimeoutOverridden_ = false;

        /**
         * if isTimeoutOverridden_ is true timeOut_ contains the Timeout for this MessageHandle.
         */
        private long timeOut_;

        ////////////////////

        /**
         * default ctor. adds a reference to the underlying message.
         */
        MessageHandle()
        {
            addReference();
        }

        /**
         * copy ctor. adds a reference to the underlying message.
         */
        private MessageHandle(int priority, boolean priorityOverride, long timeout,
                boolean timeoutOverride)
        {
            // i would like to write this() here to call the no-args
            // constructor.
            // this compiles but results in java.lang.VerifyErrors
            // at runtime
            addReference();

            priority_ = priority;
            isPriorityOverridden = priorityOverride;
            timeOut_ = timeout;
            isTimeoutOverridden_ = timeoutOverride;
        }

        /**
         * set the Inital FilterStage (the ProxyConsumer that has received the Message).
         */
        public void setInitialFilterStage(FilterStage s)
        {
            AbstractMessage.this.setFilterStage(s);
        }

        public FilterStage getInitialFilterStage()
        {
            return AbstractMessage.this.getFilterStage();
        }

        public String getConstraintKey()
        {
            return AbstractMessage.this.getConstraintKey();
        }

        public Any toAny()
        {
            return AbstractMessage.this.toAny();
        }

        public Property[] toTypedEvent() throws NoTranslationException
        {
            return AbstractMessage.this.toTypedEvent();
        }

        public StructuredEvent toStructuredEvent()
        {
            return AbstractMessage.this.toStructuredEvent();
        }

        public int getType()
        {
            return AbstractMessage.this.getType();
        }

        public EvaluationResult extractValue(EvaluationContext context,
                ComponentName componentName, RuntimeVariable runtimeVariable)
                throws EvaluationException
        {
            return AbstractMessage.this.extractValue(context, componentName, runtimeVariable);
        }

        public EvaluationResult extractValue(EvaluationContext context, ComponentName componentName)
                throws EvaluationException
        {
            return AbstractMessage.this.extractValue(context, componentName);
        }

        public EvaluationResult extractFilterableData(EvaluationContext context,
                ComponentName componentRootNode, String variable) throws EvaluationException
        {
            return AbstractMessage.this.extractFilterableData(context, componentRootNode, variable);
        }

        public EvaluationResult extractVariableHeader(EvaluationContext context,
                ComponentName componentName, String s) throws EvaluationException
        {
            return AbstractMessage.this.extractVariableHeader(context, componentName, s);
        }

        public boolean hasStartTime()
        {
            return AbstractMessage.this.hasStartTime();
        }

        public long getStartTime()
        {
            return AbstractMessage.this.getStartTime();
        }

        public boolean hasStopTime()
        {
            return AbstractMessage.this.hasStopTime();
        }

        public long getStopTime()
        {
            return AbstractMessage.this.getStopTime();
        }

        public boolean hasTimeout()
        {
            return isTimeoutOverridden_ || AbstractMessage.this.hasTimeout();
        }

        public long getTimeout()
        {
            if (isTimeoutOverridden_)
            {
                return timeOut_;
            }
            return AbstractMessage.this.getTimeout();
        }

        public void setTimeout(long timeout)
        {
            timeOut_ = timeout;

            isTimeoutOverridden_ = true;

            if (eventStateListener_ != null)
            {
                eventStateListener_.actionLifetimeChanged(timeout);
            }
        }

        public void setPriority(int priority)
        {
            isPriorityOverridden = true;

            priority_ = priority;
        }

        public int getPriority()
        {
            if (isPriorityOverridden)
            {
                return priority_;
            }
            return AbstractMessage.this.getPriority();
        }

        public boolean match(FilterStage s)
        {
            return AbstractMessage.this.match(s);
        }

        public boolean match(MappingFilter m, AnyHolder r) throws UnsupportedFilterableData
        {
            return AbstractMessage.this.match(m, r);
        }

        public Object clone()
        {
            try
            {
                checkInvalid();

                return new MessageHandle(priority_, isPriorityOverridden, timeOut_, isTimeoutOverridden_);
            } catch (IllegalArgumentException e)
            {
                return null;
            }
        }

        public void dispose()
        {
            removeReference();
        }

        public synchronized boolean isInvalid()
        {
            return isInvalid_;
        }

        public void setMessageStateListener(MessageStateListener listener)
        {
            eventStateListener_ = listener;
        }

        public MessageStateListener removeMessageStateListener()
        {
            MessageStateListener _listener = eventStateListener_;
            eventStateListener_ = null;

            return _listener;
        }

        public synchronized void actionTimeout()
        {
            isInvalid_ = true;
        }

        public long getReceiveTimestamp()
        {
            return AbstractMessage.this.getReceiveTimestamp();
        }

        public String toString()
        {
            return "[Message/" + AbstractMessage.this + "]";
        }

        private void checkInvalid() throws IllegalArgumentException
        {
            if (isInvalid())
            {
                throw new IllegalArgumentException("This Notification has been invalidated");
            }
        }
    }

    ////////////////////////////////////////

    protected boolean proxyConsumerFiltered_;

    protected boolean supplierAdminFiltered_;

    protected boolean consumerAdminFiltered_;

    protected boolean proxySupplierFiltered_;

    private FilterStage currentFilterStage_;

    private long receiveTimestamp_;

    ////////////////////////////////////////

    /**
     * get the Constraint Key for this Event. The Constraint Key is used to fetch the Filter
     * Constraints that must be evaluated for this Event. The Constraint Key consists of domain_name
     * and type_name of the Event.
     *
     * @return a <code>String</code> value
     */
    public abstract String getConstraintKey();

    public long getReceiveTimestamp()
    {
        return receiveTimestamp_;
    }

    /**
     * Access this NotificationEvent as Any.
     *
     * @return an <code>Any</code> value
     */
    public abstract Any toAny();

    /**
     * convert this message to a TypedEvent.
     *
     * @return a sequence of name-value pairs.
     * @throws NoTranslationException
     *             if the contents of the message cannot be translated into a TypedEvent.
     */
    public abstract Property[] toTypedEvent() throws NoTranslationException;

    /**
     * Access this NotificationEvent as StructuredEvent.
     *
     * @return a <code>StructuredEvent</code> value
     */
    public abstract StructuredEvent toStructuredEvent();

    /**
     * get the Type of this NotificationEvent. The value is one of
     * {@link org.jacorb.notification.interfaces.Message#TYPE_ANY},{@link
     * org.jacorb.notification.interfaces.Message#TYPE_STRUCTURED}, or {@link
     * org.jacorb.notification.interfaces.Message#TYPE_TYPED}.
     *
     * @return the Type of this NotificationEvent.
     */
    public abstract int getType();

    /**
     * Internal Reference Counter.
     */
    protected int referenced_ = 0;

    public synchronized final void reset()
    {
        referenced_ = 0;
        currentFilterStage_ = null;

        doReset();
    }

    protected void doReset()
    {
        // no operation
    }

    /**
     * Add a reference on this NotificationEvent. After Usage removeReference must be called.
     */
    public synchronized void addReference()
    {
        ++referenced_;
    }

    /**
     * release this NotificationEvent. If the internal Refcounter is zero the NotificationEvent is
     * returned to its pool.
     */
    protected synchronized void removeReference()
    {
        if (referenced_ > 0)
        {
            --referenced_;
        }

        if (referenced_ == 0)
        {
            super.dispose();
        }
    }

    public void setFilterStage(FilterStage node)
    {
        currentFilterStage_ = node;
    }

    public FilterStage getFilterStage()
    {
        return currentFilterStage_;
    }

    public EvaluationResult extractValue(EvaluationContext context,
            ComponentName componentRootNode, RuntimeVariable runtimeVariable)
            throws EvaluationException
    {
        EvaluationResult _ret = null;

        final String _completePath = componentRootNode.getComponentName();

        if (logger_.isDebugEnabled())
        {
            logger_.debug("extractValue: " + _completePath);
            logger_.debug("runtimeVariable=" + runtimeVariable);
        }

        _ret = context.lookupResult(_completePath);

        if (_ret == null)
        {
            _ret = runtimeVariable.evaluate(context);

            _ret = context.extractFromMessage(_ret, componentRootNode, runtimeVariable);

            context.storeResult(_completePath, _ret);
        }

        if (_ret == null)
        {
            throw new EvaluationException("Could not resolve " + _completePath);
        }

        return _ret;
    }

    public abstract EvaluationResult extractFilterableData(EvaluationContext context,
            ComponentName componentRootNode, String variable) throws EvaluationException;

    public abstract EvaluationResult extractVariableHeader(EvaluationContext context,
            ComponentName componentRootNode, String variable) throws EvaluationException;

    public EvaluationResult extractValue(EvaluationContext evaluationContext,
            ComponentName componentRootNode) throws EvaluationException
    {
        EvaluationResult _ret = null;

        final String _completeExpr = componentRootNode.getComponentName();

        if (logger_.isDebugEnabled())
        {
            logger_.debug("extractValue path: " + componentRootNode.toStringTree()
                    + "\n\tcomplete Expression=" + _completeExpr);
        }

        // check if the value is available in the cache
        _ret = evaluationContext.lookupResult(_completeExpr);

        if (logger_.isDebugEnabled())
        {
            logger_.debug("Cache READ: " + _ret);
        }

        if (_ret == null)
        {
            logger_.debug("Cache MISS");

            _ret = evaluationContext.extractFromMessage(this, componentRootNode);

            // Cache the EvaluationResult
            if (_ret != null)
            {
                if (logger_.isDebugEnabled())
                {
                    logger_.debug("Cache WRITE: " + _completeExpr + " => " + _ret);
                }
                evaluationContext.storeResult(_completeExpr, _ret);
            }
        }

        if (_ret == null)
        {
            throw new EvaluationException("Could not resolve " + _completeExpr);
        }

        return _ret;
    }

    public Message getHandle()
    {
        return new MessageHandle();
    }

    public void initReceiveTimestamp()
    {
        receiveTimestamp_ = System.currentTimeMillis();
    }

    public abstract boolean hasStartTime();

    public abstract long getStartTime();

    public abstract boolean hasStopTime();

    public abstract long getStopTime();

    public abstract boolean hasTimeout();

    public abstract long getTimeout();

    public abstract int getPriority();

    public abstract boolean match(Filter filter) throws UnsupportedFilterableData;

    public boolean match(FilterStage filterStage)
    {
        final List _filterList = filterStage.getFilters();

        if (_filterList.isEmpty())
        {
            return true;
        }

        final Iterator _filterIterator = _filterList.iterator();

        while (_filterIterator.hasNext())
        {
            try
            {
                final Filter _filter = (Filter) _filterIterator.next();

                if (match(_filter))
                {
                    return true;
                }
            } catch (UnsupportedFilterableData e)
            {
                // no problem
                // error means false
                logger_.info("unsupported filterable data. match result defaults to false.", e);
            }
            catch (Exception e)
            {
                logger_.warn("unexpected error during match. match result defaults to false", e);
            }
        }

        return false;
    }

    public abstract boolean match(MappingFilter filter, AnyHolder value)
            throws UnsupportedFilterableData;

    /**
     * Provide a Uniform Mapping from domain_name and type_name to a Key that can be used to put
     * EventTypes into a Map. if (d1 == d2) AND (t1 == t2) => calcConstraintKey(d1, t1) ==
     * calcConstraintKey(d2, t2).
     *
     * @param domain_name
     *            a <code>String</code> value
     * @param type_name
     *            a <code>String</code> value
     * @return an Unique Constraint Key.
     */
    public static String calcConstraintKey(String domain_name, String type_name)
    {
        if ("".equals(domain_name))
        {
            domain_name = "*";
        }

        if ("".equals(type_name))
        {
            type_name = "*";
        }

        final StringBuffer _buffer = new StringBuffer(domain_name);

        // insert a magic string-seperator
        // has no meaning though
        _buffer.append("_%_");
        _buffer.append(type_name);

        return _buffer.toString();
    }
}