package org.jacorb.test.bugs.bug923;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.omg.CORBA.ORB;

public class Client
{
    public static void main( String args[] )
    {
        if( args.length != 1 )
        {
            System.out.println( "Usage: jaco org.jacorb.test.bugs.bug923.Client <ior_file>" );
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

            String iorString = br.readLine();

            // get object reference from command-line argument file
            org.omg.CORBA.Object obj = orb.string_to_object( iorString );

            br.close();

            // and narrow it to HelloWorld.GoodDay
            // if this fails, a BAD_PARAM will be thrown
            DayFactory gdayFactory = DayFactoryHelper.narrow( obj );

            Base base = gdayFactory.getDay();

            GoodDay goodDay = GoodDayHelper.narrow( base );

            System.out.println( goodDay.hello_simple("Hey Mike") );

            System.out.println("Calling deleteDay");
            gdayFactory.deleteDay(goodDay);
            System.out.println("deleteDay complete");

        }
        catch( Exception ex )
        {
            ex.printStackTrace();
        }
    }
}
