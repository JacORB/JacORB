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

import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;
import org.tanukisoftware.wrapper.jmx.WrapperManagerMBean;
import org.tanukisoftware.wrapper.jmx.WrapperManagerTestingMBean;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class JMXMain implements WrapperListener
{
    private ObjectName notificationServiceName;
    
    private MBeanServer server;

    private JMXConnectorServer cntorServer;

    private Thread jmxConnectorRunner;

    private JMXMain() throws Exception {
        notificationServiceName = ObjectName.getInstance("NotificationService:mbean=EventChannelFactory");
    }
    
    
    public Integer start(String[] args)
    {
        init(args);

        return null;
    }

    public int stop(int code)
    {
        try
        {
            server.invoke(notificationServiceName, "stop", null, null);
            cntorServer.stop();
        } catch (Exception e)
        {
            return 1;
        }
        return 0;
    }

    public void controlEvent(int event)
    {
        if (WrapperManager.isControlledByNativeWrapper())
        {
            // The Wrapper will take care of this event
        }
        else
        {
            // We are not being controlled by the Wrapper, so
            //  handle the event ourselves.

            if ((event == WrapperManager.WRAPPER_CTRL_C_EVENT)
                    || (event == WrapperManager.WRAPPER_CTRL_CLOSE_EVENT)
                    || (event == WrapperManager.WRAPPER_CTRL_SHUTDOWN_EVENT))
            {
                org.tanukisoftware.wrapper.WrapperManager.stop(0);
            }
        }
    }

    private void init(String[] args)
    {
        try
        {
            server = MBeanServerFactory.createMBeanServer();

            JMXServiceURL address = getServiceURL();

            // The environment map, null in this case
            Map environment = null;

            // Create the JMXCconnectorServer
            cntorServer = 
                JMXConnectorServerFactory.newJMXConnectorServer(address, environment,
                    server);

            registerNotificationService();

            registerWrapperManager();

            //registerWrapperManagerTesting();

            //        Start the JMXConnectorServer
            jmxConnectorRunner = new Thread()
            {
                public void run()
                {
                    try
                    {
                        cntorServer.start();
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            };

            jmxConnectorRunner.setName("JMX Connector Runner");

            jmxConnectorRunner.start();
        } catch (Exception e)
        {
            e.printStackTrace();
            
            throw new RuntimeException();
        }
    }

    private JMXServiceURL getServiceURL() throws Exception
    {
        ObjectName namingName = ObjectName.getInstance("naming:type=rmiregistry");
        server.createMBean("mx4j.tools.naming.NamingService", namingName, null);
        System.err.println("Starting NamingService");
        
        server.invoke(namingName, "start", null, null);
        int namingPort = ((Integer) server.getAttribute(namingName, "Port")).intValue();

        String jndiPath = "/jmxconnector";

        JMXServiceURL address = new JMXServiceURL(
                "service:jmx:rmi://localhost/jndi/rmi://localhost:" + namingPort + jndiPath);
        return address;
    }

    private void registerNotificationService() throws Exception
    {
        System.err.println("Registering NotificationService MBean");
        
        StandardMBean bean = new StandardMBean(new EventChannelFactoryControl(),
                EventChannelFactoryMBean.class);

        server.registerMBean(bean, notificationServiceName);
    }

    private void registerWrapperManager() throws Exception
    {
        if (!WrapperManager.isControlledByNativeWrapper()) {
            return;
        }
        
        System.err.println("Registering WrapperMBean");
        
        ObjectName wrapperManagerName = ObjectName
                .getInstance("JavaServiceWrapper:service=WrapperManager");
        StandardMBean wrapperManagerBean = new StandardMBean(
                new org.tanukisoftware.wrapper.jmx.WrapperManager(), WrapperManagerMBean.class);
        server.registerMBean(wrapperManagerBean, wrapperManagerName);
    }

    private void registerWrapperManagerTesting() throws Exception
    {
        ObjectName wrapperManagerTestingName = ObjectName
                .getInstance("JavaServiceWrapper:service=WrapperManagerTesting");
        StandardMBean wrapperManagerTestingBean = new StandardMBean(
                new org.tanukisoftware.wrapper.jmx.WrapperManagerTesting(),
                WrapperManagerTestingMBean.class);
        server.registerMBean(wrapperManagerTestingBean, wrapperManagerTestingName);
    }

    public static void main(String[] args) throws Exception
    {
        WrapperManager.start(new JMXMain(), args);
    }
}