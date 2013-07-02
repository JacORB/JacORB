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

package org.jacorb.test.common.launch;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.jacorb.test.common.TestUtils;

/**
 * @author Alphonse Bendt
 */
public abstract class AbstractLauncher implements Launcher
{
    protected File jacorbHome;

    protected String classpath;

    protected Properties properties;

    protected boolean useCoverage;

    protected String[] vmArgs;

    protected String mainClass;

    protected String[] args;

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

    public void setUseCoverage(boolean useCoverage)
    {
        this.useCoverage = useCoverage;
    }

    public void setVmArgs(String[] vmArgs)
    {
        this.vmArgs = vmArgs;
    }

    public void setJacorbHome(File jacorbHome)
    {
        TestUtils.log("using JacORB home: " + jacorbHome);
        this.jacorbHome = jacorbHome;
    }

    /**
     * is invoked after all properties are set.
     */
    public void init()
    {
    }

    protected String[] toStringArray (List list)
    {
        return ((String[])list.toArray (new String[list.size()]));
    }

    protected String getPropertyWithDefault(Properties props, String name, String defaultValue)
    {
        return props.getProperty(name, System.getProperty(name, defaultValue));
    }

    protected String formatList(final List list)
    {
        StringBuffer buffer = new StringBuffer();

        Iterator i = list.iterator();
        while(i.hasNext())
        {
            buffer.append(i.next().toString());
            buffer.append(' ');
        }

        return buffer.toString().trim();
    }

    protected List propsToArgList(Properties props)
    {
        return TestUtils.propsToArgList(props);
    }

    public String getLauncherDetails(String prefix)
    {
        return "";
    }
}
