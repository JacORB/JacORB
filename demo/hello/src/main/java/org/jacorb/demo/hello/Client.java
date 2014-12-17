package demo.hello;

import java.io.*;
import org.omg.CORBA.*;

public class Client
{
    public static void main( String args[] )
    {
        if( args.length != 1 )
        {
            System.out.println( "Usage: jaco demo.hello.Client <ior_file>" );
            System.exit( 1 );
        }

        try
        {
            File f = new File( args[ 0 ] );

            //check if file exists
            if( ! f.exists() )
            {
                System.out.println("File " + args[0] +
                " does not exist.");

                System.exit( -1 );
            }

            //check if args[0] points to a directory
            if( f.isDirectory() )
            {
                System.out.println("File " + args[0] +
                " is a directory.");

                System.exit( -1 );
            }

            // initialize the ORB.
            ORB orb = ORB.init( args, null );

            BufferedReader br =
                new BufferedReader( new FileReader( f ));

            // get object reference from command-line argument file
            org.omg.CORBA.Object obj =
                orb.string_to_object( br.readLine() );

            br.close();

            // and narrow it to HelloWorld.GoodDay
            // if this fails, a BAD_PARAM will be thrown
            GoodDay goodDay = GoodDayHelper.narrow( obj );


            // invoke the operation and print the result
            System.out.println( goodDay.hello_simple() );

            // invoke the operation again and print the wide string result
            System.out.println( "wide string: " +
                    goodDay.hello_wide( "Hello World, from 1 2 3 0 *&^%$#@!@"));

        }
        catch( Exception ex )
        {
            ex.printStackTrace();
        }
    }
}

