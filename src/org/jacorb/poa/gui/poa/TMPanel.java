package org.jacorb.poa.gui.poa;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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
 
import org.jacorb.poa.gui.beans.FillLevelBar;

/**
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.0, 05/03/99, RT
 */
public class TMPanel extends java.awt.Panel {
	private java.awt.Label ivjLabel1 = null;
	private java.awt.Label ivjLabel2 = null;
	private org.jacorb.poa.gui.beans.FillLevelBar ivjFillLevelBar1 = null;
	private java.awt.Label ivjLabel3 = null;
	private org.jacorb.poa.gui.beans.FillLevelBar ivjFillLevelBar2 = null;
FillLevelBar _getActiveRequestsBar() {
	return getFillLevelBar1();
}
/**
 * Return the FillLevelBar1 property value.
 * @return org.jacorb.poa.gui.beans.FillLevelBar
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private org.jacorb.poa.gui.beans.FillLevelBar getFillLevelBar1() {
	if (ivjFillLevelBar1 == null) {
		try {
			ivjFillLevelBar1 = new org.jacorb.poa.gui.beans.FillLevelBar();
			ivjFillLevelBar1.setName("FillLevelBar1");
			ivjFillLevelBar1.setLocation(30, 35);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjFillLevelBar1;
}
/**
 * Return the FillLevelBar1 property value.
 * @return org.jacorb.poa.gui.beans.FillLevelBar
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private org.jacorb.poa.gui.beans.FillLevelBar getFillLevelBar2() {
	if (ivjFillLevelBar2 == null) {
		try {
			ivjFillLevelBar2 = new org.jacorb.poa.gui.beans.FillLevelBar();
			ivjFillLevelBar2.setName("FillLevelBar2");
			ivjFillLevelBar2.setLocation(105, 35);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjFillLevelBar2;
}
/**
 * Return the Label1 property value.
 * @return java.awt.Label
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Label getLabel1() {
	if (ivjLabel1 == null) {
		try {
			ivjLabel1 = new java.awt.Label();
			ivjLabel1.setName("Label1");
			ivjLabel1.setFont(new java.awt.Font("dialog", 2, 10));
			ivjLabel1.setAlignment(java.awt.Label.CENTER);
			ivjLabel1.setText("Request Processing");
			ivjLabel1.setBounds(10, 10, 140, 12);
			ivjLabel1.setForeground(java.awt.Color.black);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjLabel1;
}
/**
 * Return the Label2 property value.
 * @return java.awt.Label
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Label getLabel2() {
	if (ivjLabel2 == null) {
		try {
			ivjLabel2 = new java.awt.Label();
			ivjLabel2.setName("Label2");
			ivjLabel2.setFont(new java.awt.Font("dialog", 2, 10));
			ivjLabel2.setAlignment(java.awt.Label.CENTER);
			ivjLabel2.setText("Active Requests");
			ivjLabel2.setBounds(4, 22, 80, 12);
			ivjLabel2.setForeground(java.awt.Color.black);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjLabel2;
}
/**
 * Return the Label3 property value.
 * @return java.awt.Label
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Label getLabel3() {
	if (ivjLabel3 == null) {
		try {
			ivjLabel3 = new java.awt.Label();
			ivjLabel3.setName("Label3");
			ivjLabel3.setFont(new java.awt.Font("dialog", 2, 10));
			ivjLabel3.setAlignment(java.awt.Label.CENTER);
			ivjLabel3.setText("Thread Pool");
			ivjLabel3.setBounds(90, 22, 62, 12);
			ivjLabel3.setForeground(java.awt.Color.black);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjLabel3;
}
FillLevelBar _getThreadPoolBar() {
	return getFillLevelBar2();
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
	setName("AOMPanel");
	setLayout(null);
	setBackground(java.awt.SystemColor.control);
	setSize(160, 200);
	add(getLabel1(), getLabel1().getName());
	add(getLabel2(), getLabel2().getName());
	add(getFillLevelBar1(), getFillLevelBar1().getName());
	add(getLabel3(), getLabel3().getName());
	add(getFillLevelBar2(), getFillLevelBar2().getName());
	// user code begin {2}
	// user code end
}
/**
 * AOMPanel constructor comment.
 * @param layout java.awt.LayoutManager
 */
public TMPanel(java.awt.LayoutManager layout) {
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
		TMPanel aTMPanel;
		aTMPanel = new TMPanel();
		frame.add("Center", aTMPanel);
		frame.setSize(aTMPanel.getSize());
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
public TMPanel() {
	super();
	initialize();
}
}






