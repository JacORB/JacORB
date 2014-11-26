package org.jacorb.notification.servant;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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

import org.jacorb.config.*;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.engine.TaskProcessor;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.ProxyPullSupplierOperations;
import org.omg.CosEventChannelAdmin.ProxyPullSupplierPOATie;
import org.omg.CosEventComm.PullConsumer;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

/**
 * @jmx.mbean extends = "AbstractProxySupplierMBean"
 * @jboss.xmbean
 *
 * @author Alphonse Bendt
 */

public class ECProxyPullSupplierImpl extends ProxyPullSupplierImpl implements
        ProxyPullSupplierOperations, ECProxyPullSupplierImplMBean
{
    private static final ConsumerAdmin NO_ADMIN = null;
    
    public ECProxyPullSupplierImpl(IAdmin admin, ORB orb, POA poa, Configuration conf,
            TaskProcessor taskProcessor) throws ConfigurationException
    {
        super(admin, orb, poa, conf, taskProcessor, OfferManager.NULL_MANAGER, SubscriptionManager.NULL_MANAGER, NO_ADMIN);
    }

    public void connect_pull_consumer(PullConsumer pullConsumer) throws AlreadyConnected
    {
        connect_any_pull_consumer(pullConsumer);
    }

    public Servant newServant()
    {
        return new ProxyPullSupplierPOATie(this);
    }
}
