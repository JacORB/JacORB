package org.jacorb.notification;

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

import org.omg.CosNotifyChannelAdmin.NotConnected;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyInactive;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyActive;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyChannelAdmin.ProxyPullConsumerOperations;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import java.util.List;
import java.util.Collections;
import org.omg.CosEventComm.PullSupplier;
import org.jacorb.notification.interfaces.EventConsumer;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.Any;
import org.jacorb.notification.interfaces.TimerEventSupplier;
import org.jacorb.notification.engine.TaskProcessor;
import org.omg.CosEventComm.Disconnected;
import org.omg.PortableServer.Servant;
import org.omg.CosNotifyChannelAdmin.ProxyPullConsumerPOATie;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ProxyPullConsumerImpl
            extends ProxyBase
            implements ProxyPullConsumerOperations,
            org.omg.CosEventChannelAdmin.ProxyPullConsumerOperations,
            TimerEventSupplier
{

    private org.omg.CosEventComm.PullSupplier myPullSupplier_;

    private boolean connected_ = false;
    private boolean active_ = false;
    private long pollInterval_ = 1000L;
    private List subsequentDestinations_;
    private Object taskId_;
    private TaskProcessor engine_;
    private Runnable runQueueThis_;

    ProxyPullConsumerImpl( SupplierAdminTieImpl adminServant,
                           ApplicationContext appContext,
                           ChannelContext channelContext,
                           PropertyManager adminProperties,
                           PropertyManager qosProperties,
                           Integer key )
    {

        super( adminServant,
               appContext,
               channelContext,
               adminProperties,
               qosProperties,
               key );

        init( channelContext );
    }

    ProxyPullConsumerImpl( SupplierAdminTieImpl adminServant,
                           ApplicationContext appContext,
                           ChannelContext channelContext,
                           PropertyManager adminProperties,
                           PropertyManager qosProperties )
    {

        super( adminServant,
               appContext,
               channelContext,
               adminProperties,
               qosProperties );

        init( channelContext );
    }

    private void init( ChannelContext channelContext )
    {
        engine_ = channelContext.getTaskProcessor();

        runQueueThis_ = new Runnable()
                        {
                            public void run()
                            {
                                try
                                {
                                    engine_.scheduleTimedPullTask( ProxyPullConsumerImpl.this );
                                }
                                catch ( InterruptedException ie )
                                {}

                            }

                        }

                        ;

        connected_ = false;
        subsequentDestinations_ = Collections.singletonList( myAdmin_ );
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
                event = myPullSupplier_.try_pull( hasEvent );

                if ( hasEvent.value )
                {
                    logger_.debug( "pulled event" );

                    NotificationEvent _notifyEvent =
                        notificationEventFactory_.newEvent( event, this );

                    channelContext_.dispatchEvent( _notifyEvent );
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

    public void connect_pull_supplier( PullSupplier pullSupplier )
    throws AlreadyConnected
    {
        connect_any_pull_supplier( pullSupplier );
    }

    public SupplierAdmin MyAdmin()
    {
        return ( SupplierAdmin ) myAdmin_.getThisRef();
    }

    public List getSubsequentFilterStages()
    {
        return subsequentDestinations_;
    }

    public EventConsumer getEventConsumer()
    {
        return null;
    }

    public boolean hasEventConsumer()
    {
        return false;
    }

    synchronized private void startTask()
    {
        if ( taskId_ == null )
        {
            taskId_ = channelContext_
                      .getTaskProcessor()
                      .registerPeriodicTask( pollInterval_, runQueueThis_, true );
        }
    }

    synchronized private void stopTask()
    {
        if ( taskId_ != null )
        {
            channelContext_.getTaskProcessor().unregisterTask( taskId_ );
            taskId_ = null;
        }
    }

    public void dispose()
    {
        super.dispose();
        stopTask();
        disconnectClient();
    }

    public Servant getServant()
    {
        if ( thisServant_ == null )
        {
            synchronized ( this )
            {
                if ( thisServant_ == null )
                {
                    thisServant_ = new ProxyPullConsumerPOATie( this );
                }
            }
        }

        return thisServant_;
    }

    public void setServant( Servant servant )
    {
        thisServant_ = servant;
    }
}
