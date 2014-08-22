package org.jacorb.test.nio;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Properties;
import org.jacorb.test.harness.ORBTestCase;
import org.jacorb.test.harness.TestUtils;
import org.jacorb.util.SelectorManager;
import org.jacorb.util.SelectorRequest;
import org.jacorb.util.SelectorRequestCallback;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * FrameworkClientTest.java
 *
 * Tests for corect operation of the Transport Current framework
 */

public class NIOTimerTest extends ORBTestCase
{

    private int expected_[] = new int[5];
    private int order_[] = new int[5];

    private long requestInitTime_ = 0;
    private final int acceptableDelta_ = 30; // millis

    private SelectorManager selectorManager_;
    private long requestDuration_[] = new long[5]; // millis
    private long actualDuration_[] = new long[5];
    private long delta_[] = new long[5];
    private int index_ = 0;
    private Object lockObj_[] = new Object[5];

    private class TimerCallback extends SelectorRequestCallback
    {
	private int id;
	public TimerCallback (int id)
	{
	    this.id = id;
	}

        @Override
        public boolean call (SelectorRequest request)
        {
	    order_[index_] = id;
	    index_ ++;
            actualDuration_[id] = (System.nanoTime() - requestInitTime_) / 1000000;

            delta_[id] = actualDuration_[id] > requestDuration_[id] ?
                actualDuration_[id] - requestDuration_[id] :
                requestDuration_[id] - actualDuration_[id];
            synchronized (lockObj_)
            {
                lockObj_[id].notify();
            }
            return false;
        }
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);
    }

    @Before
    public void setUp() throws Exception
    {
        requestDuration_[0] = 500;
        requestDuration_[1] = 2000;
        requestDuration_[2] = 700;
        requestDuration_[3] = 1500;
        requestDuration_[4] = 1000;
	expected_[0] = 0;
	expected_[1] = 2;
	expected_[2] = 4;
	expected_[3] = 3;
	expected_[4] = 1;

        selectorManager_ = ((org.jacorb.orb.ORB)orb).getSelectorManager ();
        for (int i = 0; i < 5; i++)
	{
	    lockObj_[i] = new Object();
	}
    }

    @Override
    protected void patchORBProperties(Properties props) throws Exception
    {
        props.setProperty ("jacorb.connection.nonblocking", "on");
    }

    @Test
    public void testTimer() throws Exception
    {
        try
        {
            requestInitTime_ = System.nanoTime();
            SelectorRequest selectorRequest[] = new SelectorRequest[5];
            for (int i = 0; i < 5; i++)
            {
		selectorRequest[i] =
		    new SelectorRequest (new TimerCallback(i),
					 requestInitTime_ +
					 requestDuration_[i] * 1000000);

                selectorManager_.add (selectorRequest[i]);
            }

            for (int i = 0; i < 5; i++)
            {
                selectorRequest[i].waitOnCompletion (Long.MAX_VALUE);
            }

            synchronized (lockObj_[1])
            {
                lockObj_[1].wait(2*requestDuration_[1]);
            }
	    long maxDelta = 0;
	    boolean inorder = true;
	    for (int i = 0; i < 5; i++)
	    {
		maxDelta = delta_[i] > maxDelta ? delta_[i] : maxDelta;

		if (order_[i] != expected_[i])
		{
		    inorder = false;
		}
	    }
            assertTrue (maxDelta <= acceptableDelta_);
	    assertTrue (inorder);
        }
        catch (InterruptedException ex)
        {
            fail ("InterruptedException");
        }
    }
}
