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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A launcher that uses the jaco script of a given JacORB installation.
 *
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id$
 */
public class JacoLauncher extends AbstractLauncher
{
    private List command;

    public void setUseCoverage(boolean useCoverage)
    {
        System.out.println ("WARNING: Cannot find coverage when running under jaco");
    }

    public void init()
    {
        command = new ArrayList();
        command.add(jacorbHome + "/bin/jaco");
        command.addAll(propsToArgList (properties));
        command.add(mainClass);
        command.addAll (Arrays.asList(args));
    }

    public Process launch()
    {
        Runtime rt  = Runtime.getRuntime();

        try
        {
            return rt.exec(toStringArray(command));
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public String getCommand()
    {
        return formatList(command);
    }
}
