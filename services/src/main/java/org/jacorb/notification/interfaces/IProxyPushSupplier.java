/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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

package org.jacorb.notification.interfaces;

import org.jacorb.notification.engine.PushTaskExecutor;

public interface IProxyPushSupplier extends MessageConsumer, NotifyingDisposable
{
    /**
     * Disable Deliveries. no remote
     * operations may be used to deliver a message. messages
     * are enqueued instead.
     */
    void disableDelivery();
    
    /**
     * reset the error count to zero.
     */
    void resetErrorCounter();

    /**
     * increment the current error count by one
     */
    int incErrorCounter();

    boolean isRetryAllowed();

    /**
     * schedule a pushTask for execution using this Objects executor.
     */
    void scheduleTask(PushTaskExecutor.PushTask pushTask);

    void scheduleFlush();
}
