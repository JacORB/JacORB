package org.jacorb.notification.servant;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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


import org.apache.avalon.framework.configuration.Configuration;
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageSupplier;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.ProxyConsumerHelper;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullConsumerOperations;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullConsumerPOATie;
import org.omg.CosNotifyComm.StructuredPullSupplier;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

import EDU.oswego.cs.dl.util.concurrent.Semaphore;
import EDU.oswego.cs.dl.util.concurrent.Sync;


/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class StructuredProxyPullConsumerImpl
    extends AbstractProxyConsumer
    implements StructuredProxyPullConsumerOperations,
               MessageSupplier
{
    protected final Sync pullSync_ = new Semaphore(Default.DEFAULT_CONCURRENT_PULL_OPERATIONS_ALLOWED);

    protected long pollInterval_;

    private StructuredPullSupplier pullSupplier_;

    private Object taskId_;

    private final Runnable runQueueThis_;

    ////////////////////////////////////////

    public StructuredProxyPullConsumerImpl(IAdmin admin, ORB orb, POA poa, Configuration conf, TaskProcessor taskProcessor, MessageFactory mf, OfferManager offerManager, SubscriptionManager subscriptionManager)
    {
        super(admin, orb, poa, conf, taskProcessor, mf, null, offerManager, subscriptionManager);

        runQueueThis_ = new Runnable()
        {
            public void run()
            {
                try
                {
                    getTaskProcessor().scheduleTimedPullTask(StructuredProxyPullConsumerImpl.this );
                }
                catch ( InterruptedException ie )
                {}
            }
        };
    }

    ////////////////////////////////////////

    public ProxyType MyType() {
        return ProxyType.PULL_STRUCTURED;
    }


    public void configure (Configuration conf)
    {
        super.configure (conf);

        pollInterval_ =
            conf.getAttributeAsLong (Attributes.PULL_CONSUMER_POLLINTERVALL,
                                        Default.DEFAULT_PROXY_POLL_INTERVALL);
    }


    public void disconnect_structured_pull_consumer()
    {
        destroy();
    }


    public synchronized void connect_structured_pull_supplier( StructuredPullSupplier pullSupplier )
        throws AlreadyConnected
    {
        checkIsNotConnected();
        pullSupplier_ = pullSupplier;
        connectClient(pullSupplier);
        startTask();
    }


    protected void connectionSuspended()
    {
        stopTask();
    }


    public void connectionResumed()
    {
        startTask();
    }


    public void runPullMessage() throws Disconnected
    {
        if (!isConnected() || isSuspended()) {
            return;
        }

        try
        {
            runPullEventInternal();
        }
        catch (InterruptedException e)
        {
            logger_.error("pull interrupted", e);
        }
    }


    protected void runPullEventInternal()
        throws InterruptedException,
               Disconnected
    {
        BooleanHolder _hasEvent = new BooleanHolder();
        _hasEvent.value = false;
        StructuredEvent _event = null;

        try
        {
            pullSync_.acquire();
            _event = pullSupplier_.try_pull_structured_event( _hasEvent );
        }
        finally
        {
            pullSync_.release();
        }

        if ( _hasEvent.value )
        {
            Message _notifyEvent =
                getMessageFactory().newMessage( _event, this );

            getTaskProcessor().processMessage( _notifyEvent );
        }
    }


    protected void disconnectClient()
    {
        stopTask();
        pullSupplier_.disconnect_structured_pull_supplier();

        pullSupplier_ = null;
    }


    protected void startTask()
    {
        if ( taskId_ == null )
        {
            taskId_ = getTaskProcessor()
                .executeTaskPeriodically( pollInterval_,
                                          runQueueThis_,
                                          true );
        }
    }


    protected void stopTask()
    {
        if ( taskId_ != null )
        {
            getTaskProcessor().cancelTask( taskId_ );
            taskId_ = null;
        }
    }


    public synchronized Servant getServant()
    {
        if ( thisServant_ == null )
        {
            thisServant_ = new StructuredProxyPullConsumerPOATie( this );
        }

        return thisServant_;
    }


    public org.omg.CORBA.Object activate()
    {
        return ProxyConsumerHelper.narrow(getServant()._this_object(getORB()));
    }
}
