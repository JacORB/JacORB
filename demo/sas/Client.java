package demo.sas;

import java.io.*;
import org.omg.CORBA.*;
import org.ietf.jgss.*;
import org.jacorb.util.*;

/**
 * This is the client side of the sas demo. It just calls the single
 * operation "printCert()" of the server. As you can see, sas is fully
 * transparent.
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class Client
{
    public static void main( String args[] )
    {
        if( args.length != 1 )
	{
            System.out.println( "Usage: java demo.sas.Client <ior_file>" );
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

            try {
                // create my identity
                GSSManager gssManager = org.jacorb.security.sas.CSSInitializer.gssManager;
                Oid myMechOid = new Oid(org.omg.GSSUP.GSSUPMechOID.value.replaceFirst("oid:", ""));
                byte[] name = org.jacorb.security.sas.GSSUPNameSpi.encode("MeUser", "MePassword", "MeTarget");
                GSSName myName = gssManager.createName(name, null, myMechOid);
                GSSCredential myCred = gssManager.createCredential(myName, GSSCredential.DEFAULT_LIFETIME, myMechOid, GSSCredential.INITIATE_ONLY);
                org.jacorb.security.sas.CSSInvocationInterceptor.setMyCredential(myCred);
            } catch (GSSException e) {
                System.out.println("GSSException "+e.getMessage()+": "+e.getMajorString()+": "+e.getMinorString());
                e.printStackTrace();
                System.exit(1);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }

            BufferedReader br = new BufferedReader( new FileReader( f ));

            // get object reference from command-line argument file
            org.omg.CORBA.Object obj = orb.string_to_object( br.readLine() );
            br.close();

            //narrow to right type
            SASDemo demo = SASDemoHelper.narrow( obj );

            //call single operation
            demo.printSAS();
            demo.printSAS();
            demo.printSAS();

            System.out.println( "Call to server succeeded" );
        }
        catch( Exception ex )
	{
            ex.printStackTrace();
        }
    }
}
