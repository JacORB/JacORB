package demo.hello;

import org.omg.CORBA.*;
import demo.hello.GoodDayPackage.*;
import demo.hello.GoodDayPackage.ParmPackage.*;

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

    public char getwchar(char arg0)
    {
        if (arg0 == 'X') 
            System.out.println("test succeed");
        else 
            System.out.println("test failed");
        return('Y');
   }

    public void passParm(Parm p)
    {
        System.out.println("Parm ok");
    }
}
