package org.jacorb.poa.gui.beans;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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
 * Panel with a Close Button
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.01, 05/10/99
 */
public class CloseButtonPanel extends java.awt.Panel implements java.awt.event.ActionListener {
	private CloseButtonPanelController controller = null;
	private String buttonLabel = "Close";
	private java.awt.FlowLayout ivjCloseButtonPanelFlowLayout = null;
	private java.awt.Button ivjCloseButton = null;
/**
 * Constructor
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public CloseButtonPanel() {
	super();
	initialize();
}
/**
 * CloseButtonPanel constructor comment.
 * @param layout java.awt.LayoutManager
 */
public CloseButtonPanel(java.awt.LayoutManager layout) {
	super(layout);
}
private void _actionCloseButtonPressed() {
	if (controller != null) {
		controller._actionCloseButtonPressed();
	}	
}
public void _init(CloseButtonPanelController _controller, String _buttonLabel) {
	controller = _controller;
	buttonLabel = _buttonLabel;
	getCloseButton().setLabel("  "+buttonLabel+"  ");
}
/**
 * Method to handle events for the ActionListener interface.
 * @param e java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public void actionPerformed(java.awt.event.ActionEvent e) {
	// user code begin {1}
	// user code end
	if ((e.getSource() == getCloseButton()) ) {
		connEtoC1(e);
	}
	// user code begin {2}
	// user code end
}
/**
 * connEtoC1:  (Button1.action.actionPerformed(java.awt.event.ActionEvent) --> CloseButtonPanel._actionCloseButtonPressed()V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC1(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this._actionCloseButtonPressed();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * Return the Button1 property value.
 * @return java.awt.Button
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Button getCloseButton() {
	if (ivjCloseButton == null) {
		try {
			ivjCloseButton = new java.awt.Button();
			ivjCloseButton.setName("CloseButton");
			ivjCloseButton.setLabel("  Close  ");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjCloseButton;
}
/**
 * Return the CloseButtonPanelFlowLayout property value.
 * @return java.awt.FlowLayout
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.FlowLayout getCloseButtonPanelFlowLayout() {
	java.awt.FlowLayout ivjCloseButtonPanelFlowLayout = null;
	try {
		/* Create part */
		ivjCloseButtonPanelFlowLayout = new java.awt.FlowLayout();
		ivjCloseButtonPanelFlowLayout.setAlignment(java.awt.FlowLayout.RIGHT);
		ivjCloseButtonPanelFlowLayout.setHgap(10);
	} catch (java.lang.Throwable ivjExc) {
		handleException(ivjExc);
	};
	return ivjCloseButtonPanelFlowLayout;
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
	getCloseButton().addActionListener(this);
}
/**
 * Initialize the class.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initialize() {
	// user code begin {1}
	// user code end
	setName("CloseButtonPanel");
	setLayout(getCloseButtonPanelFlowLayout());
	setBackground(java.awt.SystemColor.control);
	setSize(427, 34);
	add(getCloseButton(), getCloseButton().getName());
	initConnections();
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
		CloseButtonPanel aCloseButtonPanel;
		aCloseButtonPanel = new CloseButtonPanel();
		frame.add("Center", aCloseButtonPanel);
		frame.setSize(aCloseButtonPanel.getSize());
		frame.setVisible(true);
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of java.awt.Panel");
		exception.printStackTrace(System.out);
	}
}
public void paint(java.awt.Graphics g) {
	g.setColor(getBackground());
	g.fill3DRect(0, 0, getBounds().width, getBounds().height, true);
}

}









