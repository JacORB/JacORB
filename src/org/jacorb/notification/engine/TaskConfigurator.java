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

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.jacorb.notification.NotificationEvent;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.EventConsumer;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.Poolable;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.TRANSIENT;

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

    private TaskErrorHandler filterTaskErrorHandler_ = new TaskErrorHandler()
            {
                public void handleTaskError( Task t, Throwable error )
                {
                    handleFilterTaskTaskError( t, error );
                }
            };

    private TaskFinishHandler filterTaskFinishHandler_ = new TaskFinishHandler()
            {
                public void handleTaskFinished( Task t )
                {
                    handleFilterTaskFinished( t );
                }
            };

    private TaskErrorHandler deliverTaskErrorHandler_ = new TaskErrorHandler()
            {
                public void handleTaskError( Task task, Throwable error )
                {
                    handlePushToConsumerTaskError( task, error );
                }
            };


    private TaskFinishHandler deliverTaskFinishHandler_ = new TaskFinishHandler()
            {
                public void handleTaskFinished( Task t )
                {
                    handlePushToConsumerTaskFinished( t );
                }
            };

    private TaskPoolBase filterProxyConsumerTaskPool_ = new TaskPoolBase()
            {
                public Object newInstance()
                {
                    Object _i = new FilterProxyConsumerTask();

                    return _i;
                }

            };

    private TaskPoolBase filterSupplierAdminTaskPool_ = new TaskPoolBase()
            {
                public Object newInstance()
                {
                    return new FilterSupplierAdminTask();
                }

            };

    private TaskPoolBase filterConsumerAdminTaskPool_ = new TaskPoolBase()
            {
                public Object newInstance()
                {
                    return new FilterConsumerAdminTask();
                }

            };

    private TaskPoolBase filterProxySupplierTaskPool_ = new TaskPoolBase()
            {
                public Object newInstance()
                {
                    return new FilterProxySupplierTask();
                }

            };


    private TaskPoolBase deliverTaskPool_ =
        new TaskPoolBase()
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
    }

    ////////////////////////////////////////

    public void handleFilterTaskFinished( Task t )
    {
        try
        {
            FilterTaskBase currentFilterTask = ( FilterTaskBase ) t;
            FilterTaskBase filterTaskToBeScheduled = null;
            PushToConsumerTask[] listOfPushToConsumerTaskToBeScheduled = null;

            if ( currentFilterTask.getStatus() == Task.DISPOSABLE )
            {

                taskProcessor_.fireEventDiscarded( currentFilterTask.getNotificationEvent() );

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

                listOfPushToConsumerTaskToBeScheduled =
                    newPushToConsumerTask( task.getNotificationEvent(),
                                           task.getFilterStagesWithEventConsumer() );

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

	    if (currentFilterTask.getNotificationEvent() != null) {
		currentFilterTask.removeNotificationEvent().release();
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

        logger_.debug( "Leaving TaskFinishHandler" );
    }

    public void handlePushToConsumerTaskError( Task task, Throwable error )
    {
        if ( error instanceof OBJECT_NOT_EXIST
	     || error instanceof TRANSIENT )
        {

            // push operation caused a OBJECT_NOT_EXIST Exception
            // default strategy is to
            // destroy the ProxySupplier
            PushToConsumerTask _pushToConsumerTask =
                ( PushToConsumerTask ) task;

            if ( logger_.isWarnEnabled() )
            {
                logger_.warn( "push to Consumer failed" );
                logger_.warn( "dispose EventConsumer" );
            }

            _pushToConsumerTask.getEventConsumer().dispose();

            _pushToConsumerTask.release();
        }
        else
        {
            logger_.error( "error during push", error );

            // TODO
            // backoff strategy
            // disable ProxySupplier for some time ...
        }
    }

    public void handlePushToConsumerTaskFinished( Task task )
    {
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
                    taskProcessor_.fireEventDiscarded( ( ( TaskBase ) task ).getNotificationEvent() );
                    // fallthrough

                case Task.DONE:
                    logger_.debug( "finish done task" );
                    logger_.debug( "removeNotificationEvent().release" );

                    ( ( TaskBase ) task ).removeNotificationEvent().release();

                    ( ( Poolable ) task ).release();
                    break;

                default:
                    // should not come here
                    throw new RuntimeException();
                }
            }
            catch ( InterruptedException ie )
            {
                //ignore
            }
        }
    }

    public void handleFilterTaskTaskError( Task task, Throwable error )
    {
        logger_.error( "Error occured in Task " + task, error );
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

    private FilterProxyConsumerTask newFilterConsumerProxyTask( NotificationEvent event )
    {
        FilterProxyConsumerTask task = newFilterProxyConsumerTask();

        task.setTaskErrorHandler( filterTaskErrorHandler_ );
        task.setNotificationEvent( event );
        task.setTaskFinishHandler( filterTaskFinishHandler_ );
        task.setCurrentFilterStage( new FilterStage[] { event.getFilterStage() } );

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
	task.setNotificationEvent(t.getNotificationEvent());

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
	task.setNotificationEvent(t.getNotificationEvent());

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
	task.setNotificationEvent(t.getNotificationEvent());

        task.setCurrentFilterStage( t.getFilterStageToBeProcessed() );

        return task;
    }

    /**
     * factory method for a new FilterIncomingTask instance. uses
     * an Object Pool.
     */
    FilterTaskBase newFilterIncomingTask( NotificationEvent event )
    {

        return newFilterConsumerProxyTask( event );

    }

    PushToConsumerTask[] newPushToConsumerTask(NotificationEvent event,
					       FilterStage[] nodes) {

	return newPushToConsumerTask(FilterProxySupplierTask.EMPTY_MAP, 
				     event, 
				     nodes);

    }

    PushToConsumerTask[] newPushToConsumerTask(FilterProxySupplierTask.AlternateNotificationEventMap map, 
					       NotificationEvent defaultEvent, 
					       FilterStage [] seqFilterStageWithEventConsumer) {
	
	PushToConsumerTask [] _seqPushToConsumerTask = 
	    new PushToConsumerTask[ seqFilterStageWithEventConsumer.length ];

        EventConsumer[] _seqEventConsumer = 
	    new EventConsumer[ seqFilterStageWithEventConsumer.length ];

        for ( int x = 0; x < seqFilterStageWithEventConsumer.length; ++x )
	    {
		_seqPushToConsumerTask[ x ] = 
		    ( PushToConsumerTask ) deliverTaskPool_.lendObject();

		_seqPushToConsumerTask[ x ]
		    .setEventConsumer( seqFilterStageWithEventConsumer[ x ].getEventConsumer() );

		NotificationEvent _alternateEvent =
		    map.getAlternateNotificationEvent(seqFilterStageWithEventConsumer[x]);

		if ( _alternateEvent != null ) {

		    _seqPushToConsumerTask[ x ].setNotificationEvent( _alternateEvent );
		    
		} else {
		    _seqPushToConsumerTask[x].setNotificationEvent(defaultEvent);
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
    private PushToConsumerTask[] newPushToConsumerTask( FilterProxySupplierTask task )
    {
        PushToConsumerTask[] _deliverTasks;

        NotificationEvent _notificationEvent = task.getNotificationEvent();

	FilterStage[] _seqFilterStageToBeProcessed = 
	    task.getFilterStageToBeProcessed();

	_deliverTasks = newPushToConsumerTask(task.changedNotificationEvents_, 
					      _notificationEvent, 
					      _seqFilterStageToBeProcessed);

        return _deliverTasks;
    }
}

