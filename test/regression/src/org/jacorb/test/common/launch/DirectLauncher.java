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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.jacorb.test.common.TestUtils;

/**
 * Launches a JacORB process by direct invocation of a JVM
 * with appropriate arguments.
 *
 * @author Andre Spiegel spiegel@gnu.org
 */
public class DirectLauncher extends AbstractLauncher
{
    private static boolean assertsEnabled;

    static
    {
        assertsEnabled = false;
        assert assertsEnabled = true; // Intentional side effect!!!
    }

    private List command;
    private String javaHome;
    private String javaCommand;

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

            StringBuffer buff = new StringBuffer();
            for (int i = 0; i < cmd.length; i++)
            {
                buff.append(cmd[i]);
                buff.append(' ');
            }

            Map env = new HashMap();
            String pidDir = getPropertyWithDefault(properties, "jacorb.test.piddir", System.getProperty("java.io.tmpdir"));

            env.put("JACUNIT_PID_DIR", pidDir);

            TestUtils.log("[DirectLauncher] launch: " + buff);
            TestUtils.log("[DirectLauncher] environment " + env);

            Process proc = rt.exec(cmd, formatEnv(env));
            return proc;
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private String[] formatEnv(Map env)
    {
        String[] result = new String[env.size()];

        int idx = 0;
        Iterator i = env.keySet().iterator();
        while(i.hasNext())
        {
            Object key = i.next();
            result[idx++] = key + "=" + env.get(key);
        }

        return result;
    }

    private List buildCMDLine(File jacorbHome,
                              boolean coverage,
                              String classpath,
                              Properties props,
                              String mainClass,
                              String[] args)
    {
        javaHome = getPropertyWithDefault(props, "jacorb.java.home", System.getProperty("java.home"));
        final String jvm = getPropertyWithDefault(props, "jacorb.jvm", "/bin/java");
        javaCommand = javaHome + jvm;

        final List cmdList = new ArrayList();

        cmdList.add(new File(jacorbHome, "test/regression/launch.sh").toString());
        cmdList.add (javaCommand);

        if (assertsEnabled)
        {
            cmdList.add("-ea");
        }

        if (jacorbHome != null)
        {
            cmdList.add(new BootClasspathBuilder(jacorbHome, coverage).getBootClasspath());
        }

        if (classpath != null && classpath.length() > 0)
        {
            cmdList.add ("-classpath");
            cmdList.add (classpath);
        }

        if (props != null)
        {
            if (! "".equals (getMaxHeapSize(props)))
            {
                cmdList.add ("-Xmx" + getMaxHeapSize(props));
            }
            cmdList.addAll (propsToArgList(props));
        }

        if (jacorbHome != null)
        {
            cmdList.add ("-Djacorb.home=" + jacorbHome);
        }

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
        return getPropertyWithDefault(props, "jacorb.test.maxheapsize", "");
    }

    public String getLauncherDetails(String prefix)
    {
        try
        {
            final String javaVersionCommand = javaCommand + " -version";
            Process proc = Runtime.getRuntime().exec(javaVersionCommand);

            try
            {
                InputStream inputStream = proc.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line = null;
                StringBuffer buffer = new StringBuffer();
                while((line = reader.readLine()) != null)
                {
                    buffer.append(prefix);
                    buffer.append(line);
                    buffer.append('\n');
                }

                return buffer.toString();
            }
            finally
            {
                proc.destroy();
            }
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
