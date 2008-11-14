/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * ConnectWindow.java
 *
 *
 * Created: Mon Nov  1 18:22:20 1999
 *
 */

public class ConnectWindow extends JFrame implements ActionListener  {
    private JTextField m_imr_url_tf;
    private JButton m_imr_connect_btn;
    private JButton m_cancel_btn;
    private transient ImRModel m_model;

    public ConnectWindow(ImRModel model) {
    super("Connect to remote repository");

    m_model = model;

    JPanel _url_panel = new JPanel();
    GridBagLayout _url_gbl = new GridBagLayout();
    GridBagConstraints _constraints = new GridBagConstraints();

    JLabel _url_lbl = new JLabel("Enter a URL:");
    buildConstraints(_constraints, 0, 0, 1, 1, 1, 1);
    _constraints.fill = GridBagConstraints.NONE;
    _url_gbl.setConstraints(_url_lbl, _constraints);
    _url_panel.add(_url_lbl);

    m_imr_url_tf = new JTextField("http://", 30);
    buildConstraints(_constraints, 0, 1, 2, 1, 1, 1);
    _constraints.fill = GridBagConstraints.HORIZONTAL;
    _url_gbl.setConstraints(m_imr_url_tf, _constraints);
    _url_panel.add(m_imr_url_tf);

    m_imr_connect_btn = new JButton("Connect");
    m_imr_connect_btn.addActionListener(this);
    buildConstraints(_constraints, 0, 2, 1, 1, 1, 1);
    _constraints.fill = GridBagConstraints.NONE;
    _url_gbl.setConstraints(m_imr_connect_btn, _constraints);
    _url_panel.add(m_imr_connect_btn);

    m_cancel_btn = new JButton("Cancel");
    m_cancel_btn.addActionListener(this);
    buildConstraints(_constraints, 1, 2, 1, 1, 1, 1);
    _constraints.fill = GridBagConstraints.NONE;
    _url_gbl.setConstraints(m_cancel_btn, _constraints);
    _url_panel.add(m_cancel_btn);

    _url_panel.setLayout(_url_gbl);

    getContentPane().add(_url_panel);
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

    public void actionPerformed(ActionEvent event) {
    JButton _source = (JButton) event.getSource();

    if (_source == m_cancel_btn)
        dispose();
    else if (_source == m_imr_connect_btn){
        dispose();
        m_model.connectTo(m_imr_url_tf.getText());
    }
    }
} // ConnectWindow








