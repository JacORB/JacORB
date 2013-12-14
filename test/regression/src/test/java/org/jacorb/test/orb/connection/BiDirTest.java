package org.jacorb.test.orb.connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.util.Properties;
import org.jacorb.test.BiDirServer;
import org.jacorb.test.BiDirServerHelper;
import org.jacorb.test.ClientCallback;
import org.jacorb.test.ClientCallbackHelper;
import org.jacorb.test.ClientCallbackPOA;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Andre Spiegel
 */
public class BiDirTest extends ClientServerTestCase
{
    private BiDirServer server = null;

    private final Object callbackLock = new Object();
    private boolean callbackReceived = false;
    private String  callbackMessage  = null;

    @Before
    public void setUp() throws Exception
    {
        server = BiDirServerHelper.narrow (setup.getServerObject());
    }

    private class ClientCallbackImpl extends ClientCallbackPOA
    {
        public void hello (String message)
        {
            synchronized (callbackLock)
            {
                callbackReceived = true;
                callbackMessage  = message;
                callbackLock.notifyAll();
            }
        }
    }

    private String waitForCallback (int timeout)
    {
        synchronized (callbackLock)
        {
            if (callbackReceived)
                return callbackMessage;
            else
            {
                try
                {
                    callbackLock.wait (timeout);
                }
                catch (InterruptedException ex)
                {
                    // ignore
                }
                if (callbackReceived)
                    return callbackMessage;
                else
                    throw new org.omg.CORBA.TIMEOUT
                                 ("no callback received within timeout");
            }
        }
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        // this tests counts transports which are disrupted by
        // security initialisation.
        Assume.assumeFalse(TestUtils.isSSLEnabled);
        
        Properties properties = new Properties();
        properties.setProperty
            ("org.omg.PortableInterceptor.ORBInitializerClass.bidir_init",
             "org.jacorb.orb.giop.BiDirConnectionInitializer" );

        setup = new BiDirSetup (properties, properties);
    }


    @Test
    public void test_callback()
    {
        ClientCallback c = null;
        try
        {
            c = ClientCallbackHelper.narrow (((BiDirSetup)setup).
                        getBiDirPOA().servant_to_reference(new ClientCallbackImpl()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail ("exception creating callback object: " + e);
        }

        server.register_callback (c);
        server.callback_hello ("This is a test");
        String result = waitForCallback (10000);
        int n = server.get_open_client_transports();

        assertEquals ("This is a test", result);

        // if this was bidirectional, then the server must not have
        // any open client transports now
        assertEquals ("Server has too many client transports", 0, n);
    }
}
