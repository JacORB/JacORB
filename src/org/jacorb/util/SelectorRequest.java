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
    CONNECT, ACCEPT, READ, WRITE, TIMER
      }

  public enum Status {
    PENDING, ASSIGNED, READY, EXPIRED, FAILED, FINISHED, SHUTDOWN, CLOSED
      }

  public final Type type;
  public Status status = null;
  public final SocketChannel channel;
  public SelectionKey key = null;
  public final int op;
  public final SelectorRequestCallback callback;
  public final long nanoDeadline;

  private final ReentrantLock lock = new ReentrantLock();

  // deadline is an absolute time. It has to be based upon System.nanoTime() and is used as such
  // deadline of '0' is infinite
  public SelectorRequest (Type type, SocketChannel channel, SelectorRequestCallback callback, long nanoDeadline) {
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
    this.nanoDeadline = (nanoDeadline == 0 ? Long.MAX_VALUE : nanoDeadline);
  }

  // constructor for creating timer requests
  // deadline is an absolute time. It has to be based upon System.nanoTime() and is used as such
  // deadline of '0' is infinite
  public SelectorRequest (SelectorRequestCallback callback, long nanoDeadline) {
    type = Type.TIMER;
    op = 0;
    channel = null;
    this.callback = callback;
    this.nanoDeadline = (nanoDeadline == 0 ? Long.MAX_VALUE : nanoDeadline);
  }

  public void setStatus (Status status) {

    synchronized (lock) {
      this.status = status;

      // the below might just need to be notify()
      lock.notify();
    }
  }

  public boolean isFinalized () {
    // PENDING, ASSIGNED & READY are intermediate states
    return status != null && status != Status.PENDING && status != Status.ASSIGNED && status != Status.READY;
  }

  // deadline is an absolute time. It has to be based upon System.nanoTime() and is used as such
  // deadline of '0' is infinite
  public Status waitOnCompletion (long nanoDeadline) {

    long myNanoDeadline = (nanoDeadline == 0 ? Long.MAX_VALUE : nanoDeadline);

    synchronized (lock) {
      while (myNanoDeadline > System.nanoTime() && !isFinalized()) {

        long remaining = myNanoDeadline - System.nanoTime();
        long millis = remaining/1000000;
        int nanos = (int)(remaining - (millis * 1000000));

        try {
          lock.wait (millis, nanos);
        }
        catch (InterruptedException e) {
          // ignored
        }
        catch (IllegalArgumentException ex) {

          // indicates we have run out of time
          // if the value of timeout is negative or the value of nanos is not in the range 0-999999.
          break;
        }
      }
    }
    return status;
  }

}