/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2006 The JacORB project.
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

package org.jacorb.test.common;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import junit.extensions.TestSetup;
import junit.framework.Test;
import org.jacorb.test.common.launch.JacORBLauncher;
import org.jacorb.test.common.launch.Launcher;

/**
 * @author Alphonse Bendt
 */
public class ServerSetup extends TestSetup
{
    private static final Comparator comparator = new JacORBVersionComparator();

    private static class ProcessShutdown extends Thread
    {
        // only hold a weak reference to the process to
        // allow it to be gc'ed
        private final WeakReference processRef;

        public ProcessShutdown(Process process)
        {
            processRef = new WeakReference(process);
        }

        public void run()
        {
            Process process = (Process) processRef.get();
            if (process != null)
            {
                try
                {
                    process.destroy();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private final Properties serverOrbProperties = new Properties();

    private final String servantName;
    private final long testTimeout;
    private final String testServer;

    private Process serverProcess;
    private StreamListener outListener, errListener;
    private String serverIOR;

    protected String outName = "OUT";
    protected String errName = "ERR";

    protected final List<String> serverArgs = new ArrayList();

    private String serverIORFailedMesg;

    public ServerSetup(Test test, String testServer, String servantName, Properties optionalProperties)
    {
        this(test, testServer, new String [] { servantName } , optionalProperties);
    }

    public ServerSetup(Test test, String testServer, String[] testServantArgs, Properties optionalProperties)
    {
        super(test);

        this.testServer = getTestServer(testServer);
        this.servantName = testServantArgs[0];

        if (TestUtils.verbose)
        {
            serverOrbProperties.setProperty("jacorb.log.default.verbosity", "4");
        }
        else
        {
            serverOrbProperties.setProperty("jacorb.log.initializer", MyNullLoggerInitializer.class.getName());
        }

        if (optionalProperties != null)
        {
            serverOrbProperties.putAll(optionalProperties);
        }

        testTimeout = getTestServerTimeout2();

        for (int i = 0; i < testServantArgs.length; i++)
        {
            serverArgs.add(testServantArgs[i]);
        }
    }

    public ServerSetup(Test test, String servantName)
    {
        this(test, null, servantName, null);
    }


    /**
     * how long should we wait for a testserver to come up?
     */
    public static long getTestServerTimeout()
    {
        return Long.getLong("jacorb.test.timeout.server", new Long(120000)).longValue();
    }

    private long getTestServerTimeout2()
    {
        return Long.parseLong(serverOrbProperties.getProperty("jacorb.test.timeout.server", Long.toString(getTestServerTimeout())));
    }

    private String getTestServer(String optionalTestServer)
    {
        if (optionalTestServer == null)
        {
            String serverVersion = System.getProperty("jacorb.test.server.version", "cvs");
            if (comparator.compare (serverVersion, "2.2") >= 0)
            {
                return "org.jacorb.test.common.TestServer";
            }
            return "org.jacorb.test.common.TestServer_before_2_2";
        }
        return optionalTestServer;
    }


    public void setUp() throws Exception
    {
        initSecurity();

        final String serverVersion = System.getProperty ("jacorb.test.server.version", "cvs");
        final boolean coverage = TestUtils.getSystemPropertyAsBoolean("jacorb.test.coverage", false);

        Properties serverProperties = new Properties();
        serverProperties.setProperty
        (
            "jacorb.log.default.verbosity",
            (TestUtils.verbose ? "4" : "0")
        );
        serverProperties.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        serverProperties.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
        serverProperties.put ("jacorb.implname", servantName);

        serverProperties.putAll (serverOrbProperties);


        final String prefix = "jacorb.test.serverproperty.";
        final Iterator i = System.getProperties().keySet().iterator();

        while(i.hasNext())
        {
            String key = (String) i.next();

            if (!key.startsWith(prefix))
            {
                continue;
            }

            String value = System.getProperty(key);

            String propName = key.substring(prefix.length());

            serverProperties.setProperty(propName, value);
        }

        URL launcherConfiguration = getClass().getResource(System.getProperty("jacorb.test.launcher.configuration", "/test.properties"));
        TestUtils.log("using launcherConfiguration: " + launcherConfiguration);

        assertNotNull("unable to access launcher configuration", launcherConfiguration);
        final InputStream in = launcherConfiguration.openStream();
        JacORBLauncher launcherFactory;

        Properties launcherProps = System.getProperties();

        patchLauncherProps(launcherProps);

        try
        {
            launcherFactory = new JacORBLauncher(in, launcherProps);
        }
        finally
        {
            in.close();
        }

        final Launcher launcher =
            launcherFactory.getLauncher(serverVersion,
                                        coverage,
                                        System.getProperty("java.class.path"),
                                        serverProperties,
                                        getTestServerMain(),
                                        getServerArgs());

        serverProcess = launcher.launch();

        // add a shutdown hook to ensure that the server process
        // is shutdown even if this JVM is going down unexpectedly
        Runtime.getRuntime().addShutdownHook(new ProcessShutdown(serverProcess));

        outListener = new StreamListener (serverProcess.getInputStream(), servantName + '-' + outName);
        errListener = new StreamListener (serverProcess.getErrorStream(), servantName + '-' + errName);
        outListener.start();
        errListener.start();
        serverIOR = outListener.getIOR(testTimeout);
    }

    protected void patchLauncherProps(Properties launcherProps)
    {
    }

    protected String[] getServerArgs()
    {
        return (String[])serverArgs.toArray(new String[serverArgs.size()]);
    }

    public void tearDown() throws Exception
    {
        if (serverProcess != null)
        {
            outListener.setDestroyed();
            errListener.setDestroyed();
            outListener = null;
            errListener = null;

            serverProcess.destroy();
            serverProcess.waitFor();
            serverProcess = null;

            serverIOR = null;

            Thread.sleep(1000);
        }
    }

    public String getServerIOR()
    {
        if (serverIOR == null)
        {
            if (serverIORFailedMesg == null)
            {
                String exc = errListener.getException(1000);

                String details = dumpStreamListener();

                serverIORFailedMesg = "could not access IOR for Server.\nServant: " + servantName + "\nTimeout: " + testTimeout + " millis.\nThis maybe caused by: " + exc + '\n' + details;
            }
            fail(serverIORFailedMesg);
        }

        return serverIOR;
    }

    public String getServerIorOrNull()
    {
        return serverIOR;
    }

    protected String getTestServerMain()
    {
        return testServer;
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

            Properties serverProps = CommonSetup.loadSSLProps("jsse_server_props", "jsse_server_ks");

            serverOrbProperties.putAll(serverProps);
        }
    }

    /**
     * check if SSL testing is disabled for this setup
     */
    public boolean isSSLEnabled()
    {
        final String sslProperty = serverOrbProperties.getProperty("jacorb.test.ssl", System.getProperty("jacorb.test.ssl"));
        final boolean useSSL = TestUtils.getStringAsBoolean(sslProperty);

        return useSSL && !isPropertySet(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY);
    }

    private boolean isPropertySet(String property)
    {
        return TestUtils.getStringAsBoolean(serverOrbProperties.getProperty(property, "false"));
    }

    private String dumpStreamListener()
    {
        StringBuffer details = new StringBuffer();
        details.append(outListener.toString());
        details.append(errListener.toString());
        return details.toString();
    }

    public void patchServerProperties (Properties serverProperties)
    {
        if (serverProperties != null && serverProperties.size () > 0)
        {
            serverOrbProperties.putAll (serverProperties);
        }
    }
}
