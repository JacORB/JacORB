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

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;

import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.JMXManageable;
import org.jacorb.notification.util.LogUtil;
import org.nanocontainer.remoting.jmx.DynamicMBeanProvider;
import org.nanocontainer.remoting.jmx.JMXRegistrationInfo;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;

/**
 * a DynamicMBeanProvider decorator that will add a disposehook to the instance
 * for which a mbean is created
 * the disposehook will deregister the mbean from the mbeanserver.
 *  
 * @author Alphonse Bendt
 * @version $Id$
 */

public class UnregisterObjectNameProviderDecorator implements DynamicMBeanProvider
{
    private final DynamicMBeanProvider delegate_;

    final MBeanServer mbeanServer_;

    final Logger logger_ = LogUtil.getLogger(getClass().getName());

    public UnregisterObjectNameProviderDecorator(MBeanServer mbeanServer, DynamicMBeanProvider delegate)
    {
        super();

        mbeanServer_ = mbeanServer;
        delegate_ = delegate;
    }

    public JMXRegistrationInfo provide(PicoContainer picoContainer, ComponentAdapter componentAdapter)
    {
        final JMXRegistrationInfo _info = delegate_.provide(picoContainer, componentAdapter);

        if (_info != null)
        {
            try
            {
                final JMXManageable manageable = 
                    (JMXManageable) componentAdapter.getComponentInstance(picoContainer);

                manageable.registerDisposable(new Disposable()
                {
                    public void dispose()
                    {
                        try
                        {
                            logger_.info("Unregister MBean " + _info.getObjectName());

                            mbeanServer_.unregisterMBean(_info.getObjectName());
                        } catch (InstanceNotFoundException e)
                        {
                            logger_.error("Error while unregistering MBean "
                                    + _info.getObjectName(), e);
                        } catch (MBeanRegistrationException e)
                        {
                            logger_.error("Error while unregistering MBean "
                                    + _info.getObjectName(), e);
                        }
                    }
                });
            } catch (ClassCastException e)
            {
                // ignore as componentInstance is not a JMXManageable
            }
        }

        return _info;
    }
}
