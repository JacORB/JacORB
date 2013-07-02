package org.jacorb.test.bugs.bug852;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.JacORBTestSuite;

public class Bug852Test extends ClientServerTestCase
{
    private AnyServer server;


    public Bug852Test(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    /**
     * <code>setUp</code> for junit.
     *
     * @exception Exception if an error occurs
     */
    public void setUp() throws Exception
    {
        server = AnyServerHelper.narrow( setup.getServerObject() );
    }

    /**
     * <code>tearDown</code> us used by Junit for cleaning up after the tests.
     *
     * @exception Exception if an error occurs
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();

        server._release ();
        server = null;
    }


    public static Test suite()
    {
        TestSuite suite = new JacORBTestSuite("Any client/server tests",
                                              Bug852Test.class);
        ClientServerSetup setup =
            new ClientServerSetup( suite,
                                   "org.jacorb.test.bugs.bug852.AnyServerImpl");

        suite.addTest( new Bug852Test( "test852", setup ));

        return setup;
    }


    public void test852() throws Exception
    {
        AnyServerBase asb = AnyServerBaseHelper.narrow(server);

        org.omg.CORBA.Any any = setup.getClientOrb().create_any();
        any.insert_Object(asb, AnyServerBaseHelper.type());

        org.omg.CORBA.Any any2 = server.roundtripany(any);

        org.omg.CORBA.TypeCode t1 = any.type();
        org.omg.CORBA.TypeCode t2 = any2.type();

        System.out.println("Client Received type: " + t2.toString());
        System.out.println("Client Sent type:     " + t1.toString());

        assertEquals ("Typecodes should be equal", t1, t2);
    }
}
