package demo.sas;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.security.Principal;
import java.security.PrivilegedAction;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

/**
 * This is the server part of the sas demo. It demonstrates
 * how to get access to the certificates that the client sent
 * for mutual authentication. The certificate chain can be
 * accessed via the Security Level 2 interfaces.
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class KerberosServer extends SASDemoPOA {
	private static Principal myPrincipal = null; 
	private static Subject mySubject = null;	
	private static ORB orb;

	public void printSAS() {
		try {
			org.omg.PortableInterceptor.Current current = (org.omg.PortableInterceptor.Current) orb.resolve_initial_references("PICurrent");
			org.omg.CORBA.Any anyName = current.get_slot(org.jacorb.security.sas.SASTargetInitializer.sasPrincipalNamePIC);
			String name = anyName.extract_string();
			System.out.println("printSAS for user " + name);
		} catch (Exception e) {
			System.out.println("printSAS Error: " + e);
		}
	}
	
	public KerberosServer(String[] args) {
		try {
			// initialize the ORB and POA.
			orb = ORB.init(args, null);
			POA poa = (POA) orb.resolve_initial_references("RootPOA");
			poa.the_POAManager().activate();
			
			// create object and write out IOR
			org.omg.CORBA.Object demo = poa.servant_to_reference(this);
			PrintWriter pw = new PrintWriter(new FileWriter(args[0]));
			pw.println(orb.object_to_string(demo));
			pw.flush();
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: java demo.sas.KerberosServer <ior_file> <password>");
			System.exit(-1);
		}

		// login - with Kerberos
		LoginContext loginContext = null;
		try {
			JaasTxtCalbackHandler cbHandler = new JaasTxtCalbackHandler();
			cbHandler.setMyPassword(args[1].toCharArray());
			loginContext = new LoginContext("KerberosService", cbHandler);
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
						// create application
						KerberosServer app = new KerberosServer(finalArgs);
						orb.run();
					} catch (Exception e) {
						System.out.println("Error running program: "+e);
					}
					return null;
				}
			});
		} catch (Exception e) {
			System.out.println("Error running privileged: "+e);
		}
	}
}
