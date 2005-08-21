package org.jacorb.notification.servant;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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

import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.container.CORBAObjectComponentAdapter;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.FilterStageSource;
import org.jacorb.notification.interfaces.MessageConsumer;
import org.jacorb.notification.util.CollectionsWrapper;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CORBA.UNKNOWN;
import org.omg.CosEventChannelAdmin.ProxyPullConsumer;
import org.omg.CosEventChannelAdmin.ProxyPushConsumer;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.ProxyConsumer;
import org.omg.CosNotifyChannelAdmin.ProxyConsumerHelper;
import org.omg.CosNotifyChannelAdmin.ProxyNotFound;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyChannelAdmin.SupplierAdminHelper;
import org.omg.CosNotifyChannelAdmin.SupplierAdminOperations;
import org.omg.CosNotifyChannelAdmin.SupplierAdminPOATie;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.CachingComponentAdapter;

/**
 * @jmx.mbean extends = "AbstractAdminMBean"
 * @jboss.xmbean
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */

public class SupplierAdminImpl extends AbstractSupplierAdmin implements SupplierAdminOperations,
        Disposable, SupplierAdminImplMBean
{
    private FilterStageSource subsequentFilterStagesSource_;

    private final Servant thisServant_;

    private final SupplierAdmin thisCorbaRef_;

    // //////////////////////////////////////

    public SupplierAdminImpl(IEventChannel channelServant, ORB orb, POA poa, Configuration config,
            MessageFactory messageFactory, OfferManager offerManager,
            SubscriptionManager subscriptionManager)
    {
        super(channelServant, orb, poa, config, messageFactory, offerManager, subscriptionManager);

        thisServant_ = createServant();

        thisCorbaRef_ = SupplierAdminHelper.narrow(getServant()._this_object(getORB()));

        container_.registerComponent(new CachingComponentAdapter(new CORBAObjectComponentAdapter(
                SupplierAdmin.class, thisCorbaRef_)));

        registerDisposable(new Disposable()
        {
            public void dispose()
            {
                container_.unregisterComponent(SupplierAdmin.class);
            }
        });
    }

    protected Servant createServant()
    {
        return new SupplierAdminPOATie(this);
    }

    public Servant getServant()
    {
        return thisServant_;
    }

    public org.omg.CORBA.Object activate()
    {
        return thisCorbaRef_;
    }

    public void offer_change(EventType[] added, EventType[] removed) throws InvalidEventType
    {
        offerManager_.offer_change(added, removed);
    }

    /**
     * access the ids of all PullConsumers (NotifyStyle)
     */
    public int[] pull_consumers()
    {
        return get_all_notify_proxies(pullServants_, modifyProxiesLock_);
    }

    /**
     * access the ids of all PushConsumers (NotifyStyle)
     */
    public int[] push_consumers()
    {
        return get_all_notify_proxies(pushServants_, modifyProxiesLock_);
    }

    public ProxyConsumer obtain_notification_pull_consumer(ClientType clientType,
            IntHolder intHolder) throws AdminLimitExceeded
    {
        fireCreateProxyRequestEvent();

        try
        {
            AbstractProxy _servant = obtain_notification_pull_consumer_servant(clientType);

            intHolder.value = _servant.getID().intValue();

            return ProxyConsumerHelper.narrow(_servant.activate());
        } catch (Exception e)
        {
            logger_.fatalError("obtain_notification_pull_consumer: unexpected error", e);

            throw new UNKNOWN();
        }
    }

    private AbstractProxy obtain_notification_pull_consumer_servant(ClientType clientType)
            throws Exception
    {
        AbstractProxy _servant = newProxyPullConsumer(clientType);

        configureInterFilterGroupOperator(_servant);

        configureQoS(_servant);

        addProxyToMap(_servant, pullServants_, modifyProxiesLock_);

        return _servant;
    }

    public ProxyConsumer get_proxy_consumer(int id) throws ProxyNotFound
    {
        return ProxyConsumerHelper.narrow(getProxy(id).activate());
    }

    public ProxyConsumer obtain_notification_push_consumer(ClientType clienttype,
            IntHolder intHolder) throws AdminLimitExceeded
    {
        // may throws AdminLimitExceeded
        fireCreateProxyRequestEvent();

        try
        {
            AbstractProxy _servant = obtain_notification_push_consumer_servant(clienttype);

            intHolder.value = _servant.getID().intValue();

            return ProxyConsumerHelper.narrow(_servant.activate());
        } catch (Exception e)
        {
            logger_.fatalError("obtain_notification_push_consumer: unexpected error", e);

            throw new UNKNOWN();
        }
    }

    private AbstractProxy obtain_notification_push_consumer_servant(ClientType clientType)
            throws Exception
    {
        AbstractProxy _servant = newProxyPushConsumer(clientType);

        configureInterFilterGroupOperator(_servant);

        configureQoS(_servant);

        addProxyToMap(_servant, pushServants_, modifyProxiesLock_);

        return _servant;
    }

    /**
     * get a ProxyPushConsumer (EventService Style)
     */
    public ProxyPushConsumer obtain_push_consumer()
    {
        try
        {
            MutablePicoContainer _container = newContainerForEventStyleProxy();

            _container.registerComponentImplementation(AbstractProxyConsumer.class,
                    ECProxyPushConsumerImpl.class);

            AbstractProxyConsumer _servant = (AbstractProxyConsumer) _container
                    .getComponentInstanceOfType(AbstractProxyConsumer.class);

            _servant.setSubsequentDestinations(CollectionsWrapper.singletonList(this));

            addProxyToMap(_servant, pushServants_, modifyProxiesLock_);

            return org.omg.CosEventChannelAdmin.ProxyPushConsumerHelper.narrow(_servant.activate());
        } catch (Exception e)
        {
            logger_.fatalError("obtain_push_consumer: unexpected error", e);

            throw new UNKNOWN();
        }
    }

    /**
     * get a ProxyPullConsumer (EventService Style)
     */
    public ProxyPullConsumer obtain_pull_consumer()
    {
        try
        {
            MutablePicoContainer _container = newContainerForEventStyleProxy();

            _container.registerComponentImplementation(AbstractProxyConsumer.class,
                    ECProxyPullConsumerImpl.class);

            AbstractProxyConsumer _servant = (AbstractProxyConsumer) _container
                    .getComponentInstanceOfType(AbstractProxyConsumer.class);

            _servant.setSubsequentDestinations(CollectionsWrapper.singletonList(this));

            addProxyToMap(_servant, pushServants_, modifyProxiesLock_);

            _servant.set_qos(get_qos());

            return org.omg.CosEventChannelAdmin.ProxyPullConsumerHelper.narrow(_servant.activate());
        } catch (Exception e)
        {
            logger_.fatalError("obtain_pull_consumer: unexpected error", e);

            throw new UNKNOWN();
        }
    }

    // //////////////////////////////////////

    public List getSubsequentFilterStages()
    {
        return subsequentFilterStagesSource_.getSubsequentFilterStages();
    }

    public void setSubsequentFilterStageSource(FilterStageSource source)
    {
        subsequentFilterStagesSource_ = source;
    }

    /**
     * SupplierAdmin does not ever have a MessageConsumer.
     */
    public MessageConsumer getMessageConsumer()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * SupplierAdmin does not ever have a MessageConsumer.
     */
    public boolean hasMessageConsumer()
    {
        return false;
    }

    public boolean hasInterFilterGroupOperatorOR()
    {
        return false;
    }

    /**
     * factory method to create new ProxyPullConsumerServants.
     */
    AbstractProxy newProxyPullConsumer(ClientType clientType)
    {
        final AbstractProxyConsumer _servant;

        final Class _clazz;

        switch (clientType.value()) {
        case ClientType._ANY_EVENT:
            _clazz = ProxyPullConsumerImpl.class;
            break;
        case ClientType._STRUCTURED_EVENT:
            _clazz = StructuredProxyPullConsumerImpl.class;
            break;
        case ClientType._SEQUENCE_EVENT:
            _clazz = SequenceProxyPullConsumerImpl.class;
            break;
        default:
            throw new BAD_PARAM("Invalid ClientType: ClientType." + clientType.value());
        }

        final MutablePicoContainer _containerForProxy = newContainerForNotifyStyleProxy();

        _containerForProxy.registerComponentImplementation(AbstractProxyConsumer.class, _clazz);

        _servant = (AbstractProxyConsumer) _containerForProxy
                .getComponentInstanceOfType(AbstractProxyConsumer.class);

        _servant.setSubsequentDestinations(CollectionsWrapper.singletonList(this));

        return _servant;
    }

    /**
     * factory method to create new ProxyPushConsumerServants.
     */
    AbstractProxy newProxyPushConsumer(ClientType clientType)
    {
        final AbstractProxyConsumer _servant;

        final Class _proxyClazz;

        switch (clientType.value()) {
        case ClientType._ANY_EVENT:
            _proxyClazz = ProxyPushConsumerImpl.class;
            break;
        case ClientType._STRUCTURED_EVENT:
            _proxyClazz = StructuredProxyPushConsumerImpl.class;
            break;
        case ClientType._SEQUENCE_EVENT:
            _proxyClazz = SequenceProxyPushConsumerImpl.class;
            break;
        default:
            throw new BAD_PARAM("Invalid ClientType: ClientType." + clientType.value());
        }

        final MutablePicoContainer _containerForProxy = newContainerForNotifyStyleProxy();

        _containerForProxy
                .registerComponentImplementation(AbstractProxyConsumer.class, _proxyClazz);

        _servant = (AbstractProxyConsumer) _containerForProxy
                .getComponentInstanceOfType(AbstractProxyConsumer.class);

        _servant.setSubsequentDestinations(CollectionsWrapper.singletonList(this));

        return _servant;
    }

    public String getMBeanType()
    {
        return "SupplierAdmin";
    }
}