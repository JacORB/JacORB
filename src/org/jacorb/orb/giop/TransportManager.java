package org.jacorb.orb.giop;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.orb.DefaultProfileSelector;
import org.jacorb.orb.ORB;
import org.jacorb.orb.ProfileSelector;
import org.jacorb.orb.diop.DIOPFactories;
import org.jacorb.orb.factory.SocketFactoryManager;
import org.jacorb.orb.giop.TransportListener.Event;
import org.jacorb.util.ObjectUtil;
import org.omg.CORBA.BAD_PARAM;
import org.omg.ETF.Factories;

/**
 * This class manages Transports. On the one hand it creates them, and
 * on the other it enforces an upper limit on the open transports.
 *
 * The class also receives notifications from threads that are about do use a 
 * Transport and notifies any interested listeners. "Use" is defined as 
 * sending (or handling) a request.
 *
 * @author Nicolas Noffke
 * @version $Id$
 * */

public class TransportManager
    implements Configurable
{
    /** the configuration object  */
    private org.jacorb.config.Configuration configuration = null;

    /** configuration properties */
    private Logger logger = null;
    private List factoryClassNames = null;
    private ProfileSelector profileSelector = null;
    private final SocketFactoryManager socketFactoryManager;

    /**
     * Maps ETF Profile tags (Integer) to ETF Factories objects.
     */
    private Map  factoriesMap  = null;

    /**
     * List of all installed ETF Factories.  This list contains an
     * instance of each Factories class, ordered in the same way as
     * they were specified in the jacorb.transport.factories property.
     */
    private List factoriesList = null;

    /**
     * The first listener (in a chain of instances), representing 
     * parties with interest in Transport events.
     */
    private TransportListener listener = null;

    public TransportManager( ORB orb )
    {
        socketFactoryManager = new SocketFactoryManager(orb);
    }

    public void configure(Configuration myConfiguration)
        throws ConfigurationException
    {
        this.configuration = (org.jacorb.config.Configuration)myConfiguration;
        logger =
            configuration.getNamedLogger("jacorb.orb.giop");
        socketFactoryManager.configure(configuration);

        // get factory class names
        factoryClassNames =
            this.configuration.getAttributeList("jacorb.transport.factories");

        if (factoryClassNames.isEmpty())
        {
            factoryClassNames.add("org.jacorb.orb.iiop.IIOPFactories");
        }

        // get profile selector info
        profileSelector =
            (ProfileSelector)configuration.getAttributeAsObject("jacorb.transport.client.selector");

        if (profileSelector == null)
        {
            profileSelector = new DefaultProfileSelector();
        }
    }

    public ProfileSelector getProfileSelector()
    {
        return profileSelector;
    }

    public SocketFactoryManager getSocketFactoryManager()
    {
        return socketFactoryManager;
    }

    /**
     * Returns an ETF Factories object for the given tag, or null
     * if no Factories class has been defined for this tag.
     */
    public synchronized org.omg.ETF.Factories getFactories(int tag)
    {
        // This isn't ideal. If DIOPFactories was a full implementation then
        // this class should be added to the
        // TransportManager::loadFactories. This shortcut block (which is used
        // by ParsedIOR) and the static caching in DIOPFactories wouldn't be
        // needed.
        if (tag == DIOPFactories.TAG_DIOP_UDP)
        {
            return DIOPFactories.getDIOPFactory();
        }

        if (factoriesMap == null)
        {
            loadFactories();
        }
        return (Factories)factoriesMap.get (ObjectUtil.newInteger(tag));
    }

    /**
     * Returns a list of Factories for all configured transport plugins,
     * in the same order as they were specified in the
     * jacorb.transport.factories property.
     */
    public synchronized List getFactoriesList()
    {
        if (factoriesList == null)
        {
            loadFactories();
        }
        return Collections.unmodifiableList(factoriesList);
    }

    /**
     * Build the factoriesMap and factoriesList.
     */
    private void loadFactories()
    {
        if (configuration == null )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER("TransportManager not configured!");
        }

        if (factoryClassNames == null )
        {
            throw new org.omg.CORBA.INTERNAL("factoryClassNames may not be null");
        }

        factoriesMap  = new HashMap();
        factoriesList = new ArrayList();

        for (Iterator i = factoryClassNames.iterator(); i.hasNext();)
        {
            String className = (String)i.next();
            Factories factories = instantiateFactories(className);
            factoriesMap.put(new Integer(factories.profile_tag()), factories); // NOPMD
            factoriesList.add (factories);
        }
    }

    /**
     * Instantiates the given Factories class.
     */
    private org.omg.ETF.Factories instantiateFactories (String className)
    {
        try
        {
            // ObjectUtil.classForName() uses the context class loader.
            // This is important here because JacORB might be on the
            // bootclasspath, and the external transport on the normal
            // classpath.
            Class clazz = ObjectUtil.classForName(className);
            Object instance = clazz.newInstance();

            if (instance instanceof Configurable)
            {
                Configurable configurable = (Configurable)instance;
                configurable.configure(configuration);
            }

            logger.debug("created org.omg.ETF.Factories: " + className);

            return (Factories)instance;
        }
        catch (Exception e)
        {
            throw new BAD_PARAM
                ("could not instantiate Factories class " + className
                 + ", exception: " + e);
        }
    }

	public void notifyTransportListeners(GIOPConnection giopc) {

        if (listener != null)
            listener.transportSelected (new Event (giopc));
    }

    public void addTransportListener(final TransportListener tl) {

        if (logger.isInfoEnabled ())
            logger.info ("Transport listener to add: " + tl);

        if (tl == null) return;

        synchronized (this) {
            if (listener == null) {
                listener = tl;
            }
            else {

                listener = new TransportListener () {

                    private final TransportListener next_ = listener;

                    public void transportSelected(Event event) {

                        try {
                            tl.transportSelected (event);
                        }
                        finally {
                            next_.transportSelected (event);
                        }
                    }
                };
            }
        }
    }
}
