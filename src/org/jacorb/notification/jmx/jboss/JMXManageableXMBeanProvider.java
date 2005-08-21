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

package org.jacorb.notification.jmx.jboss;

import java.net.URL;

import javax.management.AttributeChangeNotification;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanNotificationInfo;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.jacorb.notification.interfaces.JMXManageable;
import org.jacorb.notification.jmx.JMXManageableMBeanProvider;
import org.jboss.mx.modelmbean.XMBean;
import org.nanocontainer.remoting.jmx.DynamicMBeanProvider;
import org.nanocontainer.remoting.jmx.JMXRegistrationException;
import org.nanocontainer.remoting.jmx.JMXRegistrationInfo;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;

/**
 * JBoss specific DynamicMBeanProvider.
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */
public class JMXManageableXMBeanProvider implements DynamicMBeanProvider
{
    private static class JMXManageableXMBean extends XMBean implements NotificationEmitter
    {
        final NotificationBroadcasterSupport broadCaster_ = new NotificationBroadcasterSupport();
        long notificationSequence_ = 1;
        private final String[] types_;
        
        public JMXManageableXMBean(final JMXManageable manageable, URL url) throws NotCompliantMBeanException, MBeanException
        {
            super(manageable, url);
     
            types_ = manageable.getJMXNotificationTypes();
            
            manageable.setJMXCallback(new JMXManageable.JMXCallback(){

                public void sendJMXNotification(String type, String message)
                {
                    broadCaster_.sendNotification(new Notification(type,// type
                            manageable, // source
                            ++notificationSequence_, // seq. number
                            message // message
                            ));
                }

                public void sendJMXAttributeChanged(String name, Object oldValue, Object newValue)
                {
                    broadCaster_.sendNotification(new AttributeChangeNotification(manageable,
                            ++notificationSequence_, 
                            System.currentTimeMillis(), 
                            "Attribute value changed",
                            name, 
                            oldValue == null ? null : oldValue.getClass().getName(), 
                            oldValue, 
                            newValue));
                }});
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

        public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException
        {
            broadCaster_.removeNotificationListener(listener, filter, handback);
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
    }
    
    private final String domain_;
    private final JMXManageableMBeanProvider fallback_;

    public JMXManageableXMBeanProvider(String domain)
    {
        super();
        
        domain_ = domain;

        fallback_ = new JMXManageableMBeanProvider(domain);
    }

    public JMXRegistrationInfo provide(PicoContainer picoContainer,
            ComponentAdapter componentAdapter)
    {
        final Object _componentInstance = componentAdapter.getComponentInstance(picoContainer);

        try
        {
            final JMXManageable _manageable = (JMXManageable) _componentInstance;

            Exception _exception = null;

            try
            {
                // try to load XML XMBean Descriptor.
                String _clazzName = _manageable.getClass().getName().replace('.', '/');

                URL _url = _manageable.getClass().getResource("/" + _clazzName + ".xml");

                if (_url == null)
                {
                    return fallback_.provide(picoContainer, componentAdapter);
                }

                final ObjectName _objectName = ObjectName.getInstance(domain_ + ":" + _manageable.getJMXObjectName());
                final JMXManageableXMBean _xmbean = new JMXManageableXMBean(_manageable, _url);
                
                return new JMXRegistrationInfo(_objectName, _xmbean);
            } catch (MalformedObjectNameException e)
            {
                _exception = e;
            } catch (NotCompliantMBeanException e)
            {
                _exception = e;
            } catch (NullPointerException e)
            {
                _exception = e;
            } catch (MBeanException e)
            {
                _exception = e;
            }

            throw new JMXRegistrationException("Cannot create MBean for component '"
                    + componentAdapter.getComponentKey() + "'", _exception);
            
        } catch (ClassCastException e)
        {
            return null;
        }
    }
}
