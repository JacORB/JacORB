package org.jacorb.test.sas;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.omg.CORBA.ORB;

import InterOpTest.Johnson;
import InterOpTest.JohnsonHelper;

/**
 * This is the client side of the sas demo. It just calls the single
 * operation "printCert()" of the server. As you can see, sas is fully
 * transparent.
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class Basic_Client {
	public static void main(String args[]) {
		if (args.length != 3) {
			System.out.println("Usage: <ior_file> <username> <password>");
			System.exit(1);
		}

		try {
			// initialize the ORB.
			ORB orb = ORB.init(args, null);

			// get the server
			URL iorURL = new URL(args[0]);
			System.out.println("Reading object from ior: "+args[0]);
			BufferedReader br = new BufferedReader(new InputStreamReader(iorURL.openStream()));//new FileReader(f));
			org.omg.CORBA.Object obj = orb.string_to_object(br.readLine());
			br.close();
			Johnson johnson = JohnsonHelper.narrow(obj);
			
			// call
			String hello = johnson.say_hello("hello from JacORB");
			System.out.println(hello);

			System.out.println("Call to server succeeded");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
