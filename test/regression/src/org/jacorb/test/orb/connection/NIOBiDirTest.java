package org.jacorb.test.orb.connection;

import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.CommonSetup;

/**
 * @author Andre Spiegel
 * @version $Id$
 */
public class NIOBiDirTest extends BiDirTest
{
    public NIOBiDirTest (String name, ClientServerSetup setup)
    {
        super (name, setup);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite ("Bidirectional GIOP Test");

        Properties properties = new Properties();
        properties.setProperty
            ("org.omg.PortableInterceptor.ORBInitializerClass.bidir_init",
             "org.jacorb.orb.giop.BiDirConnectionInitializer" );
        properties.setProperty ("jacorb.connection.nonblocking", "true");

        // this tests counts transports which are disrupted by
        // security initialisation.
        properties.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");

        BiDirSetup setup = new BiDirSetup (suite, properties, properties);

        if ( ! setup.isSSLEnabled ())
        {
            suite.addTest (new NIOBiDirTest ("test_callback", setup));
        }

        return setup;
    }
}
