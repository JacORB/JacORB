package demo.policies;

import org.omg.CORBA.*;

public class GoodDayImpl 
    extends GoodDayPOA
{
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


}
