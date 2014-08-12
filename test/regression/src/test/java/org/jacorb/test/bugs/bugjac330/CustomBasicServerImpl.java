package org.jacorb.test.bugs.bugjac330;

import org.jacorb.test.harness.TestUtils;
import org.jacorb.test.orb.BasicServerImpl;


public class CustomBasicServerImpl extends BasicServerImpl
{
    @Override
    public void ping()
    {
        new Thread (new Runnable()
        {
            public void run()
            {
                TestUtils.getLogger().debug ("Shutting down server");
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

    @Override
    public void pass_in_long(int x)
    {
        System.exit(1);
    }
}
