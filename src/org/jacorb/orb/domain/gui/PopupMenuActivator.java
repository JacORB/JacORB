package org.jacorb.orb.domain.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JTree;
import org.jacorb.util.Debug;
/**
 * Objects of this class manage popup menus.
 * This class implements the mouse listener interface. It brings up
 * a context specific popup menu. This context specific menu is set with
 * the constructor. Aditionally a list of menu items can be provided to the constructor.
 * This is the list of the so-called selection-dependent menu items. Items of this list
 * are enabled by an PopupMenuActivator iff the tree has a selection.
 * @author Herbert Kiefer
 * @version 1.0
 */

public class PopupMenuActivator extends MouseAdapter
{
  /** the popup menu for policy lists */
  private javax.swing.JPopupMenu thePopupMenu;

  /** the tree for which a popup menu is displayed.
   */
  private JTree theTree;
  
  /** the menu items which are enabled iff an item of the tree is selected. */
  private JMenuItem theSelectionDependantMenuItems[];

  /** the current enable state of the selection-dependant menu items. */
  private boolean theItemsEnabled= false;

  /** creates a popup menu activator. 
   *  @param menu the popup menu to display on demand
   *  @param tree the tree on which the popup menu shall appear
   */
  public PopupMenuActivator(JPopupMenu menu, JTree tree)
  { this(menu, tree, null); }

  /** creates a popup menu activator. 
   *  @param menu the popup menu to display on demand
   *  @param tree the tree on which the popup menu shall appear
   *  @param selectionDependantMenuItems a list of menu items which are enabled iff
   *         parameter tree has selected node(s).
   */
  public PopupMenuActivator(JPopupMenu menu, JTree tree, 
			    JMenuItem selectionDependantMenuItems[])
  {
    Debug.assert(1, menu != null, "PopupMenuActivator.init: menu is null");
    thePopupMenu= menu;

    Debug.assert(1, tree != null, "PopupMenuActivator.init: tree is null");
    theTree= tree;

    theSelectionDependantMenuItems= selectionDependantMenuItems;
    if (theSelectionDependantMenuItems != null)
      {
	for (int i= 0; i < theSelectionDependantMenuItems.length; i++)
	  theSelectionDependantMenuItems[i].setEnabled(true);
	
	theItemsEnabled= true;
      }
    
  } // constructor


    /**
     * Invoked when the mouse has been clicked on a component.
     */
    public void mouseClicked(MouseEvent e)
    {
      if (e.getClickCount() == 2 && theTree.getSelectionCount() > 0)
      { // if the user has double clicked a tree item,
        // then fire first menu item of popup menu
        JMenuItem item= (JMenuItem) thePopupMenu.getComponent(0);
        item.doClick();
      }
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     */
    public void mousePressed(MouseEvent e)
    { mayBeShowPopup(e); }

     /**
     * Invoked when a mouse button has been released on a component.
     */
    public void mouseReleased(MouseEvent e)
    { mayBeShowPopup(e); }

  private void mayBeShowPopup(MouseEvent e)
    {
      if (e.isPopupTrigger() )
	{
	  if (theTree.getSelectionCount() > 0) enableMenuItems();
	  else disableMenuItems();

	  thePopupMenu.show(e.getComponent(), e.getX(), e.getY() );
	}
    } // mayBeShowPopup

  /** enables the selection dependant menu items. */
  private void enableMenuItems()
  {
    if (theItemsEnabled) return; // if already enabled, skip
    
    if (theSelectionDependantMenuItems == null) return;

    for (int i= 0; i < theSelectionDependantMenuItems.length; i++)
      theSelectionDependantMenuItems[i].setEnabled(true);

    theItemsEnabled= true;
  } // enableMenuItems

  /** disables the selection dependant menu items. */
  private void disableMenuItems()
  {
    if (! theItemsEnabled) return; // if already disabled, skip

    if (theSelectionDependantMenuItems == null) return;

    for (int i= 0; i < theSelectionDependantMenuItems.length; i++)
      theSelectionDependantMenuItems[i].setEnabled(false);

    theItemsEnabled= false;

  } // enableMenuItems
}


