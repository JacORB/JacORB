
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

package org.jacorb.trading.client.seqtest;

import java.io.*;
import java.util.*;
import org.omg.CORBA.*;
import org.omg.CosTrading.*;
import org.omg.CosTrading.RegisterPackage.*;

public class export
{
    public static void main(String[] args)
    {

	//    ORB orb = ORBLayer.instance().initClient(args);
	ORB orb = org.omg.CORBA.ORB.init(args, null);
	Register reg = null;

	try {
	    org.omg.CORBA.Object obj = orb.resolve_initial_references("TradingService");

	    if (obj == null) {
		System.out.println("Invalid object");
		System.exit(1);
	    }

	    Lookup lookup = LookupHelper.narrow(obj);
	    reg = lookup.register_if();
	}
	catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}

	try {
	    Random rand = new Random();

	    for (int i = 0; i < 10; i++) {
		Property[] props = new Property[4];

		int num = 0;
		TypeCode tc;

		props[num] = new Property();
		props[num].name = "shortseq";
		props[num].value = orb.create_any();
		short[] shortArr = new short[5];
		for (int n = 0; n < 5; n++)
		    shortArr[n] = (short)(Math.abs(rand.nextInt()) % 100);
		ShortSeqHelper.insert(props[num].value, shortArr);
		num++;

		props[num] = new Property();
		props[num].name = "floatseq";
		props[num].value = orb.create_any();
		float[] floatArr = new float[5];
		for (int n = 0; n < 5; n++)
		    floatArr[n] = Math.abs(rand.nextFloat());
		FloatSeqHelper.insert(props[num].value, floatArr);
		num++;

		props[num] = new Property();
		props[num].name = "booleanseq";
		props[num].value = orb.create_any();
		boolean[] booleanArr = new boolean[5];
		for (int n = 0; n < 5; n++)
		    booleanArr[n] = (rand.nextInt() % 2 == 0);
		BooleanSeqHelper.insert(props[num].value, booleanArr);
		num++;

		props[num] = new Property();
		props[num].name = "stringseq";
		props[num].value = orb.create_any();
		String[] stringArr = new String[5];
		for (int n = 0; n < 5; n++)
		    stringArr[n] = "s" + n;
		StringSeqHelper.insert(props[num].value, stringArr);
		num++;

		String id = reg.export(reg, "SeqSvc", props);
		System.out.println("Offer id = " + id);
	    }
	}
	catch (InvalidObjectRef e) {
	    System.out.println("Invalid object reference");
	}
	catch (IllegalServiceType e) {
	    System.out.println("Illegal service type: " + e.type);
	}
	catch (UnknownServiceType e) {
	    System.out.println("Unknown service type: " + e.type);
	}
	catch (InterfaceTypeMismatch e) {
	    System.out.println("Interface type mismatch: " + e.type);
	}
	catch (IllegalPropertyName e) {
	    System.out.println("Illegal property name: " + e.name);
	}
	catch (PropertyTypeMismatch e) {
	    System.out.println("Property type mismatch: " + e.prop.name);
	}
	catch (ReadonlyDynamicProperty e) {
	    System.out.println("Readonly dynamic property: " + e.name);
	}
	catch (MissingMandatoryProperty e) {
	    System.out.println("Missing mandatory property: " + e.name);
	}
	catch (DuplicatePropertyName e) {
	    System.out.println("Duplicate property: " + e.name);
	}

	System.exit(0);
    }


    protected static void usage()
    {
	System.out.println("Usage: jtclient.seqtest.export iorfile");
	System.exit(1);
    }
}










