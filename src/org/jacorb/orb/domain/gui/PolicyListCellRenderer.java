package org.jacorb.orb.domain.gui;

import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.*;
import java.awt.Component;
import org.jacorb.util.Debug;
import org.jacorb.orb.domain.*;

/**
 * This class renders nodes of the policy list.
 * @author Herbert Kiefer
 * @version $Revision$
 */


public class PolicyListCellRenderer 
    extends DefaultTreeCellRenderer 
{
    private Icon metaPolicyIcon;
    public PolicyListCellRenderer() 
    {
        // the file MetaPolicy.gif must reside in 
        // CLASSPATH/jacorb/orb/domain/gui/MetaPolicy.gif
        java.net.URL imageURL = 
            ClassLoader.getSystemResource("jacorb/orb/domain/gui/MetaPolicy.gif");
        // if (imageURL != null)
        //  Debug.output(Debug.DOMAIN | Debug.INFORMATION, imageURL.toString());
        // else Debug.output(Debug.DOMAIN | Debug.INFORMATION, "image URL is null");


        // metaPolicyIcon= new ImageIcon("jacorb/orb/domain/gui/MetaPolicy.gif", 
        //			  "icon for meta policy");
        if (imageURL != null)
            metaPolicyIcon= new ImageIcon(imageURL, "icon for meta policy");
    
    }

    /** 
     * returns the object used to draw a tree "cell". this overriden version
     *  sets the tooltip text to the policy type.
     */

    public Component getTreeCellRendererComponent(JTree tree, 
                                                  Object node,
                                                  boolean sel,
                                                  boolean exp,
                                                  boolean leaf, 
                                                  int row,
                                                  boolean hasFocus)
    {
        try 
        {	   
            PolicyListLeafNode listNode= (PolicyListLeafNode) node;
            // org.jacorb.util.Debug.output(2, "PolicyListCellRenderer");
            // setToolTipText("Type " + Integer.toString( listNode.getPolicy().policy_type() ));
            setToolTipText("Type " + listNode.cachedPolicyType);
            // testing
            org.omg.CORBA.Policy pol= listNode.getPolicy();
            try
            {
                MetaPolicy meta = MetaPolicyHelper.narrow(pol);
                setLeafIcon( metaPolicyIcon );
            }
            catch( org.omg.CORBA.BAD_PARAM bp )
            {
                setLeafIcon(getDefaultLeafIcon() );
            }					       
        }
        catch (org.omg.CORBA.COMM_FAILURE fail) {}
        catch (java.lang.ClassCastException e) 
        {
            // do not add tool tips for invisible PolicyListRootNode 
            // omit also tool tips for DefaultTreeNode's "shown" at startup
            // Debug.output(Debug.DOMAIN | 2, e.toString());
        }
        // org.jacorb.util.Debug.output(2, "DomainTreeCellRenderer");
        return super.getTreeCellRendererComponent(tree, node, sel, 
                                                  exp, leaf,
                                                  row, hasFocus);
    }

} // PolicyListCellRenderer






