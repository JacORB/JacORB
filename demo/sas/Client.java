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

            // DRR // set default SAS context
            //demo.sas.SAS_CSS.DefaultPrincipalName("David R Robison");
            //demo.sas.SAS_CSS.DefaultGSSUPAuthenticationContext("David", "Corinne", "DRR");

            try {
                java.security.Provider p = new org.jacorb.security.sas.GSSUPProvider();
                //Oid mechOid = new Oid(org.omg.GSSUP.GSSUPMechOID.value.replaceFirst("oid:",""));
                GSSManager gssManager = GSSManager.getInstance();

                // load any mechanism providors
                for (int i = 1; i <= 16; i++) {
                    String mechOID = org.jacorb.util.Environment.getProperty("jacorb.security.sas.mechanism."+i+".oid");
                    if (mechOID == null) continue;
                    String mechProvider = org.jacorb.util.Environment.getProperty("jacorb.security.sas.mechanism."+i+".provider");
                    if (mechProvider == null) continue;
                    try {
                        org.ietf.jgss.Oid oid = new org.ietf.jgss.Oid(mechOID);
                        Class cls = Class.forName (mechProvider);
                        java.lang.Object provider = cls.newInstance ();
                        gssManager.addProviderAtFront((java.security.Provider)provider, oid);
                        Debug.output(1, "Adding GSS SPI Provider: " + oid + " " + mechProvider);
                    } catch (Exception e) {
                        Debug.output( 1, "GSSProvider "+mechOID+" "+mechProvider + " error: " +e );
                    }
                }

                org.jacorb.security.sas.GSSUPProvider.setDefaultSubject("MeUser", "MePassword", "MeTarget");
                org.jacorb.security.sas.GSSUPProvider.setORB(orb);
                Oid mechOid = new org.ietf.jgss.Oid(org.jacorb.util.Environment.getProperty("jacorb.security.sas.mechanism.1.oid"));
                GSSCredential cred = gssManager.createCredential(gssManager.createName("".getBytes(), null, mechOid), GSSCredential.DEFAULT_LIFETIME, mechOid, GSSCredential.INITIATE_ONLY);
                GSSName peerName = gssManager.createName("".getBytes(), null, mechOid);
                GSSContext context = gssManager.createContext(peerName, mechOid, cred, GSSContext.DEFAULT_LIFETIME);
                byte[] token = new byte[0];
                while (!context.isEstablished()) token = context.initSecContext(token, 0, token.length);
                //GSSName targetName = gssManager.createName("User@Server".getBytes(), null /*GSSName.NT_HOSTBASED_SERVICE*/, mechOid);
                //GSSName loginName = gssManager.createName("User/Password".getBytes(), null, mechOid);
                //GSSCredential cred = gssManager.createCredential(loginName, GSSCredential.DEFAULT_LIFETIME, mechOid, GSSCredential.INITIATE_ONLY);
                //GSSContext context = gssManager.createContext(targetName, mechOid, cred, GSSContext.DEFAULT_LIFETIME);
                //org.omg.GSSUP.InitialContextToken initialContextToken = new org.omg.GSSUP.InitialContextToken("USERuser".getBytes(), "PASSWORDpassword".getBytes(), new byte[0]);
                //try {
                //    org.omg.CORBA.portable.OutputStream oStr = orb.create_output_stream();
                //    org.omg.GSSUP.InitialContextTokenHelper.write(oStr, initialContextToken);
                //    org.omg.CORBA.portable.InputStream  iStr = oStr.create_input_stream();
                //    byte[] flat = new byte[2048];
                //    iStr.read_octet_array(flat, 0, 2048);
                //    context.initSecContext(flat, 0, 2048);
                //} catch (Exception e) {
                //    System.out.println("Could not set security service context: " + e);
                //}
                //System.out.println("Export="+context.export());
                org.jacorb.security.sas.CSSInvocationInterceptor.setInitialContext(token);
            } catch (GSSException e) {
                System.out.println("GSSException "+e.getMessage()+": "+e.getMajorString()+": "+e.getMinorString());
                e.printStackTrace();
                System.exit(1);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }

            BufferedReader br =
                new BufferedReader( new FileReader( f ));

            // get object reference from command-line argument file
            org.omg.CORBA.Object obj =
                orb.string_to_object( br.readLine() );

            br.close();

            //narrow to right type
            SASDemo demo = SASDemoHelper.narrow( obj );

            //call single operation
            demo.printSAS();

            System.out.println( "Call to server succeeded" );
        }
        catch( Exception ex )
	{
            ex.printStackTrace();
        }
    }
}

