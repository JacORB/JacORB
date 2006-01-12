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
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.GCDisposable;
import org.jacorb.notification.lifecycle.IServantLifecyle;
import org.jacorb.notification.lifecycle.ServantLifecyleControl;
import org.jacorb.notification.util.DisposableManager;
import org.jacorb.notification.util.LogUtil;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterFactoryPOA;
import org.omg.CosNotifyFilter.FilterHelper;
import org.omg.CosNotifyFilter.InvalidGrammar;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.CosNotifyFilter.MappingFilterHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class FilterFactoryImpl extends FilterFactoryPOA implements Disposable, IServantLifecyle
{
    private class GCThread extends Thread implements Disposable
    {
        private final AtomicBoolean active = new AtomicBoolean(true);

        public GCThread()
        {
            super();
            setName("NotificationService Filter GC");
            setPriority(Thread.MIN_PRIORITY + 1);
        }
        
        public void run()
        {
            while (active.get())
            {
                try
                {
                    Thread.sleep(1000);

                    runLoop();
                } catch (InterruptedException e)
                {
                    // ignore. will check active_
                }
            }

            logger_.info("GCThread exits");
        }

        private void runLoop() throws InterruptedException
        {
            synchronized (allFiltersLock_)
            {
                Iterator i = new ArrayList(allFilters_).iterator();

                while (i.hasNext())
                {
                    GCDisposable item = (GCDisposable) i.next();

                    try
                    {
                        item.attemptDispose();
                    } catch (Exception e)
                    {
                        i.remove();
                    }

                    verifyIsActive();
                }
            }
        }

        private void verifyIsActive() throws InterruptedException
        {
            if (!active.get())
            {
                throw new InterruptedException();
            }
        }

        public void dispose()
        {
            logger_.info("Shutdown GCThread");

            active.set(false);
        }
    }

    private final ORB orb_;

    private final POA poa_;

    private final DisposableManager disposeHooks_ = new DisposableManager();

    private final List allFilters_ = new ArrayList();

    private final Object allFiltersLock_ = new Object();

    protected final Logger logger_;

    private final IFilterFactoryDelegate factoryDelegate_;

    private final boolean useGarbageCollector_;

    private final Configuration config_;

    private final ServantLifecyleControl servantLifecycle_;

    // //////////////////////////////////////

    public FilterFactoryImpl(ORB orb, POA poa, Configuration config,
            IFilterFactoryDelegate factoryDelegate)
    {
        super();

        orb_ = orb;
        poa_ = poa;
       
        factoryDelegate_ = factoryDelegate;

        config_ = config;
        logger_ = LogUtil.getLogger(config, getClass().getName());

        useGarbageCollector_ = config.getAttributeAsBoolean(Attributes.USE_GC,
                Default.DEFAULT_USE_GC);
        
        if (useGarbageCollector_)
        {
            logger_.info("Enable Garbage Collection for Filters");

            final GCThread _gcThread = new GCThread();

            addDisposeHook(_gcThread);
            
            _gcThread.start();
        }
        
        servantLifecycle_ = new ServantLifecyleControl(this);
    }

    public final void addDisposeHook(Disposable d)
    {
        disposeHooks_.addDisposable(d);
    }

    public final Filter create_filter(String grammar) throws InvalidGrammar
    {
        final AbstractFilter _servant = factoryDelegate_.create_filter_servant(grammar);

        registerFilter(_servant);

        Filter _filter = FilterHelper.narrow(_servant.activate());

        return _filter;
    }

    public MappingFilter create_mapping_filter(String grammar, Any any) throws InvalidGrammar
    {
        MappingFilterImpl _mappingFilterServant = factoryDelegate_.create_mapping_filter_servant(
                config_, grammar, any);

        registerFilter(_mappingFilterServant);

        MappingFilter _filter = MappingFilterHelper.narrow(_mappingFilterServant.activate());

        return _filter;
    }

    private final void registerFilter(final GCDisposable filter)
    {
        if (useGarbageCollector_)
        {
            synchronized (allFiltersLock_)
            {
                allFilters_.add(filter);

                filter.registerDisposable(new Disposable()
                {
                    public void dispose()
                    {
                        synchronized (allFiltersLock_)
                        {
                            allFilters_.remove(filter);
                        }
                    }
                });
            }
        }
    }

    public final void deactivate()
    {
        servantLifecycle_.deactivate();
    }

    public Servant newServant()
    {
        return this;
    }

    public final org.omg.CORBA.Object activate()
    {
        return servantLifecycle_.activate();
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

    protected final ORB getORB()
    {
        return orb_;
    }
    
    public POA getPOA()
    {
        return poa_;
    }
}