package org.jacorb.test.common.launch;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 2000-2012 Gerald Brose / The JacORB Team.
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.jacorb.test.common.TestUtils;

/**
 * A JacORBLauncher runs a given main class against a specified
 * JacORB version.  The class JacORBLauncher itself is an abstract
 * superclass for specific launchers that work with a given JacORB
 * installation.  To use, call JacORBLauncher.getLauncher(), then
 * invoke the launch() method on the resulting object.
 *
 * @author Andre Spiegel spiegel@gnu.org
 */
public class JacORBLauncher
{
    private final static Map LAUNCHER_SHORTCUTS;

    static
    {
        Map launchers = new HashMap();

        launchers.put("direct", "org.jacorb.test.common.launch.DirectLauncher");
        launchers.put("jaco", "org.jacorb.test.common.launch.JacoLauncher");
        launchers.put("tao", "org.jacorb.test.common.launch.TAOLauncher");

        LAUNCHER_SHORTCUTS = Collections.unmodifiableMap(launchers);
    }

    private final Properties testProperties;
    private List       versions;
    private ClassLoader classLoader;

    private String propertyPrefix;

    public JacORBLauncher(InputStream in, Properties additionalProps) throws IOException
    {
        testProperties = getTestProperties(in, additionalProps);
    }

    public JacORBLauncher(InputStream in) throws IOException
    {
        testProperties = getTestProperties(in, System.getProperties());
    }

    public void setClassLoader(ClassLoader classLoader)
    {
        this.classLoader = classLoader;
    }

    public void setPropertyPrefix(String prefix)
    {
        propertyPrefix = prefix;
    }

    /**
     * Loads and returns the properties defined in the file test.properties
     * in the regression suite.
     * @throws IOException
     */
    private Properties getTestProperties(InputStream in, Properties additionalProps) throws IOException
    {
        Properties testProperties = new Properties();
        testProperties.load (in);

        final Iterator i = additionalProps.keySet().iterator();
        while(i.hasNext())
        {
            String key = (String) i.next();

            if (key.startsWith("jacorb.test.jacorb_version"))
            {
                final String value = additionalProps.getProperty(key);

                TestUtils.log("[JacORBLauncher] merge in " + key + "=" + value);
                testProperties.put(key, value);
            }
        }

        return testProperties;
    }

    /**
     * Returns a list of all the available JacORB versions.
     */
    private List getVersions()
    {
        if (versions == null)
        {
            versions = new ArrayList();
            for (int i=0; ; i++)
            {
                String key = "jacorb.test.jacorb_version." + i + ".id";
                String value = testProperties.getProperty(key);
                if (value == null)
                {
                    break;
                }
                versions.add (value);
            }
        }
        return versions;
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
    public Launcher getLauncher (String version,
                                 boolean useCoverage,
                                 String classpath,
                                 Properties properties,
                                 String mainClass,
                                 String[] processArgs)
    {
        int index = getVersions().indexOf(version);
        if (index == -1)
        {
            throw new IllegalArgumentException(
                    "Launcher version " + version + " not available. available: " + getVersions());
        }

        String home = null;

        try
        {
            home = locateHome(properties, version, index);
        }
        catch(Exception e)
        {
            TestUtils.log("unable to locate JacORB home. classpath will be only be set using the System property java.class.path: " + e.getMessage());
        }

        String launcherClassName = lookupLauncher(version, index);

        if (LAUNCHER_SHORTCUTS.containsKey(launcherClassName))
        {
            launcherClassName = (String) LAUNCHER_SHORTCUTS.get(launcherClassName);
        }

        final Properties props = new Properties();

        if (propertyPrefix != null)
        {
            for (Iterator i = System.getProperties().keySet().iterator(); i.hasNext(); )
            {
                String key = (String) i.next();

                if (key.startsWith(propertyPrefix))
                {
                    props.setProperty(key, System.getProperty(key));
                }
            }
        }

        addCustomProps(index, props, testProperties);

        if (properties != null)
        {
            props.putAll(properties);
        }

        try
        {
            final Class launcherClass;

            if (classLoader == null)
            {
                launcherClass = TestUtils.classForName(launcherClassName);
            }
            else
            {
                launcherClass = classLoader.loadClass(launcherClassName);
            }

            Launcher launcher = (Launcher) launcherClass.newInstance();

            if (launcher instanceof AbstractLauncher)
            {
                AbstractLauncher _launcher = (AbstractLauncher) launcher;

                _launcher.setClasspath(classpath);
                _launcher.setMainClass(mainClass);
                _launcher.setArgs(processArgs);
                _launcher.setUseCoverage(useCoverage);
                if (home != null)
                {
                    _launcher.setJacorbHome(new File(home));
                }
                _launcher.setProperties(props);

                _launcher.init();
            }

            return launcher;
        }
        catch (Exception e)
        {
            StringWriter out = new StringWriter();
            e.printStackTrace(new PrintWriter(out));
            throw new IllegalArgumentException(out.toString());
        }
    }

    private String lookupLauncher(String version, int index)
    {
        final String key = "jacorb.test.jacorb_version." + index + ".launcher";
        String launcherClassName = testProperties.getProperty(key);
        if (launcherClassName == null)
        {
            throw new IllegalArgumentException("No launcher class defined for JacORB version " + version);
        }
        return launcherClassName;
    }

    private String locateHome(Properties props, String version, int index)
    {
        String key = "jacorb.test.jacorb_version." + index + ".home";
        String home = testProperties.getProperty(key);
        if (home == null)
        {
            if (version.equals("cvs"))
            {
                home = getCVSHome(props);
            }
        }
        else if ("cvs".equals(home))
        {
            home = getCVSHome(props);
        }
        return home;
    }

    private static void addCustomProps(int index, Properties propsForProcess, Properties testProps)
    {
        final String prefix = "jacorb.test.jacorb_version." + index + ".property";
        final Iterator i = testProps.keySet().iterator();

        while(i.hasNext())
        {
            String key = (String) i.next();

            if (!key.startsWith(prefix))
            {
                continue;
            }

            String value = testProps.getProperty(key);

            String propName = key.substring(prefix.length() + 1);

            propsForProcess.setProperty(propName, value);
        }
    }

    private static String getCVSHome(Properties props)
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
