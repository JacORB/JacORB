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
public class BasicMessageQueueAdapter implements MessageQueueAdapter
{
    private final MessageQueue queue_;
    private static final Message[] EMPTY = new Message[0];

    /**
     *  
     */
    public BasicMessageQueueAdapter(MessageQueue queue)
    {
        super();
        
        queue_ = queue;
    }

    /*
     * @see org.jacorb.notification.queue.EventQueueDelegate#enqeue(org.jacorb.notification.interfaces.Message)
     */
    public void enqeue(Message message) throws InterruptedException
    {
        // enqueue a copy of the Message to ensure this queue owns the Message
        queue_.put((Message) message.clone());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jacorb.notification.queue.EventQueueDelegate#hasPendingData()
     */
    public boolean hasPendingMessages() throws InterruptedException
    {
        return !queue_.isEmpty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jacorb.notification.queue.EventQueueDelegate#getPendingMessagesCount()
     */
    public int getPendingMessagesCount() throws InterruptedException
    {
        return queue_.getSize();
    }

    /*
     * caller gets ownership over the returned message
     * 
     * @see org.jacorb.notification.queue.EventQueueDelegate#getMessageBlocking()
     */
    public Message getMessageBlocking() throws InterruptedException
    {
        return queue_.getMessage(true);
    }

    /*
     * caller gets ownership over the returned message
     * 
     * @see org.jacorb.notification.queue.EventQueueDelegate#getMessageNoBlock()
     */
    public Message getMessageNoBlock() throws InterruptedException
    {
        return queue_.getMessage(false);
    }

    /*
     * caller gets ownership over the returned message
     * 
     * @see org.jacorb.notification.queue.EventQueueDelegate#getAllMessages()
     */
    public Message[] getAllMessages() throws InterruptedException
    {
        return queue_.getAllMessages(false);
    }

    /*
     * caller gets ownership over the returned message
     * 
     * @see org.jacorb.notification.queue.EventQueueDelegate#getUpToMessages(int)
     */
    public Message[] getUpToMessages(int max) throws InterruptedException
    {
        return queue_.getMessages(max, false);
    }

    /*
     * caller gets ownership over the returned message
     * 
     * @see org.jacorb.notification.queue.EventQueueDelegate#getAtLeastMessages(int)
     */
    public Message[] getAtLeastMessages(int min) throws InterruptedException
    {
        if (queue_.getSize() >= min)
        {
            return queue_.getAllMessages(true);
        }

        return EMPTY;
    }
}