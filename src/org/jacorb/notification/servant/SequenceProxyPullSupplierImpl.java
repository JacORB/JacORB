package org.jacorb.notification.servant;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

import org.omg.CORBA.BooleanHolder;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPullSupplierOperations;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPullSupplierPOATie;
import org.omg.CosNotifyComm.NotifyPublishHelper;
import org.omg.CosNotifyComm.NotifyPublishOperations;
import org.omg.CosNotifyComm.SequencePullConsumer;
import org.omg.PortableServer.Servant;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class SequenceProxyPullSupplierImpl
    extends StructuredProxyPullSupplierImpl
    implements SequenceProxyPullSupplierOperations
{
    private static final StructuredEvent[] sUndefinedSequence;

    ////////////////////////////////////////

    static {
        sUndefinedSequence = new StructuredEvent[] {undefinedStructuredEvent_};
    }

    ////////////////////////////////////////

    private SequencePullConsumer sequencePullConsumer_;

    ////////////////////////////////////////

    public SequenceProxyPullSupplierImpl( AbstractAdmin myAdminServant,
                                          ChannelContext channelContext)
    {
        super( myAdminServant,
               channelContext );
    }

    ////////////////////////////////////////

    public ProxyType MyType() {
        return ProxyType.PULL_SEQUENCE;
    }


    public void connect_sequence_pull_consumer( SequencePullConsumer consumer )
        throws AlreadyConnected
    {
        assertNotConnected();

        connectClient(consumer);

        sequencePullConsumer_ = consumer;

        logger_.info("connect sequence_pull_consumer");
    }


    public StructuredEvent[] pull_structured_events( int number )
        throws Disconnected
    {
        checkStillConnected();

        StructuredEvent _structuredEvents[] = sUndefinedSequence;

        Message[] _messages = getUpToMessages(number);

        if (_messages != null) {
            _structuredEvents = new StructuredEvent[_messages.length];

            for (int x = 0; x < _messages.length; ++x)
                {
                    _structuredEvents[x] = _messages[x].toStructuredEvent();
                    _messages[x].dispose();
                }
        }

        return _structuredEvents;
    }


    public StructuredEvent[] try_pull_structured_events( int number,
                                                         BooleanHolder success )
        throws Disconnected
    {
        checkStillConnected();

        Message[] _messages = getUpToMessages(number);

        if (_messages != null)
        {
            StructuredEvent[] _ret = new StructuredEvent[_messages.length];

            for (int x = 0; x < _messages.length; ++x)
            {
                _ret[x] = _messages[x].toStructuredEvent();

                _messages[x].dispose();
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


    public MessageConsumer getMessageConsumer()
    {
        return this;
    }


    public boolean hasMessageConsumer()
    {
        return true;
    }


    protected void disconnectClient()
    {
        sequencePullConsumer_.disconnect_sequence_pull_consumer();

        sequencePullConsumer_ = null;

        logger_.info("disconnect sequence_pull_consumer");
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
