
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

package org.jacorb.trading.client.dynprop;

import java.io.*;
import java.util.*;
import org.omg.CORBA.*;
import org.omg.CosTrading.*;
import org.omg.CosTrading.RegisterPackage.*;
import org.omg.CosTradingDynamic.*;
import org.omg.CosTradingRepos.*;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.*;

public class export
{
    public static void main(String[] args)
    {
	if (args.length < 1) 
	{
	    usage();
	}

	File dynPropFile = new File(args[0]);

	if (! dynPropFile.exists()) {
	    System.err.println("File " + args[0] + " does not exist");
	    usage();
	}

	if (! dynPropFile.isFile()) {
	    System.err.println(args[0] + " is not a file");
	    usage();
	}

	ORB orb = ORB.init(args,null);

	Register reg = null;
	DynamicPropEval eval = null;
        ServiceTypeRepository repos = null;
	try {
	    FileReader fr;
	    BufferedReader in;
	    org.omg.CORBA.Object obj;

	    // read the IOR for the DynamicPropEval object from a file
	    fr = new FileReader(dynPropFile);
	    in = new BufferedReader(fr);
	    String dynPropRef = in.readLine();
	    fr.close();

	    obj = orb.resolve_initial_references("TradingService");

	    if (obj == null) 
	    {
		System.out.println("Invalid lookup object");
		System.exit(1);
	    }

	    Lookup lookup = LookupHelper.narrow(obj);
	    reg = lookup.register_if();

	    obj = orb.string_to_object(dynPropRef);
	    if (obj == null) {
		System.out.println("Invalid dynamic prop eval object");
		System.exit(1);
	    }

	    eval = DynamicPropEvalHelper.narrow(obj);

            repos = ServiceTypeRepositoryHelper.narrow(lookup.type_repos());
	}
	catch (org.omg.CORBA.ORBPackage.InvalidName e) 
	{
	    System.out.println("Invalid initial reference name");
	}
	catch (IOException e) 
	{
	    e.printStackTrace();
	    System.exit(1);
	}

	try {
	    Random rand = new Random();

	    PropStruct[] _props = new PropStruct[3];
	    _props[0] = new PropStruct();
	    _props[0].name = "name";
	    _props[0].value_type = orb.get_primitive_tc(TCKind.tk_string);
	    _props[0].mode = PropertyMode.PROP_MANDATORY;
	    
	    _props[1] = new PropStruct();
	    _props[1].name = "cost";
	    _props[1].value_type = orb.get_primitive_tc(TCKind.tk_double);
	    _props[1].mode = PropertyMode.PROP_MANDATORY;

	    _props[2] = new PropStruct();
	    _props[2].name = "version";
	    _props[2].value_type = orb.get_primitive_tc(TCKind.tk_string);
	    _props[2].mode = PropertyMode.PROP_MANDATORY;	      

	    repos.add_type("SubSvc", "IDL:SubSvc:1.0", _props, new String[0]);

	    for (int i = 0; i < 10; i++) {
		Property[] props = new Property[3];

		int num = 0;
		TypeCode tc;

		// the "name" property
		props[num] = new Property();
		props[num].name = "name";
		props[num].value = orb.create_any();
		props[num].value.insert_string("dyn offer #" + i);
		num++;

		// the "cost" property is dynamic
		props[num] = new Property();
		props[num].name = "cost";
		props[num].value = orb.create_any();
		DynamicProp dp = new DynamicProp();
		dp.eval_if = eval;
		dp.returned_type = orb.get_primitive_tc(TCKind.tk_double);
		dp.extra_info = orb.create_any();
		dp.extra_info.insert_string("Dummy");
		DynamicPropHelper.insert(props[num].value, dp);
		num++;

		// the "version" property
		props[num] = new Property();
		props[num].name = "version";
		props[num].value = orb.create_any();
		props[num].value.insert_string("1.0" + i);
		num++;

		String id = reg.export(reg, "SubSvc", props);
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
	catch (org.omg.CosTrading.RegisterPackage.InterfaceTypeMismatch e) {
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
	catch (Exception e) {
	    System.out.println("Other: " + e);
	}

	System.exit(0);
    }


    protected static void usage()
    {
	System.out.println( "Usage: org.jacorb.trading.client.dynprop.export <dynprop-iorfile>" );
	System.exit(1);
    }
}










