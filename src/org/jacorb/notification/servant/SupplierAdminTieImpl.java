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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jacorb.notification.ChannelContext;
import org.jacorb.notification.FilterManager;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.MessageConsumer;
import org.jacorb.notification.interfaces.ProxyEvent;
import org.jacorb.notification.interfaces.ProxyEventListener;

import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.IntHolder;
import org.omg.CosEventChannelAdmin.ProxyPullConsumer;
import org.omg.CosEventChannelAdmin.ProxyPushConsumer;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosNotifyChannelAdmin.ProxyConsumer;
import org.omg.CosNotifyChannelAdmin.ProxyConsumerHelper;
import org.omg.CosNotifyChannelAdmin.ProxyNotFound;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyChannelAdmin.SupplierAdminHelper;
import org.omg.CosNotifyChannelAdmin.SupplierAdminOperations;
import org.omg.CosNotifyChannelAdmin.SupplierAdminPOATie;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.PortableServer.Servant;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.NO_IMPLEMENT;
import java.util.Map;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class SupplierAdminTieImpl
    extends AbstractAdmin
    implements SupplierAdminOperations,
               Disposable
{
    private SupplierAdminPOATie thisCorbaServant_;

    private SupplierAdmin thisCorbaRef_;

    private List listProxyEventListener_ = new ArrayList();

    ////////////////////////////////////////

    public SupplierAdminTieImpl(ChannelContext channelContext)
    {
        super(channelContext);
    }

    ////////////////////////////////////////

    public synchronized Servant getServant()
    {
        if ( thisCorbaServant_ == null )
            {
                thisCorbaServant_ = new SupplierAdminPOATie( this );
            }

        return thisCorbaServant_;
    }


    public void preActivate() {
    }


    public org.omg.CORBA.Object activate()
    {
        if ( thisCorbaRef_ == null )
            {
                thisCorbaRef_ = SupplierAdminHelper.narrow(getServant()._this_object( getORB() ));
            }

        return thisCorbaRef_;
    }

    public void offer_change( EventType[] eventType1,
                              EventType[] eventType2 )
        throws InvalidEventType
    {
        throw new NO_IMPLEMENT();
    }

    // Implementation of org.omg.CosNotifyChannelAdmin.SupplierAdminOperations

    /**
     * access the ids of all PullConsumers
     */
    public int[] pull_consumers()
    {
        return get_all_notify_proxies(pullServants_, modifyProxiesLock_);
    }


    /**
     * access the ids of all PushConsumers
     */
    public int[] push_consumers()
    {
        return get_all_notify_proxies(pushServants_, modifyProxiesLock_);
    }


    public ProxyConsumer obtain_notification_pull_consumer( ClientType clientType,
                                                            IntHolder intHolder )
        throws AdminLimitExceeded
    {
        try {
            AbstractProxy _servant = obtain_notification_pull_consumer_servant( clientType, intHolder );

            Integer _key = _servant.getKey();

            _servant.preActivate();

            ProxyConsumer _proxyConsumer = ProxyConsumerHelper.narrow( _servant.activate() );

            fireProxyCreated( _servant );

            return _proxyConsumer;
        } catch (AdminLimitExceeded e) {
            throw e;
        } catch (Exception e) {
            logger_.fatalError("unexpected exception", e);
            throw new RuntimeException();
        }
    }


    public AbstractProxy obtain_notification_pull_consumer_servant( ClientType clientType,
                                                                    IntHolder intHolder )
        throws AdminLimitExceeded
    {
        fireCreateProxyRequestEvent();

        AbstractProxy _servant;

        switch ( clientType.value() )
            {
            case ClientType._ANY_EVENT:
                _servant = new ProxyPullConsumerImpl( this,
                                                      channelContext_);
                break;
            case ClientType._STRUCTURED_EVENT:
                _servant =
                    new StructuredProxyPullConsumerImpl( this,
                                                         channelContext_);
                break;
            case ClientType._SEQUENCE_EVENT:
                _servant =
                    new SequenceProxyPullConsumerImpl( this,
                                                       channelContext_);
                break;
            default:
                throw new BAD_PARAM("ClientType: " + clientType.value() + " unknown");
            }

        configureNotifyStyleID(_servant);

        intHolder.value = _servant.getKey().intValue();

        configureInterFilterGroupOperator(_servant);

        configureQoS(_servant);

        configureAdmin(_servant, modifyProxiesLock_, pullServants_);

        return _servant;
    }


    public ProxyConsumer get_proxy_consumer( int key ) throws ProxyNotFound
    {
        return ProxyConsumerHelper.narrow(getProxy(key).activate());
    }


    public ProxyConsumer obtain_notification_push_consumer( ClientType clienttype,
                                                            IntHolder intholder )
        throws AdminLimitExceeded
    {
        try {
            AbstractProxy _servant = obtain_notification_push_consumer_servant( clienttype, intholder );
            Integer _key = _servant.getKey();

            _servant.preActivate();

            ProxyConsumer _proxyConsumer = ProxyConsumerHelper.narrow( _servant.activate() );

            fireProxyCreated( _servant );

            return _proxyConsumer;
        } catch (AdminLimitExceeded e) {
            throw e;
        } catch (Exception e) {
            logger_.fatalError("unexpected exception", e);
            throw new UNKNOWN();
        }
    }


    public AbstractProxy obtain_notification_push_consumer_servant( ClientType clientType,
                                                                    IntHolder intHolder )
        throws AdminLimitExceeded
    {
        // may throws AdminLimitExceeded
        fireCreateProxyRequestEvent();

        AbstractProxy _servant;

        switch ( clientType.value() )
            {
            case ClientType._ANY_EVENT:
                _servant = new ProxyPushConsumerImpl( this,
                                                      channelContext_);
                break;
            case ClientType._STRUCTURED_EVENT:
                _servant =
                    new StructuredProxyPushConsumerImpl( this,
                                                         channelContext_);
                break;
            case ClientType._SEQUENCE_EVENT:
                _servant =
                    new SequenceProxyPushConsumerImpl( this,
                                                       channelContext_);
                break;

            default:
                throw new BAD_PARAM();
            }

        configureNotifyStyleID(_servant);

        intHolder.value = _servant.getKey().intValue();

        configureInterFilterGroupOperator(_servant);

        configureQoS(_servant);

        configureAdmin(_servant, modifyProxiesLock_, pushServants_);

        return _servant;
    }


    private void configureAdmin(final AbstractProxy servant,
                                final Object lock,
                                final Map map) {
        synchronized(lock) {
            map.put(servant.getKey(), servant);
        }

        servant.setDisposeHook(new Runnable() {
                public void run() {
                    synchronized(lock) {
                        map.remove(servant.getKey());

                        fireProxyRemoved(servant);
                    }
                }
            });
    }

    // Implementation of org.omg.CosEventChannelAdmin.SupplierAdminOperations

    /**
     * get a ProxyPushConsumer (EventService Style)
     */
    public ProxyPushConsumer obtain_push_consumer()
    {
        try {
            final ProxyPushConsumerImpl _servant =
                new ECProxyPushConsumerImpl( this,
                                             channelContext_);

            configureEventStyleID(_servant);

            configureAdmin(_servant, modifyProxiesLock_, pushServants_);

            _servant.preActivate();

            ProxyPushConsumer _ret =
                org.omg.CosEventChannelAdmin.ProxyPushConsumerHelper.narrow( _servant.activate() );

            fireProxyCreated( _servant );

            return _ret;
        } catch (Exception e) {
            logger_.fatalError("unexpected exception", e);

            throw new RuntimeException();
        }
    }


    /**
     * get a ProxyPullConsumer (EventService Style)
     */
    public ProxyPullConsumer obtain_pull_consumer()
    {
        try {
            ECProxyPullConsumerImpl _servant =
                new ECProxyPullConsumerImpl( this,
                                             channelContext_);

            configureEventStyleID(_servant);

            configureAdmin(_servant, modifyProxiesLock_, pushServants_);

            _servant.preActivate();

            ProxyPullConsumer _ret =
                org.omg.CosEventChannelAdmin.ProxyPullConsumerHelper.narrow( _servant.activate() );

            fireProxyCreated( _servant );

            return _ret;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    ////////////////////////////////////////

    public List getSubsequentFilterStages()
    {
        return getChannelServant().getAllConsumerAdmins();
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


    void fireProxyRemoved( AbstractProxy b )
    {
        Iterator i = listProxyEventListener_.iterator();
        ProxyEvent e = new ProxyEvent( b );

        while ( i.hasNext() )
            {
                ( ( ProxyEventListener ) i.next() ).actionProxyDisposed( e );
            }
    }


    void fireProxyCreated( AbstractProxy b )
    {
        Iterator i = listProxyEventListener_.iterator();
        ProxyEvent e = new ProxyEvent( b );

        while ( i.hasNext() )
            {
                ( ( ProxyEventListener ) i.next() ).actionProxyCreated( e );
            }
    }


    public boolean hasInterFilterGroupOperatorOR()
    {
        return false;
    }


    public void addProxyEventListener( ProxyEventListener l )
    {
        listProxyEventListener_.add( l );
    }


    public void removeProxyEventListener( ProxyEventListener l )
    {
        listProxyEventListener_.remove( l );
    }
}
