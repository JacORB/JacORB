
package org.jacorb.test.orb.orbreinvoke;

import org.jacorb.naming.NameServer;
import org.jacorb.test.common.TestUtils;


/**
 * Starts the NameService and prints out its IOR
 * in a format understandable to ClientServerSetup.
 * @author nguyenq
 */
public class NSServiceRunner
{
    public static void main(final String[] args)
    {
        Thread thread = new Thread()
        {
            public void run()
            {
                TestUtils.getLogger().debug ("Starting the JacORB NameServer");
                NameServer.main (new String [] {});
            }
        };

        thread.start ();


        try
        {
            Thread.sleep (3000);
        }
        catch (Exception e)
        {
            // OK
        }

    }
}
