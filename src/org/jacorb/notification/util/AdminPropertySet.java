package org.jacorb.notification.util;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configuration;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.conf.Default;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.MaxConsumers;
import org.omg.CosNotification.MaxQueueLength;
import org.omg.CosNotification.MaxSuppliers;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.PropertyError;
import org.omg.CosNotification.RejectNewEvents;
import org.omg.CosNotification.UnsupportedAdmin;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class AdminPropertySet
    extends PropertySet {

    private final static Set sAdminPropertyNames_;

    private final static ORB sOrb_ = ORB.init();

    private final Property[] defaultProperties_;

    static {
        HashSet _adminProps = new HashSet();

        _adminProps.add(MaxQueueLength.value);
        _adminProps.add(MaxConsumers.value);
        _adminProps.add(MaxSuppliers.value);
        _adminProps.add(RejectNewEvents.value);
        
        sAdminPropertyNames_ = Collections.unmodifiableSet(_adminProps);
    }

    ////////////////////////////////////////

    private final Set validNames_ = sAdminPropertyNames_;

    ////////////////////////////////////////

    public AdminPropertySet(Configuration config)
    {
        super();
    
        int _maxConsumersDefault =
            config.getAttributeAsInteger(Attributes.MAX_NUMBER_CONSUMERS,
                                       Default.DEFAULT_MAX_NUMBER_CONSUMERS);

        Any _maxConsumersDefaultAny = sOrb_.create_any();
        _maxConsumersDefaultAny.insert_long( _maxConsumersDefault );

        //////////////////////////////

        int _maxSuppliersDefault =
            config.getAttributeAsInteger(Attributes.MAX_NUMBER_SUPPLIERS,
                                       Default.DEFAULT_MAX_NUMBER_SUPPLIERS);

        Any _maxSuppliersDefaultAny = sOrb_.create_any();
        _maxSuppliersDefaultAny.insert_long(_maxSuppliersDefault);

        //////////////////////////////

        int _maxQueueLength =
            config.getAttributeAsInteger(Attributes.MAX_QUEUE_LENGTH,
                                       Default.DEFAULT_MAX_QUEUE_LENGTH);

        Any _maxQueueLengthAny = sOrb_.create_any();
        _maxQueueLengthAny.insert_long(_maxQueueLength);

        //////////////////////////////

        boolean _rejectNewEvents =
            config.getAttribute(Attributes.REJECT_NEW_EVENTS,
                              Default.DEFAULT_REJECT_NEW_EVENTS).equals("on");

        Any _rejectNewEventsAny = sOrb_.create_any();
        _rejectNewEventsAny.insert_boolean(_rejectNewEvents);

        //////////////////////////////

        defaultProperties_ = new Property[] {
            new Property(MaxConsumers.value, _maxConsumersDefaultAny),
            new Property(MaxSuppliers.value, _maxSuppliersDefaultAny),
            new Property(MaxQueueLength.value, _maxQueueLengthAny),
            new Property(RejectNewEvents.value, _rejectNewEventsAny)
        };

        set_admin(defaultProperties_);
    }


    public Set getValidNames()
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
        List _errors = new ArrayList();

        checkPropertyExistence(ps, _errors);

        if (!_errors.isEmpty())
            {
                throw new UnsupportedAdmin((PropertyError[])_errors.toArray(PROPERTY_ERROR_ARRAY_TEMPLATE));
            }
    }
}
