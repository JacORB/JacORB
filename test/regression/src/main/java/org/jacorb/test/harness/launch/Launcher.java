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
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.jacorb.test.harness.launch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.jacorb.test.harness.TestUtils;

/**
 * Launches a JacORB process by direct invocation of a JVM
 * with appropriate arguments.
 *
 */
public class Launcher
{
    protected File jacorbHome;

    protected String classpath;

    protected Properties properties;

    protected List<String> vmArgs;

    protected String mainClass;

    protected String[] args;

    private List<String> command;

    private String javaHome;

    private String javaCommand;

    public void setArgs(String[] args)
    {
        this.args = args;
    }

    public void setClasspath(String classpath)
    {
        this.classpath = classpath;
    }

    public void setMainClass(String mainClass)
    {
        this.mainClass = mainClass;
    }

    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }

    public void setVmArgs(List<String> jvmArgs)
    {
        this.vmArgs = jvmArgs;
    }

    public void setJacorbHome(File jacorbHome)
    {
        TestUtils.getLogger().debug("using JacORB home: " + jacorbHome);
        this.jacorbHome = jacorbHome;
    }

    protected String getPropertyWithDefault(Properties props, String name, String defaultValue)
    {
        return props.getProperty(name, System.getProperty(name, defaultValue));
    }

    private List<String> propsToArgList(Properties props)
    {
        List<String> result = new ArrayList<String>();

        if (props == null) return result;

        for (Iterator<Object> i = props.keySet().iterator(); i.hasNext();)
        {
            String key = (String)i.next();
            String value = props.getProperty(key);
            result.add ("-D" + key + "=" + value);
        }

        return result;
    }

    public void init()
    {
        command = buildCMDLine(jacorbHome, classpath, properties, mainClass, args);
    }

    public Process launch()
    {
        final Runtime rt = Runtime.getRuntime();

        try
        {
            String[] cmd = (command.toArray (new String[command.size()]));

            StringBuffer buff = new StringBuffer();
            for (int i = 0; i < cmd.length; i++)
            {
                buff.append(cmd[i]);
                buff.append(' ');
            }

            TestUtils.getLogger().debug("[DirectLauncher] launch: " + buff);

            Process proc = rt.exec(cmd);
            return proc;
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private List<String> buildCMDLine(File jacorbHome, String classpath, Properties props,
            String mainClass, String[] args)
    {
        javaHome = getPropertyWithDefault(props, "jacorb.java.home", System.getProperty("java.home"));
        final String jvm = getPropertyWithDefault(props, "jacorb.jvm", "/bin/java");
        javaCommand = javaHome + jvm;

        final List<String> cmdList = new ArrayList<String>();

        cmdList.add (javaCommand);
        cmdList.addAll (vmArgs);

        if ( ! props.containsKey("ignoreXBootClasspath"))
        {
            cmdList.add("-Xbootclasspath:" + System.getProperty("sun.boot.class.path"));
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
