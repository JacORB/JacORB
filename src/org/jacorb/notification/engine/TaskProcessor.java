package org.jacorb.notification.engine;

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

import java.util.Date;

import org.jacorb.notification.ConfigurableProperties;
import org.jacorb.notification.Constants;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.MessageConsumer;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.TimerEventSupplier;
import org.jacorb.notification.util.TaskExecutor;
import org.jacorb.util.Debug;
import org.jacorb.util.Environment;

import org.omg.CORBA.Any;
import org.omg.CosNotification.StructuredEvent;

import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.engine.TaskProcessor.EnableMessageConsumer;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class TaskProcessor implements Disposable
{

    class TimeoutTask
        implements Runnable,
                   Message.MessageStateListener
    {
        Object timerRegistration_;
        Message message_;

        TimeoutTask( Message message )
        {
            message_ = message;

            message_.setMessageStateListener( this );

            timerRegistration_ =
                executeTaskAfterDelay( message.getTimeout(), this );
        }

        public void actionLifetimeChanged( long timeout )
        {
            ClockDaemon.cancel( timerRegistration_ );
            //            cancelTask( timerRegistration_ );

            timerRegistration_ =
                executeTaskAfterDelay( message_.getTimeout(), this );
        }

        public void run()
        {
            logger_.debug("run Timeout");

            message_.removeMessageStateListener();
            message_.actionTimeout();
        }
    }

    ////////////////////

    class DeferedStopTask implements Runnable
    {
        Message event_;

        DeferedStopTask( Message event )
        {
            event_ = event;

            executeTaskAt( event.getStopTime(), this );
        }

        public void run()
        {
            event_.actionTimeout();
        }
    }

    ////////////////////

    class DeferedStartTask implements Runnable
    {
        Message message_;

        DeferedStartTask( Message m )
        {
            if ( logger_.isDebugEnabled() )
            {
                logger_.debug("Message with Option StartTime="
                              + m.getStartTime()
                              + " will be defered until then");
            }

            message_ = m;

            executeTaskAt( message_.getStartTime(), this );
        }

        public void run()
        {
            if (logger_.isDebugEnabled()) {
                logger_.debug("Defered Message "
                              + message_
                              + " will be processed now");
            }
            processEventInternal( message_ );
        }
    }

    ////////////////////

    class EnableMessageConsumer implements Runnable
    {
        MessageConsumer messageConsumer_;

        EnableMessageConsumer(MessageConsumer mc)
        {
            messageConsumer_ = mc;
        }

        public void run()
        {
            try
            {
                messageConsumer_.enableDelivery();

                scheduleTimedPushTask(messageConsumer_);
            }
            catch (InterruptedException e)
            {
                logger_.error("Interrupted", e);
            }
        }
    }

    ////////////////////

    private Logger logger_ = Debug.getNamedLogger( getClass().getName() );

    private TaskExecutor filterPool_;
    private TaskExecutor deliverPool_;

    private ClockDaemon clockDaemon_;
    private TaskFactory taskFactory_;

    private long backoutInterval_;

    ////////////////////////////////////////

    /**
     * Start ClockDaemon
     * Set up DeliverThreadPool
     * Set up FilterThreadPool
     * Set up TaskFactory
     */
    public TaskProcessor()
    {
        logger_.info( "create TaskProcessor" );

        clockDaemon_ = new ClockDaemon();

        filterPool_ =
            new TaskExecutor( "FilterThread",
                            Environment.getIntPropertyWithDefault( ConfigurableProperties.FILTER_POOL_WORKERS,
                                                                   Constants.DEFAULT_FILTER_POOL_SIZE ) );

        deliverPool_ =
            new TaskExecutor( "DeliverThread",
                            Environment.getIntPropertyWithDefault( ConfigurableProperties.DELIVER_POOL_WORKERS,
                                                                   Constants.DEFAULT_DELIVER_POOL_SIZE ) );

        backoutInterval_ =
            Environment.getIntPropertyWithDefault( ConfigurableProperties.BACKOUT_INTERVAL,
                                                   Constants.DEFAULT_BACKOUT_INTERVAL );

        taskFactory_ = new TaskFactory( this );

        taskFactory_.init();
    }

    ////////////////////////////////////////

    public TaskExecutor getDeliverTaskExecutor() {
        return deliverPool_;
    }

    TaskExecutor getFilterTaskExecutor() {
        return deliverPool_;
    }


    private boolean isFilterTaskQueued()
    {
        return ( filterPool_.isTaskQueued() );
    }

    private boolean isDeliverTaskQueued()
    {
        return ( deliverPool_.isTaskQueued() );
    }

    /**
     * shutdown this TaskProcessor. The Threadpools will be shutdown, the
     * running Threads interrupted and all
     * allocated ressources will be freed. As the active Threads will
     * be interrupted pending Events will be discarded.
     */
    public void dispose()
    {
        logger_.info( "shutdown TaskProcessor" );

        clockDaemon_.shutDown();
        filterPool_.dispose();
        deliverPool_.dispose();
        taskFactory_.dispose();

        logger_.debug( "shutdown complete" );
    }


    public void processMessage( Message m )
    {
        if ( m.hasTimeout() )
        {
            new TimeoutTask( m );
        }

        if ( m.hasStopTime() )
        {
            if ( m.getStopTime().getTime() <= System.currentTimeMillis() )
            {
                fireEventDiscarded( m );

                m.dispose();

                return;
            }
            else
            {
                new DeferedStopTask( m );
            }
        }

        if ( m.hasStartTime() && (m.getStartTime().getTime() > System.currentTimeMillis() ) )
        {
            new DeferedStartTask( m );
        }
        else
        {
            processEventInternal( m );
        }
    }


    public void processEventInternal( Message event )
    {
        AbstractFilterTask _task = taskFactory_.newFilterIncomingTask( event );

        try
        {
            _task.schedule(false);
        }
        catch ( InterruptedException ie )
        {
            logger_.error( "Interrupt while scheduling FilterTask", ie );
        }
    }


    /**
     * Schedule ProxyPullConsumer for pull-Operation.
     * If a Supplier connects to a ProxyPullConsumer the
     * ProxyPullConsumer needs to regularely poll the Supplier.
     * This method queues a Task to run runPullEvent on the specified
     * TimerEventSupplier
     */
    public void scheduleTimedPullTask( TimerEventSupplier dest )
        throws InterruptedException
    {
        PullFromSupplierTask _task = new PullFromSupplierTask();

        _task.setTarget( dest );

        deliverPool_.execute( _task );
    }


    /**
     * Schedule MessageConsumer for a deliver-Operation.
     * Some MessageConsumers (namely SequenceProxyPushSuppliers) need to
     * push Messages regularely to its
     * connected Consumer. Schedule a Task to call
     * deliverPendingEvents on the specified MessageConsumer
     */
    public void scheduleTimedPushTask( MessageConsumer consumer )
        throws InterruptedException
    {
        TimerDeliverTask _task = new TimerDeliverTask(getDeliverTaskExecutor(),
                                                      this,
                                                      taskFactory_);

        _task.setMessageConsumer( consumer );

        _task.schedule(true);
    }


    /**
     * access the Clock Daemon instance.
     */
    private ClockDaemon getClockDaemon()
    {
        return clockDaemon_;
    }


    public Object executeTaskPeriodically( long intervall,
                                           Runnable task,
                                           boolean startImmediately )
    {
        return getClockDaemon().executePeriodically( intervall,
                                                     task,
                                                     startImmediately );
    }


    public void cancelTask( Object id )
    {
        ClockDaemon.cancel( id );
    }


    private Object executeTaskAfterDelay( long delay, Runnable task )
    {
        return clockDaemon_.executeAfterDelay( delay, task );
    }


    private Object executeTaskAt( Date startTime, Runnable task )
    {
        return clockDaemon_.executeAt( startTime, task );
    }


    void fireEventDiscarded( Message event )
    {
        switch ( event.getType() )
        {
            case Message.TYPE_ANY:
                fireEventDiscarded( event.toAny() );
                break;

            case Message.TYPE_STRUCTURED:
                fireEventDiscarded( event.toStructuredEvent() );
                break;

            default:
                throw new RuntimeException();
        }
    }


    void backoffMessageConsumer(MessageConsumer mc)
    {
        if (logger_.isDebugEnabled()) {
            logger_.debug("backoffMessageConsumer " + mc);
        }

        Runnable runEnableTask = new EnableMessageConsumer(mc);

        executeTaskAfterDelay(backoutInterval_, runEnableTask);
    }


    public TaskFactory getTaskFactory()
    {
        return taskFactory_;
    }


    private void fireEventDiscarded( Any a )
    {
        if (logger_.isDebugEnabled()) {
            logger_.debug("Any: " + a + " has been discarded");
        }
    }


    private void fireEventDiscarded( StructuredEvent e )
    {
        if (logger_.isDebugEnabled()) {
            logger_.debug("StructuredEvent: " + e + " has been discarded");
        }
    }

}
