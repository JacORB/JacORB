package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jacorb.notification.conf.Configuration;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.interfaces.AdminEvent;
import org.jacorb.notification.interfaces.AdminEventListener;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.ProxyCreationRequestEvent;
import org.jacorb.notification.interfaces.ProxyCreationRequestEventListener;
import org.jacorb.notification.interfaces.ProxyEvent;
import org.jacorb.notification.interfaces.ProxyEventListener;
import org.jacorb.notification.servant.AbstractAdmin;
import org.jacorb.notification.servant.AdminPropertySet;
import org.jacorb.notification.servant.ConsumerAdminTieImpl;
import org.jacorb.notification.servant.FilterStageListManager;
import org.jacorb.notification.servant.ManageableServant;
import org.jacorb.notification.servant.PropertySet;
import org.jacorb.notification.servant.QoSPropertySet;
import org.jacorb.notification.servant.SupplierAdminTieImpl;
import org.jacorb.util.Debug;
import org.jacorb.util.Environment;

import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.MaxConsumers;
import org.omg.CosNotification.MaxSuppliers;
import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.UnsupportedAdmin;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.AdminLimit;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.AdminNotFound;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.ConsumerAdminHelper;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyChannelAdmin.EventChannelFactoryHelper;
import org.omg.CosNotifyChannelAdmin.EventChannelPOA;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyChannelAdmin.SupplierAdminHelper;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;
import org.apache.avalon.framework.logger.Logger;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class EventChannelImpl
    extends EventChannelPOA
    implements Disposable,
               ManageableServant
{
    private Logger logger_ = Debug.getNamedLogger( getClass().getName() );

    private ORB orb_;

    private POA poa_;

    private EventChannel thisRef_;

    private FilterFactory defaultFilterFactory_;

    private EventChannelFactoryImpl eventChannelFactory_;

    private String ior_;

    private int key_;

    private FilterStageListManager listManager_;

    /**
     * lock variable used to access allConsumerAdmins_ and
     * consumerAdminServants_.
     */
    private Object modifyConsumerAdminsLock_ = new Object();

    /**
     * lock variable used to access allConsumerAdmins_.
     */
    private Object modifySupplierAdminsLock_ = new Object();

    /**
     * maps id's to ConsumerAdminServants (notify style).
     */
    private Map consumerAdminServants_ = new HashMap();

    /**
     * maps id's to SupplierAdminServants (notify style).
     */
    private Map supplierAdminServants_ = new HashMap();

    /**
     * pool of available ID's for Admin Objects. The Pool is used for
     * Consumer and Supplier Admins. NOTE: The least
     * available ID is 1 as the ID 0 has a special meaning.
     * @see #DEFAULT_ADMIN_KEY DEFAULT_ADMIN_KEY.
     */
    private SynchronizedInt adminIdPool_ = new SynchronizedInt(1);

    /**
     * This key is reserved for the default supplier admin and the default
     * consumer admin.
     */
    private static final Integer DEFAULT_ADMIN_KEY = new Integer(0);

    /**
     * number of Consumers that are connected to this
     * Channel
     */
    private SynchronizedInt numberOfConsumers_ = new SynchronizedInt(0);

    /**
     * number of Suppliers that are connected to this
     * Channel
     */
    private SynchronizedInt numberOfSuppliers_ = new SynchronizedInt(0);

    /**
     * max number of Consumers that may be connected at a time to this
     * Channel
     */
    private int maxNumberOfConsumers_;

    /**
     * max number of Suppliers that may be connected at a time to this
     * Channel
     */
    private int maxNumberOfSuppliers_;

    private ChannelContext channelContext_;

    private SubscriptionManager subscriptionManager_ = new SubscriptionManager();

    private OfferManager offerManager_ = new OfferManager();

    private AdminPropertySet adminSettings_ = new AdminPropertySet();

    private QoSPropertySet qosSettings_ = new QoSPropertySet(QoSPropertySet.CHANNEL_QOS);

    private List listAdminEventListeners_ = new ArrayList();

    private ProxyCreationRequestEventListener proxyConsumerCreationListener_ =
        new ProxyCreationRequestEventListener()
        {
            public void actionProxyCreationRequest( ProxyCreationRequestEvent event )
                throws AdminLimitExceeded
            {
                addSupplier();
            }
        };

    private ProxyCreationRequestEventListener proxySupplierCreationListener_ =
        new ProxyCreationRequestEventListener()
        {
            public void actionProxyCreationRequest( ProxyCreationRequestEvent event )
                throws AdminLimitExceeded
            {
                addConsumer();
            }
        };

    private ProxyEventListener proxyConsumerDisposedListener_ =
        new ProxyEventListener()
        {
            public void actionProxyCreated( ProxyEvent e)
            {
                // No Op
            }

            public void actionProxyDisposed( ProxyEvent e )
            {
                removeConsumer();
            }
        };

    private ProxyEventListener proxySupplierDisposedListener_ =
        new ProxyEventListener()
        {
            public void actionProxyCreated(ProxyEvent e)
            {
                // No OP
            }

            public void actionProxyDisposed( ProxyEvent e )
            {
                removeSupplier();
            }
        };

    ////////////////////////////////////////

    EventChannelImpl(ChannelContext channelContext)
    {
        super();

        channelContext_ = channelContext;

        eventChannelFactory_ = channelContext.getEventChannelFactoryServant();

        defaultFilterFactory_ = channelContext.getDefaultFilterFactory();

        channelContext_.setEventChannelServant(this);

        channelContext_.setProxySupplierDisposedEventListener( proxySupplierDisposedListener_ );

        channelContext_.setProxyConsumerDisposedEventListener( proxyConsumerDisposedListener_ );

        listManager_ = new FilterStageListManager() {
                public void fetchListData(FilterStageListManager.List list) {

                    synchronized (consumerAdminServants_) {
                        Iterator i = consumerAdminServants_.keySet().iterator();

                        while (i.hasNext() ) {
                            Integer _key = (Integer)i.next();
                            list.add((FilterStage)consumerAdminServants_.get(_key));
                        }
                    }
                }
            };
    }

    ////////////////////////////////////////

    public void preActivate() {
        // NO OP
    }


    public void setKey(int key) {
        key_ = key;
    }


    public void setORB(ORB orb) {
        orb_ = orb;
    }


    public void setPOA(POA poa) {
        poa_ = poa;
    }


    public Servant getServant() {
        return this;
    }


    public synchronized org.omg.CORBA.Object activate() {
        if (thisRef_ == null)
            {
                thisRef_ = _this( orb_ );

                try {
                    ior_ = orb_.object_to_string(poa_.servant_to_reference(getServant()));
                } catch (Exception e) {
                    logger_.error("unable to access IOR", e);
                }
            }

        return thisRef_;
    }


    /**
     * Callback to help keep track of the number of Consumers.
     *
     * @exception AdminLimitExceeded if creation of another Consumer
     * is prohibited.
     */
    private void addConsumer()
        throws AdminLimitExceeded
    {
        if ( numberOfConsumers_.compareTo(maxNumberOfConsumers_) < 0)
        {
            numberOfConsumers_.increment();
        }
        else
        {
            Any _any = orb_.create_any();
            _any.insert_long(maxNumberOfConsumers_);

            AdminLimit _limit = new AdminLimit("consumer limit", _any);

            throw new AdminLimitExceeded("Consumer creation request exceeds AdminLimit.", _limit);
        }
    }


    private void removeConsumer() {
        numberOfConsumers_.decrement();
    }


    /**
     * Callback to keep track of the number of Suppliers
     *
     * @exception AdminLimitExceeded if creation of another Suppliers
     * is prohibited
     */
    private void addSupplier()
        throws AdminLimitExceeded
    {
        if ( numberOfSuppliers_.compareTo(maxNumberOfSuppliers_) < 0 )
        {
            numberOfSuppliers_.increment();
        }
        else
        {
            Any _any = orb_.create_any();
            _any.insert_long(maxNumberOfSuppliers_);

            AdminLimit _limit = new AdminLimit("suppliers limit", _any);

            throw new AdminLimitExceeded("supplier creation request exceeds AdminLimit.", _limit);
        }
    }


    private void removeSupplier() {
        numberOfSuppliers_.decrement();
    }


    int getAdminId()
    {
        return adminIdPool_.increment();
    }


    private void fireAdminCreatedEvent(AbstractAdmin admin)
    {
        Iterator i = listAdminEventListeners_.iterator();
        AdminEvent e = new AdminEvent(admin);

        while (i.hasNext())
        {
            ((AdminEventListener)i.next()).actionAdminCreated(e);
        }
    }


    private void fireAdminDestroyedEvent(AbstractAdmin admin)
    {
        Iterator i = listAdminEventListeners_.iterator();
        AdminEvent e = new AdminEvent(admin);

        while (i.hasNext())
        {
            ((AdminEventListener)i.next()).actionAdminDestroyed(e);
        }
    }


    public void addAdminEventListener(AdminEventListener l)
    {
        listAdminEventListeners_.add(l);
    }


    public void removeAdminEventListener(AdminEventListener l)
    {
        listAdminEventListeners_.remove(l);
    }


    private AbstractAdmin newConsumerAdmin(ChannelContext context) {
        Integer _key = new Integer(adminIdPool_.increment());

        return newConsumerAdmin(context, _key);
    }


    private AbstractAdmin newConsumerAdmin(ChannelContext context, Integer key) {
        AbstractAdmin _admin = new ConsumerAdminTieImpl(context);

        configureAdmin(_admin);
        _admin.setKey(key);

        return _admin;
    }

    private AbstractAdmin newSupplierAdmin(ChannelContext context) {
        Integer _key = new Integer(adminIdPool_.increment());

        return newSupplierAdmin(context, _key);
    }


    private AbstractAdmin newSupplierAdmin(ChannelContext context,
                                           Integer key) {
        AbstractAdmin _admin = new SupplierAdminTieImpl(context);

        configureAdmin(_admin);
        _admin.setKey(key);

        return _admin;
    }


    private void configureAdmin(AbstractAdmin admin) {
        admin.setSubscriptionManager(subscriptionManager_);
        admin.setOfferManager(offerManager_);
    }


    AbstractAdmin getDefaultConsumerAdminServant()
    {
        AbstractAdmin _defaultConsumerAdmin;

        synchronized(modifyConsumerAdminsLock_) {

            _defaultConsumerAdmin = (AbstractAdmin)consumerAdminServants_.get(DEFAULT_ADMIN_KEY);

            if (_defaultConsumerAdmin == null) {
                _defaultConsumerAdmin = newConsumerAdmin(channelContext_, DEFAULT_ADMIN_KEY);

                try {
                    _defaultConsumerAdmin.set_qos(qosSettings_.toArray());
                } catch (UnsupportedQoS e) {
                    logger_.fatalError("unable to set qos", e);
                }

                fireAdminCreatedEvent(_defaultConsumerAdmin);

                consumerAdminServants_.put(_defaultConsumerAdmin.getKey(), _defaultConsumerAdmin);

                listManager_.actionSourceModified();
            }
        }

        return _defaultConsumerAdmin;
    }

    boolean isDefaultConsumerAdminActive() {
        synchronized (modifyConsumerAdminsLock_) {
            return consumerAdminServants_.containsKey(DEFAULT_ADMIN_KEY);
        }
    }

    /**
     * @todo factor out object creation to factory class
     */
    AbstractAdmin getDefaultSupplierAdminServant()
    {
        AbstractAdmin _admin;

        synchronized(modifySupplierAdminsLock_) {
            _admin = (AbstractAdmin)supplierAdminServants_.get(DEFAULT_ADMIN_KEY);

            if (_admin == null) {
                _admin = newSupplierAdmin(channelContext_, DEFAULT_ADMIN_KEY);

                try {
                    _admin.set_qos(qosSettings_.toArray());
                } catch (UnsupportedQoS e) {
                    logger_.fatalError("", e);
                }

                fireAdminCreatedEvent(_admin);

                supplierAdminServants_.put(_admin.getKey(), _admin);
            }
        }

        return _admin;
    }


    boolean isDefaultSupplierAdminActive() {
        synchronized(modifySupplierAdminsLock_) {
            return supplierAdminServants_.containsKey(DEFAULT_ADMIN_KEY);
        }
    }

    /**
     * The MyFactory attribute is a readonly attribute that maintains
     * the object reference of the event channel factory, which
     * created a given Notification Service EventChannel instance.
     */
    public EventChannelFactory MyFactory()
    {
        return EventChannelFactoryHelper.narrow(eventChannelFactory_.activate());
    }


    /**
     * The default_consumer_admin attribute is a readonly attribute
     * that maintains a reference to the default ConsumerAdmin
     * instance associated with the target EventChannel instance. Each
     * EventChannel instance has an associated default ConsumerAdmin
     * instance, which exists upon creation of the channel and is
     * assigned the unique identifier of zero. Subsequently, clients
     * can create additional Event Service style ConsumerAdmin
     * instances by invoking the inherited  operation, and additional
     * Notification Service style ConsumerAdmin instances by invoking
     * the new_for_consumers operation defined by the EventChannel
     * interface.
     */
    public ConsumerAdmin default_consumer_admin()
    {
        return ConsumerAdminHelper.narrow(getDefaultConsumerAdminServant().activate());
    }


    /**
     * The default_supplier_admin attribute is a readonly attribute
     * that maintains a reference to the default SupplierAdmin
     * instance associated with the target EventChannel instance. Each
     * EventChannel instance has an associated default SupplierAdmin
     * instance, which exists upon creation of the channel and is
     * assigned the unique identifier of zero. Subsequently, clients
     * can create additional Event Service style SupplierAdmin
     * instances by invoking the inherited for_suppliers operation,
     * and additional Notification Service style SupplierAdmin
     * instances by invoking the new_for_suppliers operation defined
     * by the EventChannel interface.
     */
    public SupplierAdmin default_supplier_admin()
    {
        return SupplierAdminHelper.narrow(getDefaultSupplierAdminServant().activate());
    }


    /**
     * The default_filter_factory attribute is a readonly attribute
     * that maintains an object reference to the default factory to be
     * used by the EventChannel instance with which it is associated for
     * creating filter objects. If the target channel does not support
     * a default filter factory, the attribute will maintain the value
     * of OBJECT_NIL.
     */
    public FilterFactory default_filter_factory()
    {
        return defaultFilterFactory_;
    }


    /**
     * The new_for_consumers operation is invoked to create a new
     * Notification Service style ConsumerAdmin instance. The
     * operation accepts as an input parameter a boolean flag, which
     * indicates whether AND or OR semantics will be used when
     * combining the filter objects associated with the newly created
     * ConsumerAdmin instance with those associated with a supplier
     * proxy, which was created by the ConsumerAdmin during the
     * evaluation of each event against a set of filter objects. The
     * new instance is assigned a unique identifier by the target
     * EventChannel instance that is unique among all ConsumerAdmin
     * instances currently associated with the channel. Upon
     * completion, the operation returns the reference to the new
     * ConsumerAdmin instance as the result of the operation, and the
     * unique identifier assigned to the new ConsumerAdmin instance as
     * the output parameter.
     */
    public ConsumerAdmin new_for_consumers( InterFilterGroupOperator filterGroupOperator,
                                            IntHolder intHolder )
    {
        AbstractAdmin _consumerAdminTieImpl =
            new_for_consumers_servant( filterGroupOperator, intHolder );

        fireAdminCreatedEvent(_consumerAdminTieImpl);

        return ConsumerAdminHelper.narrow(_consumerAdminTieImpl.activate());
    }


    /**
     * @todo factor out object creation to factory class
     */
    AbstractAdmin new_for_consumers_servant( InterFilterGroupOperator filterGroupOperator,
                                             IntHolder intHolder )
    {
        AbstractAdmin _admin = newConsumerAdmin(channelContext_);

        _admin.setInterFilterGroupOperator(filterGroupOperator);

        intHolder.value = _admin.getKey().intValue();

        _admin.setIsKeyPublic(true);

        try {
            _admin.set_qos(qosSettings_.toArray());
        } catch (UnsupportedQoS e) {
            logger_.error("err", e);
        }

        _admin.addProxyCreationEventListener( proxySupplierCreationListener_ );

        synchronized (modifyConsumerAdminsLock_) {
            consumerAdminServants_.put( _admin.getKey(), _admin );

            //            allConsumerAdmins_.add( _admin );

            listManager_.actionSourceModified();

            return _admin;
        }
    }


    public SupplierAdmin new_for_suppliers( InterFilterGroupOperator filterGroupOperator,
                                            IntHolder intHolder )
    {
        AbstractAdmin _supplierAdmin =
            new_for_suppliers_servant( filterGroupOperator, intHolder );

        fireAdminCreatedEvent(_supplierAdmin);

        return SupplierAdminHelper.narrow(_supplierAdmin.activate());
    }


    /**
     * @todo factor out object creation to factory class
     */
    AbstractAdmin new_for_suppliers_servant( InterFilterGroupOperator filterGroupOperator,
                                             IntHolder intHolder )
    {
        AbstractAdmin _admin = newSupplierAdmin(channelContext_);

        intHolder.value = _admin.getKey().intValue();

        _admin.setInterFilterGroupOperator(filterGroupOperator);

        _admin.setIsKeyPublic(true);

        try {
            _admin.set_qos(qosSettings_.toArray());
        } catch (UnsupportedQoS e) {
            logger_.fatalError("error setting qos", e);
        }

        _admin.addProxyCreationEventListener( proxyConsumerCreationListener_ );

        synchronized(modifySupplierAdminsLock_) {
            supplierAdminServants_.put( _admin.getKey(), _admin );
        }

        return _admin;
    }


    /**
     * @todo admins should remove themselves from the right list. not
     * the otherway around.
     */
    public void removeAdmin( AbstractAdmin admin )
    {
        Integer _key = admin.getKey();

        if ( _key != null )
        {
            if ( admin instanceof SupplierAdminTieImpl )
            {
                synchronized(modifySupplierAdminsLock_) {
                    supplierAdminServants_.remove( _key );
                }
            }
            else if ( admin instanceof ConsumerAdminTieImpl )
            {
                synchronized(modifyConsumerAdminsLock_) {
                    consumerAdminServants_.remove( _key );
                }
            }
        }

        if ( admin instanceof ConsumerAdminTieImpl  )
        {
            synchronized (modifyConsumerAdminsLock_) {
                listManager_.actionSourceModified();
            }
        }

        fireAdminDestroyedEvent(admin);
    }


    public ConsumerAdmin get_consumeradmin( int identifier )
        throws AdminNotFound
    {
        synchronized(modifyConsumerAdminsLock_) {
            Integer _key = new Integer( identifier );

            if (consumerAdminServants_.containsKey(_key)) {

                AbstractAdmin _admin = ( AbstractAdmin ) consumerAdminServants_.get( _key );

                return ConsumerAdminHelper.narrow( _admin.activate() );
            } else {
                throw new AdminNotFound("ID " + identifier + " does not exist.");
            }
        }
    }


    public SupplierAdmin get_supplieradmin( int identifier )
        throws AdminNotFound
    {
        synchronized(modifySupplierAdminsLock_) {
            Integer _key = new Integer( identifier );

            if (supplierAdminServants_.containsKey(_key)) {
                AbstractAdmin _admin = ( AbstractAdmin ) supplierAdminServants_.get( _key );

                return SupplierAdminHelper.narrow(  _admin.activate() );
            } else {
                throw new AdminNotFound("ID " + identifier + " does not exist.");
            }
        }
    }


    public int[] get_all_consumeradmins()
    {
        int[] _allKeys;
        int _defaultConsumerAdmin = 0;

        if (isDefaultConsumerAdminActive()) {
            _defaultConsumerAdmin = 1;
        }

        synchronized(modifyConsumerAdminsLock_) {
            _allKeys = new int[consumerAdminServants_.size() + _defaultConsumerAdmin];

            Iterator i = consumerAdminServants_.keySet().iterator();
            int x = 0;
            while (i.hasNext()) {
                _allKeys[x++] = ((Integer)i.next()).intValue();
            }

            if (_defaultConsumerAdmin == 1) {
                _allKeys[x] = 0;
            }
        }

        return _allKeys;
    }


    public int[] get_all_supplieradmins()
    {
        int[] _allKeys;
        int _defaultSupplierAdmin = 0;

        if (isDefaultSupplierAdminActive()) {
            _defaultSupplierAdmin = 1;
        }

        synchronized(modifySupplierAdminsLock_) {
            _allKeys = new int[supplierAdminServants_.size() + _defaultSupplierAdmin];

            Iterator i = supplierAdminServants_.keySet().iterator();
            int x = 0;
            while (i.hasNext()) {
                _allKeys[x++] = ((Integer)i.next()).intValue();
            }

            if (_defaultSupplierAdmin == 1) {
                _allKeys[x] = 0;
            }
        }

        return _allKeys;
    }


    public Property[] get_admin()
    {
        return adminSettings_.toArray();
    }


    public Property[] get_qos()
    {
        return qosSettings_.toArray();
    }


    public void set_qos( Property[] props )
        throws UnsupportedQoS
    {
        logger_.debug("set_qos");

        qosSettings_.validate_qos(props, new NamedPropertyRangeSeqHolder());

        qosSettings_.set_qos(props);
    }


    public void validate_qos( Property[] props,
                              NamedPropertyRangeSeqHolder namedPropertySeqHolder )
        throws UnsupportedQoS
    {
        qosSettings_.validate_qos(props, new NamedPropertyRangeSeqHolder());
    }


    public void set_admin( Property[] adminProps )
        throws UnsupportedAdmin
    {
        adminSettings_.validate_admin(adminProps);

        adminSettings_.set_admin(adminProps);

        configureAdminLimits(adminSettings_);
    }


    private void configureAdminLimits(PropertySet adminProperties) {
        maxNumberOfConsumers_ =
            Environment.getIntPropertyWithDefault(Configuration.MAX_NUMBER_CONSUMERS,
                                                  Default.DEFAULT_MAX_NUMBER_CONSUMERS);

        maxNumberOfSuppliers_ =
            Environment.getIntPropertyWithDefault(Configuration.MAX_NUMBER_SUPPLIERS,
                                                  Default.DEFAULT_MAX_NUMBER_SUPPLIERS);

        if ( adminProperties.containsKey( MaxConsumers.value ) )
            {
                Any _maxConsumers = adminProperties.get( MaxConsumers.value );
                maxNumberOfConsumers_ = _maxConsumers.extract_long();
            }

        if ( adminProperties.containsKey( MaxSuppliers.value ) )
            {
                Any _maxSuppliers = adminProperties.get( MaxSuppliers.value );
                maxNumberOfSuppliers_ = _maxSuppliers.extract_long();
            }

        if (logger_.isInfoEnabled()) {
            logger_.info("set MaxNumberOfConsumers=" + maxNumberOfConsumers_);
            logger_.info("set MaxNumberOfSuppliers=" + maxNumberOfSuppliers_);
        }
    }


    /**
     * destroy this Channel, all created Admins and all Proxies.
     */
    public void destroy()
    {
        dispose();
    }


    public void deactivate() {
        try {
            poa_.deactivate_object(poa_.servant_to_id(this));
        } catch (Exception e) {
            logger_.error("Unable to deactivate EventChannel Object", e);

            throw new RuntimeException();
        }
    }


    public void dispose()
    {
        deactivate();

        eventChannelFactory_.removeEventChannelServant(getKey());

        Iterator _i = consumerAdminServants_.values().iterator();

        while ( _i.hasNext() )
        {
            Disposable _d = (Disposable) _i.next();
            _i.remove();
            _d.dispose();
        }


        _i = supplierAdminServants_.values().iterator();

        while ( _i.hasNext() )
        {
            Disposable _d = ( Disposable ) _i.next();
            _i.remove();
            _d.dispose();
        }

        listAdminEventListeners_.clear();
    }


    /**
     * Return the consumerAdmin interface (event style)
     */
    public org.omg.CosEventChannelAdmin.ConsumerAdmin for_consumers()
    {
        return org.omg.CosEventChannelAdmin.ConsumerAdminHelper.narrow( default_consumer_admin() );
    }


    /**
     * Return the supplierAdmin interface (event style)
     */
    public org.omg.CosEventChannelAdmin.SupplierAdmin for_suppliers()
    {
        return org.omg.CosEventChannelAdmin.SupplierAdminHelper.narrow( default_supplier_admin() );
    }


    /**
     * Override this method from the Servant baseclass.  Fintan Bolton
     * in his book "Pure CORBA" suggests that you override this method to
     * avoid the risk that a servant object (like this one) could be
     * activated by the <b>wrong</b> POA object.
     */
    public POA _default_POA()
    {
        return poa_;
    }


    /**
     * fetch the List of all ConsumerAdmins that are connected to this
     * EventChannel.
     */
    public List getAllConsumerAdmins()
    {
        return listManager_.getList();
    }


    public int getKey()
    {
        return key_;
    }


    public String getIOR()
    {
        return ior_;
    }


    public ChannelContext getChannelContext()
    {
        return channelContext_;
    }
}
