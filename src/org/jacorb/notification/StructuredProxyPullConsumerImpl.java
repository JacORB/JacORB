package org.jacorb.notification;

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

import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.EventConsumer;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.TimerEventSupplier;
import org.jacorb.util.Environment;

import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyActive;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyInactive;
import org.omg.CosNotifyChannelAdmin.NotConnected;
import org.omg.CosNotifyChannelAdmin.ObtainInfoMode;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullConsumerOperations;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullConsumerPOATie;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyComm.StructuredPullSupplier;
import org.omg.PortableServer.Servant;

/**
 * StructuredProxyPullConsumerImpl.java
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class StructuredProxyPullConsumerImpl
    extends AbstractProxy
    implements StructuredProxyPullConsumerOperations,
               TimerEventSupplier
{
    protected long pollInterval_;
    protected boolean active_ = true;

    private StructuredPullSupplier mySupplier_;
    private List subsequentDestinations_;

    private Object taskId_;
    private Runnable runQueueThis_;
    private final TaskProcessor engine_;

    public StructuredProxyPullConsumerImpl( SupplierAdminTieImpl supplierAdminServant,
                                            ApplicationContext appContext,
                                            ChannelContext channelContext,
                                            PropertyManager adminProperties,
                                            PropertyManager qosProperties,
                                            Integer key )
    {
        super( supplierAdminServant,
               appContext,
               channelContext,
               adminProperties,
               qosProperties,
               key );

        pollInterval_ = Constants.DEFAULT_PROXY_POLL_INTERVALL;

        if (Environment.getProperty(ConfigurableProperties.PULL_CONSUMER_POLLINTERVALL) != null) {
            try {
                pollInterval_ =
                    Long.parseLong(Environment.getProperty(ConfigurableProperties.PULL_CONSUMER_POLLINTERVALL));
            } catch (NumberFormatException e) {
                logger_.error("Invalid Number Format for Property "
                              + ConfigurableProperties.PULL_CONSUMER_POLLINTERVALL,
                              e);
            }
        }

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
                                catch ( InterruptedException ie ) {
                                }
                            }
                        };

        subsequentDestinations_ = CollectionsWrapper.singletonList( myAdmin_ );
    }

    public void disconnect_structured_pull_consumer()
    {
        dispose();
    }

    public void connect_structured_pull_supplier( StructuredPullSupplier structuredPullSupplier )
        throws AlreadyConnected
    {

        if ( connected_ )
        {
            throw new AlreadyConnected();
        }

        connected_ = true;
        active_ = true;

        mySupplier_ = structuredPullSupplier;
        startTask();
    }

    public void suspend_connection()
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

    public void resume_connection()
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

    public SupplierAdmin MyAdmin()
    {
        return ( SupplierAdmin ) myAdmin_.getThisRef();
    }

    public EventType[] obtain_subscription_types( ObtainInfoMode obtainInfoMode )
    {
        return null;
    }

    public void validate_event_qos( Property[] property1,
                                    NamedPropertyRangeSeqHolder namedPropertyRangeSeqHolder )
        throws UnsupportedQoS
    {
    }

    public void runPullEvent()
    {
        runStructured();
    }

    public void runStructured()
    {
        BooleanHolder _hasEvent = new BooleanHolder();
        StructuredEvent _event = null;

        synchronized ( this )
        {
            if ( connected_ && active_ )
            {
                try
                {
                    _hasEvent.value = false;
                    _event = mySupplier_.try_pull_structured_event( _hasEvent );
                }
                catch ( UserException ex )
                {
                    connected_ = false;
                    return ;
                }
                catch ( SystemException sysEx )
                {
                    connected_ = false;
                    return ;
                }

                if ( _hasEvent.value )
                {
                    logger_.debug( "pulled Event" );

                    Message _notifyEvent =
                        notificationEventFactory_.newEvent( _event, this );

                    channelContext_.dispatchEvent( _notifyEvent );
                }
            }
        }
    }

    public List getSubsequentFilterStages()
    {
        return subsequentDestinations_;
    }

    public EventConsumer getEventConsumer()
    {
        throw new UnsupportedOperationException();
    }

    public boolean hasEventConsumer()
    {
        return false;
    }

    protected void disconnectClient()
    {
        if ( connected_ )
        {
            if ( myAdmin_ != null )
            {
                mySupplier_.disconnect_structured_pull_supplier();
                mySupplier_ = null;
            }
        }

        connected_ = false;
    }

    public void dispose()
    {
        super.dispose();
        stopTask();
        disconnectClient();
    }

    protected void startTask()
    {
        if ( taskId_ == null )
        {
            taskId_ = channelContext_
                .getTaskProcessor()
                .executeTaskPeriodically( pollInterval_,
                                          runQueueThis_,
                                          true );
        }
    }

    protected void stopTask()
    {
        if ( taskId_ != null )
        {
            channelContext_
                .getTaskProcessor()
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

}
