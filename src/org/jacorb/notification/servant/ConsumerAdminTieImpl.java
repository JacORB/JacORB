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
import java.util.Map;

import org.jacorb.notification.ChannelContext;
import org.jacorb.notification.FilterManager;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.MessageConsumer;
import org.jacorb.notification.interfaces.ProxyEvent;
import org.jacorb.notification.interfaces.ProxyEventListener;
import org.jacorb.notification.util.TaskExecutor;

import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.IntHolder;
import org.omg.CosEventChannelAdmin.ProxyPullSupplier;
import org.omg.CosEventChannelAdmin.ProxyPushSupplier;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.ConsumerAdminHelper;
import org.omg.CosNotifyChannelAdmin.ConsumerAdminOperations;
import org.omg.CosNotifyChannelAdmin.ConsumerAdminPOATie;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosNotifyChannelAdmin.ProxyNotFound;
import org.omg.CosNotifyChannelAdmin.ProxySupplier;
import org.omg.CosNotifyChannelAdmin.ProxySupplierHelper;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.PortableServer.Servant;
import org.omg.CORBA.NO_IMPLEMENT;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ConsumerAdminTieImpl
            extends AbstractAdmin
            implements ConsumerAdminOperations,
            Disposable,
            ProxyEventListener
{
    ConsumerAdmin thisRef_;

    ConsumerAdminPOATie thisServant_;

    FilterStageListManager listManager_;

    MappingFilter priorityFilter_;

    MappingFilter lifetimeFilter_;

    ////////////////////////////////////////

    public ConsumerAdminTieImpl(ChannelContext channelContext)
    {
        super(channelContext);

        listManager_ = new FilterStageListManager()
                       {
                           public void fetchListData(FilterStageListManager.List listProxy)
                           {
                               Iterator i = pullServants_.entrySet().iterator();

                               while (i.hasNext())
                               {
                                   listProxy.add((FilterStage) ((Map.Entry)i.next()).getValue());
                               }


                               i = pushServants_.entrySet().iterator();

                               while (i.hasNext())
                               {
                                   listProxy.add((FilterStage) ((Map.Entry)i.next()).getValue());
                               }
                           }
                       };
    }

    ////////////////////////////////////////

    public synchronized Servant getServant()
    {
        if ( thisServant_ == null )
        {
            thisServant_ = new ConsumerAdminPOATie( this );
        }

        return thisServant_;
    }


    public void preActivate()
    {
    }


    public org.omg.CORBA.Object activate()
    {
        if ( thisRef_ == null )
        {
            thisRef_ = ConsumerAdminHelper.narrow(getServant()._this_object( getORB() ));
        }

        return thisRef_;
    }


    public void subscription_change( EventType[] eventType1,
                                     EventType[] eventType2 )
        throws InvalidEventType
        {
            throw new NO_IMPLEMENT();
        }


    public ProxySupplier get_proxy_supplier( int key ) throws ProxyNotFound
    {
        return ProxySupplierHelper.narrow(getProxy(key).activate());
    }


    public void lifetime_filter( MappingFilter lifetimeFilter )
    {
        lifetimeFilter_ = lifetimeFilter;
    }


    public MappingFilter lifetime_filter()
    {
        if (lifetimeFilter_ == null)
        {
            throw new BAD_PARAM("No lifetime filter set");
        }
        return lifetimeFilter_;
    }


    public MappingFilter priority_filter()
    {
        if (priorityFilter_ == null)
        {
            throw new BAD_PARAM("No priority filter set");
        }
        return priorityFilter_;
    }


    public void priority_filter( MappingFilter priorityFilter )
    {
        priorityFilter_ = priorityFilter;
    }


    public ProxySupplier obtain_notification_pull_supplier( ClientType clientType,
                                                            IntHolder intHolder )
        throws AdminLimitExceeded
    {
        try
        {
            AbstractProxy _servant =
                obtain_notification_pull_supplier_servant( clientType );

            intHolder.value = _servant.getKey().intValue();

            _servant.preActivate();

            ProxySupplier _proxySupplier = ProxySupplierHelper.narrow( _servant.activate() );

            return _proxySupplier;
        }
        catch (AdminLimitExceeded e)
        {
            throw e;
        }
        catch (Exception e) {
            logger_.fatalError("unexpected exception", e);
            throw new RuntimeException();
        }
    }


    private void configureMappingFilters(AbstractProxySupplier servant)
    {
        if (lifetimeFilter_ != null)
        {
            servant.lifetime_filter(lifetimeFilter_);
        }


        if (priorityFilter_ != null)
        {
            servant.priority_filter(priorityFilter_);
        }
    }


    public AbstractProxy obtain_notification_pull_supplier_servant( ClientType clientType )
        throws AdminLimitExceeded,
               UnsupportedQoS
    {
        // may throw AdminLimitExceeded
        fireCreateProxyRequestEvent();

        AbstractProxySupplier _servant;

        switch ( clientType.value() )
        {
            case ClientType._ANY_EVENT:
                _servant = new ProxyPullSupplierImpl( this,
                                                      channelContext_);
                break;

            case ClientType._STRUCTURED_EVENT:
                _servant =
                    new StructuredProxyPullSupplierImpl( this,
                                                         channelContext_);
                break;

            case ClientType._SEQUENCE_EVENT:
                _servant =
                    new SequenceProxyPullSupplierImpl( this,
                                                       channelContext_);

                break;

            default:
                throw new BAD_PARAM();
        }

        configureNotifyStyleID(_servant);

        configureMappingFilters(_servant);

        _servant.setTaskExecutor(TaskExecutor.getDefaultExecutor());

        configureQoS(_servant);

        configureInterFilterGroupOperator(_servant);

        //        _servant.addProxyDisposedEventListener( this );

//         if ( channelContext_.getRemoveProxySupplierListener() != null )
//         {
//             _servant.addProxyDisposedEventListener( channelContext_.getRemoveProxySupplierListener() );
//         }

        configureAdmin(_servant, modifyProxiesLock_, pullServants_);

        return _servant;
    }


    public int[] pull_suppliers()
    {
        return get_all_notify_proxies(pullServants_, modifyProxiesLock_);
    }


    public int[] push_suppliers()
    {
        return get_all_notify_proxies(pushServants_, modifyProxiesLock_);
    }


    public ProxySupplier obtain_notification_push_supplier( ClientType clientType,
                                                            IntHolder intHolder )
        throws AdminLimitExceeded
    {
        try
        {
            AbstractProxy _servant =
                obtain_notification_push_supplier_servant( clientType,
                                                           intHolder );

            Integer _key = _servant.getKey();

            if (logger_.isInfoEnabled())
            {
                logger_.info("created ProxyPushSupplier with ID: " + _key);
            }

            _servant.preActivate();

            ProxySupplier _proxySupplier =
                ProxySupplierHelper.narrow( _servant.activate() );

            return _proxySupplier;
        }
        catch (Exception e)
        {
            logger_.fatalError("could not create push_supplier", e);
            throw new RuntimeException();
        }
    }


    public AbstractProxy obtain_notification_push_supplier_servant( ClientType clientType,
                                                                    IntHolder intHolder )
        throws AdminLimitExceeded,
               UnsupportedQoS
    {
        // may throw exception if admin limit is exceeded
        fireCreateProxyRequestEvent();

        AbstractProxySupplier _servant;

        switch ( clientType.value() )
        {

            case ClientType._ANY_EVENT:
                _servant = new ProxyPushSupplierImpl( this,
                                                      channelContext_);
                break;

            case ClientType._STRUCTURED_EVENT:
                _servant =
                    new StructuredProxyPushSupplierImpl( this,
                                                         channelContext_);
                break;

            case ClientType._SEQUENCE_EVENT:
                _servant =
                    new SequenceProxyPushSupplierImpl( this,
                                                       channelContext_);
                break;

            default:
                throw new BAD_PARAM("The ClientType: " + clientType.value() + " is unknown");
        }

        configureNotifyStyleID(_servant);

        configureMappingFilters(_servant);

        intHolder.value = _servant.getKey().intValue();

        configureQoS(_servant);

        channelContext_.getTaskProcessor().configureTaskExecutor(_servant);

        configureInterFilterGroupOperator(_servant);

//         _servant.addProxyDisposedEventListener( this );

//         _servant.addProxyDisposedEventListener( channelContext_.getRemoveProxySupplierListener() );

        configureAdmin(_servant, modifyProxiesLock_, pushServants_);

        return _servant;
    }


    public ProxyPullSupplier obtain_pull_supplier()
    {
        try
        {
            ProxyPullSupplierImpl _servant =
                new ECProxyPullSupplierImpl( this,
                                             channelContext_,
                                             null );

            configureEventStyleID(_servant);

            configureQoS(_servant);

            configureAdmin(_servant, modifyProxiesLock_, pullServants_);

            _servant.setTaskExecutor(TaskExecutor.getDefaultExecutor());

            _servant.addProxyDisposedEventListener( this );

            _servant.preActivate();

            return org.omg.CosEventChannelAdmin.ProxyPullSupplierHelper.narrow( _servant.activate() );
        }
        catch (UnsupportedQoS e)
        {
            logger_.fatalError("Could not create PullSupplier", e);

            throw new RuntimeException();
        }
    }


    /**
     * get ProxyPushSupplier (EventStyle)
     */
    public ProxyPushSupplier obtain_push_supplier()
    {
        try
        {
            final ProxyPushSupplierImpl _servant =
                new ECProxyPushSupplierImpl( this,
                                             channelContext_);

            configureEventStyleID(_servant);

            configureQoS(_servant);

            configureAdmin(_servant, modifyProxiesLock_, pushServants_);

            channelContext_.getTaskProcessor().configureTaskExecutor(_servant);

            _servant.addProxyDisposedEventListener( this );

            _servant.preActivate();

            return org.omg.CosEventChannelAdmin.ProxyPushSupplierHelper.narrow( _servant.activate() );
        }
        catch (UnsupportedQoS e)
        {
            logger_.fatalError("Could not create ProxyPushSupplier", e);

            throw new RuntimeException();
        }
    }


    public List getSubsequentFilterStages()
    {
        return listManager_.getList();
    }


    /**
     * ConsumerAdmin never has a MessageConsumer
     */
    public MessageConsumer getMessageConsumer()
    {
        return null;
    }


    /**
     * ConsumerAdmin never has a MessageConsumer
     */
    public boolean hasMessageConsumer()
    {
        return false;
    }


    public boolean hasInterFilterGroupOperatorOR()
    {
        return (filterGroupOperator_ != null &&
                (filterGroupOperator_.value() == InterFilterGroupOperator._OR_OP) );
    }


    public void actionProxyDisposed( ProxyEvent event )
    {
        listManager_.actionSourceModified();
    }


    public void actionProxyCreated(ProxyEvent e)
    {
        // NO Op
    }


    private void configureAdmin(final AbstractProxy servant,
                                final Object lock,
                                final Map map) {
        synchronized(lock) {
            map.put(servant.getKey(), servant);
            listManager_.actionSourceModified();
        }

        servant.setDisposeHook(new Runnable() {
                public void run() {
                    synchronized(lock) {
                        map.remove(servant.getKey());

                        listManager_.actionSourceModified();
                    }
                }
            });
    }

}
