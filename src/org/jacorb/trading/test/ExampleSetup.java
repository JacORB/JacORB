package org.jacorb.trading.test;

import org.omg.CosTrading.*;
import org.omg.CosTrading.RegisterPackage.*;
import org.omg.CosTrading.LookupPackage.*;
import org.omg.CosTradingRepos.*;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.*;
import org.omg.CORBA.*;
import java.io.*;

/**
 * This class provides four traders with eight different offers (two each)
 * and connects them fully. 
 *
 * @author Nicolas Noffke
 */

public class ExampleSetup
{
    public static void main(String[] args) 
    {
	try
        {
	    if (args.length == 0)
            {
		System.out.println("Usage: ExampleSetup <iorfile-stem>");
		System.exit(0);
	    }

	    org.omg.CORBA.ORB _orb = org.omg.CORBA.ORB.init(args, null);

	    BufferedReader in =
                new BufferedReader(new FileReader(new File(args[0] + "1")));
	    String ref = in.readLine();
	    in.close();
	    Lookup _lookup1 = LookupHelper.narrow(_orb.string_to_object(ref));
	    Link _link1 = LinkHelper.narrow(_lookup1.link_if());
	    ServiceTypeRepository _repos1 = 
                ServiceTypeRepositoryHelper.narrow(_lookup1.type_repos());

	    Register _reg1 = RegisterHelper.narrow(_lookup1.register_if());


	    in = new BufferedReader(new FileReader(new File(args[0] + "2")));
	    ref = in.readLine();
	    in.close();
	    Lookup _lookup2 = LookupHelper.narrow(_orb.string_to_object(ref));
	    Link _link2 = LinkHelper.narrow(_lookup2.link_if());
	    ServiceTypeRepository _repos2 = ServiceTypeRepositoryHelper.narrow(_lookup2.type_repos());
	    Register _reg2 = RegisterHelper.narrow(_lookup2.register_if());


	    in = new BufferedReader(new FileReader(new File(args[0] + "3")));
	    ref = in.readLine();
	    in.close();
	    Lookup _lookup3 = LookupHelper.narrow(_orb.string_to_object(ref));
	    Link _link3 = LinkHelper.narrow(_lookup3.link_if());
	    ServiceTypeRepository _repos3 = ServiceTypeRepositoryHelper.narrow(_lookup3.type_repos());
	    Register _reg3 = RegisterHelper.narrow(_lookup3.register_if());


	    in = new BufferedReader(new FileReader(new File(args[0] + "4")));
	    ref = in.readLine();
	    in.close();
	    Lookup _lookup4 = LookupHelper.narrow(_orb.string_to_object(ref));
	    Link _link4 = LinkHelper.narrow(_lookup4.link_if());
	    ServiceTypeRepository _repos4 = ServiceTypeRepositoryHelper.narrow(_lookup4.type_repos());
	    Register _reg4 = RegisterHelper.narrow(_lookup4.register_if());


	    PropStruct[] _props = new PropStruct[2];
	    _props[0] = new PropStruct();
	    _props[0].name = "Art";
	    _props[0].value_type = _orb.get_primitive_tc(TCKind.tk_string);
	    _props[0].mode = PropertyMode.PROP_MANDATORY;
	    
	    _props[1] = new PropStruct();
	    _props[1].name = "Preis";
	    _props[1].value_type = _orb.get_primitive_tc(TCKind.tk_string);
	    _props[1].mode = PropertyMode.PROP_MANDATORY;
	      

	    _repos1.add_type("Orchideen", "IDL:Orchid:1.0", _props, new String[0]);
	    _repos2.add_type("Orchideen", "IDL:Orchid:1.0", _props, new String[0]);
	    _repos3.add_type("Orchideen", "IDL:Orchid:1.0", _props, new String[0]);
	    _repos4.add_type("Orchideen", "IDL:Orchid:1.0", _props, new String[0]);
	    
// 	    String[] types;
// 	    SpecifiedServiceTypes whichTypes = new SpecifiedServiceTypes();
// 	    whichTypes.__default();
// 	    types = _repos.list_types(whichTypes);
    	    
	    Property[] _prop = new Property[2];
	    _prop[0] = new Property();
	    _prop[0].name = "Art";
	    _prop[0].value = _orb.create_any();
	    _prop[0].value.insert_string("Phalaenopsis");
	    
	    _prop[1] = new Property();
	    _prop[1].name = "Preis";
	    _prop[1].value = _orb.create_any();
	    _prop[1].value.insert_string("8");
	    
	    
	    String id = _reg1.export(_reg1, "Orchideen", _prop);
	    System.out.println("Offer id = " + id);

	    _prop[0].name = "Art";
	    _prop[0].value = _orb.create_any();
	    _prop[0].value.insert_string("Chilochista");

	    _prop[1].name = "Preis";
	    _prop[1].value = _orb.create_any();
	    _prop[1].value.insert_string("6");
	    	    
	    id = _reg1.export(_reg1, "Orchideen", _prop);
	    System.out.println("Offer id = " + id);
	    
	    //
	    _prop[0].value.insert_string("Cambria");
	    _prop[1].value.insert_string("8");	    
	    id = _reg2.export(_reg2, "Orchideen", _prop);
	    System.out.println("Offer id = " + id);

	    _prop[0].value.insert_string("Paphilopedilum");
	    _prop[1].value.insert_string("6");	    	    
	    id = _reg2.export(_reg2, "Orchideen", _prop);
	    System.out.println("Offer id = " + id);

	    //
	    _prop[0].value.insert_string("Oncidium");
	    _prop[1].value.insert_string("8");	    
	    id = _reg3.export(_reg3, "Orchideen", _prop);
	    System.out.println("Offer id = " + id);

	    _prop[0].value.insert_string("Dendrobium");
	    _prop[1].value.insert_string("6");	    	    
	    id = _reg3.export(_reg3, "Orchideen", _prop);
	    System.out.println("Offer id = " + id);

	    //
	    _prop[0].value.insert_string("Miltonia");
	    _prop[1].value.insert_string("8");	    
	    id = _reg4.export(_reg4, "Orchideen", _prop);
	    System.out.println("Offer id = " + id);

	    _prop[0].value.insert_string("Cymbidium");
	    _prop[1].value.insert_string("6");	    	    
	    id = _reg4.export(_reg4, "Orchideen", _prop);
	    System.out.println("Offer id = " + id);

	     _link1.add_link("link1", _lookup2, FollowOption.always, FollowOption.always);
	     _link1.add_link("link2", _lookup3, FollowOption.always, FollowOption.always);
	      _link1.add_link("link3", _lookup4, FollowOption.always, FollowOption.always);

	     _link2.add_link("link1", _lookup1, FollowOption.always, FollowOption.always);
	     _link2.add_link("link2", _lookup3, FollowOption.always, FollowOption.always);
	     _link2.add_link("link3", _lookup4, FollowOption.always, FollowOption.always);
	     //	     _link2.add_link("link4", _lookup4, FollowOption.always, FollowOption.always);

	     _link3.add_link("link1", _lookup2, FollowOption.always, FollowOption.always);
	     _link3.add_link("link2", _lookup1, FollowOption.always, FollowOption.always);
	     _link3.add_link("link3", _lookup4, FollowOption.always, FollowOption.always);
	    
	     _link4.add_link("link1", _lookup1, FollowOption.always, FollowOption.always);
	     _link4.add_link("link2", _lookup3, FollowOption.always, FollowOption.always);
	     _link4.add_link("link3", _lookup2, FollowOption.always, FollowOption.always);
	}
	catch (Exception e){
	    e.printStackTrace();
	}   
    }
    
} // jtrtest



