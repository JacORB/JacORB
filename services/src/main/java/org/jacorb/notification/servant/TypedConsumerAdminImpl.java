package org.jacorb.notification.servant;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.ORB;
import org.omg.CORBA.UNKNOWN;
import org.omg.CosTypedNotifyChannelAdmin.TypedConsumerAdminOperations;
import org.omg.CosTypedNotifyChannelAdmin.TypedConsumerAdminPOATie;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPullSupplier;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPullSupplierHelper;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPushSupplier;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPushSupplierHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.picocontainer.MutablePicoContainer;

/**
 * @author Alphonse Bendt
 */
public class TypedConsumerAdminImpl extends ConsumerAdminImpl implements
        TypedConsumerAdminOperations
{
    public TypedConsumerAdminImpl(ORB orb, POA poa, Configuration config,
            MessageFactory messageFactory, OfferManager offerManager,
            SubscriptionManager subscriptionManager,
            IEventChannel channelServant)
    {
        super(channelServant, orb, poa, config, messageFactory, offerManager, subscriptionManager);

        activate();
    }

    ////////////////////////////////////////

    public Servant newServant()
    {
        return new TypedConsumerAdminPOATie(this);
    }

    public TypedProxyPullSupplier obtain_typed_notification_pull_supplier(String type, IntHolder id)
    {
        try
        {
            AbstractProxy _proxy = obtain_typed_notification_pull_supplier_servant(type);

            id.value = _proxy.getID().intValue();

            return TypedProxyPullSupplierHelper.narrow(_proxy.activate());
        } catch (Exception e)
        {
            logger_.error("obtain_typed_notification_pull_supplier", e);

            throw new UNKNOWN(e.toString());
        }
    }

    private AbstractProxySupplier obtain_typed_notification_pull_supplier_servant(String type)
            throws Exception
    {
        final MutablePicoContainer _containerForProxy =
            newContainerForTypedProxy(type);

        _containerForProxy.registerComponentImplementation(AbstractProxySupplier.class, TypedProxyPullSupplierImpl.class);

        AbstractProxySupplier _servant = (AbstractProxySupplier) _containerForProxy
                .getComponentInstanceOfType(AbstractProxySupplier.class);

        configureMappingFilters(_servant);

        configureQoS(_servant);

        configureInterFilterGroupOperator(_servant);

        addProxyToMap(_servant, pullServants_, modifyProxiesLock_);

        return _servant;
    }

    public TypedProxyPushSupplier obtain_typed_notification_push_supplier(String type, IntHolder id)
    {
        try
        {
            AbstractProxy _proxy = obtain_typed_notification_push_supplier_servant(type);

            id.value = _proxy.getID().intValue();

            return TypedProxyPushSupplierHelper.narrow(_proxy.activate());
        } catch (Exception e)
        {
            logger_.error("obtain_typed_notification_pull_supplier", e);

            throw new UNKNOWN(e.toString());
        }
    }

    private AbstractProxy obtain_typed_notification_push_supplier_servant(String type)
            throws Exception
    {
        final MutablePicoContainer _containerForProxy =
            newContainerForTypedProxy(type);

        _containerForProxy.registerComponentImplementation(AbstractProxySupplier.class, TypedProxyPushSupplierImpl.class);

        AbstractProxySupplier _servant = (AbstractProxySupplier) _containerForProxy
                .getComponentInstanceOfType(AbstractProxySupplier.class);

        configureMappingFilters(_servant);

        configureQoS(_servant);

        configureInterFilterGroupOperator(_servant);

        addProxyToMap(_servant, pushServants_, modifyProxiesLock_);

        return _servant;
    }


    public org.omg.CosEventChannelAdmin.ProxyPushSupplier obtain_typed_push_supplier(String type)
    {
        throw new NO_IMPLEMENT();
    }

    public org.omg.CosTypedEventChannelAdmin.TypedProxyPullSupplier obtain_typed_pull_supplier(
            String type)
    {
        throw new NO_IMPLEMENT();
    }

    public String getMBeanType()
    {
        return "TypedConsumerAdmin";
    }
}