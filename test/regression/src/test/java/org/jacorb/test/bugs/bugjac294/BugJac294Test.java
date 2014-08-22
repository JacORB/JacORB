package org.jacorb.test.bugs.bugjac294;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.jacorb.test.harness.ORBTestCase;
import org.junit.Test;
import org.omg.CORBA.BAD_PARAM;

/**
 * @author Alphonse Bendt
 */
public class BugJac294Test extends ORBTestCase
{
    @Test
    public void testStringToObject() throws Exception
	{
		try
		{
			orb.string_to_object("bogus ior");
			fail();
		}
		catch (BAD_PARAM e)
		{
			// expected
			assertEquals(10, e.minor);
		}
	}
}
