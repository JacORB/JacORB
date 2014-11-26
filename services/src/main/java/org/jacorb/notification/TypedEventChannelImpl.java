package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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

import org.jacorb.config.*;
import org.jacorb.notification.container.PicoContainerFactory;
import org.jacorb.notification.servant.AbstractAdmin;
import org.jacorb.notification.servant.AbstractSupplierAdmin;
import org.jacorb.notification.servant.IEventChannel;
import org.jacorb.notification.servant.ITypedEventChannel;
import org.jacorb.notification.servant.TypedConsumerAdminImpl;
import org.jacorb.notification.servant.TypedSupplierAdminImpl;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosNotifyChannelAdmin.AdminNotFound;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.CosTypedNotifyChannelAdmin.TypedConsumerAdmin;
import org.omg.CosTypedNotifyChannelAdmin.TypedConsumerAdminHelper;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelFactory;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelOperations;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelPOATie;
import org.omg.CosTypedNotifyChannelAdmin.TypedSupplierAdmin;
import org.omg.CosTypedNotifyChannelAdmin.TypedSupplierAdminHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.picocontainer.MutablePicoContainer;

/**
 * @author Alphonse Bendt
 */

public class TypedEventChannelImpl extends AbstractEventChannel implements
        TypedEventChannelOperations, ITypedEventChannel
{
    private final class TypedEventChannelAdapter implements IEventChannel
    {
        private final int adminID_;

        private final MutablePicoContainer childContainer_;

        private final String channelMBean_;

        private TypedEventChannelAdapter(MutablePicoContainer container, String channelMBean, int adminID)
        {
            super();

            adminID_ = adminID;
            childContainer_ = container;
            channelMBean_ = channelMBean;
        }

        public int getAdminID()
        {
            return adminID_;
        }

        public int getChannelID()
        {
            return getID();
        }

        public EventChannel getEventChannel()
        {
            return null;
        }

        public MutablePicoContainer getContainer()
        {
            return childContainer_;
        }

        public String getChannelMBean()
        {
            return channelMBean_;
        }

        public void destroy()
        {
            container_.removeChildContainer(childContainer_);
        }
    }

    private final TypedEventChannelFactory typedEventChannelFactory_;

    public TypedEventChannelImpl(IFactory factory, ORB orb, POA poa, Configuration config,
            FilterFactory filterFactory, TypedEventChannelFactory factoryRef)
    {
        super(factory, orb, poa, config, filterFactory);

        typedEventChannelFactory_ = factoryRef;
    }

    public TypedEventChannelFactory MyFactory()
    {
        return typedEventChannelFactory_;
    }

    public TypedConsumerAdmin default_consumer_admin()
    {
        return TypedConsumerAdminHelper.narrow(getDefaultConsumerAdminServant().activate());
    }

    public TypedSupplierAdmin default_supplier_admin()
    {
        return TypedSupplierAdminHelper.narrow(getDefaultSupplierAdminServant().activate());
    }

    public TypedConsumerAdmin new_for_typed_notification_consumers(InterFilterGroupOperator ifg,
            IntHolder intHolder)
    {
        AbstractAdmin _admin = new_for_consumers_servant(ifg, intHolder);

        return TypedConsumerAdminHelper.narrow(_admin.activate());
    }

    public TypedSupplierAdmin new_for_typed_notification_suppliers(InterFilterGroupOperator ifg,
            IntHolder intHolder)
    {
        AbstractAdmin _admin = new_for_suppliers_servant(ifg, intHolder);

        return TypedSupplierAdminHelper.narrow(_admin.activate());
    }

    public TypedConsumerAdmin get_consumeradmin(int id) throws AdminNotFound
    {
        return TypedConsumerAdminHelper.narrow(get_consumeradmin_internal(id).activate());
    }

    public TypedSupplierAdmin get_supplieradmin(int id) throws AdminNotFound
    {
        return TypedSupplierAdminHelper.narrow(get_supplieradmin_internal(id).activate());
    }

    public org.omg.CosTypedEventChannelAdmin.TypedConsumerAdmin for_consumers()
    {
        return org.omg.CosTypedEventChannelAdmin.TypedConsumerAdminHelper.narrow(default_consumer_admin());
    }

    public org.omg.CosTypedEventChannelAdmin.TypedSupplierAdmin for_suppliers()
    {
        return org.omg.CosTypedEventChannelAdmin.TypedSupplierAdminHelper.narrow(default_supplier_admin());
    }

    public Servant newServant()
    {
        return new TypedEventChannelPOATie(this);
    }

    public AbstractSupplierAdmin newSupplierAdmin(final int id)
    {
        final MutablePicoContainer _container = newContainerForAdmin(id);

        _container.registerComponentImplementation(AbstractSupplierAdmin.class, TypedSupplierAdminImpl.class);

        return (AbstractSupplierAdmin) _container.getComponentInstanceOfType(AbstractSupplierAdmin.class);
    }

    public AbstractAdmin newConsumerAdmin(final int id)
    {
        final MutablePicoContainer _container = newContainerForAdmin(id);

        _container.registerComponentImplementation(AbstractAdmin.class, TypedConsumerAdminImpl.class);

        return (AbstractAdmin) _container.getComponentInstanceOfType(AbstractAdmin.class);
    }

    private MutablePicoContainer newContainerForAdmin(final int id)
    {
        final MutablePicoContainer _container = PicoContainerFactory.createChildContainer(container_);

        _container.registerComponentInstance(new TypedEventChannelAdapter(_container, getJMXObjectName(), id));

        return _container;
    }

    public String getMBeanType()
    {
        return "TypedEventChannel";
    }

    /**
     * @jmx.managed-attribute   access = "read-only"
     *                          currencyTimeLimit = "2147483647"
     */
    public String getIOR()
    {
        return orb_.object_to_string(activate());
    }
}