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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A dialog to enter the name and ior for an object binding
 *
 * @version $Id$
 * @author Gerald Brose, Xtradyne Technologies
 */

public class ObjectDialog
    extends JDialog
    implements ActionListener, KeyListener
{
    JTextField nameField;
    JTextField iorField;
    JCheckBox rebindCheckBox;
    boolean isOk;

    public ObjectDialog(Frame frame)
    {
        super(frame, "Bind Object", true);

        isOk = false;
        JPanel mainPanel = new JPanel( new BorderLayout());

        JPanel hiPanel = new JPanel();
        hiPanel.setLayout( new BoxLayout( hiPanel, BoxLayout.Y_AXIS ));
        JLabel nameLabel = new JLabel("Name:");
        JLabel objectLabel = new JLabel("IOR:");
        rebindCheckBox = new JCheckBox("Rebind if name is bound?", false);
        nameField = new JTextField(40);
        iorField = new JTextField(40);

        hiPanel.add(nameLabel); 
        hiPanel.add(nameField); 
        hiPanel.add(objectLabel);
        hiPanel.add(iorField);
        hiPanel.add(rebindCheckBox);

        JButton ok = new JButton("Ok");
        JButton cancel = new JButton("Cancel");

        JPanel loPanel = new JPanel();
        loPanel.add(ok); 
        loPanel.add(cancel);

        ok.addActionListener(this);
        cancel.addActionListener(this);

        mainPanel.add(hiPanel, BorderLayout.CENTER);
        mainPanel.add(loPanel, BorderLayout.SOUTH);
        getContentPane().add(mainPanel);

        pack();
        show();

    }

    public boolean isRebind()
    {
        return rebindCheckBox.isSelected();
    }

    public String getName()
    {
        return nameField.getText();
    }

    public String getIOR()
    {
        return iorField.getText();
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand().equals("Ok")) 
        {
            try
            {
                isOk = true; 
                dispose();
            } 
            catch (Exception ex)
            {
                JOptionPane.showMessageDialog( this, ex.getMessage(),
                                               "Input error", JOptionPane.ERROR_MESSAGE);
            }
        }
        else dispose();
    }

    public void keyPressed(KeyEvent e) 
    {
        if (e.getKeyCode()==KeyEvent.VK_ENTER) 
            actionPerformed(new ActionEvent(this, 0, "Ok"));
        else if (e.getKeyCode()==KeyEvent.VK_ESCAPE) 
            actionPerformed(new ActionEvent(this, 0, "Cancel"));
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}
}



