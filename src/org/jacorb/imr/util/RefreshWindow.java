/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2002 Gerald Brose
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
 *
 */
package org.jacorb.imr.util;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
/**
 * This class shows a window which lets the user control
 * the behaviour of the refresh thread. It allows to change
 * the refresh interval and stop/restart the thread.
 *
 * @author Nicolas Noffke
 *
 * $Log$
 * Revision 1.4  2002/03/19 09:25:11  nicolas
 * updated copyright to 2002
 *
 * Revision 1.3  2002/03/19 11:08:02  brose
 * *** empty log message ***
 *
 * Revision 1.2  2002/03/17 18:44:02  brose
 * *** empty log message ***
 *
 * Revision 1.3  1999/11/25 16:05:49  brose
 * cosmetics
 *
 * Revision 1.2  1999/11/25 10:02:14  noffke
 * Wrote small comment.
 *
 *
 */

public class RefreshWindow  extends JFrame implements ActionListener{
    private JTextField m_interval_tf;
    private JButton m_ok_btn;
    private JButton m_cancel_btn;
    private Checkbox m_disable_box;
    private ImRModel m_model;
    
    public RefreshWindow(ImRModel model) {
	super("Refresh Interval Settings");

	m_model = model;

	JPanel _interval_panel = new JPanel();
	GridBagLayout _interval_gbl = new GridBagLayout();
	GridBagConstraints _constraints = new GridBagConstraints();

	JLabel _interval_lbl = new JLabel("Enter an Interval (in ms):");
	buildConstraints(_constraints, 0, 0, 1, 1, 1, 1);
	_constraints.fill = GridBagConstraints.NONE;
	_interval_gbl.setConstraints(_interval_lbl, _constraints);
	_interval_panel.add(_interval_lbl);
	
	m_interval_tf = new JTextField("" + m_model.m_current_refresh_interval, 10);
	buildConstraints(_constraints, 0, 1, 2, 1, 1, 1);
	_constraints.fill = GridBagConstraints.HORIZONTAL;
	_interval_gbl.setConstraints(m_interval_tf, _constraints);
	_interval_panel.add(m_interval_tf);

	m_disable_box = new Checkbox("Disable automatic refresh");
	m_disable_box.setState(m_model.m_refresh_disabled);
	buildConstraints(_constraints, 0, 2, 2, 1, 1, 1);
	_constraints.fill = GridBagConstraints.HORIZONTAL;
	_interval_gbl.setConstraints(m_disable_box, _constraints);
	_interval_panel.add(m_disable_box);

	m_ok_btn = new JButton("OK");
	m_ok_btn.addActionListener(this);
	buildConstraints(_constraints, 0, 3, 1, 1, 1, 1);
	_constraints.fill = GridBagConstraints.NONE;
	_interval_gbl.setConstraints(m_ok_btn, _constraints);
	_interval_panel.add(m_ok_btn);
	
	m_cancel_btn = new JButton("Cancel");
	m_cancel_btn.addActionListener(this);
	buildConstraints(_constraints, 1, 3, 1, 1, 1, 1);
	_constraints.fill = GridBagConstraints.NONE;
	_interval_gbl.setConstraints(m_cancel_btn, _constraints);
	_interval_panel.add(m_cancel_btn);

	_interval_panel.setLayout(_interval_gbl);

	getContentPane().add(_interval_panel);
	pack();
	setVisible(true);
    }

    private void buildConstraints(GridBagConstraints gbc, int gx, int gy, 
				  int gw, int gh, int wx, int wy){
	gbc.gridx = gx;
	gbc.gridy = gy;
	gbc.gridwidth = gw;
	gbc.gridheight = gh;
	gbc.weightx = wx;
	gbc.weighty = wy;
    }

    // implementation of java.awt.event.ActionListener interface
    /**
     *
     * @param event a button has been clicked.
     */
    public void actionPerformed(ActionEvent event) {
	JButton _source = (JButton) event.getSource();
	
	if (_source == m_cancel_btn)
	    dispose();
	else if (_source == m_ok_btn){
	    dispose();
	    if (m_disable_box.getState())
		//disabled is selected
		m_model.disableRefresh();
	    else
		m_model.setRefreshInterval(Integer.parseInt(m_interval_tf.getText()));
	}
    }  
} // RefreshWindow








