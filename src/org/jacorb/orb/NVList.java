/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

package org.jacorb.orb;

/**
 * @author Gerald Brose, FU Berlin
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.omg.CORBA.NamedValue;

public class NVList
    extends org.omg.CORBA.NVList
{
    private final List<NamedValue> list;
    private final org.omg.CORBA.ORB orb;

    NVList(org.omg.CORBA.ORB orb, int count)
    {
        super();
        this.orb = orb;
        this.list = Collections.synchronizedList(new ArrayList<NamedValue>(count));
    }

    public int count()
    {
        return list.size();
    }

    public org.omg.CORBA.NamedValue add( int item_flags)
    {
        org.omg.CORBA.NamedValue namedValue = orb.create_named_value("", null, item_flags);
        list.add(namedValue);
        return namedValue;
    }

    public org.omg.CORBA.NamedValue add_item(String item_name, int item_flags)
    {
        org.omg.CORBA.NamedValue namedValue = orb.create_named_value(item_name, null, item_flags);
        list.add(namedValue);
        return namedValue;
    }

    public org.omg.CORBA.NamedValue add_value(java.lang.String item_name,
                              org.omg.CORBA.Any value,
                          int item_flags )
    {
        org.omg.CORBA.NamedValue namedValue = orb.create_named_value(item_name, value, item_flags);
        list.add(namedValue);
        return namedValue;
    }

    public org.omg.CORBA.NamedValue item(int index) throws org.omg.CORBA.Bounds
    {
        try
        {
            return list.get(index);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new org.omg.CORBA.Bounds(e.toString());
        }
    }

    public void remove(int index) throws org.omg.CORBA.Bounds
    {
        try
        {
            list.remove(index);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new org.omg.CORBA.Bounds(e.toString());
        }
    }

    public Iterator<NamedValue> iterator()
    {
        return list.iterator();
    }
}
