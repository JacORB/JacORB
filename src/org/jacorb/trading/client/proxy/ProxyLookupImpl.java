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
import org.omg.CosTrading.LookupPackage.*;
import org.jacorb.trading.client.util.*;

public class ProxyLookupImpl extends org.omg.CosTrading.LookupPOA
{
    public ProxyLookupImpl()
    {
    }


    /**
     * Overridden from Visibroker's _LookupImplBase; we do this
     * instead of the unportable super(objectName) we'd have to put in
     * the constructor; the presence of this method should not affect
     * use with other ORBs
     */
    public String _object_name()
    {
	return "ProxyDemo";
    }


    // operations inherited from CosTrading::TraderComponents

    public Lookup lookup_if()
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    public Register register_if()
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    public Link link_if()
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    public Proxy proxy_if()
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    public Admin admin_if()
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    // operations inherited from CosTrading::SupportAttributes

    public boolean supports_modifiable_properties()
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    public boolean supports_dynamic_properties()
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    public boolean supports_proxy_offers()
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    public org.omg.CORBA.Object type_repos()
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    // operations inherited from CosTrading::SupportAttributes


    public int def_search_card()
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    public int max_search_card()
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    public int def_match_card()
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    public int max_match_card()
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    public int def_return_card()
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    public int max_return_card()
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    public int max_list()
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    public int def_hop_count()
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    public int max_hop_count()
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    public FollowOption def_follow_policy()
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    public FollowOption max_follow_policy()
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();
    }



    // operations inherited from CosTrading::Lookup

    public void query(
		      String type,
		      String constr,
		      String pref,
		      org.omg.CosTrading.Policy[] policies,
		      SpecifiedProps desired_props,
		      int how_many,
		      OfferSeqHolder offers,
		      OfferIteratorHolder offer_itr,
		      PolicyNameSeqHolder limits_applied)
	throws IllegalServiceType,
	UnknownServiceType,
	IllegalConstraint,
	IllegalPreference,
	IllegalPolicyName,
	PolicyTypeMismatch,
	InvalidPolicyValue,
	IllegalPropertyName,
	DuplicatePropertyName,
	DuplicatePolicyName
    {
	PrintWriter pw = new PrintWriter(System.out);
	pw.println("Type = " + type);
	pw.println("Constraint = '" + constr + "'");
	pw.println("Preference = '" + pref + "'");

	for (int i = 0; i < policies.length; i++) 
	{
	    pw.print("Policy[" + i + "] = {" + policies[i].name + ", ");
	    AnyUtil.print(_orb(), pw, policies[i].value);
	    pw.println("}");
	}
	pw.flush();

	/********
		 // sleep for a while - also used for testing
		 try {
		 Thread.currentThread().sleep(10000); // 10 secs
		 }
		 catch (InterruptedException e) {
		 }
	********/

	Offer[] arr = createOffers();

	// process the offers
	int seqCount = Math.min(arr.length, how_many);

	offers.value = new Offer[seqCount];
	int count = 0;
	while (count < seqCount) {
	    offers.value[count] = arr[count];
	    count++;
	}

	// construct an iterator if necessary
	if (seqCount < arr.length) {
	    // create a sequence holding the remaining offers
	    int restCount = arr.length - seqCount;
	    Offer[] rest = new Offer[restCount];

	    int pos = 0;
	    while (pos < restCount) {
		rest[pos] = arr[count];
		pos++;
		count++;
	    }

	    OfferIteratorImpl iter = new OfferIteratorImpl(rest, 0);
	    //	    iter._orb( _orb() );
	    offer_itr.value = iter._this();
	}

	limits_applied.value = new String[0];
    }


    protected Offer[] createOffers()
    {
	Offer[] result;

	ORB orb = org.omg.CORBA.ORB.init();
	Random rand = new Random();
	int numOffers = Math.abs(rand.nextInt()) % 4;
	result = new Offer[numOffers];

	for (int i = 0; i < numOffers; i++) {
	    // just use this as the offer's object
	    result[i] = new Offer();
	    result[i].reference = this._this(); // GB: _this()
	    result[i].properties = new Property[3];

	    int num = 0;

	    // the "name" property
	    result[i].properties[num] = new Property();
	    result[i].properties[num].name = "name";
	    result[i].properties[num].value = orb.create_any();
	    result[i].properties[num].value.insert_string("proxy offer " + i);
	    num++;

	    // the "cost" property
	    result[i].properties[num] = new Property();
	    result[i].properties[num].name = "cost";
	    result[i].properties[num].value = orb.create_any();
	    result[i].properties[num].value.insert_double(
							  Math.abs(rand.nextDouble()));
	    num++;

	    // the "version" property
	    result[i].properties[num] = new Property();
	    result[i].properties[num].name = "version";
	    result[i].properties[num].value = orb.create_any();
	    result[i].properties[num].value.insert_string("1.0" + i);
	    num++;
	}

	return result;
    }
}




