package org.jacorb.demo.sas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.security.Principal;
import java.security.PrivilegedAction;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.omg.CORBA.ORB;

/**
 * This is the client side of the sas demo. It just calls the single
 * operation "printCert()" of the server. As you can see, sas is fully
 * transparent.
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class KerberosClient {
    private static Principal myPrincipal = null;
    private static Subject mySubject = null;
    private static ORB orb = null;

    public KerberosClient(String args[]) {

        try {
            // initialize the ORB.
            orb = ORB.init(args, null);

            // get the server
            File f = new File(args[0]);
            if (!f.exists()) {
                System.out.println("File " + args[0] + " does not exist.");
                System.exit(-1);
            }
            if (f.isDirectory()) {
                System.out.println("File " + args[0] + " is a directory.");
                System.exit(-1);
            }
            BufferedReader br = new BufferedReader(new FileReader(f));
            org.omg.CORBA.Object obj = orb.string_to_object(br.readLine());
            br.close();
            SASDemo demo = SASDemoHelper.narrow(obj);

            //call single operation
            demo.printSAS();
            demo.printSAS();
            demo.printSAS();

            System.out.println("Call to server succeeded");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String args[]) {
        if (args.length != 3) {
            System.out.println("Usage: java demo.sas.KerberosClient <ior_file> <username> <password>");
            System.exit(1);
        }

        // login - with Kerberos
        LoginContext loginContext = null;
        try {
            JaasTxtCalbackHandler txtHandler = new JaasTxtCalbackHandler();
            txtHandler.setMyUsername(args[1]);
            txtHandler.setMyPassword(args[2].toCharArray());
            loginContext = new LoginContext("KerberosClient", txtHandler);
            loginContext.login();
        } catch (LoginException le) {
            System.out.println("Login error: " + le);
            System.exit(1);
        }
        mySubject = loginContext.getSubject();
        myPrincipal = (Principal) mySubject.getPrincipals().iterator().next();
        System.out.println("Found principal " + myPrincipal.getName());

        // run in privileged mode
        final String[] finalArgs = args;
        try {
            Subject.doAs(mySubject, new PrivilegedAction() {
                public Object run() {
                    try {
                        KerberosClient client = new KerberosClient(finalArgs);
                        orb.run();
                    } catch (Exception e) {
                        System.out.println("Error running program: "+e);
                    }
                    System.out.println("Exiting privileged operation");
                    return null;
                }
            });
        } catch (Exception e) {
            System.out.println("Error running privileged: "+e);
        }
    }
}
