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

import java.util.List;

import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageConsumer;

import org.omg.CORBA.Any;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotifyChannelAdmin.ProxyConsumerHelper;
import org.omg.CosNotifyChannelAdmin.ProxyPushConsumerOperations;
import org.omg.CosNotifyChannelAdmin.ProxyPushConsumerPOATie;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.PortableServer.Servant;
import org.jacorb.notification.ChannelContext;
import org.jacorb.notification.PropertyManager;
import org.jacorb.notification.CollectionsWrapper;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ProxyPushConsumerImpl
            extends AbstractProxy
            implements ProxyPushConsumerOperations
{

    private org.omg.CosEventComm.PushSupplier myPushSupplier;

    private List subsequentDestinations_;

    ////////////////////////////////////////

    ProxyPushConsumerImpl( AbstractAdmin myAdminServant,
                           ChannelContext channelContext,
                           PropertyManager adminProperties,
                           PropertyManager qosProperties )
    {
        super( myAdminServant,
               channelContext,
               adminProperties,
               qosProperties );

        init(myAdminServant);
    }

    ProxyPushConsumerImpl( AbstractAdmin myAdminServant,
                           ChannelContext channelContext,
                           PropertyManager adminProperties,
                           PropertyManager qosProperties,
                           Integer key )
    {

        super( myAdminServant,
               channelContext,
               adminProperties,
               qosProperties,
               key,
               true);

        init(myAdminServant);
    }

    ////////////////////////////////////////

    private void init(AbstractAdmin supplierAdminServant)
    {
        setProxyType( ProxyType.PUSH_ANY );
        subsequentDestinations_ = CollectionsWrapper.singletonList( supplierAdminServant );
    }


    public void disconnect_push_consumer()
    {
        logger_.info( "disconnect any_push_supplier" );

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
        checkConnected();

        Message _notifyEvent =
            messageFactory_.newMessage( event, this );

        getTaskProcessor().processMessage( _notifyEvent );
    }


    public void connect_any_push_supplier( org.omg.CosEventComm.PushSupplier pushSupplier )
        throws AlreadyConnected
    {
        logger_.info( "connect any_push_supplier" );

        if ( connected_ )
        {
            throw new AlreadyConnected();
        }

        myPushSupplier = pushSupplier;
        connected_ = true;
    }


    public SupplierAdmin MyAdmin()
    {
        return ( SupplierAdmin ) myAdmin_.getCorbaRef();
    }


    public List getSubsequentFilterStages()
    {
        return subsequentDestinations_;
    }


    public MessageConsumer getMessageConsumer()
    {
        throw new UnsupportedOperationException();
    }

    public boolean hasMessageConsumer()
    {
        return false;
    }


    public void dispose()
    {
        super.dispose();

        disconnectClient();
    }


    public synchronized Servant getServant()
    {
        if ( thisServant_ == null )
            {
                thisServant_ = new ProxyPushConsumerPOATie( this );
            }

        return thisServant_;
    }

    public org.omg.CORBA.Object getCorbaRef() {
        return ProxyConsumerHelper.narrow( getServant()._this_object(getORB()) );
    }
}
