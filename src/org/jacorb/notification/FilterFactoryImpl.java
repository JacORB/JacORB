package org.jacorb.notification;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.filter.AbstractFilter;
import org.jacorb.notification.filter.MappingFilterImpl;
import org.jacorb.notification.filter.etcl.ETCLFilter;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.servant.ManageableServant;
import org.jacorb.util.ObjectUtil;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.CosNotifyFilter.FilterFactoryHelper;
import org.omg.CosNotifyFilter.FilterFactoryPOA;
import org.omg.CosNotifyFilter.FilterHelper;
import org.omg.CosNotifyFilter.InvalidGrammar;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.ConstructorInjectionComponentAdapter;
import org.picocontainer.defaults.DefaultPicoContainer;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class FilterFactoryImpl extends FilterFactoryPOA implements Disposable, ManageableServant
        
{
    private final ORB orb_;

    private final POA poa_;

    private final List disposeHooks_ = new ArrayList();

    private final List allFilters_ = new ArrayList();

    private final Object allFiltersLock_ = new Object();
    
    private FilterFactory thisRef_;

    private final Logger logger_;

    private final Configuration config_;

    private org.omg.CORBA.Object reference_;

    private final List availableFilters_ = new ArrayList();

    private final MutablePicoContainer pico_;

    private final MutablePicoContainer filterPico_;

    ////////////////////////////////////////

    public FilterFactoryImpl(IContainer container, ORB orb, POA poa, Configuration config)
    {
        super();

        PicoContainer parent = container.getContainer();

        if (parent != null)
        {
            pico_ = new DefaultPicoContainer(parent);

            filterPico_ = new DefaultPicoContainer(parent);
        }
        else
        {
            pico_ = new DefaultPicoContainer();

            filterPico_ = new DefaultPicoContainer();
        }

        orb_ = orb;
        poa_ = poa;
        config_ = ((org.jacorb.config.Configuration) config);

        logger_ = ((org.jacorb.config.Configuration) config_).getNamedLogger(getClass().getName());

        loadFilterPlugins(config_);
    }

    public String getControllerName()
    {
        return "org.jacorb.notification.jmx.FilterFactoryControl";
    }

    public void addDisposeHook(Disposable d)
    {
        disposeHooks_.add(d);
    }

    private void loadFilterPlugins(Configuration conf)
    {
        // add default ETCL Filter
        filterPico_.registerComponent(new ConstructorInjectionComponentAdapter(
                ETCLFilter.CONSTRAINT_GRAMMAR, ETCLFilter.class));

        availableFilters_.add(ETCLFilter.CONSTRAINT_GRAMMAR);

        Iterator i = getAttributeNamesWithPrefix(conf, Attributes.FILTER_PLUGIN_PREFIX).iterator();

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

                filterPico_.registerComponent(new ConstructorInjectionComponentAdapter(_clazzName,
                        _clazz));

                availableFilters_.add(_grammar);
            } catch (ConfigurationException e)
            {
                logger_.error("Unable to access attribute: " + key, e);
            } catch (ClassNotFoundException e)
            {
                logger_.error("Property " + key + ": class " + _clazzName + " is unknown", e);
            }
        }
    }

    public Filter create_filter(String grammar) throws InvalidGrammar
    {
        final AbstractFilter _servant = create_filter_servant(grammar);

        _servant.preActivate();

        Filter _filter = FilterHelper.narrow(_servant.activate());

        synchronized (allFiltersLock_)
        {
            allFilters_.add(_servant);

            _servant.addDisposeHook(new Disposable()
            {
                public void dispose()
                {
                    synchronized (allFiltersLock_)
                    {
                        allFilters_.remove(_servant);
                    }
                }
            });
        }

        return _filter;
    }

    public String getAvailableConstraintLanguages()
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

    private AbstractFilter create_filter_servant(String grammar) throws InvalidGrammar
    {
        AbstractFilter _filterServant = (AbstractFilter) filterPico_.getComponentInstance(grammar);

        if (_filterServant == null)
        {
            logger_.error("unable to create FilterServant as grammar " + grammar + " is unknown");

            throw new InvalidGrammar("Constraint Language '" + grammar
                    + "' is not supported. Try one of the following: "
                    + getAvailableConstraintLanguages());

        }

        return _filterServant;
    }

    public MappingFilter create_mapping_filter(String grammar, Any any) throws InvalidGrammar
    {
        AbstractFilter _filterImpl = create_filter_servant(grammar);

        MappingFilterImpl _mappingFilterServant = new MappingFilterImpl(config_, _filterImpl, any);

        MappingFilter _filter = _mappingFilterServant._this(orb_);

        return _filter;
    }

    public void preActivate()
    {

    }

    public void deactivate()
    {
        try
        {
            poa_.deactivate_object(poa_.servant_to_id(getServant()));
        } catch (Exception e)
        {
            logger_.fatalError("cannot deactivate object", e);
            throw new RuntimeException();
        }
    }

    private Servant getServant()
    {
        return this;
    }

    public synchronized org.omg.CORBA.Object activate()
    {
        if (reference_ == null)
        {
            reference_ = FilterFactoryHelper.narrow(getServant()._this_object(orb_));
        }
        return reference_;
    }

    public void dispose()
    {
        deactivate();

        Iterator i = disposeHooks_.iterator();
        while (i.hasNext())
        {
            ((Disposable) i.next()).dispose();
        }
        
        disposeHooks_.clear();
        pico_.dispose();
        filterPico_.dispose();
    }

    public synchronized FilterFactory getFilterFactory()
    {
        if (thisRef_ == null)
        {
            thisRef_ = _this(orb_);
        }

        return thisRef_;
    }

    public POA _default_POA()
    {
        return poa_;
    }

    public List getFilters()
    {
        return Collections.unmodifiableList(allFilters_);
    }
    
    private static List getAttributeNamesWithPrefix(Configuration configuration, String prefix)
    {
        final List _attributesWithPrefix = new ArrayList();

        final String[] _allAttributes = configuration.getAttributeNames();

        for (int x = 0; x < _allAttributes.length; ++x)
        {
            if (_allAttributes[x].startsWith(prefix))
            {
                _attributesWithPrefix.add(_allAttributes[x]);
            }
        }

        return Collections.unmodifiableList(_attributesWithPrefix);
    }
}