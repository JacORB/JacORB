package org.jacorb.orb.domain.gui;

import javax.swing.tree.DefaultMutableTreeNode;

import java.util.Hashtable;
import org.jacorb.orb.domain.*;
import org.omg.CORBA.Object;

/**
 * An  object of this  class represents the  root node of a  tree. The
 * tree is  designed to represent a list of  domain policies. The root
 *  holds  the domain  of  all  the policies.  The  root  node is  not
 *  displayed, only  the policies  of  the domain  are displayed.  The
 * policies are inserted as children into this root node.  They are of
 * type  PolicyListLeafNode and represent  a policy object.  All child
 * nodes are  leaf nodes.  It is used instead of  a JList because it's
 * easier to manipulate and edit than a JList.

 * @author Herbert Kiefer
 * @version 1.0 
 */

public class PolicyListRootNode 
    extends DefaultMutableTreeNode
{
    /** the domain which is the parent of all the children */
    private Domain theDomain;

    /** a cache of the names of the policies */
    private String theValidNames[];

    /** maps from the policy name to the policy object */
    private Hashtable name2policy;

    /**
     *  constructs a flat list of policies.
     *  The list members are the policies of the domain and are retrieved
     *  via domain.getPolicies()
     *  and inserted as children into this node
     *  @param domain the domain this node represents
     *  @see Domain#getPolicies
     */
    public PolicyListRootNode(Domain domain)
    {
        super(domain.name());
        update(domain);
    }

    /** 
     * updates the policy list contents with the policies  of the domain. 
     */
    public void update(Domain domain)
    {
        theDomain= domain;
        org.jacorb.util.Debug.assert(2, domain != null, "domain is null");

        org.omg.CORBA.Policy pol[]= domain.getPolicies();
        // theValidNames= new String[pol.length];
        // name2policy= new Hashtable(pol.length);

        //  for (int i= 0; i < pol.length; i++)
        //        {
        //  	theValidNames[i]= Util.getNamedKeyOfPolicy( pol [i] );
        //  	name2policy.put(theValidNames[i], pol[i]);
        //        }

        Util.quicksort(0, pol.length - 1, pol);
        // insert in sort order
        for (int i= 0; i < pol.length; i++)
            this.add( new PolicyListLeafNode(pol[i], 
                                             domain, 
                                             Util.getNameOfPolicy(pol[i]) ));

    } // update

    /** returns the name of this node which is equivalent to
     *  the domain name of the domain this node represents
     */
    //  public String toString()
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

} // PolicyListRootNode








