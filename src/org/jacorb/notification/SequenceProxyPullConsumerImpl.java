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

import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPullConsumerOperations;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPullConsumerPOATie;
import org.omg.CosNotifyComm.SequencePullSupplier;
import org.omg.PortableServer.Servant;

import org.jacorb.notification.interfaces.Message;

/**
 * SequenceProxyPullConsumerImpl.java
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class SequenceProxyPullConsumerImpl
            extends StructuredProxyPullConsumerImpl
            implements SequenceProxyPullConsumerOperations
{

    private SequencePullSupplier sequencePullSupplier_;

    public SequenceProxyPullConsumerImpl( SupplierAdminTieImpl supplierAdminServant,
                                          ApplicationContext appContext,
                                          ChannelContext channelContext,
                                          PropertyManager adminProperties,
                                          PropertyManager qosProperties,
                                          Integer key )
    {
        super( supplierAdminServant,
               appContext,
               channelContext,
               adminProperties,
               qosProperties,
               key );

        setProxyType( ProxyType.PULL_SEQUENCE );
    }

    public void disconnect_sequence_pull_consumer()
    {
        dispose();
        stopTask();
    }

    public void connect_sequence_pull_supplier( SequencePullSupplier sequencePullSupplier )
        throws AlreadyConnected
    {

        if ( connected_ )
        {
            throw new AlreadyConnected();
        }

        connected_ = true;
        active_ = true;

        sequencePullSupplier_ = sequencePullSupplier;
        startTask();
    }

    /**
     * override superclass impl
     */
    public void runPullEvent()
    {
        runPullSequenceFromSupplier();
    }

    private void runPullSequenceFromSupplier()
    {
        BooleanHolder _hasEvent = new BooleanHolder();
        StructuredEvent[] _events = null;

        synchronized ( this )
        {
            if ( connected_ && active_ )
            {
                try
                {
                    _hasEvent.value = false;
                    _events = sequencePullSupplier_.try_pull_structured_events( 1, _hasEvent );
                }
                catch ( UserException e )
                {
                    connected_ = false;
                    return ;
                }
                catch ( SystemException e )
                {
                    connected_ = false;
                    return ;
                }

                if ( _hasEvent.value )
                {
                    for ( int x = 0; x < _events.length; ++x )
                    {
                        Message _notifyEvent =
                            notificationEventFactory_.newEvent( _events[ x ], this );

                        channelContext_.dispatchEvent( _notifyEvent );
                    }
                }
            }
        }
    }

    protected void disconnectClient()
    {
        if ( connected_ )
        {
            if ( sequencePullSupplier_ != null )
            {
                sequencePullSupplier_.disconnect_sequence_pull_supplier();
                sequencePullSupplier_ = null;
            }
        }

        connected_ = false;
    }

    public void dispose()
    {
        super.dispose();
    }

    public synchronized Servant getServant()
    {
        if ( thisServant_ == null )
                {
                    thisServant_ = new SequenceProxyPullConsumerPOATie( this );
                }

        return thisServant_;
    }

}
