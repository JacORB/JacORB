package org.jacorb.test.bugs.bugjac319;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.jacorb.test.harness.ClientServerSetup;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @see org.jacorb.test.bugs.bugjac319.BugJac319AbstractTestCase
 */
public class BugJac319NoMutatorTest extends BugJac319AbstractTestCase
{
    @Test
    public void test_nomutate()
    {
        org.omg.CORBA.Object obj = server.getObject
            (setup.getClientOrb().string_to_object(DEMOIOR));

        assertEquals("Incoming objects should be zero", 0, MutatorImpl.totalIncomingObjects);
        assertEquals("Outgoing objects should be zero", 0, MutatorImpl.totalOutgoingObjects);
        assertTrue
        (
            "Should return demo ior with no mutate",
            DEMOIOR.equals (setup.getClientOrb().object_to_string(obj))
        );
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        setup = new ClientServerSetup
        (
            JAC319Impl.class.getName()
        );
   }
}
