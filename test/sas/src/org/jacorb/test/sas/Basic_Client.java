package org.jacorb.test.sas;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;

import org.omg.BiDirPolicy.BIDIRECTIONAL_POLICY_TYPE;
import org.omg.BiDirPolicy.BOTH;
import org.omg.BiDirPolicy.BidirectionalPolicyValueHelper;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;

import InterOpTest.Johnson;
import InterOpTest.JohnsonHelper;
import InterOpTest.Peter;
import InterOpTest.PeterHelper;
import InterOpTest.PeterPOA;

/**
 * This is the client side of the sas demo. It just calls the single
 * operation "printCert()" of the server. As you can see, sas is fully
 * transparent.
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class Basic_Client extends PeterPOA {

    private static ORB orb = null;
    
	public static void main(String args[]) {
		if (args.length != 3) {
			System.out.println("Usage: <ior_file> <username> <password>");
			System.exit(1);
		}

		try {
			// initialize the ORB.
			orb = ORB.init(args, null);
            
            // initialize the POA
            POA rootPOA = (POA) orb.resolve_initial_references("RootPOA");
            Policy[] policies = new Policy[4];
            policies[0] = rootPOA.create_lifespan_policy(LifespanPolicyValue.TRANSIENT);
            policies[1] = rootPOA.create_id_assignment_policy(IdAssignmentPolicyValue.SYSTEM_ID);
            policies[2] = rootPOA.create_implicit_activation_policy( ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION );
            Any bidirAny = orb.create_any();
            BidirectionalPolicyValueHelper.insert( bidirAny, BOTH.value );
            policies[3] = orb.create_policy( BIDIRECTIONAL_POLICY_TYPE.value, bidirAny );
            POA bidirPOA = rootPOA.create_POA( "BiDirPOA", rootPOA.the_POAManager(), policies );
            bidirPOA.the_POAManager().activate();
            
            // run the ORB
            Thread orbThread = new Thread(new Runnable() {
                public void run() {
                    runORB();
                    System.out.println("ORB closed");
                }
            });
            orbThread.start();

            // create peter and write his IOR
            Basic_Client server = new Basic_Client();
            bidirPOA.activate_object(server);
            Peter peter = PeterHelper.narrow(bidirPOA.servant_to_reference(server));
            PrintWriter pw = new PrintWriter(new FileWriter("peter.ior"));
            pw.println(orb.object_to_string(peter));
            pw.flush();
            pw.close();

			// get the server
			URL iorURL = new URL(args[0]);
			System.out.println("Reading object from ior: "+args[0]);
			BufferedReader br = new BufferedReader(new InputStreamReader(iorURL.openStream()));//new FileReader(f));
			org.omg.CORBA.Object obj = orb.string_to_object(br.readLine());
			br.close();
			Johnson johnson = JohnsonHelper.narrow(obj);
			
			// call
            System.out.println("CSS1:"+johnson.say_hello("hello from JacORB"));
            System.out.println("CSS2:"+johnson.say_hello("hello from JacORB"));
            System.out.println("TSS1:"+johnson.say_hello_from_you(peter, 1));
            System.out.println("TSS2:"+johnson.say_hello_from_you(peter, 1));

			System.out.println("Call to server succeeded");
		} catch (Exception ex) {
            System.out.println("Error: " + ex + ": " + ex.getMessage());
			ex.printStackTrace();
		}
        if (orb != null) orb.shutdown(false);
	}
    
    private static void runORB() {
        orb.run();
    }

    /* (non-Javadoc)
     * @see InterOpTest.PeterOperations#say_hello(java.lang.String)
     */
    public String say_hello(String msg) {
        System.out.println("TSS: "+msg);
        return "Hello from Peter!\n" + msg;
    }
}
