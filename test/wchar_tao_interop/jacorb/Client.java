import org.omg.CORBA.*;

import java.io.*;
/**
 * Client.java
 *
 *
 * Created: Mon Sep  3 19:28:34 2001
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class Client 
{

    public static void main( String[] args )
        throws Exception
    {
        if( args.length != 1 ) 
	{
            System.out.println( "Usage: jaco Client <ior_file>" );
            System.exit( 1 );
        }


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
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init( args, null );

        BufferedReader br =
            new BufferedReader( new FileReader( f ));

        // get object reference from command-line argument file
        org.omg.CORBA.Object obj = 
            orb.string_to_object( br.readLine() );

        br.close();

        GoodDay gd = GoodDayHelper.narrow( obj );

        System.out.println( "hello_simple(): " + gd.hello_simple());
        System.out.println( "hello_wide(): " + 
                            gd.hello_wide( "daß düdelt und dödelt"));

        try
        {
            gd.test();
        }
        catch( GoodDayPackage.WStringException wse )
        {
            System.out.println("Exception: " + wse.why );
        }
    }        
}// Client

