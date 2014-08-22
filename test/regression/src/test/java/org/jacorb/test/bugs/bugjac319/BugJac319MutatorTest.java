package org.jacorb.test.bugs.bugjac319;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @see org.jacorb.test.bugs.bugjac319.BugJac319AbstractTestCase
 */
public class BugJac319MutatorTest extends BugJac319AbstractTestCase
{
    @Test
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

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Properties props = new Properties();
        props.put("jacorb.iormutator",
                      MutatorImpl.class.getName());

        setup = new ClientServerSetup
        (
            JAC319Impl.class.getName(),
            props,
            props
        );
    }
 }
