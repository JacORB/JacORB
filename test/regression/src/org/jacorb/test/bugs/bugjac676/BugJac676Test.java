package org.jacorb.test.bugs.bugjac676;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.ORB;

public class BugJac676Test extends ClientServerTestCase
{
    private TestBoundedString testObj = null;

    public BugJac676Test(String name, ClientServerSetup setup)
    {
        super( name, setup );
    }

    public static Test suite ()
    {
        TestSuite suite = new TestSuite( "bugjac676" );
        ClientServerSetup setup =
            new ClientServerSetup( suite,
                                   "org.jacorb.test.bugs.bugjac676.TestBoundedStringImpl" );

        TestUtils.addToSuite( suite, setup, BugJac676Test.class );

        return setup;
    }

    protected void setUp () throws Exception
    {
        ORB orb = setup.getClientOrb();
        String ior = setup.getServerIOR();
        org.omg.CORBA.Object ref = orb.string_to_object( ior );
        testObj = TestBoundedStringHelper.narrow( ref );
    }

    protected void tearDown () throws Exception
    {
        testObj = null;
    }

    public void testBoundedString ()
    {
        try
        {
            // no exceptions expected when string length corresponds
            // to the specified bounds
            StructOne paramGood = new StructOne(
                    new String( "0123456789012345678" ),
                    new String( "some normal string" )
                );

            testObj.set_object( paramGood );

            testObj.get_object();

            // there are two exception are expected -
            // one from read(...) another from write(...)
            // methods
            int exceptionsCount = 0;
            StructOne paramBad = new StructOne(
                    new String( "012345678901234567890123456789012" ),
                    new String( "some normal string" )
                );
            try
            {
                testObj.set_object( paramBad );
            }
            catch( org.omg.CORBA.BAD_PARAM e )
            {
                // expected exception - test was successful
                exceptionsCount++;
            }

            try
            {
                testObj.get_bad_object();
            }
            catch( org.omg.CORBA.BAD_PARAM e )
            {
                // expected exception - test was successful
                exceptionsCount++;
            }

            if( exceptionsCount != 2)
            {
                fail ("JAC#676 - Expected org.omg.CORBA.BAD_PARAM exceptions haven't thrown");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail( "Unexpected exception: " + e);
        }
    }
}
