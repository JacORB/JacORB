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
import org.jacorb.notification.engine.TaskProcessor;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.ProxyPullConsumerOperations;
import org.omg.CosEventChannelAdmin.ProxyPullConsumerPOATie;
import org.omg.CosEventComm.PullSupplier;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

/**
 *  @jmx.mbean extends = "AbstractProxyConsumerMBean"
 *  @jboss.xmbean
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ECProxyPullConsumerImpl extends ProxyPullConsumerImpl implements
        ProxyPullConsumerOperations, ECProxyPullConsumerImplMBean
{
    public ECProxyPullConsumerImpl(IAdmin admin, ORB orb, POA poa, Configuration conf,
            TaskProcessor taskProcessor, MessageFactory messageFactory)
    {
        super(admin, orb, poa, conf, taskProcessor, messageFactory, OfferManager.NULL_MANAGER,
                SubscriptionManager.NULL_MANAGER);
    }

    public void connect_pull_supplier(PullSupplier pullSupplier) throws AlreadyConnected
    {
        connect_any_pull_supplier(pullSupplier);
    }

    public Servant newServant()
    {
        return new ProxyPullConsumerPOATie(this);
    }
}