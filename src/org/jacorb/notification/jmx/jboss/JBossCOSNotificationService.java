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

import java.io.IOException;

import javax.naming.InitialContext;
import javax.naming.Reference;

import org.jacorb.notification.jmx.COSNotificationService;
import org.jboss.iiop.CorbaORBService;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.system.ServiceMBeanSupport;
import org.omg.CORBA.ORB;

/**
 * Adaptor to run JMX-enabled NotificationService inside of JBoss.
 * 
 * @jmx.mbean   name = "JBossCOSNotificationService" 
 *              extends = "org.jboss.system.ServiceMBean"
 * 
 * @jboss.xmbean
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */
public class JBossCOSNotificationService extends ServiceMBeanSupport implements
        JBossCOSNotificationServiceMBean
{
    public final static String NAMING_NAME = "COSNotification";

    private static final String DEFAULT_DOMAIN = "NotificationService";

    private static final String NOT_RUNNING = "Not Started";

    private COSNotificationService delegate_;
    
    // operation from super interfaces
    // this is redundant but otherwise xmbean is not generated correctly
    
    /**
     * @jmx.managed-attribute access = "read-only"
     */
    public String getName()
    {
        return super.getName();
    }
    
    /**
     * @jmx.managed-attribute access = "read-only"
     */
    public int getState()
    {
        return super.getState();
    }
    
    /**
     * @jmx.managed-attribute access = "read-only"
     */
    public String getStateString()
    {
        return super.getStateString();
    }
    
    /**
     * @--jmx.managed-operation   description = "Detyped lifecycle invocation"
     *                          impact = "ACTION"
     */
    public void jbossInternalLifecycle(String method) throws Exception
    {
        super.jbossInternalLifecycle(method);
    }
    
    /**
     * @jmx.managed-operation   description = "create the service, do expensive operations etc"
     *                          impact = "ACTION"
     */
    public void create() throws Exception
    {
        super.create();
    }
    
    /**
     * @jmx.managed-operation   description = "start the service, create is already called"
     *                          impact = "ACTION"
     */
    public void start() throws Exception
    {
        super.start();
    }
    
    /**
     * @jmx.managed-operation   description = "stop the service"
     *                          impact = "ACTION"
     */
    public void stop()
    {
        super.stop();
    }
    
    /**
     * @jmx.managed-operation   description = "destroy the service, tear down"
     *                          impact = "ACTION"
     */
    public void destroy()
    {
        super.destroy();
    }
    
    /**
     * @jmx.managed-operation   description="create a new channel" 
     *                          impact = "ACTION"
     */
    public String createChannel()
    {
        return delegate_ == null ? NOT_RUNNING : delegate_.createChannel();
    }

    /**
     * @jmx.managed-attribute description = "NameService Entry (Optional)"
     *                        access = "read-write"
     */
    public String getCOSNamingEntry()
    {
        return delegate_ == null ? NOT_RUNNING : delegate_.getCOSNamingEntry();
    }

    /**
     * @jmx.managed-attribute description="Corbaloc to access the EventChannelFactory"
     *                        access = "read-only"
     */
    public String getCorbaloc()
    {
        return delegate_ == null ? NOT_RUNNING : delegate_.getCorbaloc();
    }

    /**
     * @jmx.managed-attribute description="IOR to access the EventChannelFactory"
     *                        access = "read-only"
     */
    public String getIOR()
    {
        return delegate_ == null ? NOT_RUNNING :  delegate_.getIOR();
    }

    /**
     * @jmx.managed-attribute description = "Filename the IOR should be written to"
     *                        access = "read-write"
     */
    public String getIORFile()
    {
        return delegate_ == null ? NOT_RUNNING : delegate_.getIORFile();
    }

    /**
     * @jmx.managed-attribute access = "read-write"
     */
    public void setCOSNamingEntry(String registerName)
    {
        delegate_.setCOSNamingEntry(registerName);
    }

    /**
     * @jmx.managed-attribute access = "read-write"
     */
    public void setIORFile(String filename) throws IOException
    {
        delegate_.setIORFile(filename);
    }

    protected void startService() throws Exception
    {
        final ORB _orb;
        InitialContext _context = new InitialContext();

        try
        {
            _orb = (ORB) _context.lookup("java:/" + CorbaORBService.ORB_NAME);

        } finally
        {
            _context.close();
        }

        delegate_ = new COSNotificationService(_orb, MBeanServerLocator.locateJBoss(),
                new JMXManageableXMBeanProvider(DEFAULT_DOMAIN), new String[] { "-channels", "1", "-registerName", "NotificationService", });

        bind(NAMING_NAME, "org.omg.CosNotifyChannelAdmin.EventChannelFactory");

        delegate_.start();

        log.info("COSNotificationService started");
    }

    protected void stopService() throws Exception
    {
        delegate_.stop();
        unbind(NAMING_NAME);
        log.info("COSNotificationService stopped");
    }

    private void bind(String name, String className) throws Exception
    {
        Reference _ref = new Reference(className, getClass().getName(), null);
        InitialContext _context = new InitialContext();

        try
        {
            _context.bind("java:/" + name, _ref);
        } finally
        {
            _context.close();
        }
    }

    private void unbind(String name) throws Exception
    {
        InitialContext _context = new InitialContext();
        
        try
        {
            _context.unbind("java:/" + name);
        } finally
        {
            _context.close();
        }
    }
}
