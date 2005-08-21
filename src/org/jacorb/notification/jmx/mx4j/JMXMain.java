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

import java.io.IOException;
import java.util.Map;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.jacorb.notification.jmx.JMXManageableMBeanProvider;
import org.omg.CORBA.ORB;
import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;
import org.tanukisoftware.wrapper.jmx.WrapperManagerMBean;

/**
 * MX4J specific startup class for JMX-enabled Notification Service
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */
public class JMXMain implements WrapperListener
{
    public static final String DEFAULT_DOMAIN = "NotificationService";
    
    private final ObjectName notificationServiceName_ = ObjectName.getInstance(DEFAULT_DOMAIN + ":type=EventChannelFactory");
    
    JMXConnectorServer connectorServer_;

    private Thread jmxConnectorRunner_;
    
    ORB orb_;
    
    private Thread orbRunner_;
    
    private final MBeanServer mbeanServer_;

    private JMXMain() throws Exception
    {
        super();
        
        mbeanServer_ = MBeanServerFactory.createMBeanServer();
    }
    
    private void initConnectorServer() throws Exception, IOException
    {
        JMXServiceURL _nameServiceURL = getJNDINameServiceURL();

        // The environment map, null in this case
        Map _environment = null;

        // Create the JMXCconnectorServer
        connectorServer_ = JMXConnectorServerFactory.newJMXConnectorServer(_nameServiceURL,
                _environment, mbeanServer_);

        // Start the JMXConnectorServer
        jmxConnectorRunner_ = new Thread("JMXConnectorServer-Thread")
        {
            public void run()
            {
                try
                {
                    connectorServer_.start();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
    }

    public Integer start(String[] args)
    {
        orb_ = ORB.init(args, null);
        
        orbRunner_ = new Thread("ORB-Thread")
        {
            public void run()
            {
                orb_.run();
            }
        };
        
        orbRunner_.start();
        
        try
        {
            initConnectorServer();
            
            MX4JCOSNotificationServiceMBean _notificationService = new MX4JCOSNotificationService(
                    orb_, mbeanServer_, new JMXManageableMBeanProvider(DEFAULT_DOMAIN), args);

            StandardMBean _mbean = new StandardMBean(_notificationService, MX4JCOSNotificationServiceMBean.class);

            mbeanServer_.registerMBean(_mbean, notificationServiceName_);

            registerWrapperManager();

            jmxConnectorRunner_.start();

            mbeanServer_.invoke(notificationServiceName_, "start", null, null);

            return null;
        } catch (Exception e)
        {
            WrapperManager.log(WrapperManager.WRAPPER_LOG_LEVEL_FATAL, "Unable to Start Service" + e);

            try
            {
                connectorServer_.stop();
            } catch (IOException e1)
            {
                e1.printStackTrace();
            }
            
            orb_.shutdown(true);
            
            throw new RuntimeException(e);
        }
    }

    public int stop(int code)
    {
        try
        {
            mbeanServer_.invoke(notificationServiceName_, "stop", null, null);

            mbeanServer_.unregisterMBean(notificationServiceName_);
            
            connectorServer_.stop();
            
            orb_.shutdown(true);
        } catch (Exception e)
        {
            e.printStackTrace();
            
            WrapperManager.log(WrapperManager.WRAPPER_LOG_LEVEL_ERROR, "Unable to Stop Service" + e);
          
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
            // handle the event ourselves.

            if ((event == WrapperManager.WRAPPER_CTRL_C_EVENT)
                    || (event == WrapperManager.WRAPPER_CTRL_CLOSE_EVENT)
                    || (event == WrapperManager.WRAPPER_CTRL_SHUTDOWN_EVENT))
            {
                org.tanukisoftware.wrapper.WrapperManager.stop(0);
            }
        }
    }

    private JMXServiceURL getJNDINameServiceURL() throws Exception
    {
        ObjectName namingName = ObjectName.getInstance("naming:type=rmiregistry");
        mbeanServer_.createMBean("mx4j.tools.naming.NamingService", namingName, null);
        WrapperManager.log(WrapperManager.WRAPPER_LOG_LEVEL_INFO, "Starting NamingService");

        mbeanServer_.invoke(namingName, "start", null, null);
        int namingPort = ((Integer) mbeanServer_.getAttribute(namingName, "Port")).intValue();

        String jndiPath = "/jmxconnector";

        JMXServiceURL address = 
            new JMXServiceURL("service:jmx:rmi://localhost/jndi/rmi://localhost:" + namingPort + jndiPath);

        return address;
    }
    
    private JMXServiceURL getCosNamingNameServiceURL() throws Exception
    {
        ORB orb = ORB.init(new String[0], null);
        org.jacorb.orb.rmi.PortableRemoteObjectDelegateImpl.setORB(orb); 
        return new JMXServiceURL("service:jmx:iiop://localhost/jndi/jmxconnector"); 
    }

    private void registerWrapperManager() throws JMException
    {
        if (!WrapperManager.isControlledByNativeWrapper())
        {
            return;
        }

        WrapperManager.log(WrapperManager.WRAPPER_LOG_LEVEL_INFO, "Registering WrapperManager MBean");

        ObjectName wrapperManagerName = ObjectName.getInstance(DEFAULT_DOMAIN + ":service=WrapperManager");

        StandardMBean wrapperManagerBean = new StandardMBean(
                new org.tanukisoftware.wrapper.jmx.WrapperManager(), WrapperManagerMBean.class);

        mbeanServer_.registerMBean(wrapperManagerBean, wrapperManagerName);
    }

    public static void main(String[] args) throws Exception
    {
        JMXMain main = new JMXMain();
        
        WrapperManager.start(main, args);
    }
}