package test.load.simple;

import org.omg.CORBA.*;

public class GoodDayImpl 
    extends GoodDayPOA
{
    private String location;

    public GoodDayImpl( String location ) 
    {
        this.location = location;
    }

    public void hello_simple( int who ) 
    {
        System.out.println("Rcv call from " + who);
    }
}
