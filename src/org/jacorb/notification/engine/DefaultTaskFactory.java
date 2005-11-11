package org.jacorb.notification.engine;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

import org.apache.avalon.framework.configuration.Configuration;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageConsumer;
import org.jacorb.notification.util.AbstractPoolablePool;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class DefaultTaskFactory implements Disposable, TaskFactory
{
    /**
     * TaskExecutor used to invoke match-Operation on filters
     */
    private final TaskExecutor filterTaskExecutor_;
    
    private final int filterWorkerPoolSize_;
    
    private final AbstractPoolablePool filterProxyConsumerTaskPool_ = 
        new AbstractPoolablePool("FilterProxyConsumerTaskPool")
    {
        public Object newInstance()
        {
            return new FilterProxyConsumerTask(DefaultTaskFactory.this,
                    filterTaskExecutor_);
        }
    };

    private final AbstractPoolablePool filterSupplierAdminTaskPool_ = 
        new AbstractPoolablePool("FilterSupplierAdminTaskPool")
    {
        public Object newInstance()
        {
            return new FilterSupplierAdminTask(DefaultTaskFactory.this,
                    filterTaskExecutor_);
        }
    };

    private final AbstractPoolablePool filterConsumerAdminTaskPool_ = 
        new AbstractPoolablePool("FilterConsumerAdminTaskPool")
    {
        public Object newInstance()
        {
            return new FilterConsumerAdminTask(DefaultTaskFactory.this,
                    filterTaskExecutor_);
        }
    };

    private final AbstractPoolablePool filterProxySupplierTaskPool_ = 
        new AbstractPoolablePool("FilterProxySupplierTaskPool")
    {
        public Object newInstance()
        {
            return new FilterProxySupplierTask(DefaultTaskFactory.this,
                    filterTaskExecutor_);
        }
    };

    // //////////////////////////////////////

    public DefaultTaskFactory(Configuration config)
    {
        filterWorkerPoolSize_ = config.getAttributeAsInteger(Attributes.FILTER_POOL_WORKERS,
                Default.DEFAULT_FILTER_POOL_SIZE);
        
        filterTaskExecutor_ = new DefaultTaskExecutor("FilterThread", filterWorkerPoolSize_);
        
        configure(config);
    }

    // //////////////////////////////////////

    private void configure(Configuration conf)
    {
        filterProxyConsumerTaskPool_.configure(conf);
        filterProxySupplierTaskPool_.configure(conf);
        filterConsumerAdminTaskPool_.configure(conf);
        filterSupplierAdminTaskPool_.configure(conf);
    }

    public void dispose()
    {
        filterTaskExecutor_.dispose();
        filterProxyConsumerTaskPool_.dispose();
        filterProxySupplierTaskPool_.dispose();
        filterConsumerAdminTaskPool_.dispose();
        filterSupplierAdminTaskPool_.dispose();
    }

    // //////////////////////////////////////
    // Factory methods for FilterProxyConsumerTasks

    private FilterProxyConsumerTask newFilterProxyConsumerTask()
    {
        return (FilterProxyConsumerTask) filterProxyConsumerTaskPool_.lendObject();
    }

    public Schedulable newFilterProxyConsumerTask(Message message)
    {
        FilterProxyConsumerTask task = newFilterProxyConsumerTask();

        task.setMessage(message);

        task.setCurrentFilterStage(new FilterStage[] { message.getInitialFilterStage() });

        return task;
    }

    // //////////////////////////////////////
    // Factory methods for FilterSupplierAdminTasks
    // //////////////////////////////////////

    private FilterSupplierAdminTask newFilterSupplierAdminTask()
    {
        return (FilterSupplierAdminTask) filterSupplierAdminTaskPool_.lendObject();
    }

    public Schedulable newFilterSupplierAdminTask(FilterProxyConsumerTask oldTask)
    {
        FilterSupplierAdminTask _newTask = newFilterSupplierAdminTask();

        // TODO this really should be an assertion
        if (oldTask.getFilterStageToBeProcessed().length != 1)
        {
            throw new RuntimeException();
        }

        _newTask.setMessage(oldTask.removeMessage());

        _newTask.setCurrentFilterStage(oldTask.getFilterStageToBeProcessed());

        _newTask.setSkip(oldTask.getSkip());

        return _newTask;
    }

    // //////////////////////////////////////
    // Factory methods for FilterConsumerAdminTasks
    // //////////////////////////////////////

    private FilterConsumerAdminTask newFilterConsumerAdminTask()
    {
        return (FilterConsumerAdminTask) filterConsumerAdminTaskPool_.lendObject();
    }

    public Schedulable newFilterConsumerAdminTask(FilterSupplierAdminTask oldTask)
    {
        FilterConsumerAdminTask _newTask = newFilterConsumerAdminTask();

        _newTask.setMessage(oldTask.removeMessage());

        _newTask.setCurrentFilterStage(oldTask.getFilterStageToBeProcessed());

        return _newTask;
    }

    // //////////////////////////////////////
    // Factory methods for FilterProxySupplierTasks
    // //////////////////////////////////////

    private FilterProxySupplierTask newFilterProxySupplierTask()
    {
        return (FilterProxySupplierTask) filterProxySupplierTaskPool_.lendObject();
    }

    public Schedulable newFilterProxySupplierTask(FilterConsumerAdminTask task)
    {
        FilterProxySupplierTask _newTask = newFilterProxySupplierTask();

        _newTask.setMessage(task.removeMessage());

        FilterStage[] _filterStageList = task.getFilterStageToBeProcessed();

        _newTask.setCurrentFilterStage(_filterStageList);

        return _newTask;
    }

    // //////////////////////////////////////
    // Factory methods for AbstractDeliverTasks
    // //////////////////////////////////////

    public void enqueueMessage(FilterStage[] nodes, Message mesg)
    {
        enqueueMessage(nodes, mesg, FilterProxySupplierTask.EMPTY_MAP);
    }

    /**
     * @param filterStagesWithMessageConsumer
     *            Array of FilterStages that have an MessageConsumer attached.
     * 
     * @param mesg
     *            the Message that is to be delivered by the created PushToConsumerTask. This method
     *            assumes ownership of the Message.
     * 
     * @param map
     *            Map(FilterStage=>Message) of alternate Messages that should be used for specific
     *            MessageConsumers.
     */
    private void enqueueMessage(FilterStage[] filterStagesWithMessageConsumer, Message mesg,
            FilterProxySupplierTask.AlternateMessageMap map)
    {
        for (int x = 0; x < filterStagesWithMessageConsumer.length; ++x)
        {
            MessageConsumer consumer = filterStagesWithMessageConsumer[x].getMessageConsumer();

            Message alternateMessage = map.getAlternateMessage(filterStagesWithMessageConsumer[x]);

            if (alternateMessage != null)
            {
                consumer.queueMessage(alternateMessage);
            }
            else
            {
                consumer.queueMessage(mesg);
            }
        }
    }

    /**
     * factory method to create PushToConsumer Tasks. The Tasks are initialized with the data taken
     * from a FilterProxySupplierTask.
     */
    public void enqueueMessage(FilterProxySupplierTask task)
    {
        Message _message = task.removeMessage();

        FilterStage[] _seqFilterStageToBeProcessed = task.getFilterStageToBeProcessed();

        enqueueMessage(_seqFilterStageToBeProcessed, _message, task.changedMessages_);
    }
}
