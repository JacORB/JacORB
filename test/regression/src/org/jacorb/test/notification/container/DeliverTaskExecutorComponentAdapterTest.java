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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.avalon.framework.configuration.Configuration;
import org.easymock.MockControl;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.container.PicoContainerFactory;
import org.jacorb.notification.engine.TaskExecutor;
import org.omg.CORBA.ORB;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class DeliverTaskExecutorComponentAdapterTest extends TestCase
{
    private MutablePicoContainer container;

    private MockControl controlConfig;

    private Configuration mockConfig;

    public DeliverTaskExecutorComponentAdapterTest(String name)
    {
        super(name);
    }

    public void setUp()
    {
        container = new DefaultPicoContainer();

        controlConfig = MockControl.createControl(Configuration.class);
        mockConfig = (Configuration) controlConfig.getMock();
        container.registerComponentInstance(Configuration.class, mockConfig);
    }

    public void testGetComponentAdapter_ThreadPool()
    {
        mockConfig.getAttribute("jacorb.notification.proxysupplier.threadpolicy", Default.DEFAULT_THREADPOLICY);
        controlConfig.setReturnValue(Default.DEFAULT_THREADPOLICY);

        mockConfig.getAttributeAsInteger("jacorb.notification.proxysupplier.thread_pool_size", Default.DEFAULT_DELIVER_POOL_SIZE);
        controlConfig.setReturnValue(4);

        controlConfig.replay();

        ComponentAdapter adapter = PicoContainerFactory
                .newDeliverTaskExecutorComponentAdapter(container);

        assertNotNull(adapter);

        container.registerComponent(adapter);

        TaskExecutor executor1 = (TaskExecutor) container.getComponentInstance(TaskExecutor.class);

        assertNotNull(executor1);
        
        TaskExecutor executor2 = (TaskExecutor) container.getComponentInstance(TaskExecutor.class);

        assertNotNull(executor2);
        
        assertSame(executor1, executor2);

        controlConfig.verify();
    }

    public void testGetComponentAdapter_ThreadPerProxy()
    {
        mockConfig.getAttribute("jacorb.notification.proxysupplier.threadpolicy", Default.DEFAULT_THREADPOLICY);
        controlConfig.setReturnValue("ThreadPerProxy");

        controlConfig.replay();

        ComponentAdapter adapter = PicoContainerFactory
                .newDeliverTaskExecutorComponentAdapter(container);

        assertNotNull(adapter);

        container.registerComponent(adapter);

        TaskExecutor executor1 = (TaskExecutor) container.getComponentInstance(TaskExecutor.class);

        assertNotNull(executor1);
        
        TaskExecutor executor2 = (TaskExecutor) container.getComponentInstance(TaskExecutor.class);

        assertNotNull(executor2);
        
        assertFalse(executor1 == executor2);

        controlConfig.verify();
    }
    
    public void testFactoryMethod() throws Exception
    {
        ORB orb = ORB.init(new String[0], null);
        
        PicoContainer container = PicoContainerFactory.createRootContainer((org.jacorb.orb.ORB)orb);
        
        ComponentAdapter adapter = PicoContainerFactory.newDeliverTaskExecutorComponentAdapter(container);
        
        assertNotNull(adapter);
    }

    public static Test suite()
    {
        return new TestSuite(DeliverTaskExecutorComponentAdapterTest.class);
    }
}