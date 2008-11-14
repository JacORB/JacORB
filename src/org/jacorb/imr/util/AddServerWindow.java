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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.MutableComboBoxModel;

/**
 * AddServerWindow.java
 *
 *
 * Created: Mon Nov  1 19:22:12 1999
 *
 */

public class AddServerWindow
    extends JFrame
    implements ActionListener, KeyListener
{
    private transient ImRModel m_model;

    private JTextField m_name_tf;
    private JComboBox m_host_box;
    private JTextField m_command_tf;
    private Component host_box_tf = null; //fast access for paste

    private JButton m_add_btn;
    private JButton m_cancel_btn;

    private transient Clipboard clip_board = null;

    public AddServerWindow(ImRModel model) {
    super("Add Server");

    m_model = model;

    clip_board = Toolkit.getDefaultToolkit().getSystemClipboard();

        addKeyListener( this );

    JPanel _panel = new JPanel();

    GridBagLayout _gbl = new GridBagLayout();
    GridBagConstraints _constraints = new GridBagConstraints();
    _constraints.fill = GridBagConstraints.HORIZONTAL;

    JLabel _name_lbl = new JLabel("Servername: ");
    buildConstraints(_constraints, 0, 0, 1, 1, 1, 1);
    _gbl.setConstraints(_name_lbl, _constraints);
    _panel.add(_name_lbl);

    m_name_tf = new JTextField();
    m_name_tf.requestFocus();
    buildConstraints(_constraints, 1, 0, 1, 1, 100, 1);
    _gbl.setConstraints(m_name_tf, _constraints);
    _panel.add(m_name_tf);


    JLabel _host_lbl = new JLabel("Host: ");
    buildConstraints(_constraints, 0, 1, 1, 1, 1, 1);
    _gbl.setConstraints(_host_lbl, _constraints);
    _panel.add(_host_lbl);

    m_host_box = m_model.getHostSelector();
    buildConstraints(_constraints, 1, 1, 1, 1, 100, 1);
    _gbl.setConstraints(m_host_box, _constraints);
    _panel.add(m_host_box);
        host_box_tf = m_host_box.getEditor().getEditorComponent();

    JLabel _cmd_lbl = new JLabel("Command: ");
    buildConstraints(_constraints, 0, 2, 1, 1, 1, 1);
    _gbl.setConstraints(_cmd_lbl, _constraints);
    _panel.add(_cmd_lbl);

    m_command_tf = new JTextField(30);
    buildConstraints(_constraints, 1, 2, 1, 1, 100, 1);
    _gbl.setConstraints(m_command_tf, _constraints);
    _panel.add(m_command_tf);

    m_add_btn = new JButton ("Add");
    m_add_btn.addActionListener(this);
    buildConstraints(_constraints, 0, 3, 1, 1, 1, 1);
    _constraints.fill = GridBagConstraints.NONE;
    _gbl.setConstraints(m_add_btn, _constraints);
    _panel.add(m_add_btn);

    m_cancel_btn = new JButton ("Cancel");
    m_cancel_btn.addActionListener(this);
    buildConstraints(_constraints, 1, 3, 1, 1, 1, 1);
    _gbl.setConstraints(m_cancel_btn, _constraints);
    _panel.add(m_cancel_btn);

    _panel.setLayout(_gbl);
    getContentPane().add(_panel);

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
    else if (_source == m_add_btn){
        dispose();
        m_model.addServer(m_name_tf.getText(), m_command_tf.getText(),
                  (String) m_host_box.getSelectedItem());
    }
    }

    // implementation of java.awt.event.ActionListener interface
    public void keyTyped( KeyEvent kevt )
    {
        //ignore
    }

    public void keyPressed( KeyEvent kevt )
    {
        try
        {
            if( kevt.getKeyCode() == KeyEvent.VK_PASTE )
            {
                Transferable cb_data = clip_board.getContents( this );

                String text = (String)
                    cb_data.getTransferData(DataFlavor.stringFlavor);

                if( m_name_tf.hasFocus() )
                {
                    m_name_tf.setText( text );
                }
                else if( m_command_tf.hasFocus() )
                {
                    m_command_tf.setText( text );
                }
                else if(host_box_tf.hasFocus() )
                {
                    MutableComboBoxModel model = (MutableComboBoxModel)
                        m_host_box.getModel();

                    model.addElement( text );
                    model.setSelectedItem( text );
                }
            }
        }
        catch( Exception e)
        {
            e.printStackTrace();
        }
    }

    public void keyReleased( KeyEvent kevt )
    {
        //ignore
    }


//     public static void main(String[] args) {
// 	new AddServerWindow(new ImRModel());
//     }
} // AddServerWindow








