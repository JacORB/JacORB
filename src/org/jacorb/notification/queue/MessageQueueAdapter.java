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

package org.jacorb.notification.queue;

import org.jacorb.notification.interfaces.Message;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public interface MessageQueueAdapter
{
    void enqeue(Message message) throws InterruptedException;

    boolean hasPendingMessages() throws InterruptedException;

    int getPendingMessagesCount() throws InterruptedException;

    Message getMessageBlocking() throws InterruptedException;

    Message getMessageNoBlock() throws InterruptedException;

    Message[] getAllMessages() throws InterruptedException;

    Message[] getUpToMessages(int max) throws InterruptedException;

    Message[] getAtLeastMessages(int min) throws InterruptedException;
}