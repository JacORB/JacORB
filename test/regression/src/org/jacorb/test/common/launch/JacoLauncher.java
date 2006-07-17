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

import java.util.*;
import java.io.*;

import org.jacorb.test.common.TestUtils;

/**
 * A launcher that uses the jaco script of a given JacORB installation.
 *
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id$
 */
public class JacoLauncher extends JacORBLauncher
{
    public JacoLauncher(String jacorbHome, boolean coverage)
    {
        super(jacorbHome, coverage);
        if (coverage)
            System.out.println ("WARNING: Cannot find coverage when running under jaco");
    }

    public Process launch(String classpath,
                          Properties props,
                          String mainClass,
                          String[] args)
    {
        Runtime rt  = Runtime.getRuntime();

        List cmdList = new ArrayList();

        cmdList.add (jacorbHome + "/bin/jaco");
        cmdList.addAll (TestUtils.propsToArgList (props));
        cmdList.add (mainClass);
        cmdList.addAll (Arrays.asList(args));

        try
        {
            return rt.exec(toStringArray(cmdList));
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
