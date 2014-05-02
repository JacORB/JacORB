/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2012 Gerald Brose / The JacORB Team.
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
 */

package org.jacorb.util;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
//import java.util.concurrent.locks.ReentrantLock;

/**
 * defines a single request to be registered with the selector.
 * this can be a timed event, I/O event, or I/O event with an
 * expiration.
 * @author Ciju John <johnc@ociweb.com>
 */
public class SelectorRequest
{

    /**
     * the types of events to be waited for.
     */
    public enum Type
    {
        CONNECT, ACCEPT, READ, WRITE, TIMER
    }

    /**
     *  the current status of the request.
     */
    public enum Status
    {
        PENDING, ASSIGNED, READY, EXPIRED, FAILED, IOERROR, FINISHED, SHUTDOWN, CLOSED
    }

    public final Type type;
    public Status status = null;
    public final SocketChannel channel;
    public SelectionKey key = null;
    public final int op;
    public SelectorRequestCallback callback;
    public final long nanoDeadline;

    //    private final ReentrantLock lock = new ReentrantLock();
    private final Object lock = new Object();

    /**
     * Constructs a new SelectorRequest for an I/O event
     * @param type is the kind of event to wait for
     * @param channel is the I/O on which the event is expected
     * @param callback is the object to be notified when the event occurs
     * @param nanoDeadline is an expiration time. It is an absolute time based upon System.nanoTime().
     * A deadline of '0' is used for no deadline.
     */
    public SelectorRequest (Type type,
                            SocketChannel channel,
                            SelectorRequestCallback callback,
                            long nanoDeadline)
    {
        this.type = type;
        switch (type)
        {
            case CONNECT:
                op = SelectionKey.OP_CONNECT;
                break;
            case READ:
                op = SelectionKey.OP_READ;
                break;
            case WRITE:
                op = SelectionKey.OP_WRITE;
                break;
            default:
                op = 0;
        }
        this.channel = channel;
        this.callback = callback;
        this.nanoDeadline = (nanoDeadline == 0 ? Long.MAX_VALUE : nanoDeadline);
    }

    /**
     * Constructs a new SelectorRequest for an timer event
     * @param callback is the object to be notified when the event occurs
     * @param nanoDeadline is an expiration time. It is an absolute time based upon System.nanoTime().
     * A deadline of '0' is used for no deadline.
     */
    public SelectorRequest (SelectorRequestCallback callback, long nanoDeadline)
    {
        type = Type.TIMER;
        op = 0;
        channel = null;
        this.callback = callback;
        this.nanoDeadline = (nanoDeadline == 0 ? Long.MAX_VALUE : nanoDeadline);
    }

   /**
     * Called by the SelectorManager to notify a change of status. Will wake up
     * a thread blocked in waitOnCOmpletion
     */
    public void setStatus (Status status)
    {
        synchronized (lock)
        {
            this.status = status;
            // the below might just need to be notify()
            lock.notify();
        }
    }

    /**
     * determines if the current status is one of the final statuses
     * @returns false if the status is PENDING, ASSIGNED, or READY, true for all others
     */

    public boolean isFinalized ()
    {
        return status != null &&
            status != Status.PENDING &&
            status != Status.ASSIGNED &&
            status != Status.READY;
    }

    /**
     * a blocking call to wait for a status change or a timeout.
     * @param nanoDeadline is an explicit timeout for waiting on a change
     * @returns this requester's status after a notification or a timeout.
     */
    public Status waitOnCompletion (long nanoDeadline)
    {

        long myNanoDeadline = (nanoDeadline == 0 ? Long.MAX_VALUE : nanoDeadline);

        synchronized (lock)
        {
            while (myNanoDeadline > System.nanoTime() && !isFinalized())
            {

                long remaining = myNanoDeadline - System.nanoTime();
                long millis = remaining / 1000000;
                int nanos = (int)(remaining - (millis * 1000000));

                try
                {
                    lock.wait (millis, nanos);
                }
                catch (InterruptedException e)
                {
                    // ignored
                }
                catch (IllegalArgumentException ex)
                {

                    // indicates we have run out of time if the value
                    // of timeout is negative or the value of nanos is
                    // not in the range 0-999999.
                    break;
                }
            }
        }
        return status;
    }

}