package org.jacorb.test.notification.common;

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


import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.container.PicoContainerFactory;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.filter.ETCLEvaluator;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.queue.EventQueueFactory;
import org.jacorb.notification.util.DisposableManager;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Repository;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.PropertySeqHelper;
import org.omg.DynamicAny.DynAnyFactory;
import org.omg.PortableServer.POA;
import org.picocontainer.MutablePicoContainer;

/**
 * helper class for notification service unit tests.
 *
 * @author Alphonse Bendt
 */

public abstract class NotificationTestCase extends TestCase
{
    private NotificationTestCaseSetup setup_;

    protected Logger logger_;

    private MutablePicoContainer container_;

    private DisposableManager disposables_;

    ///////////////////////////////

    public NotificationTestCase(String name, NotificationTestCaseSetup setup)
    {
        super(name);

        setup_ = setup;
    }

    ////////////////////////////////////////

    public final void setUp() throws Exception
    {
        disposables_ = new DisposableManager();

        container_ = PicoContainerFactory.createChildContainer(setup_.getPicoContainer());

        logger_ = ((org.jacorb.config.Configuration) getConfiguration()).getNamedLogger(getClass()
                .getName()
                + "." + getName());

        setUpTest();
    }

    protected void setUpTest() throws Exception
    {
        // empty to be overridden.
    }

    public final void tearDown() throws Exception
    {
        setup_.getPicoContainer().removeChildContainer(container_);

        tearDownTest();

        disposables_.dispose();

        super.tearDown();
    }

    protected void tearDownTest() throws Exception
    {
        // empty to be overridden.
    }

    public MutablePicoContainer getPicoContainer()
    {
        return container_;
    }

    public ORB getClientORB()
    {
        return getSetup().getClientORB();
    }

    public ORB getORB()
    {
        return setup_.getORB();
    }

    public POA getPOA()
    {
        return setup_.getPOA();
    }

    public DynAnyFactory getDynAnyFactory() throws Exception
    {
        return (DynAnyFactory) getPicoContainer().getComponentInstanceOfType(DynAnyFactory.class);
    }

    public Configuration getConfiguration()
    {
        return (Configuration) getPicoContainer().getComponentInstanceOfType(Configuration.class);
    }

    public MessageFactory getMessageFactory()
    {
        return (MessageFactory) getPicoContainer().getComponentInstanceOfType(MessageFactory.class);
    }

    public ETCLEvaluator getEvaluator()
    {
        return (ETCLEvaluator) getPicoContainer().getComponentInstanceOfType(ETCLEvaluator.class);
    }

    public TaskProcessor getTaskProcessor()
    {
        return (TaskProcessor) getPicoContainer().getComponentInstanceOfType(TaskProcessor.class);
    }

    public EventQueueFactory getEventQueueFactory()
    {
        return (EventQueueFactory) getPicoContainer().getComponentInstanceOfType(EventQueueFactory.class);
    }

    public NotificationTestUtils getTestUtils()
    {
        return setup_.getTestUtils();
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

    public static Test suite(String suiteName, Class clazz, String testMethodPrefix)
            throws Exception
    {
        return TestUtils.suite(clazz, NotificationTestCaseSetup.class, suiteName, testMethodPrefix);
    }


    protected void addDisposable(Disposable d)
    {
        disposables_.addDisposable(d);
    }

    public Any toAny(String s)
    {
        Any _any = getORB().create_any();

        _any.insert_string(s);

        return _any;
    }

    public Any toAny(int i)
    {
        Any _any = getORB().create_any();

        _any.insert_long(i);

        return _any;
    }

    public Any toAny(Property[] props) throws Exception
    {
        Any _any = getORB().create_any();

        PropertySeqHelper.insert(_any, props);

        return _any;
    }

    public Repository getRepository() throws Exception
    {
    	return setup_.getRepository();
    }
}