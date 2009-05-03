/*
 * JacORB - a free Java ORB
 * 
 * Copyright (C) 1999-2004 Gerald Brose
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Library General Public License as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139,
 * USA.
 *  
 */

package org.jacorb.notification.container;

import org.jacorb.config.*;
import org.slf4j.Logger;
import org.jacorb.notification.IContainer;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.filter.FilterFactoryImpl;
import org.jacorb.notification.filter.DefaultFilterFactoryDelegate;
import org.jacorb.notification.util.LogUtil;
import org.jacorb.notification.util.PatternWrapper;
import org.omg.CORBA.ORB;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.CosNotifyFilter.FilterFactoryHelper;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.AbstractComponentAdapter;

class FilterFactoryComponentAdapter extends AbstractComponentAdapter
{
    private static final long serialVersionUID = 1L;
    
    private final Logger logger_ = LogUtil.getLogger(getClass().getName());

    public FilterFactoryComponentAdapter()
    {
        super(FilterFactory.class, FilterFactory.class);
    }

    public void verify(PicoContainer container)
    {
        // no operation
    }

    public Object getComponentInstance(PicoContainer container)
    {
        if (!Default.DEFAULT_FILTER_FACTORY.equals(getFilterFactoryLocation(container)))
        {
            try
            {
                return lookupFilterFactory(container);
            } catch (Exception e)
            {
                logger_.info("Could not resolve FilterFactory. Will fall back to builtin FilterFactory.", e);
            }
        }

        return newFilterFactory(container);
    }

    private FilterFactory newFilterFactory(PicoContainer container)
    {
        // force Classloader to load Class PatternWrapper.
        // PatternWrapper may cause a ClassNotFoundException if
        // running on < JDK1.4 and gnu.regexp is NOT installed.
        // Therefor the Error should occur as _early_ as possible.
        PatternWrapper.class.getName();

        final MutablePicoContainer _parent = (MutablePicoContainer) container;

        final MutablePicoContainer _container = _parent.makeChildContainer();

        _container.registerComponentImplementation(DefaultFilterFactoryDelegate.class);

        _container.registerComponentImplementation(FilterFactoryImpl.class);
        
        _container.registerComponentInstance(IContainer.class, new IContainer()
        {
            public MutablePicoContainer getContainer()
            {
                return _container;
            }

            public void destroy()
            {
                _parent.removeChildContainer(_container);
            }
        });

        FilterFactoryImpl servant = 
            (FilterFactoryImpl) _container.getComponentInstanceOfType(FilterFactoryImpl.class);

        return FilterFactoryHelper.narrow(servant.activate());
    }

    private FilterFactory lookupFilterFactory(PicoContainer container)
    {
        String _filterFactoryConf = getFilterFactoryLocation(container);

        ORB orb = (ORB) container.getComponentInstance(ORB.class);

        return FilterFactoryHelper.narrow(orb.string_to_object(_filterFactoryConf));
    }

    private String getFilterFactoryLocation(PicoContainer container)
    {
        Configuration config = (Configuration) container.getComponentInstance(Configuration.class);

        String _location = config.getAttribute(Attributes.FILTER_FACTORY, Default.DEFAULT_FILTER_FACTORY);

        return _location;
    }
}