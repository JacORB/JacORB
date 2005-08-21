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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.FilterManager;
import org.jacorb.notification.IContainer;
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.container.PicoContainerFactory;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.JMXManageable;
import org.jacorb.notification.interfaces.ProxyEvent;
import org.jacorb.notification.interfaces.ProxyEventListener;
import org.jacorb.notification.util.DisposableManager;
import org.jacorb.notification.util.QoSPropertySet;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.QoSAdminOperations;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosNotifyChannelAdmin.ProxyNotFound;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterAdminOperations;
import org.omg.CosNotifyFilter.FilterNotFound;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.picocontainer.MutablePicoContainer;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;
import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;

/**
 * Abstract Baseclass for Adminobjects.
 * 
 * @jmx.mbean
 * @jboss.xmbean 
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AbstractAdmin implements QoSAdminOperations,
        FilterAdminOperations, FilterStage, ManageableServant, JMXManageable
{
    private static final class ITypedAdminImpl implements ITypedAdmin
    {
        private final IAdmin admin_;

        private final MutablePicoContainer container_;

        private final String supportedInterface_;

        private ITypedAdminImpl(IAdmin admin, MutablePicoContainer container,
                String supportedInterface)
        {
            super();
            
            admin_ = admin;
            container_ = container;
            supportedInterface_ = supportedInterface;
        }

        public String getSupportedInterface()
        {
            return supportedInterface_;
        }

        public int getProxyID()
        {
            return admin_.getProxyID();
        }

        public boolean isIDPublic()
        {
            return admin_.isIDPublic();
        }

        public MutablePicoContainer getContainer()
        {
            return admin_.getContainer();
        }

        public void destroy()
        {
            container_.unregisterComponent(ITypedAdmin.class);
            admin_.destroy();
        }

        public String getAdminMBean()
        {
            return admin_.getAdminMBean();
        }
    }

    /**
     * the default InterFilterGroupOperator used.
     */
    protected static final InterFilterGroupOperator DEFAULT_FILTER_GROUP_OPERATOR = InterFilterGroupOperator.AND_OP;

    ////////////////////////////////////////

    private final DisposableManager disposables_ = new DisposableManager();

    private final Integer id_;

    private InterFilterGroupOperator filterGroupOperator_;

    protected final MutablePicoContainer container_;

    private final FilterManager filterManager_;

    private final WeakReference eventChannelReference_;

    private final QoSPropertySet qosSettings_;

    private final MessageFactory messageFactory_;

    protected final OfferManager offerManager_;

    protected final SubscriptionManager subscriptionManager_;

    protected final Logger logger_;

    private final ORB orb_;

    private final POA poa_;

    protected final Object modifyProxiesLock_ = new Object();

    protected final Map pullServants_ = new HashMap();

    protected final Map pushServants_ = new HashMap();

    private final SynchronizedInt proxyIdPool_ = new SynchronizedInt(0);

    private final SynchronizedBoolean disposed_ = new SynchronizedBoolean(false);

    private final List proxyEventListener_ = new ArrayList();

    public final int channelID_;

    private final String parentMBean_;

    private JMXManageable.JMXCallback jmxCallback_;

    ////////////////////////////////////////

    protected AbstractAdmin(IEventChannel channel, ORB orb, POA poa, Configuration config,
            MessageFactory messageFactory, OfferManager offerManager,
            SubscriptionManager subscriptionManager)
    {
        parentMBean_ = channel.getChannelMBean();
        
        container_ = channel.getContainer();

        id_ = new Integer(channel.getAdminID());

        orb_ = orb;
        poa_ = poa;
        messageFactory_ = messageFactory;
        filterManager_ = new FilterManager();

        eventChannelReference_ = new WeakReference(channel.getEventChannel());

        channelID_ = channel.getChannelID();

        logger_ = ((org.jacorb.config.Configuration) config).getNamedLogger(getClass().getName());

        qosSettings_ = new QoSPropertySet(config, QoSPropertySet.ADMIN_QOS);

        offerManager_ = offerManager;

        subscriptionManager_ = subscriptionManager;
    }

    public final void registerDisposable(Disposable d)
    {
        disposables_.addDisposable(d);
    }

    public void setInterFilterGroupOperator(InterFilterGroupOperator op)
    {
        filterGroupOperator_ = op;
    }

    protected POA getPOA()
    {
        return poa_;
    }

    protected ORB getORB()
    {
        return orb_;
    }

    protected MessageFactory getMessageFactory()
    {
        return messageFactory_;
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

    public int add_filter(Filter aFilter)
    {
        return filterManager_.add_filter(aFilter);
    }

    public void remove_filter(int aFilterId) throws FilterNotFound
    {
        filterManager_.remove_filter(aFilterId);
    }

    public Filter get_filter(int aFilterId) throws FilterNotFound
    {
        return filterManager_.get_filter(aFilterId);
    }

    public int[] get_all_filters()
    {
        return filterManager_.get_all_filters();
    }

    public void remove_all_filters()
    {
        filterManager_.remove_all_filters();
    }

    public final InterFilterGroupOperator MyOperator()
    {
        return filterGroupOperator_;
    }

    public final EventChannel MyChannel()
    {
        return (EventChannel) eventChannelReference_.get();
    }

    public final int MyID()
    {
        return getID().intValue();
    }

    public final int getChannelID()
    {
        return channelID_;
    }
    
    public Property[] get_qos()
    {
        return qosSettings_.get_qos();
    }

    public void set_qos(Property[] props) throws UnsupportedQoS
    {
        qosSettings_.validate_qos(props, new NamedPropertyRangeSeqHolder());

        qosSettings_.set_qos(props);
    }

    public void validate_qos(Property[] props, NamedPropertyRangeSeqHolder propertyRangeSeqHolder)
            throws UnsupportedQoS
    {
        qosSettings_.validate_qos(props, propertyRangeSeqHolder);
    }

    /**
     * @jmx.managed-operation description = "Destroy this Admin" impact = "ACTION"
     */
    public final void destroy()
    {
        checkDestroyStatus();

        container_.dispose();
        
        List list = container_.getComponentInstancesOfType(IContainer.class);
        
        for (Iterator i = list.iterator(); i.hasNext();)
        {
            IContainer element = (IContainer) i.next();
            element.destroy();
        }
    }

    private void checkDestroyStatus() throws OBJECT_NOT_EXIST
    {
        if (!disposed_.commit(false, true))
        {
            throw new OBJECT_NOT_EXIST();
        }
    }

    public void dispose()
    {
        logger_.info("destroy Admin " + MyID());

        //////////////////////////////

        deactivate();

        //////////////////////////////

        remove_all_filters();

        //////////////////////////////

        disposables_.dispose();

        proxyEventListener_.clear();
    }

    public void deactivate()
    {
        if (logger_.isDebugEnabled())
        {
            logger_.debug("deactivate Admin: " + getID());
        }

        try
        {
            byte[] _oid = getPOA().servant_to_id(getServant());
            getPOA().deactivate_object(_oid);
        } catch (Exception e)
        {
            logger_.error("Couldn't deactivate Admin", e);
        }
    }

    public abstract Servant getServant();

    public Integer getID()
    {
        return id_;
    }

    public boolean isDestroyed()
    {
        return disposed_.get();
    }

    protected void fireCreateProxyRequestEvent() throws AdminLimitExceeded
    {
        synchronized (proxyEventListener_)
        {
            ProxyEvent _event = new ProxyEvent(this);

            Iterator _i = proxyEventListener_.iterator();

            while (_i.hasNext())
            {
                ProxyEventListener _listener;
                _listener = (ProxyEventListener) _i.next();
                _listener.actionProxyCreationRequest(_event);
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

    public boolean hasInterFilterGroupOperatorOR()
    {
        return (filterGroupOperator_ != null && (filterGroupOperator_.value() == InterFilterGroupOperator._OR_OP));
    }

    /**
     * fetch the proxy specified by the provided id. this method will not access an event style
     * proxy.
     */
    protected AbstractProxy getProxy(int id) throws ProxyNotFound
    {
        Integer _id = new Integer(id);

        AbstractProxy _servant = null;

        synchronized (modifyProxiesLock_)
        {
            _servant = (AbstractProxy) pullServants_.get(_id);

            if (_servant == null)
            {
                _servant = (AbstractProxy) pushServants_.get(_id);
            }
        }

        if (_servant == null)
        {
            throw new ProxyNotFound("The proxy with ID=" + id + " does not exist");
        }

        if (!_servant.isIDPublic())
        {
            throw new ProxyNotFound("The proxy with ID=" + id
                    + " is a EventStyle proxy and therefor not accessible by ID");
        }

        return _servant;
    }

    /**
     * return the ID's for all NotifyStyle proxies stored in the provided Map.
     */
    protected int[] get_all_notify_proxies(Map map, Object lock)
    {
        List _allIDsList = new ArrayList();

        synchronized (lock)
        {
            Iterator _i = map.entrySet().iterator();

            while (_i.hasNext())
            {
                Map.Entry _entry = (Map.Entry) _i.next();

                if (((AbstractProxy) _entry.getValue()).isIDPublic())
                {
                    _allIDsList.add(_entry.getKey());
                }
            }
        }

        int[] _allIDsArray = new int[_allIDsList.size()];

        for (int x = 0; x < _allIDsArray.length; ++x)
        {
            _allIDsArray[x] = ((Integer) _allIDsList.get(x)).intValue();
        }

        return _allIDsArray;
    }

    /**
     * configure initial QoS Settings for a proxy.
     */
    protected void configureQoS(AbstractProxy proxy) throws UnsupportedQoS
    {
        proxy.set_qos(qosSettings_.get_qos());
    }

    /**
     * configure the InterFilterGroupOperator a proxy should use.
     */
    protected void configureInterFilterGroupOperator(AbstractProxy proxy)
    {
        if (filterGroupOperator_ != null
                && (filterGroupOperator_.value() == InterFilterGroupOperator._OR_OP))
        {
            proxy.setInterFilterGroupOperatorOR(true);
        }
    }

    public void addProxyEventListener(ProxyEventListener l)
    {
        synchronized (proxyEventListener_)
        {
            proxyEventListener_.add(l);
        }
    }

    public void removeProxyEventListener(ProxyEventListener listener)
    {
        synchronized (proxyEventListener_)
        {
            proxyEventListener_.remove(listener);
        }
    }

    void fireProxyRemoved(AbstractProxy proxy)
    {
        synchronized (proxyEventListener_)
        {
            Iterator i = proxyEventListener_.iterator();
            ProxyEvent e = new ProxyEvent(proxy);

            while (i.hasNext())
            {
                ((ProxyEventListener) i.next()).actionProxyDisposed(e);
            }
        }
    }

    private void fireProxyCreated(AbstractProxy proxy)
    {
        synchronized (proxyEventListener_)
        {
            Iterator i = proxyEventListener_.iterator();
            ProxyEvent e = new ProxyEvent(proxy);

            while (i.hasNext())
            {
                ((ProxyEventListener) i.next()).actionProxyCreated(e);
            }
        }
    }

    protected void addProxyToMap(final AbstractProxy proxy, final Map map, final Object lock)
    {
        synchronized (lock)
        {
            map.put(proxy.getID(), proxy);
            fireProxyCreated(proxy);
        }

        // this hook is run when proxy.dispose() is called.
        // it removes proxy from map again.
        proxy.registerDisposable(new Disposable()
        {
            public void dispose()
            {
                synchronized (lock)
                {
                    map.remove(proxy.getID());

                    fireProxyRemoved(proxy);
                }
            }
        });
        
    }

    public final List getProxies()
    {
        List _list = new ArrayList();

        synchronized (modifyProxiesLock_)
        {
            _list.addAll(pullServants_.values());
            _list.addAll(pushServants_.values());
        }

        return _list;
    }

    protected MutablePicoContainer newContainerForNotifyStyleProxy()
    {
        return newContainerForProxy(true);
    }

    protected MutablePicoContainer newContainerForEventStyleProxy()
    {
        return newContainerForProxy(false);
    }

    protected MutablePicoContainer newContainerForTypedProxy(final String supportedInterface)
    {
        final MutablePicoContainer _container = newContainerForNotifyStyleProxy();

        final IAdmin _admin = (IAdmin) _container.getComponentInstanceOfType(IAdmin.class);

        ITypedAdmin _typedAdmin = new ITypedAdminImpl(_admin, _container, supportedInterface);

        _container.registerComponentInstance(ITypedAdmin.class, _typedAdmin);

        return _container;
    }

    private MutablePicoContainer newContainerForProxy(boolean isIDPublic)
    {
        int proxyID = getProxyID();

        return newContainerForProxy(proxyID, isIDPublic);
    }

    private MutablePicoContainer newContainerForProxy(final int proxyID, final boolean isIDPublic)
    {
        final MutablePicoContainer _containerForProxy = PicoContainerFactory
                .createChildContainer(container_);

        final IAdmin _admin = new IAdmin()
        {
            public MutablePicoContainer getContainer()
            {
                return _containerForProxy;
            }

            public int getProxyID()
            {
                return proxyID;
            }

            public boolean isIDPublic()
            {
                return isIDPublic;
            }

            public void destroy()
            {
                container_.removeChildContainer(_containerForProxy);
            }

            public String getAdminMBean()
            {
                return getJMXObjectName();
            }
        };

        _containerForProxy.registerComponentInstance(IAdmin.class, _admin);

        return _containerForProxy;
    }
    
    public final String getJMXObjectName()
    {
        return "admin=" + getMBeanName() + ", " + parentMBean_;
    }

    public final String getMBeanName()
    {
        return getMBeanType() + "-" + getID();
    } 
    
    abstract protected String getMBeanType();
    
    public String[] getJMXNotificationTypes()
    {
        return new String[0];
    }
    
    public final void setJMXCallback(JMXManageable.JMXCallback callback)
    {
        jmxCallback_ = callback;
    }
    
    protected final void sendNotification(String type, String message)
    {
        if (jmxCallback_ != null)
        {
            jmxCallback_.sendJMXNotification(type, message);
        }
    }
}