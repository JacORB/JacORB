package org.jacorb.poa.gui.beans;

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
 * @version 1.0, 05/10/99
 * @see		org.jacorb.poa.gui.beans.DoubleListDialog
 */
public class DoubleListHeaderPanel extends java.awt.Panel {
	private java.awt.Label ivjHeaderLabel1 = null;
	private java.awt.Label ivjHeaderLabel2 = null;
/**
 * Constructor
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public DoubleListHeaderPanel() {
	super();
	initialize();
}
/**
 * DoubleListTitlePanel constructor comment.
 * @param layout java.awt.LayoutManager
 */
public DoubleListHeaderPanel(java.awt.LayoutManager layout) {
	super(layout);
}
java.awt.Label _getHeaderLabel1() {
	return getHeaderLabel1();
}
java.awt.Label _getHeaderLabel2() {
	return getHeaderLabel2();
}
/**
 * Return the Label1 property value.
 * @return java.awt.Label
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Label getHeaderLabel1() {
	if (ivjHeaderLabel1 == null) {
		try {
			ivjHeaderLabel1 = new java.awt.Label();
			ivjHeaderLabel1.setName("HeaderLabel1");
			ivjHeaderLabel1.setAlignment(java.awt.Label.LEFT);
			ivjHeaderLabel1.setFont(new java.awt.Font("dialog", 2, 10));
			ivjHeaderLabel1.setText("Label1");
			ivjHeaderLabel1.setBackground(java.awt.SystemColor.control);
			ivjHeaderLabel1.setBounds(10, 0, 180, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjHeaderLabel1;
}
/**
 * Return the Label2 property value.
 * @return java.awt.Label
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Label getHeaderLabel2() {
	if (ivjHeaderLabel2 == null) {
		try {
			ivjHeaderLabel2 = new java.awt.Label();
			ivjHeaderLabel2.setName("HeaderLabel2");
			ivjHeaderLabel2.setAlignment(java.awt.Label.LEFT);
			ivjHeaderLabel2.setFont(new java.awt.Font("dialog", 2, 10));
			ivjHeaderLabel2.setText("Label2");
			ivjHeaderLabel2.setBackground(java.awt.SystemColor.control);
			ivjHeaderLabel2.setBounds(200, 0, 190, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjHeaderLabel2;
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
 * Initialize the class.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initialize() {
	// user code begin {1}
	// user code end
	setName("DoubleListTitlePanel");
	setLayout(null);
	setBackground(java.awt.SystemColor.control);
	setSize(400, 20);
	add(getHeaderLabel1(), getHeaderLabel1().getName());
	add(getHeaderLabel2(), getHeaderLabel2().getName());
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
		DoubleListHeaderPanel aDoubleListHeaderPanel;
		aDoubleListHeaderPanel = new DoubleListHeaderPanel();
		frame.add("Center", aDoubleListHeaderPanel);
		frame.setSize(aDoubleListHeaderPanel.getSize());
		frame.setVisible(true);
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of java.awt.Panel");
		exception.printStackTrace(System.out);
	}
}

}









