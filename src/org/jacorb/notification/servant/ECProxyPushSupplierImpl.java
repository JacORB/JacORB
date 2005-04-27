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
import org.jacorb.notification.engine.PushTaskExecutorFactory;
import org.jacorb.notification.engine.TaskProcessor;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.ProxyPushSupplierHelper;
import org.omg.CosEventChannelAdmin.ProxyPushSupplierOperations;
import org.omg.CosEventChannelAdmin.ProxyPushSupplierPOATie;
import org.omg.CosEventComm.PushConsumer;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class ECProxyPushSupplierImpl extends ProxyPushSupplierImpl implements
        ProxyPushSupplierOperations
{
    private static final ConsumerAdmin NO_ADMIN = null;
    
    public ECProxyPushSupplierImpl(IAdmin admin, ORB orb, POA poa, Configuration conf,
            TaskProcessor taskProcessor, PushTaskExecutorFactory pushTaskExecutorFactory) throws ConfigurationException
    {
        super(admin, orb, poa, conf, taskProcessor, pushTaskExecutorFactory, OfferManager.NULL_MANAGER, SubscriptionManager.NULL_MANAGER, NO_ADMIN);
    }

    ////////////////////////////////////////

    public void connect_push_consumer(PushConsumer pushConsumer) throws AlreadyConnected
    {
        connect_any_push_consumer(pushConsumer);
    }

    public synchronized Servant getServant()
    {
        if (thisServant_ == null)
        {
            thisServant_ = new ProxyPushSupplierPOATie(this);
        }
        return thisServant_;
    }

    public org.omg.CORBA.Object activate()
    {
        return ProxyPushSupplierHelper.narrow(getServant()._this_object(getORB()));
    }
}