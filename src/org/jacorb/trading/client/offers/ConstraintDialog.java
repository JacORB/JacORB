
// Copyright (C) 1998-2001
// Object Oriented Concepts, Inc.

// **********************************************************************
//
// Copyright (c) 1997
// Mark Spruiell (mark@intellisoft.com)
//
// See the COPYING file for more information
//
// **********************************************************************

package org.jacorb.trading.client.offers;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import org.omg.CORBA.ORB;
import org.omg.CosTrading.*;
import org.omg.CosTrading.RegisterPackage.*;
import org.omg.CosTradingRepos.*;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.*;
import org.jacorb.trading.client.util.*;


public class ConstraintDialog
  extends Dialog
  implements ActionListener, Runnable
{
  private Choice m_types;
  private TextField m_constraint;
  private Button m_ok;
  private Button m_cancel;
  private Label m_status;
  private Register m_register;
  private ServiceTypeRepository m_repos;
  private Vector m_listeners = new Vector();
  private String m_actionCommand;


  public ConstraintDialog(Frame f, Register reg)
  {
    super(f, "Withdraw", false);

    m_register = reg;
    org.omg.CORBA.Object obj = reg.type_repos();
    m_repos = ServiceTypeRepositoryHelper.narrow(obj);

    createContents();
    pack();

    addWindowListener(
      new WindowAdapter()
      {
        public void windowClosing(WindowEvent e)
        {
          cancel();
        }
      }
    );

    Point parentLoc = getParent().getLocation();
    setLocation(parentLoc.x + 30, parentLoc.y + 30);
  }


  protected void createContents()
  {
    Panel panel = new Panel();
    panel.setLayout(new GridBagLayout());

    Panel typesPanel = new Panel();
    typesPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    typesPanel.add(new Label("Service type", Label.LEFT));

    m_types = new Choice();
    loadTypes(m_types);
    typesPanel.add(m_types);

    Constrain.constrain(panel, typesPanel, 0, 0, 2, 1,
      GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST,
      1.0, 0.0, 5, 10, 0, 10);

    Constrain.constrain(panel, new Label("Constraint", Label.LEFT), 0, 1, 2, 1,
      GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0,
      5, 10, 0, 10);

    m_constraint = new TextField(50);
    m_constraint.setEditable(true);
    Constrain.constrain(panel, m_constraint, 0, 2, 2, 1,
      GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 1.0, 0.0,
      0, 10, 0, 10);

    m_ok = new Button("OK");
    m_ok.setActionCommand("ok");
    m_ok.addActionListener(this);
    Constrain.constrain(panel, m_ok, 0, 3, 1, 1,
      GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0,
      10, 10, 0, 0, 30, 3);

    m_cancel = new Button("Cancel");
    m_cancel.setActionCommand("cancel");
    m_cancel.addActionListener(this);
    Constrain.constrain(panel, m_cancel, 1, 3, 1, 1,
      GridBagConstraints.NONE, GridBagConstraints.NORTHEAST, 0.0, 0.0,
      10, 0, 0, 10, 10, 3);

    m_status = new Label("", Label.LEFT);
    Constrain.constrain(panel, m_status, 0, 4, 2, 1,
      GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 1.0, 0.0,
      5, 10, 5, 10);

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
    if (e.getActionCommand().equals("ok"))
      ok();
    else if (e.getActionCommand().equals("cancel"))
      cancel();
  }


  protected void ok()
  {
    new Thread(this).start();
  }


  protected void cancel()
  {
    setVisible(false);
  }


  public void run()
  {
    try {
      showStatus("Withdrawing offers...");
      m_ok.setEnabled(false);
      m_cancel.setEnabled(false);

      String type = m_types.getSelectedItem();
      String constraint = m_constraint.getText();

      m_register.withdraw_using_constraint(type, constraint);

      setVisible(false);
      notifyListeners();
      clearStatus();
    }
    catch (IllegalServiceType e) {
      showStatus("Illegal service type '" + e.type + "'");
    }
    catch (UnknownServiceType e) {
      showStatus("Unknown service type '" + e.type + "'");
    }
    catch (IllegalConstraint e) {
      showStatus("Illegal constraint");
    }
    catch (NoMatchingOffers e) {
      showStatus("No matching offers found");
    }
    finally {
        // re-enable buttons
      m_ok.setEnabled(true);
      m_cancel.setEnabled(true);
    }
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


  protected void showStatus(String message)
  {
    m_status.setText(message);
  }


  protected void clearStatus()
  {
    m_status.setText("");
  }


  protected void loadTypes(Choice choice)
  {
    choice.removeAll();
    String[] types;
    SpecifiedServiceTypes whichTypes = new SpecifiedServiceTypes();
    //whichTypes._default(ListOption.all);
    // GB:    whichTypes.all_dummy((short)0);
    whichTypes.__default();

    types = m_repos.list_types(whichTypes);
    QuickSort.sort(types);
    for (int i = 0; i < types.length; i++)
      choice.add(types[i]);
  }
}










