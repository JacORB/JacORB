
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

package org.jacorb.trading.impl;

import java.util.*;
import org.omg.CORBA.*;
import org.omg.CosTrading.*;
import org.omg.CosTrading.LookupPackage.*;
import org.omg.CosTrading.RegisterPackage.OfferInfo;
import org.omg.CosTrading.ProxyPackage.ProxyInfo;
import org.omg.CosTrading.LinkPackage.LinkInfo;
import org.omg.CosTradingRepos.*;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.*;
import org.jacorb.trading.constraint.*;
import org.jacorb.trading.db.OfferDatabase;
import org.jacorb.trading.util.*;

/**
 * Implementation of CosTrading::Lookup
 */

public class LookupImpl 
    extends org.omg.CosTrading.LookupPOA
{
    private TraderComp m_traderComp;
    private SupportAttrib m_support;
    private ImportAttrib m_import;
    private OfferDatabase m_db;
    private ServiceTypeRepository m_repos;

    //////////////////////////////////////////////////////////////// new!
    private LinkImpl m_link_if;  // for efficient access to the links
    private static int m_query_counter; // suffix for request_id
    private Hashtable m_query_cache_lookup; // efficient lookup of request_ids
    private Vector m_query_cache_queue; // efficient removal of old queries
    private int m_query_cache_max = 100; //max no. of queries to be cached
    private QueryPropagator m_query_distrib; // threadpool for concurrent query distribution
    private LinkInfo[] m_links_cache; // array of federated traders


    private static int count = 0;
    private boolean m_debug = false;
    private int m_debug_verbosity = 2;
    //////////////////////////////////////////////////////////////// new!
    private LookupImpl()
    {
    }


    public LookupImpl(
		      TraderComp traderComp,
		      SupportAttrib supportAttrib,
		      ImportAttrib importAttrib,
		      OfferDatabase db,
		      LinkImpl link)
    {
	m_traderComp = traderComp;
	m_support = supportAttrib;
	m_import = importAttrib;
	m_db = db;
	org.omg.CORBA.Object obj = supportAttrib.getTypeRepos();
	m_repos = ServiceTypeRepositoryHelper.narrow(obj);

	//////////////////////////////////////////////////////////////// new!
	m_link_if = link;
	String _m_cache_max =  org.jacorb.util.Environment.getProperty("jtrader.impl.cache_max");
	if (_m_cache_max != null){
	    try{
		m_query_cache_max = Integer.parseInt(_m_cache_max);
	    }catch (Exception _e){
		//possibly wrong number
		org.jacorb.util.Debug.output(2, _e);
	    }
	}

	// standard load factor of Hashtable is 0.75, so if we want to store efficiently 
	// m_query_cache_max elements, we have to account for the load factor.
	// The +10 is just a safety margin.
	m_query_cache_lookup = new Hashtable((int) ((100.0 / 75.0) * (double) m_query_cache_max) + 10);
	m_query_cache_queue = new Vector(m_query_cache_max + 2);

	m_query_distrib = new QueryPropagator();

	String _tmp = org.jacorb.util.Environment.getProperty("jtrader.debug");
	if (_tmp != null){
	    try {
		m_debug = (Boolean.valueOf(_tmp)).booleanValue();
	    }catch (Exception _e){
		// possibly invalid number
		org.jacorb.util.Debug.output(2, _e);
	    }
	}
	//m_debug = false;
 
    
	_tmp = org.jacorb.util.Environment.getProperty("jtrader.debug_verbosity"); 
	if (_tmp != null){
	    try {
		m_debug_verbosity = Integer.parseInt(_tmp);
	    }catch (Exception _e){
		// possibly invalid number
		org.jacorb.util.Debug.output(2, _e);
	    }
	}
	//////////////////////////////////////////////////////////////// new!
    }


    /**
     * Overridden from Visibroker's _LookupImplBase; we do this instead
     * of the unportable super("TradingService") we'd have to put in
     * the constructor; the presence of this method should not affect
     * use with other ORBs
     */
    public String _object_name()
    {
	return "TradingService";
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



    // operations inherited from CosTrading::Lookup

    public void query(String type,
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
	int no = count++;
	if (m_debug) 
	    org.jacorb.util.Debug.output(m_debug_verbosity, "### query started: " + no);

	// retrieve complete information about the service type from the
	// repository - may throw IllegalServiceType, UnknownServiceType
	TypeStruct ts = m_repos.fully_describe_type(type);

	// build a hashtable of the policies
	Hashtable policyTable = new Hashtable();
	for (int i = 0; i < policies.length; i++) 
	{
	    // check for duplicates
	    if (policyTable.containsKey(policies[i].name))
		throw new DuplicatePolicyName(policies[i].name);
	    policyTable.put(policies[i].name, policies[i].value);
	}

	if (m_debug) 
	    org.jacorb.util.Debug.output(m_debug_verbosity, "### check id: " + no);

	// check request_id
	// request_id set?
	String _id = getPolicyValue(policyTable, "request_id", new String());
	Vector _generated_policies = new Vector();

	if (_id.length() == 0)
	{
	    // no request_id set, so we're the first to receive this offer
	    //generating new one
	    StringBuffer _new_id = new StringBuffer(new String(admin_if().request_id_stem()));
	    _new_id.append(m_query_counter++);
	    org.omg.CosTrading.Policy _id_policy = new org.omg.CosTrading.Policy();
	    _id_policy.name = "request_id";
	    _id_policy.value = _orb().create_any();
	    _id_policy.value.insert_string(_new_id.toString());

	    _generated_policies.addElement(_id_policy);
	    _id = _new_id.toString();
	}

	//don't process query, if we did already
	if (queryAlreadyEncountered(_id))
	{
	    // initializing anyway so we don't run into NullPointerEcxecptions
	    offers.value = new Offer[0];
	    limits_applied.value = new String[0];
	    if (m_debug) 
		org.jacorb.util.Debug.output(m_debug_verbosity, "### Refused query request. Reason: Query was already executed");
	    if (m_debug) 
		org.jacorb.util.Debug.output(m_debug_verbosity, "### Returned from query " + no);
	    return;
	}
	if (m_debug) 
	    org.jacorb.util.Debug.output(m_debug_verbosity, "### passed id check: " + no);

	// if hop_count policy not set, we generate one from the defaults
	if (! policyTable.containsKey("hop_count"))
	{
	    org.omg.CosTrading.Policy _hop_policy = new org.omg.CosTrading.Policy();
	    _hop_policy.name = "hop_count";
	    _hop_policy.value = _orb().create_any();
	    _hop_policy.value.insert_ulong(def_hop_count());
	
	    _generated_policies.addElement(_hop_policy);
	    policyTable.put("hop_count", _hop_policy.value);
	}

	// if we have generated new policies, we merge them with the existing ones
	if (_generated_policies.size() > 0)
	{
	    org.omg.CosTrading.Policy[] _new_policies = new org.omg.CosTrading.Policy[policies.length + 
										     _generated_policies.size()];
	    System.arraycopy(policies, 0, _new_policies, 0, policies.length);

	    Enumeration _gen_polic_enum = _generated_policies.elements();
	    int _j = policies.length;
	    while(_gen_polic_enum.hasMoreElements())
		_new_policies[_j++] = (org.omg.CosTrading.Policy) _gen_polic_enum.nextElement();
	
	    policies = _new_policies;
	}

	//////////////////////////////////////////////////////////////// new!  
	if (m_debug) 
	    org.jacorb.util.Debug.output(m_debug_verbosity, "### passed policy gen: " + no);

	// determine our limiting policies
	int searchCard = getPolicyValue(policyTable, "search_card",
					def_search_card(), max_search_card());
	int matchCard = getPolicyValue(policyTable, "match_card",
				       def_match_card(), max_match_card());
	int returnCard = getPolicyValue(policyTable, "return_card",
					def_return_card(), max_return_card());
	boolean exactType = getPolicyValue(policyTable, "exact_type_match", false);
  
	// importer cannot override use_dynamic_properties if the
	// trader doesn't support it
	boolean useDynamic;
	if (supports_dynamic_properties() == false)
	    useDynamic = false;
	else
	    useDynamic =
		getPolicyValue(policyTable, "use_dynamic_properties", true);

	// importer cannot override use_modifiable_properties if the
	// trader doesn't support it
	boolean useModifiable;
	if (supports_modifiable_properties() == false)
	    useModifiable = false;
	else
	    useModifiable =
		getPolicyValue(policyTable, "use_modifiable_properties", true);

	// importer cannot override use_proxy_offers if the
	// trader doesn't support it
	boolean useProxyOffers;
	if (supports_proxy_offers() == false)
	    useProxyOffers = false;
	else
	    useProxyOffers = getPolicyValue(policyTable, "use_proxy_offers", true);

	//////////////////////////////////////////////////////////////// new!
	//determine link_follow_rule
	FollowOption link_follow_rule = getPolicyValue(policyTable, "link_follow_rule", 
						       def_follow_policy(),
						       max_follow_policy());
	// determine hop count    
	int hop_count = getPolicyValue(policyTable, "hop_count", def_hop_count(), max_hop_count());

	//decrement hop_count for query distribution
	Any _hop = (Any) policyTable.get("hop_count");
	_hop.insert_ulong(_hop.extract_ulong() - 1);


	//////////////////////////////////////////////////////////////// new!
	// starting query distribution here, since if we have illegal constraints,
	// we don't have to distribute.
	QueryContainer _templ = new QueryContainer(type,constr, pref,
						   policies, desired_props, 
						   how_many, null);
	Vector _queries = new Vector();

	// if link_follow_rule is always, we don't find offers offers locally, and we
	// have links with limiting_follow_rule if_no_local and always, we don't want 
	// to use the links with always again
	Hashtable _used_links = new Hashtable();

	if (m_debug) 
	    org.jacorb.util.Debug.output(m_debug_verbosity, "++++++++++++++++++++++++ distribution started");
	if (hop_count > 0 &&
	    link_follow_rule.value() == FollowOption.always.value())
	    distributeQuery(_queries, _templ, link_follow_rule, _used_links);
	if (m_debug) 
	    org.jacorb.util.Debug.output(m_debug_verbosity, "++++++++++++++++++++++++ distribution finished");
	//////////////////////////////////////////////////////////////// new!

	// if no preference is supplied, "first" is the default
	if (pref == null || pref.trim().length() == 0)
	    pref = "first";

	// instantiate the schema object required by the parsers
	SchemaAdapter schema = new SchemaAdapter(ts);

	Constraint constraint = new Constraint(schema);
	Preference preference = new Preference(schema);

	try {
	    // attempt to parse the constraint
	    constraint.parse(constr);
	}
	catch (ParseException ex) {
	    // the exception doesn't include a reason, so we just print it
	    System.out.println("Illegal constraint '" + constr + "'");
	    System.out.println(ex.getMessage());
	    throw new IllegalConstraint(constr);
	}

	try {
	    // attempt to parse the preference
	    preference.parse(pref);
	}
	catch (ParseException ex) {
	    // the exception doesn't include a reason, so we just print it
	    System.out.println("Illegal preference '" + pref + "'");
	    System.out.println(ex.getMessage());
	    throw new IllegalPreference(pref);
	}


	// compose a list of all the service types we will consider;
	// we start with just the type specified
	Vector types = new Vector();
	types.addElement(type);

	// if the client wishes us to consider subtypes, then we need to
	// find all service types "compatible" with the requested type
	// (i.e. the type itself, and any subtypes)
	if (! exactType)
	    findCompatibleTypes(type, types);

	try {
	    m_db.begin(OfferDatabase.READ);

	    // iterate through all of the selected service types, examining
	    // the offers of each, and adding any potential ones to
	    // potentialOffers while respecting the search cardinality limits
	    int searchCount = 0;
	    Vector potentialOffers = new Vector();
	    Enumeration e = types.elements();
 
	    while (e.hasMoreElements() && searchCount < searchCard)
	    {
		String typeName = (String)e.nextElement();
		Hashtable table = m_db.getOffers(typeName);
		if (table == null)
		    continue;
 
		// examine the offers of this service type
		Enumeration o = table.elements();
		while (o.hasMoreElements() && searchCount < searchCard)
		{
		    OfferInfo info = (OfferInfo)o.nextElement();

		    SourceAdapter source =
			new SourceAdapter(info.reference, info.properties);

		    if (considerOffer(source, useDynamic, useModifiable, ts)) {
			potentialOffers.addElement(source);
			searchCount++;
		    }
		}
	    }

	    // iterate through all of the selected service types, examining
	    // the proxy offers of each, and adding any potential ones to
	    // potentialOffers while respecting the search cardinality limits
	    if (useProxyOffers) {
		e = types.elements();
		while (e.hasMoreElements() && searchCount < searchCard)
		{
		    String typeName = (String)e.nextElement();
		    Hashtable table = m_db.getProxyOffers(typeName);
		    if (table == null)
			continue;

		    // examine the offers of this service type
		    Enumeration o = table.elements();
		    while (o.hasMoreElements() && searchCount < searchCard)
		    {
			ProxyInfo info = (ProxyInfo)o.nextElement();

			ProxySourceAdapter source = new ProxySourceAdapter(info);

			if (considerOffer(source, useDynamic, useModifiable, ts)) {
			    potentialOffers.addElement(source);
			    searchCount++;
			}
		    }
		}
	    }
 
	    // this object is used to evaluate offers
	    OfferEvaluator eval = new OfferEvaluator(type, constraint, pref,
						     policies, desired_props, potentialOffers, matchCard);

	    // retrieve the results of the offer evaluation;
	    // this will block until all offers have been evaluated or
	    // until the match cardinality limits are reached; the
	    // matchingOffers vector contains only SourceAdapter objects
	    // (i.e. no ProxySourceAdapter objects)
	    Vector matchingOffers = eval.getResults();
 

	    //////////////////////////////////////////////////////////////// new!
	    // if we didn't get any matching offers locally, we try to fetch them
	    // via the federation
	    if ( matchingOffers.size() == 0 && hop_count > 0 &&
		 link_follow_rule.value() >= FollowOption.if_no_local.value())
		distributeQuery(_queries, _templ, link_follow_rule, _used_links);

	    // processing distributed queries and merging them with local offers
	    Enumeration _results = _queries.elements();
	    Vector _dropped = new Vector();

	    // for collecting the applied_policies of the distributed queries
	    Vector _applied_policies = new Vector();

	    while (_results.hasMoreElements()){  
		QueryContainer _distrib_query = (QueryContainer) _results.nextElement();
		try{
		    if (m_debug) 
			org.jacorb.util.Debug.output(m_debug_verbosity, "+++++++++++++++Lookup, going to wait for " + _distrib_query.no);
		    _distrib_query.resultReady(); //blocks until remote query returned
		    if (m_debug) 
			org.jacorb.util.Debug.output(m_debug_verbosity, "+++++++++++++++Lookup, finished waiting for " + _distrib_query.no);
		}catch(Exception _e){
		    org.jacorb.util.Debug.output(2, _e);
		    continue; // possibly InterruptedException, dropping this result
		}
	  
		UserException _query_exception = _distrib_query.getException();
		if (_query_exception != null){
		    org.jacorb.util.Debug.output(2, _query_exception);
		    _dropped.addElement(_distrib_query);
		    continue; // an exception occured during distributed query execution,
		    // dropping this result
		}

		// get offers
		OfferSeqHolder _offers = _distrib_query.getOffers();
		if (_offers.value == null || _offers.value.length == 0){
		    // possibly loop-prevention refused to execute query
		    _dropped.addElement(_distrib_query);
		    continue;
		}

		// this is a little odd on the second look, since the local offers are 
		// stored as SourceAdapters, and we insert  Offers, but its easier for
		// determining the total amount of results
		for (int i = 0; i < _offers.value.length; i++)
		    matchingOffers.addElement(_offers.value[i]);

		// get limits_applied
		PolicyNameSeqHolder _policy_holder = _distrib_query.getLimits();
		if (_policy_holder.value != null){
		    // merging applied_policies with the others
		    for (int i = 0; i < _policy_holder.value.length; i++)
			_applied_policies.addElement(_policy_holder.value[i]);	
		}	    
	    }
	    for (int _i = 0; _i < _dropped.size(); _i++)
		_queries.removeElement(_dropped.elementAt(_i));
      
	    //////////////////////////////////////////////////////////////// new!

	    int matchCount = matchingOffers.size();
      
	    // order the matching offers using the preference
	    Vector orderedOffers = preference.order(matchingOffers);

	    // process the offers
	    int returnCount = Math.min(matchCount, returnCard);
	    int seqCount = Math.min(returnCount, how_many);

	    offers.value = new Offer[seqCount];
	    int count = 0;
	    e = orderedOffers.elements();
	    while (e.hasMoreElements() && count < seqCount) {
		java.lang.Object _element = e.nextElement();
		if (_element instanceof Offer){
		    offers.value[count] = (Offer) _element;
		    // this offer was retrieved from a federated trader
		}
		else{
		    SourceAdapter src = (SourceAdapter) _element;
		    offers.value[count] = new Offer();
		    offers.value[count].reference = src.getObject();
		    offers.value[count].properties = src.getProperties(desired_props);
		}
		count++;
	    }

	    // construct an iterator if necessary
	    if (seqCount < returnCount) {
		// create a sequence holding the remaining offers
		int restCount = returnCount - seqCount;
		Vector rest = new Vector(restCount);
	  
		int pos = 0;
		while (e.hasMoreElements() && pos < restCount) {
		    java.lang.Object _element = e.nextElement();
		    if (_element instanceof Offer){
			// this offer was retrieved from a federated trader
			rest.addElement(_element);
		    }
		    else{
			SourceAdapter src = (SourceAdapter)_element;
			Offer _off = new Offer();
			_off.reference = src.getObject();
			_off.properties = src.getProperties(desired_props);
			rest.addElement(_off);
		    }
		    pos++;
		}
	
		//////////////////////////////////////////////////////////////// new!  
		// get iterator-based offers
		_results = _queries.elements(); 
		while (pos < restCount && _results.hasMoreElements()){
		    pos++;
		    QueryContainer _distrib_query = (QueryContainer) _results.nextElement();
		    OfferIteratorHolder _itr_holder = _distrib_query.getItr();
		    if (_itr_holder.value != null){
			Offer[] _offers = null;
			try{
			    _offers = ((OfferIteratorImpl) _itr_holder.value).getOffers();
			    // if we have an OfferIteratorImpl, we can simply obtain
			    // a reference to its Offers-Array
			}catch(Exception _e){
			    // possibly the OfferIterator wasn't an OfferIteratorImpl
			}
			if (_offers == null){
			    // no OfferIteratorImpl-Instance, so we have to 
			    // do it the hard way
			    OfferIterator _itr = _itr_holder.value;
			    OfferSeqHolder _seq = new OfferSeqHolder();
			    _itr.next_n(restCount - pos, _seq); // just one call with as much
			    // as possible offers
			    _offers = _seq.value;
			}

			// copy array into vector
			int _i = 0;
			while (_i < _offers.length && pos < restCount){
			    pos++;
			    rest.addElement(_offers[_i++]);
			}
		    }	      
		}

		// build offer-array from vector
		Enumeration _offer_enum = rest.elements();
		Offer[] _offer_array = new Offer[rest.size()];
		int _i = 0;
		while (_offer_enum.hasMoreElements())
		    _offer_array[_i++] = (Offer) _offer_enum.nextElement();
	      
		// set up Iterator
		OfferIteratorImpl iter = new OfferIteratorImpl(_offer_array, 0);
		iter._this_object( _orb() );
		offer_itr.value = iter._this();
		if (m_debug) 
		    org.jacorb.util.Debug.output(m_debug_verbosity, "Returned " + _offer_array.length + " offers via Iterator");
 
	    }

	    // build array of applied_limits from vector
	    Enumeration _applied_limits = _applied_policies.elements();
	    limits_applied.value = new String[_applied_policies.size()];
	    int _i = 0;
	    while (_applied_limits.hasMoreElements())
		limits_applied.value[_i++] = (String) _applied_limits.nextElement();


	    if (m_debug) 
		org.jacorb.util.Debug.output(m_debug_verbosity, "Returned " + offers.value.length + " offers");
	    if (m_debug) 
		org.jacorb.util.Debug.output(m_debug_verbosity, "### Returned from query " + no);
	    //////////////////////////////////////////////////////////////// new! 

	}	
	finally {
	    m_db.end();
	}

    }


    //////////////////////////////////////////////////////////////// new!
    /**
     * This is the loop-prevention mechanism. It checks the request_id-policy
     * if this query was recently already encountered. If not so, the id 
     * is stored. <br>
     * Using a Hashtable *and* a Vector since lookup is slow in a Vector,
     * but a Hashtable has no method to get an element without a key (i.e 
     * the first).
     *
     * @param id  The request_id
     * @return True, if query was recently encountered
     */
    private boolean queryAlreadyEncountered (String id){

	boolean _id_known = m_query_cache_lookup.containsKey(id);
	if (! _id_known){
	    // inserting this one
	    m_query_cache_lookup.put(id, id);
	    m_query_cache_queue.addElement(id);
	    
	    // removing the oldest
	    if (m_query_cache_queue.size() > m_query_cache_max){
		java.lang.Object _old = m_query_cache_queue.firstElement();
		m_query_cache_queue.removeElementAt(0);
		m_query_cache_lookup.remove(_old);
	    }
	}
	return _id_known;
    }

    /**
     * This method looks for a policy-value in the policies-hashtable.
     *
     * @param policies Hashtable to look in
     * @param name The name of the policy
     * @param defaultValue The default, if the hashtable does not contain a value
     * @param maxValue The returned value does not exceed this value
     *
     * @exception PolicyTypeMismatch The type of the hastable-value does not have the expected type
     * @exception InvalidPolicyValue The value is not the expected one
     *
     * @return A FollowOption not more than maxValue
     */
    protected FollowOption getPolicyValue(Hashtable policies,
					  String name,
					  FollowOption defaultValue,
					  FollowOption maxValue)
	throws PolicyTypeMismatch,
	InvalidPolicyValue
    {
	FollowOption result = defaultValue;	

	Any value = (Any) policies.get(name);
	if (value != null) {
	    try {
		TypeCode _type = FollowOptionHelper.type();
		if (! _type.equal(value.type()))
		    throw new PolicyTypeMismatch(new org.omg.CosTrading.Policy(name, value));
		
		result = FollowOptionHelper.extract(value);
	    }
	    catch (BAD_OPERATION e) {
		throw new InvalidPolicyValue(new org.omg.CosTrading.Policy(name, value));
	    }
	}
	return FollowOption.from_int(Math.min(result.value(), maxValue.value()));
    }

    /**
     * This method checks with LinkImpl, if anything has changed. If so, 
     * we update our local link-array. This is for efficiency since we don't have
     * to retrieve the links always from LinkImpl, and because LinkImpl stores the
     * Links in a Hashtable
     *
     */
    private void updateLinks(){

	if (m_link_if.linksChanged())
	    m_links_cache = m_link_if.getLinks();
    }

    /**
     * This method sets up the QueryContainer-instances with the lookup-interfaces from
     * the link-array. If the link_follow_rule does not exceed the limiting_follow_rule
     * of a link the QueryContainer is handed to the QueryPropagato-object and executed.
     *
     * @param queries All new QueryContainer-objects are put here
     * @param template Contains the queries parameters. Mainly for keeping the interface small.
     * @param link_follow_rule The actual link_follow_rule-policy
     * @param used_links  For storing and lookup of links that have aready been accessed
     */
    private void distributeQuery(Vector queries, 
				 QueryContainer template, 
				 FollowOption link_follow_rule,
				 Hashtable used_links){

	updateLinks();
	LinkInfo[] _links = m_links_cache;
	for(int i = 0; i < _links.length; i++){
	    if (_links[i].limiting_follow_rule.value() >= link_follow_rule.value() && 
		! used_links.containsKey(_links[i].target)){
		QueryContainer _query = new QueryContainer(template, _links[i].target);
		queries.addElement(_query);
		m_query_distrib.putWork(_query);
		used_links.put(_links[i].target, _links[i].target);
	    }
	}
    }

    protected int getPolicyValue(Hashtable policies,
				 String name,
				 int defaultValue,
				 int maxValue)
	throws PolicyTypeMismatch,
	InvalidPolicyValue
    {

	int result = defaultValue;
	Any value = (Any)policies.get(name);

	if (value != null) 
	{
	    try 
	    {
		if (value.type().kind() != TCKind.tk_ulong)
		    throw new PolicyTypeMismatch(new org.omg.CosTrading.Policy(name, value));

		result = value.extract_ulong();
	    }
	    catch (BAD_OPERATION e) 
	    {
		throw new InvalidPolicyValue(new org.omg.CosTrading.Policy(name, value));
	    }
	}

	result = Math.min(result, maxValue);
	return result;

    }


    protected boolean getPolicyValue(Hashtable policies,
				     String name,
				     boolean defaultValue)
	throws PolicyTypeMismatch,
	InvalidPolicyValue
    {

	boolean result = defaultValue;
	Any value = (Any)policies.get(name);

	if (value != null) 
	{
	    try 
	    {
		if (value.type().kind() != TCKind.tk_boolean)
		    throw new PolicyTypeMismatch(new org.omg.CosTrading.Policy(name, value));

		result = value.extract_boolean();
	    }
	    catch (BAD_OPERATION e) 
	    {
		throw new InvalidPolicyValue(new org.omg.CosTrading.Policy(name, value));
	    }
	}
	return result;
    }


    protected String getPolicyValue(Hashtable policies,
				    String name,
				    String defaultValue)
	throws PolicyTypeMismatch,
	InvalidPolicyValue
    {
   
	String result = defaultValue;
	Any value = (Any)policies.get(name);

	if (value != null) {
	    try {
		if (value.type().kind() != TCKind.tk_string)
		    throw new PolicyTypeMismatch(new org.omg.CosTrading.Policy(name, value));

		result = value.extract_string();
	    }
	    catch (BAD_OPERATION e) 
	    {
		throw new InvalidPolicyValue(new org.omg.CosTrading.Policy(name, value));
	    }
	}
	return result;
    }


    protected void findCompatibleTypes(String type, Vector types)
    {
	// obtain the list of all service type names
	SpecifiedServiceTypes whichTypes = new SpecifiedServiceTypes();
	//whichTypes._default(ListOption.all);
	// GB:    whichTypes.all_dummy((short)0);
	whichTypes.__default();

	String[] allNames = m_repos.list_types(whichTypes);

	// iterate through all service types, obtaining the full description
	// of each to see if the service type in question is a supertype
	for (int i = 0; i < allNames.length; i++) {
	    try {
		TypeStruct ts = m_repos.fully_describe_type(allNames[i]);

		for (int n = 0; n < ts.super_types.length; n++) {
		    if (type.equals(ts.super_types[n])) {
			if (! types.contains(allNames[i]))
			    types.addElement(allNames[i]);
			break;
		    }
		}
	    }
	    catch (IllegalServiceType e) {
		// ignore
	    }
	    catch (UnknownServiceType e) {
		// ignore
	    }
	}
    }


    protected boolean considerOffer(
				    SourceAdapter source,
				    boolean useDynamic,
				    boolean useModifiable,
				    TypeStruct ts)
    {
	if (! useDynamic) {
	    Property[] props = source.getProperties();

	    // check if any property in the offer is dynamic
	    if (PropUtil.hasDynamicProperties(props))
		return false;
	}

	if (! useModifiable) {
	    // check if any property in the offer is modifiable
	    for (int i = 0; i < ts.props.length; i++) {
		// if the mode of the property is modifiable, then check
		// if this offer has a definition for the property
		if (ts.props[i].mode == PropertyMode.PROP_NORMAL ||
		    ts.props[i].mode == PropertyMode.PROP_MANDATORY)
		    if (source.exists(ts.props[i].name))
			return false;
	    }
	}

	return true;
    }
}










