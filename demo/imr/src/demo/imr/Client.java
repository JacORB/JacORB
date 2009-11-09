package demo.imr;

import java.io.*;
import org.omg.CORBA.*;

import org.jacorb.util.*;

public class Client 
{
    public static void main( String args[] ) 
    {
        if( args.length != 1 ) 
	{
            System.out.println( "Usage: jaco demo.imr.Client <ior_file>" );
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

            //narrow to correct interface
            SomeIf demo = SomeIfHelper.narrow( obj );

            //call remote op
            System.out.println("Client: will call server");
            demo.op();
            System.out.println("Client: successfully called server");
        }
        catch( Exception ex ) 
	{
            System.err.println( ex );
        }
    }
}

