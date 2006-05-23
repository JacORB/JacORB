package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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
 */

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.FilterStageSource;
import org.jacorb.notification.interfaces.JMXManageable;
import org.jacorb.notification.interfaces.ProxyEvent;
import org.jacorb.notification.interfaces.ProxyEventAdapter;
import org.jacorb.notification.interfaces.ProxyEventListener;
import org.jacorb.notification.lifecycle.IServantLifecyle;
import org.jacorb.notification.lifecycle.ServantLifecyleControl;
import org.jacorb.notification.servant.AbstractAdmin;
import org.jacorb.notification.servant.AbstractSupplierAdmin;
import org.jacorb.notification.servant.FilterStageListManager;
import org.jacorb.notification.util.AdminPropertySet;
import org.jacorb.notification.util.DisposableManager;
import org.jacorb.notification.util.PropertySet;
import org.jacorb.notification.util.QoSPropertySet;
import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.EventReliability;
import org.omg.CosNotification.MaxConsumers;
import org.omg.CosNotification.MaxSuppliers;
import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.UnsupportedAdmin;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.AdminLimit;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.AdminNotFound;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.PortableServer.POA;
import org.picocontainer.MutablePicoContainer;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicInteger;

/**
 * @jmx.mbean
 * @jboss.xmbean
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AbstractEventChannel implements IServantLifecyle, JMXManageable
{
    /**
     * This key is reserved for the default supplier admin and the default consumer admin.
     */
    private static final Integer DEFAULT_ADMIN_KEY = new Integer(0);

    private final DisposableManager disposables_ = new DisposableManager();

    protected final Logger logger_;

    protected final ORB orb_;

    private final POA poa_;

    private final Configuration configuration_;

    /**
     * max number of Suppliers that may be connected at a time to this Channel (0=unlimited)
     */
    private final AtomicInteger maxNumberOfSuppliers_ = new AtomicInteger(0);

    /**
     * max number of Consumers that may be connected at a time to this Channel (0=unlimited)
     */
    private final AtomicInteger maxNumberOfConsumers_ = new AtomicInteger(0);

    private final AdminPropertySet adminSettings_;

    private final QoSPropertySet qosSettings_;

    private final FilterStageListManager listManager_;

    private final FilterFactory defaultFilterFactory_;

    /**
     * lock variable used to access allConsumerAdmins_ and consumerAdminServants_.
     */
    private final Object modifyConsumerAdminsLock_ = new Object();

    /**
     * lock variable used to access allConsumerAdmins_.
     */
    private final Object modifySupplierAdminsLock_ = new Object();

    /**
     * maps id's to ConsumerAdminServants (notify style).
     */
    private final Map consumerAdminServants_ = new HashMap();

    /**
     * maps id's to SupplierAdminServants (notify style).
     */
    private final Map supplierAdminServants_ = new HashMap();

    /**
     * pool of available ID's for Admin Objects. The Pool is used for Consumer and Supplier Admins.
     * NOTE: The least available ID is 1 as the ID 0 has a special meaning.
     *
     * @see #DEFAULT_ADMIN_KEY DEFAULT_ADMIN_KEY.
     */
    private final AtomicInteger adminIdPool_ = new AtomicInteger(1);

    /**
     * number of Consumers that are connected to this Channel
     */
    private final AtomicInteger numberOfConsumers_ = new AtomicInteger(0);

    /**
     * number of Suppliers that are connected to this Channel
     */
    private final AtomicInteger numberOfSuppliers_ = new AtomicInteger(0);

    private final ProxyEventListener proxyConsumerEventListener_ = new ProxyEventAdapter()
    {
        public void actionProxyCreationRequest(ProxyEvent event) throws AdminLimitExceeded
        {
            addConsumer();
        }

        public void actionProxyDisposed(ProxyEvent event)
        {
            removeConsumer();
        }
    };

    private final ProxyEventListener proxySupplierEventListener_ = new ProxyEventAdapter()
    {
            public void actionProxyCreationRequest(ProxyEvent event) throws AdminLimitExceeded
        {
            addSupplier();
        }

        public void actionProxyDisposed(ProxyEvent event)
        {
            removeSupplier();
        }
    };

    protected final MutablePicoContainer container_;

    private final int id_;

    private final AtomicBoolean destroyed_ = new AtomicBoolean(false);

    protected JMXManageable.JMXCallback jmxCallback_;

    private final ServantLifecyleControl servantLifecyle_;

    ////////////////////////////////////////

    public AbstractEventChannel(IFactory factory, ORB orb, POA poa, Configuration config,
            FilterFactory filterFactory)
    {
        super();

        id_ = factory.getChannelID();

        orb_ = orb;
        poa_ = poa;
        configuration_ = config;
        defaultFilterFactory_ = filterFactory;
        container_ = factory.getContainer();

        logger_ = ((org.jacorb.config.Configuration) config).getNamedLogger(getClass().getName());

        container_.registerComponentImplementation(SubscriptionManager.class);

        container_.registerComponentImplementation(OfferManager.class);

        adminSettings_ = new AdminPropertySet(configuration_);

        qosSettings_ = new QoSPropertySet(configuration_, QoSPropertySet.CHANNEL_QOS);

        listManager_ = new FilterStageListManager()
        {
            public void fetchListData(FilterStageListManager.FilterStageList list)
            {
                synchronized (modifyConsumerAdminsLock_)
                {
                    Iterator i = consumerAdminServants_.keySet().iterator();

                    while (i.hasNext())
                    {
                        Integer _key = (Integer) i.next();
                        list.add((FilterStage) consumerAdminServants_.get(_key));
                    }
                }
            }
        };

        servantLifecyle_ = new ServantLifecyleControl(this, config);
    }

    ////////////////////////////////////////

    public final void deactivate()
    {
        servantLifecyle_.deactivate();
    }

    public final org.omg.CORBA.Object activate()
    {
        return servantLifecyle_.activate();
    }

    /**
     * Callback to help keep track of the number of Consumers.
     *
     * @exception AdminLimitExceeded
     *                if creation of another Consumer is prohibited.
     */
    private void addConsumer() throws AdminLimitExceeded
    {
        final int _maxNumberOfConsumers = maxNumberOfConsumers_.get();
        final int _numberOfConsumers = numberOfConsumers_.incrementAndGet();

        if (_maxNumberOfConsumers == 0)
        {
            // no limit set
        }
        else if (_numberOfConsumers > _maxNumberOfConsumers)
        {
            // too many consumers
            numberOfConsumers_.decrementAndGet();
            Any _any = orb_.create_any();
            _any.insert_long(_maxNumberOfConsumers);

            AdminLimit _limit = new AdminLimit("consumer limit", _any);

            throw new AdminLimitExceeded("Consumer creation request exceeds AdminLimit.", _limit);
        }
    }

    private void removeConsumer()
    {
        numberOfConsumers_.decrementAndGet();
    }

    /**
     * Callback to keep track of the number of Suppliers
     *
     * @exception AdminLimitExceeded
     *                if creation of another Suppliers is prohibited
     */
    private void addSupplier() throws AdminLimitExceeded
    {
        final int _numberOfSuppliers = numberOfSuppliers_.incrementAndGet();
        final int _maxNumberOfSuppliers = maxNumberOfSuppliers_.get();

        if (_maxNumberOfSuppliers == 0)
        {
            // no limit set
        }
        else if (_numberOfSuppliers > _maxNumberOfSuppliers)
        {
            // too many suppliers
            numberOfSuppliers_.decrementAndGet();

            Any _any = orb_.create_any();
            _any.insert_long(_maxNumberOfSuppliers);

            AdminLimit _limit = new AdminLimit("supplier limit", _any);

            throw new AdminLimitExceeded("supplier creation request exceeds AdminLimit.", _limit);
        }
    }

    private void removeSupplier()
    {
        numberOfSuppliers_.decrementAndGet();
    }

    protected final boolean isDefaultConsumerAdminActive()
    {
        synchronized (modifyConsumerAdminsLock_)
        {
            return consumerAdminServants_.containsKey(DEFAULT_ADMIN_KEY);
        }
    }

    protected final boolean isDefaultSupplierAdminActive()
    {
        synchronized (modifySupplierAdminsLock_)
        {
            return supplierAdminServants_.containsKey(DEFAULT_ADMIN_KEY);
        }
    }

    /**
     * The default_filter_factory attribute is a readonly attribute that maintains an object
     * reference to the default factory to be used by the EventChannel instance with which it is
     * associated for creating filter objects. If the target channel does not support a default
     * filter factory, the attribute will maintain the value of OBJECT_NIL.
     */
    public final FilterFactory default_filter_factory()
    {
        return defaultFilterFactory_;
    }

    public final int[] get_all_consumeradmins()
    {
        synchronized (modifyConsumerAdminsLock_)
        {
            final int[] _allConsumerAdminKeys = new int[consumerAdminServants_.size()];
            final Iterator i = consumerAdminServants_.keySet().iterator();

            for(int x = 0; i.hasNext(); ++x)
            {
                _allConsumerAdminKeys[x] = ((Integer) i.next()).intValue();
            }
            return _allConsumerAdminKeys;
        }
    }

    public final int[] get_all_supplieradmins()
    {
        synchronized (modifySupplierAdminsLock_)
        {
            final int[] _allSupplierAdminKeys = new int[supplierAdminServants_.size()];
            final Iterator i = supplierAdminServants_.keySet().iterator();

            for(int x = 0; i.hasNext(); ++x)
            {
                _allSupplierAdminKeys[x] = ((Integer) i.next()).intValue();
            }
            return _allSupplierAdminKeys;
        }
    }

    public final Property[] get_admin()
    {
        return adminSettings_.toArray();
    }

    public final Property[] get_qos()
    {
        return qosSettings_.toArray();
    }

    public final void set_qos(Property[] props) throws UnsupportedQoS
    {
        if (logger_.isDebugEnabled())
        {
            logger_.debug("AbstractEventChannel.set_qos: " + qosSettings_);
        }

        qosSettings_.validate_qos(props, new NamedPropertyRangeSeqHolder());

        qosSettings_.set_qos(props);
    }

    public final void validate_qos(Property[] props,
            NamedPropertyRangeSeqHolder namedPropertySeqHolder) throws UnsupportedQoS
    {
        qosSettings_.validate_qos(props, namedPropertySeqHolder);
    }

    public final void set_admin(Property[] adminProps) throws UnsupportedAdmin
    {
        adminSettings_.validate_admin(adminProps);

        adminSettings_.set_admin(adminProps);

        configureAdminLimits(adminSettings_);
    }

    private void configureAdminLimits(PropertySet adminProperties)
    {
        Any _maxConsumers = adminProperties.get(MaxConsumers.value);
        setMaxNumberOfConsumers(_maxConsumers.extract_long());

        Any _maxSuppliers = adminProperties.get(MaxSuppliers.value);
        setMaxNumberOfSuppliers(_maxSuppliers.extract_long());
    }

    /**
     * destroy this Channel, all created Admins and all Proxies.
     *
     * @jmx.managed-operation   description = "Destroy this Channel"
     *                          impact = "ACTION"
     */
    public final void destroy()
    {
        if (destroyed_.compareAndSet(false, true))
        {
            container_.dispose();

            final List list = container_.getComponentInstancesOfType(IContainer.class);

            for (Iterator i = list.iterator(); i.hasNext();)
            {
                IContainer element = (IContainer) i.next();
                element.destroy();
            }
        }
        else
        {
            throw new OBJECT_NOT_EXIST();
        }
    }

    public final void dispose()
    {
        if (logger_.isInfoEnabled())
        {
            logger_.info("destroy channel " + id_);
        }

        deactivate();

        disposables_.dispose();
    }

    public final POA getPOA()
    {
        return poa_;
    }

    public boolean isPersistent()
    {
        return false;
    }

    /**
     * get the number of clients connected to this event channel. the number is the total of all
     * Suppliers and Consumers connected to this channel.
     */
    public final int getNumberOfConnectedClients()
    {
        return numberOfConsumers_.get() + numberOfSuppliers_.get();
    }

    /**
     * @jmx.managed-attribute description = "maximum number of suppliers that are allowed at a time"
     *                        access = "read-write"
     */
    public final int getMaxNumberOfSuppliers()
    {
        return maxNumberOfSuppliers_.get();
    }

    /**
     * @jmx.managed-attribute access = "read-write"
     */
    public void setMaxNumberOfSuppliers(int max)
    {
        if (max < 0)
        {
            throw new IllegalArgumentException();
        }

        maxNumberOfSuppliers_.set(max);

        if (logger_.isInfoEnabled())
        {
            logger_.info("set MaxNumberOfSuppliers=" + maxNumberOfSuppliers_);
        }
    }

    /**
     * @jmx.managed-attribute description = "maximum number of consumers that are allowed at a time"
     *                        access = "read-write"
     */
    public final int getMaxNumberOfConsumers()
    {
        return maxNumberOfConsumers_.get();
    }

    /**
     * @jmx.managed-attribute access = "read-write"
     */
    public void setMaxNumberOfConsumers(int max)
    {
        if (max < 0)
        {
            throw new IllegalArgumentException();
        }

        maxNumberOfConsumers_.set(max);

        if (logger_.isInfoEnabled())
        {
            logger_.info("set MaxNumberOfConsumers=" + maxNumberOfConsumers_);
        }
    }

    private Property[] createQoSPropertiesForAdmin()
    {
        Map _copy = new HashMap(qosSettings_.toMap());

        // remove properties that are not relevant for admins
        _copy.remove(EventReliability.value);

        return PropertySet.map2Props(_copy);
    }

    protected AbstractAdmin get_consumeradmin_internal(int identifier) throws AdminNotFound
    {
        synchronized (modifyConsumerAdminsLock_)
        {
            Integer _key = new Integer(identifier);

            if (consumerAdminServants_.containsKey(_key))
            {
                return (AbstractAdmin) consumerAdminServants_.get(_key);
            }

            throw new AdminNotFound("ID " + identifier + " does not exist.");
        }
    }

    protected AbstractAdmin get_supplieradmin_internal(int identifier) throws AdminNotFound
    {
        synchronized (modifySupplierAdminsLock_)
        {
            Integer _key = new Integer(identifier);

            if (supplierAdminServants_.containsKey(_key))
            {
                return (AbstractAdmin) supplierAdminServants_.get(_key);
            }

            throw new AdminNotFound("ID " + identifier + " does not exist.");
        }
    }

    /**
     * fetch the List of all ConsumerAdmins that are connected to this EventChannel.
     */
    private List getAllConsumerAdmins()
    {
        return listManager_.getList();
    }

    protected AbstractAdmin getDefaultConsumerAdminServant()
    {
        AbstractAdmin _admin;

        synchronized (modifyConsumerAdminsLock_)
        {
            _admin = (AbstractAdmin) consumerAdminServants_.get(DEFAULT_ADMIN_KEY);

            if (_admin == null)
            {
                _admin = newConsumerAdminServant(DEFAULT_ADMIN_KEY.intValue());
                _admin.setInterFilterGroupOperator(InterFilterGroupOperator.AND_OP);
                try
                {
                    _admin.set_qos(createQoSPropertiesForAdmin());
                } catch (UnsupportedQoS e)
                {
                    logger_.error("unable to set qos", e);
                }

                addToConsumerAdmins(_admin);
            }
        }

        return _admin;
    }

    private void addToConsumerAdmins(AbstractAdmin admin)
    {
        final Integer _key = admin.getID();

        admin.registerDisposable(new Disposable()
        {
            public void dispose()
            {
                synchronized (modifyConsumerAdminsLock_)
                {
                    consumerAdminServants_.remove(_key);
                    listManager_.actionSourceModified();
                }
            }
        });

        synchronized (modifyConsumerAdminsLock_)
        {
            consumerAdminServants_.put(_key, admin);

            listManager_.actionSourceModified();
        }
    }

    protected AbstractAdmin new_for_consumers_servant(InterFilterGroupOperator filterGroupOperator,
            IntHolder intHolder)
    {
        final AbstractAdmin _admin = newConsumerAdminServant(createAdminID());

        intHolder.value = _admin.getID().intValue();

        _admin.setInterFilterGroupOperator(filterGroupOperator);

        try
        {
            _admin.set_qos(createQoSPropertiesForAdmin());
        } catch (UnsupportedQoS e)
        {
            logger_.error("unable to set QoS", e);
        }

        _admin.addProxyEventListener(proxySupplierEventListener_);

        addToConsumerAdmins(_admin);

        return _admin;
    }

    private int createAdminID()
    {
        return adminIdPool_.incrementAndGet();
    }

    private void addToSupplierAdmins(AbstractAdmin admin)
    {
        final Integer _key = admin.getID();

        admin.registerDisposable(new Disposable()
        {
            public void dispose()
            {
                synchronized (modifySupplierAdminsLock_)
                {
                    supplierAdminServants_.remove(_key);
                }
            }
        });

        synchronized (modifySupplierAdminsLock_)
        {
            supplierAdminServants_.put(_key, admin);
        }
    }

    protected AbstractAdmin new_for_suppliers_servant(InterFilterGroupOperator filterGroupOperator,
            IntHolder intHolder)
    {
        final AbstractAdmin _admin = newSupplierAdminServant(createAdminID());

        intHolder.value = _admin.getID().intValue();

        _admin.setInterFilterGroupOperator(filterGroupOperator);

        try
        {
            _admin.set_qos(createQoSPropertiesForAdmin());
        } catch (UnsupportedQoS e)
        {
            logger_.error("unable to set QoS", e);
        }

        _admin.addProxyEventListener(proxyConsumerEventListener_);

        addToSupplierAdmins(_admin);

        return _admin;
    }

    protected AbstractAdmin getDefaultSupplierAdminServant()
    {
        AbstractAdmin _admin;

        synchronized (modifySupplierAdminsLock_)
        {
            _admin = (AbstractAdmin) supplierAdminServants_.get(DEFAULT_ADMIN_KEY);

            if (_admin == null)
            {
                _admin = newSupplierAdminServant(DEFAULT_ADMIN_KEY.intValue());
                _admin.setInterFilterGroupOperator(InterFilterGroupOperator.AND_OP);
                try
                {
                    _admin.set_qos(createQoSPropertiesForAdmin());
                } catch (UnsupportedQoS e)
                {
                    logger_.error("unable to set qos", e);
                }

                addToSupplierAdmins(_admin);
            }
        }

        return _admin;
    }

    ////////////////////////////////////////

    private AbstractAdmin newConsumerAdminServant(int id)
    {
        return newConsumerAdmin(id);
    }

    protected abstract AbstractAdmin newConsumerAdmin(int id);

    ////////////////////////////////////////

    private static class FilterStageSourceAdapter implements FilterStageSource
    {
        final WeakReference channelRef_;

        FilterStageSourceAdapter(AbstractEventChannel channel)
        {
            channelRef_ = new WeakReference(channel);
        }

        public List getSubsequentFilterStages()
        {
            return ((AbstractEventChannel) channelRef_.get()).getAllConsumerAdmins();
        }
    }

    private AbstractAdmin newSupplierAdminServant(int id)
    {
        final AbstractSupplierAdmin _admin = newSupplierAdmin(id);

        _admin.setSubsequentFilterStageSource(new FilterStageSourceAdapter(this));

        return _admin;
    }

    protected abstract AbstractSupplierAdmin newSupplierAdmin(int id);

    /**
     * @jmx.managed-attribute description="ID that identifies this EventChannel"
     *                        access = "read-only"
     *                        currencyTimeLimit = "2147483647"
     */
    public int getID()
    {
        return id_;
    }

    public final void registerDisposable(Disposable d)
    {
        disposables_.addDisposable(d);
    }

    public final String getJMXObjectName()
    {
        return "channel=" + getMBeanName();
    }

    public final String getMBeanName()
    {
        return getMBeanType() + "-" + getID();
    }

    protected abstract String getMBeanType();

    public String[] getJMXNotificationTypes()
    {
        return new String[0];
    }

    public void setJMXCallback(JMXManageable.JMXCallback callback)
    {
        jmxCallback_ = callback;
    }
}

