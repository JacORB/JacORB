// Copyright (C) 1998-1999
// Object Oriented Concepts, Inc.

// **********************************************************************
//
// Copyright (c) 1997
// Mark Spruiell (mark@intellisoft.com)
//
// See the COPYING file for more information
//
// **********************************************************************

package org.jacorb.trading.client.offers;

import java.io.*;
import java.util.*;
import org.omg.CORBA.*;
import org.omg.CosTrading.*;
import org.omg.CosTrading.RegisterPackage.*;
import org.jacorb.trading.client.util.AnyUtil;

public class describe
{
    public static void main(String[] args)
    {
	if (args.length < 2) {
	    usage();
	    return;
	}

	File f = new File(args[0]);
	if (! f.exists()) {
	    System.err.println("File " + args[0] + " does not exist");
	    usage();
	}

	if (! f.isFile()) {
	    System.err.println(args[0] + " is not a file");
	    usage();
	}

	ORB orb = ORB.init(args, null);

	Register reg = null;

	try {
	    FileReader fr = new FileReader(f);
	    BufferedReader in = new BufferedReader(fr);
	    String ref = in.readLine();
	    fr.close();

	    org.omg.CORBA.Object obj = orb.string_to_object(ref);
	    if (obj == null) {
		System.out.println("Invalid object");
		System.exit(1);
	    }

	    Lookup lookup = LookupHelper.narrow(obj);
	    reg = lookup.register_if();
	}
	catch (IOException e) {
	    e.printStackTrace();
	    System.exit(1);
	}

	try {
	    OfferInfo info = reg.describe(args[1]);
	    PrintWriter pw = new PrintWriter(System.out);
	    pw.println("Service type: " + info.type);
	    pw.println();
	    pw.println("Reference: " + orb.object_to_string(info.reference));
	    pw.println();

	    for (int i = 0; i < info.properties.length; i++) {
		pw.println("Property: " + info.properties[i].name);
		pw.print("    Type: ");
		AnyUtil.print(pw, info.properties[i].value.type());
		pw.println();
		pw.print("   Value: ");
		AnyUtil.print(orb, pw, info.properties[i].value);
		pw.println();
		pw.println();
	    }

	    pw.flush();
	}
	catch (IllegalOfferId e) {
	    System.out.println("Illegal offer ID: " + e.id);
	}
	catch (UnknownOfferId e) {
	    System.out.println("Unknown offer ID: " + e.id);
	}
	catch (ProxyOfferId e) {
	    System.out.println("Offer is a proxy");
	}

	System.exit(0);
    }


    protected static void usage()
    {
	System.out.println("Usage: jtclient.offers.describe iorfile offerid");
	System.exit(1);
    }
}










