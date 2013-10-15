package org.jacorb.test.bugs.bugjac330;

import org.jacorb.test.orb.BasicServerImpl;


public class CustomBasicServerImpl extends BasicServerImpl
{
    public void ping()
    {
        new Thread (new Runnable()
        {
            public void run()
            {
                System.err.println ("Shutting down server");
                try
                {
                    Thread.sleep (5000);
                }
                catch (InterruptedException e)
                {
                }
                System.exit(1);
            }
        },
            "Exiting").start();
    }
}
