package org.jacorb.test.bugs.bugjac685;

import java.util.Properties;
import junit.framework.TestCase;
import org.jacorb.test.common.ORBSetup;
import org.jacorb.test.common.ServerSetup;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

public class BugJac685Test extends TestCase
{
    private SessionFactory sf;
    private org.omg.CORBA.ORB orb;
    private NamingContextExt nc;

    private NameServiceSetup nsSetup;
    private String NS_IOR;
    private ServerSetup serverSetup;
    private ORBSetup clientSetup;

    protected void setUp() throws Exception
    {
        try
        {
            nsSetup = new NameServiceSetup (this);
            nsSetup.setUp();

            Properties serverprops = new Properties();
            serverprops.setProperty ("ORBInitRef.NameService", nsSetup.getServerIOR());
            serverprops.setProperty ("jacorb.test.timeout.server", Long.toString(15000));
            serverprops.setProperty("jacorb.test.ssl", "false");

            serverSetup = new ServerSetup (this,
                                           "org.jacorb.test.bugs.bugjac685.BugJac685TestServer",
                                           "",
                                           serverprops);

            serverSetup.setUp();

            Properties clientprops = new Properties();
            clientprops.setProperty ("ORBInitRef.NameService", nsSetup.getServerIOR());
            clientprops.setProperty("jacorb.test.ssl", "false");

            clientSetup = new ORBSetup (this, clientprops);
            clientSetup.setUp();

            orb = clientSetup.getORB();

            nc = NamingContextExtHelper.narrow
                (orb.resolve_initial_references ("NameService"));

            sf = SessionFactoryHelper.narrow
                (nc.resolve(nc.to_name ("ServantScaling/SessionFactory")));
        }
        catch (Exception e)
        {
            fail ("Unexpected exception setting up " + e);
        }
    }

    protected void tearDown() throws Exception
    {
        nsSetup.tearDown();
        serverSetup.tearDown();
        clientSetup.tearDown();
    }

    public void setPoa (POA_Kind pk)
    {
        sf.set_poa (pk);
    }

    private void doTest()
    {
        try
        {
            int counts[] = {100};

            for (int i = 0; i < counts.length; i++)
            {
                sf.create_sessions (counts[i]);

                int sample = 100;

                for (int j = 0; j < counts[i]; j += counts[i]/sample)
                {
                    Session s = sf.get_session (j);
                }
            }
        }
        catch (Exception e)
        {
            fail ("Unexpected exception doing test " + e);
        }
    }

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
