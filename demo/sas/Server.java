package demo.sas;

import java.io.*;

import java.security.cert.X509Certificate;

import org.omg.PortableServer.POA;
import org.omg.SecurityLevel2.*;
import org.omg.Security.*;
import org.omg.CORBA.ORB;
import org.ietf.jgss.*;
import org.jacorb.util.*;

import org.jacorb.security.level2.*;

/**
 * This is the server part of the sas demo. It demonstrates
 * how to get access to the certificates that the client sent
 * for mutual authentication. The certificate chain can be
 * accessed via the Security Level 2 interfaces.
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class Server
    extends SASDemoPOA
{
    private ORB orb;

    public Server(ORB orb)
    {
        this.orb = orb;
    }

    /**
     * This method is from the IDL--interface. It prints out the
     * received client cert (if available).
     */
    public void printSAS()
    {
        try
        {
            org.omg.PortableInterceptor.Current current = (org.omg.PortableInterceptor.Current) orb.resolve_initial_references( "PICurrent" );
            org.omg.CORBA.Any any = current.get_slot( org.jacorb.security.sas.TSSInitializer.sourceNameSlotID );
            org.omg.GSSUP.InitialContextToken token = org.jacorb.security.sas.GSSUPNameSpi.decode(any.extract_string().getBytes());
            System.out.println("printSAS for user " + (new String(token.username)));
        }
        catch (Exception e)
        {
            System.out.println("printSAS Error: " + e);
        }
    }

    public static void main( String[] args )
    {
        if( args.length != 1 )
	{
            System.out.println( "Usage: java demo.sas.Server <ior_file>" );
            System.exit( -1 );
        }

        try
        {
            ORB orb = ORB.init( args, null );

            POA poa = (POA)
                orb.resolve_initial_references( "RootPOA" );

            try
            {
                // create my identity
                GSSManager gssManager = org.jacorb.security.sas.TSSInitializer.gssManager;
                Oid myMechOid = new Oid(org.omg.GSSUP.GSSUPMechOID.value.replaceFirst("oid:", ""));
                GSSName myName = gssManager.createName("".getBytes(), GSSName.NT_ANONYMOUS, myMechOid);
                GSSCredential myCred = gssManager.createCredential(myName, GSSCredential.DEFAULT_LIFETIME, myMechOid, GSSCredential.ACCEPT_ONLY);
                org.jacorb.security.sas.TSSInvocationInterceptor.setMyCredential(myCred);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            poa.the_POAManager().activate();

            org.omg.CORBA.Object demo =
                poa.servant_to_reference( new Server( orb ));

            PrintWriter pw =
                new PrintWriter( new FileWriter( args[ 0 ] ));

            // print stringified object reference to file
            pw.println( orb.object_to_string( demo ));

            pw.flush();
            pw.close();

            // wait for requests
	    orb.run();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
} // Server
