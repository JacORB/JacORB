package org.jacorb.test.bugs.bugjac294;

import org.jacorb.test.common.ORBTestCase;
import org.omg.CORBA.BAD_PARAM;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class BugJac294Test extends ORBTestCase
{
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
