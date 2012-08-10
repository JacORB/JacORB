/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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
import org.jacorb.notification.queue.MessageQueue.DiscardListener;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class DefaultMessageQueueAdapter implements MessageQueueAdapter
{
    private final MessageQueue queue_;

    private static final Message[] EMPTY = new Message[0];

    public DefaultMessageQueueAdapter(MessageQueue queue)
    {
        super();

        queue_ = queue;
    }

    /*
     * @see org.jacorb.notification.queue.EventQueueDelegate#enqeue(org.jacorb.notification.interfaces.Message)
     */
    public void enqeue(Message message)
    {
        queue_.put(message);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jacorb.notification.queue.EventQueueDelegate#hasPendingData()
     */
    public boolean hasPendingMessages()
    {
        return !queue_.isEmpty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jacorb.notification.queue.EventQueueDelegate#getPendingMessagesCount()
     */
    public int getPendingMessagesCount()
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

    public void clear()
    {
        try
        {
            Message[] allMessages = queue_.getAllMessages(false);

            for (int i = 0; i < allMessages.length; i++)
            {
                Message message = allMessages[i];
                message.dispose();
            }
        } catch (InterruptedException e)
        {
            // should not happen as above call does not wait.
        }
    }
    
    public String toString()
    {
        return queue_.toString();
    }

    public String getDiscardPolicyName()
    {
        return queue_.getDiscardPolicyName();
    }

    public String getOrderPolicyName()
    {
        return queue_.getOrderPolicyName();
    }

    public void addDiscardListener(DiscardListener listener)
    {
        queue_.addDiscardListener(listener);
    }

    public void removeDiscardListener(DiscardListener listener)
    {
        queue_.removeDiscardListener(listener);
    }
}