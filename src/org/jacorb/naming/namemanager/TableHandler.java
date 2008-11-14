/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

/**
 *	This class handles the events on the table
 */

public class TableHandler
    implements MouseListener, ActionListener, KeyListener
{
    Component frame;
    JTextField editName;
    JPopupMenu popup;
    NSTable table;
 
    public TableHandler(Component fr, NSTable t) 
	{ 
	    frame=fr; 
	    table = t;
	    popup=new JPopupMenu();
	    JMenuItem unbind=new JMenuItem("Unbind name");
	    JMenuItem call=new JMenuItem("Call Object...");

	    // until DII client is incorporated...
	    call.setEnabled(false);

	    popup.add(unbind);
	    popup.add(call);
		
	    unbind.addActionListener(this);
	    call.addActionListener(this);
	}
    /**
     * 
     * @param e java.awt.event.ActionEvent
     */
    public void actionPerformed(ActionEvent e) 
	{
	    if (e.getActionCommand().equals("Unbind name")) 
	    {
		table.unbind();
	    }
	    else if (e.getActionCommand().equals("Call Object...")) 
	    {
		// to come: DII pop-up
		;
	    }
	    else
		throw new RuntimeException("sollte nicht auftreten");
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
		table.unbind();
	}

    public void keyTyped(KeyEvent k) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    // MouseListener
    public void mousePressed(MouseEvent e) {}

    // Open context menu

    public void mouseReleased(MouseEvent e)
    {
	if (e.isPopupTrigger() || e.getModifiers() == java.awt.event.InputEvent.BUTTON3_MASK )
	{
	    popup.pack();
	    popup.show(table, e.getX(), e.getY());
	}
    }

    // WindowListener
    public void windowClosing(WindowEvent e) 
    { 
	System.exit(0); 
    }
}









