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

package org.jacorb.trading.client.query;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import org.omg.CORBA.ORB;
import org.omg.CosTrading.*;
import org.omg.CosTrading.LookupPackage.*;
import org.omg.CosTradingRepos.*;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.*;
import org.jacorb.trading.client.util.*;

public class Query
    extends Frame
    implements ActionListener, ItemListener, Runnable
{
    private Choice m_types;
    private TextField m_constraint;
    private TextField m_preference;
    private Checkbox m_exactType;
    private Checkbox m_useDynamic;
    private Checkbox m_useProxy;
    private Checkbox m_useProps;
    private TextField m_props;
    private Button m_query;
    private TextArea m_results;
    private Label m_status;
    private Lookup m_lookup;
    private ServiceTypeRepository m_repos;
    private static ORB s_orb;


    public Query(Lookup lookup)
    {
	super("Query");

	setFont(new Font("Helvetica", Font.PLAIN, 12));

	m_lookup = lookup;
	org.omg.CORBA.Object obj = lookup.type_repos();
	m_repos = ServiceTypeRepositoryHelper.narrow(obj);

	createContents();
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

	Constrain.constrain(panel, new Label("Preference", Label.LEFT), 0, 3, 2, 1,
			    GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0,
			    5, 10, 0, 10);

	m_preference = new TextField(50);
	m_preference.setEditable(true);
	Constrain.constrain(panel, m_preference, 0, 4, 2, 1,
			    GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 1.0, 0.0,
			    0, 10, 0, 10);

	m_useProps = new Checkbox("Desired properties", false);
	m_useProps.addItemListener(this);
	Constrain.constrain(panel, m_useProps, 0, 5, 2, 1,
			    GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0,
			    5, 10, 0, 10);

	m_props = new TextField(40);
	m_props.setEditable(false);
	Constrain.constrain(panel, m_props, 0, 6, 2, 1,
			    GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 1.0, 0.0,
			    0, 10, 0, 10);

	Panel policiesPanel = new Panel();
	policiesPanel.setLayout(new GridBagLayout());

	m_exactType = new Checkbox("Exact type", false);
	Constrain.constrain(policiesPanel, m_exactType, 0, 0, 1, 1,
			    GridBagConstraints.NONE, GridBagConstraints.WEST);

	m_useDynamic = new Checkbox("Dynamic properties", false);
	Constrain.constrain(policiesPanel, m_useDynamic, 1, 0, 1, 1,
			    GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0,
			    0, 10, 0, 10);

	m_useProxy = new Checkbox("Proxy offers", false);
	Constrain.constrain(policiesPanel, m_useProxy, 2, 0, 1, 1,
			    GridBagConstraints.NONE, GridBagConstraints.EAST);

	Constrain.constrain(panel, policiesPanel, 0, 7, 2, 1,
			    GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 1.0, 0.0,
			    5, 10, 0, 10);

	m_query = new Button("Query");
	m_query.setActionCommand("query");
	m_query.addActionListener(this);
	Constrain.constrain(panel, m_query, 0, 8, 2, 1,
			    GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0,
			    5, 10, 0, 10, 30, 3);

	Constrain.constrain(panel, new Label("Results", Label.LEFT), 0, 9, 2, 1,
			    GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0,
			    5, 10, 0, 10);

	m_results = new TextArea(10, 50);
	m_results.setEditable(false);
	Constrain.constrain(panel, m_results, 0, 10, 2, 1,
			    GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, 1.0, 1.0,
			    0, 10, 0, 10);

	m_status = new Label("", Label.LEFT);
	Constrain.constrain(panel, m_status, 0, 11, 2, 1,
			    GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 1.0, 0.0,
			    0, 10, 3, 10);

	add(panel);
    }


    public void actionPerformed(ActionEvent e)
    {
	String cmd = e.getActionCommand();

	if (cmd.equals("query"))
	    new Thread(this).start();
    }


    public void itemStateChanged(ItemEvent e)
    {
	// make the m_props text field editable or not depending on the
	// state of the m_useProps checkbox
	m_props.setEditable(e.getStateChange() == ItemEvent.SELECTED);
    }


    public void run()
    {
	try {
	    showStatus("Performing query...");
	    m_query.setEnabled(false);
	    m_results.setText("");

	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);

	    String type = m_types.getSelectedItem();
	    String constraint = m_constraint.getText();
	    String preference = m_preference.getText();

	    Policy[] policies = new Policy[3];
	    policies[0] = new Policy();
	    policies[0].name = "exact_type_match";
	    policies[0].value = s_orb.create_any();
	    policies[0].value.insert_boolean(m_exactType.getState());
	    policies[1] = new Policy();
	    policies[1].name = "use_dynamic_properties";
	    policies[1].value = s_orb.create_any();
	    policies[1].value.insert_boolean(m_useDynamic.getState());
	    policies[2] = new Policy();
	    policies[2].name = "use_proxy_offers";
	    policies[2].value = s_orb.create_any();
	    policies[2].value.insert_boolean(m_useProxy.getState());

	    SpecifiedProps desiredProps = new SpecifiedProps();
	    if (! m_useProps.getState())
		//desiredProps._default(HowManyProps.all);
		desiredProps.all_dummy((short)0);
	    else {
		String props = m_props.getText().trim();
		if (props.length() == 0)
		    //desiredProps._default(HowManyProps.none);
		    desiredProps.none_dummy((short)0);
		else {
		    StringTokenizer tok = new StringTokenizer(props, ",");
		    String[] names = new String[tok.countTokens()];
		    int count = 0;
		    while (tok.hasMoreTokens())
			names[count++] = tok.nextToken().trim();
		    desiredProps.prop_names(names);
		}
	    }

	    OfferSeqHolder offers = new OfferSeqHolder();
	    OfferIteratorHolder iter = new OfferIteratorHolder();
	    PolicyNameSeqHolder limits = new PolicyNameSeqHolder();

	    m_lookup.query(type, constraint, preference, policies, desiredProps, 20,
			   offers, iter, limits);

	    int count = offers.value.length;
	    showStatus("Received " + count + " offers...");

	    describeOffers(pw, offers.value);

	    if (iter.value != null) {
		OfferSeqHolder seq = new OfferSeqHolder();
		boolean more;
		do {
		    more = iter.value.next_n(20, seq);
		    count += seq.value.length;
		    showStatus("Received " + count + " offers...");
		    describeOffers(pw, seq.value);
		}
		while (more);

		iter.value.destroy();
	    }

	    pw.flush();
	    m_results.setText(sw.toString());
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
	catch (IllegalPreference e) {
	    showStatus("Illegal preference");
	}
	catch (IllegalPolicyName e) {
	    showStatus("Illegal policy '" + e.name + "'");
	}
	catch (PolicyTypeMismatch e) {
	    showStatus("Policy type mismatch for '" + e.the_policy.name + "'");
	}
	catch (InvalidPolicyValue e) {
	    showStatus("Invalid policy value for '" + e.the_policy.name + "'");
	}
	catch (IllegalPropertyName e) {
	    showStatus("Illegal property name '" + e.name + "'");
	}
	catch (DuplicatePropertyName e) {
	    showStatus("Duplicate property name '" + e.name + "'");
	}
	catch (DuplicatePolicyName e) {
	    showStatus("Duplicate policy name '" + e.name + "'");
	}
	catch (org.omg.CORBA.SystemException e) {
	    showStatus("System error occurred");
	}
	finally {
	    // re-enable button
	    m_query.setEnabled(true);
	}
    }


    protected void describeOffers(PrintWriter pw, Offer[] offers)
    {
	for (int i = 0; i < offers.length; i++) {
	    pw.println("Offer:");
	    pw.println();

	    for (int p = 0; p < offers[i].properties.length; p++) {
		pw.print("  " + offers[i].properties[p].name + " = ");
		AnyUtil.print(s_orb, pw, offers[i].properties[p].value);
		pw.println();
	    }

	    pw.println();
	    pw.println("  Reference:");
	    String ref = s_orb.object_to_string(offers[i].reference);
	    pw.println("    " + ref);
	    pw.println();
	}
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
	System.out.println("Usage: Query iorfile");
	System.exit(1);
    }


    public static void main(String[] args)
    {
	s_orb = ORB.init(args, null); //GB
	Lookup lookup = null;

	try 
	{
	    org.omg.CORBA.Object obj = s_orb.resolve_initial_references("TradingService");

	    if (obj == null) 
	    {
		System.out.println("Invalid object");
		System.exit(1);
	    }

	    lookup = LookupHelper.narrow(obj);
	}
	catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}

	Query app = new Query(lookup);

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










