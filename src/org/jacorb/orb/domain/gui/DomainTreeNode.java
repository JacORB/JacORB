package org.jacorb.orb.domain.gui;

import javax.swing.tree.DefaultMutableTreeNode;

import org.jacorb.orb.domain.Domain;
import org.jacorb.orb.domain.Util;
import org.omg.CORBA.Object;

/**
 * A DomainTreeNode represents a domain.
 * This class inherits from DefaultMutableTreeNode and overrides the
 * method toString. It holds the domain which represents a tree node. The tree
 * node user object caches the name of the domain. The name of the doamin is
 * equivalent to domain.name. I
 * @author Herbert Kiefer
 * @version 1.0
 */
public class DomainTreeNode extends DefaultMutableTreeNode
{
  /** a value of true indicates, that thze view this node needs an update
   *  public for happy hacking :)
   */
  public boolean viewNeedsUpdate;

  /** the domain this node represents */
  private Domain theDomain;

  public DomainTreeNode()
  {
    super();
    viewNeedsUpdate= true;
  }

  public DomainTreeNode(Domain domain)
  {
    super();
    try 
      {
	setUserObject(domain.name()); // set user object of this node 
      }
    catch (org.omg.CORBA.COMM_FAILURE fail) { setUserObject("???"); }

    theDomain= domain;
    org.jacorb.util.Debug.myAssert(2, domain != null, "domain is null");

    //    try { cachedName= domain.name(); }
    // catch (org.omg.CORBA.COMM_FAILURE fail) { cachedName= "???"; }
      
    viewNeedsUpdate= true;
  }

  /** returns the user object name. It assumes that user objects are
   * of type Domain or are org.omg.Objects. If the user object is of type
   * Domain it returns the domains name. It it's a CORBA object, this
   * method returns the IDL type (id) of the CORBA object.
   */

 //   public String toString()
//    {

//      org.jacorb.util.Debug.myAssert(1, theDomain != null, " domain representing "
//        +"tree node is null ");
//      return cachedName;
//    } // toString

  /** returns false. Because a domain tree node always contains a domain as
   * user object all a node is not a leaf.
   */
  public boolean isLeaf()
  {
    // return getDomain().getChilds().length == 0;
    return false;
  }

  /** gets the domain used in this node. The domain ist the user object and
   * is alwys of type org.jacorb.orb.domain.Domain.
   */
  public Domain getDomain()
  {
    org.jacorb.util.Debug.myAssert(1, theDomain != null, " domain representing "
      +"tree node is null ");
    return theDomain;
  }
}














