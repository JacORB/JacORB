/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.jacorb.naming.namemanager;

import java.awt.event.*;
import java.awt.*;
import javax.swing.tree.*;
import javax.swing.*;

import org.jacorb.naming.Name;
import org.omg.CosNaming.*;

/**
 * This class handles the events on the tree
 *
 * @author Gerald Brose, FU Berlin
 * @version $Id$ 
 */

public class Handler
    extends WindowAdapter
    implements ActionListener, MouseListener, KeyListener
{
    int updateInterval;
    NSTree tree;
    Component frame;
    JDialog dlg;
    JTextField editName;
    JPopupMenu popup;
    Updater updater;

    public Handler(Component fr,NSTree tr) 
    { 
	frame = fr;
	tree = tr;
		
	popup = new JPopupMenu();
	JMenuItem bindContext = new JMenuItem("BindNewContext");
	JMenuItem bindObject = new JMenuItem("Bind Object");
	JMenuItem unbind=new JMenuItem("Unbind name");

	popup.add(bindContext);
	popup.add(bindObject);
	popup.add(unbind);
		
	bindContext.addActionListener(this);
	bindObject.addActionListener(this);
	unbind.addActionListener(this);

	updateInterval = 10;		
	updater = new Updater( tree, updateInterval);
	updater.start();
    }

    public void actionPerformed(ActionEvent e)
    {
	if (e.getActionCommand().equals("Quit"))
	{
	    System.exit(0);
	}
	else if (e.getActionCommand().equals("Unbind name")) 
	{
	    tree.unbind();
	}
	else if (e.getActionCommand().equals("Bind Object")) 
	{
	    ObjectDialog dialog = new ObjectDialog((Frame)frame);
	    //dialog.pack(); 
	    //dialog.show();

	    if (dialog.isOk)
            {
                try
                {
                    tree.bindObject( dialog.getName(), dialog.getIOR(), dialog.isRebind());
                }               
                catch ( org.omg.CORBA.UserException ue )
                {
                    JOptionPane.showMessageDialog( frame, 
                                                   ue.getClass().getName() + 
                                                  (ue.getMessage() != null ? (":" + ue.getMessage()):""),
                                                  "Exception", 
                                                   JOptionPane.INFORMATION_MESSAGE);
                }
            }
	}
	else if (e.getActionCommand().equals("BindNewContext")) 
	{
	    try
	    {
                String contextName = 
                    JOptionPane.showInputDialog(frame,
                                                "Name of the new context", 
                                                "BindNewContext",
                                                JOptionPane.QUESTION_MESSAGE);

                // check if user input is okay or if CANCEL was hit
                if (contextName != null && contextName.length() > 0)
                    tree.bind(contextName);
	    } 
	    catch ( org.omg.CORBA.UserException ue )
	    {
		JOptionPane.showMessageDialog(frame, 
                                              ue.getClass().getName() + 
					      (ue.getMessage() != null ? (":" + ue.getMessage()):""),
					      "Exception",
                                              JOptionPane.INFORMATION_MESSAGE);
	    }
	}
	else if (e.getActionCommand().equals("About..."))
	{
	    JOptionPane.showMessageDialog(frame,
					  "JacORB NameManager 1.2\n(C) 1998-2004 Gerald Brose, Wei-ju Wu & Volker Siegel\nFreie Universitaet Berlin",
					  "About",
                                          JOptionPane.INFORMATION_MESSAGE);
	}
	else if (e.getActionCommand().equals("Options"))
	{
	    NSPrefsDlg dlg = new NSPrefsDlg((Frame) frame, updateInterval);
	    dlg.pack(); 
	    dlg.show();
	    if (dlg.isOk)
		updater.setSeconds(dlg.updateInterval);
	}
	else 
	    throw new RuntimeException("Should not happen");
    }

    /**
     * @param k java.awt.event.KeyEvent
     */
	
    public void keyPressed(KeyEvent k) {}

    /**
     * @param k java.awt.event.KeyEvent
     */

    public void keyReleased(KeyEvent k) 
    {
	if( k.getKeyCode() == KeyEvent.VK_DELETE )
	    tree.unbind();
    }

    public void keyTyped(KeyEvent k) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}

    /**
     * opens pop-up menu or displays context node content
     */

    public void mouseReleased(MouseEvent e)
    {
	// on Solaris, the right mouse button somehow seems not be a popup trigger, so we
        // accept mouse 3 explicitly
	if (e.isPopupTrigger() || e.getModifiers() == java.awt.event.InputEvent.BUTTON3_MASK )
	{
	    popup.pack();
	    popup.show(tree, e.getX(), e.getY());
	}
	else
	{
	    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
	    if (path != null)
	    {
		DefaultMutableTreeNode node=(DefaultMutableTreeNode)path.getPathComponent(path.getPathCount()-1);
		((ContextNode)node.getUserObject()).display();
	    }
	}
    }

    // WindowListener
    public void windowClosing(WindowEvent e) 
    { 
	System.exit(0); 
    }
}

