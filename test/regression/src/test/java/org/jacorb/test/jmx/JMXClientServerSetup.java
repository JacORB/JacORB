/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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
 *
 */

package org.jacorb.test.jmx;

import java.net.MalformedURLException;
import java.util.Properties;
import javax.management.remote.JMXServiceURL;
import org.jacorb.test.harness.ClientServerSetup;

public class JMXClientServerSetup extends ClientServerSetup
{
    private JMXServiceURL serviceURL;

    public JMXClientServerSetup(
            Properties clientOrbProperties,
            Properties serverOrbProperties)
        throws Exception
    {
        super(ExampleServiceRunner.class.getName(), "ignored", clientOrbProperties, serverOrbProperties);

        try
        {
            serviceURL = new JMXServiceURL(this.getServerIOR());
        }
        catch (MalformedURLException e)
        {
            throw new IllegalArgumentException();
        }
    }

    public JMXServiceURL getServiceURL()
    {
        return serviceURL;
    }

    public String getTestServerMain()
    {
        return ExampleServiceRunner.class.getName();
    }
}
