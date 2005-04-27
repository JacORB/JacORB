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

package org.jacorb.notification.engine;

import org.jacorb.notification.interfaces.Message;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public abstract class AbstractMessageTask extends AbstractTask
{
    private Message message_;

    /**
     * set the Message for this Task to use.
     */
    public void setMessage(Message message)
    {
        if (message_ != null)
        {
            throw new RuntimeException("remove old first");
        }

        message_ = message;
    }

    public Message removeMessage()
    {
        Message _mesg = message_;

        message_ = null;

        return _mesg;
    }

    public Message copyMessage()
    {
        return (Message) message_.clone();
    }

    protected boolean isRunnable()
    {
        return !message_.isInvalid();
    }

    protected void checkInterrupt() throws InterruptedException
    {
        super.checkInterrupt();

        if (message_.isInvalid())
        {
            throw new InterruptedException();
        }
    }

    protected Message getMessage()
    {
        return message_;
    }

    public void dispose()
    {
        if (message_ != null)
        {
            message_.dispose();
        }

        super.dispose();
    }

    public void reset()
    {
        message_ = null;
    }
}