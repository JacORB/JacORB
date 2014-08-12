package org.jacorb.test.orb;

import java.util.Properties;
import org.jacorb.orb.giop.CodeSet;
import org.jacorb.test.harness.ClientServerSetup;
import org.junit.Before;
import org.junit.BeforeClass;

public class CodesetOffTest extends AbstractCodesetTestCase
{
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        configuration = Mode.CODESET_OFF;
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        if (!CodeSet.getTCSDefault().getName().equals( "UTF8" ))
        {
            System.err.println
            ("WARNING - TESTS ARE NOT RUNNING WITH UTF8 - THEY MAY NOT PASS.");
        }

        Properties client_props = new Properties();
        Properties server_props = new Properties();

        client_props.setProperty ("jacorb.native_char_codeset", "utf8");
        server_props.setProperty ("jacorb.native_char_codeset", "utf8");
        client_props.setProperty ("jacorb.codeset", "off");
        server_props.setProperty ("jacorb.codeset", "off");

        server_props.setProperty ("jacorb.logfile.append", "on");


        setup = new ClientServerSetup(
                "org.jacorb.test.orb.CodesetServerImpl",
                client_props,
                server_props);

    }
}
