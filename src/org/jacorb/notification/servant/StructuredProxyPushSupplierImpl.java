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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.engine.MessagePushOperation;
import org.jacorb.notification.engine.PushTaskExecutorFactory;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushSupplierOperations;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushSupplierPOATie;
import org.omg.CosNotifyComm.StructuredPushConsumer;
import org.omg.CosNotifyComm.StructuredPushConsumerOperations;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

/**
 * @jmx.mbean extends = "AbstractProxyPushSupplierMBean"
 * @jboss.xmbean
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */

public class StructuredProxyPushSupplierImpl extends AbstractProxyPushSupplier implements
        StructuredProxyPushSupplierOperations, StructuredProxyPushSupplierImplMBean
{
    private class PushStructuredOperation extends MessagePushOperation 
    {    
        public PushStructuredOperation(Message message) {
            super(message);
        }

        public void invokePush() throws Disconnected {
            deliverMessageInternal(message_);
        }
    }
    
    private StructuredPushConsumerOperations pushConsumer_;

    private long timeSpent_;
    
    // //////////////////////////////////////

    public StructuredProxyPushSupplierImpl(IAdmin admin, ORB orb, POA poa, Configuration conf,
            TaskProcessor taskProcessor, PushTaskExecutorFactory pushTaskExecutorFactory, OfferManager offerManager,
            SubscriptionManager subscriptionManager, ConsumerAdmin consumerAdmin)
            throws ConfigurationException
    {
        super(admin, orb, poa, conf, taskProcessor, pushTaskExecutorFactory, offerManager,
                subscriptionManager, consumerAdmin);        
    }

    public ProxyType MyType()
    {
        return ProxyType.PUSH_STRUCTURED;
    }

    public boolean pushEvent()
    {
        final Message _message = getMessageNoBlock();

        if (_message != null)
        {
            try
            {
                return deliverMessageWithRetry(_message);
            } finally
            {
                _message.dispose();
            }
        }

        return false;
    }

    private boolean deliverMessageWithRetry(final Message message)
    {
        try
        {
            deliverMessageInternal(message);
            
            return true;
        } catch (Exception e)
        {
            final PushStructuredOperation _failedOperation = new PushStructuredOperation(message);

            handleFailedPushOperation(_failedOperation, e);
            
            return false;
        }
    }

    private void deliverMessageInternal(final Message message) throws Disconnected
    {
        final long now = System.currentTimeMillis();
        pushConsumer_.push_structured_event(message.toStructuredEvent());
        final long _duration = (System.currentTimeMillis() - now);
        timeSpent_ += _duration;
        resetErrorCounter();
        
        if (logger_.isDebugEnabled())
        {
            logger_.debug("Push took " + _duration + " ms");
        }
    }

    public void connect_structured_push_consumer(StructuredPushConsumer consumer)
            throws AlreadyConnected
    {
        checkIsNotConnected();

        if (logger_.isDebugEnabled())
        {
            logger_.debug("connect structured_push_consumer");
        }

        pushConsumer_ = consumer;

        connectClient(consumer);
    }

    public void disconnect_structured_push_supplier()
    {
        destroy();
    }

    protected void connectionResumed()
    {
        scheduleFlush();
    }

    protected void disconnectClient()
    {
        pushConsumer_.disconnect_structured_push_consumer();

        pushConsumer_ = null;
    }

    public Servant newServant()
    {
        return new StructuredProxyPushSupplierPOATie(this);
    }

    protected long getCost()
    {
        return timeSpent_;
    }
}