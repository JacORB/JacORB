package org.jacorb.test.bugs.bug272;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.jacorb.test.harness.ORBTestCase;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CORBA.OctetSeqHelper;
/**
 * Test for bug 272, byte array in Any.
 *
 * @author Andre Spiegel
 */
public class Bug272Test extends ORBTestCase
{
	/**
	 * Puts a byte array of length 1 into an Any.
	 * (Regression test for bug #272)
	 */
    @Test
    public void test_Any_byte_array_1()
	{
		do_Any_byte_array (1);
	}

	/**
	 * Puts a byte array of length 3999 into an Any,
	 * which is one less than the deferred write limit.
	 * (Regression test for bug #272)
	 */
    @Test
    public void test_Any_byte_array_3999()
	{
		do_Any_byte_array (3999);
	}

	/**
	 * Puts a byte array of length 4000 into an Any,
	 * which is the deferred write limit.
	 * (Regression test for bug #272)
	 */
    @Test
    public void test_Any_byte_array_4000()
	{
		do_Any_byte_array (4000);
	}

	/**
	 * Puts a byte array of length 4001 into an Any,
	 * which is one beyond the deferred write limit.
	 * (Regression test for bug #272)
	 */
    @Test
    public void test_Any_byte_array_4001()
	{
		do_Any_byte_array (4001);
	}

	/**
	 * Puts a byte array of length 40123 into an Any,
	 * which is way beyond the deferred write limit.
	 * (Regression test for bug #272)
	 */
    @Test
    public void test_Any_byte_array_40123()
	{
		do_Any_byte_array (40123);
	}

	private void do_Any_byte_array (int length)
	{
		Any a = orb.create_any();

		byte[] b = new byte[length];
		for (int i=0; i<b.length; i++)
		{
			b[i] = (byte)(i % 256);
		}

		OctetSeqHelper.insert( a, b );

		byte[] result = OctetSeqHelper.extract( a );

		assertTrue ( "arrays are the same", b != result);
	    assertEquals( "wrong length of resulting array: ", b.length, result.length );

		for (int i=0; i<result.length; i++)
		{
			assertEquals( "wrong array element at index " + i, b[i], result[i] );
		}
	}

}
