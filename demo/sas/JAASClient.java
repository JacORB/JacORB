package demo.sas;

import java.io.*;
import java.util.*;
import org.omg.CORBA.*;
import org.ietf.jgss.*;
import org.jacorb.util.*;
import java.security.Principal;
import javax.security.auth.*;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;
import javax.security.auth.spi.*;
import com.tagish.auth.*;

/**
 * This is the client side of the sas demo. It just calls the single
 * operation "printCert()" of the server. As you can see, sas is fully
 * transparent.
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class JAASClient
{
    private static LoginCallbackHandler loginCallbackHandler = new LoginCallbackHandler();
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

            /////////////////////
            // Login with JAAS //
            /////////////////////

            // use the configured LoginModules for the "Login" entry
            LoginContext lc = null;
            try
            {
                lc = new LoginContext("NTLogin", loginCallbackHandler);
            }
            catch (LoginException le)
            {
                le.printStackTrace();
                System.exit(-1);
            }

            try
            {
                // attempt authentication
                lc.login();
            }
            catch (AccountExpiredException aee)
            {
                System.out.println("Your account has expired. Please notify your administrator.");
                System.exit(-1);
            }
            catch (CredentialExpiredException cee)
            {
                System.out.println("Your credentials have expired.");
                System.exit(-1);
            }
            catch (FailedLoginException fle)
            {
                System.out.println("Authentication Failed");
                System.exit(-1);
            }
            catch (Exception e)
            {
                System.out.println("Unexpected Exception - unable to continue");
                e.printStackTrace();
                System.exit(-1);
            }

            // let's see what Principals we have
            Iterator principalIterator = lc.getSubject().getPrincipals().iterator();
            System.out.println("Authenticated user has the following Principals:");
            while (principalIterator.hasNext())
            {
                Principal p = (Principal) principalIterator.next();
                System.out.println("\t" + p.toString());
            }

            System.out.println("User has " + lc.getSubject().getPublicCredentials().size() + " Public Credential(s)");

            try {
                // create my identity
                GSSManager gssManager = org.jacorb.security.sas.CSSInitializer.gssManager;
                Oid myMechOid = new Oid(org.omg.GSSUP.GSSUPMechOID.value.replaceFirst("oid:", ""));
                byte[] name = org.jacorb.security.sas.GSSUPNameSpi.encode(loginCallbackHandler.username, loginCallbackHandler.password, loginCallbackHandler.domain);
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
