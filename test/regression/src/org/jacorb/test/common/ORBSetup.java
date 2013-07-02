package org.jacorb.test.common;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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
 */

import java.io.IOException;
import java.util.Properties;
import junit.extensions.TestSetup;
import junit.framework.Test;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;


/**
 * @author Alphonse Bendt
 */
public class ORBSetup extends TestSetup
{
    private final Properties orbProps = new Properties();
    private ORB orb;
    protected POA rootPOA;

    public ORBSetup(Test test, Properties optionalProperties)
    {
        super(test);

    	orbProps.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
    	orbProps.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");

        if (optionalProperties != null)
        {
            orbProps.putAll(optionalProperties);
        }
    }

    public ORBSetup(Test test)
    {
        this(test, null);
    }

    public void setUp() throws Exception
    {
        initSecurity();

        orb = ORB.init (new String[0], orbProps );
        rootPOA = POAHelper.narrow
                          ( orb.resolve_initial_references( "RootPOA" ) );
        rootPOA.the_POAManager().activate();
    }

    public void tearDown() throws Exception
    {
        rootPOA.destroy(false, true);
        rootPOA = null;

        orb.shutdown(true);
        orb = null;
    }

    public ORB getORB()
    {
        return orb;
    }

    public POA getRootPOA()
    {
        return rootPOA;
    }

    /**
     * <code>initSecurity</code> adds security properties if so configured
     * by the environment. It is possible to turn this off for selected tests
     * either by overriding this method or by setting properties for checkProperties
     * to handle.
     *
     * @exception IOException if an error occurs
     */
    protected void initSecurity() throws IOException
    {
        if (isSSLEnabled())
        {
            // In this case we have been configured to run all the tests
            // in SSL mode. For simplicity, we will use the demo/ssl keystore
            // and properties (partly to ensure that they always work)
            Properties clientProps = CommonSetup.loadSSLProps("jsse_client_props", "jsse_client_ks");

            orbProps.putAll(clientProps);
        }
    }

    /**
     * check if SSL testing is disabled for this setup
     */
    public boolean isSSLEnabled()
    {
        final String sslProperty = orbProps.getProperty("jacorb.test.ssl", System.getProperty("jacorb.test.ssl"));

        final boolean useSSL = TestUtils.getStringAsBoolean(sslProperty);

        return useSSL && !isPropertySet(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY);
    }

    private boolean isPropertySet(String property)
    {
        return TestUtils.getStringAsBoolean(orbProps.getProperty(property, "false"));
    }

    public void patchORBProperties (Properties clientProperties)
    {
        if (clientProperties != null && clientProperties.size () > 0)
        {
            orbProps.putAll (clientProperties);
        }
    }
}
