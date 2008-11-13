package org.jacorb.test.common.launch;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 2005  Gerald Brose.
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
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
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
 * @version $Id$
 */
public abstract class JacORBLauncher
{
    private final static Map LAUNCHER_SHORTCUTS;

    static
    {
        Map launchers = new HashMap();

        launchers.put("direct", DirectLauncher.class);
        launchers.put("jaco", JacoLauncher.class);
        launchers.put("tao", TAOLauncher.class);

        LAUNCHER_SHORTCUTS = Collections.unmodifiableMap(launchers);
    }

    private static Properties testProperties;
    private static List       versions;

    /**
     * Loads and returns the properties defined in the file test.properties
     * in the regression suite.
     */
    private synchronized static Properties getTestProperties()
    {
        if (testProperties == null)
        {          
            InputStream in = null;
            try
            {
                File d = new File (TestUtils.testHome(), "resources");
                File f = new File (d, "test.properties");
                if (f.exists())
                {
                    in = new FileInputStream (f);
                }
                else
                {
                    final ClassLoader cl;

                    cl = JacORBLauncher.class.getClassLoader();

                    URL url = cl.getResource("/test.properties");

                    if (url == null)
                    {
                        url = JacORBLauncher.class.getResource("/test.properties");
                    }

                    if (url == null)
                    {
                        throw new IllegalArgumentException("cannot locate test.properties!");
                    }
                    TestUtils.log("using test.properties from " + url);
                }

                try
                {
                    testProperties = new Properties();
                    testProperties.load (in);
                }
                finally
                {
                    in.close();
                }
            }
            catch (IOException ex)
            {
                testProperties = null;
                throw new RuntimeException (ex);
            }
        }
        return testProperties;
    }

    /**
     * Returns a list of all the available JacORB versions.
     */
    private synchronized static List getVersions()
    {
        if (versions == null)
        {
            versions = new ArrayList();
            for (int i=0; ; i++)
            {
                String key = "jacorb.test.jacorb_version." + i + ".id";
                String value = getTestProperties().getProperty(key);
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
    public static Launcher getLauncher (String version,
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

        final String home = locateHome(version, index);

        final String launcherClassName = lookupLauncher(version, index);

        final Properties props = new Properties();

        addCustomProps(index, props, getTestProperties());

        props.putAll(properties);

        try
        {
            final Class launcherClass;

            if (LAUNCHER_SHORTCUTS.containsKey(launcherClassName))
            {
                launcherClass = (Class) LAUNCHER_SHORTCUTS.get(launcherClassName);
            }
            else
            {
                launcherClass = Class.forName (launcherClassName);
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

    private static String lookupLauncher(String version, int index)
    {
        final String key = "jacorb.test.jacorb_version." + index + ".launcher";
        String launcherClassName = getTestProperties().getProperty(key);
        if (launcherClassName == null)
        {
            throw new IllegalArgumentException("No launcher class defined for JacORB version " + version);
        }
        return launcherClassName;
    }

    private static String locateHome(String version, int index)
    {
        String key = "jacorb.test.jacorb_version." + index + ".home";
        String home = getTestProperties().getProperty(key);
        if (home == null)
        {
            if (version.equals("cvs"))
            {
                home = getCVSHome();
            }
        }
        else if ("cvs".equals(home))
        {
            home = getCVSHome();
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

    private static String getCVSHome()
    {
        final String testHome = TestUtils.testHome();

        if (testHome == null)
        {
            throw new IllegalArgumentException("need to set testhome (-Djacorb.test.home)");
        }

        return new File(testHome).getParentFile().getParentFile().toString();
    }
}
