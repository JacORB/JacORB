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

package org.jacorb.trading;

import java.io.*;
import java.util.*;
import org.omg.CORBA.*;
import org.omg.CosTrading.*;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.*;
import org.jacorb.trading.impl.*;
import org.jacorb.trading.db.DatabaseMgr;
import org.jacorb.trading.db.OfferDatabase;
import org.jacorb.trading.db.TypeDatabase;
import org.jacorb.trading.util.*;


public class TradingService
{
    private static final String s_defaultDbpath = "db";
    private static ORB orb;

    protected TradingService()
    {
    }

    public static ORB getORB()
    {
	return orb;
    }

    public TradingService(DatabaseMgr dbMgr, String iorfile)
    {

	// see if we have an interface repository

	org.omg.CORBA.Repository intRep = null;
	org.omg.CORBA.Object obj = null;

	try 
	{
	    obj =  orb.resolve_initial_references("InterfaceRepository");
	}
	catch (org.omg.CORBA.ORBPackage.InvalidName e) {
	    // ignore - no interface repository available
	}

	try
	{
	    org.omg.PortableServer.POA poa = 
                org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
	    poa.the_POAManager().activate();

	    if (obj != null)
		intRep = org.omg.CORBA.RepositoryHelper.narrow(obj);
	}
	catch (org.omg.CORBA.ORBPackage.InvalidName e) {
	    // ignore - no interface repository available
	}
	catch (org.omg.CORBA.SystemException e) {
	    // ignore
	}
	catch (org.omg.CORBA.UserException e) {
	    // ignore
	}


	// retrieve the database objects

	OfferDatabase offerDb = dbMgr.getOfferDatabase();
	TypeDatabase typeDb = dbMgr.getTypeDatabase();

	// create the service type repository implementation

	RepositoryImpl typeRepos = new RepositoryImpl(typeDb, intRep);
	typeRepos._this_object( orb );
	// GB:    orb.connect(typeRepos);

	// create and initialize the attributes objects
	SupportAttrib supportAttrib = new SupportAttrib();
	supportAttrib.setModifiableProperties(getProperty("jtrader.modifiable_properties", true));
	supportAttrib.setDynamicProperties(getProperty("jtrader.dynamic_properties",true));
	supportAttrib.setProxyOffers(getProperty("jtrader.proxy_offers",true));
	supportAttrib.setTypeRepos(typeRepos._this());

	ImportAttrib importAttrib = new ImportAttrib();
	importAttrib.setDefSearchCard(getProperty("jtrader.def_search_card",Integer.MAX_VALUE));
	importAttrib.setMaxSearchCard(getProperty("jtrader.max_search_card",Integer.MAX_VALUE));
	importAttrib.setDefMatchCard(getProperty("jtrader.def_match_card",Integer.MAX_VALUE));
	importAttrib.setMaxMatchCard(getProperty("jtrader.max_match_card",Integer.MAX_VALUE));
	importAttrib.setDefReturnCard(getProperty("jtrader.def_return_card",Integer.MAX_VALUE));
	importAttrib.setMaxReturnCard(getProperty("jtrader.max_return_card",Integer.MAX_VALUE));
	importAttrib.setMaxList(getProperty("jtrader.max_list",Integer.MAX_VALUE));

	//////////////////////////////////////////////////////////////// new!
	importAttrib.setDefHopCount(getProperty("jtrader.def_hop_count",Integer.MAX_VALUE));
	importAttrib.setMaxHopCount(getProperty("jtrader.max_hop_count",Integer.MAX_VALUE));
	importAttrib.setDefFollowPolicy(getProperty("jtrader.def_follow_policy",FollowOption.always));
	importAttrib.setMaxFollowPolicy(getProperty("jtrader.max_follow_policy",FollowOption.always));
	

	LinkAttrib linkAttrib = new LinkAttrib();
	linkAttrib.setMaxLinkFollowPolicy(getProperty("jtrader.max_link_follow_policy",FollowOption.always));
	//////////////////////////////////////////////////////////////// new!

	TraderComp traderComp = new TraderComp();


	// create the trader components

	RegisterImpl reg =
	    new RegisterImpl(traderComp, supportAttrib, offerDb, intRep);
	reg._this_object(orb);
	traderComp.setRegisterInterface(reg._this());

	LinkImpl link = new LinkImpl(traderComp, supportAttrib, linkAttrib);
	link._this_object(orb);
	traderComp.setLinkInterface(link._this());

	LookupImpl lookup = new LookupImpl(traderComp, supportAttrib,
					   importAttrib, offerDb, link);
	lookup._this_object(orb);
	traderComp.setLookupInterface(lookup._this());

	// creating request_id_stem out of inet-address. Might as well use any other
	// way of creating a unique string
	byte[] stem =  orb.object_to_string(lookup._this()).getBytes();


	AdminImpl admin = new AdminImpl(traderComp, supportAttrib,
					importAttrib, linkAttrib, 
					offerDb, stem);
	admin._this_object(orb);
	traderComp.setAdminInterface(admin._this());

	ProxyImpl proxy = new ProxyImpl(traderComp, supportAttrib, offerDb);
	proxy._this_object(orb);
	traderComp.setProxyInterface(proxy._this());
	
	// write the IOR of the Lookup object (if necessary)
	if (iorfile != null) {
	    try {
		FileOutputStream out = new FileOutputStream(iorfile);
		PrintWriter pw = new PrintWriter(out);
		pw.println(orb.object_to_string(lookup._this()));
		pw.flush();
		out.close();
	    }
	    catch (IOException e) {
		System.err.println("Unable to write IOR to file " + iorfile);
		System.exit(1);
	    }
	}
    }

    public static void main(String[] args)
    {
	String iorfile = null;
	String dbpath = s_defaultDbpath;

	if( args.length != 1 && args.length != 3)
	    usage();

	iorfile = args[0];

	if( args.length == 3 )
	{
	    if( args[1].equals("-d"))
		dbpath = args[2];
	    else
		usage();
	}

	// validate the database pathname

	File f = new File(dbpath);
	if (! f.exists()) {
	    System.out.println("The directory " + dbpath + " does not exist");
	    System.exit(1);
	}
	else if (! f.isDirectory()) {
	    System.out.println("The path " + dbpath + " is not a directory");
	    System.exit(1);
	}

	// initialize the ORB
	//    ORBLayer.instance().init(args);
	orb = ORB.init(args,null);

	// create the appropriate database manager object
	DatabaseMgr dbMgr;

	///////// Simple database implementation
	dbMgr = new org.jacorb.trading.db.simple.SimpleDatabaseMgr(dbpath);

	///////// ObjectStore PSE database implementation
	//dbMgr = new org.jacorb.trading.db.pse.PSEDatabaseMgr(dbpath);

	new TradingService(dbMgr, iorfile);
	orb.run();
	
	dbMgr.shutdown();
	System.exit(0);
    }


    protected static void usage()
    {
	System.err.println("Usage: org.jacorb.trading.TradingService <iorfile> [-d dbpath]");
	System.exit(1);
    }

    private int getProperty(String prop_name, int default_val){
	String _res = org.jacorb.util.Environment.getProperty(prop_name);
	int _value = default_val;
	if (_res != null){
	    try{
		_value = Integer.parseInt(_res);
	    }catch (Exception _e){
		// sth wrong, ignore
	    }
	}
	return _value;
    }

    private FollowOption getProperty(String prop_name, FollowOption default_val){
	String _res = org.jacorb.util.Environment.getProperty(prop_name);
	int _value = default_val.value();
	if (_res != null){
	    try{
		_value = Integer.parseInt(_res);
	    }catch (Exception _e){
		// sth wrong, ignore
	    }
	}
	return FollowOption.from_int(_value);
    }

    private boolean getProperty(String prop_name, boolean default_val)
    {
	String _res = org.jacorb.util.Environment.getProperty(prop_name);
	boolean _value = default_val;
	if (_res != null)
	{
	    try
	    {
		_value = Boolean.valueOf(_res).booleanValue();
	    }
	    catch (Exception _e)
	    {
		// sth wrong, ignore
	    }
	}
	return _value;
    }
}









