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
import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.CosTrading.*;
import org.omg.CosTrading.AdminPackage.*;
import org.omg.CosTrading.RegisterPackage.*;
import org.omg.CosTrading.ProxyPackage.*;
import org.omg.CosTradingDynamic.*;
import org.jacorb.trading.client.util.*;

public class Offers
    extends Frame
    implements ActionListener, ItemListener, Runnable
{
    private Choice m_view;
    private List m_offers;
    private TextArea m_description;
    private Button m_refresh;
    private Button m_withdraw;
    private Button m_withdrawConstraint;
    private Label m_status;
    private ConstraintDialog m_constraintDialog;
    private Admin m_admin;
    private Register m_register;
    private Proxy m_proxy;

    private static ORB s_orb;


    public Offers(Admin admin, Register reg, Proxy proxy)
    {
	super("Service Offers");

	setFont(new Font("Helvetica", Font.PLAIN, 12));

	m_admin = admin;
	m_register = reg;
	m_proxy = proxy;

	createContents();

	refreshOffers();
    }


    protected void createContents()
    {
	Panel panel = new Panel();
	panel.setLayout(new GridBagLayout());

	Panel selectPanel = new Panel();
	selectPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
	selectPanel.add(new Label("View", Label.LEFT));
	m_view = new Choice();
	m_view.add("Offers");
	m_view.add("Proxy Offers");
	m_view.addItemListener(this);
	selectPanel.add(m_view);

	Constrain.constrain(panel, selectPanel, 0, 0, 2, 1,
			    GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 1.0, 0.0,
			    5, 10, 0, 5);

	Panel offersPanel = new Panel();
	offersPanel.setLayout(new GridBagLayout());
	Constrain.constrain(offersPanel, new Label("Offers", Label.LEFT),
			    0, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST,
			    0.0, 0.0, 0, 0, 0, 0);
	m_offers = new List(10, false);
	m_offers.addItemListener(this);
	Constrain.constrain(offersPanel, m_offers, 0, 1, 1, 1,
			    GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, 1.0, 1.0,
			    0, 0, 0, 0);

	Constrain.constrain(panel, offersPanel, 0, 1, 1, 1, GridBagConstraints.BOTH,
			    GridBagConstraints.NORTHWEST, 0.25, 1.0, 5, 10, 5, 5);

	Panel infoPanel = new Panel();
	infoPanel.setLayout(new GridBagLayout());
	Constrain.constrain(infoPanel, new Label("Offer information", Label.LEFT),
			    0, 0, 2, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST,
			    0.0, 0.0, 0, 0, 0, 0);
	m_description = new TextArea(10, 40);
	m_description.setEditable(false);
	Constrain.constrain(infoPanel, m_description, 0, 1, 2, 1,
			    GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, 1.0, 1.0,
			    0, 0, 0, 0);

	Constrain.constrain(panel, infoPanel, 1, 1, 1, 1, GridBagConstraints.BOTH,
			    GridBagConstraints.NORTHEAST, 0.75, 1.0, 5, 5, 5, 10);

	Panel buttonPanel = new Panel();
	buttonPanel.setLayout(new FlowLayout());

	m_refresh = new Button("Refresh");
	m_refresh.setActionCommand("refresh");
	m_refresh.addActionListener(this);
	buttonPanel.add(m_refresh);

	m_withdraw = new Button("Withdraw...");
	m_withdraw.setActionCommand("withdraw");
	m_withdraw.addActionListener(this);
	buttonPanel.add(m_withdraw);

	m_withdrawConstraint = new Button("Withdraw using constraint...");
	m_withdrawConstraint.setActionCommand("withdrawConstraint");
	m_withdrawConstraint.addActionListener(this);
	buttonPanel.add(m_withdrawConstraint);

	Constrain.constrain(panel, buttonPanel, 0, 2, 2, 1, GridBagConstraints.NONE,
			    GridBagConstraints.NORTHWEST, 0.0, 0.0, 5, 5, 0, 10);

	m_status = new Label("", Label.LEFT);
	Constrain.constrain(panel, m_status, 0, 3, 2, 1,
			    GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 1.0, 0.0,
			    0, 10, 3, 10);

	add(panel);
    }


    public void itemStateChanged(ItemEvent e)
    {
	if (e.getItemSelectable() == m_view)
	    refreshOffers();
	else if (e.getItemSelectable() == m_offers) {
	    updateButtons();
	    clearStatus();
	    if (e.getStateChange() == ItemEvent.SELECTED)
		describe();
	}
    }


    public void actionPerformed(ActionEvent e)
    {
	String cmd = e.getActionCommand();

	if (cmd.equals("refresh"))
	    refreshOffers();
	else if (cmd.equals("withdrawConstraint")) {
	    if (m_constraintDialog == null) {
		m_constraintDialog = new ConstraintDialog(this, m_register);
		m_constraintDialog.setActionCommand("refresh");
		m_constraintDialog.addActionListener(this);
	    }

	    clearStatus();
	    m_constraintDialog.setVisible(true);
	}
	else if (cmd.equals("withdraw")) {
	    String id = m_offers.getSelectedItem();
	    ConfirmDialog confirm =
		new ConfirmDialog(this, "Remove offer '" + id + "'?");
	    confirm.setActionCommand("confirm");
	    confirm.addActionListener(this);
	    clearStatus();
	    confirm.setVisible(true);
	}
	else if (cmd.equals("confirm"))
	    removeOffer();
    }


    protected void removeOffer()
    {
	try {
	    String id = m_offers.getSelectedItem();
	    if (m_view.getSelectedItem().equals("Offers"))
		m_register.withdraw(id);
	    else
		m_proxy.withdraw_proxy(id);
	    m_offers.remove(id);
	    m_description.setText("");
	    clearStatus();
	    updateButtons();
	}
	catch (IllegalOfferId e) {
	    showStatus("Illegal offer ID '" + e.id + "'");
	}
	catch (UnknownOfferId e) {
	    showStatus("Unknown offer ID '" + e.id + "'");
	}
	catch (ProxyOfferId e) {
	    showStatus("Offer '" + e.id + "' is a proxy");
	}
	catch (NotProxyOfferId e) {
	    showStatus("Offer '" + e.id + "' is not a proxy");
	}
    }


    protected void refreshOffers()
    {
	new Thread(this).start();
    }


    public void run()
    {
	showStatus("Refreshing offers...");

	m_offers.removeAll();
	m_description.setText("");
	updateButtons();

	try {
	    OfferIdSeqHolder ids = new OfferIdSeqHolder();
	    OfferIdIteratorHolder iter = new OfferIdIteratorHolder();

	    if (m_view.getSelectedItem().equals("Offers"))
		m_admin.list_offers(100, ids, iter);
	    else
		m_admin.list_proxies(100, ids, iter);

	    int count = ids.value.length;

	    showStatus("Received " + count + " offers...");

	    for (int i = 0; i < ids.value.length; i++)
		m_offers.add(ids.value[i]);

	    if (iter.value != null) {
		OfferIdSeqHolder seq = new OfferIdSeqHolder();
		boolean more;
		do {
		    more = iter.value.next_n(100, seq);
		    count += seq.value.length;
		    showStatus("Received " + count + " offers...");
		    for (int i = 0; i < seq.value.length; i++)
			m_offers.add(seq.value[i]);
		}
		while (more);

		iter.value.destroy();
	    }
	}
	catch (NotImplemented e) {
	    showStatus("Admin::list_offers not implemented");
	}
    }


    protected void updateButtons()
    {
	if (m_offers.getSelectedIndex() < 0)
	    m_withdraw.setEnabled(false);
	else
	    m_withdraw.setEnabled(true);

	if (m_view.getSelectedItem().equals("Offers"))
	    m_withdrawConstraint.setEnabled(true);
	else
	    m_withdrawConstraint.setEnabled(false);
    }


    protected void describe()
    {
	String id = m_offers.getSelectedItem();
	if (m_view.getSelectedItem().equals("Offers"))
	    describeOffer(id);
	else
	    describeProxy(id);
    }


    protected void describeOffer(String id)
    {
	try {
	    OfferInfo info = m_register.describe(id);

	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);

	    pw.println("Type: " + info.type);
	    pw.println();

	    describeProperties(pw, info.properties);

	    pw.println();
	    pw.println("Reference:");
	    String ref = s_orb.object_to_string(info.reference);
	    pw.println("  " + ref);

	    pw.flush();

	    m_description.setText(sw.toString());
	}
	catch (IllegalOfferId e) {
	    showStatus("Illegal offer ID '" + e.id + "'");
	}
	catch (UnknownOfferId e) {
	    showStatus("Unknown offer ID '" + e.id + "'");
	}
	catch (ProxyOfferId e) {
	    showStatus("Offer '" + e.id + "' is a proxy");
	}
    }


    protected void describeProxy(String id)
    {
	try {
	    ProxyInfo info = m_proxy.describe_proxy(id);

	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);

	    pw.println("Type: " + info.type);
	    pw.println();

	    describeProperties(pw, info.properties);

	    pw.println();
	    pw.println("Target:");
	    String ref = s_orb.object_to_string(info.target);
	    pw.println("  " + ref);

	    pw.println();
	    pw.println("If match all: " + info.if_match_all);

	    pw.println();
	    pw.println("Recipe:");
	    pw.println("  " + info.recipe);

	    pw.println();
	    pw.println("Policies to pass on:");
	    for (int p = 0; p < info.policies_to_pass_on.length; p++) {
		pw.print("  " + info.policies_to_pass_on[p].name + " = ");
		AnyUtil.print(s_orb,pw, info.policies_to_pass_on[p].value);
		pw.println();
	    }

	    pw.flush();

	    m_description.setText(sw.toString());
	}
	catch (IllegalOfferId e) {
	    showStatus("Illegal offer ID '" + e.id + "'");
	}
	catch (UnknownOfferId e) {
	    showStatus("Unknown offer ID '" + e.id + "'");
	}
	catch (NotProxyOfferId e) {
	    showStatus("Offer '" + e.id + "' is not a proxy");
	}
    }


    protected void describeProperties(PrintWriter pw, Property[] props)
    {
	pw.println("Properties:");

	for (int p = 0; p < props.length; p++) {
	    pw.print("  " + props[p].name + " = ");

	    TypeCode tc = props[p].value.type();
	    if (tc.equal(DynamicPropHelper.type())) {
		pw.println("<Dynamic>");
		DynamicProp dp = DynamicPropHelper.extract(props[p].value);
		pw.print("    eval_if = ");
		String ref = s_orb.object_to_string(dp.eval_if);
		pw.println(ref);
		pw.print("    returned_type = ");
		AnyUtil.print(pw, dp.returned_type);
		pw.println();
		pw.print("    extra_info = ");
		AnyUtil.print(s_orb,pw, dp.extra_info);
		pw.println();
	    }
	    else {
		AnyUtil.print(s_orb,pw, props[p].value);
		pw.println();
	    }
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


    protected static void usage()
    {
	System.out.println("Usage: Offers iorfile");
	System.exit(1);
    }


    public static void main(String[] args)
    {
	s_orb = org.omg.CORBA.ORB.init(args,null);

	Admin admin = null;
	Register reg = null;
	Proxy proxy = null;

	try 
	{
	    org.omg.CORBA.Object obj = s_orb.resolve_initial_references("TradingService");

	    if (obj == null) {
		System.out.println("Invalid object");
		System.exit(1);
	    }

	    Lookup lookup = LookupHelper.narrow(obj);
	    admin = lookup.admin_if();
	    reg = lookup.register_if();
	    proxy = lookup.proxy_if();
	}
	catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}

	Offers app = new Offers(admin, reg, proxy);

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










