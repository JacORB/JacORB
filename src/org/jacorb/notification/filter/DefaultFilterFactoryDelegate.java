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

package org.jacorb.notification.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.IContainer;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.filter.etcl.ETCLFilter;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.util.LogUtil;
import org.jacorb.util.ObjectUtil;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosNotifyFilter.InvalidGrammar;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.ConstructorInjectionComponentAdapterFactory;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class DefaultFilterFactoryDelegate implements IFilterFactoryDelegate, Disposable
{
    private final List availableFilters_ = new ArrayList();

    private final MutablePicoContainer filterPico_;

    private final ORB orb_;

    private final Logger logger_;

    private final IContainer container_;

    private final ComponentAdapterFactory componentAdapterFactory_;

    // //////////////////////////////////////

    public DefaultFilterFactoryDelegate(IContainer container, Configuration config, ComponentAdapterFactory componentAdapterFactory)
    {
        componentAdapterFactory_ = componentAdapterFactory;

        container_ = container;

        MutablePicoContainer parent = container.getContainer();

        filterPico_ = parent;

        orb_ = (ORB) parent.getComponentInstanceOfType(ORB.class);

        logger_ = LogUtil.getLogger(config, getClass().getName());

        loadFilterPlugins(config);
    }

    public DefaultFilterFactoryDelegate(IContainer container, Configuration config)
    {
        this(container, config, new ConstructorInjectionComponentAdapterFactory());
    }

    public void dispose()
    {
        container_.destroy();
    }

    private void loadFilterPlugins(Configuration conf)
    {
        org.jacorb.config.Configuration config = (org.jacorb.config.Configuration)conf;
        ComponentAdapter etclCA = componentAdapterFactory_.createComponentAdapter(ETCLFilter.CONSTRAINT_GRAMMAR, ETCLFilter.class, null);

        // add default ETCL Filter
        filterPico_.registerComponent(etclCA);

        availableFilters_.add(ETCLFilter.CONSTRAINT_GRAMMAR);

        Iterator i = config.getAttributeNamesWithPrefix(Attributes.FILTER_PLUGIN_PREFIX).iterator();

        while (i.hasNext())
        {
            String key = (String) i.next();
            String _clazzName = null;
            try
            {
                String _grammar = key.substring(Attributes.FILTER_PLUGIN_PREFIX.length() + 1);

                logger_.info("Loading Filterplugin for Grammar: " + _grammar);

                _clazzName = conf.getAttribute(key);

                Class _clazz = ObjectUtil.classForName(_clazzName);

                ComponentAdapter customCA = componentAdapterFactory_.createComponentAdapter(_grammar, _clazz, null);

                filterPico_.registerComponent(customCA);

                availableFilters_.add(_grammar);
            } catch (ConfigurationException e)
            {
                logger_.error("Unable to access attribute: " + key, e);
            } catch (ClassNotFoundException e)
            {
                logger_.error("Property " + key + ": Unable to load FilterPlugin " + _clazzName + ". The FilterPlugin will not be available.", e);
            }
        }
    }

    private String getAvailableConstraintLanguages()
    {
        Iterator i = availableFilters_.iterator();

        StringBuffer b = new StringBuffer((String) i.next());

        while (i.hasNext())
        {
            b.append(", ");
            b.append(i.next());
        }

        return b.toString();
    }

    public AbstractFilter create_filter_servant(String grammar) throws InvalidGrammar
    {
        AbstractFilter _filterServant = (AbstractFilter) filterPico_.getComponentInstance(grammar);

        if (_filterServant == null)
        {
            logger_.error("unable to create FilterServant as grammar " + grammar + " is unknown");

            throw new InvalidGrammar("Constraint Language '" + grammar
                    + "' is not supported. Supported are: "
                    + getAvailableConstraintLanguages());
        }

        return _filterServant;
    }

    public MappingFilterImpl create_mapping_filter_servant(Configuration config, String grammar,
            Any any) throws InvalidGrammar
    {
        AbstractFilter _filter = create_filter_servant(grammar);

        return new MappingFilterImpl(orb_, config, _filter, any);
    }
}
