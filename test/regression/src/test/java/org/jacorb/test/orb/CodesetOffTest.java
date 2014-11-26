package org.jacorb.test.orb;

import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.junit.Before;
import org.junit.BeforeClass;

public class CodesetOffTest extends AbstractCodesetTestCase
{
    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        configuration = Mode.CODESET_OFF;
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
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
