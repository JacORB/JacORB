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

import java.util.Properties;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.jacorb.config.*;
import org.jacorb.notification.container.PicoContainerFactory;
import org.jacorb.test.common.TestUtils;
import org.jacorb.test.ir.IFRServerSetup;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Repository;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoInitializationException;
import org.picocontainer.PicoIntrospectionException;
import org.picocontainer.defaults.AbstractComponentAdapter;

/**
 * helper class for internal notification service tests.
 * this class sets up basic parts of the notification
 * service internals to support writing Unit tests.
 *
 * @author Alphonse Bendt
 */

public class NotificationTestCaseSetup extends TestSetup
{
    private MutablePicoContainer container_;

    private NotificationTestUtils testUtils_;

    private ORB clientORB_;

    private ORB orb_;

    private final Properties orbProps = new Properties();

	private IFRServerSetup ifrServerSetup;

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

    public final void setUp() throws Exception
    {
        super.setUp();

        orb_ = ORB.init(new String[0], orbProps);

        POAHelper.narrow(orb_.resolve_initial_references("RootPOA")).the_POAManager().activate();

        container_ = PicoContainerFactory.createRootContainer((org.jacorb.orb.ORB) orb_);
        container_.unregisterComponent(Repository.class);
        container_.registerComponent(new AbstractComponentAdapter(Repository.class, Repository.class) {

			public Object getComponentInstance(PicoContainer picocontainer) throws PicoInitializationException, PicoIntrospectionException
			{
				try
				{
					return getRepository();
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public void verify(PicoContainer picocontainer) throws PicoIntrospectionException
			{
			}
			});

        testUtils_ = new NotificationTestUtils(getORB());

        clientORB_ = ORB.init(new String[] {}, null);
        POAHelper.narrow(clientORB_.resolve_initial_references("RootPOA")).the_POAManager()
                .activate();
    }

    public final void tearDown() throws Exception
    {
        container_.dispose();

        orb_.shutdown(true);

        clientORB_.shutdown(true);

        if (ifrServerSetup != null)
        {
        	ifrServerSetup.tearDown();
        	ifrServerSetup = null;
        }

        super.tearDown();
    }

    public ORB getClientORB()
    {
        return clientORB_;
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

    public Repository getRepository() throws Exception
    {
    	if (ifrServerSetup == null)
    	{
    		ifrServerSetup = new IFRServerSetup(this, TestUtils.testHome() + "/idl/TypedNotification.idl", null, null);
    		ifrServerSetup.setUp();
    	}

        return ifrServerSetup.getRepository();
    }
}