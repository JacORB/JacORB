package org.jacorb.orb.domain.gui;

import javax.swing.tree.DefaultMutableTreeNode;

import org.jacorb.orb.domain.*;
import org.jacorb.util.Debug;
import org.omg.CORBA.Object;

/**
 * Objects of this class represent the root node of a domain member tree.
 *  The tree is designed to
 * represent a list of domain members. The root holds the domain of all the
 * members. The root node is not displayed, only the members of the domain
 * are displayed. The members are inserted as children into this root node.
 * They are of type MemberListLeafNode and represent a member object. All
 * child nodes are leaf nodes.
 * It is used
 * instead of a JList because it's easier to manipulate and edit than a JList.
 * @author Herbert Kiefer
 * @version 1.0
 */
public class MemberListRootNode extends DefaultMutableTreeNode
{
  /** the domain which is the parent of all the children */
  private Domain theDomain;

  /** a cache of all the member names */
  private String theValidNames[];

  /** constructs a flat list of members.
   *  The list members are the members of the domain and are retrieved
   *  via domain.getMembers()
   *  and inserted as children into this node
   *  @param domain the domain this node represents
   *  @see Domain#getMembers
   */
 public MemberListRootNode(Domain domain)
  {
    super(domain.name());
    theDomain= domain;
    org.jacorb.util.Debug.assert(2, domain != null, "domain is null");

    org.omg.CORBA.Object member[]= domain.getMembers();
    theValidNames= new String[member.length];
    for (int i= 0; i < member.length; i++)
      {
	try 
	  {
	    theValidNames[i]= domain.getNameOf(member[i]);
	  
	    if ( theValidNames[i] == null || theValidNames[i].equals("") )
	      theValidNames[i]= "???";
	  }
	catch (org.omg.CORBA.COMM_FAILURE fail)
	  {
	    Debug.output(Debug.DOMAIN | 2, " MemberListRootNode.init: comm "
			 +"failure at call of <"+domain.name()+">.getNameOf()");
	      theValidNames[i]= "???";

	  }
      } // for
    Util.quicksort(0, member.length - 1, theValidNames);
    org.omg.CORBA.Object obj;
    for (int i= 0; i < member.length; i++)
      {
	obj= theDomain.resolveName(theValidNames[i]);
	if (obj == null)
          {
            // name is not valid, skip
            Debug.output( Debug.DOMAIN | 4, "name " + theValidNames[i] + "not valid");
            continue;
          }
        this.add(new MemberListLeafNode( obj, theDomain, theValidNames[i]) );
      }
  } // MemberListRootNode

  /** returns the name of this node which is equivalent to
   *  the domain name of the domain this node represents
   */
 //   public String toString()
//    {
//      org.jacorb.util.Debug.assert(1, theDomain != null, " domain representing "
//        +"tree node is null ");
//      return theDomain.name();
//    } // toString

  /** returns false. This node represents a domain
   */
  public boolean isLeaf()
  {
    return false;
  }


  /** gets the domain used in this node.
   */
  public Domain getDomain()
  {
    org.jacorb.util.Debug.assert(1, theDomain != null, " domain representing "
      +"tree node is null ");
    return theDomain;
  }

} // MemberListRootNode








