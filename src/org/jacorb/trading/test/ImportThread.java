package org.jacorb.trading.test;

import org.omg.CosTrading.*;
import org.omg.CosTrading.RegisterPackage.*;
import org.omg.CosTrading.LookupPackage.*;
import org.omg.CosTradingRepos.*;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.*;
import org.omg.CORBA.*;
import java.util.*;
/**
 * This class is an importer, which randomly selects a trader to query.
 *
 * @author Nicolas Noffke
 */

public class ImportThread extends Thread {
    private static final int TOTAL_QUERIES = 100;
    private static final int MAX_SLEEP = 10;

    private static long m_received_queries = 0;
    private static Random m_rand = new Random();

    private Lookup[] m_lookup;
    private String m_type;
    private org.omg.CORBA.ORB m_orb;

    public int m_correct_results = 0;
    public int m_incorrect_results = 0;

    public ImportThread(String name, Lookup[] lookup, String type, org.omg.CORBA.ORB orb){
	super(name);
	m_lookup = lookup;
	m_type = type;
	m_orb = orb;

	start();
    }
    
    public void run (){
	//build import policies
	org.omg.CosTrading.Policy[] _policies = new org.omg.CosTrading.Policy[3];
	_policies[0] = new org.omg.CosTrading.Policy();
	_policies[0].name = "exact_type_match";
	_policies[0].value = m_orb.create_any();
	_policies[0].value.insert_boolean(false);
	_policies[1] = new org.omg.CosTrading.Policy();
	_policies[1].name = "use_dynamic_properties";
	_policies[1].value = m_orb.create_any();
	_policies[1].value.insert_boolean(false);
	_policies[2] = new org.omg.CosTrading.Policy();
	_policies[2].name = "use_proxy_offers";
	_policies[2].value = m_orb.create_any();
	_policies[2].value.insert_boolean(false);

	for (int _i = 0; _i < TOTAL_QUERIES; _i++){
	    try {
		//build up import parameters
		String _constraint = "TRUE";
		String _preference = "";
		
		SpecifiedProps _desired_props = new SpecifiedProps();
		_desired_props.all_dummy((short)0);	    
		
		OfferSeqHolder _offers = new OfferSeqHolder();
		OfferIteratorHolder _iter = new OfferIteratorHolder();
		PolicyNameSeqHolder _limits = new PolicyNameSeqHolder();

		//randomly select a trader and query it
		int n = Math.abs(m_rand.nextInt() % 4);
		m_lookup[n].query(m_type, _constraint, _preference, _policies, _desired_props, 20,
				  _offers, _iter, _limits);
		
		boolean[] _offer_ok = new boolean[8];
		if (_offers.value != null && _offers.value.length == 8){

		    //init array
		    for (int _j = 0; _j < 8; _j++)
			_offer_ok[_j] = false;

		    //check if result correct (offers are numbers)
		    for (int _j = 0; _j < 8; _j++){
			String _name = _offers.value[_j].properties[0].value.extract_string();
			_offer_ok[Integer.parseInt(_name)] = true;
		    }

		    //test if all expected results were received
		    boolean _result = true;
		    for (int _j = 0; _j < 8; _j++)
			_result &= _offer_ok[_j];
		    
		    System.out.println("(" + m_received_queries++ + " / " + _i + ") Thread " + getName() + 
				       " received a correct result");
		    m_correct_results++;
		}
		else {
		    System.out.println("(" + m_received_queries++ + " / " + _i + ") Thread " + getName() + 
				       " received an incorrect result");
		    System.out.println("Reason: only " + _offers.value.length + " offers instead of 8 received");
		    m_incorrect_results++;
		}
		
		sleep(Math.abs(m_rand.nextInt() % MAX_SLEEP));
		
	    } catch (Exception _e){
		System.out.println("(" + m_received_queries++ + " / " + _i + ") Thread " + getName() + 
				   " caught an exception");
		_e.printStackTrace();
		m_incorrect_results++;
	    }
	}
	System.out.println("(" + getName() + ") Finished querying. Received " + m_correct_results + 
			   "correct results and " + m_incorrect_results + " incorrect results");

    }    
} // ImportThread
