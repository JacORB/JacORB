
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

package org.jacorb.trading.client.typemgr;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import org.omg.CORBA.ORB;
import org.omg.CosTrading.*;
import org.omg.CosTradingRepos.*;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.*;
import org.jacorb.trading.client.util.*;


public class AddTypeDialog extends Dialog implements ActionListener
{
  private TextArea m_description;
  private Button m_ok;
  private Button m_clear;
  private Button m_cancel;
  private Label m_status;
  private ServiceTypeRepository m_repos;
  private Vector m_listeners = new Vector();
  private String m_actionCommand;


  public AddTypeDialog(Frame f, ServiceTypeRepository repos)
  {
    super(f, "Add Service Type", false);

    m_repos = repos;

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

      // a default description
    m_description.setText(
      "service YourService : BaseService {\n" +
      "  interface YourInterface;\n" +
      "  mandatory readonly property sequence<string> myprop;\n" +
      "};\n");
  }


  protected void createContents()
  {
    Panel panel = new Panel();
    panel.setLayout(new GridBagLayout());

    Constrain.constrain(panel, new Label("Description", Label.LEFT),
      0, 0, 3, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST,
      0.0, 0.0, 5, 10, 0, 10);
    m_description = new TextArea(10, 50);
    m_description.setEditable(true);
    Constrain.constrain(panel, m_description, 0, 1, 3, 1,
      GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, 1.0, 1.0,
      0, 10, 0, 10);

    m_ok = new Button("OK");
    m_ok.setActionCommand("ok");
    m_ok.addActionListener(this);
    Constrain.constrain(panel, m_ok, 0, 2, 1, 1,
      GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0,
      10, 10, 0, 0, 30, 3);

    m_clear = new Button("Clear");
    m_clear.setActionCommand("clear");
    m_clear.addActionListener(this);
    Constrain.constrain(panel, m_clear, 1, 2, 1, 1,
      GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0,
      10, 0, 0, 0, 20, 3);

    m_cancel = new Button("Cancel");
    m_cancel.setActionCommand("cancel");
    m_cancel.addActionListener(this);
    Constrain.constrain(panel, m_cancel, 2, 2, 1, 1,
      GridBagConstraints.NONE, GridBagConstraints.NORTHEAST, 0.0, 0.0,
      10, 0, 0, 10, 10, 3);

    m_status = new Label("", Label.LEFT);
    Constrain.constrain(panel, m_status, 0, 3, 2, 1,
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
    else if (e.getActionCommand().equals("clear"))
      m_description.setText("");
    else if (e.getActionCommand().equals("cancel"))
      cancel();
  }


  protected void ok()
  {
    try {
      String text = m_description.getText();

      StringReader reader = new StringReader(text);
      Parser parser = new Parser();
      parser.parse(reader);

      m_repos.add_type(parser.getName(), parser.getInterface(),
        parser.getProperties(), parser.getSuperTypes());

      setVisible(false);
      notifyListeners();
    }
    catch (ParserException e) {
      showStatus("Line " + e.getLine() + ": " + e.getMessage());
    }
    catch (IllegalServiceType e) {
      showStatus("Illegal service type '" + e.type + "'");
    }
    catch (UnknownServiceType e) {
      showStatus("Unknown service type '" + e.type + "'");
    }
    catch (IllegalPropertyName e) {
      showStatus("Illegal property name '" + e.name + "'");
    }
    catch (DuplicatePropertyName e) {
      showStatus("Duplicate property name '" + e.name + "'");
    }
    catch (ServiceTypeExists e) {
      showStatus("Service type '" + e.name + "' already exists");
    }
    catch (DuplicateServiceTypeName e) {
      showStatus("Duplicate super type '" + e.name + "'");
    }
    catch (InterfaceTypeMismatch e) {
      showStatus("Interface type mismatch between " + e.derived_service +
        " and " + e.base_service);
    }
    catch (ValueTypeRedefinition e) {
      showStatus("Value type redefinition in " + e.type_1 +
        " for property '" + e.definition_1.name + "'");
    }
  }


  protected void cancel()
  {
    setVisible(false);
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
}




