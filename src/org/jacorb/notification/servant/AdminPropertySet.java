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

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class AdminPropertySet extends PropertySet
{
    static HashSet sAdminPropertyNames_;

    static {
        sAdminPropertyNames_ = new java.util.HashSet();

        sAdminPropertyNames_.add(MaxQueueLength.value);
        sAdminPropertyNames_.add(MaxConsumers.value);
        sAdminPropertyNames_.add(MaxSuppliers.value);
        sAdminPropertyNames_.add(RejectNewEvents.value);
    }

    ////////////////////////////////////////

    private HashSet validNames_ = sAdminPropertyNames_;

    ////////////////////////////////////////

    public AdminPropertySet(Property[] props)
    {
        super(props);
    }

    public AdminPropertySet()
    {
        super();
    }

    ////////////////////////////////////////

    public java.util.HashSet getValidNames()
    {
        return (validNames_);
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
                throw new UnsupportedAdmin((PropertyError[])_errors.toArray(PropertySet.PROPERTY_ERROR_ARRAY_TEMPLATE));
            }
    }
}

