package org.jacorb.notification.servant;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageSupplier;
import org.omg.CORBA.Any;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosEventComm.PullSupplier;
import org.omg.CosNotifyChannelAdmin.ProxyConsumerHelper;
import org.omg.CosNotifyChannelAdmin.ProxyPullConsumerOperations;
import org.omg.CosNotifyChannelAdmin.ProxyPullConsumerPOATie;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.PortableServer.POA;
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
    /**
     * this sync is accessed during a pull operation. therby the
     * maximal number of concurrent pull operations per pull supplier
     * can be controlled.
     */
    private final Sync pullSync_ =
        new Semaphore(Default.DEFAULT_CONCURRENT_PULL_OPERATIONS_ALLOWED);

    /**
     * the connected PullSupplier
     */
    private PullSupplier pullSupplier_;
    private long pollInterval_;
    private Object timerRegistration_;

    /**
     * Callback that is run by the Timer.
     */
    private final Runnable runQueueThis_;

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

    public ProxyPullConsumerImpl(IAdmin admin, ORB orb, POA poa, Configuration conf, TaskProcessor taskProcessor, MessageFactory messageFactory, OfferManager offerManager, SubscriptionManager subscriptionManager)
    {
        super(admin, orb, poa, conf, taskProcessor, messageFactory, null, offerManager, subscriptionManager);

        
        pollInterval_ =
            conf.getAttributeAsLong (Attributes.PULL_CONSUMER_POLLINTERVALL,
                                     Default.DEFAULT_PROXY_POLL_INTERVALL);
    
        runQueueThis_ = new Runnable()
        {
            public void run()
            {
                schedulePullTask( ProxyPullConsumerImpl.this );
            }
        };    
    }

    ////////////////////////////////////////

    public ProxyType MyType() {
        return ProxyType.PULL_ANY;
    }


    public void disconnect_pull_consumer()
    {
        destroy();
    }


    protected void disconnectClient()
    {
        stopTask();

        pullSupplier_.disconnect_pull_supplier();

        pullSupplier_ = null;
    }


    protected void connectionSuspended()
    {
        stopTask();
    }


    protected void connectionResumed()
    {
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
                    getMessageFactory().newMessage( event, this );

                checkMessageProperties(_message);

                getTaskProcessor().processMessage( _message );
            }
    }


    public void connect_any_pull_supplier( PullSupplier pullSupplier )
        throws AlreadyConnected
    {
        checkIsNotConnected();

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
