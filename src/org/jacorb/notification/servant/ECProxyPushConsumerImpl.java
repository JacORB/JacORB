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

import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.ProxyPushConsumerHelper;
import org.omg.CosEventChannelAdmin.ProxyPushConsumerOperations;
import org.omg.CosEventChannelAdmin.ProxyPushConsumerPOATie;
import org.omg.PortableServer.Servant;

import org.jacorb.notification.ChannelContext;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class ECProxyPushConsumerImpl
    extends ProxyPushConsumerImpl
    implements ProxyPushConsumerOperations
{

    ECProxyPushConsumerImpl( SupplierAdminTieImpl myAdminServant,
                             ChannelContext channelContext)
    {
        super( myAdminServant,
               channelContext);
    }

    ////////////////////////////////////////

    public void connect_push_supplier( org.omg.CosEventComm.PushSupplier pushSupplier )
        throws AlreadyConnected
    {
        connect_any_push_supplier( pushSupplier );
    }


    public synchronized Servant getServant()
    {
        if ( thisServant_ == null )
        {
            thisServant_ = new ProxyPushConsumerPOATie( this );
        }

        return thisServant_;
    }


    public org.omg.CORBA.Object activate()
    {
        return ProxyPushConsumerHelper.narrow(getServant()._this_object(getORB()));
    }

}
