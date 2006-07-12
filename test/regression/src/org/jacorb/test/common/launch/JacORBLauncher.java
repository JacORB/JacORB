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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
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
    private static Properties testProperties;
    private static List       versions;

    protected String jacorbHome;
    protected boolean coverage;

    protected JacORBLauncher (String jacorbHome, boolean coverage)
    {
        this.jacorbHome = jacorbHome;
        this.coverage = coverage;
    }

    public abstract Process launch (String classpath,
                                    Properties props,
                                    String mainClass,
                                    String[] args);

    public String getJacorbHome()
    {
        return jacorbHome;
    }

    public String[] toStringArray (List list)
    {
        return ((String[])list.toArray (new String[list.size()]));
    }

    /**
     * Loads and returns the properties defined in the file test.properties
     * in the regression suite.
     */
    public synchronized static Properties getTestProperties()
    {
        if (testProperties == null)
        {
            try
            {
                InputStream in = new FileInputStream
                (
                    TestUtils.osDependentPath(TestUtils.testHome() + "/test.properties")
                );
                testProperties = new Properties();
                testProperties.load (in);
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
    public synchronized static List getVersions()
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
     */
    public static JacORBLauncher getLauncher (String version,
                                              boolean coverage)
    {
        if (version.startsWith("tao"))
        {
            return new TAOLauncher (null, false);
        }

        int index = getVersions().indexOf (version);
        if (index == -1)
        {
            throw new IllegalArgumentException(
                    "JacORB version " + version + " not available");
        }

        String key = "jacorb.test.jacorb_version." + index + ".home";
        String home = getTestProperties().getProperty(key);
        if (home == null)
        {
            if (version.equals("cvs"))
            {
                home = getCVSHome();
            }
            else
            {
                throw new IllegalArgumentException
                (
                    "No home directory for JacORB version " + version
                );
            }
        }
        key = "jacorb.test.jacorb_version." + index + ".launcher";
        String launcherClassName = getTestProperties().getProperty(key);
        if (launcherClassName == null)
        {
            throw new IllegalArgumentException("No launcher class defined for JacORB version " + version);
        }

        try
        {
            Class launcherClass = Class.forName (launcherClassName);
            Constructor ctor = launcherClass.getConstructor
            (
                new Class[] { java.lang.String.class,
                              boolean.class }
            );
            return (JacORBLauncher)ctor.newInstance
            (
                new Object[] { home, coverage ? Boolean.TRUE : Boolean.FALSE }
            );
        }
        catch (Exception ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    private static String getCVSHome()
    {
        String testHome = TestUtils.testHome();
        String patternString;
        String separator = System.getProperty("file.separator");

        // "\" must be escaped
        if (separator.equals("\\"))
        {
            patternString = "\\test\\regression";
        }
        else
        {
            patternString = "/test/regression";
        }

        int index;
        if ((testHome != null) && ((index=testHome.indexOf(patternString)) != -1))
        {
            return testHome.substring(0,index);
        }
        throw new IllegalArgumentException("couldn't find CVS home: "
                + testHome);
    }
}
