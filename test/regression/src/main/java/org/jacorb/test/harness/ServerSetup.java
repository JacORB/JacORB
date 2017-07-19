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

package org.jacorb.test.harness;

import static org.junit.Assert.fail;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.jacorb.test.harness.launch.Launcher;

/**
 * @author Alphonse Bendt
 * @author Nick Cross
 */
public class ServerSetup
{
    private static class ProcessShutdown extends Thread
    {
        // only hold a weak reference to the process to
        // allow it to be gc'ed
        private final WeakReference<Process> processRef;

        public ProcessShutdown(Process process)
        {
            processRef = new WeakReference<Process>(process);
        }

        @Override
        public void run()
        {
            Process process = processRef.get();
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
    private final String testServer;

    private Process serverProcess;
    private StreamListener outListener, errListener;
    private String serverIOR;

    protected String outName = "OUT";
    protected String errName = "ERR";

    private final List<String> serverArgs = new ArrayList<String>();

    private String serverIORFailedMesg;


    /**
     * Server setup with explicit server class/servant name and optional properties.
     * @param testServer
     * @param servantName
     * @param optionalProperties
     * @throws IOException
     */
    public ServerSetup(String testServer, String servantName, Properties optionalProperties) throws IOException
    {
        this(testServer, servantName, null , optionalProperties);
    }

    public ServerSetup(String testServer, String servantName, String[] testServantArgs, Properties optionalProperties) throws IOException
    {
        this.testServer = getTestServer(testServer);
        this.servantName = servantName;

        serverArgs.add(servantName);

        if (TestUtils.verbose)
        {
            serverOrbProperties.setProperty("jacorb.log.default.verbosity", "4");
        }
        else
        {
            serverOrbProperties.setProperty("jacorb.log.default.verbosity", "0");
        }
        if (TestUtils.isSSLEnabled)
        {
            // In this case we have been configured to run all the tests
            // in SSL mode. For simplicity, we will use the demo/ssl keystore
            // and properties (partly to ensure that they always work)
            Properties serverProps = CommonSetup.loadSSLProps("jsse_server_props", "jsse_server_ks");

            serverOrbProperties.putAll(serverProps);
        }

        if (optionalProperties != null)
        {
            serverOrbProperties.putAll(optionalProperties);
        }

        for (int i = 0; testServantArgs != null && i < testServantArgs.length; i++)
        {
            serverArgs.add(testServantArgs[i]);
        }
    }

    /**
     * Default server setup
     * @param servantName
     * @throws IOException
     */
    public ServerSetup(String servantName) throws IOException
    {
        this(null, servantName, null);
    }

    private String getTestServer(String optionalTestServer)
    {
        if (optionalTestServer == null)
        {
            return "org.jacorb.test.harness.TestServer";
        }
        return optionalTestServer;
    }

    public void setUp()
    {
        Properties serverProperties = new Properties();
        serverProperties.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        serverProperties.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
        serverProperties.put ("jacorb.implname", servantName);
        serverProperties.putAll (serverOrbProperties);

        final String prefix = "jacorb.test.serverproperty.";
        final Iterator<String> i = System.getProperties().stringPropertyNames().iterator();

        while(i.hasNext())
        {
            String key = i.next();

            if (!key.startsWith(prefix))
            {
                continue;
            }

            String value = System.getProperty(key);

            String propName = key.substring(prefix.length());

            serverProperties.setProperty(propName, value);
        }

        final Launcher launcher = getLauncher(System.getProperty("java.class.path"),
                                        serverProperties,
                                        getTestServerMain(),
                                        serverArgs.toArray(new String[serverArgs.size()]));

        serverProcess = launcher.launch();

        // add a shutdown hook to ensure that the server process
        // is shutdown even if this JVM is going down unexpectedly
        Runtime.getRuntime().addShutdownHook(new ProcessShutdown(serverProcess));

        outListener = new StreamListener (serverProcess.getInputStream(), servantName + '-' + outName);
        errListener = new StreamListener (serverProcess.getErrorStream(), servantName + '-' + errName);
        outListener.start();
        errListener.start();
        serverIOR = outListener.getIOR(TestUtils.timeout);
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

                serverIORFailedMesg = "could not access IOR for Server.\nServant: " + servantName + "\nTimeout: " + TestUtils.timeout + " millis.\nThis maybe caused by: " + exc + '\n' + details;
            }
            fail(serverIORFailedMesg);
        }

        return serverIOR;
    }

    public String getTestServerMain()
    {
        return testServer;
    }

    private String dumpStreamListener()
    {
        StringBuffer details = new StringBuffer();
        details.append(outListener.toString());
        details.append(errListener.toString());
        return details.toString();
    }

    /**
     * Returns a launcher for the specified JacORB version.
     * If coverage is true, sets up the launcher to that
     * coverage information will be gathered.
     * @param classpath
     * @param properties
     * @param mainClass
     * @param processArgs
     */
    private Launcher getLauncher (String classpath,
                                 Properties properties,
                                 String mainClass,
                                 String[] processArgs)
    {
        String home = null;

        try
        {
            home = locateHome(properties);
        }
        catch(Exception e)
        {
            TestUtils.getLogger().debug("unable to locate JacORB home. classpath will be only be set using the System property java.class.path: " + e.getMessage());
        }

        final Properties props = new Properties();

        if (properties != null)
        {
            props.putAll(properties);
        }

        Iterator<Object> it = System.getProperties().keySet().iterator();
        while (it.hasNext())
        {
            String key = (String)it.next();
            if (key.startsWith("jacorb.test"))
            {
                props.put(key, System.getProperty(key));
            }
        }

        // Extract any VM arguments e.g. java agent (used by coverage) etc and pass
        // them to the server launcher so it uses the same parameters.
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> jvmArgs = new ArrayList<String>();
        Iterator<String> s = runtimeMxBean.getInputArguments().iterator();
        while (s.hasNext())
        {
            String jvmArg = s.next();
            // Don't pass Xboot or -D - they are handled separately.
            if ( ! jvmArg.startsWith("-Xbootclasspath") && ! jvmArg.startsWith("-D") && !jvmArg.contains("jdwp"))
            {
                jvmArgs.add(jvmArg);
            }
        }

        try
        {
            Launcher launcher = new Launcher();

            launcher.setClasspath(classpath);
            launcher.setMainClass(mainClass);
            launcher.setArgs(processArgs);
            launcher.setVmArgs(jvmArgs);

            if (home != null)
            {
                launcher.setJacorbHome(new File(home));
            }
            launcher.setProperties(props);

            launcher.init();

            return launcher;
        }
        catch (Exception e)
        {
            StringWriter out = new StringWriter();
            e.printStackTrace(new PrintWriter(out));
            throw new IllegalArgumentException(out.toString());
        }
    }

    private String locateHome(Properties props)
    {
        String jacorbHome = props.getProperty("jacorb.home");

        if (jacorbHome == null)
        {
            jacorbHome = System.getProperty("jacorb.home");
        }

        if (jacorbHome != null)
        {
            return jacorbHome;
        }

        String testHome = props.getProperty("jacorb.test.home");

        if (testHome == null)
        {
            testHome = System.getProperty("jacorb.test.home");
        }

        if (testHome == null)
        {
            testHome = TestUtils.testHome();
        }

        if (testHome == null)
        {
            throw new RuntimeException("cannot determine jacorb.test.home");
        }

        return new File(testHome).getParentFile().getParentFile().toString();
    }
}
