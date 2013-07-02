package org.jacorb.test.orb.connection;

import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.BiDirServer;
import org.jacorb.test.BiDirServerHelper;
import org.jacorb.test.ClientCallback;
import org.jacorb.test.ClientCallbackHelper;
import org.jacorb.test.ClientCallbackPOA;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.CommonSetup;

/**
 * @author Andre Spiegel
 */
public class BiDirTest extends ClientServerTestCase
{
    private BiDirServer server = null;
    private org.omg.PortableServer.POA biDirPOA = null;

    private final Object callbackLock = new Object();
    private boolean callbackReceived = false;
    private String  callbackMessage  = null;

    public BiDirTest (String name, ClientServerSetup setup)
    {
        super (name, setup);
    }

    public void setUp() throws Exception
    {
        server = BiDirServerHelper.narrow (setup.getServerObject());
        biDirPOA = ((BiDirSetup)setup).getBiDirPOA();
    }

    protected void tearDown() throws Exception
    {
        biDirPOA = null;
        server = null;
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

    public static Test suite()
    {
        TestSuite suite = new TestSuite ("Bidirectional GIOP Test");

        Properties properties = new Properties();
        properties.setProperty
            ("org.omg.PortableInterceptor.ORBInitializerClass.bidir_init",
             "org.jacorb.orb.giop.BiDirConnectionInitializer" );

        // this tests counts transports which are disrupted by
        // security initialisation.
        properties.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");

        BiDirSetup setup = new BiDirSetup (suite, properties, properties);

        suite.addTest (new BiDirTest ("test_callback", setup));

        return setup;
    }


    public void test_callback()
    {
        ClientCallback c = null;
        try
        {
            c = ClientCallbackHelper.narrow (
                  biDirPOA.servant_to_reference
                                          (new ClientCallbackImpl()));
        }
        catch (Exception e)
        {
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
