package org.jacorb.trading.test;

/**
 * This class is a load test for a trader federation. It starts four traders,
 * supplies them with two offers each and links every trader with every other.
 * Then it starts several ImportThreads which randomly query the traders.
 *
 * @author Nicolas Noffke
 */

import org.omg.CosTrading.*;
import org.omg.CosTrading.RegisterPackage.*;
import org.omg.CosTrading.LookupPackage.*;
import org.omg.CosTradingRepos.*;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.*;
import org.omg.CORBA.*;
import java.io.*;
//import java.util.*;

public class LoadTest {
    private static final int THREADS = 2;
    
    public static void main(String[] args) {
      if (args.length == 0){
	System.out.println("Usage: LoadTest <iorfile-stem>");
	System.exit(0);
      }
	
    	try{
	    // exec four independent traders
	    Runtime _rt = Runtime.getRuntime();
	    
	    new OutputForwarder(_rt.exec("ts " + args[0] + "1 -d db1"), "Trader1");
	    new OutputForwarder(_rt.exec("ts " + args[0] + "2 -d db2"), "Trader2");
	    new OutputForwarder(_rt.exec("ts " + args[0] + "3 -d db3"), "Trader3");
	    new OutputForwarder(_rt.exec("ts " + args[0] + "4 -d db4"), "Trader4");

	    System.out.println("Press any key when all four Traders are ready");
	    System.in.read();

	    //set up ORB
	    org.omg.CORBA.ORB _orb = org.omg.CORBA.ORB.init(args, null);

	    // get lookup interfaces
	    Lookup[] _lookup = new Lookup[4];
	    
	    BufferedReader in = new BufferedReader(new FileReader(new File(args[0] + "1")));
	    String ref = in.readLine();
	    in.close();
	    _lookup[0] = LookupHelper.narrow(_orb.string_to_object(ref));
	    Link _link1 = LinkHelper.narrow(_lookup[0].link_if());
	    ServiceTypeRepository _repos1 = ServiceTypeRepositoryHelper.narrow(_lookup[0].type_repos());
	    Register _reg1 = RegisterHelper.narrow(_lookup[0].register_if());


	    in = new BufferedReader(new FileReader(new File(args[0] + "2")));
	    ref = in.readLine();
	    in.close();
	     _lookup[1] = LookupHelper.narrow(_orb.string_to_object(ref));
	    Link _link2 = LinkHelper.narrow(_lookup[1].link_if());
	    ServiceTypeRepository _repos2 = ServiceTypeRepositoryHelper.narrow(_lookup[1].type_repos());
	    Register _reg2 = RegisterHelper.narrow(_lookup[1].register_if());


	    in = new BufferedReader(new FileReader(new File(args[0] + "3")));
	    ref = in.readLine();
	    in.close();
	     _lookup[2] = LookupHelper.narrow(_orb.string_to_object(ref));
	    Link _link3 = LinkHelper.narrow(_lookup[2].link_if());
	    ServiceTypeRepository _repos3 = ServiceTypeRepositoryHelper.narrow(_lookup[2].type_repos());
	    Register _reg3 = RegisterHelper.narrow(_lookup[2].register_if());


	    in = new BufferedReader(new FileReader(new File(args[0] + "4")));
	    ref = in.readLine();
	    in.close();
	     _lookup[3] = LookupHelper.narrow(_orb.string_to_object(ref));
	    Link _link4 = LinkHelper.narrow(_lookup[3].link_if());
	    ServiceTypeRepository _repos4 = ServiceTypeRepositoryHelper.narrow(_lookup[3].type_repos());
	    Register _reg4 = RegisterHelper.narrow(_lookup[3].register_if());


	    //build up properties for exporting type
	    PropStruct[] _props = new PropStruct[2];
	    _props[0] = new PropStruct();
	    _props[0].name = "Art";
	    _props[0].value_type = _orb.get_primitive_tc(TCKind.tk_string);
	    _props[0].mode = PropertyMode.PROP_MANDATORY;
	    
	    _props[1] = new PropStruct();
	    _props[1].name = "Preis";
	    _props[1].value_type = _orb.get_primitive_tc(TCKind.tk_string);
	    _props[1].mode = PropertyMode.PROP_MANDATORY;
	      
	    //export the type
	    _repos1.add_type("Orchideen", "IDL:Orchid:0.1", _props, new String[0]);
	    _repos2.add_type("Orchideen", "IDL:Orchid:0.1", _props, new String[0]);
	    _repos3.add_type("Orchideen", "IDL:Orchid:0.1", _props, new String[0]);
	    _repos4.add_type("Orchideen", "IDL:Orchid:0.1", _props, new String[0]);
	    
	    
	    //build and export offers
	    String[] types;
 	    SpecifiedServiceTypes whichTypes = new SpecifiedServiceTypes();
 	    whichTypes.__default();
 	    types = _repos1.list_types(whichTypes);
    	    
	    Property[] _prop = new Property[2];
	    _prop[0] = new Property();
	    _prop[0].name = "Art";
	    _prop[0].value = _orb.create_any();
	    _prop[0].value.insert_string("1");
	    
	    _prop[1] = new Property();
	    _prop[1].name = "Preis";
	    _prop[1].value = _orb.create_any();
	    _prop[1].value.insert_string("8");
	    
	    String id = _reg1.export(_reg1, "Orchideen", _prop);
	    System.out.println("Offer id = " + id);

	    _prop[0].name = "Art";
	    _prop[0].value = _orb.create_any();
	    _prop[0].value.insert_string("2");

	    _prop[1].name = "Preis";
	    _prop[1].value = _orb.create_any();
	    _prop[1].value.insert_string("6");
	    	    
	    id = _reg1.export(_reg1, "Orchideen", _prop);
	    System.out.println("Offer id = " + id);
	    
	    //
	    _prop[0].value.insert_string("3");
	    _prop[1].value.insert_string("8");	    
	    id = _reg2.export(_reg2, "Orchideen", _prop);
	    System.out.println("Offer id = " + id);

	    _prop[0].value.insert_string("4");
	    _prop[1].value.insert_string("6");	    	    
	    id = _reg2.export(_reg2, "Orchideen", _prop);
	    System.out.println("Offer id = " + id);

	    //
	    _prop[0].value.insert_string("5");
	    _prop[1].value.insert_string("8");	    
	    id = _reg3.export(_reg3, "Orchideen", _prop);
	    System.out.println("Offer id = " + id);

	    _prop[0].value.insert_string("6");
	    _prop[1].value.insert_string("6");	    	    
	    id = _reg3.export(_reg3, "Orchideen", _prop);
	    System.out.println("Offer id = " + id);

	    //
	    _prop[0].value.insert_string("7");
	    _prop[1].value.insert_string("8");	    
	    id = _reg4.export(_reg4, "Orchideen", _prop);
	    System.out.println("Offer id = " + id);

	    _prop[0].value.insert_string("0");
	    _prop[1].value.insert_string("6");	    	    
	    id = _reg4.export(_reg4, "Orchideen", _prop);
	    System.out.println("Offer id = " + id);

	    //link traders
	    _link1.add_link("link1", _lookup[1], FollowOption.always, FollowOption.always);
	    _link1.add_link("link2", _lookup[2], FollowOption.always, FollowOption.always);
	    _link1.add_link("link3", _lookup[3], FollowOption.always, FollowOption.always);
	    
	    _link2.add_link("link1", _lookup[0], FollowOption.always, FollowOption.always);
	    _link2.add_link("link2", _lookup[2], FollowOption.always, FollowOption.always);
	    _link2.add_link("link3", _lookup[3], FollowOption.always, FollowOption.always);
	    
	    _link3.add_link("link1", _lookup[1], FollowOption.always, FollowOption.always);
	    _link3.add_link("link2", _lookup[0], FollowOption.always, FollowOption.always);
	    _link3.add_link("link3", _lookup[3], FollowOption.always, FollowOption.always);
	    
	    _link4.add_link("link1", _lookup[0], FollowOption.always, FollowOption.always);
	    _link4.add_link("link2", _lookup[2], FollowOption.always, FollowOption.always);
	    _link4.add_link("link3", _lookup[1], FollowOption.always, FollowOption.always);
	    

	    // run query threads
	     for (int i = 0; i < THREADS; i++)
		 new ImportThread("" + i, _lookup, types[0], _orb);
	}
	catch (Exception e){
	    e.printStackTrace();
	}   
    }
    
} // LoadTest



