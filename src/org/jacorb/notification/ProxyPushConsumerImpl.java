package org.jacorb.notification;

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

import java.util.Collections;
import java.util.List;
import org.jacorb.notification.interfaces.EventConsumer;
import org.omg.CORBA.Any;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotifyChannelAdmin.ProxyPushConsumerOperations;
import org.omg.CosNotifyChannelAdmin.ProxyPushConsumerPOATie;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.PortableServer.Servant;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ProxyPushConsumerImpl
            extends ProxyBase
            implements ProxyPushConsumerOperations,
            org.omg.CosEventChannelAdmin.ProxyPushConsumerOperations
{

    private org.omg.CosEventComm.PushSupplier myPushSupplier;
    private boolean connected;
    private List subsequentDestinations_;

    ProxyPushConsumerImpl( SupplierAdminTieImpl myAdminServant,
                           ApplicationContext appContext,
                           ChannelContext channelContext,
                           PropertyManager adminProperties,
                           PropertyManager qosProperties )
    {
        super( myAdminServant,
               appContext,
               channelContext,
               adminProperties,
               qosProperties );

        init();
    }

    ProxyPushConsumerImpl( SupplierAdminTieImpl myAdminServant,
                           ApplicationContext appContext,
                           ChannelContext channelContext,
                           PropertyManager adminProperties,
                           PropertyManager qosProperties,
                           Integer key )
    {

        super( myAdminServant,
               appContext,
               channelContext,
               adminProperties,
               qosProperties,
               key );

        init();
    }

    private void init()
    {
        setProxyType( ProxyType.PUSH_ANY );
        connected = false;
        subsequentDestinations_ = JDK13CollectionsWrapper.singletonList( myAdmin_ );
    }

    public void disconnect_push_consumer()
    {
        if ( !disposed_ )
        {
            dispose();
        }
        else
        {
            throw new OBJECT_NOT_EXIST();
        }
    }

    private void disconnectClient()
    {
        if ( myPushSupplier != null )
        {
            logger_.info( "disconnect()" );
            myPushSupplier.disconnect_push_supplier();
            myPushSupplier = null;
        }
    }

    /**
     * Supplier sends data to the consumer (this object) using this call.
     */
    public void push( Any event ) throws Disconnected
    {
	//	logger_.debug("push(Any)");

        if ( !connected )
        {
            throw new Disconnected();
        }

        NotificationEvent _notifyEvent =
            notificationEventFactory_.newEvent( event, this );

	//logger_.debug("createdEvent");

        channelContext_.dispatchEvent( _notifyEvent );

	//logger_.debug("dispatchedEvent");
    }

    public void connect_push_supplier( org.omg.CosEventComm.PushSupplier pushSupplier )
    throws AlreadyConnected
    {
        connect_any_push_supplier( pushSupplier );
    }

    public void connect_any_push_supplier( org.omg.CosEventComm.PushSupplier pushSupplier )
    throws AlreadyConnected
    {

        logger_.info( "connect pushsupplier" );

        if ( connected )
        {
            throw new AlreadyConnected();
        }

        myPushSupplier = pushSupplier;
        connected = true;
    }

    public SupplierAdmin MyAdmin()
    {
        return ( SupplierAdmin ) myAdmin_.getThisRef();
    }

    public List getSubsequentFilterStages()
    {
        return subsequentDestinations_;
    }

    public EventConsumer getEventConsumer()
    {
        return null;
    }

    public boolean hasEventConsumer()
    {
        return false;
    }

    public void dispose()
    {
        super.dispose();
        disconnectClient();
    }

    public Servant getServant()
    {
        if ( thisServant_ == null )
        {
            synchronized ( this )
            {
                if ( thisServant_ == null )
                {
                    thisServant_ = new ProxyPushConsumerPOATie( this );
                }
            }
        }

        return thisServant_;
    }

    public void setServant( Servant servant )
    {
        thisServant_ = servant;
    }
}
