package org.jacorb.notification.servant;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jacorb.notification.ChannelContext;
import org.jacorb.notification.FilterManager;
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.conf.Configuration;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.ProxyEvent;
import org.jacorb.notification.interfaces.ProxyEventListener;
import org.jacorb.util.Debug;
import org.jacorb.util.Environment;

import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.ORB;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.QoSAdminOperations;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterAdminOperations;
import org.omg.CosNotifyFilter.FilterNotFound;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;
import org.apache.avalon.framework.logger.Logger;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AbstractProxy
    implements FilterAdminOperations,
               QoSAdminOperations,
               FilterStage,
               Disposable,
               ManageableServant
{
    private Runnable disposeHook_;

    private SynchronizedInt errorCounter_ = new SynchronizedInt(0);

    private POA poa_;

    private ORB orb_;

    private TaskProcessor taskProcessor_;

    protected final static Integer NO_KEY = null;

    protected boolean isKeyPublic_;

    protected Logger logger_ = Debug.getNamedLogger(getClass().getName());

    protected List proxyDisposedEventListener_;

    protected MessageFactory messageFactory_;

    protected boolean connected_;

    protected QoSPropertySet qosSettings_ =
        new QoSPropertySet(QoSPropertySet.PROXY_QOS);

    protected Integer key_;

    protected AbstractAdmin myAdmin_;

    protected OfferManager offerManager_;

    protected SubscriptionManager subscriptionManager_;

    /**
     * delegate for FilterAdminOperations
     */
    protected FilterManager filterManager_;

    protected boolean disposed_ = false;

    private ProxyType proxyType_;

    private boolean isInterFilterGroupOperatorOR_;

    protected Servant thisServant_;

    protected MappingFilter lifetimeFilter_;

    protected MappingFilter priorityFilter_;

    private boolean disposedProxyDisconnectsClient_;

    ////////////////////////////////////////

    AbstractProxy(AbstractAdmin admin,
                  ChannelContext channelContext)
    {
        myAdmin_ = admin;

        connected_ = false;

        messageFactory_ =
            channelContext.getMessageFactory();

        filterManager_ = new FilterManager(channelContext);

        setPOA(channelContext.getPOA());

        setORB(channelContext.getORB());

        setTaskProcessor(channelContext.getTaskProcessor());

        disposedProxyDisconnectsClient_ =
            Environment.isPropertyOn(Configuration.DISPOSE_PROXY_CALLS_DISCONNECT,
                                     Default.DEFAULT_DISPOSE_PROXY_CALLS_DISCONNECT);
    }

    ////////////////////////////////////////

    public void setOfferManager(OfferManager m) {
        offerManager_ = m;
    }


    public void setSubscriptionManager(SubscriptionManager m) {
        subscriptionManager_ = m;
    }


    public void setDisposeHook(Runnable hook) {
        disposeHook_ = hook;
    }


    public void setKey(Integer key, boolean isKeyPublic)
    {
        key_ = key;
        isKeyPublic_ = isKeyPublic;
    }


    public boolean isKeyPublic()
    {
        return isKeyPublic_;
    }


    public void setPOA(POA poa)
    {
        poa_ = poa;
    }


    protected POA getPOA()
    {
        return poa_;
    }


    public void setORB(ORB orb)
    {
        orb_ = orb;
    }


    protected ORB getORB()
    {
        return orb_;
    }


    public void setTaskProcessor(TaskProcessor tp)
    {
        taskProcessor_ = tp;
    }


    protected TaskProcessor getTaskProcessor()
    {
        return taskProcessor_;
    }


    public synchronized void addProxyDisposedEventListener(ProxyEventListener listener)
    {
        if (proxyDisposedEventListener_ == null)
        {
            proxyDisposedEventListener_ = new ArrayList();
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

    //////////////////////////////////////////////////////
    // delegate FilterAdmin Operations to FilterManager //
    //////////////////////////////////////////////////////

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


    public void remove_all_filters()
    {
        filterManager_.remove_all_filters();
    }

    ////////////////////////////////////////

    public void validate_event_qos(Property[] qosProps,
                                   NamedPropertyRangeSeqHolder propSeqHolder)
        throws UnsupportedQoS
    {
        throw new NO_IMPLEMENT();
    }


    public void validate_qos(Property[] props,
                             NamedPropertyRangeSeqHolder propertyRange)
        throws UnsupportedQoS
    {
        qosSettings_.validate_qos(props, propertyRange);
    }


    public void set_qos(Property[] qosProps) throws UnsupportedQoS
    {
        qosSettings_.set_qos(qosProps);
    }


    public Property[] get_qos()
    {
        return qosSettings_.get_qos();
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
    public final POA _default_POA()
    {
        return getPOA();
    }


    void setFilterManager(FilterManager manager)
    {
        filterManager_ = manager;
    }


    public List getFilters()
    {
        return filterManager_.getFilters();
    }


    public void deactivate()  {
        logger_.info("deactivate_object");

        try
            {
                byte[] _oid = getPOA().servant_to_id(getServant());
                getPOA().deactivate_object(_oid);
            }
        catch (Exception e)
            {
                logger_.fatalError("Couldn't deactivate Object", e);
            }
    }


    public void dispose()
    {
        synchronized (this)
        {
            if (!disposed_)
            {
                disposed_ = true;
            }
            else
            {
                throw new OBJECT_NOT_EXIST();
            }
        }

        //////////////////////////////

        deactivate();

        //////////////////////////////

        if (disposeHook_ != null) {
            disposeHook_.run();
        }

        //////////////////////////////

        remove_all_filters();

        //////////////////////////////

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

        //////////////////////////////

        if (disposedProxyDisconnectsClient_) {
            try
                {
                    disconnectClient();
                }
            catch (Throwable e)
                {
                    logger_.error("error disconnecting client", e);
                }
        }
    }


    protected void setProxyType(ProxyType p)
    {
        proxyType_ = p;
    }


    public final ProxyType MyType()
    {
        return proxyType_;
    }


    void setInterFilterGroupOperatorOR(boolean b)
    {
        isInterFilterGroupOperatorOR_ = b;
    }


    public boolean hasInterFilterGroupOperatorOR()
    {
        return isInterFilterGroupOperatorOR_;
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


    protected void checkConnected() throws Disconnected
    {
        if ( !connected_ )
        {
            throw new Disconnected();
        }
    }


    public void preActivate() throws Exception
    {
        // NO Op
    }

    protected abstract void disconnectClient();


    abstract Servant getServant();
}
