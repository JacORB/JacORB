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

import java.util.Collections;
import java.util.List;

import org.jacorb.notification.interfaces.MessageConsumer;
import org.jacorb.notification.interfaces.Message;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushConsumerOperations;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushConsumerPOATie;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyComm.StructuredPushSupplier;
import org.omg.PortableServer.Servant;

import org.jacorb.notification.*;
import org.omg.CosNotifyChannelAdmin.ProxyConsumerHelper;

/**
 * StructuredProxyPushConsumerImpl.java
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class StructuredProxyPushConsumerImpl
    extends AbstractProxy
    implements StructuredProxyPushConsumerOperations {

    private StructuredPushSupplier myPushSupplier_;
    private List subsequentDestinations_;

    ////////////////////////////////////////

    public StructuredProxyPushConsumerImpl(AbstractAdmin supplierAdminServant,
                                           ChannelContext channelContext,
                                           PropertyManager adminProperties,
                                           PropertyManager qosProperties,
                                           Integer key) {
        super(supplierAdminServant,
              channelContext,
              adminProperties,
              qosProperties,
              key,
              true);

        setProxyType(ProxyType.PUSH_STRUCTURED);

        subsequentDestinations_ = CollectionsWrapper.singletonList(myAdmin_);
    }

    ////////////////////////////////////////

    public void push_structured_event(StructuredEvent structuredEvent) throws Disconnected {

        checkConnected();

        Message _notifyEvent =
            messageFactory_.newMessage(structuredEvent, this);

        getTaskProcessor().processMessage(_notifyEvent);
    }

    public void disconnect_structured_push_consumer() {
        dispose();
    }

    protected void disconnectClient() {
        if (connected_) {
            if (myPushSupplier_ != null) {
                connected_ = false;
                myPushSupplier_.disconnect_structured_push_supplier();
                myPushSupplier_ = null;
            }
        }
    }

    public void connect_structured_push_supplier(StructuredPushSupplier structuredPushSupplier)
        throws AlreadyConnected {

        if (connected_) {
            throw new AlreadyConnected();
        }
        connected_ = true;
        myPushSupplier_ = structuredPushSupplier;
    }


    // Implementation of org.omg.CosNotifyChannelAdmin.ProxyConsumerOperations


    /**
     * Describe <code>MyAdmin</code> method here.
     *
     * @return a <code>SupplierAdmin</code> value
     */
    public SupplierAdmin MyAdmin() {
        return (SupplierAdmin)myAdmin_.getCorbaRef();
    }

    public MessageConsumer getMessageConsumer() {
        throw new UnsupportedOperationException();
    }

    public boolean hasMessageConsumer() {
        return false;
    }

    public List getSubsequentFilterStages() {
        return subsequentDestinations_;
    }

    public void dispose() {
        super.dispose();
        disconnectClient();
    }

    public synchronized Servant getServant() {
        if (thisServant_ == null) {
            thisServant_ = new StructuredProxyPushConsumerPOATie(this);
        }
        return thisServant_;
    }

    public org.omg.CORBA.Object getCorbaRef() {
        return ProxyConsumerHelper.narrow( getServant()._this_object(getORB()) );
    }


}
