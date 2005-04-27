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

package org.jacorb.notification.interfaces;

import org.jacorb.notification.engine.PushTaskExecutor;


public interface IProxyPushSupplier extends MessageConsumer, CallbackingDisposable
{
    /**
     * process pending work. push events to its connected
     * (Push)Consumer.
     */
    void pushPendingData();

    /**
     * check if this MessageConsumer has pending work to do. pending
     * work is to push events
     * to its connected (Push)Consumer.
     */
    boolean hasPendingData();

    /**
     * Disable Deliveries. this MessageConsumer may not invoke remote
     * operations. events are enqueued instead.
     */
    void disableDelivery();
    
    /**
     * reset the error counter for this MessageConsumer to zero.
     */
    void resetErrorCounter();


    /**
     * increment the current error count by one for this MessageConsumer.
     */
    int incErrorCounter();

    /**
     * 
     */
    boolean isRetryAllowed();

    void schedulePush(PushTaskExecutor.PushTask pushTask);
}
