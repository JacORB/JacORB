package org.jacorb.notification.servant;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

import org.omg.CosEventChannelAdmin.ProxyPushSupplierOperations;
import org.omg.CosEventComm.PushConsumer;
import org.jacorb.notification.ChannelContext;
import org.jacorb.notification.PropertyManager;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.PortableServer.Servant;
import org.omg.CosEventChannelAdmin.ProxyPushSupplierPOATie;
import org.omg.CosEventChannelAdmin.ProxyPushSupplierHelper;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class ECProxyPushSupplierImpl
    extends ProxyPushSupplierImpl
    implements ProxyPushSupplierOperations{

    ECProxyPushSupplierImpl(AbstractAdmin myAdminServant,
                          ChannelContext channelContext,
                          PropertyManager adminProperties,
                          PropertyManager qosProperties,
                          Integer key)
        throws UnsupportedQoS
    {

        super(myAdminServant,
              channelContext,
              adminProperties,
              qosProperties,
              key);

        isKeyPublic_ = false;
    }

    ////////////////////////////////////////

    public void connect_push_consumer(PushConsumer pushConsumer)
        throws AlreadyConnected
    {
        connect_any_push_consumer(pushConsumer);
    }


    public synchronized Servant getServant() {
        if (thisServant_ == null) {
            thisServant_ = new ProxyPushSupplierPOATie(this);
        }
        return thisServant_;
    }

    public org.omg.CORBA.Object getCorbaRef() {
        return ProxyPushSupplierHelper.narrow( getServant()._this_object(getORB()) );
    }

}
