package test.load.simple;

import java.io.*;
import java.util.*;
import org.omg.CORBA.*;

public class Client 
{
    public static void main( String args[] ) 
    {
        if( args.length != 1 ) 
	{
            System.out.println( "Usage: jaco test.load.simple.Client <ior_file>" );
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
            
            for( int i = 0; i < 40; i++ )
            {
                Thread th = new Thread( new CallRunnable( goodDay, i ));
                th.start();
            }
        }
        catch( Exception ex ) 
	{
            System.err.println( ex );
        }
    }
    
    private static class CallRunnable 
        implements Runnable
    {
        private int me = 0;
        private GoodDay good_day = null;
        private Random rnd = null;
        
        public CallRunnable( GoodDay good_day,
                             int me )
        {
            this.good_day = good_day;
            this.me = me;

            rnd = new Random();
        }

        public void run()
        {
            try
            {
                while( true )
                {
                    good_day.hello_simple( me );
                    
                    //Thread.currentThread().sleep( Math.abs( rnd.nextInt() % 100));
                }
            }
            catch( Throwable th )
            {
                th.printStackTrace();
                
                System.exit( -1 );
            }
        }
    }
}

