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

import org.apache.avalon.framework.configuration.Configuration;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.container.CORBAObjectComponentAdapter;
import org.jacorb.notification.container.PicoContainerFactory;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.servant.AbstractAdmin;
import org.jacorb.notification.servant.AbstractSupplierAdmin;
import org.jacorb.notification.servant.ConsumerAdminImpl;
import org.jacorb.notification.servant.IEventChannel;
import org.jacorb.notification.servant.SupplierAdminImpl;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosNotifyChannelAdmin.AdminNotFound;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.ConsumerAdminHelper;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyChannelAdmin.EventChannelHelper;
import org.omg.CosNotifyChannelAdmin.EventChannelOperations;
import org.omg.CosNotifyChannelAdmin.EventChannelPOATie;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyChannelAdmin.SupplierAdminHelper;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.picocontainer.MutablePicoContainer;

/**
 * @jmx.mbean name="EventChannelMBean" description="Control an EventChannel"
 * extends = "AbstractEventChannelMBean"
 * 
 * @jboss.xmbean
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */

public class EventChannelImpl extends AbstractEventChannel implements EventChannelOperations, EventChannelImplMBean
{
    private final EventChannelFactory eventChannelFactory_;
    
    private final EventChannel thisRef_;

    private final Servant thisServant_;

    ////////////////////////////////////////

    public EventChannelImpl(IFactory factory, ORB orb, POA poa, Configuration config,
            FilterFactory filterFactory, EventChannelFactory factoryRef)
    {
        super(factory, orb, poa, config, filterFactory);

        eventChannelFactory_ = factoryRef;

        thisServant_ = new EventChannelPOATie(this);

        thisRef_ = EventChannelHelper.narrow(thisServant_._this_object(orb_));

        container_.registerComponent(new CORBAObjectComponentAdapter(EventChannel.class,
                EventChannelHelper.narrow(thisRef_)));

        if (config.getAttributeAsBoolean(Attributes.LAZY_DEFAULT_ADMIN_INIT, Default.DEFAULT_LAZY_DEFAULT_ADMIN_INIT))
        {
            default_consumer_admin();
            default_supplier_admin();
        }
        
        registerDisposable(new Disposable()
        {
            public void dispose()
            {
                container_.unregisterComponent(EventChannel.class);
            }
        });
    }

    protected Servant getServant()
    {
        return thisServant_;
    }

    public synchronized org.omg.CORBA.Object activate()
    {
        return thisRef_;
    }

    protected AbstractAdmin newConsumerAdmin(final int id)
    {
        final MutablePicoContainer _adminContainer = newContainerForAdmin(id);

        _adminContainer.registerComponentImplementation(AbstractAdmin.class, ConsumerAdminImpl.class);

        final AbstractAdmin _admin = (AbstractAdmin) _adminContainer.getComponentInstanceOfType(AbstractAdmin.class);

        return _admin;
    }

    protected AbstractSupplierAdmin newSupplierAdmin(final int id)
    {
        final MutablePicoContainer _adminContainer = newContainerForAdmin(id);

        _adminContainer.registerComponentImplementation(AbstractSupplierAdmin.class, SupplierAdminImpl.class);

        final AbstractSupplierAdmin _admin = (AbstractSupplierAdmin) _adminContainer
                .getComponentInstanceOfType(AbstractSupplierAdmin.class);

        return _admin;
    }

    private MutablePicoContainer newContainerForAdmin(final int id)
    {
        final MutablePicoContainer _adminContainer = PicoContainerFactory.createChildContainer(container_);
        
        final IEventChannel _channelAdapter = new IEventChannel()
        {
            public int getAdminID()
            {
                return id;
            }

            public EventChannel getEventChannel()
            {
                return (EventChannel) _adminContainer.getComponentInstanceOfType(EventChannel.class);
            }

            public int getChannelID()
            {
                return EventChannelImpl.this.getID();
            }

            public MutablePicoContainer getContainer()
            {
                return _adminContainer;
            }
            
            public String getChannelMBean()
            {
                return getJMXObjectName();
            }

            public void destroy()
            {
                _adminContainer.unregisterComponent(IEventChannel.class);

                container_.removeChildContainer(_adminContainer);
            }
        };
        
        _adminContainer.registerComponentInstance(IEventChannel.class, _channelAdapter);
        return _adminContainer;
    }
    
    /**
     * The MyFactory attribute is a readonly attribute that maintains the object reference of the
     * event channel factory, which created a given Notification Service EventChannel instance.
     */
    public EventChannelFactory MyFactory()
    {
        return eventChannelFactory_;
    }

    /**
     * The default_consumer_admin attribute is a readonly attribute that maintains a reference to
     * the default ConsumerAdmin instance associated with the target EventChannel instance. Each
     * EventChannel instance has an associated default ConsumerAdmin instance, which exists upon
     * creation of the channel and is assigned the unique identifier of zero. Subsequently, clients
     * can create additional Event Service style ConsumerAdmin instances by invoking the inherited
     * operation, and additional Notification Service style ConsumerAdmin instances by invoking the
     * new_for_consumers operation defined by the EventChannel interface.
     */
    public ConsumerAdmin default_consumer_admin()
    {
        return ConsumerAdminHelper.narrow(getDefaultConsumerAdminServant().activate());
    }

    /**
     * The default_supplier_admin attribute is a readonly attribute that maintains a reference to
     * the default SupplierAdmin instance associated with the target EventChannel instance. Each
     * EventChannel instance has an associated default SupplierAdmin instance, which exists upon
     * creation of the channel and is assigned the unique identifier of zero. Subsequently, clients
     * can create additional Event Service style SupplierAdmin instances by invoking the inherited
     * for_suppliers operation, and additional Notification Service style SupplierAdmin instances by
     * invoking the new_for_suppliers operation defined by the EventChannel interface.
     */
    public SupplierAdmin default_supplier_admin()
    {
        return SupplierAdminHelper.narrow(getDefaultSupplierAdminServant().activate());
    }

    /**
     * The new_for_consumers operation is invoked to create a new Notification Service style
     * ConsumerAdmin instance. The operation accepts as an input parameter a boolean flag, which
     * indicates whether AND or OR semantics will be used when combining the filter objects
     * associated with the newly created ConsumerAdmin instance with those associated with a
     * supplier proxy, which was created by the ConsumerAdmin during the evaluation of each event
     * against a set of filter objects. The new instance is assigned a unique identifier by the
     * target EventChannel instance that is unique among all ConsumerAdmin instances currently
     * associated with the channel. Upon completion, the operation returns the reference to the new
     * ConsumerAdmin instance as the result of the operation, and the unique identifier assigned to
     * the new ConsumerAdmin instance as the output parameter.
     */
    public ConsumerAdmin new_for_consumers(InterFilterGroupOperator filterGroupOperator,
            IntHolder intHolder)
    {
        AbstractAdmin _consumerAdminTieImpl = new_for_consumers_servant(filterGroupOperator,
                intHolder);

        return ConsumerAdminHelper.narrow(_consumerAdminTieImpl.activate());
    }

    public SupplierAdmin new_for_suppliers(InterFilterGroupOperator filterGroupOperator,
            IntHolder intHolder)
    {
        AbstractAdmin _supplierAdmin = new_for_suppliers_servant(filterGroupOperator, intHolder);

        return SupplierAdminHelper.narrow(_supplierAdmin.activate());
    }

    public ConsumerAdmin get_consumeradmin(int identifier) throws AdminNotFound
    {
        return ConsumerAdminHelper.narrow(get_consumeradmin_internal(identifier).activate());
    }

    public SupplierAdmin get_supplieradmin(int identifier) throws AdminNotFound
    {
        return SupplierAdminHelper.narrow(get_supplieradmin_internal(identifier).activate());
    }

    /**
     * Return the consumerAdmin interface (event style)
     */
    public org.omg.CosEventChannelAdmin.ConsumerAdmin for_consumers()
    {
        AbstractAdmin _admin = getDefaultConsumerAdminServant();

        return org.omg.CosEventChannelAdmin.ConsumerAdminHelper.narrow(_admin.activate());
    }

    /**
     * Return the supplierAdmin interface (event style)
     */
    public org.omg.CosEventChannelAdmin.SupplierAdmin for_suppliers()
    {
        AbstractAdmin _admin = getDefaultSupplierAdminServant();

        return org.omg.CosEventChannelAdmin.SupplierAdminHelper.narrow(_admin.activate());
    }

    public String getMBeanType()
    {
        return "EventChannel";
    }
    
    /**
     * @jmx.managed-attribute   access = "read-only"
     *                          currencyTimeLimit = "2147483647"
     */
    public String getIOR()
    {
        return orb_.object_to_string(thisRef_);
    }
}

