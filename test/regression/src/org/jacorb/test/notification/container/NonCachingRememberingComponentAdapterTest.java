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

import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.easymock.MockControl;
import org.jacorb.notification.container.NonCachingRememberingComponentAdapter;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.ConstructorInjectionComponentAdapter;
import org.picocontainer.defaults.DefaultPicoContainer;
import org.picocontainer.defaults.InstanceComponentAdapter;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class NonCachingRememberingComponentAdapterTest extends TestCase
{
    private MutablePicoContainer container_;

    public NonCachingRememberingComponentAdapterTest(String name)
    {
        super(name);
    }

    protected void setUp()
    {
        container_ = new DefaultPicoContainer();
    }

    public void testInContainer()
    {
        container_.registerComponent(new NonCachingRememberingComponentAdapter(
                new ConstructorInjectionComponentAdapter(Service.class, ServiceImpl.class)));

        container_.registerComponent(new ConstructorInjectionComponentAdapter(ServiceUser.class,
                ServiceUser.class));

        ServiceUser user1 = (ServiceUser) container_.getComponentInstance(ServiceUser.class);

        ServiceUser user2 = (ServiceUser) container_.getComponentInstance(ServiceUser.class);

        assertNotNull(user1);
        assertNotNull(user2);
        
        assertNotNull(user1.service_);
        assertNotNull(user2.service_);

        assertFalse(user1.service_ == user2.service_);
    }

    public void testWithCachingCA()
    {
        final MockControl controlService = MockControl.createNiceControl(Service.class);
        final Service mockService = (Service) controlService.getMock();
        mockService.invoke();
        mockService.invoke();
        mockService.dispose();
        controlService.replay();

        MockControl controlComponentAdapter = MockControl.createControl(ComponentAdapter.class);
        ComponentAdapter mockComponentAdapter = (ComponentAdapter) controlComponentAdapter
                .getMock();

        mockComponentAdapter.getComponentKey();
        controlComponentAdapter.setDefaultReturnValue(Service.class);

        mockComponentAdapter.getComponentImplementation();
        controlComponentAdapter.setDefaultReturnValue(Service.class);

        mockComponentAdapter.getComponentInstance(container_);
        controlComponentAdapter.setDefaultReturnValue(mockService);

        controlComponentAdapter.replay();

        NonCachingRememberingComponentAdapter adapter = new NonCachingRememberingComponentAdapter(
                mockComponentAdapter);

        container_.registerComponent(adapter);

        Service service1 = (Service) container_.getComponentInstance(Service.class);
        Service service2 = (Service) container_.getComponentInstance(Service.class);

        service1.invoke();
        service2.invoke();

        List active = adapter.getActiveInstances();
        for (Iterator iter = active.iterator(); iter.hasNext();)
        {
            Service element = (Service) iter.next();
            element.dispose();
        }
        //container_.dispose();

        // begin verify

        controlComponentAdapter.verify();
        controlService.verify();
    }

    public void testComponentAdapter()
    {
        final MockControl controlService1 = MockControl.createNiceControl(Service.class);
        final Service mockService1 = (Service) controlService1.getMock();

        final MockControl controlService2 = MockControl.createNiceControl(Service.class);
        final Service mockService2 = (Service) controlService2.getMock();

        mockService1.invoke();
        mockService2.invoke();

        mockService1.dispose();
        mockService2.dispose();

        controlService1.replay();
        controlService2.replay();

        MockControl controlComponentAdapter = MockControl.createControl(ComponentAdapter.class);
        ComponentAdapter mockComponentAdapter = (ComponentAdapter) controlComponentAdapter
                .getMock();

        mockComponentAdapter.getComponentKey();
        controlComponentAdapter.setDefaultReturnValue(Service.class);

        mockComponentAdapter.getComponentImplementation();
        controlComponentAdapter.setDefaultReturnValue(Service.class);

        mockComponentAdapter.getComponentInstance(container_);
        controlComponentAdapter.setReturnValue(mockService1);

        mockComponentAdapter.getComponentInstance(container_);
        controlComponentAdapter.setReturnValue(mockService2);

        controlComponentAdapter.replay();

        // begin test

        NonCachingRememberingComponentAdapter adapter = new NonCachingRememberingComponentAdapter(
                mockComponentAdapter);

        container_.registerComponent(adapter);

        Service service1 = (Service) container_.getComponentInstance(Service.class);
        Service service2 = (Service) container_.getComponentInstance(Service.class);

        service1.invoke();
        service2.invoke();

        List active = adapter.getActiveInstances();
        for (Iterator iter = active.iterator(); iter.hasNext();)
        {
            Service element = (Service) iter.next();
            element.dispose();
        }

        // container_.dispose();

        // begin verify

        controlComponentAdapter.verify();
        controlService1.verify();
        controlService2.verify();
    }

    public void testDisposeRemovesFromActive() throws Exception
    {
        MockControl controlDelegate = MockControl.createControl(Runnable.class);
        final Runnable mockDelegate = (Runnable) controlDelegate.getMock();

        final ServiceImpl registeredService = new ServiceImpl()
        {
            public void invoke()
            {
                mockDelegate.run();
            }
        };
        
        mockDelegate.run();
        controlDelegate.replay();
        
        ComponentAdapter delegate = new InstanceComponentAdapter(Service.class, registeredService);

        NonCachingRememberingComponentAdapter adapter = new NonCachingRememberingComponentAdapter(delegate);
        
        container_.registerComponent(adapter);
        
        Service service = (Service) container_.getComponentInstance(Service.class);
        
        assertTrue(adapter.getActiveInstances().contains(service));
        
        service.invoke();
        
        service.dispose();
        
        assertFalse(adapter.getActiveInstances().contains(service));
        
        controlDelegate.verify();
    }

    /**
     * @return
     */
    public static Test suite()
    {
       return new TestSuite(NonCachingRememberingComponentAdapterTest.class);
    }
}

