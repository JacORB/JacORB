package test.timerqueue;

import java.util.Calendar;
import org.jacorb.util.TimerQueue;
import org.jacorb.util.TimerQueueAction;
import org.jacorb.orb.ORB;


public class Test
{
    public TimerQueue tq;
    private ORB orb;


    public class TestAction extends TimerQueueAction 
    {
        private int id;
        public TestAction (long duration, int ident) 
        {
            super(duration);
            id = ident;
        }

        public void expire ()
        {
            System.out.println ("TestAction[" + id + 
                                "].expire called on thread[" + 
                                Thread.currentThread().getId() + "] at " + 
                                nowstr());
        }
    }

    public class TestNotifyWaiter extends Thread
    {
        public Object sync = null;
        private TimerQueueAction act;
        private int id;
        

        public TestNotifyWaiter (long duration, int ident)
        {
            id = ident;
            sync = new Object();
            act = new TimerQueueAction (duration, sync);
            start();
        }

        public TestNotifyWaiter (TestAction action)
        {
            id = 0;
            act = action;
            start();
        }

        public void cancel ()
        {
            tq.remove (act);
        }

        public void run ()
        {
            long tid = Thread.currentThread().getId();
            if (sync == null) {
                tq.add(act);
                System.out.println ("Thread[" + tid + 
                                    "] not waiting, action should fire independently");
                return;
            }
     
            synchronized (sync) {
                System.out.println ("Thread[" + tid + 
                                    "] entering wait for id = " + id + 
                                    " at " + nowstr());
                tq.add(act);
                try {
                    sync.wait();
                }
                catch (InterruptedException ex) {
                    System.out.println ("Thread[" + tid + "] interrupted");
                }
                System.out.println ("Thread[" + tid +
                                    "] finishing for id = " + id + 
                                    " at " + nowstr());
            }
        }
                
    }

    public static String nowstr ()
    {
        Calendar c = Calendar.getInstance();
        String result = 
            "" + c.get(Calendar.HOUR) +
            ":" + c.get(Calendar.MINUTE) +
            ":" + c.get(Calendar.SECOND) + 
            ".";
        int msec = 1000 + c.get(Calendar.MILLISECOND);
        String msecStr = Integer.toString(msec);
        result += msecStr.substring(1);
        return result;
    }


    public void setUp()
        throws Exception
    {
        orb = (ORB) ORB.init(new String[0], null);
        tq = orb.getTimerQueue();
    }

    public void runTest ()
    {
        int initial = Thread.activeCount();
        System.out.println ("Starting test at " + nowstr());
        TestNotifyWaiter waiters[] = new TestNotifyWaiter[10];
        long dur = 1000;
        for (int i = 0; i < 5; i++) {
            waiters[i] = new TestNotifyWaiter (dur, i+1);
            dur += 500;
            waiters[i+5] = new TestNotifyWaiter ( new TestAction(dur, i+6));
            dur += 500;
        }
        try {
            Thread.sleep (10);
            System.out.println ("All threads started at " + nowstr());
            int active = Thread.activeCount() - initial;
            System.out.println ("Active thread count is " + active +
                                " wait count = " + tq.depth());
            Thread.sleep (1000);
            waiters[9].cancel();
            waiters[8].cancel();
            System.out.println ("After canceling wait count = " + tq.depth());
            Thread.sleep (5000);
            System.out.println ("Test exiting at " + nowstr());            
            active = Thread.activeCount() - initial;
            System.out.println ("stuck threads = " + active);
        }
        catch( InterruptedException ex )
	{
            ex.printStackTrace();
        }
        finally
        {
            tq.halt();
        }

    }

    public static void main( String args[] )
    {

        Test c = new Test();
        try {
            c.setUp();
            c.runTest();
        }
        catch( Exception ex ) {
            ex.printStackTrace();
        }


    }



}