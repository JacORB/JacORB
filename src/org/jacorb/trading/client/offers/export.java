
// Copyright (C) 1998-2001
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
// GB: import jtport.ORBLayer;


public class export
{
  public static void main(String[] args)
  {
    if (args.length < 1) {
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
      Random rand = new Random();

      for (int i = 0; i < 10; i++) {
        Property[] props;

          // only use the 'cost' property for even numbers; allows
          // testing of the 'exist' operator in constraint expressions
        if (i % 2 == 0)
          props = new Property[4];
        else
          props = new Property[3];

        int num = 0;
        TypeCode tc;

        props[num] = new Property();
        props[num].name = "name";
        props[num].value = orb.create_any();
        props[num].value.insert_string("name #" + i);
        num++;

        if (i % 2 == 0) {
          props[num] = new Property();
          props[num].name = "cost";
          props[num].value = orb.create_any();
          props[num].value.insert_double(Math.abs(rand.nextDouble()));
          num++;
        }

        props[num] = new Property();
        props[num].name = "version";
        props[num].value = orb.create_any();
        props[num].value.insert_string("1.0" + i);
        num++;

        props[num] = new Property();
        props[num].name = "count";
        props[num].value = orb.create_any();
        props[num].value.insert_long(Math.abs(rand.nextInt()) % 100);
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
    System.out.println("Usage: jtclient.offers.export iorfile");
    System.exit(1);
  }
}










