package test.longlongseq;

import java.io.*;
import org.omg.CORBA.*;

import org.jacorb.util.*;

public class Client 
{
    private static void test( long[] arg )
    {
        Debug.myAssert( arg[0] == Long.MIN_VALUE, "" );
        Debug.myAssert( arg[1] == Long.MIN_VALUE, "" );
    }

    public static void main( String args[] ) 
    {
        if( args.length != 1 ) 
	{
            System.out.println( "Usage: jaco test.longlongseq.Client <ior_file>" );
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

            TestIf t = TestIfHelper.narrow( obj );

            long[] l = new long[]{ Long.MIN_VALUE, Long.MIN_VALUE };

            SeqLongLongHolder h_out = new SeqLongLongHolder();
            SeqLongLongHolder h_inout = new SeqLongLongHolder();

            h_inout.value = l;

            for( int i = 0; i < 1000; i++ )
            {
                test( t.test1( l, h_out, h_inout ));
                test( h_out.value );
                test( h_inout.value );

                test( t.test2( l, h_out ));
                test( h_out.value );

                t.test3( h_inout );
                test( h_inout.value );
            }
        }
        catch( Exception ex ) 
	{
            System.err.println( ex );
        }
    }
}

