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

import org.jacorb.notification.ChannelContext;
import org.jacorb.notification.conf.Configuration;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageSupplier;
import org.jacorb.util.Environment;

import org.omg.CORBA.Any;
import org.omg.CORBA.BooleanHolder;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosEventComm.PullSupplier;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyActive;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyInactive;
import org.omg.CosNotifyChannelAdmin.NotConnected;
import org.omg.CosNotifyChannelAdmin.ProxyConsumerHelper;
import org.omg.CosNotifyChannelAdmin.ProxyPullConsumerOperations;
import org.omg.CosNotifyChannelAdmin.ProxyPullConsumerPOATie;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyComm.NotifySubscribeHelper;
import org.omg.CosNotifyComm.NotifySubscribeOperations;
import org.omg.PortableServer.Servant;

import EDU.oswego.cs.dl.util.concurrent.Semaphore;
import EDU.oswego.cs.dl.util.concurrent.Sync;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ProxyPullConsumerImpl
    extends AbstractProxyConsumer
    implements ProxyPullConsumerOperations,
               MessageSupplier
{
    private Sync pullSync_ = new Semaphore(Default.DEFAULT_CONCURRENT_PULL_OPERATIONS_ALLOWED);

    /**
     * the connected PullSupplier
     */
    private PullSupplier pullSupplier_;

    /**
     * flag to indicate if the connection from this PullConsumer to
     * its PullSupplier is active.
     */
    private boolean isConnectionActive_ = false;

    private long pollInterval_;

    private Object timerRegistration_;

    /**
     * Callback that is run by the Timer.
     */
    private Runnable runQueueThis_;

    //////////////////////////////
    // Some Management Information

    /**
     * Total number of pull-Operations
     */
    private int pullCounter_;

    /**
     * Total time spent within pull-Operations
     */
    private long timeSpentInPull_;

    /**
     * Total number of successful pull-Operations
     */
    private int successfulPullCounter_;

    ////////////////////////////////////////

    ProxyPullConsumerImpl( AbstractAdmin adminServant,
                           ChannelContext channelContext)
    {
        super( adminServant,
               channelContext);

        setProxyType(ProxyType.PULL_ANY);

        configurePullIntervall();

        configureTimerCallback();
    }

    ////////////////////////////////////////

    private void configurePullIntervall() {
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
                                      + Configuration.PULL_CONSUMER_POLLINTERVALL, e);

                    }
            }
    }


    private void configureTimerCallback() {
        runQueueThis_ = new Runnable()
            {
                public void run()
                {
                    schedulePullTask( ProxyPullConsumerImpl.this );
                }
            };
    }


    public void disconnect_pull_consumer()
    {
        dispose();
    }


    protected void disconnectClient()
    {
        stopTask();

        pullSupplier_.disconnect_pull_supplier();

        pullSupplier_ = null;
    }


    synchronized public void suspend_connection()
        throws NotConnected,
               ConnectionAlreadyInactive
    {
        assertConnected();

        if ( !isConnectionActive_ )
        {
            throw new ConnectionAlreadyInactive();
        }

        isConnectionActive_ = false;

        stopTask();
    }


    synchronized public void resume_connection()
        throws ConnectionAlreadyActive,
               NotConnected
    {
        assertConnected();

        if ( isConnectionActive_ )
        {
            throw new ConnectionAlreadyActive();
        }

        startTask();
    }


    public void runPullMessage() throws Disconnected
    {
        if ( !isConnected() )
            {
                return;
            }

        try {
            runPullEventInternal();
        } catch (InterruptedException e) {
            logger_.error("pull was interrupted", e);
        }
    }


    private void runPullEventInternal()
        throws InterruptedException,
               Disconnected
    {
        BooleanHolder hasEvent = new BooleanHolder();
        Any event = null;

        try {
            pullSync_.acquire();

            ++pullCounter_;

            long _start = System.currentTimeMillis();

            event = pullSupplier_.try_pull( hasEvent );

            timeSpentInPull_ += System.currentTimeMillis() - _start;
        }
        finally {
            pullSync_.release();
        }

        if ( hasEvent.value )
            {
                ++successfulPullCounter_;

                Message _message =
                    messageFactory_.newMessage( event, this );

                checkMessageProperties(_message);

                getTaskProcessor().processMessage( _message );
            }
    }


    public void connect_any_pull_supplier( PullSupplier pullSupplier )
        throws AlreadyConnected
    {
        assertNotConnected();

        isConnectionActive_ = true;

        pullSupplier_ = pullSupplier;

        connectClient(pullSupplier);

        startTask();
    }


    synchronized private void startTask()
    {
        if ( timerRegistration_ == null )
        {
            timerRegistration_ =
                getTaskProcessor().executeTaskPeriodically( pollInterval_,
                                                            runQueueThis_,
                                                            true );
        }
    }


    synchronized private void stopTask()
    {
        if ( timerRegistration_ != null )
        {
            getTaskProcessor().cancelTask( timerRegistration_ );

            timerRegistration_ = null;
        }
    }


    public synchronized Servant getServant()
    {
        if ( thisServant_ == null )
        {
            thisServant_ = new ProxyPullConsumerPOATie( this );
        }

        return thisServant_;
    }


    public org.omg.CORBA.Object activate()
    {
        return ProxyConsumerHelper.narrow(getServant()._this_object(getORB()));
    }

    ////////////////////////////////////////
    // todo collect management informations

    public long getPollInterval()
    {
        return pollInterval_;
    }


    public long getPullTimer()
    {
        return timeSpentInPull_;
    }


    public int getPullCounter()
    {
        return pullCounter_;
    }


    public int getSuccessfulPullCounter()
    {
        return successfulPullCounter_;
    }
}
