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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.jacorb.test.common.TestUtils;

/**
 * Launches a JacORB process by direct invocation of a JVM
 * with appropriate arguments.
 *
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id$
 */
public class DirectLauncher extends AbstractLauncher
{
    private static boolean assertsEnabled;

    static {
        assertsEnabled = false;
        assert assertsEnabled = true; // Intentional side effect!!!
    }

    private List command;

    public void init()
    {
        command = buildCMDLine(jacorbHome, useCoverage, classpath, properties, mainClass, args);
    }

    public String getCommand()
    {
        final List list = command;

        return formatList(list);
    }

    public Process launch()
    {
        final Runtime rt = Runtime.getRuntime();

        try
        {
            String[] cmd = toStringArray(command);

            TestUtils.log("[DirectLauncher] launch: " + command);

            Process proc = rt.exec (cmd);
            return proc;
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private List buildCMDLine(File jacorbHome,
                              boolean coverage,
                              String classpath,
                              Properties props,
                              String mainClass,
                              String[] args)
    {
        final String javaHome = getPropertyWithDefault(props, "jacorb.java.home", System.getProperty("java.home"));
        final String jvm = getPropertyWithDefault(props, "jacorb.jvm", "/bin/java");
        final String javaCommand = javaHome + jvm;

        final List cmdList = new ArrayList();
        cmdList.add (javaCommand);

        if (assertsEnabled)
        {
            cmdList.add("-ea");
        }

        Assert.assertNotNull("need to specify jacorbHome", jacorbHome);

        cmdList.add(new BootClasspathBuilder(jacorbHome, coverage).getBootClasspath());

        if (classpath != null && classpath.length() > 0)
        {
            cmdList.add ("-classpath");
            cmdList.add (classpath);
        }

        cmdList.add ("-Xmx" + getMaxHeapSize(props));

        cmdList.add ("-Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB");
        cmdList.add ("-Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton");

        cmdList.addAll (propsToArgList(props));

        cmdList.add ("-Djacorb.home=" + jacorbHome);

        if (TestUtils.isWindows())
        {
            try
            {
                cmdList.add ("-DSystemRoot=" + TestUtils.systemRoot());
            }
            catch (RuntimeException e)
            {
                System.out.println("WARNING: caught RuntimeException when reading SystemRoot: " + e.getMessage());
            }
            catch (IOException e)
            {
                System.out.println("WARNING: caught IOException when reading SystemRoot: " + e.getMessage());
            }
        }

        cmdList.add (mainClass);
        if (args != null)
        {
            cmdList.addAll (Arrays.asList(args));
        }
        return cmdList;
    }

    private String getMaxHeapSize(Properties props)
    {
        return getPropertyWithDefault(props, "jacorb.test.maxheapsize", "64m");
    }
}
