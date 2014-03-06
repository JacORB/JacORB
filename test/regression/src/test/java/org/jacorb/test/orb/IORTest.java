package org.jacorb.test.orb;

import static org.junit.Assert.assertTrue;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;
import org.jacorb.orb.ParsedIOR;
import org.jacorb.orb.etf.ProfileBase;
import org.jacorb.orb.iiop.IIOPAddress;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS;
import org.omg.IOP.TaggedComponent;

/**
 * Verify the correct number of profiles/TAG_ALTERNATE_IIOP_ADDRESS in the IOR.
 */
@RunWith(Parameterized.class)
public class IORTest extends ClientServerTestCase
{
    /**
     * Total count of non-loopback interfaces
     */
    private final static int nonLocalInterfaceCount;

    static
    {
        int counter = 0;

        LinkedList<InetAddress> n = IIOPAddress.getNetworkInetAddresses();
        for (InetAddress ia : n)
        {
            if (!ia.isLoopbackAddress() && !ia.isLinkLocalAddress() && !(ia instanceof Inet6Address))
            {
                counter++;
            }
        }

        nonLocalInterfaceCount = counter;
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);
    }

    private BasicServer server;

    @Parameter
    public String key;

    @Parameter(value = 1)
    public String value;

    @Parameters
    public static Collection<Object[]> data()
    {
        return Arrays.asList(new Object [][] {
                { "jacorb.iiop.alternate_addresses", "192.168.123.7:45679" },
                { "-ORBListenEndpoints", "'iiop://:45678,iiop://:45679'" }
        } );
    }

    private int matchCount;

    @Before
    public void setUp() throws Exception
    {
        Properties properties = new Properties ();
        String [] orbargs;

        // We have to match interfaces that are not in the primary profile. Therefore
        // there will always be one less than we have. We can't have negative amount of
        // interfaces.
        int interfaceCount = (nonLocalInterfaceCount - 1) < 0 ? 0 : (nonLocalInterfaceCount - 1);

        // If the key (from the parameters above starts with '-' it is an ORB argument
        if (key.startsWith("-"))
        {
            orbargs = new String [] { key, value };
        }
        // Else its a property
        else
        {
            orbargs = new String [] {};
            properties.put(key, value);
            interfaceCount++;
        }

        matchCount = interfaceCount;

        setup = new ClientServerSetup(null, "org.jacorb.test.orb.BasicServerImpl", orbargs, properties, properties);

        server = BasicServerHelper.narrow( setup.getServerObject() );
    }

    @After
    public void tearDown() throws Exception
    {
        setup.tearDown();
        server._release();
    }

    @Test
    public void profileSize()
    {
        ParsedIOR p = new ParsedIOR (setup.getORB(), setup.getServerIOR());

        // If we're not using -ORBListenEndpoints then expect one profile
        assertTrue ("Should be two profiles", p.getProfiles().size() == ( key.startsWith("-") ? 2 : 1));
    }

    @Test
    public void interfaceCount()
    {
        ParsedIOR p = new ParsedIOR (setup.getORB(), setup.getServerIOR());
        TaggedComponent[] taggedComponents = ((ProfileBase)p.getEffectiveProfile()).getComponents().asArray();

        int tagCount = 0;
        for (TaggedComponent t : taggedComponents)
        {
            if ( t.tag == TAG_ALTERNATE_IIOP_ADDRESS.value)
            {
                tagCount++;
            }
        }

        assertTrue ("Network interface count does not match TAG_ALTERNATE_IIOP_ADDRESS: "+
        setup.getServerIOR() + " and " + tagCount + " and " + matchCount , tagCount == matchCount);
    }
}
