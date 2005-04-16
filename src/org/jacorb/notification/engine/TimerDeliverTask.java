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

import org.jacorb.notification.interfaces.MessageConsumer;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotifyChannelAdmin.NotConnected;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class TimerDeliverTask extends AbstractTask
{
    private final MessageConsumer messageConsumer_;
    
    public TimerDeliverTask(TaskProcessor tp, MessageConsumer messageConsumer) {
        super(tp);
        
        messageConsumer_ = messageConsumer;
    }

    ////////////////////////////////////////

    public void doWork()
        throws Disconnected,
               NotConnected,
               InterruptedException
    {
        if ( !messageConsumer_.isDisposed() && messageConsumer_.hasPendingData() )
        {
            messageConsumer_.deliverPendingData();
        } 
    }


    public void schedule() throws InterruptedException {
        schedule(false);
    }

    /* (non-Javadoc)
     * @see org.jacorb.notification.engine.AbstractTask#handleTaskError(org.jacorb.notification.engine.AbstractTask, java.lang.Throwable)
     */
    void handleTaskError(AbstractTask t, Throwable error)
    {
        logger_.error("Error", error);
    }

    /* (non-Javadoc)
     * @see org.jacorb.notification.util.AbstractPoolable#reset()
     */
    public void reset()
    {
        // no operation
    }
}
