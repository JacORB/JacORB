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

import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.ORB;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPullConsumer;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPullConsumerHelper;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPushConsumer;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPushConsumerHelper;
import org.omg.CosTypedNotifyChannelAdmin.TypedSupplierAdminOperations;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

import org.omg.CosTypedNotifyChannelAdmin.TypedSupplierAdminPOATie;
import org.omg.CosTypedNotifyChannelAdmin.TypedSupplierAdmin;
import org.omg.CosTypedNotifyChannelAdmin.TypedSupplierAdminHelper;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class TypedSupplierAdminImpl
    extends SupplierAdminImpl
    implements TypedSupplierAdminOperations {

    private TypedSupplierAdmin thisRef_;

    public synchronized Servant getServant() {
        if (thisServant_ == null) {
            thisServant_ = new TypedSupplierAdminPOATie(this);
        }
        return thisServant_;
    }

    public org.omg.CORBA.Object activate() {
        if (thisRef_ == null) {
            thisRef_ = TypedSupplierAdminHelper.narrow(getServant()._this_object(getORB()));
        }
        return thisRef_;
    }


    public TypedProxyPushConsumer obtain_typed_notification_push_consumer(String type, IntHolder id)
        throws AdminLimitExceeded {

        fireCreateProxyRequestEvent();

        try {
            TypedProxyPushConsumerImpl _servant = new TypedProxyPushConsumerImpl(type);

            configureManagers(_servant);

            configureNotifyStyleID(_servant);

            configureInterFilterGroupOperator(_servant);

            configureQoS(_servant);

            addProxyToMap(_servant, pushServants_, modifyProxiesLock_);

            id.value = _servant.getID().intValue();

            _servant.preActivate();

            return TypedProxyPushConsumerHelper.narrow(_servant.activate());
        } catch (Exception e) {
            throw new INTERNAL();
        }
    }


    public TypedProxyPullConsumer obtain_typed_notification_pull_consumer(String type, IntHolder id)
        throws AdminLimitExceeded {

        fireCreateProxyRequestEvent();

        try {
            TypedProxyPullConsumerImpl _servant = new TypedProxyPullConsumerImpl(type);

            configureManagers(_servant);

            configureNotifyStyleID(_servant);

            configureInterFilterGroupOperator(_servant);

            configureQoS(_servant);

            addProxyToMap(_servant, pullServants_, modifyProxiesLock_);

            id.value = _servant.getID().intValue();

            _servant.preActivate();

            return TypedProxyPullConsumerHelper.narrow(_servant.activate());
        } catch (Exception e) {
            throw new INTERNAL();
        }
    }


    public org.omg.CosTypedEventChannelAdmin.TypedProxyPushConsumer obtain_typed_push_consumer(String type) {
        throw new NO_IMPLEMENT();
    }


    public org.omg.CosEventChannelAdmin.ProxyPullConsumer obtain_typed_pull_consumer(String type) {
        throw new NO_IMPLEMENT();
    }
}
