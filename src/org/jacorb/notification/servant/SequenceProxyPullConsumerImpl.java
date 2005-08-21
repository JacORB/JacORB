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
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageSupplier;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.ProxyConsumerHelper;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPullConsumerOperations;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPullConsumerPOATie;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyComm.SequencePullSupplier;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

/**
 * @jmx.mbean extends = "AbstractProxyConsumerMBean"
 * @jboss.xmbean
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */

public class SequenceProxyPullConsumerImpl extends AbstractProxyConsumer implements
        SequenceProxyPullConsumerOperations, SequenceProxyPullConsumerImplMBean, MessageSupplier, MessageSupplierDelegate
{
    private SequencePullSupplier sequencePullSupplier_;

    private final PullMessagesUtility pollTaskUtility_;

    private final long pollInterval_;

    private final PullMessagesOperation pullMessagesOperation_;
    
    // //////////////////////////////////////

    public SequenceProxyPullConsumerImpl(IAdmin admin, ORB orb, POA poa, Configuration config,
            TaskProcessor taskProcessor, MessageFactory messageFactory, OfferManager offerManager,
            SubscriptionManager subscriptionManager, SupplierAdmin supplierAdmin)
    {
        super(admin, orb, poa, config, taskProcessor, messageFactory, supplierAdmin, offerManager,
                subscriptionManager);

        pollInterval_ = config.getAttributeAsLong(Attributes.PULL_CONSUMER_POLL_INTERVAL,
                Default.DEFAULT_PULL_CONSUMER_POLL_INTERVAL);

        pollTaskUtility_ = new PullMessagesUtility(taskProcessor, this);
        
        pullMessagesOperation_ = new PullMessagesOperation(this);
    }

    public ProxyType MyType()
    {
        return ProxyType.PULL_SEQUENCE;
    }

    public void disconnect_sequence_pull_consumer()
    {
        destroy();
    }

    public synchronized void connect_sequence_pull_supplier(
            SequencePullSupplier sequencePullSupplier) throws AlreadyConnected
    {
        checkIsNotConnected();

        sequencePullSupplier_ = sequencePullSupplier;

        connectClient(sequencePullSupplier);

        pollTaskUtility_.startTask(pollInterval_);
    }

    protected void disconnectClient()
    {
        pollTaskUtility_.stopTask();
        sequencePullSupplier_.disconnect_sequence_pull_supplier();
        sequencePullSupplier_ = null;
    }

    public synchronized Servant getServant()
    {
        if (thisServant_ == null)
        {
            thisServant_ = new SequenceProxyPullConsumerPOATie(this);
        }

        return thisServant_;
    }

    public org.omg.CORBA.Object activate()
    {
        return ProxyConsumerHelper.narrow(getServant()._this_object(getORB()));
    }

    public PullResult pullMessages() throws Disconnected
    {
        BooleanHolder _hasEvent = new BooleanHolder();
        _hasEvent.value = false;
        StructuredEvent[] _events = sequencePullSupplier_.try_pull_structured_events(1, _hasEvent);

        return new MessageSupplierDelegate.PullResult(_events, _hasEvent.value);
    }

    public void queueMessages(PullResult pullResult)
    {
        StructuredEvent[] _events = (StructuredEvent[]) pullResult.data_;
        Message[] _messages = newMessages(_events);

        for (int x = 0; x < _messages.length; ++x)
        {
            processMessage(_messages[x]);
        }
    }

    public void runPullMessage() throws Disconnected
    {
        pullMessagesOperation_.runPull();
    }
}
