package org.jacorb.poa.gui.pm;

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
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.0, 05/03/99, RT
 */
public class POAManagerStatePanel extends java.awt.Panel {
	private RegisterPanel ivjRegisterPanel = null;
	private StatePanel ivjStatePanel = null;
	private java.awt.BorderLayout ivjPOAManagerStateLabelBorderLayout = null;
	private java.awt.Panel ivjRightPanel = null;
	private java.awt.Panel ivjBottomPanel = null;
/**
 * Constructor
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public POAManagerStatePanel() {
	super();
	initialize();
}
/**
 * POAManagerStateLabel constructor comment.
 * @param layout java.awt.LayoutManager
 */
public POAManagerStatePanel(java.awt.LayoutManager layout) {
	super(layout);
}
RegisterPanel _getRegisterPanel() {
	return getRegisterPanel();
}
StatePanel _getStatePanel() {
	return getStatePanel();
}
/**
 * Return the BottomPanel property value.
 * @return java.awt.Panel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Panel getBottomPanel() {
	if (ivjBottomPanel == null) {
		try {
			ivjBottomPanel = new java.awt.Panel();
			ivjBottomPanel.setName("BottomPanel");
			ivjBottomPanel.setLayout(null);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjBottomPanel;
}
/**
 * Return the POAManagerStateLabelBorderLayout property value.
 * @return java.awt.BorderLayout
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.BorderLayout getPOAManagerStateLabelBorderLayout() {
	java.awt.BorderLayout ivjPOAManagerStateLabelBorderLayout = null;
	try {
		/* Create part */
		ivjPOAManagerStateLabelBorderLayout = new java.awt.BorderLayout();
		ivjPOAManagerStateLabelBorderLayout.setVgap(10);
		ivjPOAManagerStateLabelBorderLayout.setHgap(10);
	} catch (java.lang.Throwable ivjExc) {
		handleException(ivjExc);
	};
	return ivjPOAManagerStateLabelBorderLayout;
}
/**
 * Return the RegisterPanel property value.
 * @return org.jacorb.poa.gui.pm.RegisterPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private RegisterPanel getRegisterPanel() {
	if (ivjRegisterPanel == null) {
		try {
			ivjRegisterPanel = new org.jacorb.poa.gui.pm.RegisterPanel();
			ivjRegisterPanel.setName("RegisterPanel");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjRegisterPanel;
}
/**
 * Return the RightPanel property value.
 * @return java.awt.Panel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Panel getRightPanel() {
	if (ivjRightPanel == null) {
		try {
			ivjRightPanel = new java.awt.Panel();
			ivjRightPanel.setName("RightPanel");
			ivjRightPanel.setLayout(null);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjRightPanel;
}
/**
 * Return the StatePanel property value.
 * @return org.jacorb.poa.gui.pm.StatePanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private StatePanel getStatePanel() {
	if (ivjStatePanel == null) {
		try {
			ivjStatePanel = new org.jacorb.poa.gui.pm.StatePanel();
			ivjStatePanel.setName("StatePanel");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjStatePanel;
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
	setName("POAManagerStateLabel");
	setLayout(getPOAManagerStateLabelBorderLayout());
	setBackground(java.awt.SystemColor.control);
	setSize(310, 100);
	add(getRegisterPanel(), "Center");
	add(getStatePanel(), "West");
	add(getRightPanel(), "East");
	add(getBottomPanel(), "South");
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
		POAManagerStatePanel aPOAManagerStatePanel;
		aPOAManagerStatePanel = new POAManagerStatePanel();
		frame.add("Center", aPOAManagerStatePanel);
		frame.setSize(aPOAManagerStatePanel.getSize());
		frame.setVisible(true);
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of java.awt.Panel");
		exception.printStackTrace(System.out);
	}
}

}









