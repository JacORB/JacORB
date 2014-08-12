package org.jacorb.test.orb.connection;

import java.util.Properties;
import org.jacorb.test.harness.TestUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * @author Andre Spiegel
 */
public class NIOBiDirTest extends BiDirTest
{
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);
        
        Properties properties = new Properties();
        properties.setProperty
            ("org.omg.PortableInterceptor.ORBInitializerClass.bidir_init",
             "org.jacorb.orb.giop.BiDirConnectionInitializer" );
        properties.setProperty ("jacorb.connection.nonblocking", "true");

        setup = new BiDirSetup (properties, properties);
    }
}
