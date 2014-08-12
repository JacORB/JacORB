package org.jacorb.test.bugs.bug852;

import static org.junit.Assert.assertEquals;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class Bug852Test extends ClientServerTestCase
{
    private AnyServer server;


    /**
     * <code>setUp</code> for junit.
     *
     * @exception Exception if an error occurs
     */
    @Before
    public void setUp() throws Exception
    {
        server = AnyServerHelper.narrow( setup.getServerObject() );
    }


    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        setup = new ClientServerSetup("org.jacorb.test.bugs.bug852.AnyServerImpl");
    }


    @Test
    public void test852() throws Exception
    {
        AnyServerBase asb = AnyServerBaseHelper.narrow(server);

        org.omg.CORBA.Any any = setup.getClientOrb().create_any();
        any.insert_Object(asb, AnyServerBaseHelper.type());

        org.omg.CORBA.Any any2 = server.roundtripany(any);

        org.omg.CORBA.TypeCode t1 = any.type();
        org.omg.CORBA.TypeCode t2 = any2.type();

        assertEquals ("Typecodes should be equal", t1, t2);
    }
}
