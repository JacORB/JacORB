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

import org.jacorb.notification.CollectionsWrapper;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.FilterStageSource;
import org.jacorb.notification.interfaces.MessageConsumer;

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

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class SupplierAdminImpl
    extends AbstractAdmin
    implements SupplierAdminOperations,
               Disposable
{
    protected Servant thisServant_;

    private SupplierAdmin thisCorbaRef_;

    private FilterStageSource subsequentFilterStagesSource_;

    ////////////////////////////////////////

    public synchronized Servant getServant()
    {
        if ( thisServant_ == null )
            {
                thisServant_ = new SupplierAdminPOATie( this );
            }

        return thisServant_;
    }


    public org.omg.CORBA.Object activate()
    {
        if ( thisCorbaRef_ == null )
            {
                thisCorbaRef_ = SupplierAdminHelper.narrow(getServant()._this_object( getORB() ));
            }

        return thisCorbaRef_;
    }


    public void offer_change( EventType[] added,
                              EventType[] removed )
        throws InvalidEventType
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


    public ProxyConsumer obtain_notification_pull_consumer( ClientType clientType,
                                                            IntHolder intHolder )
        throws AdminLimitExceeded
    {
        fireCreateProxyRequestEvent();

        try {
            AbstractProxy _servant = obtain_notification_pull_consumer_servant( clientType );

            intHolder.value = _servant.getID().intValue();

            _servant.preActivate();

            return ProxyConsumerHelper.narrow( _servant.activate() );
        } catch (Exception e) {
            logger_.fatalError("obtain_notification_pull_consumer: unexpected error", e);

            throw new UNKNOWN();
        }
    }


    private AbstractProxy obtain_notification_pull_consumer_servant( ClientType clientType ) throws Exception
    {
        AbstractProxy _servant = AbstractProxyConsumer.newProxyPullConsumer(this, clientType);

        configureManagers(_servant);

        configureNotifyStyleID(_servant);

        configureInterFilterGroupOperator(_servant);

        configureQoS(_servant);

        addProxyToMap(_servant, pullServants_, modifyProxiesLock_);

        return _servant;
    }


    public ProxyConsumer get_proxy_consumer( int id ) throws ProxyNotFound
    {
        return ProxyConsumerHelper.narrow(getProxy(id).activate());
    }


    public ProxyConsumer obtain_notification_push_consumer( ClientType clienttype,
                                                            IntHolder intHolder )
        throws AdminLimitExceeded
    {
        // may throws AdminLimitExceeded
        fireCreateProxyRequestEvent();

        try {
            AbstractProxy _servant =
                obtain_notification_push_consumer_servant( clienttype );

            intHolder.value =  _servant.getID().intValue();

            _servant.preActivate();

            return ProxyConsumerHelper.narrow( _servant.activate() );
        } catch (Exception e) {
            logger_.fatalError("obtain_notification_push_consumer: unexpected error", e);

            throw new UNKNOWN();
        }
    }


    private AbstractProxy obtain_notification_push_consumer_servant( ClientType clientType ) throws Exception
    {
        AbstractProxy _servant = AbstractProxyConsumer.newProxyPushConsumer(this, clientType);

        configureManagers(_servant);

        configureNotifyStyleID(_servant);

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
        try {
            AbstractProxyConsumer _servant =
                new ECProxyPushConsumerImpl();

            _servant.setSubsequentDestinations(CollectionsWrapper.singletonList(this));

            configureEventStyleID(_servant);

            addProxyToMap(_servant, pushServants_, modifyProxiesLock_);

            return org.omg.CosEventChannelAdmin.ProxyPushConsumerHelper.narrow( _servant.activate() );
        } catch (Exception e) {
            logger_.fatalError("obtain_push_consumer: unexpected error", e);

            throw new UNKNOWN();
        }
    }


    /**
     * get a ProxyPullConsumer (EventService Style)
     */
    public ProxyPullConsumer obtain_pull_consumer()
    {
        try {
            AbstractProxyConsumer _servant =
                new ECProxyPullConsumerImpl();

            _servant.setSubsequentDestinations(CollectionsWrapper.singletonList(this));

            configureEventStyleID(_servant);

            addProxyToMap(_servant, pushServants_, modifyProxiesLock_);

            _servant.set_qos(get_qos());

            _servant.preActivate();

            return org.omg.CosEventChannelAdmin.ProxyPullConsumerHelper.narrow( _servant.activate() );
        } catch (Exception e) {
            logger_.fatalError("obtain_pull_consumer: unexpected error", e);

            throw new UNKNOWN();
        }
    }

    ////////////////////////////////////////

    public List getSubsequentFilterStages()
    {
        return subsequentFilterStagesSource_.getSubsequentFilterStages();
    }


    public void setSubsequentFilterStageSource(FilterStageSource source) {
        subsequentFilterStagesSource_ = source;
    }


    /**
     * SupplierAdmin does not ever have a MessageConsumer.
     */
    public MessageConsumer getMessageConsumer()
    {
        return null;
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
}
