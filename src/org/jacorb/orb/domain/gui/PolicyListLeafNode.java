package org.jacorb.orb.domain.gui;

import javax.swing.tree.DefaultMutableTreeNode;

import org.jacorb.orb.domain.*;
import org.omg.CORBA.Object;

/**
 * Objects of his class represents a domain policy.
 * @author Herbert Kiefer
 * @version 1.0
 */
public class PolicyListLeafNode 
    extends DefaultMutableTreeNode
{
    /** parent of the policy represented by this node */
    private Domain theParent;

    /** the policy represented by this node */
    private org.omg.CORBA.Policy thePolicy;

    /** the cached policy type of the policy */
    public String cachedPolicyType;

    public PolicyListLeafNode(org.omg.CORBA.Policy pol, 
                              Domain parent, 
                              String name )
    {
        super();
        theParent= parent;
        thePolicy= pol;
        try 
        { 
            cachedPolicyType = Integer.toString ( thePolicy.policy_type() ); 
        }
        catch (org.omg.CORBA.COMM_FAILURE fail) 
        { 
            cachedPolicyType= "???"; 
        }
        if ( name == null )
            name= "???";
        this.setUserObject(name);
    } // constructor


    /** returns the name of this node which is the name of the policy. */
    public String toString()
    {
        return (String)this.getUserObject();
    } // toString

    /** returns true. */
    public boolean isLeaf()
    {
        return true;
    }
  
    /** returns the policy this node represents. */
    public org.omg.CORBA.Policy getPolicy()
    {
        return thePolicy;
    }

} // PolicyListLeafNode



