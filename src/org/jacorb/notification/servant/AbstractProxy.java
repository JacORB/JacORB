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
import org.jacorb.util.Debug;
import org.jacorb.util.Environment;

import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.QoSAdminOperations;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.NotConnected;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterAdminOperations;
import org.omg.CosNotifyFilter.FilterNotFound;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;
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
    protected boolean isIDPublic_;

    protected Logger logger_ = Debug.getNamedLogger(getClass().getName());

    protected MessageFactory messageFactory_;

    private SynchronizedBoolean connected_ = new SynchronizedBoolean(false);

    protected QoSPropertySet qosSettings_ =
        new QoSPropertySet(QoSPropertySet.PROXY_QOS);

    protected Integer id_;

    protected AbstractAdmin admin_;

    protected OfferManager offerManager_;

    protected SubscriptionManager subscriptionManager_;

    protected Servant thisServant_;

    protected MappingFilter lifetimeFilter_;

    protected MappingFilter priorityFilter_;

    /**
    * delegate for FilterAdminOperations
    */
    private FilterManager filterManager_;

    private SynchronizedBoolean disposed_ = new SynchronizedBoolean(false);

    private Runnable disposeHook_;

    private SynchronizedInt errorCounter_ = new SynchronizedInt(0);

    private POA poa_;

    private ORB orb_;

    private TaskProcessor taskProcessor_;

    private ProxyType proxyType_;

    private boolean isInterFilterGroupOperatorOR_;

    private boolean disposedProxyDisconnectsClient_;

    ////////////////////////////////////////

    AbstractProxy(AbstractAdmin admin,
                  ChannelContext channelContext)
    {
        admin_ = admin;

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

    public void setOfferManager(OfferManager m)
    {
        offerManager_ = m;
    }


    public void setSubscriptionManager(SubscriptionManager m)
    {
        subscriptionManager_ = m;
    }


    public void setDisposeHook(Runnable hook)
    {
        disposeHook_ = hook;
    }


    public void setKey(Integer key, boolean isKeyPublic)
    {
        id_ = key;
        isIDPublic_ = isKeyPublic;
    }


    public boolean isKeyPublic()
    {
        return isIDPublic_;
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
        return id_;
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


    public void deactivate()
    {
        logger_.info("deactivate Proxy");

        try
        {
            byte[] _oid = getPOA().servant_to_id(getServant());
            getPOA().deactivate_object(_oid);
        }
        catch (Exception e)
        {
            logger_.fatalError("Couldn't deactivate Proxy", e);
        }
    }


    private void tryDisconnectClient()
    {
        try {
            if (disposedProxyDisconnectsClient_ && isConnected() )
                {
                    logger_.info("disconnect_client");

                    disconnectClient();
                }
        } catch (Exception e) {
            logger_.error("try to disconnect client: unexpected error", e);
        } finally {
            connected_.set(false);
        }
    }


    private void checkDisposalStatus() throws OBJECT_NOT_EXIST
    {
        if (disposed_.get())
        {
            logger_.fatalError("dispose has been called twice");

            throw new OBJECT_NOT_EXIST();
        }
        disposed_.set(true);
    }


    public void dispose()
    {
        checkDisposalStatus();

        //////////////////////////////

        tryDisconnectClient();

        //////////////////////////////

        deactivate();

        //////////////////////////////

        remove_all_filters();

        //////////////////////////////

        disposeHook_.run();
    }


    protected void setProxyType(ProxyType proxyType)
    {
        proxyType_ = proxyType;
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
        return disposed_.get();
    }


    public boolean isConnected()
    {
        return connected_.get();
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


    protected void assertConnected() throws NotConnected
    {
        if ( !connected_.get() )
        {
            throw new NotConnected();
        }
    }


    protected void assertNotConnected() throws AlreadyConnected
    {
        if (connected_.get())
        {
            throw new AlreadyConnected();
        }
    }


    protected void connectClient(org.omg.CORBA.Object client) {
        connected_.set(true);
    }


    public void preActivate() throws Exception
    {
        // NO Op
    }


    protected abstract void disconnectClient();


    abstract Servant getServant();
}
