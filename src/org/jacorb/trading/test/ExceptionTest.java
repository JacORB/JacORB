package org.jacorb.trading.test;

import org.omg.CosTrading.*;
import org.omg.CosTrading.RegisterPackage.*;
import org.omg.CosTrading.LookupPackage.*;
import org.omg.CosTrading.LinkPackage.*;
import org.omg.CosTradingRepos.*;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.*;
import org.omg.CORBA.*;
import java.io.*;

/**
 * This class launches a single trader and test, if the various exceptions
 * thrown by the methods from the link interface, are thrown correctly.
 *
 * @author Nicolas Noffke
 */
public class ExceptionTest{
    public static void main(String[] args) {
	if (args.length == 0){
	    System.out.println("Usage: ExceptionTest <ior-file>");
	    System.exit(0);
	}

	int _correct_results = 0;
	int _incorrect_results = 0;
  
	try{

	    //start trader
	    Runtime _rt = Runtime.getRuntime();
	    new OutputForwarder(_rt.exec("ts " + args[0] + " -d db"), "Trader");

	    System.out.println("Press any key when Trader is ready");
	    System.in.read();

	    //start orb
	    org.omg.CORBA.ORB _orb = org.omg.CORBA.ORB.init(args, null);

	    //get ior-file content
	    BufferedReader _ior_in = new BufferedReader(new FileReader(new File(args[0])));
	    String _ior_str = _ior_in.readLine();
	    _ior_in.close();

	    //get interfaces
	    Lookup _lookup = LookupHelper.narrow(_orb.string_to_object(_ior_str));

	    if( _lookup == null ){
		System.out.println("No lookup!");
		System.exit(1);
	    }

	    Link _link = LinkHelper.narrow(_lookup.link_if());

	    if( _link == null )
	    {
		System.out.println("No link!");
		System.exit(1);
	    }

	    System.out.println("********** Testing exceptions **********");
	    

	    //IllegalLinkName
	    try{
		System.out.println("\nExpecting IllegalLinkName");
		_link.add_link("", _lookup, FollowOption.if_no_local, FollowOption.if_no_local);
	    }catch(Exception _e){
		System.out.println("Caught "  + _e.toString());
		if (_e instanceof IllegalLinkName){
		    System.out.println("Test passed");
		    _correct_results++;
		}
		else{
		    System.out.println("Test failed");
		    _incorrect_results++;
		}
	    }	    

	    // DuplicateLinkName
	    try{
		_link.add_link("link", _lookup, FollowOption.if_no_local, FollowOption.if_no_local);
		System.out.println("\nExpecting DuplicateLinkName"); 
		_link.add_link("link", _lookup, FollowOption.if_no_local, FollowOption.if_no_local);
	    }catch(Exception _e){		
		System.out.println("Caught "  + _e.toString());
		if (_e instanceof DuplicateLinkName){
		    System.out.println("Test passed");
		    _correct_results++;
		}
		else{
		    System.out.println("Test failed");
		    _incorrect_results++;
		}
	    }

	    // InvalidLookupRef
	    try{
		System.out.println("\nExpecting InvalidLookupRef");
		_link.add_link("link2", null, FollowOption.if_no_local, FollowOption.if_no_local);
	    }catch(Exception _e){
		System.out.println("Caught "  + _e.toString());
		if (_e instanceof InvalidLookupRef){
		    System.out.println("Test passed");
		    _correct_results++;
		}
		else{
		    System.out.println("Test failed");
		    _incorrect_results++;
		}
	    }
	    
	    //DefaultFollowTooPermissive
	    try{
		System.out.println("\nExpecting DefaultFollowTooPermissive");
		_link.add_link("link3", _lookup, FollowOption.if_no_local, FollowOption.local_only);
	    }catch(Exception _e){
		System.out.println("Caught "  + _e.toString());
		if (_e instanceof DefaultFollowTooPermissive){
		    System.out.println("Test passed");
		    _correct_results++;
		}
		else{
		    System.out.println("Test failed");
		    _incorrect_results++;
		}
	    }

	    // LimitingFollowTooPermissive
	    try{
		System.out.println("\nExpecting LimitingFollowTooPermissive");
		_link.add_link("link4", _lookup, FollowOption.if_no_local, FollowOption.always);
	    }catch(Exception _e){
		System.out.println("Caught "  + _e.toString());
		if (_e instanceof LimitingFollowTooPermissive){
		    System.out.println("Test passed");
		    _correct_results++;
		}
		else{
		    System.out.println("Test failed");
		    _incorrect_results++;
		}
	    }

	    // test modify_link
	    try{
		_link.add_link("link5", _lookup, FollowOption.if_no_local, FollowOption.if_no_local);
		_link.modify_link("link5", FollowOption.local_only, FollowOption.local_only);
	    }catch(Exception e){
		e.printStackTrace();
	    }
	    

	    System.out.println("\n********** Testing link modification **********");
	    
	    LinkInfo _info = _link.describe_link("link5");
	    if (_info.def_pass_on_follow_rule.value() == FollowOption.local_only.value() && 
		_info.limiting_follow_rule.value() == FollowOption.local_only.value()){
		System.out.println("Test passed");
		_correct_results++;
	    }
	    else{
		System.out.println("Test failed");
		_incorrect_results++;
	    }

	    System.out.println("\n********** Testing link removing **********");
	    // test remove_link
	     try{
		_link.add_link("link6", _lookup, FollowOption.if_no_local, FollowOption.if_no_local);
		_link.remove_link("link");
	     }catch(Exception e){
		 e.printStackTrace();
	     }
	    
	     String[] _links_names = _link.list_links();
	     boolean _failed = false;

	     for (int i = 0; i < _links_names.length; i++){
		 if (_links_names[i].equals("link"))
		     _failed = true;
	     }

	    if (! _failed){
		    System.out.println("Test passed");
		    _correct_results++;
		}
		else{
		    System.out.println("Test failed");
		    _incorrect_results++;
		}
	}
	catch (Exception e){
	    e.printStackTrace();
	}
	System.out.println("\n********** Testing finished **********");
	System.out.println("Total tests: " + (_correct_results + _incorrect_results));
	System.out.println("Correct results: " + _correct_results);
	System.out.println("Incorrect results: " + _incorrect_results);

	System.exit(0);
    }    
} // ExceptionTest








