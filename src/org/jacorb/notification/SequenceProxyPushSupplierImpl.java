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

import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.EventConsumer;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.TimerEventConsumer;
import org.jacorb.util.Environment;

import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.MaximumBatchSize;
import org.omg.CosNotification.PacingInterval;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyActive;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyInactive;
import org.omg.CosNotifyChannelAdmin.NotConnected;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPushSupplierOperations;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPushSupplierPOATie;
import org.omg.CosNotifyComm.SequencePushConsumer;
import org.omg.PortableServer.Servant;
import org.omg.TimeBase.TimeTHelper;

/**
 * SequenceProxyPushSupplierImpl.java
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class SequenceProxyPushSupplierImpl
            extends StructuredProxyPushSupplierImpl
            implements SequenceProxyPushSupplierOperations,
            EventConsumer,
            TimerEventConsumer
{

    static final StructuredEvent[] STRUCTURED_EVENT_ARRAY_TEMPLATE =
        new StructuredEvent[ 0 ];

    /**
     * The connected SequencePushConsumer.
     */
    private SequencePushConsumer sequencePushConsumer_;

    /**
     * maximum queue size before a delivery is forced.
     */
    private int maxBatchSize_;

    /**
     * how long to wait between two scheduled deliveries.
     */
    private long pacingInterval_;

    /**
     * registration for the Scheduled DeliverTask.
     */
    private Object taskId_;

    /**
     * this callback is called by the TimerDaemon. Check if there are
     * pending Events and deliver them to the Consumer. As there's only one
     * TimerDaemon its important to
     * block it only a minimal amount of time. Therefor the Callback
     * should not do the actual delivery. Instead schedule a
     * DeliverTask for this Supplier.
     */
    private Runnable timerCallback_;

    private boolean delivering_;

    final TaskProcessor engine_;

    public SequenceProxyPushSupplierImpl( ConsumerAdminTieImpl myAdminServant,
                                          ApplicationContext appContext,
                                          ChannelContext channelContext,
                                          PropertyManager adminProperties,
                                          PropertyManager qosProperties,
                                          Integer key ) throws UnsupportedQoS
    {
        super( myAdminServant,
               appContext,
               channelContext,
               adminProperties,
               qosProperties,
               key );

        setProxyType( ProxyType.PUSH_SEQUENCE );

        engine_ = channelContext.getTaskProcessor();

        configureMaxBatchSize();

        configurePacingInterval();

        // configure the callback
        timerCallback_ = new Runnable()
                   {
                       public void run()
                       {
                           try
                           {
                               engine_.scheduleTimedPushTask( SequenceProxyPushSupplierImpl.this );
                           }
                           catch ( InterruptedException e ) {}
                       }
                   };
    }

    // overwrite
    public void deliverEvent( Message event )
    {
        logger_.debug( "deliverEvent(...)" );

        if ( connected_ )
        {
            try
            {
                pendingEvents_.put( event );

                if ( logger_.isDebugEnabled() )
                {
                    logger_.debug( "added to pendingEvents: "
                                   + pendingEvents_.getSize() );

                    logger_.debug( "maxBatchSize: "
                                   + maxBatchSize_
                                   + " Active: "
                                   + active_ );
                }

                if ( active_ && ( pendingEvents_.getSize() >= maxBatchSize_ ) )
                {
                    deliverPendingEvents();
                }
            }
            catch ( NotConnected d )
            {
                connected_ = false;
                logger_.debug( "push failed - Recipient is Disconnected" );
            }
        }
        else
        {
            logger_.debug( "Not connected" );
        }
    }

    public void runDeliverEvent() throws NotConnected
    {
        deliverPendingEvents();
    }

    public boolean hasPendingEvents() {
        return !pendingEvents_.isEmpty();
    }

    public void deliverPendingEvents() throws NotConnected
    {
        logger_.debug( "deliverPendingEvents()" );

        if ( !delivering_ )
        {
            synchronized ( this )
            {
                if ( !delivering_ )
                {
                    delivering_ = true;
                    StructuredEvent[] _eventsToDeliver;

                    if ( hasPendingEvents() )
                    {
                        synchronized ( pendingEvents_ )
                        {
                            int _deliverBatchSize =
                                ( pendingEvents_.getSize() > maxBatchSize_ ) ?
                                maxBatchSize_ :
                                pendingEvents_.getSize();

                            _eventsToDeliver =
                                new StructuredEvent[ _deliverBatchSize ];

                            Message[] _notificationEvents = null;

                            try {
                                _notificationEvents =
                                    pendingEvents_.getEvents(_deliverBatchSize, true);
                            } catch (InterruptedException e) {}

                            for ( int x = 0; x < _deliverBatchSize; ++x )
                                {
                                    _eventsToDeliver[ x ] =
                                        _notificationEvents[x].toStructuredEvent();

                                    _notificationEvents[x].dispose();
                                    _notificationEvents[x] = null;
                                }
                        }

                        try
                            {
                            sequencePushConsumer_.push_structured_events( _eventsToDeliver );
                        }
                        catch ( Disconnected d )
                        {
                            throw new NotConnected();
                        }
                    }
                    delivering_ = false;
                }
            }
        }
    }

    // new
    public void connect_sequence_push_consumer( SequencePushConsumer consumer )
    throws AlreadyConnected,
                TypeError
    {
        logger_.debug( "connect_sequence_push_consumer" );

        if ( connected_ )
        {
            throw new AlreadyConnected();
        }

        sequencePushConsumer_ = consumer;
        connected_ = true;
        active_ = true;

        startCronJob();
    }

    // overwrite
    public void resume_connection()
        throws NotConnected,
               ConnectionAlreadyActive
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

        if ( hasPendingEvents() )
        {
            deliverPendingEvents();
        }

        startCronJob();
    }

    public void suspend_connection()
        throws NotConnected,
               ConnectionAlreadyInactive
    {
        super.suspend_connection();
        stopCronJob();
    }

    public void disconnect_sequence_push_supplier()
    {
        dispose();
    }

    // overwrite
    protected void disconnectClient()
    {
        if ( connected_ )
        {
            if ( sequencePushConsumer_ != null )
            {
                sequencePushConsumer_.disconnect_sequence_push_consumer();
                sequencePushConsumer_ = null;
                connected_ = false;
                stopCronJob();
            }
        }
    }

    private void startCronJob()
    {
        if ( pacingInterval_ > 0 )
        {
            taskId_ = channelContext_.getTaskProcessor().
                executeTaskPeriodically( pacingInterval_,
                                      timerCallback_,
                                      true );
        }
    }

    synchronized private void stopCronJob()
    {
        if ( taskId_ != null )
        {
            channelContext_.getTaskProcessor().cancelTask( taskId_ );
            taskId_ = null;
        }
    }

    private boolean configurePacingInterval()
    {
        if ( qosProperties_.hasProperty(PacingInterval.value)) {
            long _pacingInterval =
                TimeTHelper.extract( qosProperties_.getProperty( PacingInterval.value ) );

            if ( pacingInterval_ != _pacingInterval )
                {
                    pacingInterval_ = _pacingInterval;
                    return true;
                }
        }
        return false;
    }

    private boolean configureMaxBatchSize()
    {
        int _maxBatchSize;

        if (qosProperties_.hasProperty(MaximumBatchSize.value)) {
            _maxBatchSize = qosProperties_.getProperty( MaximumBatchSize.value ).extract_long();
        } else {
            _maxBatchSize = Environment.getIntPropertyWithDefault(ConfigurableProperties.MAX_BATCH_SIZE,
                                                                  Constants.DEFAULT_MAX_BATCH_SIZE);
        }

        if ( maxBatchSize_ != _maxBatchSize )
            {
                logger_.info("Set MaxBatchSize to: " + _maxBatchSize);
                maxBatchSize_ = _maxBatchSize;
                return true;
            }
    return false;
    }

    public void enableDelivery()
    {}

    public void disableDelivery()
    {}

    public Servant getServant()
    {
        if ( thisServant_ == null )
        {
            synchronized ( this )
            {
                if ( thisServant_ == null )
                {
                    thisServant_ = new SequenceProxyPushSupplierPOATie( this );
                }
            }
        }
        return thisServant_;
    }

}
