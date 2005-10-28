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

import java.io.IOException;
import java.util.Properties;

import javax.management.MBeanServer;

import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.AbstractChannelFactory;
import org.jacorb.notification.ConsoleMain;
import org.jacorb.notification.EventChannelFactoryImpl;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.util.LogUtil;
import org.nanocontainer.remoting.jmx.DynamicMBeanProvider;
import org.nanocontainer.remoting.jmx.JMXExposingComponentAdapterFactory;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.Property;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.CachingComponentAdapterFactory;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.ConstructorInjectionComponentAdapterFactory;
import org.picocontainer.defaults.DefaultPicoContainer;

/**
 * @jmx.mbean   name="COSNotificationServiceMBean" 
 *              description="Control the JacORB Notification Service"
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */
public class COSNotificationService implements COSNotificationServiceMBean
{
    private AbstractChannelFactory factory_;

    private final MutablePicoContainer container_;

    private final static String STARTED = "EventChannelFactory was started";

    private final static String RUNNING = "EventChannelFactory is running";

    private final static String NOT_RUNNING = "EventChannelFactory is not running";

    private final static String STOPPED = "EventChannelFactory was stopped";

    private final static String IOR_DEFAULT = "IOR:0";

    private final static String CORBALOC_DEFAULT = "<undefined>";

    private final Logger logger_ = LogUtil.getLogger(getClass().getName());

    private final Properties properties_;

    private final ORB optionalORB_;

    public COSNotificationService(ORB orb, MBeanServer mbeanServer,
            DynamicMBeanProvider mbeanProvider, String[] args)
    {
        super();

        optionalORB_ = orb;
        properties_ = ConsoleMain.parseProperties(args);

        DynamicMBeanProvider _decoratedProvider = new UnregisterObjectNameProviderDecorator(
                mbeanServer, mbeanProvider);

        ComponentAdapterFactory _defaultCAF = new JMXExposingComponentAdapterFactory(
                new ConstructorInjectionComponentAdapterFactory(), mbeanServer,
                new DynamicMBeanProvider[] { _decoratedProvider });

        ComponentAdapterFactory _cachingCAF = new CachingComponentAdapterFactory(
                _defaultCAF);

        container_ = new DefaultPicoContainer(_cachingCAF);
        container_.registerComponentInstance(ComponentAdapterFactory.class, _defaultCAF);
    }

    /**
     * @jmx.managed-operation description="create a new channel"
     */
    public String createChannel()
    {
        try
        {
            if (factory_ != null)
            {
                EventChannelFactoryImpl factory = (EventChannelFactoryImpl) factory_;

                IntHolder id = new IntHolder();

                factory.create_channel(new Property[0], new Property[0], id);

                return "Created Channel id=" + id.value;
            }

            return NOT_RUNNING;
        } catch (Exception e)
        {
            logger_.error("Error creating Channel", e);

            throw new RuntimeException("Create channel failed: " + e.getMessage());
        }
    }

    public String start()
    {
        if (factory_ != null)
        {
            return RUNNING;
        }

        try
        {
            factory_ = AbstractChannelFactory.newFactory(optionalORB_, container_, properties_);

            return STARTED;
        } catch (Exception e)
        {
            logger_.error("Error starting Service", e);

            throw new RuntimeException("Start failed: " + e.getMessage());
        }
    }

    public String stop()
    {
        if (factory_ != null)
        {
            factory_.dispose();
            factory_ = null;

            return STOPPED;
        }
        return NOT_RUNNING;
    }

    /**
     * @jmx.managed-attribute   description="IOR to access the EventChannelFactory" 
     *                          access = "read-only"
     */
    public String getIOR()
    {
        return (factory_ == null) ? IOR_DEFAULT : factory_.getIOR();
    }

    /**
     * @jmx.managed-attribute   description="Corbaloc to access the EventChannelFactory 
     *                          access = "read-only"
     */
    public String getCorbaloc()
    {
        return (factory_ == null) ? CORBALOC_DEFAULT : factory_.getCorbaLoc();
    }

    /**
     * @jmx.managed-attribute   description = "Filename the IOR should be written to" 
     *                          access = "read-write"
     */
    public String getIORFile()
    {
        return properties_.getProperty(Attributes.IOR_FILE);
    }

    /**
     * @jmx.managed-attribute
     */
    public void setIORFile(String filename) throws IOException
    {
        properties_.setProperty(Attributes.IOR_FILE, filename);

        if (factory_ != null)
        {
            factory_.writeIOR(filename);
        }
    }

    /**
     * @jmx.managed-attribute   description = "NameService Entry (Optional)" 
     *                          access = "read-write"
     */
    public String getCOSNamingEntry()
    {
        StringBuffer name = new StringBuffer(properties_.getProperty(Attributes.REGISTER_NAME_ID,
                "<undefined>"));

        final String nameKind = properties_.getProperty(Attributes.REGISTER_NAME_KIND);
        if (nameKind != null)
        {
            name.append('.');
            name.append(nameKind);
        }
        return name.toString();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setCOSNamingEntry(String registerName)
    {
        ConsoleMain.addCOSNamingName(properties_, registerName);

        if (factory_ != null)
        {
            try
            {
                factory_.unregisterName();
                factory_.registerName(properties_);
            } catch (Exception e)
            {
                logger_.error("Error changing COSNaming entry", e);

                throw new RuntimeException("Changing the COSNaming entry failed: " + e.getMessage());
            }
        }
    }
    
    public org.omg.CORBA.Object getEventChannelFactory()
    {
        return factory_.activate();
    }
}