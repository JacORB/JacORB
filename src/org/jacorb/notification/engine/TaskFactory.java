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

import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.util.Debug;

import org.apache.avalon.framework.logger.Logger;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class TaskFactory implements Disposable
{
    final Logger logger_ = Debug.getNamedLogger( getClass().getName() );


    private TaskProcessor taskProcessor_;


    private AbstractTaskPool filterProxyConsumerTaskPool_ =
        new AbstractTaskPool("FilterProxyConsumerTaskPool")
        {
            public Object newInstance()
            {
                return new FilterProxyConsumerTask(taskProcessor_.getFilterTaskExecutor(),
                                                   taskProcessor_,
                                                   TaskFactory.this);
            }
        };


    private AbstractTaskPool filterSupplierAdminTaskPool_ =
        new AbstractTaskPool("FilterSupplierAdminTaskPool")
        {
            public Object newInstance()
            {
                return new FilterSupplierAdminTask(taskProcessor_.getFilterTaskExecutor(),
                                                   taskProcessor_,
                                                   TaskFactory.this);
            }
        };


    private AbstractTaskPool filterConsumerAdminTaskPool_ =
        new AbstractTaskPool("FilterConsumerAdminTaskPool")
        {
            public Object newInstance()
            {
                return new FilterConsumerAdminTask(taskProcessor_.getFilterTaskExecutor(),
                                                   taskProcessor_,
                                                   TaskFactory.this);
            }
        };


    private AbstractTaskPool filterProxySupplierTaskPool_ =
        new AbstractTaskPool("FilterProxySupplierTaskPool")
        {
            public Object newInstance()
            {
                return new FilterProxySupplierTask(taskProcessor_.getFilterTaskExecutor(),
                                                   taskProcessor_,
                                                   TaskFactory.this);
            }
        };


    private AbstractTaskPool deliverTaskPool_ =
        new AbstractTaskPool("PushToConsumerTaskPool")
        {
            public Object newInstance()
            {
                PushToConsumerTask _task = new PushToConsumerTask(taskProcessor_.getDeliverTaskExecutor(),
                                                                  taskProcessor_,
                                                                  TaskFactory.this);

                return _task;
            }
        };

    ////////////////////////////////////////

    public TaskFactory( TaskProcessor taskProcessor )
    {
        taskProcessor_ = taskProcessor;
    }

    ////////////////////////////////////////

    public void setDeliverTaskPool(AbstractTaskPool pool) {
        deliverTaskPool_ = pool;
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


    FilterProxyConsumerTask newFilterProxyConsumerTask() {
        return (FilterProxyConsumerTask)filterProxyConsumerTaskPool_.lendObject();
    }


    FilterProxyConsumerTask newFilterConsumerProxyTask( Message event )
    {
        FilterProxyConsumerTask task = newFilterProxyConsumerTask();

        task.setMessage( event );
        task.setCurrentFilterStage( new FilterStage[] { event.getInitialFilterStage() } );

        return task;
    }

    private FilterSupplierAdminTask newFilterSupplierAdminTask() {
        return (FilterSupplierAdminTask)filterSupplierAdminTaskPool_.lendObject();
    }

    FilterSupplierAdminTask newFilterSupplierAdminTask( FilterProxyConsumerTask t )
    {
        FilterSupplierAdminTask task = newFilterSupplierAdminTask();

        if (t.getFilterStageToBeProcessed().length != 1) {
            throw new RuntimeException();
        }

        task.setMessage( t.removeMessage() );

        task.setCurrentFilterStage( t.getFilterStageToBeProcessed() );

        task.setSkip( t.getSkip() );

        return task;
    }

    FilterConsumerAdminTask newFilterConsumerAdminTask() {
        return (FilterConsumerAdminTask)filterConsumerAdminTaskPool_.lendObject();
    }

    FilterConsumerAdminTask newFilterConsumerAdminTask( FilterSupplierAdminTask t )
    {
        FilterConsumerAdminTask task = newFilterConsumerAdminTask();

        task.setMessage(t.removeMessage());
        task.setCurrentFilterStage( t.getFilterStageToBeProcessed() );

        return task;
    }

    private FilterProxySupplierTask newFilterProxySupplierTask() {
        return (FilterProxySupplierTask)filterProxySupplierTaskPool_.lendObject();
    }

    FilterProxySupplierTask newFilterProxySupplierTask( FilterConsumerAdminTask t )
    {
        FilterProxySupplierTask task = newFilterProxySupplierTask();

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
     * @param seqFilterStageWithMessageConsumer Array of FilterStages
     * that have an MessageConsumer attached.
     *
     * @param defaultMessage the Message that is to be
     * delivered by the created PushToConsumerTask. This method gains
     * possession of the Message.
     *
     * @param map alternate Messages that should be used for
     * MessageConsumers.
     *
     * @return a <code>PushToConsumerTask[]</code> value
     */
    AbstractDeliverTask[] newPushToConsumerTask(FilterStage [] filterStagesWithMessageConsumer,
                                                Message defaultMessage,
                                                FilterProxySupplierTask.AlternateMessageMap map) {

        AbstractDeliverTask[] _seqPushToConsumerTask =
            new AbstractDeliverTask[ filterStagesWithMessageConsumer.length ];

        for ( int x = 0; x < filterStagesWithMessageConsumer.length; ++x )
            {
                _seqPushToConsumerTask[ x ] =
                    ( AbstractDeliverTask ) deliverTaskPool_.lendObject();

                _seqPushToConsumerTask[ x ]
                    .setMessageConsumer( filterStagesWithMessageConsumer[ x ].getMessageConsumer() );

                Message _alternateEvent =
                    map.getAlternateMessage(filterStagesWithMessageConsumer[x]);

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
            }
        return _seqPushToConsumerTask;
    }

    /**
     * factory method to create PushToConsumer Tasks. The Tasks are
     * initialized with the data taken from a FilterProxySupplierTask.
     */
    AbstractDeliverTask[] newPushToConsumerTask( FilterProxySupplierTask task )
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

