package demo.ami;

import java.io.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;

public class Client 
{
    public static void main( String args[] ) 
    {
        if( args.length != 1 ) 
	{
            System.out.println("Usage: java demo.ami.Client <ior_file>");
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

            POA poa = 
               POAHelper.narrow (orb.resolve_initial_references ("RootPOA"));
            poa.the_POAManager().activate();

            BufferedReader br =
                new BufferedReader( new FileReader( f ));

            // get object reference from command-line argument file
            org.omg.CORBA.Object obj = 
                orb.string_to_object( br.readLine() );
            br.close();

            AsyncServer s = AsyncServerHelper.narrow( obj );
            AMI_AsyncServerHandler h = 
                new AMI_AsyncServerHandlerImpl()._this(orb);

            System.out.println ("* sending async...");
            ((_AsyncServerStub)s).sendc_op2 (h, 2);
            System.out.println ("* ...done.  Waiting for reply...");
            
            try
            {
                Thread.currentThread().sleep ( 10000 );
            }
            catch (InterruptedException ex)
            {
            }

        }
        catch( Exception ex ) 
	{
            System.err.println( ex );
        }
    }
}

