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
import org.jacorb.notification.interfaces.EventConsumer;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.Poolable;
import org.jacorb.notification.util.ObjectPoolBase;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.TRANSIENT;
import org.jacorb.notification.interfaces.Disposable;

/**
 * TaskConfigurator.java
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class TaskConfigurator implements Disposable
{
    class PushToConsumerTaskErrorHandler implements TaskErrorHandler
    {
        public void handleTaskError( Task task, Throwable error )
        {
            if ( error instanceof OBJECT_NOT_EXIST 
		 || error instanceof TRANSIENT)
            {
		
                // push operation caused a OBJECT_NOT_EXIST Exception
		// default strategy is to
                // destroy the ProxySupplier
                PushToConsumerTask _pushToConsumerTask = 
		    ( PushToConsumerTask ) task;
		
		if (logger_.isWarnEnabled()) {
		    logger_.warn("push to Consumer failed");
		    logger_.warn("dispose EventConsumer");
		}

                _pushToConsumerTask.getEventConsumer().dispose();

		_pushToConsumerTask.release();
            }
            else
            {
		logger_.error("error during push", error);

                // TODO
                // backoff strategy
                // disable ProxySupplier for some time ...
            }
        }
    }

    class PushToConsumerTaskFinishHandler implements TaskFinishHandler
    {
        public void handleTaskFinished( Task task )
        {
            try
            {
                switch ( task.getStatus() )
                {

                case Task.RESCHEDULE:
                    // deliverTask needs to be rescheduled
		    logger_.warn("reschedule PushToConsumerTask");

                    taskProcessor_.scheduleOrExecutePushToConsumerTask( ( PushToConsumerTask ) task );

                    break;

                case Task.DONE:
		    logger_.debug("finish done task");
		    logger_.debug("removeNotificationEvent().release");
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

    class FilterTaskErrorHandler implements TaskErrorHandler {
	public void handleTaskError(Task task, Throwable error) {
	    logger_.error("Error occured in Task " + task, error);
	}
    }

    class FilterTaskFinishHandler implements TaskFinishHandler
    {
        public void handleTaskFinished( Task task )
        {

            FilterTaskBase _filterTask = ( FilterTaskBase ) task;

            try
            {
                switch ( _filterTask.getStatus() )
                {

                case Task.RESCHEDULE:

                    // fetch the FilterStages for which filtering was
                    // successful and configure task to eval them
                    FilterStage[] _dest = _filterTask.getMatchingFilterStage();

                    _filterTask.setCurrentFilterStage( _dest );

                    if ( _filterTask instanceof FilterOutgoingTask )
                    {
                        // if we are filtering Outgoing events its
                        // possible that deliveries can be made as soon as
                        // the ConsumerAdmin Filters are eval'd
                        // (if InterFilterGroupOperator.OR_OP is set !) 

                        FilterOutgoingTask _outgoingTask = 
			    ( FilterOutgoingTask ) task;

                        PushToConsumerTask[] _deliverTask =
                            newPushToConsumerTask( _outgoingTask.getNotificationEvent(),
                                                   _outgoingTask.getFilterStagesWithEventConsumer() );

                        if ( _deliverTask != null && _deliverTask.length > 0 )
                        {
                            taskProcessor_.schedulePushToConsumerTask( _deliverTask );
                            _outgoingTask.clearFilterStagesWithEventConsumer();
                        }
                    }

                    if ( _dest.length == 0 )
                    {
                        // if no filter matched the task is done and can
                        // be dropped
                        _filterTask.removeNotificationEvent().release();
                        _filterTask.release();
                    }
                    else
                    {
                        // schedule for evaluation of the remaining filters
                        _filterTask.clearMatchingFilterStage();

			taskProcessor_.scheduleOrExecuteFilterTask( _filterTask );
			
                    }

                    break;

                case Task.DONE:

                    if ( task instanceof FilterOutgoingTask )
                    {
                        Poolable _toBeReleased = ( Poolable ) task;

                        PushToConsumerTask[] _deliverTasks =
                            newPushToConsumerTask( ( FilterOutgoingTask ) task );

                        taskProcessor_.schedulePushToConsumerTask( _deliverTasks );
                        _toBeReleased.release();
                    }
                    else if ( task instanceof FilterIncomingTask )
                    {

                        FilterOutgoingTask _newTask =
                            newFilterOutgoingTask( ( FilterIncomingTask ) task );

			taskProcessor_.scheduleOrExecuteFilterTask( _newTask );

                        ( ( Poolable ) task ).release();
                    }
                    else
                    {
                        // bug
                        throw new RuntimeException();
                    }

                    break;

                case Task.ERROR:
                    // fallthrough

                default:
                    logger_.fatalError( "wrong status: " + task.getStatus() );
                    logger_.fatalError( task.toString() );
                    throw new RuntimeException();
                }
            }
            catch ( InterruptedException e )
            {
                // ignore
            }
        }
    }

    ////////////////////////////////////////
    //

    final Logger logger_ =
        Hierarchy.getDefaultHierarchy().getLoggerFor( getClass().getName() );    

    private TaskProcessor taskProcessor_;

    private TaskErrorHandler filterTaskErrorHandler_ = new FilterTaskErrorHandler();

    private TaskFinishHandler deliverTaskFinishHandler_ = new PushToConsumerTaskFinishHandler();

    private TaskFinishHandler filterTaskFinishHandler_ = new FilterTaskFinishHandler();

    private TaskErrorHandler deliverTaskErrorHandler_ = new PushToConsumerTaskErrorHandler();

    private ObjectPoolBase filterIncomingTaskPool_ = new ObjectPoolBase()
            {
                public Object newInstance()
                {
		    Object _i = new FilterIncomingTask();		    

                    return _i;
                }

                public void passivateObject( Object o )
                {
                    ( ( Poolable ) o ).reset();
                }

                public void activateObject( Object o )
                {
                    ( ( Poolable ) o ).setObjectPool( this );
                }
            };

    private ObjectPoolBase filterOutgoingTaskPool_ = new ObjectPoolBase()
            {
                public Object newInstance()
                {
                    return new FilterOutgoingTask();
                }

                public void passivateObject( Object o )
                {
                    ( ( Poolable ) o ).reset();
                }

                public void activateObject( Object o )
                {
                    ( ( Poolable ) o ).setObjectPool( this );
                }
            };

    private ObjectPoolBase deliverTaskPool_ =
        new ObjectPoolBase()
        {
            public Object newInstance()
            {
                return new PushToConsumerTask();
            }

            public void passivateObject( Object o )
            {
		Poolable _p = (Poolable) o;
		_p.setObjectPool(null);
                _p.reset();
            }

            public void activateObject( Object o )
            {
		Poolable _p = (Poolable) o;
		_p.setObjectPool( this );
            }
        };

    ////////////////////////////////////////

    public TaskConfigurator( TaskProcessor taskProcessor )
    {
        taskProcessor_ = taskProcessor;
    }

    ////////////////////////////////////////

    public void init()
    {
        filterIncomingTaskPool_.init();
        filterOutgoingTaskPool_.init();
        deliverTaskPool_.init();
    }

    public void dispose() {
	filterIncomingTaskPool_.dispose();
	filterOutgoingTaskPool_.dispose();
	deliverTaskPool_.dispose();
    }

    /**
     * factory method for a new FilterIncomingTask instance. uses
     * an Object Pool.
     */
    FilterIncomingTask newFilterIncomingTask( NotificationEvent event )
    {
        try
        {
            FilterIncomingTask task =
                ( FilterIncomingTask ) filterIncomingTaskPool_.lendObject();

            task.setNotificationEvent( event );

            task.setTaskFinishHandler( filterTaskFinishHandler_ );

            FilterStage[] _d = new FilterStage[] {event.getFilterStage() };

            if ( _d.length > 1 )
            {
                throw new RuntimeException( "Assertion Failed" );
            }

            task.setCurrentFilterStage( _d );

            return task;
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
            throw new RuntimeException();
        }
    }

    /**
     * factory method to create PushToConsumer Tasks. The Tasks are
     * initialized with the data taken from a FilterOutgoingTask.
     */
    private PushToConsumerTask[] newPushToConsumerTask( FilterOutgoingTask task )
    {

        PushToConsumerTask[] _deliverTasks;
        FilterStage[] _allDests = task.getMatchingFilterStage();

        NotificationEvent _event = task.removeNotificationEvent();

        _deliverTasks = newPushToConsumerTask( _event, _allDests );

        _event.release();

        return _deliverTasks;
    }

    /**
     * factory method to create PushToConsumer Tasks. The Tasks are
     * initialized with a NotificationEvent and the EventConsumers
     * associated to some FilterStages
     */
    private PushToConsumerTask[] newPushToConsumerTask( NotificationEvent event,
							FilterStage[] nodes )
    {

        PushToConsumerTask _deliverTasks[] = new PushToConsumerTask[ nodes.length ];

        EventConsumer[] _disp = new EventConsumer[ nodes.length ];

        for ( int x = 0; x < nodes.length; ++x )
        {
            _deliverTasks[ x ] = ( PushToConsumerTask ) deliverTaskPool_.lendObject();

            _deliverTasks[ x ].setEventConsumer( nodes[ x ].getEventConsumer() );
            _deliverTasks[ x ].setNotificationEvent( event );
            _deliverTasks[ x ].setTaskFinishHandler( deliverTaskFinishHandler_ );
            _deliverTasks[ x ].setTaskErrorHandler( deliverTaskErrorHandler_ );
        }

        return _deliverTasks;
    }

    private FilterOutgoingTask newFilterOutgoingTask( FilterIncomingTask task )
    {
        FilterOutgoingTask _newTask =
            ( FilterOutgoingTask ) filterOutgoingTaskPool_.lendObject();

        _newTask.setFilterStage( task );
        _newTask.setNotificationEvent( task.removeNotificationEvent() );
        _newTask.setTaskFinishHandler( filterTaskFinishHandler_ );
	_newTask.setTaskErrorHandler( filterTaskErrorHandler_ );
	
        return _newTask;
    }
} 

