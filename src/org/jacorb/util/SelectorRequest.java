/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2008 Gerald Brose.
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

import java.util.Date;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectionKey;
import java.util.concurrent.locks.ReentrantLock;

public class SelectorRequest {

  public enum Type {
    CONNECT, READ, WRITE
      }

  public enum Status {
    PENDING, ASSIGNED, EXPIRED, FAILED, SUCCESS, SHUTDOWN, CLOSED
      }

  public final Type type;
  public Status status = null;
  public final SocketChannel channel;
  public SelectionKey key = null;
  public final int op;
  public final Callback callback;
  public final Date deadline;

  private final ReentrantLock lock = new ReentrantLock();

  public SelectorRequest (Type type, SocketChannel channel, Callback callback, Date deadline) {
    this.type = type;
    switch (type) {
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
    this.deadline = (deadline != null ? deadline : new Date (Long.MAX_VALUE));
  }

  public void setStatus (Status status) {
    lock.lock ();
    try {
      this.status = status;

      // the below might just need to be notify()
      lock.notifyAll();
    }
    finally {
      lock.unlock();
    }
  }

  public Status waitOnCompletion (long timeout) {

    lock.lock ();

    try {
      while (status == Status.PENDING || status == Status.ASSIGNED) {
        try {
          lock.wait (timeout);
        }
        catch (InterruptedException e) {
          // ignored
        }
      }
    }
    finally {
      lock.unlock ();
    }
    return status;
  }

  public abstract class Callback {

    /**
       The callback to requestor. The return value determines if
       request needs to be re-registered.
       return: true (re-register action), false (don't register)
     */
    protected abstract boolean call (SelectorRequest action);
  }
}