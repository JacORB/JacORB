package org.jacorb.test.nio;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jacorb.util.SelectorManager;
import org.jacorb.util.SelectorRequest;
import org.jacorb.util.SelectorRequestCallback;
import org.omg.CORBA.ORB;

/**
 * FrameworkClientTest.java
 *
 * Tests for corect operation of the Transport Current framework
 */

public class NIOTimerTest extends TestCase
{

    private int expected_[] = new int[5];
    private int order_[] = new int[5];

    private long requestInitTime_ = 0;
    private final int acceptableDelta_ = 10; // millis

    private org.omg.CORBA.ORB orb_;
    private SelectorManager selectorManager_;
    private SelectorRequest selectorRequest_;
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


    public static Test suite()
    {
        return new TestSuite(NIOTimerTest.class);
    }

    public NIOTimerTest(String name)
    {
        super (name);
    }

    protected void setUp() throws Exception
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
  
        java.util.Properties props = new java.util.Properties();
        props.setProperty ("jacorb.connection.nonblocking", "on");
        orb_ = ORB.init(new String[0], props);
        selectorManager_ = ((org.jacorb.orb.ORB)orb_).getSelectorManager ();
        for (int i = 0; i < 5; i++)
	{
	    lockObj_[i] = new Object();
	}
    }

    protected void tearDown() throws Exception
    {
        orb_.shutdown(true);
    }

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
            assert (maxDelta <= acceptableDelta_);
	    assert (inorder);
        }
        catch (InterruptedException ex)
        {
            assert (false);
        }
    }
}
