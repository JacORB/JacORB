package demo.sas;

import java.io.FileWriter;
import java.io.PrintWriter;

import org.jacorb.sasPolicy.SASPolicyValues;
import org.jacorb.sasPolicy.SAS_POLICY_TYPE;
import org.jacorb.sasPolicy.SASPolicyValuesHelper;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CSIIOP.EstablishTrustInClient;

/**
 * This is the server part of the sas demo. It demonstrates
 * how to get access to the certificates that the client sent
 * for mutual authentication. The certificate chain can be
 * accessed via the Security Level 2 interfaces.
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class GssUpServer extends SASDemoPOA {

	private ORB orb;

	public GssUpServer(ORB orb) {
		this.orb = orb;
	}

	public void printSAS() {
		try {
			org.omg.PortableInterceptor.Current current = (org.omg.PortableInterceptor.Current)orb.resolve_initial_references("PICurrent");
			org.omg.CORBA.Any anyName = current.get_slot(org.jacorb.security.sas.SASInitializer.sasPrincipalNamePIC);
			if( anyName.type().kind().value() == org.omg.CORBA.TCKind._tk_null ) {
				System.out.println("Null Name");
			} else {
				String name = anyName.extract_string();
				System.out.println("printSAS for user " + name);
			}
		} catch (Exception e) {
			System.out.println("printSAS Error: " + e);
		}
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: java demo.sas.GssUpServer <ior_file>");
			System.exit(-1);
		}

		try {
			// initialize the ORB and POA.
			ORB orb = ORB.init(args, null);
			POA rootPOA = (POA) orb.resolve_initial_references("RootPOA");
			org.omg.CORBA.Policy [] policies = new org.omg.CORBA.Policy[3];
			policies[0] = rootPOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID);
			policies[1] = rootPOA.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);
			Any sasAny = orb.create_any();
			SASPolicyValuesHelper.insert( sasAny, new SASPolicyValues(EstablishTrustInClient.value, EstablishTrustInClient.value, true) );
			policies[2] = orb.create_policy(SAS_POLICY_TYPE.value, sasAny);
			POA securePOA = rootPOA.create_POA("SecurePOA", rootPOA.the_POAManager(), policies);
 			rootPOA.the_POAManager().activate();
			
			// create object and write out IOR
			GssUpServer server = new GssUpServer(orb);
			securePOA.activate_object_with_id("SecureObject".getBytes(), server);
			org.omg.CORBA.Object demo = securePOA.servant_to_reference(server);
			PrintWriter pw = new PrintWriter(new FileWriter(args[0]));
			pw.println(orb.object_to_string(demo));
			pw.flush();
			pw.close();
			
			// run the ORB
			orb.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
