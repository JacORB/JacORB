/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2006 Gerald Brose
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.jacorb.orb.rmi.PortableRemoteObjectDelegateImpl;
import org.omg.CORBA.ORB;

public class ExampleServiceRunner
{
    public static void main(String[] args) throws Exception
    {
        System.setProperty("javax.rmi.CORBA.PortableRemoteObjectClass", PortableRemoteObjectDelegateImpl.class.getName());

        final ORB orb = ORB.init(args, null);

        PortableRemoteObjectDelegateImpl.setORB(orb);

        MBeanServer server = MBeanServerFactory.createMBeanServer();

        JMXServiceURL serviceURL = startIIOPConnector(orb, server);

        server.registerMBean(new ExampleService(), new ObjectName(":service=example"));

        System.out.println("SERVER IOR: " + serviceURL);
    }

    private static JMXServiceURL startIIOPConnector(ORB orb, MBeanServer mbeanserver) throws Exception, IOException
    {
        JMXServiceURL serviceURL = new JMXServiceURL("service:jmx:iiop://localhost");

        Map environment = new HashMap();

        environment.put("java.naming.corba.orb", orb);

        // create the JMXCconnectorServer
        JMXConnectorServer connectorServer =
            JMXConnectorServerFactory.newJMXConnectorServer(serviceURL, environment, mbeanserver);

        // register the JMXConnectorServer in the MBeanServer
        ObjectName connectorServerName = ObjectName.getInstance("connectors:protocol=iiop");
        mbeanserver.registerMBean(connectorServer, connectorServerName);

        connectorServer.start();

        return connectorServer.getAddress();
    }
}
