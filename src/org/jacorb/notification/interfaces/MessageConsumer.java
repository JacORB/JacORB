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

/**
 * The interface MessageConsumer provides an abstraction of an ProxySupplier. <br>
 * The MessageConsumer is responsible to maintain the Connection to the real Consumer. To deliver a
 * Message the MessageConsumer converts the Message to the appropiate Format (Any, StructuredEvent,
 * Sequence of StructuredEvent) required by its Consumer and delivers it. <br>
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */

public interface MessageConsumer extends Comparable
{
    /**
     * Deliver a Message to the associated Consumer.
     */
    void deliverMessage(Message m);

    /**
     * check if this MessageConsumer is still valid.
     */
    boolean isDisposed();

    void destroy();
}
