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

import org.jacorb.notification.interfaces.AbstractPoolable;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.EventConsumer;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.Message;

import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.TRANSIENT;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.jacorb.notification.ConfigurableProperties;
import org.jacorb.util.Environment;
import org.jacorb.notification.Constants;

/**
 * TaskConfigurator.java
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class TaskConfigurator implements Disposable
{

    final Logger logger_ =
        Hierarchy.getDefaultHierarchy().getLoggerFor( getClass().getName() );

    private TaskProcessor taskProcessor_;

    /**
     * max time a EventConsumer is allowed to fail before its disconnected.
     */
    private int ERROR_THRESHOLD;

    public TaskErrorHandler filterTaskErrorHandler_ = new TaskErrorHandler()
            {
                public void handleTaskError( Task t, Throwable error )
                {
                    handleFilterTaskTaskError( t, error );
                }
            };

    public TaskFinishHandler filterTaskFinishHandler_ = new TaskFinishHandler()
            {
                public void handleTaskFinished( Task t )
                {
                    onFilterTaskFinished( t );
                }
            };

    public TaskErrorHandler deliverTaskErrorHandler_ = new TaskErrorHandler()
            {
                public void handleTaskError( Task task, Throwable error )
                {
                    onPushToConsumerTaskError( task, error );
                }
            };


    public TaskFinishHandler deliverTaskFinishHandler_ = new TaskFinishHandler()
            {
                public void handleTaskFinished( Task t )
                {
                    onPushToConsumerTaskFinished( t );
                }
            };

    private AbstractTaskPool filterProxyConsumerTaskPool_ =
        new AbstractTaskPool()
        {
            public Object newInstance()
            {
                return new FilterProxyConsumerTask();
            }
        };

    private AbstractTaskPool filterSupplierAdminTaskPool_ =
        new AbstractTaskPool()
        {
            public Object newInstance()
            {
                return new FilterSupplierAdminTask();
            }
        };

    private AbstractTaskPool filterConsumerAdminTaskPool_ =
        new AbstractTaskPool()
        {
            public Object newInstance()
            {
                return new FilterConsumerAdminTask();
            }
        };

    private AbstractTaskPool filterProxySupplierTaskPool_ =
        new AbstractTaskPool()
        {
            public Object newInstance()
            {
                return new FilterProxySupplierTask();
            }
        };


    private AbstractTaskPool deliverTaskPool_ =
        new AbstractTaskPool()
        {
            public Object newInstance()
            {
                return new PushToConsumerTask();
            }
        };

    ////////////////////////////////////////

    public TaskConfigurator( TaskProcessor taskProcessor )
    {
        taskProcessor_ = taskProcessor;

        ERROR_THRESHOLD =
            Environment.getIntPropertyWithDefault(ConfigurableProperties.EVENTCONSUMER_ERROR_THRESHOLD,
                                                  Constants.DEFAULT_EVENTCONSUMER_ERROR_THRESHOLD);
    }

    ////////////////////////////////////////

    public void setDeliverTaskPool(AbstractTaskPool pool) {
        deliverTaskPool_ = pool;
    }

    public void onFilterTaskFinished( Task t )
    {
        try
        {
            AbstractFilterTask currentFilterTask = ( AbstractFilterTask ) t;
            AbstractFilterTask filterTaskToBeScheduled = null;
            AbstractDeliverTask[] listOfPushToConsumerTaskToBeScheduled = null;

            if ( currentFilterTask.getStatus() == Task.DISPOSABLE )
            {

                taskProcessor_.fireEventDiscarded( currentFilterTask.removeMessage() );

            }
            else if ( currentFilterTask instanceof FilterProxyConsumerTask )
            {

                filterTaskToBeScheduled =
                    newFilterSupplierAdminTask( ( FilterProxyConsumerTask ) currentFilterTask );

            }
            else if ( currentFilterTask instanceof FilterSupplierAdminTask )
            {

                filterTaskToBeScheduled =
                    newFilterConsumerAdminTask( ( FilterSupplierAdminTask ) currentFilterTask );

            }
            else if ( currentFilterTask instanceof FilterConsumerAdminTask )
            {

                FilterConsumerAdminTask task = ( FilterConsumerAdminTask ) currentFilterTask;

                // if we are filtering Outgoing events its
                // possible that deliveries can be made as soon as
                // the ConsumerAdmin Filters are eval'd
                // (if InterFilterGroupOperator.OR_OP is set !)

                FilterStage[] _filterStagesWithEventConsumer =
                    task.getFilterStagesWithEventConsumer();

                if (_filterStagesWithEventConsumer.length > 0) {
                    listOfPushToConsumerTaskToBeScheduled =
                        newPushToConsumerTask(_filterStagesWithEventConsumer,
                                              task.copyMessage());
                }

                filterTaskToBeScheduled = newFilterProxySupplierTask( task );

            }
            else if ( currentFilterTask instanceof FilterProxySupplierTask )
            {

                listOfPushToConsumerTaskToBeScheduled =
                    newPushToConsumerTask( ( FilterProxySupplierTask ) currentFilterTask );

            }
            else
            {
                throw new RuntimeException();
            }

            Message event = currentFilterTask.removeMessage();
            if (event != null) {
                event.dispose();
            }

            currentFilterTask.release();

            if ( filterTaskToBeScheduled != null )
            {
                taskProcessor_.scheduleOrExecuteFilterTask( filterTaskToBeScheduled );
            }

            if ( listOfPushToConsumerTaskToBeScheduled != null )
            {
                taskProcessor_.schedulePushToConsumerTask( listOfPushToConsumerTaskToBeScheduled );
            }

        }
        catch ( InterruptedException e )
        {
            logger_.error( "Task has been Interrupted", e );
        }
    }

    void onPushToConsumerTaskError( Task task, Throwable error )
    {

        if (logger_.isDebugEnabled()) {
            logger_.debug("Entering Exceptionhandler for Task:"
                          + task.getClass().getName(),
                          error);
        }

        AbstractDeliverTask _pushToConsumerTask = (AbstractDeliverTask)task;

        if ( error instanceof OBJECT_NOT_EXIST )
        {

            // push operation caused a OBJECT_NOT_EXIST Exception
            // default strategy is to
            // destroy the ProxySupplier

            if ( logger_.isWarnEnabled() )
            {
                logger_.warn( "push to Consumer failed: Dispose EventConsumer" );
            }

            _pushToConsumerTask.getEventConsumer().dispose();

        }
        else
        {
            EventConsumer _consumer = _pushToConsumerTask.getEventConsumer();

            logger_.info("EventConsumer errCount: " + _consumer.getErrorCounter());

            if (_consumer.getErrorCounter() > ERROR_THRESHOLD) {

                if (logger_.isWarnEnabled()) {
                    logger_.warn("EventConsumer is repeatingly failing. Error Counter is: "
                                 + _consumer.getErrorCounter()
                                 + ". The EventConsumer will be disconnected");
                }

                _consumer.dispose();

            } else {

                _consumer.incErrorCounter();

                if (logger_.isInfoEnabled()) {
                    logger_.info("Increased the ErrorCount for "
                                 + _consumer
                                 + " to "
                                 +_consumer.getErrorCounter());
                }

                _consumer.disableDelivery();

                try {
                    // as delivery has been disabled
                    // the message will be queued by the EventConsumer
                    _consumer.deliverEvent(_pushToConsumerTask.removeMessage());

                    logger_.info("will backoff EventConsumer for a while");
                    taskProcessor_.backoffEventConsumer(_consumer);
                } catch (Exception e) {
                    // if regardless of disabling the EventConsumer
                    // above the EventConsumer still
                    // throws an exception we'll assume its totally
                    // messed up and dispose it.
                    logger_.fatalError("a disabled EventConsumer should never throw an exception during deliverEvent", e);
                    try {
                        _consumer.dispose();
                    } catch (Exception ex) {}
                }
            }
        }

        Message m = _pushToConsumerTask.removeMessage();

        if (m != null) {
            m.dispose();
        }

        _pushToConsumerTask.release();

    }

    void onPushToConsumerTaskFinished( Task task )
    {
        try
            {
                switch ( task.getStatus() )
                    {

                    case Task.RESCHEDULE:
                        // deliverTask needs to be rescheduled
                        logger_.warn( "reschedule PushToConsumerTask" );

                        taskProcessor_.scheduleOrExecutePushToConsumerTask( ( PushToConsumerTask ) task );

                        break;

                    case Task.DISPOSABLE:
                        logger_.debug( "PushToConsumerFinishHandler: Event has been marked disposable" );
                        taskProcessor_.fireEventDiscarded( ( ( AbstractTask ) task ).removeMessage() );
                        // fallthrough

                    case Task.DONE:
                        // ( ( AbstractTask ) task ).removeMessage().dispose();

                        ( ( AbstractPoolable ) task ).release();
                        break;

                    default:
                        // does not happen
                        throw new RuntimeException("maybe you've forgotten to set a sensible status in the doWork() method");
                    }
            }
        catch ( InterruptedException ie )
            {
                //ignore
            }
    }


    public void handleFilterTaskTaskError( Task task, Throwable error )
    {
        logger_.fatalError( "Error while Filtering in Task:" + task, error );
    }

    public void init()
    {
        filterProxyConsumerTaskPool_.init();
        filterProxySupplierTaskPool_.init();
        filterConsumerAdminTaskPool_.init();
        filterSupplierAdminTaskPool_.init();

        deliverTaskPool_.init();
    }

    public void dispose()
    {
        filterProxyConsumerTaskPool_.dispose();
        filterProxySupplierTaskPool_.dispose();
        filterConsumerAdminTaskPool_.dispose();
        filterSupplierAdminTaskPool_.dispose();

        deliverTaskPool_.dispose();
    }

    protected FilterProxyConsumerTask newFilterProxyConsumerTask() {
        return (FilterProxyConsumerTask)filterProxyConsumerTaskPool_.lendObject();
    }

    private FilterProxyConsumerTask newFilterConsumerProxyTask( Message event )
    {
        FilterProxyConsumerTask task = newFilterProxyConsumerTask();

        task.setTaskErrorHandler( filterTaskErrorHandler_ );
        task.setMessage( event );
        task.setTaskFinishHandler( filterTaskFinishHandler_ );
        task.setCurrentFilterStage( new FilterStage[] { event.getInitialFilterStage() } );

        return task;
    }

    private FilterSupplierAdminTask newFilterSupplierAdminTask() {
        return (FilterSupplierAdminTask)filterSupplierAdminTaskPool_.lendObject();
    }

    private FilterSupplierAdminTask newFilterSupplierAdminTask( FilterProxyConsumerTask t )
    {
        FilterSupplierAdminTask task = newFilterSupplierAdminTask();

        task.setTaskFinishHandler( filterTaskFinishHandler_ );
        task.setTaskErrorHandler( filterTaskErrorHandler_ );
        task.setMessage( t.removeMessage() );

        task.setCurrentFilterStage( t.getFilterStageToBeProcessed() );

        task.setSkip( t.getSkip() );

        return task;
    }

    private FilterConsumerAdminTask newFilterConsumerAdminTask() {
        return (FilterConsumerAdminTask)filterConsumerAdminTaskPool_.lendObject();
    }

    private FilterConsumerAdminTask newFilterConsumerAdminTask( FilterSupplierAdminTask t )
    {
        FilterConsumerAdminTask task = newFilterConsumerAdminTask();

        task.setTaskFinishHandler( filterTaskFinishHandler_ );
        task.setTaskErrorHandler( filterTaskErrorHandler_ );
        task.setMessage(t.removeMessage());

        task.setCurrentFilterStage( t.getFilterStageToBeProcessed() );

        return task;
    }

    private FilterProxySupplierTask newFilterProxySupplierTask() {
        return (FilterProxySupplierTask)filterProxySupplierTaskPool_.lendObject();
    }

    private FilterProxySupplierTask newFilterProxySupplierTask( FilterConsumerAdminTask t )
    {
        FilterProxySupplierTask task = newFilterProxySupplierTask();

        task.setTaskFinishHandler( filterTaskFinishHandler_ );
        task.setTaskErrorHandler( filterTaskErrorHandler_ );
        task.setMessage(t.removeMessage());

        task.setCurrentFilterStage( t.getFilterStageToBeProcessed() );

        return task;
    }

    /**
     * factory method for a new FilterIncomingTask instance. uses
     * an Object Pool.
     */
    AbstractFilterTask newFilterIncomingTask( Message event )
    {

        return newFilterConsumerProxyTask( event );

    }

    AbstractDeliverTask[] newPushToConsumerTask(FilterStage[] nodes, Message event) {

        return newPushToConsumerTask(nodes, event, FilterProxySupplierTask.EMPTY_MAP);

    }


    /**
     * Create a Array of PushToConsumerTask.
     *
     * @param seqFilterStageWithEventConsumer Array of FilterStages
     * that have an EventConsumer attached.
     *
     * @param defaultMessage the Message that is to be
     * delivered by the created PushToConsumerTask. This method gains
     * possession of the Message.
     *
     * @param map alternate Messages that should be used for
     * EventConsumers.
     *
     * @return a <code>PushToConsumerTask[]</code> value
     */
    AbstractDeliverTask[] newPushToConsumerTask(FilterStage [] seqFilterStageWithEventConsumer,
                                                      Message defaultMessage,
                                                      FilterProxySupplierTask.AlternateMessageMap map) {

        AbstractDeliverTask[] _seqPushToConsumerTask =
            new AbstractDeliverTask[ seqFilterStageWithEventConsumer.length ];

        for ( int x = 0; x < seqFilterStageWithEventConsumer.length; ++x )
            {
                _seqPushToConsumerTask[ x ] =
                    ( AbstractDeliverTask ) deliverTaskPool_.lendObject();

                _seqPushToConsumerTask[ x ]
                    .setEventConsumer( seqFilterStageWithEventConsumer[ x ].getEventConsumer() );

                Message _alternateEvent =
                    map.getAlternateMessage(seqFilterStageWithEventConsumer[x]);

                if ( _alternateEvent != null ) {

                    _seqPushToConsumerTask[ x ].setMessage( _alternateEvent );

                } else {
                    if (x == 0) {
                        // the first Message can be simply
                        // used as this method gains possession of the
                        // Message
                        _seqPushToConsumerTask[x].setMessage(defaultMessage);
                    } else {
                        // all following Messages must be copied
                        _seqPushToConsumerTask[x].setMessage((Message)defaultMessage.clone());
                    }
                }

                _seqPushToConsumerTask[ x ].setTaskFinishHandler( deliverTaskFinishHandler_ );
                _seqPushToConsumerTask[ x ].setTaskErrorHandler( deliverTaskErrorHandler_ );
            }
        return _seqPushToConsumerTask;
    }

    /**
     * factory method to create PushToConsumer Tasks. The Tasks are
     * initialized with the data taken from a FilterProxySupplierTask.
     */
    private AbstractDeliverTask[] newPushToConsumerTask( FilterProxySupplierTask task )
    {
        AbstractDeliverTask[] _deliverTasks;

        Message _notificationEvent =
            task.removeMessage();

        FilterStage[] _seqFilterStageToBeProcessed =
            task.getFilterStageToBeProcessed();

        _deliverTasks = newPushToConsumerTask(_seqFilterStageToBeProcessed,
                                              _notificationEvent,
                                              task.changedMessages_);

        return _deliverTasks;
    }
}

