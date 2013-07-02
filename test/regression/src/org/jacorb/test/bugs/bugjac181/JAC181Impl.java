package org.jacorb.test.bugs.bugjac181;

import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.omg.CORBA.ORB;

/**
 * <code>JAC181Impl</code> is a basic server implementation.
 *
 * @author Nick Cross
 */
public class JAC181Impl extends JAC181POA implements Configurable
{
    private ORB orb;

    /**
     * <code>ping1</code> - dummy implementation that calls destroy on the
     * server orb in order to shut it down. This allows the client to detect
     * connection closure.
     */
    public void ping1()
    {
        Thread thread1 = new Thread("ORBDestroyThread")
        {
            public void run()
            {
                try
                {
                    orb.destroy();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        thread1.start();
    }

    /**
     * <code>ping2</code> - dummy implementation.
     */
    public void ping2()
    {
        // nothing to do
    }

    public void configure(Configuration arg0) throws ConfigurationException
    {
        orb = ((org.jacorb.config.Configuration)arg0).getORB();
    }
}
