package org.jacorb.notification.servant;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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
import org.omg.CosEventComm.PushConsumer;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.ProxyPushSupplierOperations;
import org.omg.CosNotifyChannelAdmin.ProxyPushSupplierPOATie;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

/**
 * @jmx.mbean extends = "AbstractProxyPushSupplierMBean"
 * @jboss.xmbean
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ProxyPushSupplierImpl extends AbstractProxyPushSupplier implements
        ProxyPushSupplierOperations, ProxyPushSupplierImplMBean
{
    private class PushAnyOperation extends MessagePushOperation 
    {
        public PushAnyOperation(Message message) {
            super(message);
        }

        public void invokePush() throws Disconnected {
            deliverMessageInternal(message_);
        }
    }
    
    private PushConsumer pushConsumer_;
    
    private long timeSpent_;

    // //////////////////////////////////////

    public ProxyPushSupplierImpl(IAdmin admin, ORB orb, POA poa, Configuration conf,
            TaskProcessor taskProcessor, PushTaskExecutorFactory pushTaskExecutorFactory, OfferManager offerManager,
            SubscriptionManager subscriptionManager, ConsumerAdmin consumerAdmin)
            throws ConfigurationException
    {
        super(admin, orb, poa, conf, taskProcessor, pushTaskExecutorFactory, offerManager,
                subscriptionManager, consumerAdmin);
    }

    public ProxyType MyType()
    {
        return ProxyType.PUSH_ANY;
    }

    public void disconnect_push_supplier()
    {
        destroy();
    }

    protected void disconnectClient()
    {
        pushConsumer_.disconnect_push_consumer();

        pushConsumer_ = null;
    }

    private void deliverMessageWithRetry(final Message message)
    {
        try
        {
            deliverMessageInternal(message);
        } catch (Exception e)
        {
            final PushAnyOperation _failedOperation = new PushAnyOperation(message);

            handleFailedPushOperation(_failedOperation, e);
        }
    }

    void deliverMessageInternal(final Message message) throws Disconnected
    {
        long now = System.currentTimeMillis();
        pushConsumer_.push(message.toAny());
        timeSpent_ += (System.currentTimeMillis() - now);
        resetErrorCounter();
    }

    public void pushPendingData()
    {
        Message _message = null;
        
        while((_message = getMessageNoBlock()) != null)
        {        
            try
            {
                deliverMessageWithRetry(_message);
            } finally
            {
                _message.dispose();
            }
        }
    }

    public void connect_any_push_consumer(PushConsumer pushConsumer) throws AlreadyConnected
    {
        checkIsNotConnected();

        pushConsumer_ = pushConsumer;

        connectClient(pushConsumer);
    }

   
    protected void connectionResumed()
    {
        schedulePush();
    }

    public Servant newServant()
    {
        return new ProxyPushSupplierPOATie(this);
    }

    public long getCost()
    {
        return timeSpent_;
    }
}