
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

package org.jacorb.trading.client.proxy;

import java.io.*;
import java.util.*;
import org.omg.CORBA.*;
import org.omg.CosTrading.*;
import org.omg.CosTrading.ProxyPackage.*;
import org.omg.CosTradingRepos.*;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.*;
/**
 * Exports proxy offers
 */

public class export
{
    public static void main(String[] args)
    {
        if (args.length != 1) 
        {
            usage();
        }

        File targetFile = new File(args[0]);

        if (! targetFile.exists()) 
        {
            System.err.println("File " + args[0] + " does not exist");
            usage();
        }

        if (! targetFile.isFile()) 
        {
            System.err.println(args[0] + " is not a file");
            usage();
        }

        ORB orb = null;

        Proxy proxy = null;
        Lookup target = null;
        ServiceTypeRepository repos = null;
        try 
        {
            org.omg.CORBA.Object obj = null;

            orb = ORB.init(args,null);

            //resolve trader
            obj = orb.resolve_initial_references("TradingService");
            if (obj == null) 
            {
                System.out.println("Invalid lookup object");
                System.exit(1);
            }

            Lookup lookup = LookupHelper.narrow(obj);
            proxy = lookup.proxy_if();
            repos = ServiceTypeRepositoryHelper.narrow(lookup.type_repos());

            // read the IOR for the Lookup target object from a file
            FileReader fr = new FileReader(targetFile);
            BufferedReader in = new BufferedReader(fr);
            String targetRef = in.readLine();
            fr.close();


            obj = orb.string_to_object(targetRef);
            if (obj == null) 
            {
                System.out.println("Invalid target object");
                System.exit(1);
            }

            target = LookupHelper.narrow(obj);
        }
        catch (Exception e) 
        {
            e.printStackTrace();
            System.exit(1);
        }

        try 
        {
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


            for (int i = 0; i < 10; i++) 
            {
                Property[] props = new Property[3];

                int num = 0;
                TypeCode tc;

                // the "name" property
                props[num] = new Property();
                props[num].name = "name";
                props[num].value = orb.create_any();
                props[num].value.insert_string("proxy #" + i);
                num++;

                // the "cost" property
                props[num] = new Property();
                props[num].name = "cost";
                props[num].value = orb.create_any();
                props[num].value.insert_double(Math.abs(rand.nextDouble()));
                num++;

                // the "version" property
                props[num] = new Property();
                props[num].name = "version";
                props[num].value = orb.create_any();
                props[num].value.insert_string("1.0" + i);
                num++;

                String recipe = "$(cost) < 1.50 && $(version) = '1.03'";

                org.omg.CosTrading.Policy[] policies = new org.omg.CosTrading.Policy[2];
                policies[0] = new org.omg.CosTrading.Policy();
                policies[0].name = "policy1";
                policies[0].value = orb.create_any();
                policies[0].value.insert_boolean(true);
                policies[1] = new org.omg.CosTrading.Policy();
                policies[1].name = "policy2";
                policies[1].value = orb.create_any();
                policies[1].value.insert_ulong(i);

                boolean ifMatchAll = (i % 2 == 0);

                String id = proxy.export_proxy(target, "SubSvc", props, ifMatchAll,
                                               recipe, policies);
                System.out.println("Offer id = " + id);
            }
        }
        catch (IllegalServiceType e) {
            System.out.println("Illegal service type: " + e.type);
        }
        catch (UnknownServiceType e) {
            System.out.println("Unknown service type: " + e.type);
        }
        catch (InvalidLookupRef e) {
            System.out.println("Invalid target object");
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
        catch (IllegalRecipe e) {
            System.out.println("Illegal recipe: " + e.recipe);
        }
        catch (DuplicatePropertyName e) {
            System.out.println("Duplicate property: " + e.name);
        }
        catch (DuplicatePolicyName e) {
            System.out.println("Duplicate policy: " + e.name);
        }
        catch (Exception e) {
            System.out.println("Exception: " + e);
        }
        

        System.exit(0);
    }


    protected static void usage()
    {
        System.out.println("Usage: org.jacorb.trading.client.proxy.export <proxy-iorfile>");
        System.exit(1);
    }
}










