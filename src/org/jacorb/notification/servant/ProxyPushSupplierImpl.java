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

import org.jacorb.notification.ChannelContext;
import org.jacorb.notification.CollectionsWrapper;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageConsumer;

import org.omg.CORBA.BAD_PARAM;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosEventComm.PushConsumer;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyActive;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyInactive;
import org.omg.CosNotifyChannelAdmin.NotConnected;
import org.omg.CosNotifyChannelAdmin.ProxyPushSupplierHelper;
import org.omg.CosNotifyChannelAdmin.ProxyPushSupplierOperations;
import org.omg.CosNotifyChannelAdmin.ProxyPushSupplierPOATie;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyComm.NotifyPublishHelper;
import org.omg.CosNotifyComm.NotifyPublishOperations;
import org.omg.PortableServer.Servant;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ProxyPushSupplierImpl
    extends AbstractProxySupplier
    implements ProxyPushSupplierOperations
{
    private PushConsumer pushConsumer_;

    private NotifyPublishOperations offerListener_;

    private boolean enabled_;

    private boolean active_;

    ////////////////////////////////////////

    ProxyPushSupplierImpl(AbstractAdmin myAdminServant,
                          ChannelContext channelContext)
    {
        super(myAdminServant,
              channelContext);

        setProxyType(ProxyType.PUSH_ANY);

        enabled_ = true;
    }

    ////////////////////////////////////////

    public void disconnect_push_supplier()
    {
        dispose();
    }


    protected void disconnectClient()
    {
        pushConsumer_.disconnect_push_consumer();
        pushConsumer_ = null;
    }


    public void deliverMessage(Message event) throws Disconnected
    {
        if (isConnected())
        {
            if (active_ && enabled_)
                {
                    pushConsumer_.push(event.toAny());

                    event.dispose();
                }
            else
                {
                    enqueue(event);
                }
        }
        else
        {
            logger_.debug("Not connected");
        }
    }


    public void connect_any_push_consumer(PushConsumer pushConsumer)
        throws AlreadyConnected
    {
        assertNotConnected();

        pushConsumer_ = pushConsumer;

        connectClient(pushConsumer);

        active_ = true;

        try {
            offerListener_ = NotifyPublishHelper.narrow(pushConsumer);
        } catch (Throwable t) {}
    }


    public List getSubsequentFilterStages()
    {
        return CollectionsWrapper.singletonList(this);
    }


    public MessageConsumer getMessageConsumer()
    {
        return this;
    }


    public boolean hasMessageConsumer()
    {
        return true;
    }


    synchronized public void suspend_connection()
        throws NotConnected,
               ConnectionAlreadyInactive
    {

        assertConnected();

        if (!active_)
        {
            throw new ConnectionAlreadyInactive();
        }

        active_ = false;
    }


    public void deliverPendingMessages()
        throws Disconnected
    {
        Message[] _events = getAllMessages();

        try {
            for (int x = 0; x < _events.length; ++x)
                {
                    pushConsumer_.push(_events[x].toAny());
                }
        }
        finally {
            for (int x=0; x<_events.length; ++x) {
                _events[x].dispose();
            }
        }
    }



    public void resume_connection()
        throws NotConnected,
               ConnectionAlreadyActive
    {
        assertConnected();

        synchronized (this)
        {
            if (active_)
            {
                throw new ConnectionAlreadyActive();
            }

            active_ = true;
        }

        try {
            deliverPendingMessages();
        } catch (Disconnected e) {
            logger_.fatalError("illegal state: PushConsumer think it's disconnected. ProxyPushSupplier think it's connected", e);

            dispose();
        }
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
        if (thisServant_ == null)
        {
            thisServant_ = new ProxyPushSupplierPOATie(this);
        }
        return thisServant_;
    }


    public org.omg.CORBA.Object activate()
    {
        return ProxyPushSupplierHelper.narrow( getServant()._this_object(getORB()) );
    }


    NotifyPublishOperations getOfferListener() {
        return offerListener_;
    }
}
