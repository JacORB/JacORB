package test.interceptor.client_flow;

import java.io.*;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.PortableInterceptor.*;

public class Client 
{
    public static ORB orb = null;

    public static void main( String args[] ) 
    {
        if( args.length != 1 ) 
	{
            System.out.println( "Usage: jaco test.interceptor.ctx_passing.Client <ior_file>" );
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

            System.setProperty( "org.omg.PortableInterceptor.ORBInitializerClass.a",
                                "test.interceptor.client_flow.ClientInitializer" );
            
            // initialize the ORB.
            orb = ORB.init( args, null );

            BufferedReader br =
                new BufferedReader( new FileReader( f ));

            // get object reference from command-line argument file
            org.omg.CORBA.Object obj = 
                orb.string_to_object( br.readLine() );

            br.close();

            TestObject to = TestObjectHelper.narrow( obj );
            
            to.foo();            
        }
        catch( Exception ex ) 
	{
            System.err.println( ex );
        }
    }
}

