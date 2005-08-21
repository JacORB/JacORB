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

package org.jacorb.notification.jmx.mx4j;

import javax.management.MBeanServer;

import org.jacorb.notification.jmx.COSNotificationService;
import org.nanocontainer.remoting.jmx.DynamicMBeanProvider;
import org.omg.CORBA.ORB;

/**
 * MX4J specific NotificationService MBean.
 * 
 * @jmx.mbean   name="MX4JCosNotificationService" 
 *              extends = "org.jacorb.notification.jmx.COSNotificationServiceMBean"
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */
public class MX4JCOSNotificationService 
    extends COSNotificationService 
    implements MX4JCOSNotificationServiceMBean
{
    public MX4JCOSNotificationService(ORB orb, MBeanServer mbeanServer,
            DynamicMBeanProvider mbeanProvider, String[] args)
    {
        super(orb, mbeanServer, mbeanProvider, args);
    }

    /**
     * @jmx.managed-operation 
     */
    public String start()
    {
        return super.start();
    }

    /**
     * @jmx.managed-operation 
     */
    public String stop()
    {
        return super.stop();
    }
}
