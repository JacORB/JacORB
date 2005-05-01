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

import java.util.Properties;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.apache.avalon.framework.configuration.Configuration;
import org.jacorb.notification.AbstractChannelFactory;
import org.jacorb.notification.container.PicoContainerFactory;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.picocontainer.MutablePicoContainer;

/**
 * @author Alphonse Bendt
 */

public class NotificationTestCaseSetup extends TestSetup
{
    private MutablePicoContainer container_;

    private Thread orbThread_;

    private NotificationTestUtils testUtils_;

    private AbstractChannelFactory eventChannelFactory_;

    private ORB clientORB_;

    private ORB orb_;

    // //////////////////////////////////////

    public NotificationTestCaseSetup(Test suite) throws Exception
    {
        super(suite);
    }

    // //////////////////////////////////////

    public NotificationTestUtils getTestUtils()
    {
        return testUtils_;
    }

    public void setUp() throws Exception
    {
        super.setUp();

        orb_ = ORB.init(new String[0], null);
        POAHelper.narrow(orb_.resolve_initial_references("RootPOA")).the_POAManager().activate();

        container_ = PicoContainerFactory.createRootContainer((org.jacorb.orb.ORB) orb_);

        testUtils_ = new NotificationTestUtils(getORB());

        orbThread_ = new Thread(new Runnable()
        {
            public void run()
            {
                getORB().run();
            }
        });

        orbThread_.setDaemon(true);

        orbThread_.start();

        clientORB_ = ORB.init(new String[] {}, null);
        POAHelper.narrow(clientORB_.resolve_initial_references("RootPOA")).the_POAManager()
                .activate();

        Thread clientOrbThread = new Thread(new Runnable()
        {
            public void run()
            {
                getClientORB().run();
            }
        }, "JUnit-Client-ORB-Runner");

        clientOrbThread.setDaemon(true);

        clientOrbThread.start();
    }

    public void tearDown() throws Exception
    {
        if (eventChannelFactory_ != null)
        {
            eventChannelFactory_.dispose();
        }

        container_.dispose();

        orb_.shutdown(true);

        clientORB_.shutdown(true);

        super.tearDown();
    }

    public ORB getClientORB()
    {
        return clientORB_;
    }

    public AbstractChannelFactory getFactoryServant() throws Exception
    {
        if (eventChannelFactory_ == null)
        {
            eventChannelFactory_ = AbstractChannelFactory.newFactory(new Properties());
        }

        return eventChannelFactory_;
    }

    public ORB getORB()
    {
        return (ORB) container_.getComponentInstanceOfType(ORB.class);
    }

    public POA getPOA()
    {
        return (POA) container_.getComponentInstanceOfType(POA.class);
    }

    public MutablePicoContainer getPicoContainer()
    {
        return container_;
    }

    public Configuration getConfiguration()
    {
        return (Configuration) container_.getComponentInstanceOfType(Configuration.class);
    }
}