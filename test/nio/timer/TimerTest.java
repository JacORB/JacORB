
package test.nio.timer;

import org.jacorb.util.SelectorManager;
import org.jacorb.util.SelectorRequest;
import org.jacorb.util.SelectorRequestCallback;
import org.jacorb.orb.ORB;
import org.omg.CORBA.*;

public class TimerTest
{

    private static long requestDuration = 500; // millis
    private static long requestInitTime = 0;
    private final static int acceptableDelta = 10; // millis

    public static void main(String args[])
    {

        try
        {
            java.util.Properties props = new java.util.Properties();
            props.setProperty ("jacorb.connection.nonblocking", "on");
            ORB orb = (ORB) ORB.init(new String[0], props);
            SelectorManager selectorManager = orb.getSelectorManager ();

            if (selectorManager == null)
            {
                System.out.println ("SelectorManager Failed to initialize.");
                orb.shutdown (true);
                return;
            }

            // request a callback
            SelectorRequest selectorRequest =
                new SelectorRequest (new TimerCallback(), System.nanoTime() + requestDuration * 1000000);

            requestInitTime = System.nanoTime();
            selectorManager.add (selectorRequest);
            selectorRequest.waitOnCompletion (Long.MAX_VALUE);

            System.out.println ("Sleeping for a second for the callback to happen on its own.");
            Thread.sleep (1000);

            System.out.println ("Halting the SelectorManager via orb shutdown");
            orb.shutdown (true);
        }
        catch (Exception ex)
        {
            System.out.println ("Got Exception:" + ex.getMessage());
            ex.printStackTrace ();
        }
    }

    private static class TimerCallback extends SelectorRequestCallback
    {

        public boolean call (SelectorRequest request)
        {

            long actualDuration = (System.nanoTime() - requestInitTime) / 1000000;

            System.out.println ("Requested duration (millis): " + requestDuration +
                                ", actual duration (millis): " + actualDuration);

            int delta = (int)(actualDuration > requestDuration ? actualDuration - requestDuration : requestDuration - actualDuration);

            if (delta > acceptableDelta)
            {
                System.out.println ("FAILURE: Actual callback delta " + delta + " is greated than the acceptable delta: " + acceptableDelta);
            }

            return false;
        }
    }
}