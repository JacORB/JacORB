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

import java.util.Properties;
import org.jacorb.config.Configuration;
import org.jacorb.notification.container.PicoContainerFactory;
import org.jacorb.test.harness.ORBTestCase;
import org.jacorb.test.harness.TestUtils;
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

public class NotificationTestCaseSetup extends ORBTestCase
{
    private MutablePicoContainer container_;

    private NotificationTestUtils testUtils_;

    private ORB serverORB;

	private IFRServerSetup ifrServerSetup;

    public NotificationTestUtils getTestUtils()
    {
        return testUtils_;
    }

    @Override
    protected void patchORBProperties(Properties props) throws Exception
    {
        props.setProperty("ORBInitRef.InterfaceRepository", getRepository().toString());
    }

    public NotificationTestCaseSetup() throws Exception
    {
        ifrServerSetup = new IFRServerSetup(TestUtils.testHome() + "/src/test/idl/TypedNotification.idl", null, null);

        ORBSetUp();

        serverORB = this.getAnotherORB(orbProps);

        POAHelper.narrow(serverORB.resolve_initial_references("RootPOA")).the_POAManager().activate();

        container_ = PicoContainerFactory.createRootContainer((org.jacorb.orb.ORB) serverORB);
        container_.unregisterComponent(Repository.class);
        container_.registerComponent(new AbstractComponentAdapter(Repository.class, Repository.class) {

			@Override
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

			@Override
            public void verify(PicoContainer picocontainer) throws PicoIntrospectionException
			{
			}
			});

        testUtils_ = new NotificationTestUtils(getServerORB());

        POAHelper.narrow(orb.resolve_initial_references("RootPOA")).the_POAManager()
                .activate();
    }

    public final void tearDown() throws Exception
    {
        container_.dispose();

        serverORB.shutdown(true);

        if (ifrServerSetup != null)
        {
        	ifrServerSetup.tearDown();
        	ifrServerSetup = null;
        }
    }

    public ORB getClientORB()
    {
        return orb;
    }

    public ORB getServerORB()
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
       return ifrServerSetup.getRepository();
    }
}