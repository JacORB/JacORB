package org.jacorb.test.bugs.bug272;

import junit.framework.*;

import org.omg.CORBA.*;

/**
 * Test for bug 272, byte array in Any.
 * 
 * @author Andre Spiegel
 * @version $Id$
 */
public class TestCase extends junit.framework.TestCase
{
	public TestCase (String name)
	{
		super (name);
	}
	
	public static Test suite()
	{
		TestSuite suite = new TestSuite ("bug 272 byte array in Any");
		
		suite.addTest (new TestCase ("test_Any_byte_array_1"));
		suite.addTest (new TestCase ("test_Any_byte_array_3999"));
		suite.addTest (new TestCase ("test_Any_byte_array_4000"));
		suite.addTest (new TestCase ("test_Any_byte_array_4001"));
		suite.addTest (new TestCase ("test_Any_byte_array_40123"));

		return suite;
	}
	
	/**
	 * Puts a byte array of length 1 into an Any.
	 * (Regression test for bug #272)
	 */
	public void test_Any_byte_array_1()
	{
		do_Any_byte_array (1);
	}

	/**
	 * Puts a byte array of length 3999 into an Any,
	 * which is one less than the deferred write limit.
	 * (Regression test for bug #272)
	 */
	public void test_Any_byte_array_3999()
	{
		do_Any_byte_array (3999);
	}

	/**
	 * Puts a byte array of length 4000 into an Any,
	 * which is the deferred write limit.
	 * (Regression test for bug #272)
	 */
	public void test_Any_byte_array_4000()
	{
		do_Any_byte_array (4000);
	}

	/**
	 * Puts a byte array of length 4001 into an Any,
	 * which is one beyond the deferred write limit.
	 * (Regression test for bug #272)
	 */
	public void test_Any_byte_array_4001()
	{
		do_Any_byte_array (4001);
	}

	/**
	 * Puts a byte array of length 40123 into an Any,
	 * which is way beyond the deferred write limit.
	 * (Regression test for bug #272)
	 */
	public void test_Any_byte_array_40123()
	{
		do_Any_byte_array (40123);	
	}

	private void do_Any_byte_array (int length)
	{
		ORB orb = ORB.init();
		Any a = orb.create_any();
		
		byte[] b = new byte[length];
		for (int i=0; i<b.length; i++) b[i] = (byte)(i % 256);
		
		OctetSeqHelper.insert( a, b );
		
		byte[] result = OctetSeqHelper.extract( a );
		
		assertTrue ( "arrays are the same", b != result);
	    assertEquals( "wrong length of resulting array: ", b.length, result.length );
			
		for (int i=0; i<result.length; i++)
			assertEquals( "wrong array element at index " + i, b[i], result[i] );
	}

}
