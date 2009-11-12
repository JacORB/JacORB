package org.jacorb.test.bugs.bugjac319;

import junit.framework.Test;

import org.jacorb.test.common.ClientServerSetup;

/**
 * @see org.jacorb.test.bugs.bugjac319.BugJac319AbstractTestCase
 */
public class BugJac319MutatorTest extends BugJac319AbstractTestCase
{
    public BugJac319MutatorTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public void test_mutate()
    {
        org.omg.CORBA.Object obj = server.getObject
            (setup.getClientOrb().string_to_object(DEMOIOR));

        assertEquals("Incoming objects should be one", 1, MutatorImpl.totalIncomingObjects);
        assertEquals("Outgoing objects should be one", 1, MutatorImpl.totalOutgoingObjects);

        assertTrue
        (
            "Should return imr ior with mutate",
            IMRIOR.equals(setup.getClientOrb().object_to_string(obj))
        );

        assertTrue(MutatorImpl.isConnectionUpdated);
    }

    public static Test suite()
    {
        return BugJac319AbstractTestCase.suite(true, BugJac319MutatorTest.class);
    }
}
