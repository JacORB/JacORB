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

package org.jacorb.test.bugs.bugjac563;

import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.jacorb.test.BiDirServer;
import org.jacorb.test.BiDirServerHelper;
import org.jacorb.test.ClientCallback;
import org.jacorb.test.ClientCallbackHelper;
import org.jacorb.test.ClientCallbackOperations;
import org.jacorb.test.ClientCallbackPOATie;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.TestUtils;
import org.jacorb.test.orb.connection.BiDirSetup;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Alphonse Bendt
 */
public class BugJac563Test extends ClientServerTestCase
{
    private BiDirServer server;

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        // We want to test with a custom set of keystores so disable normal SSL testing.
        Assume.assumeFalse(TestUtils.isSSLEnabled);

        Properties clientProps = new Properties();

        clientProps.setProperty("org.omg.PortableInterceptor.ORBInitializerClass.bidir_init",
                                 "org.jacorb.orb.giop.BiDirConnectionInitializer" );

        clientProps.setProperty("jacorb.security.support_ssl", "on");

        clientProps.setProperty("jacorb.security.keystore", TestUtils.testHome() + "/src/test/java/org/jacorb/test/bugs/bugjac563/OCTrustStore");
        clientProps.setProperty("jacorb.security.keystore_password", "OCKeyStorePass");

        clientProps.setProperty("jacorb.security.ssl.client.supported_options", "20");
        clientProps.setProperty("jacorb.security.ssl.client.required_options", "20");
        clientProps.setProperty("jacorb.security.ssl.server.supported_options", "0");
        clientProps.setProperty("jacorb.security.ssl.server.required_options", "0");

        clientProps.setProperty("jacorb.security.jsse.trustees_from_ks", "on");

        clientProps.setProperty("jacorb.ssl.socket_factory", "org.jacorb.security.ssl.sun_jsse.SSLSocketFactory");
        clientProps.setProperty("jacorb.ssl.server_socket_factory", "org.jacorb.security.ssl.sun_jsse.SSLServerSocketFactory");

        clientProps.setProperty("jacorb.security.ssl.client.cipher_suites", "TLS_RSA_WITH_AES_128_CBC_SHA");

        if (TestUtils.isIBM)
        {
            clientProps.put("jacorb.security.jsse.server.key_manager_algorithm", "IbmX509");
            clientProps.put("jacorb.security.jsse.server.trust_manager_algorithm", "IbmX509");
            clientProps.put("jacorb.security.jsse.client.key_manager_algorithm", "IbmX509");
            clientProps.put("jacorb.security.jsse.client.trust_manager_algorithm", "IbmX509");
        }

        Properties serverProps = new Properties();

        if (TestUtils.isIBM)
        {
            serverProps.put("jacorb.security.jsse.server.key_manager_algorithm", "IbmX509");
            serverProps.put("jacorb.security.jsse.server.trust_manager_algorithm", "IbmX509");
            serverProps.put("jacorb.security.jsse.client.key_manager_algorithm", "IbmX509");
            serverProps.put("jacorb.security.jsse.client.trust_manager_algorithm", "IbmX509");
        }

        serverProps.setProperty("org.omg.PortableInterceptor.ORBInitializerClass.bidir_init",
                                "org.jacorb.orb.giop.BiDirConnectionInitializer" );

        serverProps.setProperty("jacorb.security.support_ssl", "on");

        serverProps.setProperty("jacorb.security.keystore", TestUtils.testHome() + "/src/test/java/org/jacorb/test/bugs/bugjac563/OCKeyStore");
        serverProps.setProperty("jacorb.security.keystore_password", "OCKeyStorePass");

        serverProps.setProperty("jacorb.security.ssl.client.supported_options", "20");
        serverProps.setProperty("jacorb.security.ssl.client.required_options", "20");
        serverProps.setProperty("jacorb.security.ssl.server.supported_options", "20");
        serverProps.setProperty("jacorb.security.ssl.server.required_options", "20");

        serverProps.setProperty("jacorb.security.jsse.trustees_from_ks", "on");

        serverProps.setProperty("jacorb.ssl.socket_factory", "org.jacorb.security.ssl.sun_jsse.SSLSocketFactory");
        serverProps.setProperty("jacorb.ssl.server_socket_factory", "org.jacorb.security.ssl.sun_jsse.SSLServerSocketFactory");

        setup = new BiDirSetup(clientProps, serverProps);
    }

    @Before
    public void setUp() throws Exception
    {
        server = BiDirServerHelper.narrow(setup.getServerObject());
    }

    @Test
    public void testCallbackWorks() throws Exception
    {
        final Map<String, String> result = new HashMap<String, String>();
        ClientCallbackOperations callbackServant = new ClientCallbackOperations()
        {
            public void hello(String message)
            {
                synchronized(result)
                {
                    result.put("result", message);

                    result.notifyAll();
                }
            }
        };

        ClientCallback callback = ClientCallbackHelper.narrow(((BiDirSetup)setup).getBiDirPOA().servant_to_reference(new ClientCallbackPOATie(callbackServant)));

        server.register_callback(callback);

        server.callback_hello("Hello Callback!");

        final long timeout = System.currentTimeMillis() + TestUtils.getMediumTimeout();
        synchronized(result)
        {
            while(result.isEmpty() && System.currentTimeMillis() < timeout)
            {
                result.wait();
            }
        }

        assertEquals("Hello Callback!", result.get("result"));
    }
}
