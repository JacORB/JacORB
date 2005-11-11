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
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.jacorb.notification.ConsoleMain;
import org.jacorb.notification.jmx.COSNotificationService;
import org.jboss.iiop.CorbaORBService;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.system.ServiceMBeanSupport;
import org.omg.CORBA.ORB;

/**
 * Adaptor to run a JMX-enabled NotificationService inside of JBoss.
 * 
 * @jmx.mbean   name = "JBossCOSNotificationService" 
 *              extends = "org.jboss.system.ServiceMBean"
 *              persistPolicy = "OnUpdate" 
 *              persistPeriod = "10" 
 *              persistLocation = "${jboss.server.data.dir}" 
 *              persistName = "COSNotification.ser" 
 *              state-action-on-update = "keep-running" 
 *              persistence-manager = "org.jboss.mx.persistence.ObjectStreamPersistenceManager"
 *              
 * @jboss.xmbean 
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */
public class JBossCOSNotificationService extends ServiceMBeanSupport implements
        JBossCOSNotificationServiceMBean, ObjectFactory
{
    public final static String NAMING_NAME = "COSNotification";

    private static final String DEFAULT_DOMAIN = "NotificationService";

    private static final String NOT_RUNNING = "Not Started";

    private COSNotificationService delegate_;

    private String iorFileName_;

    private String cosNamingEntry_;

    private String additionalArguments_;

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
     * @--jmx.managed-operation description = "Detyped lifecycle invocation" 
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
        return isStarted() ? delegate_.createChannel() : NOT_RUNNING;
    }

    /**
     * @jmx.managed-attribute   description = "NameService Entry (Optional)" 
     *                          access = "read-write"
     */
    public String getCOSNamingEntry()
    {
        return cosNamingEntry_;
    }

    /**
     * @jmx.managed-attribute access = "read-write"
     */
    public void setCOSNamingEntry(String cosNamingEntry)
    {
        cosNamingEntry_ = cosNamingEntry;

        if (isStarted())
        {
            updateCOSNamingEntry();
        }
    }

    private void updateCOSNamingEntry()
    {
        delegate_.setCOSNamingEntry(cosNamingEntry_);
        log.info("Bound to COSNaming name: " + cosNamingEntry_);
    }

    /**
     * @jmx.managed-attribute   description="Corbaloc to access the EventChannelFactory" 
     *                          access = "read-only"
     */
    public String getCorbaloc()
    {
        return isStarted() ? delegate_.getCorbaloc() : NOT_RUNNING;
    }

    /**
     * @jmx.managed-attribute   description="IOR to access the EventChannelFactory" 
     *                          access = "read-only"
     */
    public String getIOR()
    {
        return isStarted() ? delegate_.getIOR() : NOT_RUNNING;
    }

    /**
     * @jmx.managed-attribute   description = "Filename the IOR should be written to" 
     *                          access = "read-write"
     */
    public String getIORFile()
    {
        return iorFileName_;
    }

    /**
     * @jmx.managed-attribute access = "read-write"
     */
    public void setIORFile(String filename) throws IOException
    {
        iorFileName_ = filename;

        if (isStarted())
        {
            updateIORFile();
        }
    }

    private void updateIORFile() throws IOException
    {
        delegate_.setIORFile(iorFileName_);
        log.info("set IOR filename to " + iorFileName_);
    }

    /**
     * @jmx.managed-attribute access = "read-write"
     */
    public void setAdditionalArguments(String additionalArguments)
    {
        additionalArguments_ = additionalArguments;
    }

    /**
     * @jmx.managed-attribute description = "Additional startup arguments. Setting these on an
     *                        already running service will have no effect!" 
     *                        access = "read-write"
     */
    public String getAdditionalArguments()
    {
        return additionalArguments_;
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

        String[] args = ConsoleMain.splitArgs(additionalArguments_);

        delegate_ = new COSNotificationService(_orb, MBeanServerLocator.locateJBoss(),
                new JMXManageableXMBeanProvider(DEFAULT_DOMAIN), args);

        bind(NAMING_NAME, "org.omg.CosNotifyChannelAdmin.EventChannelFactory");

        updateIORFile();

        updateCOSNamingEntry();

        delegate_.start();

        log.info("COSNotificationService started");
    }

    protected void stopService() throws Exception
    {
        delegate_.stop();
        unbind(NAMING_NAME);
        log.info("COSNotificationService stopped");
    }

    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment)
            throws Exception
    {
        String s = name.toString();
        if (getLog().isTraceEnabled())
        {
            getLog().trace(
                    "getObjectInstance: obj.getClass().getName=\"" + obj.getClass().getName()
                            + "\n                   name=" + s);
        }
        if (NAMING_NAME.equals(s))
        {
            return delegate_.getEventChannelFactory();
        }
        
        throw new IllegalArgumentException();
    }

    private void bind(String name, String className) throws Exception
    {
        Reference _ref = new Reference(className, getClass().getName(), null);
        InitialContext _context = new InitialContext();

        try
        {
            _context.bind("java:/" + name, _ref);
            log.info("Bound to JNDI name: " + name);
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

    private boolean isStarted()
    {
        return getState() == STARTED;
    }
}
