package org.jacorb.orb.domain.gui;

import javax.swing.tree.DefaultMutableTreeNode;

import org.jacorb.orb.domain.*;
import org.omg.CORBA.Object;

/**
 * A MemberListLeafNode object represents a member of a domain.
 * It is displayed as an icon.
 * @author Herbert Kiefer
 * @version 1.0
 */
public class MemberListLeafNode extends DefaultMutableTreeNode
{
  /** parent of the object represented by this node */
  private Domain theParent;

  /** the object represented by this node */
  private org.omg.CORBA.Object theObject;

  public MemberListLeafNode(org.omg.CORBA.Object obj, Domain parent, 
			    String name)
  {
    super();
    theParent= parent;
    theObject= obj;
    if ( name == null )
      name= theParent.getNameOf(theObject);
    this.setUserObject(name);
  } // constructor

  /** returns the object this node represents */
  public org.omg.CORBA.Object getObject()
  {
    return theObject;
  }

  /** returns the name of this node which is the name of the domain member. */
  public String toString()
  {
    return (String) this.getUserObject();
  } // toString

  public void setName(String name)
  {
    this.setUserObject(name);
  }
  /** returns true. */
  public boolean isLeaf()
  {
    return true;
  }


} // MemberListLeafNode












