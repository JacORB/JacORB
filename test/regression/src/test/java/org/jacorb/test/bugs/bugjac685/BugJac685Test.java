package org.jacorb.test.bugs.bugjac685;

import java.util.Properties;
import org.jacorb.test.harness.NameServiceSetup;
import org.jacorb.test.harness.ORBTestCase;
import org.jacorb.test.harness.ServerSetup;
import org.jacorb.test.harness.TestUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

public class BugJac685Test extends ORBTestCase
{
    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();

    private SessionFactory sf;
    private NamingContextExt nc;
    private ServerSetup serverSetup;

    private static NameServiceSetup nsSetup;

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);

        nsSetup = new NameServiceSetup (folder);
        nsSetup.setUp();
    }

    @Override
    protected void patchORBProperties(Properties props) throws Exception
    {
        props.setProperty ("ORBInitRef.NameService", nsSetup.getServerIOR());
    }

    @Before
    public void setUp() throws Exception
    {
        Properties serverprops = new Properties();
        serverprops.setProperty ("ORBInitRef.NameService", nsSetup.getServerIOR());
        serverprops.setProperty ("jacorb.test.timeout.server", Long.toString(15000));

        serverSetup = new ServerSetup (
            "org.jacorb.test.bugs.bugjac685.BugJac685TestServer",
            "",
            serverprops);

        serverSetup.setUp();

        nc = NamingContextExtHelper.narrow
            (orb.resolve_initial_references ("NameService"));

        sf = SessionFactoryHelper.narrow
            (nc.resolve(nc.to_name ("ServantScaling/SessionFactory")));
    }

    @AfterClass
    public static void afterClassTearDown() throws Exception
    {
        if (nsSetup != null)
        {
            nsSetup.tearDown();
        }
    }

    @After
    public void tearDown() throws Exception
    {
        serverSetup.tearDown();
    }

    private void setPoa (POA_Kind pk)
    {
        sf.set_poa (pk);
    }

    private void doTest()
    {
        int counts[] = {100};

        for (int i = 0; i < counts.length; i++)
        {
            sf.create_sessions (counts[i]);

            int sample = 100;

            for (int j = 0; j < counts[i]; j += counts[i]/sample)
            {
                Session s = sf.get_session (j);
                s._release();
            }
        }
    }

    @Test
    public void testDifferentPOAConfigs()
    {
        setPoa (POA_Kind.PK_SYSTEMID);
        doTest();

        setPoa (POA_Kind.PK_USERID);
        doTest();

        setPoa (POA_Kind.PK_DEFSERVANT);
        doTest();

        setPoa (POA_Kind.PK_SERVANTLOC);
        doTest();
    }
}
