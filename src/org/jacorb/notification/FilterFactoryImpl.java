package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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

import java.util.List;

import org.jacorb.notification.interfaces.Disposable;
//import org.jacorb.util.Debug;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.CosNotifyFilter.FilterFactoryPOA;
import org.omg.CosNotifyFilter.FilterHelper;
import org.omg.CosNotifyFilter.InvalidGrammar;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.*;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class FilterFactoryImpl
    extends FilterFactoryPOA
    implements Disposable,
               Configurable
{
    public final static String CONSTRAINT_GRAMMAR = "EXTENDED_TCL";

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

    ////////////////////////////////////////

    public FilterFactoryImpl() throws InvalidName, AdapterInactive
    {
        super();
        orb_ = ORB.init( new String[ 0 ], null );

        poa_ =
            POAHelper.narrow( orb_.resolve_initial_references( "RootPOA" ) );

        applicationContext_ = new ApplicationContext( orb_, poa_);

        applicationContext_.configure( ( ( org.jacorb.orb.ORB ) orb_ ).getConfiguration() );

        isApplicationContextCreatedHere_ = true;

        getFilterFactory();

        poa_.the_POAManager().activate();

        Thread t =
            new Thread( new Runnable()
                        {
                            public void run()
                            {
                                orb_.run();
                            }
                        }
                      );

        t.setDaemon( true );
        t.start();
    }


    public FilterFactoryImpl( ApplicationContext applicationContext )
        throws InvalidName
    {
        super();

        applicationContext_ = applicationContext;

        poa_ = applicationContext.getPoa();

        orb_ = applicationContext.getOrb();

        isApplicationContextCreatedHere_ = false;
    }


    public void configure (Configuration conf)
    {
        config_ = ((org.jacorb.config.Configuration)conf);

        logger_ = config_.getNamedLogger(getClass().getName());
    }

    ////////////////////////////////////////

    public Filter create_filter( String grammar )
        throws InvalidGrammar
    {
        final FilterImpl _servant = create_filter_servant( grammar );

        _servant.setORB(orb_);

        _servant.setPOA(poa_);

        _servant.preActivate();

        Filter _filter = FilterHelper.narrow(_servant.activate());

        synchronized(allFiltersLock_) {
            allFilters_.add(_servant);

            _servant.setDisposeHook(new Runnable() {
                    public void run() {
                        synchronized(allFiltersLock_) {
                            allFilters_.remove(_servant);
                        }
                    }
                });
        }

        return _filter;
    }


    private FilterImpl create_filter_servant( String grammar )
        throws InvalidGrammar
    {
        if ( CONSTRAINT_GRAMMAR.equals( grammar ) )
        {

            FilterImpl _filterServant =
                new FilterImpl( applicationContext_,
                                CONSTRAINT_GRAMMAR );
            _filterServant.configure (config_);
            return _filterServant;
        }
        throw new InvalidGrammar( "Constraint Language '"
                                  + grammar
                                  + "' is not supported. Try one of the following: "
                                  + CONSTRAINT_GRAMMAR );
    }


    public MappingFilter create_mapping_filter( String grammar,
                                                Any any ) throws InvalidGrammar
    {

        FilterImpl _filterImpl = create_filter_servant( grammar );

        MappingFilterImpl _mappingFilterServant =
            new MappingFilterImpl( applicationContext_,
                                   _filterImpl,
                                   any );

        _mappingFilterServant.configure (config_);

        MappingFilter _filter =
            _mappingFilterServant._this( orb_ );

        return _filter;
    }


    public void dispose()
    {
        Iterator i = getAllFilters().iterator();

        while (i.hasNext()) {
            Disposable d = (Disposable)i.next();
            i.remove();
            d.dispose();
        }

        if ( isApplicationContextCreatedHere_ )
        {
            orb_.shutdown( true );
            applicationContext_.dispose();
        }
    }


    public synchronized FilterFactory getFilterFactory()
    {
        if ( thisRef_ == null )
            {
                thisRef_ = _this( orb_ );
            }

        return thisRef_;
    }


    public POA _default_POA()
    {
        return poa_;
    }


    public List getAllFilters() {
        return allFilters_;
    }
}
