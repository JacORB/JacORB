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

package org.jacorb.notification;

import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.servant.AbstractProxyConsumerI;
import org.omg.CORBA.Any;
import org.omg.CORBA.NVList;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public interface MessageFactory
{
    Message newMessage(Any any, AbstractProxyConsumerI consumer);

    Message newMessage(StructuredEvent structuredEvent,
            AbstractProxyConsumerI consumer);

    Message newMessage(String interfaceName, String operationName, NVList args,
            AbstractProxyConsumerI consumer);

    ////////////////////////////////////////
    
    Message newMessage(Property[] props);

    Message newMessage(Any any);

    Message newMessage(StructuredEvent structuredEvent);
}