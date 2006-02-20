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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.management.Attribute;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.StandardMBean;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;

import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.ConsoleMain;
import org.jacorb.notification.jmx.JMXManageableMBeanProvider;
import org.jacorb.orb.rmi.PortableRemoteObjectDelegateImpl;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
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

    private static boolean sUseHTTPConnector = false;

    private static boolean sUseMX4J;
    
    private ObjectName notificationServiceName_;

    private final List connectors_ = new ArrayList();

    ORB orb_;

    private MBeanServer mbeanServer_;

    private Logger logger_;

    private JMXMain()
    {
        super();
    }

    private void stopConnectors()
    {
        for (Iterator i = connectors_.iterator(); i.hasNext();)
        {
            ObjectName name = (ObjectName) i.next();
            try
            {
                mbeanServer_.invoke(name, "stop", null, null);
            } catch (Exception e)
            {
                logger_.warn("Unable to stop Connnector " + name, e);
            }
        }
    }

    private void startHTTPConnector() throws Exception
    {
        final ObjectName _connectorName = new ObjectName("connectors:protocol=http");
        mbeanServer_.createMBean("mx4j.tools.adaptor.http.HttpAdaptor", _connectorName, null);
        // TODO make port default configurable
        mbeanServer_.setAttribute(_connectorName, new Attribute("Port", new Integer(8001)));
        mbeanServer_.setAttribute(_connectorName, new Attribute("Host", "localhost"));
        mbeanServer_.invoke(_connectorName, "start", null, null);

        final ObjectName _processorName = new ObjectName("Server:name=XSLTProcessor");
        mbeanServer_.createMBean("mx4j.tools.adaptor.http.XSLTProcessor", _processorName, null);

        mbeanServer_.setAttribute(_connectorName, new Attribute("ProcessorName", _processorName));
        
        connectors_.add(_connectorName);
    }

    private void startIIOPConnector() throws Exception, IOException
    {
        JMXServiceURL _serviceURL = new JMXServiceURL("service:jmx:iiop://localhost/jndi/COSNotification");

        Map _environment = new HashMap();
        _environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.cosnaming.CNCtxFactory");

        // fetch NameService Ref via ORB
        org.omg.CORBA.Object _nameService = orb_.resolve_initial_references("NameService");
        String _nameServiceIOR = orb_.object_to_string(_nameService);
        _environment.put(Context.PROVIDER_URL, _nameServiceIOR);

        _environment.put("java.naming.corba.orb", orb_);

        // create the JMXCconnectorServer
        JMXConnectorServer _connectorServer = 
            JMXConnectorServerFactory.newJMXConnectorServer(_serviceURL, _environment, mbeanServer_);

        // register the JMXConnectorServer in the MBeanServer
        ObjectName _connectorServerName = ObjectName.getInstance("connectors:protocol=iiop");
        mbeanServer_.registerMBean(_connectorServer, _connectorServerName);

        _connectorServer.start();

        connectors_.add(_connectorServerName);
    }

    public Integer start(String[] args)
    {
        try
        {
            initORB(args);

            notificationServiceName_ = ObjectName.getInstance(DEFAULT_DOMAIN
                    + ":type=EventChannelFactory");

            mbeanServer_ = MBeanServerFactory.createMBeanServer();

            registerNotificationService(args);

            registerWrapperManager();

            startIIOPConnector();
            
            if (sUseHTTPConnector)
            {
                startHTTPConnector();
            }
            
            return null;
        } catch (Exception e)
        {
            if (logger_ != null)
            {
                logger_.error("Unable to Start Service", e);
            }

            WrapperManager.log(WrapperManager.WRAPPER_LOG_LEVEL_FATAL, "Unable to Start Service" + e);

            stopConnectors();

            orb_.shutdown(true);

            throw new RuntimeException(e);
        }
    }

    private void registerNotificationService(String[] args) throws NotCompliantMBeanException,
            InstanceAlreadyExistsException, InstanceNotFoundException, MBeanException,
            ReflectionException
    {
        final MX4JCOSNotificationServiceMBean _notificationService = new MX4JCOSNotificationService(orb_,
                mbeanServer_, new JMXManageableMBeanProvider(DEFAULT_DOMAIN), args);

        final StandardMBean _mbean = new StandardMBean(_notificationService,
                MX4JCOSNotificationServiceMBean.class);

        mbeanServer_.registerMBean(_mbean, notificationServiceName_);

        mbeanServer_.invoke(notificationServiceName_, "start", null, null);
    }

    private void initORB(String[] args) throws InvalidName, AdapterInactive
    {
        Properties _props = ConsoleMain.parseProperties(args);
        
        orb_ = ORB.init(args, _props);
        
        PortableRemoteObjectDelegateImpl.setORB(orb_);
        
        logger_ = ((org.jacorb.orb.ORB) orb_).getConfiguration().getNamedLogger(
                getClass().getName());

        Thread _orbRunner = new Thread("ORB-Thread")
        {
            public void run()
            {
                orb_.run();
            }
        };

        POA _poa = POAHelper.narrow(orb_.resolve_initial_references("RootPOA"));
        _poa.the_POAManager().activate();

        _orbRunner.start();
    }

    public int stop(int code)
    {
        try
        {
            mbeanServer_.invoke(notificationServiceName_, "stop", null, null);

            mbeanServer_.unregisterMBean(notificationServiceName_);

            stopConnectors();

            orb_.shutdown(true);
        } catch (Exception e)
        {
            logger_.error("Error while stopping Service", e);

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
            return;
        }

        // We are not being controlled by the Wrapper, so
        // handle the event ourselves.

        if ((event == WrapperManager.WRAPPER_CTRL_C_EVENT)
                || (event == WrapperManager.WRAPPER_CTRL_CLOSE_EVENT)
                || (event == WrapperManager.WRAPPER_CTRL_SHUTDOWN_EVENT))
        {
            org.tanukisoftware.wrapper.WrapperManager.stop(0);
        }
    }

    private void registerWrapperManager() throws JMException
    {
        if (!WrapperManager.isControlledByNativeWrapper())
        {
            return;
        }

        WrapperManager.log(WrapperManager.WRAPPER_LOG_LEVEL_INFO,
                "Registering WrapperManager MBean");

        ObjectName wrapperManagerName = ObjectName.getInstance(DEFAULT_DOMAIN
                + ":service=WrapperManager");

        StandardMBean wrapperManagerBean = new StandardMBean(
                new org.tanukisoftware.wrapper.jmx.WrapperManager(), WrapperManagerMBean.class);

        mbeanServer_.registerMBean(wrapperManagerBean, wrapperManagerName);
    }

    public static void main(String[] args) throws Exception
    {
        List list = new ArrayList(Arrays.asList(args));
        
        if (list.remove("-mx4j"))
        {
            sUseMX4J = true;
            System.setProperty("javax.management.builder.initial", "mx4j.server.MX4JMBeanServerBuilder");
        }

        if (list.remove("-mx4j:http") && sUseMX4J)
        {
            sUseHTTPConnector = true;
        }
        
        System.setProperty("javax.rmi.CORBA.PortableRemoteObjectClass", PortableRemoteObjectDelegateImpl.class.getName());        
        
        JMXMain main = new JMXMain();
        
        WrapperManager.start(main, (String[])list.toArray(new String[list.size()]));
    }
}