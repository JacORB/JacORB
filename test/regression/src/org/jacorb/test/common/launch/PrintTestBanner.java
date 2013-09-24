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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Redirector;
import org.apache.tools.ant.types.PropertySet;

/**
 * @author Alphonse Bendt
 */
public class PrintTestBanner extends Task
{
    private static final DateFormat dateStringFormatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss Z");
    private final Properties props = new Properties();
    protected Redirector redirector = new Redirector(this);

    private void printTestHeader (PrintWriter out, String clientDetails)
    {
        out.println("-------------------------------------------------------------------------------");
        out.println();
        out.println("JacORB Regression Test Report");
        out.println();
        out.println("Date:     " + getTestDateString());
        out.println();
        out.println(clientDetails);
        out.println();
        out.println("Coverage:         " + formatBooleanProperty("jacorb.test.coverage"));
        out.println("SSL:              " + formatBooleanProperty("jacorb.test.ssl"));
        out.println("IMR:              " + formatBooleanProperty("jacorb.test.imr"));
        out.println();
        out.println("-------------------------------------------------------------------------------");
        out.println();
    }

    public void setOutputproperty(String outputProp) {
        redirector.setOutputProperty(outputProp);
    }

    private String getTestDateString()
    {
        return dateStringFormatter.format(new Date());
    }

    public void addConfiguredSyspropertyset(PropertySet sysp)
    {
        props.putAll(sysp.getProperties());
    }

    public void execute() throws BuildException
    {
        redirector.createStreams();

        OutputStream out = redirector.getOutputStream();

        try
        {
            PrintWriter writer = new PrintWriter(out);

            try
            {
                printTestHeader(writer, "");
            }
            finally
            {
                writer.close();
            }
        }
        catch(Exception e)
        {
            throw new BuildException(e);
        }
        finally
        {
            try
            {
                redirector.complete();
            }
            catch (IOException e)
            {
               throw new BuildException(e);
            }
        }
    }

    private String formatBooleanProperty(String name)
    {
        return props.containsKey(name) ? "yes" : "no";
    }
}
