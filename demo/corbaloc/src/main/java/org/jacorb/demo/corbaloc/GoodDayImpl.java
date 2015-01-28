package org.jacorb.demo.corbaloc;

public class GoodDayImpl
    extends GoodDayPOA
{
    private String location;
    private boolean shutdown;

    public GoodDayImpl( String location )
    {
        this.location = location;
    }

    public String hello_simple()
    {
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
