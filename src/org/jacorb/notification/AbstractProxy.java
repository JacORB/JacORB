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

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.ProxyEvent;
import org.jacorb.notification.interfaces.ProxyEventListener;
import org.jacorb.util.Debug;

import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.QoSAdminOperations;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.ObtainInfoMode;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.NotifyPublishOperations;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterAdminOperations;
import org.omg.CosNotifyFilter.FilterNotFound;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;
import org.apache.avalon.framework.logger.Logger;

/**
 * ProxyBase.java
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AbstractProxy implements FilterAdminOperations,
            NotifyPublishOperations,
            QoSAdminOperations,
            FilterStage,
            Disposable
{

    private SynchronizedInt errorCounter_ = new SynchronizedInt(0);

    protected final static Integer NO_KEY = null;

    protected Logger logger_ = Debug.getNamedLogger(getClass().getName());

    protected List proxyDisposedEventListener_;
    protected MessageFactory notificationEventFactory_;
    protected boolean connected_;
    protected ChannelContext channelContext_;
    protected ApplicationContext applicationContext_;
    protected Integer key_;
    protected AbstractAdmin myAdmin_;
    protected FilterManager filterManager_;
    protected boolean disposed_ = false;
    protected PropertyManager adminProperties_;
    protected PropertyManager qosProperties_;
    private ProxyType proxyType_;
    private boolean hasOrSemantic_;
    protected Servant thisServant_;
    protected MappingFilter lifetimeFilter_;
    protected MappingFilter priorityFilter_;

    protected AbstractProxy(AbstractAdmin admin,
                            ApplicationContext appContext,
                            ChannelContext channelContext,
                            PropertyManager adminProperties,
                            PropertyManager qosProperties)
    {
        this(admin,
             appContext,
             channelContext,
             adminProperties,
             qosProperties,
             NO_KEY);
    }

    protected AbstractProxy(AbstractAdmin admin,
                            ApplicationContext appContext,
                            ChannelContext channelContext,
                            PropertyManager adminProperties,
                            PropertyManager qosProperties,
                            Integer key)
    {
        myAdmin_ = admin;
        key_ = key;
        adminProperties_ = adminProperties;
        qosProperties_ = qosProperties;
        applicationContext_ = appContext;
        channelContext_ = channelContext;
        connected_ = false;

        notificationEventFactory_ =
            applicationContext_.getMessageFactory();

        filterManager_ = new FilterManager(applicationContext_);
    }

    abstract public Servant getServant();

    public synchronized void addProxyDisposedEventListener(ProxyEventListener listener)
    {
        if (proxyDisposedEventListener_ == null)
            {
                proxyDisposedEventListener_ = new Vector();
            }

        proxyDisposedEventListener_.add(listener);
    }

    public void removeProxyDisposedEventListener(ProxyEventListener listener)
    {
        if (proxyDisposedEventListener_ != null)
        {
            proxyDisposedEventListener_.remove(listener);
        }
    }

    public int add_filter(Filter filter)
    {
        return filterManager_.add_filter(filter);
    }

    public void remove_filter(int n) throws FilterNotFound
    {
        filterManager_.remove_filter(n);
    }

    public Filter get_filter(int n) throws FilterNotFound
    {
        return filterManager_.get_filter(n);
    }

    public int[] get_all_filters()
    {
        return filterManager_.get_all_filters();
    }

    synchronized public void remove_all_filters()
    {
        filterManager_.remove_all_filters();
    }

    public EventType[] obtain_subscription_types(ObtainInfoMode obtainInfoMode)
    {
        throw new NO_IMPLEMENT();
    }

    public void validate_event_qos(Property[] qosProps,
                                   NamedPropertyRangeSeqHolder propSeqHolder)
        throws UnsupportedQoS
    {
        throw new NO_IMPLEMENT();
    }

    public void validate_qos(Property[] qosProps,
                             NamedPropertyRangeSeqHolder propSeqHolder)
    throws UnsupportedQoS
    {
        throw new NO_IMPLEMENT();
    }

    public void set_qos(Property[] qosProps) throws UnsupportedQoS
    {
        throw new NO_IMPLEMENT();
    }

    public Property[] get_qos()
    {
        return qosProperties_.toArray();
    }

    public void offer_change(EventType[] eventTypes,
                             EventType[] eventTypes2)
        throws InvalidEventType
    {
        throw new NO_IMPLEMENT();
    }

    public void subscription_change(EventType[] eventType,
                                    EventType[] eventType2)
        throws InvalidEventType
    {
        throw new NO_IMPLEMENT();
    }

    public void priority_filter(MappingFilter filter)
    {
        priorityFilter_ = filter;
    }

    public MappingFilter priority_filter()
    {
        return priorityFilter_;
    }

    public MappingFilter lifetime_filter()
    {
        return lifetimeFilter_;
    }

    public void lifetime_filter(MappingFilter filter)
    {
        lifetimeFilter_ = filter;
    }

    public EventType[] obtain_offered_types(ObtainInfoMode obtaininfomode)
    {
        throw new NO_IMPLEMENT();
    }

    public Integer getKey()
    {
        return key_;
    }

    /**
     * Override this method from the Servant baseclass.  Fintan Bolton
     * in his book "Pure CORBA" suggests that you override this method to
     * avoid the risk that a servant object (like this one) could be
     * activated by the <b>wrong</b> POA object.
     */
    public POA _default_POA()
    {
        return applicationContext_.getPoa();
    }

    void setFilterManager(FilterManager manager)
    {
        filterManager_ = manager;
    }

    public List getFilters()
    {
        return filterManager_.getFilters();
    }

    public void dispose()
    {
        if (logger_.isDebugEnabled())
        {
            logger_.debug("dispose()");
        }

        if (!disposed_)
        {
            remove_all_filters();
            disposed_ = true;
            Iterator _i;

            if (proxyDisposedEventListener_ != null)
            {
                _i = proxyDisposedEventListener_.iterator();
                ProxyEvent _event = new ProxyEvent(this);
                while (_i.hasNext())
                {

                    ProxyEventListener _listener =
                        (ProxyEventListener)_i.next();

                    _listener.actionProxyDisposed(_event);
                }
            }
            try
            {
                byte[] _oid = applicationContext_.getPoa().servant_to_id(getServant());
                applicationContext_.getPoa().deactivate_object(_oid);
            }
            catch (Exception e)
            {
                logger_.fatalError("Couldnt deactivate Object", e);
            }
        }
    }

    protected void setProxyType(ProxyType p)
    {
        proxyType_ = p;
    }

    public ProxyType MyType()
    {
        return proxyType_;
    }

    void setOrSemantic(boolean b)
    {
        hasOrSemantic_ = b;
    }

    public boolean hasOrSemantic()
    {
        return hasOrSemantic_;
    }

    public boolean isDisposed()
    {
        return disposed_;
    }

    public boolean isConnected()
    {
        return connected_;
    }

    public boolean hasLifetimeFilter()
    {
        return lifetimeFilter_ != null;
    }

    public boolean hasPriorityFilter()
    {
        return priorityFilter_ != null;
    }

    public MappingFilter getLifetimeFilter()
    {
        return lifetimeFilter_;
    }

    public MappingFilter getPriorityFilter()
    {
        return priorityFilter_;
    }

    public void resetErrorCounter()
    {
        errorCounter_.set(0);
    }

    public int getErrorCounter()
    {
        return errorCounter_.get();
    }

    public int incErrorCounter()
    {
        return errorCounter_.increment();
    }

}
