package org.jacorb.notification.util;

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

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.MaxConsumers;
import org.omg.CosNotification.MaxQueueLength;
import org.omg.CosNotification.MaxSuppliers;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.PropertyError;
import org.omg.CosNotification.RejectNewEvents;
import org.omg.CosNotification.UnsupportedAdmin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.util.PropertySet;

import org.apache.avalon.framework.configuration.Configuration;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class AdminPropertySet
    extends PropertySet
{
    private static HashSet sAdminPropertyNames_;

    private static ORB sOrb_ = ORB.init();

    private static Property[] sDefaultProperties_;

    public static void initStatics (Configuration conf)
    {
        sAdminPropertyNames_ = new java.util.HashSet();

        sAdminPropertyNames_.add(MaxQueueLength.value);
        sAdminPropertyNames_.add(MaxConsumers.value);
        sAdminPropertyNames_.add(MaxSuppliers.value);
        sAdminPropertyNames_.add(RejectNewEvents.value);

        //////////////////////////////

        int _maxConsumersDefault =
            conf.getAttributeAsInteger(Attributes.MAX_NUMBER_CONSUMERS,
                                       Default.DEFAULT_MAX_NUMBER_CONSUMERS);

        Any _maxConsumersDefaultAny = sOrb_.create_any();
        _maxConsumersDefaultAny.insert_long( _maxConsumersDefault );

        //////////////////////////////

        int _maxSuppliersDefault =
            conf.getAttributeAsInteger(Attributes.MAX_NUMBER_SUPPLIERS,
                                       Default.DEFAULT_MAX_NUMBER_SUPPLIERS);

        Any _maxSuppliersDefaultAny = sOrb_.create_any();
        _maxSuppliersDefaultAny.insert_long(_maxSuppliersDefault);

        //////////////////////////////

        int _maxQueueLength =
            conf.getAttributeAsInteger(Attributes.MAX_QUEUE_LENGTH,
                                       Default.DEFAULT_MAX_QUEUE_LENGTH);

        Any _maxQueueLengthAny = sOrb_.create_any();
        _maxQueueLengthAny.insert_long(_maxQueueLength);

        //////////////////////////////

        boolean _rejectNewEvents =
            conf.getAttribute(Attributes.REJECT_NEW_EVENTS,
                              Default.DEFAULT_REJECT_NEW_EVENTS).equals("on");

        Any _rejectNewEventsAny = sOrb_.create_any();
        _rejectNewEventsAny.insert_boolean(_rejectNewEvents);

        //////////////////////////////

        sDefaultProperties_ = new Property[] {
            new Property(MaxConsumers.value, _maxConsumersDefaultAny),
            new Property(MaxSuppliers.value, _maxSuppliersDefaultAny),
            new Property(MaxQueueLength.value, _maxQueueLengthAny),
            new Property(RejectNewEvents.value, _rejectNewEventsAny)
        };
    }

    ////////////////////////////////////////

    private java.util.HashSet validNames_ = sAdminPropertyNames_;

    ////////////////////////////////////////

    public AdminPropertySet()
    {
        super();

        set_admin(sDefaultProperties_);
    }

    ////////////////////////////////////////

    public java.util.HashSet getValidNames()
    {
        return validNames_;
    }

    public void set_admin(Property[] ps)
    {
        set_properties(ps);
    }


    public Property[] get_admin()
    {
        return toArray();
    }


    public void validate_admin(Property[] ps) throws UnsupportedAdmin
    {
        java.util.List _errors = new java.util.ArrayList();

        checkPropertyExistence(ps, _errors);

        if (!_errors.isEmpty())
            {
                throw new UnsupportedAdmin((PropertyError[])_errors.toArray(PROPERTY_ERROR_ARRAY_TEMPLATE));
            }
    }
}
