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

import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.jacorb.notification.interfaces.JMXManageable;
import org.nanocontainer.remoting.jmx.DynamicMBeanProvider;
import org.nanocontainer.remoting.jmx.JMXRegistrationException;
import org.nanocontainer.remoting.jmx.JMXRegistrationInfo;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;

public class JMXManageableMBeanProvider implements DynamicMBeanProvider
{
    private final String domain_;

    public JMXManageableMBeanProvider(String domain)
    {
        super();
        
        domain_ = domain;
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
                return new JMXRegistrationInfo(ObjectName.getInstance(domain_ + ":"
                        + _manageable.getJMXObjectName()), new BroadcastSupportMBeanDecorator(
                        _manageable));
            } catch (MalformedObjectNameException e)
            {
                _exception = e;
            } catch (NotCompliantMBeanException e)
            {
                _exception = e;
            } catch (ClassNotFoundException e)
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
