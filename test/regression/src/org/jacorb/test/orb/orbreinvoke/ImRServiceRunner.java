
package org.jacorb.test.orb.orbreinvoke;

import org.jacorb.imr.ImplementationRepositoryImpl;

/**
 * starts the ImplementationRepository and prints out its IOR
 * in a format understandable to ClientServerSetup.
 * @author nguyenq
 */
public class ImRServiceRunner {
public static void main(final String[] args)
    {
        Thread thread = new Thread()
        {
            public void run()
            {
                System.out.println ("Starting the JacORB ImR");
                ImplementationRepositoryImpl.main (new String [] {"-PrintIOR"});
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
