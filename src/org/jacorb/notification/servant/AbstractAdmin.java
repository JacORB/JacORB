package org.jacorb.notification.servant;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jacorb.notification.ChannelContext;
import org.jacorb.notification.EventChannelImpl;
import org.jacorb.notification.FilterManager;
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.ProxyEvent;
import org.jacorb.notification.interfaces.ProxyEventListener;
import org.jacorb.notification.util.QoSPropertySet;

import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.QoSAdminOperations;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.EventChannelHelper;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosNotifyChannelAdmin.ProxyNotFound;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterAdminOperations;
import org.omg.CosNotifyFilter.FilterNotFound;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;
import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.Logger;

/**
 * Abstract Baseclass for Adminobjects.
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AbstractAdmin
    implements QoSAdminOperations,
               FilterAdminOperations,
               FilterStage,
               ManageableServant,
               Configurable
{
    /**
     * the default InterFilterGroupOperator used.
     */
    protected static final InterFilterGroupOperator
        DEFAULT_FILTER_GROUP_OPERATOR = InterFilterGroupOperator.AND_OP;

    ////////////////////////////////////////

    protected OfferManager offerManager_;

    protected SubscriptionManager subscriptionManager_;

    protected Logger logger_ = null;

    protected final Object modifyProxiesLock_ = new Object();

    protected final Map pullServants_ = new HashMap();

    protected final Map pushServants_ = new HashMap();

    private ChannelContext channelContext_;

    private Integer id_;

    private boolean isIDPublic_;

    private POA poa_;

    private ORB orb_;

    private InterFilterGroupOperator filterGroupOperator_;

    private FilterManager filterManager_;

    private final SynchronizedInt proxyIdPool_ = new SynchronizedInt(0);

    private QoSPropertySet qosSettings_;

    private final SynchronizedBoolean disposed_ = new SynchronizedBoolean(false);

    private final List proxyEventListener_ = new ArrayList();

    /**
     * hook that is run during dispose
     */
    private Runnable disposeHook_;

    ////////////////////////////////////////

    protected AbstractAdmin() {
    }

    protected AbstractAdmin(ChannelContext channelContext)
    {
        channelContext_ = channelContext;

        filterManager_ =
            new FilterManager(channelContext_);

        setPOA(channelContext_.getPOA());

        setORB(channelContext_.getORB());

        configure( ( (org.jacorb.orb.ORB)channelContext_.getORB() ).getConfiguration() );
    }

    public void configure (Configuration conf)
    {
        logger_ = ((org.jacorb.config.Configuration)conf).
            getNamedLogger(getClass().getName());

        filterManager_.configure (conf);

        qosSettings_  = new QoSPropertySet(conf, QoSPropertySet.ADMIN_QOS);
    }

    ////////////////////////////////////////

    public ChannelContext getChannelContext() {
        return channelContext_;
    }


    public void setDisposeHook(Runnable disposeHook) {
        disposeHook_ = disposeHook;
    }


    public void setInterFilterGroupOperator(InterFilterGroupOperator op)
    {
        filterGroupOperator_ = op;
    }


    public void setID(Integer id)
    {
        id_ = id;
    }


    public void setIsIDPublic(boolean isIDPublic)
    {
        isIDPublic_ = isIDPublic;
    }


    protected POA getPOA()
    {
        return poa_;
    }


    public void setPOA(POA poa)
    {
        poa_ = poa;
    }


    protected ORB getORB()
    {
        return orb_;
    }


    public void setORB(ORB orb)
    {
        orb_ = orb;
    }


    protected MessageFactory getMessageFactory()
    {
        return channelContext_.getMessageFactory();
    }


    protected EventChannelImpl getChannelServant()
    {
        return channelContext_.getEventChannelServant();
    }


    private EventChannel getChannel()
    {
        return EventChannelHelper.narrow(getChannelServant().activate());
    }


    public POA _default_POA()
    {
        return getPOA();
    }


    int getProxyID()
    {
        return proxyIdPool_.increment();
    }


    public List getFilters()
    {
        return filterManager_.getFilters();
    }


    public int add_filter( Filter aFilter )
    {
        return filterManager_.add_filter( aFilter );
    }


    public void remove_filter( int aFilterId ) throws FilterNotFound
    {
        filterManager_.remove_filter( aFilterId );
    }


    public Filter get_filter( int aFilterId ) throws FilterNotFound
    {
        return filterManager_.get_filter( aFilterId );
    }


    public int[] get_all_filters()
    {
        return filterManager_.get_all_filters();
    }


    public void remove_all_filters()
    {
        filterManager_.remove_all_filters();
    }


    public InterFilterGroupOperator MyOperator()
    {
        return filterGroupOperator_;
    }


    public EventChannel MyChannel()
    {
        return getChannel();
    }


    public int MyID()
    {
        return getID().intValue();
    }


    public Property[] get_qos()
    {
        return qosSettings_.get_qos();
    }


    public void set_qos( Property[] props ) throws UnsupportedQoS
    {
        qosSettings_.validate_qos(props, new NamedPropertyRangeSeqHolder());

        qosSettings_.set_qos(props);
    }


    public void validate_qos( Property[] props,
                              NamedPropertyRangeSeqHolder propertyRangeSeqHolder )
        throws UnsupportedQoS
    {
        qosSettings_.validate_qos(props, propertyRangeSeqHolder);
    }


    public void destroy()
    {
        dispose();
    }


    private void checkDisposalStatus() throws OBJECT_NOT_EXIST {
        if (disposed_.get()) {
            throw new OBJECT_NOT_EXIST();
        }
        disposed_.set(true);
    }


    private void disposeProxies() {
        synchronized(modifyProxiesLock_) {
            logger_.debug("dispose PushServants");

            Iterator _i;
            _i = pushServants_.values().iterator();

            while ( _i.hasNext() )
                {
                    try
                        {
                            Disposable _d = (Disposable)_i.next();

                            _i.remove();

                            _d.dispose();
                        }
                    catch ( Exception e )
                        {
                            logger_.warn( "Error disposing a PushServant", e );
                        }
                }

            pushServants_.clear();

            //////////////////////////////

            logger_.debug("dispose PullServants");

            _i = pullServants_.values().iterator();

            while ( _i.hasNext() )
                {
                    try
                        {
                            Disposable _d = (Disposable)_i.next();

                            _i.remove();

                            _d.dispose();
                        }
                    catch ( Exception e )
                        {
                            logger_.warn( "Error disposing a PullServant", e );
                        }
                }

            pullServants_.clear();
        }
    }


    public void dispose()
    {
        checkDisposalStatus();

        //////////////////////////////

        deactivate();

        //////////////////////////////

        remove_all_filters();

        //////////////////////////////

        disposeProxies();

        //////////////////////////////

        disposeHook_.run();
    }


    public void deactivate() {
        logger_.debug( "deactivate Admin" );

        try
            {
                byte[] _oid = getPOA().servant_to_id( getServant() );
                getPOA().deactivate_object( _oid );
            }
        catch ( Exception e )
            {
                logger_.fatalError("Couldn't deactivate Admin", e);
            }
    }


    public abstract Servant getServant();


    public Integer getID()
    {
        return id_;
    }


    public boolean isDisposed()
    {
        return disposed_.get();
    }


    protected void fireCreateProxyRequestEvent() throws AdminLimitExceeded
    {
        synchronized( proxyEventListener_ ) {
            ProxyEvent _event =
                new ProxyEvent( this );

            Iterator _i = proxyEventListener_.iterator();

            while ( _i.hasNext() )
                {
                    ProxyEventListener _listener;
                    _listener = ( ProxyEventListener ) _i.next();
                    _listener.actionProxyCreationRequest( _event );
                }
        }
    }


    /**
     * admin does not have a lifetime filter
     */
    public boolean hasLifetimeFilter()
    {
        return false;
    }


    /**
     * admin does not have a priority filter
     */
    public boolean hasPriorityFilter()
    {
        return false;
    }


    /**
     * admin does not have a lifetime filter
     */
    public MappingFilter getLifetimeFilter()
    {
        throw new UnsupportedOperationException();
    }


    /**
     * admin does not have a priority filter
     */
    public MappingFilter getPriorityFilter()
    {
        throw new UnsupportedOperationException();
    }


    public void setOfferManager(OfferManager offerManager) {
        offerManager_ = offerManager;
    }


    public void setSubscriptionManager(SubscriptionManager subscriptionManager) {
        subscriptionManager_ = subscriptionManager;
    }


    public boolean hasInterFilterGroupOperatorOR()
    {
        return (filterGroupOperator_ != null &&
                (filterGroupOperator_.value() == InterFilterGroupOperator._OR_OP) );
    }


    /**
     * fetch the proxy specified by the provided id. this method will
     * not access an event style proxy.
     */
    protected AbstractProxy getProxy(int id) throws ProxyNotFound
    {
        Integer _id = new Integer(id);

        AbstractProxy _servant = null;

        synchronized (modifyProxiesLock_)
        {
            _servant = (AbstractProxy)pullServants_.get(_id);

            if (_servant == null)
            {
                _servant = (AbstractProxy)pullServants_.get(_id);
            }
        }

        if (_servant == null)
        {
            throw new ProxyNotFound("The proxy with ID=" + id + " does not exist");
        }

        if ( !_servant.isIDPublic() ) {
            throw new ProxyNotFound("The proxy with ID="
                                    + id
                                    + " is a EventStyle proxy and therefor not accessible");
        }

        return _servant;
    }


    /**
     * return the ID's for all NotifyStyle proxies stored in the
     * provided Map.
     */
    protected int[] get_all_notify_proxies(Map map, Object lock)
    {
        List _allIDsList = new ArrayList();

        synchronized (lock)
        {
            Iterator _i = map.entrySet().iterator();

            while ( _i.hasNext() )
            {
                Map.Entry _entry = (Map.Entry)_i.next();

                if ( ( (AbstractProxy)_entry.getValue() ).isIDPublic() ) {
                    _allIDsList.add(_entry.getKey());
                }

            }
        }

        int[] _allIDsArray = new int[_allIDsList.size()];

        for (int x=0; x<_allIDsArray.length; ++x) {
            _allIDsArray[x] = ((Integer)_allIDsList.get(x)).intValue();
        }

        return _allIDsArray;
    }


    /**
     * configure a event style proxy. the key is only for internal
     * use. especially the key cannot used to fetch the proxy via
     * get_proxy_consumer or get_proxy_supplier.
     */
    protected void configureEventStyleID(AbstractProxy proxy) {
        proxy.setID(new Integer(getProxyID()), false);

        proxy.setFilterManager( FilterManager.EMPTY_FILTER_MANAGER );
    }


    /**
     * configure the ID for a notify style proxy. the id is
     * public. the proxy can be accessed via a call to
     * get_proxy_consumer or get_proxy_supplier.
     */
    protected void configureNotifyStyleID(AbstractProxy proxy) {
        proxy.setID(new Integer(getProxyID()), true);
    }


    /**
     * configure initial QoS Settings for a proxy.
     */
    protected void configureQoS(AbstractProxy proxy)  {
        try {
            proxy.set_qos(qosSettings_.get_qos());
        } catch (UnsupportedQoS e) {
            logger_.fatalError("unexpected exception", e);

            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * configure the InterFilterGroupOperator a proxy should use.
     */
    protected void configureInterFilterGroupOperator(AbstractProxy proxy) {
        if ( filterGroupOperator_ != null &&
             (filterGroupOperator_.value() == InterFilterGroupOperator._OR_OP ) )
            {
                proxy.setInterFilterGroupOperatorOR( true );
            }
    }


    /**
     * configure OfferManager and SubscriptionManager for a proxy.
     */
    protected void configureManagers(AbstractProxy proxy) {
        proxy.setOfferManager(offerManager_);
        proxy.setSubscriptionManager(subscriptionManager_);
    }


    /**
     * satisfy method implementation
     */
    public void preActivate() {
    }


    public void addProxyEventListener( ProxyEventListener l )
    {
        synchronized(proxyEventListener_) {
            proxyEventListener_.add( l );
        }
    }


    public void removeProxyEventListener( ProxyEventListener listener )
    {
        synchronized(proxyEventListener_) {
            proxyEventListener_.remove( listener );
        }
    }


    private void fireProxyRemoved( AbstractProxy proxy )
    {
        synchronized(proxyEventListener_) {
            Iterator i = proxyEventListener_.iterator();
            ProxyEvent e = new ProxyEvent( proxy );

            while ( i.hasNext() )
            {
                ( ( ProxyEventListener ) i.next() ).actionProxyDisposed( e );
            }
        }
    }


    private void fireProxyCreated( AbstractProxy proxy )
    {
        synchronized(proxyEventListener_) {
            Iterator i = proxyEventListener_.iterator();
            ProxyEvent e = new ProxyEvent( proxy );

            while ( i.hasNext() )
                {
                    ( ( ProxyEventListener ) i.next() ).actionProxyCreated( e );
                }
        }
    }


    protected void addProxyToMap(final AbstractProxy proxy,
                                 final Map map,
                                 final Object lock) {
        synchronized(lock) {
            map.put(proxy.getID(), proxy);
            fireProxyCreated(proxy);
        }

        // this hook is run when proxy.dispose() is called.
        // it removes proxy from proxies again.
        proxy.setDisposeHook(new Runnable() {
                public void run() {
                    synchronized(lock) {
                        map.remove(proxy.getID());

                        fireProxyRemoved(proxy);
                    }
                }
            });
    }


    public final List getProxies() {
        List _list = new ArrayList();

        synchronized(modifyProxiesLock_) {
            _list.addAll(pullServants_.values());
            _list.addAll(pushServants_.values());
        }

        return _list;
    }
}
