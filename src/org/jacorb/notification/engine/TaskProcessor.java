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
import org.jacorb.notification.interfaces.EventConsumer;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.TimerEventSupplier;
import org.jacorb.notification.util.ThreadPool;
import org.jacorb.util.Debug;
import org.jacorb.util.Environment;

import org.omg.CORBA.Any;
import org.omg.CosNotification.StructuredEvent;

import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;
import org.apache.avalon.framework.logger.Logger;

/**
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class TaskProcessor implements Disposable
{

    private Logger logger_ = Debug.getNamedLogger( getClass().getName() );

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

    class DeferedStartTask implements Runnable
    {
        Message event_;

        DeferedStartTask( Message event )
        {
            if ( logger_.isDebugEnabled() )
            {
                logger_.debug("Message has StartTime and will be run at" + event.getStartTime());
            }

            event_ = event;
            executeTaskAt( event.getStartTime(), this );
        }

        public void run()
        {
            processEventInternal( event_ );
        }
    }

    class EnableEventConsumer implements Runnable
    {
        EventConsumer eventConsumer_;

        EnableEventConsumer(EventConsumer ec)
        {
            logger_.debug("new EnableEventConsumer(" + ec + ")");

            eventConsumer_ = ec;
        }

        public void run()
        {
            logger_.debug("run enableEventConsumer");

            try
            {
                eventConsumer_.enableDelivery();
                scheduleTimedPushTask(eventConsumer_);
            }
            catch (InterruptedException e)
            {}
        }
    }

    private TaskErrorHandler nullErrorHandler_ =
        new TaskErrorHandler()
        {
            public void handleTaskError( Task task, Throwable error )
            {
                logger_.error( "Error in Task: " + task, error );
            }
        };

    private TaskFinishHandler nullFinishHandler_ =
        new TaskFinishHandler()
        {
            public void handleTaskFinished( Task task )
            {
                logger_.debug( "Task " + task + " finished" );
            }
        };

    private ThreadPool filterPool_;
    private ThreadPool deliverPool_;

    private ClockDaemon clockDaemon_;
    private TaskConfigurator taskConfigurator_;

    private long backoutInterval_;


    ////////////////////////////////////////

    /**
     * Start ClockDaemon
     * Set up DeliverThreadPool
     * Set up FilterThreadPool
     * Set up TaskConfigurator
     */
    public TaskProcessor()
    {
        logger_.info( "create TaskProcessor" );

        clockDaemon_ = new ClockDaemon();

        filterPool_ =
            new ThreadPool( "FilterThread",
                            Environment.getIntPropertyWithDefault( ConfigurableProperties.FILTER_POOL_WORKERS,
                                                                   Constants.DEFAULT_FILTER_POOL_SIZE ) );

        deliverPool_ =
            new ThreadPool( "DeliverThread",
                            Environment.getIntPropertyWithDefault( ConfigurableProperties.DELIVER_POOL_WORKERS,
                                                                   Constants.DEFAULT_DELIVER_POOL_SIZE ) );

        backoutInterval_ =
            Environment.getIntPropertyWithDefault( ConfigurableProperties.BACKOUT_INTERVAL,
                                                   Constants.DEFAULT_BACKOUT_INTERVAL );

        taskConfigurator_ = new TaskConfigurator( this );
        taskConfigurator_.init();
    }

    ////////////////////////////////////////

    boolean isFilterTaskQueued()
    {
        return ( filterPool_.isTaskQueued() );
    }

    boolean isDeliverTaskQueued()
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
        logger_.info( "dispose" );

        clockDaemon_.shutDown();
        filterPool_.dispose();
        deliverPool_.dispose();
        taskConfigurator_.dispose();

        logger_.debug( "dispose - complete" );
    }


    public void processEvent( Message event )
    {
        if ( event.hasTimeout() )
        {
            new TimeoutTask( event );
        }

        if ( event.hasStopTime() )
        {

            if ( event.getStopTime().getTime() <= System.currentTimeMillis() )
            {

                fireEventDiscarded( event );

                return;
            }
            else
            {
                new DeferedStopTask( event );
            }
        }

        if ( event.hasStartTime() ) // && event.getStartTime().getTime() <= System.currentTimeMillis() )
        {
            new DeferedStartTask( event );
        }
        else
        {
            processEventInternal( event );
        }
    }


    public void processEventInternal( Message event )
    {

        AbstractFilterTask _task = taskConfigurator_.newFilterIncomingTask( event );

        try
        {
            scheduleFilterTask( _task );
        }
        catch ( InterruptedException ie )
        {
            logger_.error( "Interrupt while scheduling FilterTask", ie );
        }
    }


    /**
     * Schedule a FilterTask for execution.
     */
    public void scheduleFilterTask( AbstractFilterTask task )
    throws InterruptedException
    {
        filterPool_.execute( task );
    }


    /**
     * Schedule a FilterTask for execution. Bypass Queuing if
     * possible. If no FilterTasks are queued this
     * Thread can be used to perform the FilterTask. Otherwise queue
     * FilterTask for execution
     */
    void scheduleOrExecuteFilterTask( AbstractFilterTask task )
        throws InterruptedException
    {
        if ( isFilterTaskQueued() )
        {
            scheduleFilterTask( task );
        }
        else
        {
            task.run();
        }
    }

    /**
     * Schedule or Execute PushToConsumerTask for execution. Bypass
     * Scheduling if possible.
     */
    public void scheduleOrExecutePushToConsumerTask( AbstractDeliverTask task )
        throws InterruptedException
    {
        if ( isDeliverTaskQueued() )
        {
            schedulePushToConsumerTask( task );
        }
        else
        {
            task.run();
        }
    }


    /**
     * Schedule a PushToConsumerTask for execution.
     */
    public void schedulePushToConsumerTask( AbstractDeliverTask task )
        throws InterruptedException
    {
        if (logger_.isDebugEnabled()) {
            logger_.debug("schedulePushToConsumerTask(" + task + ")");
        }

        deliverPool_.execute( task );
    }


    /**
     * Schedule an array of PushToConsumerTask for execution.
     */
    void schedulePushToConsumerTask( AbstractDeliverTask[] tasks )
    throws InterruptedException
    {
        for ( int x = 0; x < tasks.length; ++x )
        {
            schedulePushToConsumerTask( tasks[ x ] );
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

        _task.setTaskFinishHandler( nullFinishHandler_ );
        _task.setTaskErrorHandler( nullErrorHandler_ );
        _task.setTarget( dest );

        deliverPool_.execute( _task );
    }


    /**
     * Schedule ProxyPushSupplier for push-Operation.
     * A SequenceProxyPushSuppliers need to push Events regularely to its
     * connected Consumer. This method allows to queue a Task to call
     * deliverPendingEvents on the specified EventConsumer
     */
    public void scheduleTimedPushTask( EventConsumer consumer )
        throws InterruptedException
    {
        TimerDeliverTask _task = new TimerDeliverTask();

        _task.setEventConsumer( consumer );
        _task.setTaskFinishHandler( taskConfigurator_.deliverTaskFinishHandler_ );
        _task.setTaskErrorHandler( taskConfigurator_.deliverTaskErrorHandler_ );

        deliverPool_.execute( _task );
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

        event.dispose();
    }

    void backoffEventConsumer(EventConsumer ec)
    {
        if (logger_.isDebugEnabled()) {
            logger_.debug("backoffEventConsumer " + ec);
        }

        Runnable runEnableTask = new EnableEventConsumer(ec);

        executeTaskAfterDelay(backoutInterval_, runEnableTask);
    }

    public TaskConfigurator getTaskConfigurator()
    {
        return taskConfigurator_;
    }

    void fireEventDiscarded( Any a )
    {
    }

    void fireEventDiscarded( StructuredEvent e )
    {
    }

}
