package org.jacorb.demo.ami;

public class AsyncServerImpl extends AsyncServerPOA
{
    private boolean shutdown;

    public int operation (int a, int b)
    {
        try
        {
            Thread.currentThread().sleep( 2000 );
        }
        catch (InterruptedException e)
        {
        }
        return a + b;
    }

    public int op2 (int a) throws MyException
    {
        try
        {
            Thread.currentThread().sleep( 2000 );
        }
        catch (InterruptedException e)
        {
        }
        throw new MyException ("Hello exceptional world");
    }

    public boolean getShutdown ()
    {
        return shutdown;
    }

    public void shutdown ()
    {
        shutdown = true;
    }
}
