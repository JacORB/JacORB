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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.filter.etcl.ETCLFilter;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.servant.ManageableServant;
import org.jacorb.util.ObjectUtil;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.CosNotifyFilter.FilterFactoryHelper;
import org.omg.CosNotifyFilter.FilterFactoryPOA;
import org.omg.CosNotifyFilter.FilterHelper;
import org.omg.CosNotifyFilter.InvalidGrammar;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;

/**
 * @author Alphonse Bendt
 * @version $Id: FilterFactoryImpl.java,v 1.24 2004/07/12 11:21:19
 *          alphonse.bendt Exp $
 */

public class FilterFactoryImpl extends FilterFactoryPOA implements Disposable, Configurable,
        ManageableServant
{
    private static Iterator getAttributeNamesWithPrefix(Configuration configuration, String prefix)
    {
        List _attributesWithPrefix = new ArrayList();

        String[] _allAttributes = configuration.getAttributeNames();

        for (int x = 0; x < _allAttributes.length; ++x)
        {
            if (_allAttributes[x].startsWith(prefix))
            {
                _attributesWithPrefix.add(_allAttributes[x]);
            }
        }

        return _attributesWithPrefix.iterator();
    }

    ////////////////////////////////////////

    private ApplicationContext applicationContext_;

    private ORB orb_;

    private POA poa_;

    private boolean isApplicationContextCreatedHere_;

    private List allFilters_ = new ArrayList();

    private Object allFiltersLock_ = allFilters_;

    private FilterFactory thisRef_;

    private Logger logger_ = null;

    private org.jacorb.config.Configuration config_ = null;

    private org.omg.CORBA.Object reference_;

    private Map availableFilters_ = new HashMap();

    ////////////////////////////////////////

    public FilterFactoryImpl() throws InvalidName, AdapterInactive
    {
        super();
        orb_ = ORB.init(new String[0], null);

        poa_ = POAHelper.narrow(orb_.resolve_initial_references("RootPOA"));

        applicationContext_ = new ApplicationContext(orb_, poa_);

        applicationContext_.configure(((org.jacorb.orb.ORB) orb_).getConfiguration());

        isApplicationContextCreatedHere_ = true;

        getFilterFactory();

        poa_.the_POAManager().activate();

        Thread t = new Thread(new Runnable()
        {
            public void run()
            {
                orb_.run();
            }
        });

        t.setDaemon(true);
        t.start();
    }

    public FilterFactoryImpl(ApplicationContext applicationContext) 
    {
        super();

        applicationContext_ = applicationContext;

        poa_ = applicationContext.getPoa();

        orb_ = applicationContext.getOrb();

        isApplicationContextCreatedHere_ = false;
    }

    private void loadFilterPlugins(Configuration conf) throws ConfigurationException
    {
        Iterator i = getAttributeNamesWithPrefix(conf, Attributes.FILTER_PLUGIN_PREFIX);

        while (i.hasNext())
        {
            String key = null;
            String _clazzName = null;
            try
            {
                key = (String) i.next();

                String _grammar = key.substring(Attributes.FILTER_PLUGIN_PREFIX.length() + 1);

                logger_.info("Loading Filterplugin for Grammar: " + _grammar);

                _clazzName = conf.getAttribute(key);

                Class _clazz = ObjectUtil.classForName(_clazzName);

                Constructor _constructor = _clazz
                        .getConstructor(new Class[] { ApplicationContext.class });

                availableFilters_.put(_grammar, _constructor);
            } catch (ClassNotFoundException e)
            {
                throw new ConfigurationException("Property " + key + ": class " + _clazzName
                        + " is unknown");
            } catch (NoSuchMethodException e)
            {
                throw new ConfigurationException("Property " + key + ": does the c'tor of class "
                        + _clazzName + " accept param ApplicationContext ?");
            }
        }
    }

    public void configure(Configuration conf) throws ConfigurationException
    {
        config_ = ((org.jacorb.config.Configuration) conf);

        logger_ = config_.getNamedLogger(getClass().getName());

        loadFilterPlugins(conf);
    }

    ////////////////////////////////////////

    public Filter create_filter(String grammar) throws InvalidGrammar
    {
        final AbstractFilter _servant = create_filter_servant(grammar);

        _servant.setORB(orb_);

        _servant.setPOA(poa_);

        _servant.preActivate();

        Filter _filter = FilterHelper.narrow(_servant.activate());

        synchronized (allFiltersLock_)
        {
            allFilters_.add(_servant);

            _servant.setDisposeHook(new Runnable()
            {
                public void run()
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

    private String getFilterGrammarNames()
    {
        Iterator i = availableFilters_.keySet().iterator();

        StringBuffer b = new StringBuffer();

        while (i.hasNext())
        {
            b.append(", ");
            b.append(i.next());
        }

        return b.toString();
    }

    private AbstractFilter create_filter_servant(String grammar) throws InvalidGrammar
    {
        AbstractFilter _filterServant;

        if (ETCLFilter.CONSTRAINT_GRAMMAR.equals(grammar))
        {

            _filterServant = new ETCLFilter(applicationContext_);
        }
        else if (availableFilters_.containsKey(grammar))
        {
            try
            {
                Constructor _constructor = (Constructor) availableFilters_.get(grammar);

                _filterServant = (AbstractFilter) _constructor
                        .newInstance(new Object[] { applicationContext_ });

            } catch (Exception e)
            {
                logger_.fatalError("unable to create custom filter", e);

                throw new UNKNOWN();
            }
        }
        else
        {
            throw new InvalidGrammar("Constraint Language '" + grammar
                    + "' is not supported. Try one of the following: "
                    + ETCLFilter.CONSTRAINT_GRAMMAR + getFilterGrammarNames());
        }

        _filterServant.configure(config_);

        return _filterServant;
    }

    public MappingFilter create_mapping_filter(String grammar, Any any) throws InvalidGrammar
    {

        AbstractFilter _filterImpl = create_filter_servant(grammar);

        MappingFilterImpl _mappingFilterServant = new MappingFilterImpl(applicationContext_,
                _filterImpl, any);

        _mappingFilterServant.configure(config_);

        MappingFilter _filter = _mappingFilterServant._this(orb_);

        return _filter;
    }

    public void setPOA(POA poa) {
        
    }
    
    public void setORB(ORB orb) {
        
    }
    
    public void preActivate()
    {

    }

    public void deactivate()
    {
        try {
        poa_.deactivate_object(poa_.servant_to_id(getServant()));
        } catch (Exception e) {
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
        
        Iterator i = getAllFilters().iterator();

        while (i.hasNext())
        {
            Disposable d = (Disposable) i.next();
            i.remove();
            d.dispose();
        }

        if (isApplicationContextCreatedHere_)
        {
            orb_.shutdown(true);
            applicationContext_.dispose();
        }
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

    public List getAllFilters()
    {
        return allFilters_;
    }
}