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

package org.jacorb.test.notification.container;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.jacorb.notification.container.DisposeLifecycleVisitor;
import org.jacorb.notification.container.NonCachingRememberingComponentAdapter;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.Disposable;
import org.picocontainer.LifecycleManager;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Startable;
import org.picocontainer.alternatives.RootVisitingLifecycleManager;
import org.picocontainer.defaults.ComponentMonitor;
import org.picocontainer.defaults.DefaultPicoContainer;
import org.picocontainer.defaults.LifecycleVisitor;
import org.picocontainer.defaults.NullComponentMonitor;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class ContainerLifecycleTest extends TestCase
{
    private MutablePicoContainer container_;

    private MockControl controlCA_;

    private ComponentAdapter mockCA_;

    private static Method START;

    private static Method STOP;

    private static Method DISPOSE;

    protected void setUp() throws Exception
    {
        super.setUp();

        ComponentMonitor cm = new NullComponentMonitor();

        LifecycleManager lm = new RootVisitingLifecycleManager(new DisposeLifecycleVisitor(START,
                Startable.class, true), new LifecycleVisitor(STOP, Startable.class, false, cm),
                new LifecycleVisitor(DISPOSE, Disposable.class, false, cm));

        container_ = new DefaultPicoContainer(lm);

        controlCA_ = MockControl.createControl(ComponentAdapter.class);
        mockCA_ = (ComponentAdapter) controlCA_.getMock();
    }

    public void testLifeCycleStart() throws Exception
    {
        mockCA_.getComponentKey();
        controlCA_.setDefaultReturnValue(Startable.class);

        mockCA_.getComponentImplementation();
        controlCA_.setDefaultReturnValue(Startable.class);

        mockCA_.accept(null);
        controlCA_.setMatcher(MockControl.ALWAYS_MATCHER);

        controlCA_.replay();
        container_.registerComponent(new NonCachingRememberingComponentAdapter(mockCA_));

        container_.start();

        controlCA_.verify();
    }

    public void testLifeCycleStart2() throws Exception
    {
        mockCA_.getComponentKey();
        controlCA_.setDefaultReturnValue(Startable.class);

        mockCA_.getComponentImplementation();
        controlCA_.setDefaultReturnValue(Startable.class);

        mockCA_.accept(null);
        controlCA_.setMatcher(MockControl.ALWAYS_MATCHER);

        MockControl controlService = MockControl.createControl(Startable.class);
        Startable mockService = (Startable) controlService.getMock();

        mockService.start();
        
        controlService.replay();
        
        mockCA_.getComponentInstance(container_);
        controlCA_.setReturnValue(mockService);
        
        controlCA_.replay();
        container_.registerComponent(new NonCachingRememberingComponentAdapter(mockCA_));

        Startable service = (Startable) container_.getComponentInstance(Startable.class);
        assertNotNull(service);
        
        container_.start();

        controlCA_.verify();
        controlService.verify();
    }
    

    static
    {
        try
        {
            START = Startable.class.getMethod("start", null);
            STOP = Startable.class.getMethod("stop", null);
            DISPOSE = Disposable.class.getMethod("dispose", null);
        } catch (NoSuchMethodException e)
        {
            throw new InternalError(e.getMessage());
        }
    }
}