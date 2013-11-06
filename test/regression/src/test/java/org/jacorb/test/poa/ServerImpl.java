package org.jacorb.test.poa;


public class ServerImpl extends MyServerPOA
{
    private Object lock = new Object();

    public void block()
    {
        System.out.println ("### Block - entering");
        synchronized (lock)
        {
            try
            {
                lock.wait(15000);
            }
            catch (InterruptedException e)
            {
            }
        }
        System.out.println ("### Block - exiting");
    }

    public boolean testCall()
    {
        System.out.println ("### testCall");
        return true;
    }
}
