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

import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageConsumer;
import org.jacorb.notification.interfaces.MessageSupplier;
import org.jacorb.notification.servant.AbstractProxySupplier;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */


public interface TaskProcessor
{
    TaskFactory getTaskFactory();


    long getBackoutInterval ();

    /**
     * configure a AbstractProxySupplier to use a TaskExecutor
     * dependent on the settings for the current Channel.
     *
     * @todo remove dependency from class AbstractProxySupplier
     */
     void configureTaskExecutor(AbstractProxySupplier proxySupplier);


    TaskExecutor getFilterTaskExecutor();


    /**
     * process a Message. the various settings for the Message
     * (timeout, starttime, stoptime) are checked and applied.
     */
     void processMessage( Message mesg );


    /**
     * Schedule ProxyPullConsumer for pull-Operation.
     * If a Supplier connects to a ProxyPullConsumer the
     * ProxyPullConsumer needs to regularely poll the Supplier.
     * This method queues a Task to run runPullEvent on the specified
     * TimerEventSupplier
     */
     void scheduleTimedPullTask( MessageSupplier dest )
        throws InterruptedException;

    /**
     * Schedule MessageConsumer for a deliver-Operation.
     * Some MessageConsumers (namely SequenceProxyPushSuppliers) need to
     * push Messages regularely to its
     * connected Consumer. Schedule a Task to call
     * deliverPendingEvents on the specified MessageConsumer.
     * Also used after a disabled MessageConsumer is enabled again to
     * push the pending Messages.
     */
     void scheduleTimedPushTask( MessageConsumer consumer )
        throws InterruptedException;

    ////////////////////////////////////////
    // Timer Operations
    ////////////////////////////////////////



     Object executeTaskPeriodically( long intervall,
				     Runnable task,
				     boolean startImmediately );


    void cancelTask( Object id );


    Object executeTaskAfterDelay( long delay, Runnable task );


    //    Object executeTaskAt( Date startTime, Runnable task );

}
