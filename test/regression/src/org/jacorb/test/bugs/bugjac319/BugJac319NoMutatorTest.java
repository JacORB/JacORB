package org.jacorb.test.bugs.bugjac319;

import junit.framework.Test;

import org.jacorb.test.common.ClientServerSetup;

/**
 * @see org.jacorb.test.bugs.bugjac319.BugJac319AbstractTestCase
 */
public class BugJac319NoMutatorTest extends BugJac319AbstractTestCase
{
    public BugJac319NoMutatorTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

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

    public static Test suite()
    {
        return BugJac319AbstractTestCase.suite(false, BugJac319NoMutatorTest.class);
    }
}
