
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

package org.jacorb.trading.impl;

import java.util.*;
import org.omg.CosTradingRepos.*;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.*;
import org.omg.CosTrading.*;
import org.jacorb.trading.constraint.*;
import org.jacorb.trading.db.OfferDatabase;
import org.jacorb.trading.util.*;

/**
 * Implementation of CosTrading::Admin
 */

public class AdminImpl 
    extends org.omg.CosTrading.AdminPOA
{
    private TraderComp m_traderComp;
    private SupportAttrib m_support;
    private ImportAttrib m_import;
    private LinkAttrib m_link;
    private OfferDatabase m_db;
    private ServiceTypeRepository m_repos;
    private byte[] m_request_id_stem;

    private AdminImpl()
    {
    }

    public AdminImpl(
		     TraderComp traderComp,
		     SupportAttrib supportAttrib,
		     ImportAttrib importAttrib,
		     LinkAttrib linkAttrib,
		     OfferDatabase db,
		     byte[] stem)
    {
	m_traderComp = traderComp;
	m_support = supportAttrib;
	m_import = importAttrib;
	m_link = linkAttrib;
	m_db = db;
	org.omg.CORBA.Object obj = supportAttrib.getTypeRepos();
	m_repos = ServiceTypeRepositoryHelper.narrow(obj);
	m_request_id_stem = stem;
    }


    // operations inherited from CosTrading::TraderComponents

    public Lookup lookup_if()
    {
	return m_traderComp.getLookupInterface();
    }


    public Register register_if()
    {
	return m_traderComp.getRegisterInterface();
    }


    public Link link_if()
    {
	return m_traderComp.getLinkInterface();
    }


    public Proxy proxy_if()
    {
	return m_traderComp.getProxyInterface();
    }


    public Admin admin_if()
    {
	return m_traderComp.getAdminInterface();
    }


    // operations inherited from CosTrading::SupportAttributes

    public boolean supports_modifiable_properties()
    {
	return m_support.getModifiableProperties();
    }


    public boolean supports_dynamic_properties()
    {
	return m_support.getDynamicProperties();
    }


    public boolean supports_proxy_offers()
    {
	return m_support.getProxyOffers();
    }


    public org.omg.CORBA.Object type_repos()
    {
	return m_support.getTypeRepos();
    }


    // operations inherited from CosTrading::SupportAttributes


    public int def_search_card()
    {
	return m_import.getDefSearchCard();
    }


    public int max_search_card()
    {
	return m_import.getMaxSearchCard();
    }


    public int def_match_card()
    {
	return m_import.getDefMatchCard();
    }


    public int max_match_card()
    {
	return m_import.getMaxMatchCard();
    }


    public int def_return_card()
    {
	return m_import.getDefReturnCard();
    }


    public int max_return_card()
    {
	return m_import.getMaxReturnCard();
    }


    public int max_list()
    {
	return m_import.getMaxList();
    }


    public int def_hop_count()
    {
	return m_import.getDefHopCount();
    }


    public int max_hop_count()
    {
	return m_import.getMaxHopCount();
    }


    public FollowOption def_follow_policy()
    {
	return m_import.getDefFollowPolicy();
    }


    public FollowOption max_follow_policy()
    {
	return m_import.getMaxFollowPolicy();
    }


    // operations inherited from CosTrading::LinkAttributes


    public FollowOption max_link_follow_policy()
    {
	return m_link.getMaxLinkFollowPolicy();
    }


    // operations inherited from CosTrading::Admin


    public byte[] request_id_stem()
    {
	return m_request_id_stem; 
    }


    public int set_def_search_card(int value)
    {
	return m_import.setDefSearchCard(value);
    }


    public int set_max_search_card(int value)
    {
	return m_import.setMaxSearchCard(value);
    }


    public int set_def_match_card(int value)
    {
	return m_import.setDefMatchCard(value);
    }


    public int set_max_match_card(int value)
    {
	return m_import.setMaxMatchCard(value);
    }


    public int set_def_return_card(int value)
    {
	return m_import.setDefReturnCard(value);
    }


    public int set_max_return_card(int value)
    {
	return m_import.setMaxReturnCard(value);
    }


    public int set_max_list(int value)
    {
	return m_import.setMaxList(value);
    }


    public boolean set_supports_modifiable_properties(boolean value)
    {
	return m_support.setModifiableProperties(value);
    }


    public boolean set_supports_dynamic_properties(boolean value)
    {
	return m_support.setDynamicProperties(value);
    }


    public boolean set_supports_proxy_offers(boolean value)
    {
	return m_support.setProxyOffers(value);
    }


    public int set_def_hop_count(int value)
    {
	return m_import.setDefHopCount(value);
    }


    public int set_max_hop_count(int value)
    {
	return m_import.setMaxHopCount(value);
    }


    public FollowOption set_def_follow_policy(FollowOption policy)
    {
	return m_import.setDefFollowPolicy(policy);
    }


    public FollowOption set_max_follow_policy(FollowOption policy)
    {
	return m_import.setMaxFollowPolicy(policy);
    }


    public FollowOption set_max_link_follow_policy(FollowOption policy)
    {
	return m_link.setMaxLinkFollowPolicy(policy);
    }


    public org.omg.CORBA.Object set_type_repos(org.omg.CORBA.Object repository)
    {
	return m_support.setTypeRepos(repository);
    }


    public byte[] set_request_id_stem(byte[] stem)
    {
	byte[] _old = m_request_id_stem;
	m_request_id_stem = stem;
	return _old;  
    }


    public void list_offers(
			    int how_many,
			    OfferIdSeqHolder ids,
			    OfferIdIteratorHolder id_itr)
	throws NotImplemented
    {
	// obtain the list of all service type names
	SpecifiedServiceTypes whichTypes = new SpecifiedServiceTypes();
	//whichTypes._default(ListOption.all);
	//    whichTypes.all_dummy((short)0);
	// GB:
	whichTypes.__default();

	String[] allNames = m_repos.list_types(whichTypes);

	try {
	    m_db.begin(OfferDatabase.READ);

	    // construct a Vector of all offers for all types
	    Vector vec = new Vector();
	    for (int i = 0; i < allNames.length; i++) {
		String[] seq = m_db.getOfferIds(allNames[i]);
		if (seq != null)
		    for (int n = 0; n < seq.length; n++)
			vec.addElement(seq[n]);
	    }

	    int offerCount = vec.size();
	    int seqCount = Math.min(offerCount, how_many);

	    ids.value = new String[seqCount];
	    int count = 0;
	    Enumeration e = vec.elements();
	    while (e.hasMoreElements() && count < seqCount) {
		String id = (String)e.nextElement();
		ids.value[count] = id;
		count++;
	    }

	    // construct an iterator if necessary
	    if (seqCount < offerCount) {
		// create a sequence holding the remaining offer IDs
		int restCount = offerCount - seqCount;
		String[] rest = new String[restCount];

		int pos = 0;
		while (e.hasMoreElements() && pos < restCount) {
		    String id = (String)e.nextElement();
		    rest[pos] = id;
		    pos++;
		}

		OfferIdIteratorImpl iter = new OfferIdIteratorImpl(rest);
		iter._this_object( _orb() );
		id_itr.value = iter._this();
	    }
	}
	finally {
	    m_db.end();
	}
    }


    public void list_proxies(
			     int how_many,
			     OfferIdSeqHolder ids,
			     OfferIdIteratorHolder id_itr)
	throws NotImplemented
    {
	// obtain the list of all service type names
	SpecifiedServiceTypes whichTypes = new SpecifiedServiceTypes();
	//whichTypes._default(ListOption.all);
	//GB:    whichTypes.all_dummy((short)0);
	whichTypes.__default();

	String[] allNames = m_repos.list_types(whichTypes);

	try {
	    m_db.begin(OfferDatabase.READ);

	    // construct a Vector of all offers for all types
	    Vector vec = new Vector();
	    for (int i = 0; i < allNames.length; i++) {
		String[] seq = m_db.getProxyOfferIds(allNames[i]);
		if (seq != null)
		    for (int n = 0; n < seq.length; n++)
			vec.addElement(seq[n]);
	    }

	    int offerCount = vec.size();
	    int seqCount = Math.min(offerCount, how_many);

	    ids.value = new String[seqCount];
	    int count = 0;
	    Enumeration e = vec.elements();
	    while (e.hasMoreElements() && count < seqCount) {
		String id = (String)e.nextElement();
		ids.value[count] = id;
		count++;
	    }

	    // construct an iterator if necessary
	    if (seqCount < offerCount) {
		// create a sequence holding the remaining offer IDs
		int restCount = offerCount - seqCount;
		String[] rest = new String[restCount];

		int pos = 0;
		while (e.hasMoreElements() && pos < restCount) {
		    String id = (String)e.nextElement();
		    rest[pos] = id;
		    pos++;
		}

		OfferIdIteratorImpl iter = new OfferIdIteratorImpl(rest);
		iter._this_object( _orb() );
		id_itr.value = iter._this();
	    }
	}
	finally {
	    m_db.end();
	}
    }
}










