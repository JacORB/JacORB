package demo.sas;

import java.io.*;
import java.util.*;
import org.omg.CORBA.*;
import org.jacorb.util.*;

/**
 * This is the client side of the sas demo. It just calls the single
 * operation "printCert()" of the server. As you can see, sas is fully
 * transparent.
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class SslClient
{
    public static void main( String args[] )
    {
        if( args.length != 1 )
	{
            System.out.println( "Usage: java demo.sas.SslClient <ior_file>" );
            System.exit( 1 );
        }

        try
	{
            File f = new File( args[ 0 ] );

            //check if file exists
            if( ! f.exists() )
            {
                System.out.println("File " + args[0] + " does not exist.");

                System.exit( -1 );
            }

            //check if args[0] points to a directory
            if( f.isDirectory() )
            {
                System.out.println("File " + args[0] + " is a directory.");

                System.exit( -1 );
            }

            // initialize the ORB.
            ORB orb = ORB.init( args, null );

            BufferedReader br = new BufferedReader( new FileReader( f ));

            // get object reference from command-line argument file
            org.omg.CORBA.Object obj = orb.string_to_object( br.readLine() );
            br.close();

            //narrow to right type
            SASDemo demo = SASDemoHelper.narrow( obj );

            //call single operation
            demo.printSAS();
            demo.printSAS();
            demo.printSAS();

            System.out.println( "Call to server succeeded" );
        }
        catch( Exception ex )
	{
            ex.printStackTrace();
        }
    }
}
