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

import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class JMXMain
{
    public JMXMain()
    {
    }

    public static void main(String[] args) throws Exception
    {
        MBeanServer server = MBeanServerFactory.createMBeanServer();

        ObjectName name = new ObjectName("NotificationService:mbean=EventChannelFactory");
        
        StandardMBean bean = new StandardMBean(new EventChannelFactoryControl(),
                EventChannelFactoryMBean.class);
     
        server.registerMBean(bean, name);
        
        ObjectName namingName = ObjectName.getInstance("naming:type=rmiregistry");
        server.createMBean("mx4j.tools.naming.NamingService", namingName, null);
        server.invoke(namingName, "start", null, null);
        int namingPort = ((Integer)server.getAttribute(namingName, "Port")).intValue();        

        String jndiPath = "/jmxconnector";

        //        The environment map, null in this case
        Map environment = null;
        
        JMXServiceURL address = 
            new JMXServiceURL("service:jmx:rmi://localhost/jndi/rmi://localhost:" + namingPort + jndiPath);
        
        //        Create the JMXCconnectorServer
        JMXConnectorServer cntorServer = 
            JMXConnectorServerFactory.newJMXConnectorServer(address,
                environment, server);

        //        Start the JMXConnectorServer
        cntorServer.start();     
   }
}