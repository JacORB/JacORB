package org.jacorb.notification.interfaces;

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

import org.jacorb.notification.engine.TaskExecutor;

/**
 * The interface MessageConsumer provides an abstraction of an
 * ProxySupplier.
 * <br>
 * The MessageConsumer is responsible
 * to maintain the Connection to the real Consumer. To deliver a
 * Message
 * the MessageConsumer converts the Message to the
 * appropiate Format (Any, StructuredEvent, Sequence of
 * StructuredEvent) required by its Consumer and delivers it.
 * <br>
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public interface MessageConsumer extends Disposable {

    /**
     * @return the <code>TaskExecutor</code> that should be used to
     * push Messages to the connected Consumer.
     */
    TaskExecutor getExecutor();


    /**
     * process pending work. push events to its connected
     * (Push)Consumer.
     */
    void deliverPendingData();


    /**
     * check if this MessageConsumer has pending work to do. pending
     * work is to push events
     * to its connected (Push)Consumer.
     */
    boolean hasPendingData();


    /**
     * activate deliveries. this MessageConsumer may invoke remote
     * operations again.
     */
    void enableDelivery();


    /**
     * Disable Deliveries. this MessageConsumer may not invoke remote
     * operations. events are enqueued instead.
     */
    void disableDelivery();


    /**
     * Deliver a Message to the associated Consumer.
     */
    void deliverMessage(Message m);


    /**
     * reset the error counter for this MessageConsumer to zero.
     */
    void resetErrorCounter();


    /**
     * increment the current error count by one for this MessageConsumer.
     */
    int incErrorCounter();


    /**
     * check if this MessageConsumer is still valid.
     */
    boolean isDisposed();
    
    /**
     * 
     */
    boolean isRetryAllowed();
}
