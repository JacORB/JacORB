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

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.container.PicoContainerFactory;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.FilterStageSource;
import org.jacorb.notification.interfaces.ProxyEvent;
import org.jacorb.notification.interfaces.ProxyEventListener;
import org.jacorb.notification.servant.AbstractAdmin;
import org.jacorb.notification.servant.AbstractSupplierAdmin;
import org.jacorb.notification.servant.FilterStageListManager;
import org.jacorb.notification.servant.ManageableServant;
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
import org.omg.PortableServer.Servant;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.CachingComponentAdapter;
import org.picocontainer.defaults.ConstructorInjectionComponentAdapter;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;
import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AbstractEventChannel implements Disposable, Configurable, ManageableServant
{
    /**
     * This key is reserved for the default supplier admin and the default consumer admin.
     */
    private static final Integer DEFAULT_ADMIN_KEY = new Integer(0);

    private final DisposableManager disposables_ = new DisposableManager();

    protected final Logger logger_;

    protected final ORB orb_;

    protected final POA poa_;

    protected final Configuration configuration_;

    /**
     * max number of Suppliers that may be connected at a time to this Channel
     */
    private final SynchronizedInt maxNumberOfSuppliers_ = new SynchronizedInt(0);

    /**
     * max number of Consumers that may be connected at a time to this Channel
     */
    private final SynchronizedInt maxNumberOfConsumers_ = new SynchronizedInt(0);

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
    private final SynchronizedInt adminIdPool_ = new SynchronizedInt(1);

    /**
     * number of Consumers that are connected to this Channel
     */
    private final SynchronizedInt numberOfConsumers_ = new SynchronizedInt(0);

    /**
     * number of Suppliers that are connected to this Channel
     */
    private final SynchronizedInt numberOfSuppliers_ = new SynchronizedInt(0);

    protected boolean duringConstruction_ = true;

    private final ProxyEventListener proxyConsumerEventListener_ = new ProxyEventListener()
    {
        public void actionProxyCreationRequest(ProxyEvent event) throws AdminLimitExceeded
        {
            addConsumer();
        }

        public void actionProxyCreated(ProxyEvent event)
        {
            // No Op
        }

        public void actionProxyDisposed(ProxyEvent event)
        {
            removeConsumer();
        }
    };

    private final ProxyEventListener proxySupplierEventListener_ = new ProxyEventListener()
    {
        public void actionProxyCreationRequest(ProxyEvent event) throws AdminLimitExceeded
        {
            addSupplier();
        }

        public void actionProxyCreated(ProxyEvent event)
        {
            // No OP
        }

        public void actionProxyDisposed(ProxyEvent event)
        {
            removeSupplier();
        }
    };

    protected final MutablePicoContainer container_;

    private final int id_;

    private final SynchronizedBoolean destroyed_ = new SynchronizedBoolean(false);

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

        container_.registerComponent(new CachingComponentAdapter(
                new ConstructorInjectionComponentAdapter(SubscriptionManager.class,
                        SubscriptionManager.class)));

        container_.registerComponent(new CachingComponentAdapter(
                new ConstructorInjectionComponentAdapter(OfferManager.class, OfferManager.class)));

        container_.registerComponent(PicoContainerFactory
                .newDeliverTaskExecutorComponentAdapter(container_));

        adminSettings_ = new AdminPropertySet(configuration_);

        qosSettings_ = new QoSPropertySet(configuration_, QoSPropertySet.CHANNEL_QOS);

        listManager_ = new FilterStageListManager()
        {
            public void fetchListData(FilterStageListManager.List list)
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
    }

    
    public void configure(Configuration conf) throws ConfigurationException
    {
    }

    ////////////////////////////////////////

    public void preActivate()
    {
        // NO OP
    }

    /**
     * Callback to help keep track of the number of Consumers.
     * 
     * @exception AdminLimitExceeded
     *                if creation of another Consumer is prohibited.
     */
    private void addConsumer() throws AdminLimitExceeded
    {
        if ((maxNumberOfConsumers_.get() == 0)
                || (numberOfConsumers_.compareTo(maxNumberOfConsumers_) < 0))
        {
            numberOfConsumers_.increment();
        }
        else
        {
            Any _any = orb_.create_any();
            _any.insert_long(maxNumberOfConsumers_.get());

            AdminLimit _limit = new AdminLimit("consumer limit", _any);

            throw new AdminLimitExceeded("Consumer creation request exceeds AdminLimit.", _limit);
        }
    }

    private void removeConsumer()
    {
        numberOfConsumers_.decrement();
    }

    /**
     * Callback to keep track of the number of Suppliers
     * 
     * @exception AdminLimitExceeded
     *                if creation of another Suppliers is prohibited
     */
    private void addSupplier() throws AdminLimitExceeded
    {
        if ((maxNumberOfSuppliers_.get() == 0)
                || (numberOfSuppliers_.compareTo(maxNumberOfSuppliers_) < 0))
        {
            numberOfSuppliers_.increment();
        }
        else
        {
            Any _any = orb_.create_any();
            _any.insert_long(maxNumberOfSuppliers_.get());

            AdminLimit _limit = new AdminLimit("suppliers limit", _any);

            throw new AdminLimitExceeded("supplier creation request exceeds AdminLimit.", _limit);
        }
    }

    private void removeSupplier()
    {
        numberOfSuppliers_.decrement();
    }

    public final int getAdminID()
    {
        if (duringConstruction_)
        {
            return 0;
        }
        return adminIdPool_.increment();
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
        int[] _allKeys;//         }

        synchronized (modifyConsumerAdminsLock_)
        {
            _allKeys = new int[consumerAdminServants_.size()]; // + _defaultConsumerAdmin];

            Iterator i = consumerAdminServants_.keySet().iterator();
            int x = 0;
            while (i.hasNext())
            {
                _allKeys[x++] = ((Integer) i.next()).intValue();
            }
        }

        return _allKeys;
    }

    public final int[] get_all_supplieradmins()
    {
        int[] _allKeys;

        synchronized (modifySupplierAdminsLock_)
        {
            _allKeys = new int[supplierAdminServants_.size()];

            Iterator i = supplierAdminServants_.keySet().iterator();
            int x = 0;
            while (i.hasNext())
            {
                _allKeys[x++] = ((Integer) i.next()).intValue();
            }
        }

        return _allKeys;
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
        maxNumberOfConsumers_.set(_maxConsumers.extract_long());

        Any _maxSuppliers = adminProperties.get(MaxSuppliers.value);
        maxNumberOfSuppliers_.set(_maxSuppliers.extract_long());

        if (logger_.isInfoEnabled())
        {
            logger_.info("set MaxNumberOfConsumers=" + maxNumberOfConsumers_);
            logger_.info("set MaxNumberOfSuppliers=" + maxNumberOfSuppliers_);
        }
    }

    /**
     * destroy this Channel, all created Admins and all Proxies.
     */
    public final void destroy()
    {
        if (destroyed_.commit(false, true))
        {
            container_.dispose();
            
            List list = container_.getComponentInstancesOfType(IContainer.class);
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
        logger_.info("destroy channel " + id_);

        deactivate();

        disposables_.dispose();
    }

    /**
     * Override this method from the Servant baseclass. Fintan Bolton in his book "Pure CORBA"
     * suggests that you override this method to avoid the risk that a servant object (like this
     * one) could be activated by the <b>wrong </b> POA object.
     */
    public final POA _default_POA()
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

    public final int getMaxNumberOfSuppliers()
    {
        return maxNumberOfSuppliers_.get();
    }

    public final int getMaxNumberOfConsumers()
    {
        return maxNumberOfConsumers_.get();
    }

    public final void deactivate()
    {
        try
        {
            poa_.deactivate_object(poa_.servant_to_id(getServant()));
        } catch (Exception e)
        {
            logger_.error("Unable to deactivate EventChannel Object", e);

            throw new RuntimeException();
        }
    }

    abstract protected Servant getServant();

    private Property[] createQoSPropertiesForAdmin()
    {
        Map _copy = new HashMap(qosSettings_.toMap());

        _copy.remove(EventReliability.value);

        return PropertySet.map2Props(_copy);
    }

    private void configureAdmin(AbstractAdmin admin)
    {
        admin.configure(configuration_);
    }

    protected AbstractAdmin get_consumeradmin_internal(int identifier) throws AdminNotFound
    {
        synchronized (modifyConsumerAdminsLock_)
        {
            Integer _key = new Integer(identifier);

            if (consumerAdminServants_.containsKey(_key))
            {
                AbstractAdmin _admin = (AbstractAdmin) consumerAdminServants_.get(_key);

                return _admin;
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
                AbstractAdmin _admin = (AbstractAdmin) supplierAdminServants_.get(_key);

                return _admin;
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

                try
                {
                    _admin.set_qos(createQoSPropertiesForAdmin());
                } catch (UnsupportedQoS e)
                {
                    logger_.fatalError("unable to set qos", e);
                }

                addToConsumerAdmins(_admin);
            }
        }

        return _admin;
    }

    private void addToConsumerAdmins(AbstractAdmin admin)
    {
        final Integer _key = admin.getID();

        admin.addDisposeHook(new Disposable()
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
        AbstractAdmin _admin = newConsumerAdminServant(createAdminID());

        _admin.setInterFilterGroupOperator(filterGroupOperator);

        intHolder.value = _admin.getID().intValue();

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

    /**
     * @return
     */
    private int createAdminID()
    {
        return adminIdPool_.increment();
    }

    private void addToSupplierAdmins(AbstractAdmin admin)
    {
        final Integer _key = admin.getID();

        admin.addDisposeHook(new Disposable()
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
        AbstractAdmin _admin = newSupplierAdminServant(createAdminID());

        intHolder.value = _admin.getID().intValue();

        _admin.setInterFilterGroupOperator(filterGroupOperator);

        try
        {
            _admin.set_qos(createQoSPropertiesForAdmin());
        } catch (UnsupportedQoS e)
        {
            logger_.fatalError("error setting qos", e);
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

                try
                {
                    _admin.set_qos(createQoSPropertiesForAdmin());
                } catch (UnsupportedQoS e)
                {
                    logger_.fatalError("unable to set qos", e);
                }

                addToSupplierAdmins(_admin);
            }
        }

        return _admin;
    }

    ////////////////////////////////////////

    private AbstractAdmin newConsumerAdminServant(int id)
    {
        AbstractAdmin _admin = newConsumerAdmin(id);

        configureAdmin(_admin);

        return _admin;
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
        AbstractSupplierAdmin _admin = newSupplierAdmin(id);

        _admin.setSubsequentFilterStageSource(new FilterStageSourceAdapter(this));

        configureAdmin(_admin);

        return _admin;
    }

    protected abstract AbstractSupplierAdmin newSupplierAdmin(int id);

    public int getID()
    {
        return id_;
    }

    public final void addDisposeHook(Disposable d)
    {
        disposables_.addDisposable(d);
    }
}

