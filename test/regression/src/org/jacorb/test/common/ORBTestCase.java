package org.jacorb.test.common;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2013 Gerald Brose / The JacORB Team.
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
 *   Software Foundation, 51 Franklin Street, Fifth Floor, Boston,
 *   MA 02110-1301, USA.
 */

import static org.junit.Assert.assertTrue;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

/**
 * An abstract class for those tests that need a local client ORB. The
 * ORB may be configured by implementing {@link ORBTestCase#patchORBProperties(Properties)}.
 * <p>
 * Normally the ORBID is set to the currently executing test. If {@link ORBTestCase#name}
 * does not return a test name then it searches for a property {@link ClientServerSetup#SERVANT_NAME}
 * in the properties. If that fails, the ID is set to "".
 * <p>
 * Note the ORB is setup and torn down once per test by the {@literal @}Before and {@literal @}After
 * annotations.
 *
 * @author Nick Cross
 */
public abstract class ORBTestCase
{
    @Rule
    public TestName name = new TestName();

    protected ORB orb;
    protected POA rootPOA;
    protected Properties orbProps = new Properties();

    public ORBTestCase()
    {
        orbProps.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        orbProps.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
    }

    @Before
    public void ORBSetUp() throws Exception
    {
        patchORBProperties(orbProps);

        String mn = (name.getMethodName() == null ?
                orbProps.getProperty(ClientServerSetup.SERVANT_NAME, "") : name.getMethodName());

        TestUtils.log ("ORBTestCase::setUp for " + mn);

        orb = ORB.init(new String[] { "-ORBID" , mn }, orbProps);
        rootPOA = POAHelper.narrow(orb.resolve_initial_references( "RootPOA" ));
        rootPOA.the_POAManager().activate();
    }

    /**
     * This method is called before the ORB is created and therefore allows ORB properties to
     * be modified.
     *
     * @param props properties to modify
     * @throws Exception if an error occurs
     */
    protected void patchORBProperties(Properties props) throws Exception
    {
        TestUtils.log("ORBTestCase::patchORBProperties " + props);
    }

    @After
    public void ORBTearDown() throws Exception
    {
        TestUtils.log("ORBTestCase::tearDown");

        assertTrue ("POA should not have been destroyed", rootPOA != null);
        rootPOA.destroy(true, true);
        rootPOA = null;

        assertTrue ("ORB should not have been destroyed", orb != null);
        orb.shutdown(true);
        orb = null;

        orbProps.clear();

        orbProps.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        orbProps.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
    }

    /**
     * Returns the ORB cast to a JacORB ORB.
     *
     * @return a JacORB ORB
     */
    public org.jacorb.orb.ORB getORB ()
    {
        assertTrue ("ORB should be a JacORB ORB", orb instanceof org.jacorb.orb.ORB);
        return (org.jacorb.orb.ORB)orb;
    }
}
