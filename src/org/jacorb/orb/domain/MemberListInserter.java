package org.jacorb.orb.domain;

import org.jacorb.util.Debug;

/**
 * A thread for inserting domain members at domain creation.
 * This class is a workaround for the following problem:
 * When a domain is created, an initial member list can optionally be specified.
 * This members have to be inserted in the context of the DomainImpl constructor.
 * In this insertion process the _this() function is called but this fails because
 * the consctruction call is still in process and the servant is not activated.
 * </p>
 * The solution to this proplem is to insert the members in the domain asynchronously.
 * This is the task of an instance of an MemberListInserter.
 *
 * Created: Fri Aug 11 13:05:39 2000
 *
 * @author Herbert Kiefer
 * @version $Revision$
 */

public class MemberListInserter extends java.lang.Thread 
{
  DomainImpl _target;
  org.omg.CORBA.Object _memberlist[];

  public MemberListInserter(DomainImpl target, org.omg.CORBA.Object memberlist[]) 
  {
    _target=target;
    _memberlist= memberlist;
  }

  public void run()
  {
    Domain domain= null;

    boolean available= false;
    while ( ! available )
      {
	try 
	  {
	    domain= _target._this();
	    available= true;
	  }
	catch (org.omg.CORBA.BAD_INV_ORDER badOrder)
	  { 
	    try 
	      {
		Debug.output(Debug.DOMAIN | Debug.INFORMATION, "MemberListInserter.run: "
			     +"catched a " +badOrder.toString() + ", sleeping 10 ms ..." );
		sleep(10); // wait until resource becomes available
	      }
	    catch (Exception e)
	      {
		Debug.output(Debug.DOMAIN | Debug.IMPORTANT, e);
	      }
	  }
      } // while

    // now domain is valid, insertion possible
    Debug.output(Debug.DOMAIN | Debug.INFORMATION, "MemberListInserter.run: "
		 +"now _this() is available, do the work.");

    for (int i= 0 ; i < _memberlist.length; i++)
      {
	//	Debug.output(Debug.DOMAIN | Debug.INFORMATION, "insert member "+ i +" of " 
	//     +   _memberlist.length);

	domain.insertMember( _memberlist[i] );
      }
    Debug.output(Debug.DOMAIN | Debug.DEBUG1, "memberlist insertion of " 
		 + _memberlist.length + " objects finished");
	 
    
  } // run
  
} // MemberListInserter
















