
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
import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.TCKind;
import org.omg.CosTrading.*;
import org.omg.CosTradingRepos.*;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.*;
// GB: import jtport.ORBLayer;
import org.jacorb.trading.client.util.*;


public class TypeManager
    extends Frame
    implements ActionListener, ItemListener, Runnable
{
    private List m_types;
    private TextArea m_description;
    private Button m_add;
    private Button m_remove;
    private Checkbox m_masked;
    private Label m_incarnation;
    private Label m_status;
    private AddTypeDialog m_addDialog;
    private ServiceTypeRepository m_repos;
    private static ORB s_orb;


    public TypeManager(ServiceTypeRepository repos)
    {
	super("Service Type Repository");

	setFont(new Font("Helvetica", Font.PLAIN, 12));

	m_repos = repos;

	createContents();

	refreshTypes();
    }


    protected void createContents()
    {
	Panel panel = new Panel();
	panel.setLayout(new GridBagLayout());

	Panel typesPanel = new Panel();
	typesPanel.setLayout(new GridBagLayout());
	Constrain.constrain(typesPanel, new Label("Service types", Label.LEFT),
			    0, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST,
			    0.0, 0.0, 0, 0, 0, 0);
	m_types = new List();
	m_types.setMultipleMode(false);
	m_types.setSize(m_types.getMinimumSize(10));
	m_types.addItemListener(this);
	Constrain.constrain(typesPanel, m_types, 0, 1, 1, 1,
			    GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, 1.0, 1.0,
			    0, 0, 0, 0);

	Constrain.constrain(panel, typesPanel, 0, 0, 1, 1, GridBagConstraints.BOTH,
			    GridBagConstraints.NORTHWEST, 0.25, 1.0, 5, 10, 5, 5);

	Panel descPanel = new Panel();
	descPanel.setLayout(new GridBagLayout());
	Constrain.constrain(descPanel, new Label("Description", Label.LEFT),
			    0, 0, 2, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST,
			    0.0, 0.0, 0, 0, 0, 0);
	m_description = new TextArea(10, 40);
	m_description.setEditable(false);
	Constrain.constrain(descPanel, m_description, 0, 1, 2, 1,
			    GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, 1.0, 1.0,
			    0, 0, 0, 0);

	Constrain.constrain(panel, descPanel, 1, 0, 1, 1, GridBagConstraints.BOTH,
			    GridBagConstraints.NORTHEAST, 0.75, 1.0, 5, 5, 5, 10);

	Panel buttonPanel = new Panel();
	buttonPanel.setLayout(new GridLayout(1, 2, 10, 0));

	m_add = new Button("Add...");
	m_add.setActionCommand("add");
	m_add.addActionListener(this);
	buttonPanel.add(m_add);

	m_remove = new Button("Remove...");
	m_remove.setActionCommand("remove");
	m_remove.addActionListener(this);
	buttonPanel.add(m_remove);

	Constrain.constrain(panel, buttonPanel, 0, 1, 1, 1, GridBagConstraints.NONE,
			    GridBagConstraints.NORTHWEST, 0.0, 0.0, 5, 10, 5, 5);

	Panel maskPanel = new Panel();
	maskPanel.setLayout(new GridBagLayout());

	m_masked = new Checkbox(" Masked");
	m_masked.addItemListener(this);
	Constrain.constrain(maskPanel, m_masked, 0, 2, 1, 1,
			    GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0,
			    0, 0, 0, 0);
	m_incarnation = new Label("Incarnation:", Label.RIGHT);
	Constrain.constrain(maskPanel, m_incarnation, 1, 2, 1, 1,
			    GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHEAST, 1.0, 0.0,
			    0, 0, 0, 0);

	Constrain.constrain(panel, maskPanel, 1, 1, 1, 1,
			    GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHEAST, 1.0, 0.0,
			    5, 5, 5, 10);

	m_status = new Label("", Label.LEFT);
	Constrain.constrain(panel, m_status, 0, 2, 2, 1,
			    GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 1.0, 0.0,
			    0, 10, 3, 10);

	add(panel);
    }


    public void itemStateChanged(ItemEvent e)
    {
	if (e.getItemSelectable() == m_types) {
	    updateButtons();
	    clearStatus();
	    if (e.getStateChange() == ItemEvent.SELECTED)
		describeType();
	}
	else if (e.getItemSelectable() == m_masked)
	    maskType(e.getStateChange() == ItemEvent.SELECTED);
    }


    public void actionPerformed(ActionEvent e)
    {
	String cmd = e.getActionCommand();

	if (cmd.equals("add")) {
	    if (m_addDialog == null) {
		m_addDialog = new AddTypeDialog(this, m_repos);
		m_addDialog.setActionCommand("refresh");
		m_addDialog.addActionListener(this);
	    }

	    clearStatus();
	    m_addDialog.setVisible(true);
	}
	else if (cmd.equals("remove")) {
	    String type = m_types.getSelectedItem();
	    ConfirmDialog confirm =
		new ConfirmDialog(this, "Remove type '" + type + "'?");
	    confirm.setActionCommand("confirm");
	    confirm.addActionListener(this);
	    clearStatus();
	    confirm.setVisible(true);
	}
	else if (cmd.equals("refresh"))
	    refreshTypes();
	else if (cmd.equals("confirm"))
	    removeType();
    }


    protected void removeType()
    {
	try {
	    String type = m_types.getSelectedItem();
	    m_repos.remove_type(type);
	    m_types.remove(type);
	    m_description.setText("");
	    clearStatus();
	    updateButtons();
	}
	catch (HasSubTypes e) {
	    showStatus("Service type '" + e.the_type + "' has subtypes");
	}
	catch (IllegalServiceType e) {
	    showStatus("Illegal service type: " + e.type);
	}
	catch (UnknownServiceType e) {
	    showStatus("Unknown service type: " + e.type);
	}
    }


    protected void maskType(boolean mask)
    {
	try {
	    String type = m_types.getSelectedItem();
	    if (mask)
		m_repos.mask_type(type);
	    else
		m_repos.unmask_type(type);
	    clearStatus();
	}
	catch (NotMasked e) {
	    showStatus("Service type '" + e.name + "' is not masked");
	    m_masked.setState(false);
	}
	catch (AlreadyMasked e) {
	    showStatus("Service type '" + e.name + "' is already masked");
	    m_masked.setState(true);
	}
	catch (IllegalServiceType e) {
	    showStatus("Illegal service type: " + e.type);
	}
	catch (UnknownServiceType e) {
	    showStatus("Unknown service type: " + e.type);
	}
    }


    protected void refreshTypes()
    {
	new Thread(this).start();
    }


    public void run()
    {
	showStatus("Refreshing service types...");
	String[] types;
	SpecifiedServiceTypes whichTypes = new SpecifiedServiceTypes();
	//whichTypes._default(ListOption.all);
	// GB: whichTypes.all_dummy((short)0);
	whichTypes.__default();

	types = m_repos.list_types(whichTypes);
	QuickSort.sort(types);
	m_types.removeAll();
	for (int i = 0; i < types.length; i++)
	    m_types.add(types[i]);
	updateButtons();
	m_description.setText("");
	clearStatus();
    }


    protected void updateButtons()
    {
	if (m_types.getSelectedIndex() < 0) {
	    m_remove.setEnabled(false);
	    m_masked.setEnabled(false);
	    m_incarnation.setText("Incarnation:");
	    m_incarnation.setEnabled(false);
	}
	else {
	    m_remove.setEnabled(true);
	    m_masked.setEnabled(true);
	    m_incarnation.setEnabled(true);
	}
    }


    protected void describeType()
    {
	try {
	    String type = m_types.getSelectedItem();
	    TypeStruct ts = m_repos.describe_type(type);
	    String script = printType(type, ts);

	    m_description.setText(script);
	    m_masked.setState(ts.masked);
	    m_incarnation.setText("Incarnation: {" + ts.incarnation.high +
				  ", " + ts.incarnation.low + "}");
	}
	catch (IllegalServiceType e) {
	    showStatus("Illegal service type: " + e.type);
	}
	catch (UnknownServiceType e) {
	    showStatus("Unknown service type: " + e.type);
	}
	catch (org.omg.CORBA.SystemException e) {
	    showStatus("System error occurred");
	    e.printStackTrace();
	}
    }


    protected String printType(String name, TypeStruct ts)
    {
	StringBuffer buff = new StringBuffer();

	buff.append("service " + name + " ");
	if (ts.super_types.length > 0) {
	    buff.append(": ");
	    for (int i = 0; i < ts.super_types.length; i++) {
		buff.append(ts.super_types[i]);
		if (i < ts.super_types.length - 1)
		    buff.append(", ");
		else
		    buff.append(" ");
	    }
	}
	buff.append("{\n");

	buff.append("  interface " + ts.if_name + ";\n");
	for (int i = 0; i < ts.props.length; i++) {
	    buff.append("  ");
	    switch (ts.props[i].mode.value()) {
	    case PropertyMode._PROP_NORMAL:
		buff.append("property ");
		break;
	    case PropertyMode._PROP_READONLY:
		buff.append("readonly property ");
		break;
	    case PropertyMode._PROP_MANDATORY:
		buff.append("mandatory property ");
		break;
	    case PropertyMode._PROP_MANDATORY_READONLY:
		buff.append("mandatory readonly property ");
		break;
	    }

	    buff.append(convertType(ts.props[i].value_type) + " " +
			ts.props[i].name + ";\n");
	}

	buff.append("};\n");

	return buff.toString();
    }


    protected String convertType(TypeCode tc)
    {
	String result = null;

	TCKind kind = tc.kind();
	if (kind == TCKind.tk_sequence) {
	    try {
		TypeCode elemTC = tc.content_type();
		kind = elemTC.kind();
		result = "sequence<" + convertKind(kind) + ">";
	    }
	    catch (org.omg.CORBA.TypeCodePackage.BadKind e) {
		throw new RuntimeException();
	    }
	}
	else
	    result = convertKind(kind);

	return result;
    }


    protected String convertKind(TCKind kind)
    {
	String result = "unknown";

	switch (kind.value()) {
	case TCKind._tk_null:
	    result = "other";
	    break;
	case TCKind._tk_boolean:
	    result = "boolean";
	    break;
	case TCKind._tk_short:
	    result = "short";
	    break;
	case TCKind._tk_ushort:
	    result = "unsigned short";
	    break;
	case TCKind._tk_long:
	    result = "long";
	    break;
	case TCKind._tk_ulong:
	    result = "unsigned long";
	    break;
	case TCKind._tk_float:
	    result = "float";
	    break;
	case TCKind._tk_double:
	    result = "double";
	    break;
	case TCKind._tk_char:
	    result = "char";
	    break;
	case TCKind._tk_string:
	    result = "string";
	    break;
	}

	return result;
    }


    protected void showStatus(String message)
    {
	m_status.setText(message);
    }


    protected void clearStatus()
    {
	m_status.setText("");
    }


    protected static void usage()
    {
	System.out.println("Usage: TypeManager iorfile");
	System.exit(1);
    }


    public static void main(String[] args)
    {
	s_orb = org.omg.CORBA.ORB.init(args,null);

	ServiceTypeRepository repos = null;

	try 
	{

	    org.omg.CORBA.Object obj = s_orb.resolve_initial_references("TradingService");
	    if (obj == null) 
	    {
		System.out.println("Invalid object");
		System.exit(1);
	    }

	    Lookup lookup = LookupHelper.narrow(obj);
	    obj = lookup.type_repos();
	    repos = ServiceTypeRepositoryHelper.narrow(obj);
	}
	catch (Exception e) 
	{
	    e.printStackTrace();
	    System.exit(1);
	}

	TypeManager app = new TypeManager(repos);

	app.addWindowListener(
			      new WindowAdapter()
			      {
				  public void windowClosing(WindowEvent e)
				      {
					  e.getWindow().dispose();
					  System.exit(0);
				      }
			      }
			      );

	app.pack();
	app.setVisible(true);
    }
}










