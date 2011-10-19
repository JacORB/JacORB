package org.jacorb.test.nio;

import java.util.Properties;

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

    private long requestDuration_ = 500; // millis
    private long requestInitTime_ = 0;
    private final int acceptableDelta_ = 10; // millis

    private org.omg.CORBA.ORB orb_;
    private SelectorManager selectorManager_;
    private SelectorRequest selectorRequest_;
    private long actualDuration_ = 0;
    private long delta_ = 0;

    private Object lockObj_;

    private class TimerCallback extends SelectorRequestCallback
    {
        public boolean call (SelectorRequest request)
        {
            actualDuration_ = (System.nanoTime() - requestInitTime_) / 1000000;

            delta_ = actualDuration_ > requestDuration_ ?
                actualDuration_ - requestDuration_ :
                requestDuration_ - actualDuration_;
            synchronized (lockObj_)
            {
                lockObj_.notify();
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
        java.util.Properties props = new java.util.Properties();
        props.setProperty ("jacorb.connection.nonblocking", "on");
        orb_ = ORB.init(new String[0], props);
        selectorManager_ = ((org.jacorb.orb.ORB)orb_).getSelectorManager ();
        lockObj_ = new Object();
    }

    protected void tearDown() throws Exception
    {
        orb_.shutdown(true);
    }

    public void testTimer() throws Exception
    {
        try
        {
            SelectorRequest selectorRequest =
                new SelectorRequest (new TimerCallback(),
                                     System.nanoTime() + requestDuration_ * 1000000);

            requestInitTime_ = System.nanoTime();
            selectorManager_.add (selectorRequest);
            selectorRequest.waitOnCompletion (Long.MAX_VALUE);

            synchronized (lockObj_)
            {
                lockObj_.wait(2*requestDuration_);
            }
            assert (delta_ <= acceptableDelta_);
        }
        catch (InterruptedException ex)
        {
            assert (false);
        }
    }
}
