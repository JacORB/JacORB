package test.interceptor.ctx_passing;

import java.io.*;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.PortableInterceptor.*;

public class Client 
{
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
                                "test.interceptor.ctx_passing.ClientInitializer" );
            
            // initialize the ORB.
            ORB orb = ORB.init( args, null );

            BufferedReader br =
                new BufferedReader( new FileReader( f ));

            // get object reference from command-line argument file
            org.omg.CORBA.Object obj = 
                orb.string_to_object( br.readLine() );

            br.close();

            TestObject to = TestObjectHelper.narrow( obj );
            
            Current current =
                (Current) orb.resolve_initial_references( "PICurrent" );
            
            Any any = orb.create_any();
            any.insert_string( "This is a test!" );
            
            current.set_slot( ClientInitializer.slot_id, any );
            
            System.out.println("Client added any to PICurrent");
            to.foo();            
        }
        catch( Exception ex ) 
	{
            System.err.println( ex );
        }
    }
}

