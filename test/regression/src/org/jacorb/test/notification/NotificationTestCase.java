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
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.config.Configuration;
import org.jacorb.notification.ChannelContext;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.PropertySeqHelper;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.DynamicAny.DynAnyFactory;
import org.omg.DynamicAny.DynAnyFactoryHelper;
import org.omg.PortableServer.POA;


/**
 * @author Alphonse Bendt
 */

public class NotificationTestCase extends TestCase
{
    private NotificationTestCaseSetup setup_;

    private EventChannel defaultChannel_;

    protected Logger logger_;

    ///////////////////////////////

    public NotificationTestCase(String name, NotificationTestCaseSetup setup)
    {
        super(name);

        setup_ = setup;

        logger_ = getConfiguration().getNamedLogger(getClass().getName());
    }

    ////////////////////////////////////////

    public void tearDown() throws Exception
    {
        super.tearDown();

        if (defaultChannel_ != null)
        {
            defaultChannel_.destroy();
        }
    }


    public ORB getClientORB() {
        return getSetup().getClientORB();
    }


    public EventChannel getDefaultChannel() throws Exception
    {
        if (defaultChannel_ == null)
        {
            defaultChannel_ = getFactory().create_channel(new Property[0],
                              new Property[0],
                              new IntHolder() );
        }

        return defaultChannel_;
    }


    public ORB getORB()
    {
        return setup_.getORB();
    }


    public POA getPOA()
    {
        return setup_.getPOA();
    }


    public DynAnyFactory getDynAnyFactory() throws Exception {
        return DynAnyFactoryHelper.narrow(getORB().resolve_initial_references("DynAnyFactory"));
    }


    public Configuration getConfiguration()
    {
        return ((org.jacorb.orb.ORB)getORB()).getConfiguration();
    }


    public NotificationTestUtils getTestUtils()
    {

        return setup_.getTestUtils();
    }


    public EventChannelFactory getFactory()
    {
        try
        {
            return setup_.getFactoryServant().getEventChannelFactory();
        }
        catch (Exception e)
        {
            e.printStackTrace();

            throw new RuntimeException(e.getMessage());
        }
    }


    private NotificationTestCaseSetup getSetup()
    {
        return setup_;
    }

    protected ChannelContext getChannelContext() {
        return getSetup().getChannelContext();
    }


    public static Test suite(Class clazz) throws Exception
    {
        return suite("TestSuite defined in Class: " + clazz.getName(), clazz);
    }


    public static Test suite(String suiteName, Class clazz) throws Exception
    {
        return suite(suiteName, clazz, "test");
    }


    public static Test suite(String suiteName, Class clazz, String testMethodPrefix) throws Exception
    {
        TestSuite _suite = new TestSuite(suiteName);

        NotificationTestCaseSetup _setup =
            new NotificationTestCaseSetup(_suite);

        String[] _methodNames = TestUtils.getTestMethods(clazz, testMethodPrefix);

        addToSuite(_suite, _setup, clazz, _methodNames);

        return _setup;
    }


    private static void addToSuite(TestSuite suite,
                                   NotificationTestCaseSetup setup,
                                   Class clazz,
                                   String[] testMethods) throws Exception
    {
        Constructor _ctor =
            clazz.getConstructor(new Class[] {String.class, NotificationTestCaseSetup.class});

        for (int x = 0; x < testMethods.length; ++x)
        {
            suite.addTest((Test)_ctor.newInstance(new Object[] {testMethods[x], setup}));
        }
    }


    public Any toAny(String s) {
        Any a = getORB().create_any();

        a.insert_string(s);

        return a;
    }


    public Any toAny(int i) {
        Any a = getORB().create_any();

        a.insert_long(i);

        return a;
    }


    public Any toAny(Property[] props) throws Exception {
        Any _any = getORB().create_any();

        PropertySeqHelper.insert(_any, props);

        return _any;
    }

}
