package test.wchar_seq;

import java.io.*;
import org.omg.CORBA.*;

import org.jacorb.util.*;

public class Client 
{
    private static void test( char[] arg, String what )
    {
        Debug.myAssert( arg.length == 2 &&
                        arg[ 0 ] == 'a' &&
                        arg[ 1 ] == 'a',
                        what );

    }

    public static void main( String args[] ) 
    {
        if( args.length != 1 ) 
	{
            System.out.println( "Usage: jaco test.wchar_seq.Client <ior_file>" );
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

            //narrow to test interface
            TestIf t = TestIfHelper.narrow( obj );

            wcharSeqHolder argout = new wcharSeqHolder();
            wcharSeqHolder arginout = new wcharSeqHolder( new char[]{ 'a', 'a' } );
            wcharSeqHolder argin = new wcharSeqHolder( new char[]{ 'a', 'a' } );
            
            for( int i = 0; i < 1000; i++ )
            {
                //call remote op
                test( t.test_wchar_seq( new char[]{ 'a', 'a' }, 
                                        argout, 
                                        arginout ), 
                      "result" );
                
                test( argout.value, "argout" );
            }
        }
        catch( Exception ex ) 
	{
            System.err.println( ex );
        }
    }
}

