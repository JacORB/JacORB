package org.jacorb.poa.gui.pm;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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
 
/**
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.0, 05/03/99, RT
 */
public class StatePanel extends java.awt.Panel implements java.awt.event.ItemListener {
	private StatePanelController controller = null;
	private java.awt.Choice ivjStateChoice = null;
	private java.awt.Label ivjStateLabel = null;
	private java.awt.Checkbox ivjWaitCheckbox = null;
	private java.awt.Panel ivjChoicePanel = null;
	private java.awt.BorderLayout ivjStatePanelBorderLayout = null;
	private java.awt.Checkbox ivjEtherializeCheckbox = null;
/**
 * Constructor
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public StatePanel() {
	super();
	initialize();
}
/**
 * StatePanel constructor comment.
 * @param layout java.awt.LayoutManager
 */
public StatePanel(java.awt.LayoutManager layout) {
	super(layout);
}
java.awt.Checkbox _getEtherializeCheckbox() {
	return getEtherializeCheckbox();
}
java.awt.Choice _getStateChoice() {
	return getStateChoice();
}
java.awt.Checkbox _getWaitCheckbox() {
	return getWaitCheckbox();
}
void _init(StatePanelController _controller) {
	controller = _controller;
}
private void _stateItemChanged(String item, boolean wait, boolean etherialize) {
	if (controller != null) {
		controller._stateItemChanged(item, wait, etherialize);
	}
}
/**
 * connEtoC1:  (StateChoice.item.itemStateChanged(java.awt.event.ItemEvent) --> StatePanel._stateItemChanged(Ljava.lang.String;ZZ)V)
 * @param arg1 java.awt.event.ItemEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC1(java.awt.event.ItemEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this._stateItemChanged(getStateChoice().getSelectedItem(), getWaitCheckbox().getState(), getEtherializeCheckbox().getState());
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * Return the ChoicePanel property value.
 * @return java.awt.Panel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Panel getChoicePanel() {
	java.awt.GridBagConstraints constraintsStateChoice = new java.awt.GridBagConstraints();
	java.awt.GridBagConstraints constraintsWaitCheckbox = new java.awt.GridBagConstraints();
	java.awt.GridBagConstraints constraintsEtherializeCheckbox = new java.awt.GridBagConstraints();
	if (ivjChoicePanel == null) {
		try {
			ivjChoicePanel = new java.awt.Panel();
			ivjChoicePanel.setName("ChoicePanel");
			ivjChoicePanel.setLayout(new java.awt.GridBagLayout());

			constraintsStateChoice.gridx = 0; constraintsStateChoice.gridy = 0;
			constraintsStateChoice.gridwidth = 1; constraintsStateChoice.gridheight = 1;
			constraintsStateChoice.fill = java.awt.GridBagConstraints.HORIZONTAL;
			constraintsStateChoice.anchor = java.awt.GridBagConstraints.WEST;
			constraintsStateChoice.weightx = 1.0;
			constraintsStateChoice.weighty = 0.0;
			constraintsStateChoice.insets = new java.awt.Insets(0, 10, 0, 0);
			getChoicePanel().add(getStateChoice(), constraintsStateChoice);

			constraintsWaitCheckbox.gridx = 0; constraintsWaitCheckbox.gridy = 1;
			constraintsWaitCheckbox.gridwidth = 1; constraintsWaitCheckbox.gridheight = 1;
			constraintsWaitCheckbox.fill = java.awt.GridBagConstraints.HORIZONTAL;
			constraintsWaitCheckbox.anchor = java.awt.GridBagConstraints.WEST;
			constraintsWaitCheckbox.weightx = 0.0;
			constraintsWaitCheckbox.weighty = 0.0;
			constraintsWaitCheckbox.insets = new java.awt.Insets(5, 10, 0, 0);
			getChoicePanel().add(getWaitCheckbox(), constraintsWaitCheckbox);

			constraintsEtherializeCheckbox.gridx = 0; constraintsEtherializeCheckbox.gridy = 2;
			constraintsEtherializeCheckbox.gridwidth = 1; constraintsEtherializeCheckbox.gridheight = 1;
			constraintsEtherializeCheckbox.fill = java.awt.GridBagConstraints.HORIZONTAL;
			constraintsEtherializeCheckbox.anchor = java.awt.GridBagConstraints.WEST;
			constraintsEtherializeCheckbox.weightx = 1.0;
			constraintsEtherializeCheckbox.weighty = 0.0;
			constraintsEtherializeCheckbox.insets = new java.awt.Insets(0, 10, 0, 0);
			getChoicePanel().add(getEtherializeCheckbox(), constraintsEtherializeCheckbox);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjChoicePanel;
}
/**
 * Return the EhterializeCheckbox property value.
 * @return java.awt.Checkbox
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Checkbox getEtherializeCheckbox() {
	if (ivjEtherializeCheckbox == null) {
		try {
			ivjEtherializeCheckbox = new java.awt.Checkbox();
			ivjEtherializeCheckbox.setName("EtherializeCheckbox");
			ivjEtherializeCheckbox.setFont(new java.awt.Font("dialog", 2, 10));
			ivjEtherializeCheckbox.setLabel("etherialize_objects");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjEtherializeCheckbox;
}
/**
 * Return the StateChoice property value.
 * @return java.awt.Choice
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Choice getStateChoice() {
	if (ivjStateChoice == null) {
		try {
			ivjStateChoice = new java.awt.Choice();
			ivjStateChoice.setName("StateChoice");
			ivjStateChoice.setBackground(java.awt.SystemColor.window);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjStateChoice;
}
/**
 * Return the StateLabel property value.
 * @return java.awt.Label
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Label getStateLabel() {
	if (ivjStateLabel == null) {
		try {
			ivjStateLabel = new java.awt.Label();
			ivjStateLabel.setName("StateLabel");
			ivjStateLabel.setText("   State:");
			ivjStateLabel.setForeground(java.awt.Color.black);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjStateLabel;
}
/**
 * Return the StatePanelBorderLayout property value.
 * @return java.awt.BorderLayout
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.BorderLayout getStatePanelBorderLayout() {
	java.awt.BorderLayout ivjStatePanelBorderLayout = null;
	try {
		/* Create part */
		ivjStatePanelBorderLayout = new java.awt.BorderLayout();
		ivjStatePanelBorderLayout.setVgap(0);
	} catch (java.lang.Throwable ivjExc) {
		handleException(ivjExc);
	};
	return ivjStatePanelBorderLayout;
}
/**
 * Return the WaitCheckbox property value.
 * @return java.awt.Checkbox
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Checkbox getWaitCheckbox() {
	if (ivjWaitCheckbox == null) {
		try {
			ivjWaitCheckbox = new java.awt.Checkbox();
			ivjWaitCheckbox.setName("WaitCheckbox");
			ivjWaitCheckbox.setFont(new java.awt.Font("dialog", 2, 10));
			ivjWaitCheckbox.setLabel("wait_for_completion");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjWaitCheckbox;
}
/**
 * Called whenever the part throws an exception.
 * @param exception java.lang.Throwable
 */
private void handleException(Throwable exception) {

	/* Uncomment the following lines to print uncaught exceptions to stdout */
	// System.out.println("--------- UNCAUGHT EXCEPTION ---------");
	// exception.printStackTrace(System.out);
}
/**
 * Initializes connections
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initConnections() {
	// user code begin {1}
	// user code end
	getStateChoice().addItemListener(this);
}
/**
 * Initialize the class.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initialize() {
	// user code begin {1}
	// user code end
	setName("StatePanel");
	setLayout(getStatePanelBorderLayout());
	setBackground(java.awt.SystemColor.control);
	setSize(125, 90);
	add(getStateLabel(), "North");
	add(getChoicePanel(), "Center");
	initConnections();
	// user code begin {2}
	// user code end
}
/**
 * Method to handle events for the ItemListener interface.
 * @param e java.awt.event.ItemEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public void itemStateChanged(java.awt.event.ItemEvent e) {
	// user code begin {1}
	// user code end
	if ((e.getSource() == getStateChoice()) ) {
		connEtoC1(e);
	}
	// user code begin {2}
	// user code end
}
/**
 * main entrypoint - starts the part when it is run as an application
 * @param args java.lang.String[]
 */
public static void main(java.lang.String[] args) {
	try {
		java.awt.Frame frame;
		try {
			Class aFrameClass = Class.forName("com.ibm.uvm.abt.edit.TestFrame");
			frame = (java.awt.Frame)aFrameClass.newInstance();
		} catch (java.lang.Throwable ivjExc) {
			frame = new java.awt.Frame();
		}
		StatePanel aStatePanel;
		aStatePanel = new StatePanel();
		frame.add("Center", aStatePanel);
		frame.setSize(aStatePanel.getSize());
		frame.setVisible(true);
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of java.awt.Panel");
		exception.printStackTrace(System.out);
	}
}

}









