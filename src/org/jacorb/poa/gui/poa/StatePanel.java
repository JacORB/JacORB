package org.jacorb.poa.gui.poa;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-98  Gerald Brose.
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
 
import java.awt.Label;

/**
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.0, 05/03/99, RT
 */
public class StatePanel extends java.awt.Panel {
	private java.awt.Label ivjNameLabel = null;
	private java.awt.Label ivjValueLabel = null;
/**
 * Return the NameLabel property value.
 * @return java.awt.Label
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Label getNameLabel() {
	if (ivjNameLabel == null) {
		try {
			ivjNameLabel = new java.awt.Label();
			ivjNameLabel.setName("NameLabel");
			ivjNameLabel.setText("State:");
			ivjNameLabel.setBounds(10, 10, 50, 23);
			ivjNameLabel.setForeground(java.awt.Color.black);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjNameLabel;
}
Label _getStateLabel() {
	return getValueLabel();
}
/**
 * Return the ValueLabel property value.
 * @return java.awt.Label
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Label getValueLabel() {
	if (ivjValueLabel == null) {
		try {
			ivjValueLabel = new java.awt.Label();
			ivjValueLabel.setName("ValueLabel");
			ivjValueLabel.setFont(new java.awt.Font("dialog", 2, 12));
			ivjValueLabel.setText("active");
			ivjValueLabel.setBounds(60, 10, 230, 23);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjValueLabel;
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
	setName("StatePanel");
	setLayout(null);
	setBackground(java.awt.SystemColor.control);
	setSize(300, 43);
	add(getNameLabel(), getNameLabel().getName());
	add(getValueLabel(), getValueLabel().getName());
	// user code begin {2}
	// user code end
}
/**
 * StatePanel constructor comment.
 * @param layout java.awt.LayoutManager
 */
public StatePanel(java.awt.LayoutManager layout) {
	super(layout);
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
public void paint(java.awt.Graphics g) {
	g.setColor(getBackground());
	g.fill3DRect(0, 0, getBounds().width, getBounds().height, true);
}
/**
 * Constructor
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public StatePanel() {
	super();
	initialize();
}
}






