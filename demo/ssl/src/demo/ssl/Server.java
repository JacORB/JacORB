package demo.ssl;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

/**
 * This is the server part of the ssl demo.
 *
 * @author Nicolas Noffke
 */

public class Server
    extends SSLDemoPOA
{
    /**
     * This method is from the IDL--interface. It prints out the
     * received client cert (if available).
     */
    public void printCert()
    {
        System.out.println("[Server] invoked printCert()");
    }


    public static void main( String[] args ) throws Exception
    {
        if( args.length != 1 && args.length != 2)
        {
            System.out.println( "Usage: java demo.ssl.Server <ior_file> <killfile>" );
            System.exit( -1 );
        }

        ORB orb = ORB.init( args, null );

        POA poa = (POA)
        orb.resolve_initial_references( "RootPOA" );

        poa.the_POAManager().activate();

        org.omg.CORBA.Object demo = poa.servant_to_reference( new Server());

        PrintWriter pw = new PrintWriter( new FileWriter( args[ 0 ] ));

        // print stringified object reference to file
        pw.println( orb.object_to_string( demo ));

        pw.flush();
        pw.close();

        if (args.length == 2)
        {
            File killFile = new File(args[1]);
            while(!killFile.exists())
            {
                Thread.sleep(1000);
            }
            orb.shutdown(true);
        }
        else
        {
            orb.run();
        }
    }
} // Server
