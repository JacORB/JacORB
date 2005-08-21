/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

package org.jacorb.notification.jmx;

import javax.management.Attribute;
import javax.management.AttributeChangeNotification;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ReflectionException;
import javax.management.StandardMBean;

import org.jacorb.notification.interfaces.JMXManageable;
import org.jacorb.util.ObjectUtil;

public class BroadcastSupportMBeanDecorator implements DynamicMBean, NotificationEmitter
{
    private final NotificationBroadcasterSupport broadCaster_ = new NotificationBroadcasterSupport();

    private final DynamicMBean delegate_;

    private final String[] types_;

    private int notificationSequence_ = 0;

    public BroadcastSupportMBeanDecorator(JMXManageable manageable)
            throws NotCompliantMBeanException, ClassNotFoundException
    {
        super();
        
        types_ = manageable.getJMXNotificationTypes();

        delegate_ = new StandardMBean(manageable, getManagementInterface(manageable.getClass()));

        manageable.setJMXCallback(new JMXManageable.JMXCallback()
        {
            public void sendJMXNotification(String type, String message)
            {
                sendNotification(type, message);
            }

            public void sendJMXAttributeChanged(String name, Object oldValue, Object newValue)
            {
                sendAttributeChanged(name, oldValue, newValue);
            }
        });
    }

    private Class getManagementInterface(Class clazz) throws IllegalArgumentException,
            ClassNotFoundException
    {
        String managementInterfaceName = clazz.getName() + "MBean";

        if (clazz.getClassLoader() != null)
        {
            return clazz.getClassLoader().loadClass(managementInterfaceName);
        }
        
        return ObjectUtil.classForName(managementInterfaceName);
    }

    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException,
            ReflectionException
    {
        return delegate_.getAttribute(attribute);
    }

    public AttributeList getAttributes(String[] attributes)
    {
        return delegate_.getAttributes(attributes);
    }

    public MBeanInfo getMBeanInfo()
    {
        MBeanInfo _info = delegate_.getMBeanInfo();

        if (types_ != null && types_.length > 0)
        {
            return new MBeanInfo(_info.getClassName(), _info.getDescription(), _info
                    .getAttributes(), _info.getConstructors(), _info.getOperations(),
                    getNotificationInfo());
        }

        return _info;
    }

    public Object invoke(String method, Object[] arguments, String[] params) throws MBeanException,
            ReflectionException
    {
        return delegate_.invoke(method, arguments, params);
    }

    public void setAttribute(Attribute attribute) throws AttributeNotFoundException,
            InvalidAttributeValueException, MBeanException, ReflectionException
    {
        delegate_.setAttribute(attribute);
    }

    public AttributeList setAttributes(AttributeList attributes)
    {
        return delegate_.setAttributes(attributes);
    }

    public void addNotificationListener(NotificationListener listener, NotificationFilter filter,
            Object handback) throws IllegalArgumentException
    {
        broadCaster_.addNotificationListener(listener, filter, handback);
    }

    public void removeNotificationListener(NotificationListener listener)
            throws ListenerNotFoundException
    {
        broadCaster_.removeNotificationListener(listener);
    }

    public MBeanNotificationInfo[] getNotificationInfo()
    {
        return new MBeanNotificationInfo[] {
                new MBeanNotificationInfo(types_, 
                        Notification.class.getName(),
                        "User Notifications"),
                new MBeanNotificationInfo(new String[] { AttributeChangeNotification.ATTRIBUTE_CHANGE },
                        AttributeChangeNotification.class.getName(),
                        "User attribute change notification") };
    }

    public void sendNotification(String type, String message)
    {
        broadCaster_.sendNotification(new Notification(type,// type
                this, // source
                ++notificationSequence_, // seq. number
                message // message
                ));
    }

    public void removeNotificationListener(NotificationListener listener,
            NotificationFilter filter, Object handback) throws ListenerNotFoundException
    {
        broadCaster_.removeNotificationListener(listener, filter, handback);
    }

    public void sendAttributeChanged(String name, Object oldValue, Object newValue)
    {
        broadCaster_.sendNotification(new AttributeChangeNotification(delegate_,
                ++notificationSequence_, 
                System.currentTimeMillis(), 
                "Attribute value changed",
                name, 
                oldValue == null ? null : oldValue.getClass().getName(), 
                oldValue, 
                newValue));
    }
}