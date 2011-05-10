/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2011 Gerald Brose / The JacORB Team.
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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class BootClasspathBuilder
{
    private final String bootClasspath;

    public BootClasspathBuilder(String jacorbHome, boolean useCoverage)
    {
        this(new File(jacorbHome), useCoverage);
    }

    public BootClasspathBuilder(File jacorbHome, boolean useCoverage)
    {
        if (jacorbHome == null)
        {
            throw new IllegalArgumentException("JacORB Home may not be null");
        }

        List entries = new ArrayList();

        if (useCoverage)
        {
            String coverageDir = System.getProperty("jacorb.test.coverage.classes-instrumented");

            final File instrumentedClasses;
            if (coverageDir == null)
            {
                instrumentedClasses = new File (jacorbHome, "test/regression/classes-instrumented");
            }
            else
            {
                instrumentedClasses = new File (coverageDir);
            }

            if (!instrumentedClasses.exists())
            {
                throw new IllegalArgumentException("JacORB installation "
                        + jacorbHome
                        + " is not instrumented; coverage "
                        + " will not be available");
            }

            entries.add(instrumentedClasses.getAbsolutePath());
        }

        entries.add(new File(jacorbHome, "classes").getAbsolutePath());

        File libDir = new File(jacorbHome, "lib");
        String[] jars = libDir.list(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".jar") && !name.startsWith("idl.jar");
            }
        });

        if (jars != null)
        {
            for (int i = 0; i < jars.length; i++)
            {
                final String entry = new File(libDir, jars[i]).getAbsolutePath();
                entries.add(entry);
            }
        }

        if (useCoverage)
        {
            entries.add(System.getProperty("jacorb.test.coverage.classpath.server"));

//            final File emmaJar = new File(jacorbHome, "test/regression/lib/emma.jar");
//            if (!emmaJar.exists())
//            {
//                throw new IllegalArgumentException("cannot locate emma.jar at " + emmaJar);
//            }
//            entries.add(emmaJar.toString());
        }

        final StringBuffer buffer = new StringBuffer();
        if (!entries.isEmpty())
        {
            buffer.append("-Xbootclasspath/p");

            for(int x=0; x<entries.size(); ++x)
            {
                buffer.append(':');
                buffer.append(entries.get(x));
            }
        }

        bootClasspath = buffer.toString();
    }

    public String getBootClasspath()
    {
        return bootClasspath;
    }
}
