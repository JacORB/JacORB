package org.jacorb.notification.servant;

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

import org.jacorb.notification.ChannelContext;
import org.jacorb.notification.CollectionsWrapper;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageConsumer;

import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyActive;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyInactive;
import org.omg.CosNotifyChannelAdmin.NotConnected;
import org.omg.CosNotifyChannelAdmin.ProxySupplierHelper;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushSupplierOperations;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushSupplierPOATie;
import org.omg.CosNotifyComm.NotifyPublishHelper;
import org.omg.CosNotifyComm.StructuredPushConsumer;
import org.omg.PortableServer.Servant;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class StructuredProxyPushSupplierImpl
    extends AbstractProxySupplier
    implements StructuredProxyPushSupplierOperations
{
    private StructuredPushConsumer pushConsumer_;

    protected boolean active_;

    ////////////////////////////////////////

    public StructuredProxyPushSupplierImpl( AbstractAdmin myAdminServant,
                                            ChannelContext channelContext)
    {
        super( myAdminServant,
               channelContext );

        setProxyType( ProxyType.PUSH_STRUCTURED );
    }

    ////////////////////////////////////////

    /**
     * TODO check error handling when push fails
     */
    public void deliverMessage( Message event ) throws Disconnected
    {
        if (logger_.isDebugEnabled())
        {
            logger_.debug( "deliverEvent connected="
                           + isConnected()
                           + " active="
                           + active_
                           + " enabled="
                           + isEnabled() );
        }

        if ( isConnected() ) {

            if ( active_ && isEnabled() )
                {
                    pushConsumer_.push_structured_event( event.toStructuredEvent() );

                    event.dispose();
                }
            else
                {
                    // not enabled
                    enqueue( event );
                }
        }
        else
        {
            logger_.debug( "Not connected" );
        }
    }


    public void connect_structured_push_consumer( StructuredPushConsumer consumer )
        throws AlreadyConnected
    {
        assertNotConnected();

        if (logger_.isDebugEnabled())
        {
            logger_.debug("connect structured_push_consumer");
        }

        pushConsumer_ = consumer;

        connectClient(consumer);

        active_ = true;
    }


    public void disconnect_structured_push_supplier()
    {
        dispose();
    }


    synchronized public void suspend_connection()
        throws NotConnected,
               ConnectionAlreadyInactive
    {
        assertConnected();

        if ( !active_ )
        {
            throw new ConnectionAlreadyInactive();
        }

        active_ = false;
    }


    public void deliverPendingMessages() throws Disconnected
    {
        Message[] _events = getAllMessages();

        if (_events != null)
        {
            try
            {
                for (int x = 0; x < _events.length; ++x)
                {
                    pushConsumer_.push_structured_event( _events[x].toStructuredEvent() );
                    _events[x].dispose();
                }
            }
            finally
            {

            }
        }
    }


    public void resume_connection() throws NotConnected, ConnectionAlreadyActive
    {
        assertConnected();

        if ( active_ )
        {
            throw new ConnectionAlreadyActive();
        }

        try {
            deliverPendingMessages();

            active_ = true;
        } catch (Disconnected e) {
            logger_.fatalError("Illegal State: PushConsumer thinks it is disconnected."
                               + " StructuredProxyPushSupplier thinks it is connected", e);

            dispose();
        }
    }


    protected void disconnectClient()
    {
        pushConsumer_.disconnect_structured_push_consumer();

        pushConsumer_ = null;
    }


    public List getSubsequentFilterStages()
    {
        return CollectionsWrapper.singletonList( this );
    }


    public MessageConsumer getMessageConsumer()
    {
        return this;
    }


    public boolean hasMessageConsumer()
    {
        return true;
    }


    public synchronized Servant getServant()
    {
        if ( thisServant_ == null )
        {
            thisServant_ = new StructuredProxyPushSupplierPOATie( this );
        }
        return thisServant_;
    }


    public org.omg.CORBA.Object activate()
    {
        return ProxySupplierHelper.narrow( getServant()._this_object(getORB()) );
    }
}
