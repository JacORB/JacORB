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

import java.util.Collections;
import java.util.List;

import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.MessageConsumer;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.TimerEventSupplier;
import org.jacorb.util.Environment;

import org.omg.CORBA.Any;
import org.omg.CORBA.BooleanHolder;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosEventComm.PullSupplier;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyActive;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyInactive;
import org.omg.CosNotifyChannelAdmin.NotConnected;
import org.omg.CosNotifyChannelAdmin.ProxyPullConsumerOperations;
import org.omg.CosNotifyChannelAdmin.ProxyPullConsumerPOATie;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.PortableServer.Servant;

import org.omg.CosNotifyChannelAdmin.ProxyConsumerHelper;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.ChannelContext;
import org.jacorb.notification.PropertyManager;
import org.jacorb.notification.conf.Configuration;
import org.jacorb.notification.CollectionsWrapper;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ProxyPullConsumerImpl
    extends AbstractProxy
    implements ProxyPullConsumerOperations,
               TimerEventSupplier
{
    private PullSupplier myPullSupplier_;

    private boolean active_ = false;

    private long pollInterval_;

    private List subsequentDestinations_;

    private Object taskId_;

    private TaskProcessor engine_;

    /**
     * Callback that is run by a Timer.
     */
    private Runnable runQueueThis_;

    /**
     * Total number of pull-Operations
     */
    private int runCounter_;

    /**
     * Total time spent within pull-Operations
     */
    private long runTime_;

    /**
     * Total number of successful pull-Operations
     */
    private int successfulPull_;

    ////////////////////////////////////////

    ProxyPullConsumerImpl( AbstractAdmin adminServant,
                           ChannelContext channelContext,
                           PropertyManager adminProperties,
                           PropertyManager qosProperties,
                           Integer key )
    {

        super( adminServant,
               channelContext,
               adminProperties,
               qosProperties,
               key,
               true);

        init( channelContext );
    }

    ProxyPullConsumerImpl( AbstractAdmin adminServant,
                           ChannelContext channelContext,
                           PropertyManager adminProperties,
                           PropertyManager qosProperties )
    {

        super( adminServant,
               channelContext,
               adminProperties,
               qosProperties );

        init( channelContext );
    }

    ////////////////////////////////////////

    private void init( ChannelContext channelContext )
    {
        pollInterval_ = Default.DEFAULT_PROXY_POLL_INTERVALL;

        if (Environment.getProperty(Configuration.PULL_CONSUMER_POLLINTERVALL) != null) {
            try {
                pollInterval_ =
                    Long.parseLong(Environment.getProperty(Configuration.PULL_CONSUMER_POLLINTERVALL));
            } catch (NumberFormatException e) {
                logger_.error("Invalid Number Format for Property "
                              + Configuration.PULL_CONSUMER_POLLINTERVALL, e);

            }
        }

        engine_ = channelContext.getTaskProcessor();

        runQueueThis_ = new Runnable()
            {
                public void run()
                {
                    try
                        {
                            engine_.scheduleTimedPullTask( ProxyPullConsumerImpl.this );
                        }
                    catch ( InterruptedException e )
                        {
                            logger_.debug("Interrupted", e);
                        }
                }
            };

        connected_ = false;

        subsequentDestinations_ = CollectionsWrapper.singletonList( myAdmin_ );
    }


    public void disconnect_pull_consumer()
    {
        dispose();
    }


    private void disconnectClient()
    {
        if ( myPullSupplier_ != null )
        {
            myPullSupplier_.disconnect_pull_supplier();
            myPullSupplier_ = null;
            connected_ = false;
            active_ = false;
        }
    }


    synchronized public void suspend_connection()
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


    synchronized public void resume_connection()
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

        startTask();
    }


    public void runPullEvent() throws Disconnected
    {
        BooleanHolder hasEvent = new BooleanHolder();
        Any event = null;

        synchronized ( this )
        {
            if ( connected_ )
            {
                ++runCounter_;

                long _start = System.currentTimeMillis();

                event = myPullSupplier_.try_pull( hasEvent );

                runTime_ += System.currentTimeMillis() - _start;

                if ( hasEvent.value )
                {
                    logger_.debug( "pulled event" );

                    ++successfulPull_;

                    Message _message =
                        messageFactory_.newMessage( event, this );

                    getTaskProcessor().processMessage( _message );
                }
            }
        }
    }


    public void connect_any_pull_supplier( PullSupplier pullSupplier )
        throws AlreadyConnected
    {
        if ( connected_ )
        {
            throw new AlreadyConnected();
        }
        else
        {
            connected_ = true;
            active_ = true;
            myPullSupplier_ = pullSupplier;
            startTask();
        }
    }


    public SupplierAdmin MyAdmin()
    {
        return ( SupplierAdmin ) myAdmin_.getCorbaRef();
    }


    public List getSubsequentFilterStages()
    {
        return subsequentDestinations_;
    }


    public MessageConsumer getMessageConsumer()
    {
        throw new UnsupportedOperationException();
    }


    public boolean hasMessageConsumer()
    {
        return false;
    }


    synchronized private void startTask()
    {
        if ( taskId_ == null )
        {
            taskId_ = getTaskProcessor()
                      .executeTaskPeriodically( pollInterval_, runQueueThis_, true );
        }
    }


    synchronized private void stopTask()
    {
        if ( taskId_ != null )
        {
            getTaskProcessor().cancelTask( taskId_ );
            taskId_ = null;
        }
    }


    public void dispose()
    {
        super.dispose();
        stopTask();
        disconnectClient();
    }


    public synchronized Servant getServant()
    {
        if ( thisServant_ == null )
            {
                thisServant_ = new ProxyPullConsumerPOATie( this );
            }

        return thisServant_;
    }


    public org.omg.CORBA.Object getCorbaRef() {
        return ProxyConsumerHelper.narrow(getServant()._this_object(getORB()));
    }


    public long getPollInterval() {
        return pollInterval_;
    }


    public long getPullTimer() {
        return runTime_;
    }


    public int getPullCounter() {
        return runCounter_;
    }


    public int getSuccessfulPullCounter() {
        return successfulPull_;
    }
}
