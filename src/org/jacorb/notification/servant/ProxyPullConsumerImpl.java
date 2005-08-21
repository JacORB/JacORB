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
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageSupplier;
import org.omg.CORBA.Any;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosEventComm.PullSupplier;
import org.omg.CosNotifyChannelAdmin.ProxyConsumerHelper;
import org.omg.CosNotifyChannelAdmin.ProxyPullConsumerOperations;
import org.omg.CosNotifyChannelAdmin.ProxyPullConsumerPOATie;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

/**
 * @jmx.mbean extends = "AbstractProxyConsumerMBean"
 * @jboss.xmbean
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ProxyPullConsumerImpl extends AbstractProxyConsumer implements
        ProxyPullConsumerOperations, MessageSupplier, MessageSupplierDelegate, ProxyPullConsumerImplMBean
{
    /**
     * the connected PullSupplier
     */
    private PullSupplier pullSupplier_;

    private long pollInterval_;

    private final PullMessagesOperation pullMessagesOperation_;

    private final PullMessagesUtility pollTaskUtility_;

    // //////////////////////////////////////

    public ProxyPullConsumerImpl(IAdmin admin, ORB orb, POA poa, Configuration conf,
            TaskProcessor taskProcessor, MessageFactory messageFactory, OfferManager offerManager,
            SubscriptionManager subscriptionManager)
    {
        super(admin, orb, poa, conf, taskProcessor, messageFactory, null, offerManager,
                subscriptionManager);

        pollInterval_ = conf.getAttributeAsLong(Attributes.PULL_CONSUMER_POLL_INTERVAL,
                Default.DEFAULT_PULL_CONSUMER_POLL_INTERVAL);

        pullMessagesOperation_ = new PullMessagesOperation(this);
        
        pollTaskUtility_ = new PullMessagesUtility(taskProcessor, this);
    }

    // //////////////////////////////////////

    public ProxyType MyType()
    {
        return ProxyType.PULL_ANY;
    }

    public void disconnect_pull_consumer()
    {
        destroy();
    }

    protected void disconnectClient()
    {
        stopTask();

        pullSupplier_.disconnect_pull_supplier();

        pullSupplier_ = null;
    }

    protected void connectionSuspended()
    {
        stopTask();
    }

    protected void connectionResumed()
    {
        startTask();
    }

    public void runPullMessage() throws Disconnected
    {
        pullMessagesOperation_.runPull();
    }

    public void connect_any_pull_supplier(PullSupplier pullSupplier) throws AlreadyConnected
    {
        checkIsNotConnected();

        pullSupplier_ = pullSupplier;

        connectClient(pullSupplier);

        startTask();
    }

    private synchronized void startTask()
    {
        pollTaskUtility_.startTask(pollInterval_);
    }

    private synchronized void stopTask()
    {
        pollTaskUtility_.stopTask();
    }

    public synchronized Servant getServant()
    {
        if (thisServant_ == null)
        {
            thisServant_ = new ProxyPullConsumerPOATie(this);
        }

        return thisServant_;
    }

    public org.omg.CORBA.Object activate()
    {
        return ProxyConsumerHelper.narrow(getServant()._this_object(getORB()));
    }

    // //////////////////////////////////////
    // todo collect management informations

    public long getPollInterval()
    {
        return pollInterval_;
    }

    public long getPullTimer()
    {
        return pullMessagesOperation_.getTimeSpentInPull();
    }

    public int getPullCounter()
    {
        return pullMessagesOperation_.getPullCounter();
    }

    public int getSuccessfulPullCounter()
    {
        return pullMessagesOperation_.getSuccessfulPullCounter();
    }

    public PullResult pullMessages() throws Disconnected
    {
        BooleanHolder _hasEvent = new BooleanHolder();
        Any _event = pullSupplier_.try_pull(_hasEvent);

        return new MessageSupplierDelegate.PullResult(_event, _hasEvent.value);
    }

    public void queueMessages(PullResult data)
    {
        Message _message = getMessageFactory().newMessage((Any) data.data_, this);

        checkMessageProperties(_message);

        processMessage(_message);
    }
}
