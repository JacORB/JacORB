package test.maxConnectionEnforcement;

import java.io.*;
import java.util.*;
import org.omg.CORBA.*;

import org.jacorb.util.*;

public class Client 
{
    static TestIf remoteObj = null;
    static long callInterval = 0;
    static Random rnd = new Random();

    public static void main( String args[] ) 
    {
        if( args.length != 3 ) 
	{
            System.out.println( "Usage: jaco test.maxConnectionEnforcement.Client <ior_file> <call interval> <# of threads>" );
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
            remoteObj = TestIfHelper.narrow( obj );

            callInterval = Integer.parseInt( args[1] );

            int threads = Integer.parseInt( args[2] );
            for( int i = 0; i < threads; i++ )
            {
                (new Thread( new Runnable()
                    {
                        public void run()
                        {
                            while( true )
                            {                                   
                                try
                                {                                
                                    //call remote op
                                    remoteObj.op();
                                    System.out.println(
                                        "Thread " + 
                                        Thread.currentThread().getName() + 
                                        " made call" );
                                    
                                    Thread.sleep( Math.abs( rnd.nextLong() ) % callInterval );
                                }
                                catch( Exception e )
                                {
                                    e.printStackTrace();
                                }
                            }
                        }
                    })).start();
            }
            
            Thread.sleep( Long.MAX_VALUE );
        }
        catch( Exception ex ) 
	{
            ex.printStackTrace();
        }
    }
}


