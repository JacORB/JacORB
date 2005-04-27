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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageConsumer;
import org.jacorb.notification.util.CollectionsWrapper;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPullSupplierOperations;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPullSupplierPOATie;
import org.omg.CosNotifyComm.SequencePullConsumer;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class SequenceProxyPullSupplierImpl extends StructuredProxyPullSupplierImpl implements
        SequenceProxyPullSupplierOperations
{
    private static final StructuredEvent[] UNDEFINED_SEQUENCE;

    ////////////////////////////////////////

    static
    {
        UNDEFINED_SEQUENCE = new StructuredEvent[] { UNDEFINED_STRUCTURED_EVENT };
    }

    ////////////////////////////////////////

    private SequencePullConsumer sequencePullConsumer_;

    ////////////////////////////////////////

    public SequenceProxyPullSupplierImpl(IAdmin admin, ORB orb, POA poa, Configuration conf,
            TaskProcessor taskProcessor, OfferManager offerManager, SubscriptionManager subscriptionManager, ConsumerAdmin consumerAdmin) throws ConfigurationException
    {
        super(admin, orb, poa, conf, taskProcessor, offerManager, subscriptionManager, consumerAdmin);
    }

    public ProxyType MyType()
    {
        return ProxyType.PULL_SEQUENCE;
    }

    public void connect_sequence_pull_consumer(SequencePullConsumer consumer)
            throws AlreadyConnected
    {
        checkIsNotConnected();

        connectClient(consumer);

        sequencePullConsumer_ = consumer;

        logger_.info("connect sequence_pull_consumer");
    }

    public StructuredEvent[] pull_structured_events(int number) throws Disconnected
    {
        checkStillConnected();

        StructuredEvent _structuredEvents[] = UNDEFINED_SEQUENCE;

        Message[] _messages = getUpToMessages(number);

        if (_messages != null && _messages.length > 0)
        {
            _structuredEvents = new StructuredEvent[_messages.length];

            for (int x = 0; x < _messages.length; ++x)
            {
                _structuredEvents[x] = _messages[x].toStructuredEvent();
                _messages[x].dispose();
            }
        }

        return _structuredEvents;
    }

    public StructuredEvent[] try_pull_structured_events(int number, BooleanHolder success)
            throws Disconnected
    {
        checkStillConnected();

        Message[] _messages = getUpToMessages(number);

        if (_messages != null && _messages.length > 0)
        {
            StructuredEvent[] _ret = new StructuredEvent[_messages.length];

            for (int x = 0; x < _messages.length; ++x)
            {
                _ret[x] = _messages[x].toStructuredEvent();

                _messages[x].dispose();
            }
            success.value = true;

            return _ret;
        }
        success.value = false;

        return UNDEFINED_SEQUENCE;
    }

    public List getSubsequentFilterStages()
    {
        return CollectionsWrapper.singletonList(this);
    }

    public MessageConsumer getMessageConsumer()
    {
        return this;
    }

    protected void disconnectClient()
    {
        sequencePullConsumer_.disconnect_sequence_pull_consumer();

        sequencePullConsumer_ = null;

        logger_.info("disconnect sequence_pull_consumer");
    }

    public void disconnect_sequence_pull_supplier()
    {
        destroy();
    }

    public synchronized Servant getServant()
    {
        if (thisServant_ == null)
        {
            thisServant_ = new SequenceProxyPullSupplierPOATie(this);
        }

        return thisServant_;
    }
}