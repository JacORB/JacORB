
package org.jacorb.test.orb.orbreinvoke;

import org.jacorb.naming.NameServer;

/**
 * starts the ImplementationRepository and prints out its IOR
 * in a format understandable to ClientServerSetup.
 * @author nguyenq
 */
public class NSServiceRunner {
public static void main(final String[] args)
    {
        Thread thread = new Thread()
        {
            public void run()
            {
                System.out.println ("Starting the JacORB NameServer");
                NameServer.main (new String [] {"-PrintIOR"});
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
