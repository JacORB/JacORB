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

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class PushToConsumerTask extends AbstractDeliverTask
{
    private static int sCount = 0;

    private final int id_ = ++sCount;

    ////////////////////

    public PushToConsumerTask(TaskProcessor tp) {
        super(tp);
    }

    ////////////////////

    public void doWork() throws Exception
    {
        if ( logger_.isDebugEnabled() )
        {
            logger_.debug( this
                           + ".push "
                           + getMessage()
                           + " to "
                           + getMessageConsumer() );
        }

        getMessageConsumer().deliverMessage( getMessage() );
    }


    public String toString() {
        return "[PushToConsumerTask#" + id_ + "]";
    }
}
