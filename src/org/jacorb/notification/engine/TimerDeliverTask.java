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

import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotifyChannelAdmin.NotConnected;

import org.jacorb.notification.interfaces.MessageConsumer;
import org.jacorb.notification.util.TaskExecutor;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class TimerDeliverTask extends AbstractDeliverTask
{
    TimerDeliverTask(TaskProcessor tp, TaskFactory tc) {
        super(tp, tc);
    }

    ////////////////////////////////////////

    public void doWork() throws Disconnected, NotConnected, InterruptedException
    {
        if ( getMessageConsumer().hasPendingMessages() )
        {
            getMessageConsumer().deliverPendingMessages();

            if ( getMessageConsumer().hasPendingMessages() )
            {
                schedule(true);
            }

        } else {
            if (logger_.isDebugEnabled()) {
                logger_.debug("Nothing to do as the Target:"
                              + getMessageConsumer()
                              + " has no Pending Events.");
            }

            dispose();
        }
    }

    public void schedule() throws InterruptedException {
        schedule(false);
    }
}
