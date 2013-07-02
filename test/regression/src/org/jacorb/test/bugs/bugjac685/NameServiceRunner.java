package org.jacorb.test.bugs.bugjac685;

import java.io.File;
import org.jacorb.naming.NameServer;
import org.jacorb.test.common.TestUtils;


public class NameServiceRunner
{
    public static void main (final String [] args) throws Exception
    {
        Thread thread = new Thread()
        {
            public void run()
            {
                System.out.println ("Starting the JacORB NameService");
                NameServer.main (args);
            }
        };

        thread.start ();


        try
        {
            Thread.sleep (3000);
        }
        catch (InterruptedException ie)
        {
            // OK
        }


        File file = new File (System.getProperty ("jacorb.naming.ior_filename"));

        TestUtils.printServerIOR (file);
    }
}
