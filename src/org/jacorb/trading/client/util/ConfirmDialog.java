
// Copyright (C) 1998-1999
// Object Oriented Concepts, Inc.

// **********************************************************************
//
// Copyright (c) 1997
// Mark Spruiell (mark@intellisoft.com)
//
// See the COPYING file for more information
//
// **********************************************************************

package org.jacorb.trading.client.util;

import java.awt.*;
import java.awt.event.*;
import java.util.*;


public class ConfirmDialog extends Dialog implements ActionListener
{
  private Label m_label;
  private Button m_yes;
  private Button m_no;
  private Vector m_listeners = new Vector();
  private String m_actionCommand;


  public ConfirmDialog(Frame f, String message)
  {
    super(f, "Confirm", true);

    createContents();

    addWindowListener(
      new WindowAdapter()
      {
        public void windowClosing(WindowEvent e)
        {
          no();
        }
      }
    );

    Point parentLoc = getParent().getLocation();
    setLocation(parentLoc.x + 30, parentLoc.y + 30);

    m_label.setText(message);
    pack();
  }


  protected void createContents()
  {
    Panel panel = new Panel();
    panel.setLayout(new GridBagLayout());

    m_label = new Label("", Label.CENTER);
    Constrain.constrain(panel, m_label,
      0, 0, 2, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER,
      1.0, 0.0, 5, 10, 5, 10, 20, 0);

    m_yes = new Button("Yes");
    m_yes.setActionCommand("yes");
    m_yes.addActionListener(this);
    Constrain.constrain(panel, m_yes, 0, 1, 1, 1,
      GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0,
      10, 10, 10, 10, 20, 3);

    m_no = new Button("No");
    m_no.setActionCommand("no");
    m_no.addActionListener(this);
    Constrain.constrain(panel, m_no, 1, 1, 1, 1,
      GridBagConstraints.NONE, GridBagConstraints.NORTHEAST, 0.0, 0.0,
      10, 0, 10, 10, 20, 3);

    add(panel);
  }


  public void addActionListener(ActionListener l)
  {
    m_listeners.addElement(l);
  }


  public void removeActionListener(ActionListener l)
  {
    m_listeners.removeElement(l);
  }


  public void setActionCommand(String command)
  {
    m_actionCommand = command;
  }


  public void actionPerformed(ActionEvent e)
  {
    if (e.getActionCommand().equals("yes"))
      yes();
    else if (e.getActionCommand().equals("no"))
      no();
  }


  protected void yes()
  {
    setVisible(false);
    dispose();
    notifyListeners();
  }


  protected void no()
  {
    setVisible(false);
    dispose();
  }


  protected void notifyListeners()
  {
    ActionEvent evt =
      new ActionEvent(this, ActionEvent.ACTION_PERFORMED, m_actionCommand);

    Enumeration e = m_listeners.elements();
    while (e.hasMoreElements()) {
      ActionListener l = (ActionListener)e.nextElement();
      l.actionPerformed(evt);
    }
  }
}




