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

import java.util.Collections;
import java.util.List;

import org.jacorb.notification.interfaces.EventConsumer;
import org.jacorb.notification.interfaces.Message;

import org.omg.CORBA.BooleanHolder;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPullSupplierOperations;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPullSupplierPOATie;
import org.omg.CosNotifyComm.SequencePullConsumer;
import org.omg.PortableServer.Servant;

/**
 * SequenceProxyPullSupplierImpl.java
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class SequenceProxyPullSupplierImpl
    extends StructuredProxyPullSupplierImpl
    implements SequenceProxyPullSupplierOperations,
               EventConsumer
{

    private SequencePullConsumer sequencePullConsumer_;
    private static final StructuredEvent[] sUndefinedSequence;

    static {
        sUndefinedSequence = new StructuredEvent[] {undefinedStructuredEvent_};
    }

    public SequenceProxyPullSupplierImpl( ConsumerAdminTieImpl myAdminServant,
                                          ApplicationContext appContext,
                                          ChannelContext channelContext,
                                          PropertyManager adminProperties,
                                          PropertyManager qosProperties,
                                          Integer key ) throws UnsupportedQoS
    {

        super( myAdminServant,
               appContext,
               channelContext,
               adminProperties,
               qosProperties,
               key );

        setProxyType( ProxyType.PULL_STRUCTURED );
    }

    public void connect_sequence_pull_consumer( SequencePullConsumer consumer )
        throws AlreadyConnected
    {
        if ( connected_ )
        {
            throw new AlreadyConnected();
        }

        connected_ = true;
        sequencePullConsumer_ = consumer;
    }

    public StructuredEvent[] pull_structured_events( int number ) throws Disconnected
    {
        StructuredEvent[] _event = null;
        BooleanHolder _hasEvent = new BooleanHolder();
        StructuredEvent _ret[] = sUndefinedSequence;

        Message[] _events = getUpToMessages(number);
        _ret = new StructuredEvent[_events.length];

        for (int x=0; x<_events.length; ++x) {
            _ret[x] = _events[x].toStructuredEvent();
            _events[x].dispose();
        }

        return _ret;
    }

    public StructuredEvent[] try_pull_structured_events( int number,
                                                         BooleanHolder success )
        throws Disconnected
    {
        Message[] _events = getUpToMessages(number);
        if (_events != null) {
            StructuredEvent[] _ret = new StructuredEvent[_events.length];

            for (int x=0; x<_events.length; ++x) {
                _ret[x] = _events[x].toStructuredEvent();

                _events[x].dispose();
            }
            success.value = true;
            return _ret;
        }
        success.value = false;
        return sUndefinedSequence;
    }


    public List getSubsequentFilterStages()
    {
        return CollectionsWrapper.singletonList( this );
    }

    public EventConsumer getEventConsumer()
    {
        return this;
    }

    public boolean hasEventConsumer()
    {
        return true;
    }

    public void dispose()
    {
        super.dispose();
        disconnectClient();
    }

    public void markError()
    {
        connected_ = false;
    }

    private void disconnectClient()
    {
        if ( connected_ )
        {
            if ( sequencePullConsumer_ != null )
            {
                sequencePullConsumer_.disconnect_sequence_pull_consumer();
                connected_ = false;
                sequencePullConsumer_ = null;
            }
        }
    }

    public ConsumerAdmin MyAdmin()
    {
        return ( ConsumerAdmin ) myAdmin_.getThisRef();
    }

    public void disconnect_sequence_pull_supplier()
    {
        dispose();
    }

    public synchronized Servant getServant()
    {
        if ( thisServant_ == null )
            {
                thisServant_ = new SequenceProxyPullSupplierPOATie( this );
            }

        return thisServant_;
    }
}
