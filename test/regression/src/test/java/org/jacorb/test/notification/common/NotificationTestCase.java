package org.jacorb.test.notification.common;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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


import org.jacorb.config.Configuration;
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.container.PicoContainerFactory;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.filter.ETCLEvaluator;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.queue.EventQueueFactory;
import org.jacorb.notification.util.DisposableManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Repository;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.PropertySeqHelper;
import org.omg.DynamicAny.DynAnyFactory;
import org.omg.PortableServer.POA;
import org.picocontainer.MutablePicoContainer;
import org.slf4j.Logger;

/**
 * helper class for notification service unit tests.
 *
 * @author Alphonse Bendt
 */

public abstract class NotificationTestCase
{
    @Rule
    public TestName name = new TestName();

    protected static NotificationTestCaseSetup setup;

    protected Logger logger_;

    private MutablePicoContainer container_;

    private DisposableManager disposables_;

    @BeforeClass
    public static void beforeClassSetup() throws Exception
    {
        setup = new NotificationTestCaseSetup();
    }

    @AfterClass
    public static void afterClassSetup() throws Exception
    {
        setup.tearDown();
    }

    @Before
    public void NTCsetUp() throws Exception
    {
        disposables_ = new DisposableManager();

        container_ = PicoContainerFactory.createChildContainer(setup.getPicoContainer());

        logger_ = getConfiguration().getLogger(getClass().getName()
                + "." + name.getMethodName());

        setUpTest();
    }

    protected void setUpTest() throws Exception
    {
        // empty to be overridden.
    }

    @After
    public void NTCtearDown() throws Exception
    {
        setup.getPicoContainer().removeChildContainer(container_);

        tearDownTest();

        disposables_.dispose();
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
        return setup.getClientORB();
    }

    public ORB getORB()
    {
        return setup.getServerORB();
    }

    public POA getPOA()
    {
        return setup.getPOA();
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
        return setup.getTestUtils();
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
    	return setup.getRepository();
    }
}