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
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyActive;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyInactive;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.NotConnected;
import org.omg.CosNotifyChannelAdmin.ProxySupplierHelper;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushSupplierOperations;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushSupplierPOATie;
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

    protected boolean enabled_;

    ////////////////////////////////////////

    public StructuredProxyPushSupplierImpl( AbstractAdmin myAdminServant,
                                            ChannelContext channelContext)
        throws UnsupportedQoS
    {
        super( myAdminServant,
               channelContext );

        setProxyType( ProxyType.PUSH_STRUCTURED );
        enabled_ = true;
    }

    ////////////////////////////////////////

    public void deliverMessage( Message event )
    {
        if (logger_.isDebugEnabled()) {
            logger_.debug( "deliverEvent connected="
                           + connected_
                           + " active="
                           + active_
                           + " enabled="
                           + enabled_ );
        }

        if ( connected_ )
        {
            try
            {
                if ( active_ && enabled_ )
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
            catch ( Disconnected d )
            {
                connected_ = false;
                logger_.warn( "push failed - PushConsumer was disconnected" );
            }
        }
        else
        {
            logger_.debug( "Not connected" );
        }
    }


    public void connect_structured_push_consumer( StructuredPushConsumer consumer )
        throws AlreadyConnected,
               TypeError
    {
        if ( connected_ )
        {
            throw new AlreadyConnected();
        }

        if (logger_.isDebugEnabled()) {
            logger_.debug("connect structured_push_consumer");
        }

        pushConsumer_ = consumer;
        connected_ = true;
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
        if ( !connected_ )
        {
            throw new NotConnected();
        }

        if ( !active_ )
        {
            throw new ConnectionAlreadyInactive();
        }

        active_ = false;
    }


    public void deliverPendingMessages() throws NotConnected
    {
        Message[] _events = getAllMessages();

        if (_events != null) {
            for (int x=0; x<_events.length; ++x) {
                try {
                    if (logger_.isDebugEnabled()) {
                        logger_.debug(pushConsumer_
                                      + ".push_structured_event("
                                      + _events[x].toStructuredEvent()
                                      + ")" );
                    }

                    pushConsumer_.push_structured_event( _events[x].toStructuredEvent() );
                } catch (Disconnected e) {
                    connected_ = false;
                    throw new NotConnected();
                } finally {
                    _events[x].dispose();
                }
            }
        }
    }


    public void resume_connection() throws NotConnected, ConnectionAlreadyActive
    {
        if ( !connected_ )
        {
            throw new NotConnected();
        }

        if ( active_ )
        {
            throw new ConnectionAlreadyActive();
        }

        deliverPendingMessages();

        active_ = true;
    }


    protected void disconnectClient()
    {
        if ( connected_ )
        {
            if ( pushConsumer_ != null )
            {
                try {
                    pushConsumer_.disconnect_structured_push_consumer();
                } catch (Exception e) {
                    logger_.warn("Error disconnecting consumer: ", e);
                }
                pushConsumer_ = null;
                connected_ = false;
            }
        }
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


    synchronized public void enableDelivery()
    {
        enabled_ = true;
    }


    synchronized public void disableDelivery()
    {
        enabled_ = false;
    }


    public synchronized Servant getServant()
    {
        if ( thisServant_ == null )
            {
                thisServant_ = new StructuredProxyPushSupplierPOATie( this );
            }
        return thisServant_;
    }


    public org.omg.CORBA.Object activate() {
        return ProxySupplierHelper.narrow( getServant()._this_object(getORB()) );
    }
}
