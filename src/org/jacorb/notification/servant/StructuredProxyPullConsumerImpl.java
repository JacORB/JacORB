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


import org.jacorb.notification.ChannelContext;
import org.jacorb.notification.conf.Configuration;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.TimerEventSupplier;
import org.jacorb.util.Environment;

import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyActive;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyInactive;
import org.omg.CosNotifyChannelAdmin.NotConnected;
import org.omg.CosNotifyChannelAdmin.ObtainInfoMode;
import org.omg.CosNotifyChannelAdmin.ProxyConsumerHelper;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullConsumerOperations;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullConsumerPOATie;
import org.omg.CosNotifyComm.NotifySubscribeHelper;
import org.omg.CosNotifyComm.NotifySubscribeOperations;
import org.omg.CosNotifyComm.StructuredPullSupplier;
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
               TimerEventSupplier
{
    protected Sync pullSync_ = new Semaphore(Default.DEFAULT_CONCURRENT_PULL_OPERATIONS_ALLOWED);

    protected long pollInterval_;

    protected boolean active_ = true;

    private StructuredPullSupplier pullSupplier_;

    private NotifySubscribeOperations subscriptionListener_;

    private Object taskId_;

    private Runnable runQueueThis_;

    private final TaskProcessor engine_;

    ////////////////////////////////////////

    public StructuredProxyPullConsumerImpl( AbstractAdmin admin,
                                            ChannelContext channelContext)
    {
        super( admin,
               channelContext);

        configurePollIntervall();

        setProxyType( ProxyType.PULL_STRUCTURED );

        engine_ = channelContext.getTaskProcessor();

        runQueueThis_ = new Runnable()
                        {
                            public void run()
                            {
                                try
                                {
                                    engine_.scheduleTimedPullTask( StructuredProxyPullConsumerImpl.this );
                                }
                                catch ( InterruptedException ie )
                                {}
                            }
                        };
    }

    ////////////////////////////////////////

    private void configurePollIntervall() {
        pollInterval_ = Default.DEFAULT_PROXY_POLL_INTERVALL;

        if (Environment.getProperty(Configuration.PULL_CONSUMER_POLLINTERVALL) != null)
            {
                try
                    {
                        pollInterval_ =
                            Long.parseLong(Environment.getProperty(Configuration.PULL_CONSUMER_POLLINTERVALL));
                    }
                catch (NumberFormatException e)
                    {
                        logger_.error("Invalid Number Format for Property "
                                      + Configuration.PULL_CONSUMER_POLLINTERVALL,
                                      e);
                    }
            }
    }


    public void disconnect_structured_pull_consumer()
    {
        dispose();
    }


    public synchronized void connect_structured_pull_supplier( StructuredPullSupplier pullSupplier )
        throws AlreadyConnected
    {
        if ( connected_ )
        {
            throw new AlreadyConnected();
        }

        connected_ = true;
        active_ = true;

        pullSupplier_ = pullSupplier;

        try {
            subscriptionListener_ = NotifySubscribeHelper.narrow(pullSupplier);
        } catch (Throwable t) {}

        startTask();
    }


    public synchronized void suspend_connection()
        throws NotConnected,
               ConnectionAlreadyInactive
    {
        if ( !connected_ )
        {
            throw new NotConnected();
        }

        if ( !active_ )
        {
            throw new ConnectionAlreadyInactive();
        }

        active_ = false;

        stopTask();
    }


    public synchronized void resume_connection()
        throws ConnectionAlreadyActive,
               NotConnected
    {
        if ( !connected_ )
        {
            throw new NotConnected();
        }

        if ( active_ )
        {
            throw new ConnectionAlreadyActive();
        }

        active_ = true;

        startTask();
    }


    public EventType[] obtain_subscription_types( ObtainInfoMode obtainInfoMode )
    {
        throw new NO_IMPLEMENT();
    }


    public void runPullEvent()
    {
        synchronized ( this )
        {
            if (!connected_ || !active_)
            {
                return;
            }
        }

        try
        {
            runPullEventInternal();
        }
        catch (InterruptedException e)
        {
            logger_.error("pull interrupted", e);
        }
        catch (Disconnected e)
        {
            logger_.error("supplier thinks its disconnected. we think its connected.", e);

            synchronized (this)
            {
                connected_ = false;
            }
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
                messageFactory_.newMessage( _event, this );

            getTaskProcessor().processMessage( _notifyEvent );
        }
    }


    protected void disconnectClient()
    {
        if ( connected_ )
        {
            stopTask();
            pullSupplier_.disconnect_structured_pull_supplier();

            pullSupplier_ = null;
            connected_ = false;
        }
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
            getTaskProcessor()
                .cancelTask( taskId_ );

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


    NotifySubscribeOperations getSubscriptionListener() {
        return subscriptionListener_;
    }
}
