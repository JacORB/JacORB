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
import org.jacorb.notification.interfaces.JMXManageable;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageSupplier;
import org.jacorb.notification.util.DisposableManager;
import org.omg.CORBA.Any;
import org.omg.CosNotification.StructuredEvent;

import edu.emory.mathcs.backport.java.util.concurrent.Executors;
import edu.emory.mathcs.backport.java.util.concurrent.ScheduledExecutorService;
import edu.emory.mathcs.backport.java.util.concurrent.ScheduledFuture;
import edu.emory.mathcs.backport.java.util.concurrent.ThreadFactory;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

/**
 * @jmx.mbean
 * @jboss.xmbean
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class DefaultTaskProcessor implements TaskProcessor, Disposable, JMXManageable, DefaultTaskProcessorMBean
{
    private class TimeoutTask implements Runnable, Message.MessageStateListener
    {
        ScheduledFuture timerRegistration_;

        final Message message_;

        public TimeoutTask(Message message)
        {
            message_ = message;
            message_.setMessageStateListener(this);
            timerRegistration_ = executeTaskAfterDelay(message.getTimeout(), this);
        }

        public void actionLifetimeChanged(long timeout)
        {
            timerRegistration_.cancel(true);
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

    /**
     * TaskExecutor used to invoke pull-Operation on PullSuppliers.
     */
    private TaskExecutor pullTaskExecutor_;

    /**
     * ClockDaemon to schedule Operation that must be run at a specific time.
     */
    private ScheduledExecutorService clockDaemon_;

    /**
     * TaskFactory that is used to create new Tasks.
     */
    private final TaskFactory taskFactory_;

    private final DisposableManager disposables_ = new DisposableManager();

    private int pullWorkerPoolSize_;

    private int filterWorkerPoolSize_;

    // //////////////////////////////////////

    /**
     * Start ClockDaemon Set up TaskExecutors Set up TaskFactory
     */
    public DefaultTaskProcessor(Configuration config, TaskFactory taskFactory)
    {
        clockDaemon_ = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactory()
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

        pullWorkerPoolSize_ = config.getAttributeAsInteger(Attributes.PULL_POOL_WORKERS,
                        Default.DEFAULT_PULL_POOL_SIZE);

        pullTaskExecutor_ = new DefaultTaskExecutor("PullThread", pullWorkerPoolSize_, true);

        filterWorkerPoolSize_ = config.getAttributeAsInteger(Attributes.FILTER_POOL_WORKERS,
                        Default.DEFAULT_FILTER_POOL_SIZE);

        taskFactory_ = taskFactory;
    }

    public TaskFactory getTaskFactory()
    {
        return taskFactory_;
    }

    /**
     * shutdown this TaskProcessor. The TaskExecutors will be shutdown, the running Threads
     * interrupted and all allocated ressources will be freed. As the active Threads will be
     * interrupted pending Events will be discarded.
     */
    public void dispose()
    {
        logger_.info("shutdown TaskProcessor");

        clockDaemon_.shutdown();

        pullTaskExecutor_.dispose();

        disposables_.dispose();

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
        Schedulable _task = taskFactory_.newFilterProxyConsumerTask(event);

        _task.schedule();
    }

    /**
     * Schedule ProxyPullConsumer for pull-Operation. If a Supplier connects to a ProxyPullConsumer
     * the ProxyPullConsumer needs to regularely poll the Supplier. This method queues a Task to run
     * runPullEvent on the specified TimerEventSupplier
     */
    public void scheduleTimedPullTask(MessageSupplier messageSupplier)
    {
        PullFromSupplierTask _task = new PullFromSupplierTask(pullTaskExecutor_);

        _task.setTarget(messageSupplier);

        _task.schedule();
    }

    // //////////////////////////////////////
    // Timer Operations
    // //////////////////////////////////////

    /**
     * access the Clock Daemon instance.
     */
    private ScheduledExecutorService getClockDaemon()
    {
        return clockDaemon_;
    }

    public ScheduledFuture executeTaskPeriodically(long intervall, Runnable task, boolean startImmediately)
    {
        return getClockDaemon().scheduleAtFixedRate(task, startImmediately ? 0 : intervall, intervall, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture executeTaskAfterDelay(long delay, Runnable task)
    {
        return clockDaemon_.schedule(task, delay, TimeUnit.MILLISECONDS);
    }

    Object executeTaskAt(long startTime, Runnable task)
    {
        return executeTaskAt(new Date(startTime), task);
    }

    Object executeTaskAt(Date startTime, Runnable task)
    {
        long now = System.currentTimeMillis();
        long then = startTime.getTime();

        long delay = then - now;

        if (delay < 1000)
        {
            delay = 1000;
        }

        return clockDaemon_.schedule(task, delay, TimeUnit.MILLISECONDS);
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

    public String getJMXObjectName()
    {
       return "service=TaskProcessor";
    }

    public void registerDisposable(Disposable disposable)
    {
        disposables_.addDisposable(disposable);
    }

    public String[] getJMXNotificationTypes()
    {
        return null;
    }

    public void setJMXCallback(JMXCallback callback)
    {
        // no notifications yet
    }

    /**
     * @jmx.managed-attribute description = "FilterPoolWorkers are used to invoke the Filters attached to Proxies and Admins"
     *                        access = "read-only"
     */
    public int getFilterWorkerPoolSize()
    {
        return filterWorkerPoolSize_;
    }

    /**
     * @jmx.managed-attribute description = "PullWorkers are used to invoke try_pull on PushSupplier-Clients"
     *                        access = "read-only"
     */
    public int getPullWorkerPoolSize()
    {
        return pullWorkerPoolSize_;
    }
}