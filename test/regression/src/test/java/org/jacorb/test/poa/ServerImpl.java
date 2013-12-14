package org.jacorb.test.poa;


public class ServerImpl extends MyServerPOA
{
    private Object lock = new Object();

    public void block()
    {
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
    }

    public boolean testCall()
    {
        return true;
    }
}
