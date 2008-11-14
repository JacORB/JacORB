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

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class NSPrefsDlg
extends JDialog
implements ActionListener, KeyListener
{
	JTextField editSeconds;
	boolean isOk;
	public int updateInterval;

	public NSPrefsDlg(Frame frame, int updInt)
	{
		super(frame,"Preferences",true);
		isOk=false;
		JPanel mainPanel=new JPanel(new GridLayout(2,1));
		getContentPane().add(mainPanel);
		JPanel hiPanel=new JPanel(new FlowLayout());
		JPanel loPanel=new JPanel();
		mainPanel.add(hiPanel);
		mainPanel.add(loPanel);

		JLabel label1=new JLabel("Update view after ");

		Integer upd=new Integer(updInt);
		editSeconds=new JTextField(upd.toString(),3);
		JLabel label2=new JLabel("seconds ");
		hiPanel.add(label1); hiPanel.add(editSeconds); hiPanel.add(label2);

		JButton ok=new JButton("Ok");
		JButton cancel=new JButton("Cancel");
		loPanel.add(ok); loPanel.add(cancel);
		ok.addActionListener(this);
		cancel.addActionListener(this);
		editSeconds.addKeyListener(this);
	}
	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand().equals("Ok")) 
		{
			try
			{
				updateInterval=Integer.parseInt(editSeconds.getText());
				isOk=true; dispose();
			} catch (Exception ex)
			{
				JOptionPane.showMessageDialog(this,"Wrong number format",
					"Input error", JOptionPane.ERROR_MESSAGE);
				editSeconds.grabFocus(); editSeconds.selectAll();
			}
		}
		else dispose();
	}
	public void keyPressed(KeyEvent e) 
	{
		if (e.getKeyCode()==KeyEvent.VK_ENTER) 
			actionPerformed(new ActionEvent(this,0,"Ok"));
		else if (e.getKeyCode()==KeyEvent.VK_ESCAPE) 
			actionPerformed(new ActionEvent(this,0,"Cancel"));
	}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
}








