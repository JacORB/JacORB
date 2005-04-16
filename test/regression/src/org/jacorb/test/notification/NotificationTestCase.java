package org.jacorb.test.notification;

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

import java.lang.reflect.Constructor;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.container.PicoContainerFactory;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.filter.ETCLEvaluator;
import org.jacorb.notification.queue.EventQueueFactory;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Repository;
import org.omg.CORBA.RepositoryHelper;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.PropertySeqHelper;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyChannelAdmin.EventChannelFactoryHelper;
import org.omg.CosNotifyFilter.Filter;
import org.omg.DynamicAny.DynAnyFactory;
import org.omg.PortableServer.POA;
import org.picocontainer.MutablePicoContainer;

/**
 * @author Alphonse Bendt
 */

public abstract class NotificationTestCase extends TestCase
{
    private NotificationTestCaseSetup setup_;

    private EventChannel defaultChannel_;

    protected MutablePicoContainer container_;

    protected Logger logger_;
    
    ///////////////////////////////

    public NotificationTestCase(String name, NotificationTestCaseSetup setup)
    {
        super(name);

        setup_ = setup;
    }

    ////////////////////////////////////////

    public final void setUp() throws Exception
    {
        container_ = PicoContainerFactory.createRootContainer((org.jacorb.orb.ORB)setup_.getORB());

        logger_ = ((org.jacorb.config.Configuration) getConfiguration()).getNamedLogger(getClass()
                .getName()
                + "." + getName());

        setUpTest();
    }

    protected void setUpTest() throws Exception
    {
        // empty on purpose.
    }

    public final void tearDown() throws Exception
    {
        tearDownTest();

        if (defaultChannel_ != null)
        {
            defaultChannel_.destroy();
        }
        
        super.tearDown();
    }
    
    protected void tearDownTest() throws Exception
    {
        // empty to be overridden.
    }

    public MutablePicoContainer getContainer()
    {
        return container_;
    }

    public Filter createFilter() throws Exception
    {
        return getDefaultChannel().default_filter_factory().create_filter("EXTENDED_TCL");
    }

    public ORB getClientORB()
    {
        return getSetup().getClientORB();
    }

    public EventChannel getDefaultChannel()
    {
        try
        {
            if (defaultChannel_ == null)
            {
                defaultChannel_ = getFactory().create_channel(new Property[0], new Property[0],
                        new IntHolder());

                assertTrue(defaultChannel_.MyFactory()._is_equivalent(getFactory()));
            }

            return defaultChannel_;
        } catch (Exception e)
        {
            throw new RuntimeException();
        }
    }

    public ORB getORB()
    {
        return (ORB) container_.getComponentInstance(ORB.class);
    }

    public POA getPOA()
    {
        return (POA) container_.getComponentInstance(POA.class);
    }

    public DynAnyFactory getDynAnyFactory() throws Exception
    {
        return (DynAnyFactory) container_.getComponentInstance(DynAnyFactory.class);
    }

    public Configuration getConfiguration()
    {
        return (Configuration) container_.getComponentInstance(Configuration.class);
    }

    public MessageFactory getMessageFactory()
    {
        return (MessageFactory) container_.getComponentInstance(MessageFactory.class);
    }

    public ETCLEvaluator getEvaluator()
    {
        return (ETCLEvaluator) container_.getComponentInstance(ETCLEvaluator.class);
    }

    public TaskProcessor getTaskProcessor()
    {
        return (TaskProcessor) container_.getComponentInstance(TaskProcessor.class);
    }

    public EventQueueFactory getEventQueueFactory()
    {
        return (EventQueueFactory) container_.getComponentInstance(EventQueueFactory.class);
    }

    public NotificationTestUtils getTestUtils()
    {
        return setup_.getTestUtils();
    }

    public EventChannelFactory getFactory()
    {
        try
        {
            return EventChannelFactoryHelper.narrow(setup_.getFactoryServant().activate());
        } catch (Exception e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }

    private NotificationTestCaseSetup getSetup()
    {
        return setup_;
    }

    public static Test suite(Class clazz) throws Exception
    {
        return suite(clazz, "test");
    }

    public static Test suite(Class clazz, String testPrefix) throws Exception
    {
        return suite("TestSuite defined in Class: " + clazz.getName(), clazz, testPrefix);
    }
    
    public static Test suite(String suiteName, Class clazz) throws Exception
    {
        return suite(suiteName, clazz, "test");
    }

    private static Test suite(String suiteName, Class clazz, String testMethodPrefix)
            throws Exception
    {
        TestSuite _suite = new TestSuite(suiteName);

        NotificationTestCaseSetup _setup = new NotificationTestCaseSetup(_suite);

        String[] _methodNames = TestUtils.getTestMethods(clazz, testMethodPrefix);

        addToSuite(_suite, _setup, clazz, _methodNames);

        return _setup;
    }

    private static void addToSuite(TestSuite suite, NotificationTestCaseSetup setup, Class clazz,
            String[] testMethods) throws Exception
    {
        Constructor _ctor = clazz.getConstructor(new Class[] { String.class,
                NotificationTestCaseSetup.class });

        for (int x = 0; x < testMethods.length; ++x)
        {
            suite.addTest((Test) _ctor.newInstance(new Object[] { testMethods[x], setup }));
        }
    }

    public Any toAny(String s)
    {
        Any a = getORB().create_any();

        a.insert_string(s);

        return a;
    }

    public Any toAny(int i)
    {
        Any a = getORB().create_any();

        a.insert_long(i);

        return a;
    }

    public Any toAny(Property[] props) throws Exception
    {
        Any _any = getORB().create_any();

        PropertySeqHelper.insert(_any, props);

        return _any;
    }

    public Repository getRepository() throws Exception
    {
        return RepositoryHelper.narrow(getORB().resolve_initial_references("InterfaceRepository"));
    }
}