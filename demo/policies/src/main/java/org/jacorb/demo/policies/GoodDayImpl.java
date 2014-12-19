package org.jacorb.demo.policies;

import org.omg.CORBA.*;

public class GoodDayImpl
    extends GoodDayPOA
{
    private boolean shutdown;
    private String location;

    public GoodDayImpl( String location )
    {
        this.location = location;
    }

    public String hello(int sleep)
    {
        System.out.println("Hello goes to sleep for " + sleep + " msecs.");
        try
        {
            Thread.currentThread().sleep(sleep);
        }
        catch( Exception e)
        {
            e.printStackTrace();
        }
        return "Hello World, from " + location;
    }


    public void shutdown ()
    {
        shutdown = true;
    }

    public boolean getShutdown ()
    {
        return shutdown;
    }
}
