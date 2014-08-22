package org.jacorb.notification.util;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.PropertyError;
import org.omg.CosNotification.PropertyRange;
import org.omg.CosNotification.QoSError_code;
import org.slf4j.Logger;

/**
 * @author Alphonse Bendt
 */

public abstract class PropertySet
{
    protected static final ORB sORB = org.omg.CORBA.ORBSingleton.init();

    protected static final PropertyError[] PROPERTY_ERROR_ARRAY_TEMPLATE = new PropertyError[0];

    protected static final PropertyRange EMPTY_PROPERTY_RANGE = new PropertyRange(
            sORB.create_any(), sORB.create_any());

    ////////////////////////////////////////

    protected final Logger logger_ = LogUtil.getLogger(getClass().getName());

    private final HashMap<String, List<PropertySetListener>> listeners_ = new HashMap<String, List<PropertySetListener>>();

    private boolean modified_ = true;

    private final Map<String, Any> properties_ = new HashMap<String, Any>();

    private Property[] arrayView_ = null;

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
        final List<PropertySetListener> _list;

        if (!listeners_.containsKey(property))
        {
            _list = new ArrayList<PropertySetListener>();
            listeners_.put(property, _list);
        }
        else
        {
            _list = (List<PropertySetListener>) listeners_.get(property);
        }

        _list.add(listener);
    }

    public synchronized Property[] toArray()
    {
        if (arrayView_ == null || modified_)
        {
            Property[] _props = new Property[properties_.size()];

            Iterator<String> i = properties_.keySet().iterator();
            int x = 0;
            while (i.hasNext())
            {
                String _key = (String) i.next();
                _props[x++] = new Property(_key, (Any) properties_.get(_key));
            }
            arrayView_ = _props;
            modified_ = false;
        }

        return arrayView_;
    }

    public Map<String, Any> toMap()
    {
        return Collections.unmodifiableMap(properties_);
    }

    public String toString()
    {
        return properties_.toString();
    }

    public boolean containsKey(String propertyName)
    {
        return properties_.containsKey(propertyName);
    }

    public Any get(String propertyName)
    {
        return (Any) properties_.get(propertyName);
    }

    protected void set_properties(Property[] props)
    {
        final HashSet<PropertySetListener> _toBeNotified = new HashSet<PropertySetListener>();

        for (int x = 0; x < props.length; ++x)
        {
            Any _oldValue = null;

            if (properties_.containsKey(props[x].name))
            {
                _oldValue = (Any) properties_.get(props[x].name);
            }

            properties_.put(props[x].name, props[x].value);

            if (listeners_.containsKey(props[x].name))
            {
                if (!props[x].value.equals(_oldValue))
                {
                    _toBeNotified.addAll(listeners_.get(props[x].name));
                }
            }
        }

        synchronized (this)
        {
            modified_ = true;
        }

        Iterator<?> i = _toBeNotified.iterator();
        while (i.hasNext())
        {
            try
            {
                ((PropertySetListener) i.next()).actionPropertySetChanged(this);
            } catch (Exception e)
            {
                logger_.error("exception in listener", e);
            }
        }
    }

    abstract Set<?> getValidNames();

    protected void checkPropertyExistence(Property[] props, List<PropertyError> errorList)
    {
        for (int x = 0; x < props.length; ++x)
        {
            if (!getValidNames().contains(props[x].name))
            {
                errorList.add(badProperty(props[x].name));
            }
        }
    }

    protected PropertyError badProperty(String name)
    {
        return new PropertyError(QoSError_code.BAD_PROPERTY, name, EMPTY_PROPERTY_RANGE);
    }

    protected PropertyError badType(String name)
    {
        return new PropertyError(QoSError_code.BAD_TYPE, name, EMPTY_PROPERTY_RANGE);
    }

    ////////////////////////////////////////

    public static Property[] map2Props(Map<?, ?> props)
    {
        Property[] _ps = new Property[props.size()];

        Iterator<?> i = props.keySet().iterator();
        int x = 0;
        while (i.hasNext())
        {
            String _key = (String) i.next();
            _ps[x++] = new Property(_key, (Any) props.get(_key));
        }
        return _ps;
    }
}