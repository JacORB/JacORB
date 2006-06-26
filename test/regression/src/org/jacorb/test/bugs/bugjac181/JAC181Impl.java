package org.jacorb.test.bugs.bugjac181;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.omg.CORBA.ORB;

/**
 * <code>JAC181Impl</code> is a basic server implementation.
 *
 * @author Nick Cross
 * @version $Id$
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
        System.err.println ("Called ping1!");

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
