package org.jacorb.notification.filter;

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
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.servant.ManageableServant;
import org.jacorb.notification.util.DisposableManager;
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

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class FilterFactoryImpl extends FilterFactoryPOA implements Disposable,
        ManageableServant

{
    private final ORB orb_;

    private final POA poa_;

    private final DisposableManager disposeHooks_ = new DisposableManager();

    private final List allFilters_ = new ArrayList();

    private final Object allFiltersLock_ = new Object();

    protected final Logger logger_;

    private final Configuration config_;

    private org.omg.CORBA.Object reference_;

    private FilterFactory thisRef_;

    private final IFilterFactoryDelegate filterDelegate_;

    // //////////////////////////////////////

    /**
     * @param filterDelegate this Factory assumes ownership over the delegate and will dispose it after use.
     */
    public FilterFactoryImpl(ORB orb, POA poa, Configuration config,
            IFilterFactoryDelegate filterDelegate)
    {
        super();

        orb_ = orb;
        poa_ = poa;
        config_ = ((org.jacorb.config.Configuration) config);

        filterDelegate_ = filterDelegate;

        logger_ = ((org.jacorb.config.Configuration) config_).getNamedLogger(getClass().getName());

        addDisposeHook(new Disposable()
        {
            public void dispose()
            {
                filterDelegate_.dispose();
            }
        });
    }

    public final void addDisposeHook(Disposable d)
    {
        disposeHooks_.addDisposable(d);
    }

    public final Filter create_filter(String grammar) throws InvalidGrammar
    {
        final AbstractFilter _servant = filterDelegate_.create_filter_servant(grammar);

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

    public MappingFilter create_mapping_filter(String grammar, Any any) throws InvalidGrammar
    {
        MappingFilterImpl _mappingFilterServant = filterDelegate_.create_mapping_filter_servant(
                config_, grammar, any);

        MappingFilter _filter = _mappingFilterServant._this(orb_);

        return _filter;
    }

    /**
     * @deprecated
     */
    public final void preActivate()
    {
        // no op
    }

    public final void deactivate()
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

    protected Servant getServant()
    {
        return this;
    }

    public synchronized FilterFactory getFilterFactory()
    {
        if (thisRef_ == null)
        {
            thisRef_ = newFilterFactory();
        }

        return thisRef_;
    }

    public FilterFactory newFilterFactory()
    {
        return _this(getORB());
    }

    public synchronized org.omg.CORBA.Object activate()
    {
        if (reference_ == null)
        {
            reference_ = FilterFactoryHelper.narrow(getServant()._this_object(orb_));
        }
        return reference_;
    }

    public final void dispose()
    {
        try
        {
            deactivate();
        } finally
        {
            disposeHooks_.dispose();
        }
    }

    public final POA _default_POA()
    {
        return poa_;
    }

    protected final ORB getORB()
    {
        return orb_;
    }

    public List getFilters()
    {
        return Collections.unmodifiableList(allFilters_);
    }
}