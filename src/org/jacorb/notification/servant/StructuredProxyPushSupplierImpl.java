package org.jacorb.notification.servant;

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

import java.util.List;

import org.jacorb.notification.CollectionsWrapper;
import org.jacorb.notification.engine.PushStructuredOperation;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageConsumer;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.ProxySupplierHelper;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushSupplierOperations;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushSupplierPOATie;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.StructuredPushConsumer;
import org.omg.CosNotifyComm.StructuredPushConsumerOperations;
import org.omg.PortableServer.Servant;

/**
 * @author Alphonse Bendt
 * @version $Id: StructuredProxyPushSupplierImpl.java,v 1.9 2004/07/12 11:19:56
 *          alphonse.bendt Exp $
 */

public class StructuredProxyPushSupplierImpl extends AbstractProxySupplier implements
        StructuredProxyPushSupplierOperations
{
    private final static StructuredPushConsumerOperations NULL_CONSUMER = new StructuredPushConsumerOperations()
    {
        public void push_structured_event(StructuredEvent event)
        {
        }

        public void disconnect_structured_push_consumer()
        {
        }

        public void offer_change(EventType[] added, EventType[] removed) throws InvalidEventType
        {
        }
    };

    private StructuredPushConsumerOperations pushConsumer_;
    
    private final Object refLock_ = new Object();

    ////////////////////////////////////////

    public ProxyType MyType()
    {
        return ProxyType.PUSH_STRUCTURED;
    }

    /**
     * TODO check error handling when push fails
     */
    public void deliverMessage(final Message message)
    {
        if (logger_.isDebugEnabled())
        {
            logger_.debug("deliverMessage() connected=" + isConnected() + " suspended="
                    + isSuspended() + " enabled=" + isEnabled());
        }

        if (isConnected())
        {

            if (!isSuspended() && isEnabled())
            {
                try
                {
                    pushConsumer_.push_structured_event(message.toStructuredEvent());
                } catch (Throwable e)
                {
                    PushStructuredOperation _failedOperation = new PushStructuredOperation(
                            pushConsumer_, message);

                    handleFailedPushOperation(_failedOperation, e);
                }
            }
            else
            {
                // not enabled
                enqueue(message);
            }
        }
        else
        {
            logger_.debug("Not connected");
        }
    }

    public void connect_structured_push_consumer(StructuredPushConsumer consumer)
            throws AlreadyConnected
    {
        assertNotConnected();

        if (logger_.isDebugEnabled())
        {
            logger_.debug("connect structured_push_consumer");
        }

        pushConsumer_ = consumer;

        connectClient(consumer);
    }

    public void disconnect_structured_push_supplier()
    {
        dispose();
    }

    public void deliverPendingData()
    {
        Message[] _events = getAllMessages();

        if (_events != null)
        {
            for (int x = 0; x < _events.length; ++x)
            {
                deliverMessage(_events[x]);

                _events[x].dispose();
            }
        }
    }

    protected void connectionResumed()
    {
        scheduleDeliverPendingMessagesOperation_.run();
    }

    protected void disconnectClient()
    {
        pushConsumer_.disconnect_structured_push_consumer();

        pushConsumer_ = NULL_CONSUMER;
    }

    public List getSubsequentFilterStages()
    {
        return CollectionsWrapper.singletonList(this);
    }

    public MessageConsumer getMessageConsumer()
    {
        return this;
    }

    public boolean hasMessageConsumer()
    {
        return true;
    }

    public synchronized Servant getServant()
    {
        if (thisServant_ == null)
        {
            thisServant_ = new StructuredProxyPushSupplierPOATie(this);
        }
        return thisServant_;
    }

    public org.omg.CORBA.Object activate()
    {
        return ProxySupplierHelper.narrow(getServant()._this_object(getORB()));
    }
}