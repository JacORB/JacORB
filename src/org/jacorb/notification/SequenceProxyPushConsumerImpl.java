package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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

import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPushConsumerOperations;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPushConsumerPOATie;
import org.omg.CosNotifyComm.SequencePushSupplier;
import org.omg.PortableServer.Servant;

/**
 * SequenceProxyPushConsumerImpl.java
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class SequenceProxyPushConsumerImpl
    extends StructuredProxyPushConsumerImpl
    implements SequenceProxyPushConsumerOperations
{

    private SequencePushSupplier mySequencePushSupplier_;

    public SequenceProxyPushConsumerImpl( SupplierAdminTieImpl supplierAdmin,
                                          ApplicationContext appContext,
                                          ChannelContext channelContext,
                                          PropertyManager adminProperties,
                                          PropertyManager qosProperties,
                                          Integer key )
    {
        super( supplierAdmin,
               appContext,
               channelContext,
               adminProperties,
               qosProperties,
               key );

        setProxyType( ProxyType.PUSH_SEQUENCE );
    }

    protected void disconnectClient()
    {
        if ( connected_ )
        {
            if ( mySequencePushSupplier_ != null )
            {
                connected_ = false;
                mySequencePushSupplier_.disconnect_sequence_push_supplier();
                mySequencePushSupplier_ = null;
            }

        }
    }

    public void connect_sequence_push_supplier( SequencePushSupplier supplier )
        throws AlreadyConnected
    {
        if ( connected_ )
        {
            throw new AlreadyConnected();
        }

        connected_ = true;
        mySequencePushSupplier_ = supplier;
    }

    public void push_structured_events( StructuredEvent[] events ) throws Disconnected
    {
        for ( int x = 0; x < events.length; ++x )
        {
            push_structured_event( events[ x ] );
        }
    }

    public void disconnect_sequence_push_consumer()
    {
        dispose();
    }

    public synchronized Servant getServant()
    {
        if ( thisServant_ == null )
            {
                thisServant_ = new SequenceProxyPushConsumerPOATie( this );
            }

        return thisServant_;
    }
}
