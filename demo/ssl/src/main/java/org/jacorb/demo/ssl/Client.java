package org.jacorb.demo.ssl;

import java.io.*;
import org.omg.CORBA.*;

/**
 * This is the client side of the ssl demo. It just calls the single
 * operation "printCert()" of the server. As you can see, ssl is fully
 * transparent.
 *
 * @author Nicolas Noffke
 */

public class Client
{
    public static void main( String args[] ) throws Exception
    {
        File file = new File( args[ 0 ] );

        //check if file exists
        if( !file.exists() )
        {
            System.out.println("File " + args[0] + " does not exist.");

            System.exit( -1 );
        }

        //check if args[0] points to a directory
        if( file.isDirectory() )
        {
            System.out.println("File " + args[0] + " is a directory.");

            System.exit( -1 );
        }

        // initialize the ORB.
        ORB orb = ORB.init( args, null );

        BufferedReader br =
            new BufferedReader( new FileReader( file ));

        // get object reference from command-line argument file
        org.omg.CORBA.Object obj =
            orb.string_to_object( br.readLine() );

        br.close();

        //narrow to right type
        SSLDemo demo = SSLDemoHelper.narrow( obj );

        System.out.println("[Client] about to invoke printCert()");

        try
        {
            //call single operation
            demo.printCert();

            System.out.println( "[Client] Call to server succeeded" );
        }
        catch(Exception e)
        {
            System.out.println("[Client] failed to invoke: " + e.getMessage());
        }

        if ( args.length > 1 )
        {
            demo.shutdown();
        }
    }
}
