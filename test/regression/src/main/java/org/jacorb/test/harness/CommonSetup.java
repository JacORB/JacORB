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

package org.jacorb.test.harness;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * methods shared between JacORB's test setup classes.
 *
 * @author Alphonse Bendt
 */
public class CommonSetup
{
    /**
     * its assumed that the property file and the keystore file
     * are located in the demo/ssl dir.
     */
    public static Properties loadSSLProps(String propertyFilename, String keystoreFilename) throws IOException
    {
        Properties props = loadProps ("ssl", propertyFilename, keystoreFilename);

        props.put("jacorb.security.ssl.ssl_listener", SSLListener.class.getName());
        props.remove("org.omg.PortableInterceptor.ORBInitializerClass.ForwardInit");

        if (TestUtils.isIBM)
        {
            props.put("jacorb.security.jsse.server.key_manager_algorithm", "IbmX509");
            props.put("jacorb.security.jsse.server.trust_manager_algorithm", "IbmX509");
            props.put("jacorb.security.jsse.client.key_manager_algorithm", "IbmX509");
            props.put("jacorb.security.jsse.client.trust_manager_algorithm", "IbmX509");
        }

        return props;
    }

    /**
     * its assumed that the property file and the keystore file
     * are located in the demo/ssl dir.
     */
    public static Properties loadSASProps(String propertyFilename) throws IOException
    {
        return loadProps ("sas", propertyFilename, null);
    }

    private static Properties loadProps(String dir, String propertyFilename, String keystoreFilename) throws IOException
    {
        final Properties props = new Properties();

        final File file = new File
        (
            TestUtils.testHome()
            + File.separatorChar
            + ".."
            + File.separatorChar
            + ".."
            + File.separatorChar
            + "demo"
            + File.separatorChar
            + dir
            + File.separatorChar
            + "src"
            + File.separatorChar
            + "main"
            + File.separatorChar
            + "resources"
            + File.separatorChar
            + propertyFilename
        );

        BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));

        try
        {
            props.load(input);
        }
        finally
        {
            input.close();
        }

        if (keystoreFilename != null)
        {
            props.put
            (
               "jacorb.security.keystore",
               file.getParent() + File.separatorChar + keystoreFilename
            );
        }

        return props;
    }

    public static boolean getSystemPropertyAsBoolean(final String prop)
    {
        final String propValue = System.getProperty(prop);
        return TestUtils.getStringAsBoolean(propValue);
    }

}
