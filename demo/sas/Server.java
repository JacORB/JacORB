package demo.sas;

import java.io.*;

import java.security.cert.X509Certificate;

import org.omg.PortableServer.POA;
import org.omg.Security.*;
import org.omg.CORBA.ORB;
import org.ietf.jgss.*;
import org.jacorb.util.*;

//import org.jacorb.security.level2.*;

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
    public static final int AUTHTYPE_NT_User_Principal     = org.jacorb.orb.ORBConstants.JACORB_ORB_ID | 0x01;
    public static final int AUTHTYPE_NT_Group_Principal    = org.jacorb.orb.ORBConstants.JACORB_ORB_ID | 0x02;
    public static final int AUTHTYPE_NT_Domain_Principal   = org.jacorb.orb.ORBConstants.JACORB_ORB_ID | 0x03;
    public static final int AUTHTYPE_NT_Unknown_Principal  = org.jacorb.orb.ORBConstants.JACORB_ORB_ID | 0x04;

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
            org.omg.CORBA.Any any1 = current.get_slot( org.jacorb.security.sas.TSSInitializer.sourceNameSlotID );
            org.omg.GSSUP.InitialContextToken token = org.jacorb.security.sas.GSSUPNameSpi.decode(any1.extract_string().getBytes());
            org.omg.CORBA.Any any2 = current.get_slot( org.jacorb.security.sas.TSSInitializer.contextMsgSlotID );
            org.omg.CSI.EstablishContext msg = org.omg.CSI.EstablishContextHelper.extract(any2);
            System.out.println("printSAS for user " + (new String(token.username)));

            // print authentication
            org.omg.CSI.AuthorizationElement[] auth = msg.authorization_token;
            for (int i = 0; i < auth.length; i++)
            {
                String name = new String(auth[i].the_element);
                switch (auth[i].the_type)
                {
                case AUTHTYPE_NT_User_Principal: System.out.println("\tUser: " + name); break;
                case AUTHTYPE_NT_Group_Principal: System.out.println("\tGroup: " + name); break;
                case AUTHTYPE_NT_Domain_Principal: System.out.println("\tDomain: " + name); break;
                case AUTHTYPE_NT_Unknown_Principal: System.out.println("\tUnknown: " + name); break;
                }
            }
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
