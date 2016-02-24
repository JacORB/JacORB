package org.jacorb.test.harness;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.rules.Timeout;
import org.junit.runner.Description;
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

    @Rule
    public TestRule watcher = new TestWatcher()
    {
        @Override
        protected void starting(Description description)
        {
            TestUtils.getLogger().debug("Starting test: {}:{}", description.getClassName(), description.getMethodName());
        }
    };

    @ClassRule
    public static Timeout globalTimeout = new Timeout(480000); // 8 minutes max per class tested

    @Rule
    public Timeout testTimeout = new Timeout(150000); // 2.5 minutes max per method tested

    protected ORB orb;
    protected POA rootPOA;
    protected Properties orbProps = new Properties();

    private ArrayList<ORBTestCase> otherORBs = new ArrayList<ORBTestCase>();

    public ORBTestCase()
    {
        orbProps.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        orbProps.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");

        if (TestUtils.verbose)
        {
            orbProps.setProperty("jacorb.log.default.verbosity", "4");
        }
        else
        {
            orbProps.setProperty("jacorb.log.default.verbosity", "0");
        }
        if (TestUtils.isSSLEnabled)
        {
            TestUtils.getLogger().debug("ORBTestCase SSL enabled");

            // In this case we have been configured to run all the tests
            // in SSL mode. For simplicity, we will use the demo/ssl keystore
            // and properties (partly to ensure that they always work)
            Properties cp;
            try
            {
                cp = CommonSetup.loadSSLProps("jsse_client_props", "jsse_client_ks");
                orbProps.putAll(cp);
            }
            catch (IOException e)
            {
                assertFalse("Exception was thrown " + e, true);
            }
        }
    }

    @Before
    public void ORBSetUp() throws Exception
    {
        patchORBProperties(orbProps);

        String mn = (name.getMethodName() == null ?
                orbProps.getProperty(ClientServerSetup.SERVANT_NAME, "") : name.getMethodName());

        TestUtils.getLogger().debug("ORBTestCase::setUp for " + mn);

        orb = ORB.init(new String[] { "-ORBID" , mn }, orbProps);

        initialisePOA();
    }

    // Split out POA initialisation so getAnotherORB does not automatically initialise it.
    void initialisePOA () throws Exception
    {
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
        TestUtils.getLogger().debug("ORBTestCase::patchORBProperties " + props);
    }

    @After
    public void ORBTearDown() throws Exception
    {
        TestUtils.getLogger().debug("ORBTestCase::tearDown");

        // Null check because its possible a POA initialise could have failed.
        if (rootPOA != null)
        {
            rootPOA.destroy(true, true);
            rootPOA = null;
        }

        // Null check because its possible an ORB initialise could have failed.
        if (orb != null)
        {
            orb.shutdown(true);
            orb = null;
        }

        Iterator<ORBTestCase> i = otherORBs.iterator();
        while (i.hasNext())
        {
            ORBTestCase otc = i.next();
            otc.ORBTearDown();
            i.remove();
        }

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


    /**
     * Returns another client ORB. The test case will automatically shut it down at
     * the end of the test.
     *
     * @param override to supplement the ORB configuration.
     * @return a pre-configured ORB to use.
     * @throws Exception
     */
    public ORB getAnotherORB(final Properties override) throws Exception
    {
        ORBTestCase otc = new ORBTestCase ()
        {
            @Override
            protected void patchORBProperties (Properties p)
            {
                if (override != null)
                {
                    p.putAll(override);
                }
            }

            @Override
            void initialisePOA () throws Exception
            {
                // Don't resolve the RootPOA
            }
        };

        otherORBs.add(otc);

        otc.ORBSetUp();

        return otc.orb;
    }
}
