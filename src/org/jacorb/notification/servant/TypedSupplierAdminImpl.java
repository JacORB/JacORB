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
import org.jacorb.notification.util.CollectionsWrapper;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.ORB;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPullConsumer;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPullConsumerHelper;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPushConsumer;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPushConsumerHelper;
import org.omg.CosTypedNotifyChannelAdmin.TypedSupplierAdmin;
import org.omg.CosTypedNotifyChannelAdmin.TypedSupplierAdminHelper;
import org.omg.CosTypedNotifyChannelAdmin.TypedSupplierAdminOperations;
import org.omg.CosTypedNotifyChannelAdmin.TypedSupplierAdminPOATie;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.picocontainer.MutablePicoContainer;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class TypedSupplierAdminImpl extends SupplierAdminImpl implements
        TypedSupplierAdminOperations
{
    private final TypedSupplierAdmin thisRef_;

    public TypedSupplierAdminImpl(IEventChannel channelServant, ORB orb, POA poa,
            Configuration config, MessageFactory messageFactory, OfferManager offerManager,
            SubscriptionManager subscriptionManager)
    {
        super(channelServant, orb, poa, config, messageFactory, offerManager, subscriptionManager);

        thisRef_ = TypedSupplierAdminHelper.narrow(getServant()._this_object(getORB()));
    }

    protected Servant createServant()
    {
        return new TypedSupplierAdminPOATie(this);
    }

    public org.omg.CORBA.Object activate()
    {
        return thisRef_;
    }

    public TypedProxyPushConsumer obtain_typed_notification_push_consumer(String type, IntHolder id)
            throws AdminLimitExceeded
    {
        fireCreateProxyRequestEvent();

        try
        {
            final MutablePicoContainer _containerForProxy = newContainerForTypedProxy(type);

            _containerForProxy.registerComponent(newComponentAdapter(
                    TypedProxyPushConsumerImpl.class, TypedProxyPushConsumerImpl.class));

            TypedProxyPushConsumerImpl _servant = (TypedProxyPushConsumerImpl) _containerForProxy
                    .getComponentInstance(TypedProxyPushConsumerImpl.class);

            _servant.setSubsequentDestinations(CollectionsWrapper.singletonList(this));

            configureInterFilterGroupOperator(_servant);

            configureQoS(_servant);

            addProxyToMap(_servant, pushServants_, modifyProxiesLock_);

            id.value = _servant.getID().intValue();

            return TypedProxyPushConsumerHelper.narrow(_servant.activate());
        } catch (Exception e)
        {
            logger_.error("unable to create typed notification push consumer", e);
            throw new INTERNAL();
        }
    }

    public TypedProxyPullConsumer obtain_typed_notification_pull_consumer(String type, IntHolder id)
            throws AdminLimitExceeded
    {
        fireCreateProxyRequestEvent();

        try
        {
            final MutablePicoContainer _containerForProxy = newContainerForTypedProxy(type);

            _containerForProxy.registerComponent(newComponentAdapter(
                    TypedProxyPullConsumerImpl.class, TypedProxyPullConsumerImpl.class));

            TypedProxyPullConsumerImpl _servant = (TypedProxyPullConsumerImpl) _containerForProxy
                    .getComponentInstance(TypedProxyPullConsumerImpl.class);

            configureInterFilterGroupOperator(_servant);

            configureQoS(_servant);

            addProxyToMap(_servant, pullServants_, modifyProxiesLock_);

            id.value = _servant.getID().intValue();

            return TypedProxyPullConsumerHelper.narrow(_servant.activate());
        } catch (Exception e)
        {
            logger_.error("unable to create typed notification push consumer", e);

            throw new INTERNAL();
        }
    }

    public org.omg.CosTypedEventChannelAdmin.TypedProxyPushConsumer obtain_typed_push_consumer(
            String type)
    {
        throw new NO_IMPLEMENT();
    }

    public org.omg.CosEventChannelAdmin.ProxyPullConsumer obtain_typed_pull_consumer(String type)
    {
        throw new NO_IMPLEMENT();
    }
}