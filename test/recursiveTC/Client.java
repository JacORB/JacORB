package test.recursiveTC;

import java.io.*;
import test.recursiveTC.TestPackage.*;
import test.recursiveTC.TestPackage.ParmPackage.*;

import org.omg.CORBA.*;

public class Client 
{
    public static void main( String args[] ) 
    {
        if( args.length != 1 ) 
	{
            System.out.println( "Usage: java test.recursiveTC.Client <ior_file>" );
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

            // if this fails, a BAD_PARAM will be thrown
            Test goodDay = TestHelper.narrow( obj );

            ParmValue pv = new ParmValue();
            pv.string_value("inner");
            Parm p = new Parm("v", pv );

            ParmValue pvi = new ParmValue();
            Parm[][] pp = new Parm[1][1];
            pp[0] = new Parm[]{p};
            pvi.nested_value( pp );

            Parm outerParm = new Parm("outer", pvi  );
            goodDay.passParm( outerParm );
        }
        catch( Exception ex ) 
	{
            System.err.println( ex );
        }
    }
}

