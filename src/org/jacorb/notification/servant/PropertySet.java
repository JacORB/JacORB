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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.PropertyError;
import org.omg.CosNotification.PropertyRange;
import org.omg.CosNotification.QoSError_code;

import org.jacorb.util.Debug;

import org.apache.avalon.framework.logger.Logger;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class PropertySet
{
    static ORB orb_ = ORB.init();

    static final PropertyError[] PROPERTY_ERROR_ARRAY_TEMPLATE =
        new PropertyError[0];

    static final PropertyRange EMPTY_PROPERTY_RANGE =
        new PropertyRange(orb_.create_any(), orb_.create_any());

    ////////////////////////////////////////

    private Logger logger_ = Debug.getNamedLogger(getClass().getName());

    private Map listeners_ = new HashMap();

    private boolean modified_ = true;

    private Map properties_ = new HashMap();

    private Property[] arrayView_ = null;

    ////////////////////////////////////////

    protected PropertySet()
    {}

    protected PropertySet(Property[] ps)
    {
        properties_ = getUniqueSet(ps);
    }

    ////////////////////////////////////////

    public void addPropertySetListener(String[] props, PropertySetListener listener)
    {
        for (int x = 0; x < props.length; ++x)
            {
                addPropertySetListener(props[x], listener);
            }
    }


    public void addPropertySetListener(String property, PropertySetListener listener)
    {
        List _list;

        if (!listeners_.containsKey(property))
            {
                _list = new ArrayList();
                listeners_.put(property, _list);
            }
        else
            {
                _list = (List)listeners_.get(property);
            }

        _list.add(listener);
    }


    public Property[] toArray()
    {
        if (arrayView_ == null || modified_)
            {
                Property[] _ps = new Property[properties_.size()];

                Iterator i = properties_.keySet().iterator();
                int x = 0;
                while (i.hasNext())
                    {
                        String _key = (String)i.next();
                        _ps[x++] = new Property(_key, (Any)properties_.get(_key));
                    }
                arrayView_ = _ps;
                modified_ = false;
            }
        return arrayView_;
    }


    public String toString()
    {
        return properties_.toString();
    }


    public boolean containsKey(String key)
    {
        return properties_.containsKey(key);
    }


    public Any get
        (String key)
    {
        return (Any)properties_.get(key);
    }


    protected void set_properties(Property[] props)
    {
        HashSet _toBeNotified = new HashSet();

        for (int x = 0; x < props.length; ++x)
            {
                Any _oldValue = null;

                if (properties_.containsKey(props[x].name))
                    {
                        _oldValue = (Any)properties_.get(props[x].name);
                    }

                properties_.put(props[x].name, props[x].value);

                logger_.debug("set " + props[x].name + " => " + props[x].value);

                if (listeners_.containsKey(props[x].name))
                    {
                        if (!props[x].value.equals(_oldValue))
                            {
                                _toBeNotified.addAll((List)listeners_.get(props[x].name));
                            }
                    }
            }

        modified_ = true;

        Iterator i = _toBeNotified.iterator();
        while (i.hasNext())
            {
                try
                    {
                        ((PropertySetListener)i.next()).actionPropertySetChanged(this);
                    }
                catch (Exception e)
                    {
                        logger_.error("exception in listener", e);
                    }
            }
    }

    abstract HashSet getValidNames();

    protected void checkPropertyExistence(Property[] p, List errorList)
    {
        for (int x = 0; x < p.length; ++x)
            {
                if (!getValidNames().contains(p[x].name))
                    {
                        errorList.add(new PropertyError(QoSError_code.BAD_PROPERTY,
                                                        p[x].name,
                                                        new PropertyRange(orb_.create_any(), orb_.create_any())));
                    }
            }
    }


    protected PropertyError badType(String name) {
        return new PropertyError(QoSError_code.BAD_TYPE,
                                 name,
                                 EMPTY_PROPERTY_RANGE);
    }


    ////////////////////////////////////////

    private static Map getUniqueSet(Property[] p)
    {
        Map _map = new HashMap();

        for (int x = 0; x < p.length; ++x)
            {
                _map.put(p[x].name, p[x].value);
            }

        return _map;
    }
}
