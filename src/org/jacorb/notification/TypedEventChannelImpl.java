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

import org.jacorb.notification.servant.AbstractAdmin;
import org.jacorb.notification.servant.AbstractSupplierAdmin;
import org.jacorb.notification.servant.TypedConsumerAdminImpl;
import org.jacorb.notification.servant.TypedSupplierAdminImpl;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotifyChannelAdmin.AdminNotFound;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosTypedNotifyChannelAdmin.TypedConsumerAdmin;
import org.omg.CosTypedNotifyChannelAdmin.TypedConsumerAdminHelper;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannel;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelFactory;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelFactoryHelper;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelHelper;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelOperations;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelPOATie;
import org.omg.CosTypedNotifyChannelAdmin.TypedSupplierAdmin;
import org.omg.CosTypedNotifyChannelAdmin.TypedSupplierAdminHelper;
import org.omg.PortableServer.Servant;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class TypedEventChannelImpl
    extends AbstractEventChannel
    implements TypedEventChannelOperations
{
    private TypedEventChannel thisRef_;

    private TypedEventChannelFactory typedEventChannelFactory_;

    // Implementation of org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelOperations

    public TypedEventChannelFactory MyFactory() {
        return typedEventChannelFactory_;
    }


    public TypedConsumerAdmin default_consumer_admin() {
        return TypedConsumerAdminHelper.narrow(getDefaultConsumerAdminServant().activate());
    }


    public TypedSupplierAdmin default_supplier_admin() {
        return TypedSupplierAdminHelper.narrow(getDefaultSupplierAdminServant().activate());
    }


    public TypedConsumerAdmin new_for_typed_notification_consumers(InterFilterGroupOperator ifg,
                                                                   IntHolder intHolder) {
        AbstractAdmin _admin =
            new_for_consumers_servant(ifg, intHolder);

        return TypedConsumerAdminHelper.narrow(_admin.activate());
    }


    public TypedSupplierAdmin new_for_typed_notification_suppliers(InterFilterGroupOperator ifg,
                                                                   IntHolder intHolder) {
        AbstractAdmin _admin =
            new_for_suppliers_servant(ifg, intHolder);

        return TypedSupplierAdminHelper.narrow(_admin.activate());
    }


    public TypedConsumerAdmin get_consumeradmin(int n) throws AdminNotFound {
        return TypedConsumerAdminHelper.narrow(get_consumeradmin_internal(n).activate());
    }


    public TypedSupplierAdmin get_supplieradmin(int n) throws AdminNotFound {
        return TypedSupplierAdminHelper.narrow(get_supplieradmin_internal(n).activate());
    }


    public org.omg.CosTypedEventChannelAdmin.TypedConsumerAdmin for_consumers() {
        return org.omg.CosTypedEventChannelAdmin.TypedConsumerAdminHelper.narrow(default_consumer_admin());
    }


    public org.omg.CosTypedEventChannelAdmin.TypedSupplierAdmin for_suppliers() {
        return org.omg.CosTypedEventChannelAdmin.TypedSupplierAdminHelper.narrow(default_supplier_admin());
    }


    public Servant getServant() {
        if (thisServant_ == null) {
            thisServant_ = new TypedEventChannelPOATie(this);
        }
        return thisServant_;
    }


    public org.omg.CORBA.Object activate() {
        if (thisRef_ == null) {
            thisRef_ = TypedEventChannelHelper.narrow(getServant()._this_object(getORB()));
        }
        return thisRef_;
    }


    public AbstractSupplierAdmin newSupplierAdmin() {
        return new TypedSupplierAdminImpl();
    }


    public AbstractAdmin newConsumerAdmin() {
        return new TypedConsumerAdminImpl();
    }


    public void setEventChannelFactory(org.omg.CORBA.Object factory) {
        typedEventChannelFactory_ = TypedEventChannelFactoryHelper.narrow(factory);
    }
}
