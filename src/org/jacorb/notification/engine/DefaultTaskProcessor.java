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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.IProxyPushSupplier;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageSupplier;
import org.omg.CORBA.Any;
import org.omg.CosNotification.StructuredEvent;

import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class DefaultTaskProcessor implements TaskProcessor, Disposable
{
    private class TimeoutTask implements Runnable, Message.MessageStateListener
    {
        Object timerRegistration_;

        final Message message_;

        public TimeoutTask(Message message)
        {
            message_ = message;
            message_.setMessageStateListener(this);
            timerRegistration_ = executeTaskAfterDelay(message.getTimeout(), this);
        }

        public void actionLifetimeChanged(long timeout)
        {
            ClockDaemon.cancel(timerRegistration_);
            timerRegistration_ = executeTaskAfterDelay(message_.getTimeout(), this);
        }

        public void run()
        {
            logger_.debug("run Timeout");

            message_.removeMessageStateListener();

            message_.actionTimeout();
        }
    }

    // //////////////////

    private class DeferedStopTask implements Runnable
    {
        final Message message_;

        public DeferedStopTask(Message message)
        {
            message_ = message;

            executeTaskAt(message.getStopTime(), this);
        }

        public void run()
        {
            message_.actionTimeout();
        }
    }

    // //////////////////

    class DeferedStartTask implements Runnable
    {
        final Message message_;

        DeferedStartTask(Message m)
        {
            if (logger_.isDebugEnabled())
            {
                logger_.debug("Message with Option StartTime=" + m.getStartTime()
                        + " will be defered until then");
            }

            message_ = m;

            executeTaskAt(message_.getStartTime(), this);
        }

        public void run()
        {
            if (logger_.isDebugEnabled())
            {
                logger_.debug("Defered Message " + message_ + " will be processed now");
            }

            processMessageInternal(message_);
        }
    }

    // //////////////////

    final Logger logger_;

    /**
     * TaskExecutor used to invoke match-Operation on filters
     */
    private TaskExecutor matchTaskExecutor_;

    /**
     * TaskExecutor used to invoke pull-Operation on PullSuppliers.
     */
    private TaskExecutor pullTaskExecutor_;

    /**
     * ClockDaemon to schedule Operation that must be run at a specific time.
     */
    private ClockDaemon clockDaemon_;

    /**
     * TaskFactory that is used to create new Tasks.
     */
    private DefaultTaskFactory taskFactory_;

    // //////////////////////////////////////

    /**
     * Start ClockDaemon Set up TaskExecutors Set up TaskFactory
     */
    public DefaultTaskProcessor(Configuration config)
    {
        clockDaemon_ = new ClockDaemon();

        clockDaemon_.setThreadFactory(new ThreadFactory()
        {
            public Thread newThread(Runnable command)
            {
                Thread _t = new Thread(command);
                _t.setName("ClockDaemonThread");
                return _t;
            }
        });

        logger_ = ((org.jacorb.config.Configuration) config).getNamedLogger(getClass().getName());

        logger_.info("create TaskProcessor");

        // create pull worker pool. allow pull workers to die
        int value = config.getAttributeAsInteger(Attributes.PULL_POOL_WORKERS,
                Default.DEFAULT_PULL_POOL_SIZE);

        pullTaskExecutor_ = new DefaultTaskExecutor("PullThread", value, true);

        value = config.getAttributeAsInteger(Attributes.FILTER_POOL_WORKERS,
                Default.DEFAULT_FILTER_POOL_SIZE);

        matchTaskExecutor_ = new DefaultTaskExecutor("FilterThread", value);

        value = config.getAttributeAsInteger(Attributes.DELIVER_POOL_WORKERS,
                Default.DEFAULT_DELIVER_POOL_WORKERS);

        taskFactory_ = new DefaultTaskFactory(this);

        taskFactory_.configure(config);
    }

    public TaskFactory getTaskFactory()
    {
        return taskFactory_;
    }

    public TaskExecutor getFilterTaskExecutor()
    {
        return matchTaskExecutor_;
    }

    /**
     * shutdown this TaskProcessor. The TaskExecutors will be shutdown, the running Threads
     * interrupted and all allocated ressources will be freed. As the active Threads will be
     * interrupted pending Events will be discarded.
     */
    public void dispose()
    {
        logger_.info("shutdown TaskProcessor");

        clockDaemon_.shutDown();

        matchTaskExecutor_.dispose();

        pullTaskExecutor_.dispose();

        taskFactory_.dispose();

        logger_.debug("shutdown complete");
    }

    /**
     * process a Message. the various settings for the Message (timeout, starttime, stoptime) are
     * checked and applied.
     */
    public void processMessage(Message mesg)
    {
        if (mesg.hasStopTime())
        {
            logger_.debug("Message has StopTime");
            if (mesg.getStopTime() <= System.currentTimeMillis())
            {
                fireEventDiscarded(mesg);
                mesg.dispose();
                logger_.debug("Message Stoptime is passed already");

                return;
            }

            new DeferedStopTask(mesg);
        }

        if (mesg.hasTimeout())
        {
            logger_.debug("Message has TimeOut");
            new TimeoutTask(mesg);
        }

        if (mesg.hasStartTime() && (mesg.getStartTime() > System.currentTimeMillis()))
        {
            new DeferedStartTask(mesg);
        }
        else
        {
            processMessageInternal(mesg);
        }
    }

    /**
     * process a Message. create FilterTask and schedule it.
     */
    protected void processMessageInternal(Message event)
    {
        logger_.debug("processMessageInternal");

        Schedulable _task = taskFactory_.newFilterProxyConsumerTask(event);

        try
        {
            _task.schedule();
        } catch (InterruptedException ie)
        {
            logger_.info("Interrupt while scheduling FilterTask", ie);
        }
    }

    /**
     * Schedule ProxyPullConsumer for pull-Operation. If a Supplier connects to a ProxyPullConsumer
     * the ProxyPullConsumer needs to regularely poll the Supplier. This method queues a Task to run
     * runPullEvent on the specified TimerEventSupplier
     */
    public void scheduleTimedPullTask(MessageSupplier messageSupplier) throws InterruptedException
    {
        PullFromSupplierTask _task = new PullFromSupplierTask(pullTaskExecutor_);

        _task.setTarget(messageSupplier);

        _task.schedule();
    }

    /**
     * Schedule MessageConsumer for a deliver-Operation. Some MessageConsumers (namely
     * SequenceProxyPushSuppliers) need to push Messages regularely to its connected Consumer.
     * Schedule a Task to call deliverPendingEvents on the specified MessageConsumer. Also used
     * after a disabled MessageConsumer is enabled again to push the pending Messages.
     */
    public void schedulePushOperation(IProxyPushSupplier pushSupplier) throws InterruptedException
    {
        throw new UnsupportedOperationException();
    }

    // //////////////////////////////////////
    // Timer Operations
    // //////////////////////////////////////

    /**
     * access the Clock Daemon instance.
     */
    private ClockDaemon getClockDaemon()
    {
        return clockDaemon_;
    }

    public Object executeTaskPeriodically(long intervall, Runnable task, boolean startImmediately)
    {
        logger_.debug("executeTaskPeriodically");

        return getClockDaemon().executePeriodically(intervall, task, startImmediately);
    }

    public void cancelTask(Object id)
    {
        ClockDaemon.cancel(id);
    }

    public Object executeTaskAfterDelay(long delay, Runnable task)
    {
        return clockDaemon_.executeAfterDelay(delay, task);
    }

    Object executeTaskAt(long startTime, Runnable task)
    {
        return executeTaskAt(new Date(startTime), task);
    }

    Object executeTaskAt(Date startTime, Runnable task)
    {
        return clockDaemon_.executeAt(startTime, task);
    }

    // //////////////////////////////////////

    private void fireEventDiscarded(Message event)
    {
        switch (event.getType()) {
        case Message.TYPE_ANY:
            fireEventDiscarded(event.toAny());
            break;

        case Message.TYPE_STRUCTURED:
            fireEventDiscarded(event.toStructuredEvent());
            break;

        default:
            throw new RuntimeException();
        }
    }

    private void fireEventDiscarded(Any a)
    {
        if (logger_.isDebugEnabled())
        {
            logger_.debug("Any: " + a + " has been discarded");
        }
    }

    private void fireEventDiscarded(StructuredEvent e)
    {
        if (logger_.isDebugEnabled())
        {
            logger_.debug("StructuredEvent: " + e + " has been discarded");
        }
    }
}