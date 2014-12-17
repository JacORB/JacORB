package org.jacorb.demo.hello;

import org.omg.CORBA.*;

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

    public String hello_wide(String wide_msg)
    {
        System.out.println("The message is: " + wide_msg );
        return "Hello World, from 1 2 3 0 *&^%$#@!@";
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
