package org.jacorb.notification.servant;

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

import org.apache.avalon.framework.configuration.Configuration;
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.ORB;
import org.omg.CORBA.UNKNOWN;
import org.omg.CosTypedNotifyChannelAdmin.TypedConsumerAdmin;
import org.omg.CosTypedNotifyChannelAdmin.TypedConsumerAdminHelper;
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
 * @version $Id$
 */
public class TypedConsumerAdminImpl extends ConsumerAdminImpl implements
        TypedConsumerAdminOperations
{
    private final TypedConsumerAdmin thisRef_;

    ////////////////////////////////////////

    public TypedConsumerAdminImpl(ORB orb, POA poa, Configuration config,
            MessageFactory messageFactory, OfferManager offerManager,
            SubscriptionManager subscriptionManager, 
            IEventChannel channelServant)
    {
        super(channelServant, orb, poa, config, messageFactory, offerManager, subscriptionManager);
  
        thisRef_ = TypedConsumerAdminHelper.narrow(getServant()._this_object(getORB()));
    }

    ////////////////////////////////////////

    protected Servant createServant()
    {
        return new TypedConsumerAdminPOATie(this);
    }

    public org.omg.CORBA.Object activate()
    {
        return thisRef_;
    }

    public TypedProxyPullSupplier obtain_typed_notification_pull_supplier(String type, IntHolder id)
    {
        try
        {
            AbstractProxy _proxy = obtain_typed_notification_pull_supplier_servant(type);

            id.value = _proxy.getID().intValue();

            _proxy.preActivate();

            return TypedProxyPullSupplierHelper.narrow(_proxy.activate());
        } catch (Exception e)
        {
            logger_.fatalError("obtain_typed_notification_pull_supplier", e);

            throw new UNKNOWN(e.getMessage());
        }
    }

    private AbstractProxySupplier obtain_typed_notification_pull_supplier_servant(String type)
            throws Exception
    {
        final MutablePicoContainer _containerForProxy =
            newContainerForTypedProxy(type);
        
        _containerForProxy.registerComponent(newComponentAdapter(TypedProxyPullSupplierImpl.class, TypedProxyPullSupplierImpl.class));
        
        TypedProxyPullSupplierImpl _servant = (TypedProxyPullSupplierImpl) _containerForProxy
                .getComponentInstance(TypedProxyPullSupplierImpl.class);

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

            _proxy.preActivate();

            return TypedProxyPushSupplierHelper.narrow(_proxy.activate());
        } catch (Exception e)
        {
            logger_.fatalError("obtain_typed_notification_pull_supplier", e);

            throw new UNKNOWN(e.getMessage());
        }
    }

    private AbstractProxy obtain_typed_notification_push_supplier_servant(String type)
            throws Exception
    {
        final MutablePicoContainer _containerForProxy =
            newContainerForTypedProxy(type);
        
        _containerForProxy.registerComponent(newComponentAdapter(TypedProxyPushSupplierImpl.class, TypedProxyPushSupplierImpl.class));
        
        TypedProxyPushSupplierImpl _servant = (TypedProxyPushSupplierImpl) _containerForProxy
                .getComponentInstance(TypedProxyPushSupplierImpl.class);

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
}