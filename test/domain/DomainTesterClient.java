package org.jacorb.orb.domain.test;

import org.jacorb.orb.domain.*;
import org.omg.CosNaming.*;
import org.jacorb.util.*;
import java.io.*;

/**
 * A test client for domains.
 * This program browses a domain and can call most of the operations a domain offers. The
 * operation of a domain to be called can be selected interactively via a text menu.
 * <br>
 * The domain to be tested can be retrieved the following ways. Firstly, if no command line
 * arguments are given, the program contacts the name service and tries to retrieve a domain
 * object named "Domain.service". Secondly, a IOR of a domain can be specified via command
 * line arguments. If the first command line argument reads "-f" then the next command line
 * argument is assumed to be the file name containing the IOR. Otherwise the first command line * argument is assumed to BE the IOR.
 *
 * Created: Tue Jan  4 2000
 *
 * @author Herbert Kiefer
 * @version $Revision$
 */
public class DomainTesterClient  {

 
 public static void main( String[] args ) 
  {
    try 
      {
	Util.FileEchoOn("fileOutput");
	new DomainTesterClient(null, args);
	Util.FileEchoOff();

      }
    catch (Exception e)
      {
	e.printStackTrace();
	Util.FileEchoOff();
      }
  } // main

  // dom is domain to test
  DomainTesterClient(Domain dom, String[] args) {

    int n= 10;
    Domain dm[]= new Domain[n];
    Domain domain= null ;
    Domain[] dmList;
    TestPolicy pol[]= new TestPolicy[n];

    int i;
    String submenu[]= null;
    org.omg.CORBA.Policy policy=     null;
    TestPolicy           testPolicy= null;
    String [] lines={
      "insert member", 
      "delete member",
      "has    member",
      "list   member", 
      "-------------", 
      "get         policy", 
      "set         policy", 
      "       <empty>    ",
      "       <empty>",
      "-----------------------", 
      "create domain                ",
      "get domains from name service",
      "-----------------------------",
      "insert child domain   ", 
      "delete child domain   ",
      "has    child domain   ",
      "list   child domains  ", 
      "goto   child domain...",
      "-----------------------",
      "insert parent domain   ",
      "delete parent domain   ",
      "has    parent domain   ",
      "list   parent domains  ",
      "goto   parent domain...",
      "-----------------------", 
      "get policy         ", 
      "get domain managerns", 
      "-------------------", 
      "check if is root ",
      "get root domain  ",
      "list slot content",
      "finish"
      //  "---------------------",
      // "has indirect member  ",
      // "list indirect members"
    };
    int count= 0;
    org.omg.CORBA.ORB orb= null;
    NamingContextExt nc= null;
  
    try
      { // get name service
	orb = org.omg.CORBA.ORB.init(args,null);
	nc = NamingContextExtHelper.narrow
	  (orb.resolve_initial_references("NameService"));

	
	if (dom == null) 
	  { // get domain service
	    if ( args.length > 0 )
	      { // take domain reference from args

		String iorString= null;

		if( args[0].equals("-f"))
		  { // read IOR of domain from file
		    String line= null;
		    try
		      {
			// System.out.println ( "arg.length: " + arg.length );
			// System.out.println ( "arg[ 0 ]: " + arg[ 0 ] );
			// System.out.println ( "reading IOR from file: " + arg[ 1 ] );
			BufferedReader br = new BufferedReader (new FileReader(args[1]),2048 );
			line = br.readLine();
			//		System.out.print ( line );
			if ( line != null ) 
			  { 
			    iorString = line;
			    while ( line != null ) 
			    { 
			      line = br.readLine();
			      if ( line != null ) iorString = iorString + line;
			      // System.out.print ( line );
			    }
			  }
			// System.out.println ( "red IOR from file:" );
		      } 
		    catch ( IOException ioe )
		      {
			ioe.printStackTrace();
			// System.exit(1);
		      }
		  }
		else
		  {
		    iorString = args[0];
		  }
		dom = DomainHelper.narrow(orb.string_to_object(iorString));
	      }
	    // take domain reference from name server
	    else try { dom= DomainHelper.narrow(nc.resolve(nc.to_name("Domain.service")));}
	         catch (org.omg.CosNaming.NamingContextPackage.NotFound e) 
		   {
		     org.jacorb.util.Debug.output(1, "domain server not found at name server, "
					      +" possibly not running");
		     System.exit(-1);
	      }
	  } // else use given one
	else System.out.println("observing domain " + dom.name() );

	// get test policies

	for (i= 0; i < n; i++) 
	  { 
	    try { 
	      pol[i]= TestPolicyHelper.narrow(nc.resolve(nc.to_name("TestPolicy"+i+".whatever")));
	    }  catch (org.omg.CosNaming.NamingContextPackage.NotFound e) 
	      {
		org.jacorb.util.Debug.output(1, "Testpolicy "+ i + " not found at name server");
		continue;
	      }
	    // System.out.println("got " + pol[i].description());
	  }

	} catch (Exception e) {		
	  Debug.output(1,e);
	}

    // get test domains 
    for (i= 0; i < n; i++) 
	  {
	    try 
	      {
		domain= DomainHelper.narrow( nc.resolve(nc.to_name("Domain"+i+".whatever") ) );
	      }
	    catch (org.omg.CosNaming.NamingContextPackage.NotFound e) 
	      {
		// org.jacorb.util.Debug.output(1, "domain "+ i + " not found at name server");
		continue;
	      }
	    catch (Exception e) { e.printStackTrace(); }
	    dm[i]= domain;
	  }

    org.omg.CORBA.Object[] members= null;
    // main loop
    boolean end= false;
    int j, answer;

  try {
    while (!end)
     
      {
	System.out.println("current: "+ dom.name());
	answer= Util.textmenu(lines);
	System.out.println(" choice was " + answer);
	switch (answer) {

	case 1: // insert member
	  System.out.print("insert which member ? ");
	  dom.insertMember(pol[Util.readInt()]); 
	  break;

	case 2: // delete member
	  System.out.print("choose member to delete from list:  ");
	  members= dom.getMembers();
	  if (members.length == 0) break;
	  Util.quicksort(0, members.length-1, members);

	  submenu= new String[members.length];
	  for (i= 0; i < members.length; i++) 
	    submenu[i]= Util.downcast(members[i]);
	  answer= Util.textmenu(submenu);
	  System.out.println(" choice was " + answer);

	  dom.deleteMember(members[answer-1]); 
	  break;

	case 3: // has member
	  System.out.print("check which member ? ");
	  if ( dom.hasMember(pol[Util.readInt()]) ) System.out.println("yes");
	  else System.out.println("no");
	  break;
	

	case 4: // list members
	 members= dom.getMembers();
	  Util.quicksort(0, members.length-1, members);
	  System.out.println(members.length + " members:");
	  for (i= 0; i < members.length; i++) 
	    {
	      System.out.println("\t" + Util.downcast(members[i]));
	      // System.out.println("\t an " + Util.toID(members[i].toString()) + " object");

	    }
	  break;

	case 5: // get policy
	  System.out.println("get which type ?:");
	  try 
	    {
	      policy= dom.get_domain_policy( Util.readInt() );
	    }
	  catch (org.omg.CORBA.INV_POLICY inv) 
	    {
	      System.out.println("this domain has no policy of this type");
	      break;
	    }
	      
	  testPolicy= TestPolicyHelper.narrow(policy);
	  if (testPolicy != null)
	    System.out.println("returned policy is " + testPolicy._toString());
	  else // cast failed
	    {
	      System.out.println("returned policy is an " + Util.toID(policy.toString()));

	    }
	  break;

	case 6: // set policy
	  System.out.println("set which type (only slot#) ?:");
	  dom.set_domain_policy(pol[Util.readInt()]);
	  break;

	case 7: // get default policy type (deleted)
	  break;

	case 8: // set default policy type (deleted)
	  break;

	case 9: // create domain
	  try {
	  System.out.print("into which slot ? :");
	  // for (i= 0; i < n; i++) System.out.println(pol[i].description());

	  dm[j= Util.readInt()]= dom.createDomain
	    (pol, pol, "dm # "+j);
	  // dom.insertChild(dm[j]);
	  System.out.print("register at name service ? (y/n) ");
	  if (Util.readChar() != 'n') 
	    {
	      nc.bind(nc.to_name("Domain"+j+".whatever"), dm[j]); 
	    }
	  }
	  catch (Exception e) { e.printStackTrace(); }
	  break;

	case 10: // get domains from name service
	  for (i= 0; i < n; i++) {
	    try { dm[i]= DomainHelper.narrow(nc.resolve(nc.to_name("Domain"+i+"whatever"))); }
	    catch (org.omg.CosNaming.NamingContextPackage.NotFound e) {
	      System.out.println("dm # "+i+" not found at name service");
	    }
	  }
	  break;
	  
	case 11: //  insert child domain 
	  System.out.print("take child domain from which slot ?");
	  dom.insertChild( dm[Util.readInt()] );
	  break;

	case 12: // delete child domain
	  dmList= dom.getChilds();
	  if (dmList.length == 0) break;
	  Util.quicksort(0, dmList.length-1, dmList);

	  System.out.print("choose child domain to delete from list:  ");

	  submenu= new String[dmList.length];
	  for (i= 0; i < dmList.length; i++) 
	    submenu[i]= Util.downcast(dmList[i]);
	  answer= Util.textmenu(submenu);
	  System.out.println(" choice was " + answer);

	  dom.deleteChild(dmList[answer-1]); 
	  // old version
	  // System.out.println("which one ? (slot#): ");
	  // dom.deleteChild(dm[Util.readInt()]);
	  break;

	case 13: // has child domain
	  System.out.println("check which domain (slot#) ? ");
	  if ( dom.hasChild(dm[Util.readInt()]) ) System.out.println("yes");
	  else System.out.println("no");
	  break;

	case 14: // list child domains
	  dmList= dom.getChilds();

	  // System.out.println("list before sort:");
	  // for (i= 0; i < dmList.length; i++) 
	  // System.out.println(dmList[i].name());

	  Util.quicksort(0, dmList.length-1, dmList);
	  System.out.println(dmList.length + " child domains:");
	  for (i= 0; i < dmList.length; i++) 
	    System.out.println("\t" + dmList[i].name());
	  break;

	case 15: // goto child domain
	  dmList= dom.getChilds();
	  if (dmList.length == 0) break;
	  Util.quicksort(0, dmList.length-1, dmList);
	  submenu= new String[dmList.length];
	  for (i= 0; i < dmList.length; i++) 
	    submenu[i]= dmList[i].name();
	  answer= Util.textmenu(submenu);
	  System.out.println(" choice was " + answer+ " which is " + dmList[answer-1].name());
	  new DomainTesterClient(dmList[answer-1], args);
	  break;

	case 16: // insert parent domain
	  System.out.print("insert which parent (slot # from 1 to 9) ? : ");
	  dom.insertParent(dm[Util.readInt()]);
	  break;

	case 17: // delete parent domain
	  System.out.print("delete which parent (slot # from 1 to 9) ? : ");
	  dom.deleteParent(dm[Util.readInt()]);
	  break;

	case 18: // has parent domain
	  System.out.println("check which domain (slot#) ? ");
	  if ( dom.hasParent(dm[Util.readInt()]) ) System.out.println("yes");
	  else System.out.println("no");
	  break;

	case 19: // list parent domains
	  Domain[] domainList= dom.getParents();
	  Util.quicksort(0, domainList.length-1, domainList);
	  System.out.println(domainList.length + " parent domains:");
	  for (i= 0; i < domainList.length; i++) 
	    System.out.println("\t" + domainList[i].name());
	  break;

	case 20: // goto parent domain...
	  dmList= dom.getParents();
	  if (dmList.length == 0) break;
	  Util.quicksort(0, dmList.length-1, dmList);
	  submenu= new String[dmList.length];
	  for (i= 0; i < dmList.length; i++) 
	    submenu[i]= dmList[i].name();
	  answer= Util.textmenu(submenu);
	  System.out.println(" choice was " + answer+ " which is " + dmList[answer-1].name());
	  new DomainTesterClient(dmList[answer-1], args);
	  break; 

	case 21: // get policy
	  System.out.println("of which object ?: ");
	  i= Util.readInt();
	  System.out.println
	    ("policy is :"+TestPolicyHelper.narrow
	     (dom.getPolicy(pol[i],i))._toString() );
	  break;

	case 22: // get domains
	  System.out.println("of which object ?: ");
	  dmList= dom.getDomains(pol[Util.readInt()]);
	  System.out.println(dmList.length + " domains:");
	  Util.quicksort(0, dmList.length-1, dmList);
	  for (i= 0; i < dmList.length; i++) 
	    System.out.println("\t" + dmList[i].name());
	  break;

	case 23: // check if is root 
	   if ( dom.isRoot() ) 
	    System.out.println("domains "+ dom.name() + "is a root domain");	  
	   else   System.out.println("domains "+ dom.name() + " is NOT a root domain");	
	  break;

	//  case 24: // get root domain 
//  	  domain= dom.getRootDomain();
//  	  System.out.print("the root is " + domain.name() + ". GOTO ? ");
//  	  if (Util.readChar() == 'y') 
//  	    new DomainTesterClient(domain, args);
//  	  break;
	

	case 25: // list slot contents
	  System.out.println("\t slot # \t name");
	  for (i= 0; i < n; i++) 
	    {
	      System.out.print("\t " + i + "\t");
	      if (dm[i] != null)
		System.out.println(dm[i].name());
	      else System.out.println("is null");

	    }
	    

	  break;

	case 26: 
	  end= true; 
	  break;

//  	case 27: // has indirect member 
//  	  System.out.print("check which member ? ");
//  	  if ( dom.hasIndirectMember(pol[Util.readInt()]) ) System.out.println("yes");
//  	  else System.out.println("no");
//  	  break;

//  	case 28: // list indirect members
//  	  org.omg.CORBA.Object[] Imembers= dom.getIndirectMembers();
//  	  Util.quicksort(0, Imembers.length-1, Imembers);
//  	  System.out.println(Imembers.length + " indirect members:");
//  	  for (i= 0; i < Imembers.length; i++) 
//  	    System.out.println("\t" + Util.downcast(Imembers[i]));
//  	  break;
	}
      }
  } 
  catch (Exception e) 
    { 
      e.printStackTrace(); 
    }
  }
} // DomainTester







