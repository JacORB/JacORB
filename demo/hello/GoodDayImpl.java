package demo.hello;

import org.omg.CORBA.*;

public class GoodDayImpl 
    extends GoodDayPOA
{
    private String location;

    // constructor
    public GoodDayImpl( String location ) 
    {
	// initialize location
        this.location = location;
    }

    // method
    public String hello() 
    {
        return "Hello World, from " + location;
    }

    public String hello_wide(String wide_msg) 
    {
        System.out.println("The message is: " + wide_msg );
        return "Hello Wörld, from ö 1 2 3 0 *&^%$#@!@";
    }


}
