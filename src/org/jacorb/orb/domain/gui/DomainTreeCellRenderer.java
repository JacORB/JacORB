package org.jacorb.orb.domain.gui;

import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.JTree;
import java.awt.Component;
/**
 * This class renders nodes of a domain tree.
 * @author Herbert Kiefer
 * @version 1.0
 */




public class DomainTreeCellRenderer extends DefaultTreeCellRenderer
{

  public DomainTreeCellRenderer()
  {
  }

/** returns the object used to draw a tree "cell". this overriden version
 *  sets the tooltip text.
 */
public Component getTreeCellRendererComponent(JTree tree, Object node,
            boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus)
  {
    DomainTreeNode domainNode= (DomainTreeNode) node;
    setToolTipText(domainNode.toString());
    // org.jacorb.util.Debug.output(2, "DomainTreeCellRenderer");
    return super.getTreeCellRendererComponent(tree, node, sel, exp, leaf, row, hasFocus);
  }
}






