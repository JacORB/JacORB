package demo.hello;

import java.io.*;
import org.omg.CORBA.*;

public class Client 
{
    public static void main(String args[]) 
    {
        try 
	{
            // initialize the ORB.
            ORB orb = ORB.init( args, null );

            if( args.length == 0 )
            {
                System.err.println("usage: jaco Client <ior>");
                System.exit(1);
            }

            // get object reference from command-line argument
            org.omg.CORBA.Object obj = orb.string_to_object( args[0] );

            // and narrowed it to HelloWorld.GoodDay
            GoodDay goodDay = GoodDayHelper.narrow( obj );

            // check if stringified IOR is of the right type
            if( goodDay == null ) 
	    {
                 System.err.println("stringified IOR is not of type GoodDay");
                 System.exit( 1 );
            }

            // invoke the operation and print the result
            System.out.println( goodDay.hello() );

            // invoke the operation again and print the wide string result
            System.out.println( "wide string: " + goodDay.hello_wide("Hello Wörld, from ö 1 2 3 0 *&^%$#@!@") );
        }
        // catch CORBA system exceptions
        catch(SystemException ex) 
	{
            System.err.println(ex);
        }
    }
}

