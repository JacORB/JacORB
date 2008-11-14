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

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class InfoDlg
extends JDialog
implements ActionListener
{
	public InfoDlg(Frame frame, String typeid, String objkey,
		String version, String host, String port)
	{
		super(frame,"Info");
		JPanel mainPanel=new JPanel();
		getContentPane().add(mainPanel);
		JPanel hiPanel=new JPanel(new GridLayout(3,1));
		JPanel midPanel=new JPanel(new GridLayout(3,1));
		JPanel loPanel=new JPanel();

		JLabel label;
		label=new JLabel(" TypeID: "+typeid+" "); hiPanel.add(label); 
		label=new JLabel(" Object Key: "+objkey+" "); hiPanel.add(label);
		label=new JLabel(" "); hiPanel.add(label);

		Border tmp=BorderFactory.createEtchedBorder();
		TitledBorder border=
			BorderFactory.createTitledBorder(tmp,"IIOP info");
		midPanel.setBorder(border);
		label=new JLabel(" Version: "+version); midPanel.add(label);
		label=new JLabel(" Host: "+host); midPanel.add(label);
		label=new JLabel(" Port: "+port); midPanel.add(label);

		JButton ok=new JButton("Ok");
		loPanel.add(ok);
		ok.addActionListener(this);

		// Jetzt die Panels richtig einfuegen
		GridBagLayout gridbag=new GridBagLayout();
		GridBagConstraints c=new GridBagConstraints();
		mainPanel.setLayout(gridbag);

		c.anchor=GridBagConstraints.EAST;
		c.fill=GridBagConstraints.BOTH;
		c.weightx=0.6; c.weighty=0.2;
		c.gridx=0; c.gridy=0; c.gridheight=3; c.gridwidth=1;

		gridbag.setConstraints(hiPanel,c);
		mainPanel.add(hiPanel);

		c.gridy=3; c.gridheight=3; c.gridwidth=2;
		gridbag.setConstraints(midPanel,c);
		mainPanel.add(midPanel);

		c.gridy=6; c.gridheight=1; c.gridwidth=2;
		gridbag.setConstraints(loPanel,c);
		mainPanel.add(loPanel);
	}
	public void actionPerformed(ActionEvent e) { dispose(); }
}








