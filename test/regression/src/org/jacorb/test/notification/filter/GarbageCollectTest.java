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

package org.jacorb.test.notification.filter;

import java.util.Collections;

import junit.framework.Test;

import org.apache.avalon.framework.logger.Logger;
import org.easymock.MockControl;
import org.jacorb.config.Configuration;
import org.jacorb.notification.IContainer;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.filter.DefaultFilterFactoryDelegate;
import org.jacorb.notification.filter.FilterFactoryImpl;
import org.jacorb.notification.util.WeakCacheWildcardMap;
import org.jacorb.test.notification.common.NotificationTestCase;
import org.jacorb.test.notification.common.NotificationTestCaseSetup;
import org.omg.CORBA.Any;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.CosNotifyFilter.FilterFactoryHelper;
import org.picocontainer.MutablePicoContainer;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class GarbageCollectTest extends NotificationTestCase
{
    private MockControl controlConfiguration_;

    private Configuration mockConfiguration_;

    private IContainer iContainerForTest_;

    public GarbageCollectTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    protected void setUpTest() throws Exception
    {
        controlConfiguration_ = MockControl.createControl(Configuration.class);

        iContainerForTest_ = new IContainer()
        {
            public MutablePicoContainer getContainer()
            {
                return getPicoContainer();
            }

            public void destroy()
            {
                // no operation
            }
        };

        mockConfiguration_ = (Configuration) controlConfiguration_.getMock();

        // configuration options that setup code depends on.
        mockConfiguration_.getAttributeNamesWithPrefix(null);
        controlConfiguration_.setMatcher(MockControl.ALWAYS_MATCHER);
        controlConfiguration_.setReturnValue(Collections.EMPTY_LIST);

        mockConfiguration_.getAttribute(Attributes.WILDCARDMAP_CLASS, WeakCacheWildcardMap.class
                .getName());
        controlConfiguration_.setReturnValue(WeakCacheWildcardMap.class.getName());
    }

    public void testGCFilter() throws Exception
    {
        MockControl loggerControl = MockControl.createNiceControl(Logger.class);
        mockConfiguration_.getNamedLogger(null);
        controlConfiguration_.setMatcher(MockControl.ALWAYS_MATCHER);
        controlConfiguration_.setReturnValue(loggerControl.getMock(), MockControl.ZERO_OR_MORE);

        // enable gc
        mockConfiguration_.getAttributeAsBoolean(Attributes.USE_GC, Default.DEFAULT_USE_GC);
        controlConfiguration_.setReturnValue(true);

        // set timeout
        mockConfiguration_.getAttributeAsLong(Attributes.DEAD_FILTER_INTERVAL,
                Default.DEFAULT_DEAD_FILTER_INTERVAL);
        controlConfiguration_.setReturnValue(100);

        controlConfiguration_.expectAndReturn(mockConfiguration_.getAttribute(Attributes.RUN_SYSTEM_GC, Default.DEFAULT_RUN_SYSTEM_GC), "off");
        controlConfiguration_.expectAndReturn(mockConfiguration_.getAttribute(Attributes.RUN_SYSTEM_GC, Default.DEFAULT_RUN_SYSTEM_GC), "off");

        // another picocontainer is necessary so that registered
        // Configuration can be overridden locally to configure
        // garbage collection.
        getPicoContainer().registerComponentInstance(Configuration.class, mockConfiguration_);

        controlConfiguration_.replay();

        // will use our mocked configuration.
        FilterFactoryImpl factoryServant_ = new FilterFactoryImpl(getORB(), getPOA(),
                mockConfiguration_, new DefaultFilterFactoryDelegate(iContainerForTest_, mockConfiguration_));

        String _factoryRef = getORB().object_to_string(factoryServant_.activate());

        FilterFactory _factory = FilterFactoryHelper.narrow(getClientORB().string_to_object(_factoryRef));

        Filter _filter = _factory.create_filter("EXTENDED_TCL");

        assertFalse(_filter._non_existent());

        // wait some time. give gc thread chance to clean up filter.
        Thread.sleep(10000);

        try
        {
            Any any = toAny(5);
            _filter.match(any);
        } catch (OBJECT_NOT_EXIST e)
        {
            // expected
        }
    }


    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite(GarbageCollectTest.class);
    }
}
