package org.jacorb.poa.gui.beans;

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
 
/**
 * A Message Dialog Box
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.0, 05/10/99, RT
 */
public class MessageDialog extends java.awt.Dialog 
	implements CloseButtonPanelController, java.awt.event.WindowListener {
	private String message = "";
	private int xPos = 0;
	private int yPos = 0;
	private CloseButtonPanel ivjButtonPanel = null;
	private java.awt.Panel ivjContentsPane = null;
	private java.awt.Label ivjMessageLabel = null;
	private java.awt.Panel ivjWorkPanel = null;
/**
 * Constructor
 * @param parent Symbol
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public MessageDialog(java.awt.Frame parent) {
	super(parent);
	initialize();
}
/**
 * MessageDialog constructor comment.
 * @param parent java.awt.Frame
 * @param title java.lang.String
 */
public MessageDialog(java.awt.Frame parent, String title) {
	super(parent, title);
}
public MessageDialog(java.awt.Frame parent, String title, String _message) {
	super(parent, title);
	message = _message;
	xPos = parent.getBounds().x + parent.getBounds().width/4;
	yPos = parent.getBounds().y + parent.getBounds().height/3;
	initialize();
}
/**
 * MessageDialog constructor comment.
 * @param parent java.awt.Frame
 * @param title java.lang.String
 * @param modal boolean
 */
public MessageDialog(java.awt.Frame parent, String title, boolean modal) {
	super(parent, title, modal);
}
/**
 * MessageDialog constructor comment.
 * @param parent java.awt.Frame
 * @param modal boolean
 */
public MessageDialog(java.awt.Frame parent, boolean modal) {
	super(parent, modal);
}
public void _actionCloseButtonPressed() {
	dispose();
}
/**
 * connEtoC1:  (MessageDialog.window.windowClosing(java.awt.event.WindowEvent) --> MessageDialog.dispose()V)
 * @param arg1 java.awt.event.WindowEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC1(java.awt.event.WindowEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.dispose();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * Return the ButtonPanel property value.
 * @return org.jacorb.poa.gui.beans.CloseButtonPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private CloseButtonPanel getButtonPanel() {
	if (ivjButtonPanel == null) {
		try {
			ivjButtonPanel = new org.jacorb.poa.gui.beans.CloseButtonPanel();
			ivjButtonPanel.setName("ButtonPanel");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjButtonPanel;
}
/**
 * Return the ContentsPane property value.
 * @return java.awt.Panel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Panel getContentsPane() {
	if (ivjContentsPane == null) {
		try {
			ivjContentsPane = new java.awt.Panel();
			ivjContentsPane.setName("ContentsPane");
			ivjContentsPane.setLayout(new java.awt.BorderLayout());
			getContentsPane().add(getButtonPanel(), "South");
			getContentsPane().add(getWorkPanel(), "Center");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjContentsPane;
}
/**
 * Return the MessageLabel property value.
 * @return java.awt.Label
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Label getMessageLabel() {
	if (ivjMessageLabel == null) {
		try {
			ivjMessageLabel = new java.awt.Label();
			ivjMessageLabel.setName("MessageLabel");
			ivjMessageLabel.setAlignment(java.awt.Label.CENTER);
			ivjMessageLabel.setText("Message");
			ivjMessageLabel.setBackground(java.awt.SystemColor.control);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjMessageLabel;
}
/**
 * Return the WorkPanel property value.
 * @return java.awt.Panel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Panel getWorkPanel() {
	if (ivjWorkPanel == null) {
		try {
			ivjWorkPanel = new java.awt.Panel();
			ivjWorkPanel.setName("WorkPanel");
			ivjWorkPanel.setLayout(new java.awt.BorderLayout());
			getWorkPanel().add(getMessageLabel(), "Center");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjWorkPanel;
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
	this.addWindowListener(this);
}
/**
 * Initialize the class.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initialize() {
	// user code begin {1}
	// user code end
	setName("MessageDialog");
	setLayout(new java.awt.BorderLayout());
	setBackground(java.awt.SystemColor.control);
	setSize(420, 120);
	setModal(true);
	add(getContentsPane(), "Center");
	initConnections();
	// user code begin {2}
	getButtonPanel()._init(this, "    OK    ");
	setLocation(xPos, yPos);
	getMessageLabel().setText(message);
	// user code end
}
/**
 * main entrypoint - starts the part when it is run as an application
 * @param args java.lang.String[]
 */
public static void main(java.lang.String[] args) {
	try {
		MessageDialog aMessageDialog = new org.jacorb.poa.gui.beans.MessageDialog(new java.awt.Frame());
		aMessageDialog.setModal(true);
		try {
			Class aCloserClass = Class.forName("com.ibm.uvm.abt.edit.WindowCloser");
			Class parmTypes[] = { java.awt.Window.class };
			Object parms[] = { aMessageDialog };
			java.lang.reflect.Constructor aCtor = aCloserClass.getConstructor(parmTypes);
			aCtor.newInstance(parms);
		} catch (java.lang.Throwable exc) {};
		aMessageDialog.setVisible(true);
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of java.awt.Dialog");
		exception.printStackTrace(System.out);
	}
}
/**
 * Method to handle events for the WindowListener interface.
 * @param e java.awt.event.WindowEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public void windowActivated(java.awt.event.WindowEvent e) {
	// user code begin {1}
	// user code end
	// user code begin {2}
	// user code end
}
/**
 * Method to handle events for the WindowListener interface.
 * @param e java.awt.event.WindowEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public void windowClosed(java.awt.event.WindowEvent e) {
	// user code begin {1}
	// user code end
	// user code begin {2}
	// user code end
}
/**
 * Method to handle events for the WindowListener interface.
 * @param e java.awt.event.WindowEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public void windowClosing(java.awt.event.WindowEvent e) {
	// user code begin {1}
	// user code end
	if ((e.getSource() == this) ) {
		connEtoC1(e);
	}
	// user code begin {2}
	// user code end
}
/**
 * Method to handle events for the WindowListener interface.
 * @param e java.awt.event.WindowEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public void windowDeactivated(java.awt.event.WindowEvent e) {
	// user code begin {1}
	// user code end
	// user code begin {2}
	// user code end
}
/**
 * Method to handle events for the WindowListener interface.
 * @param e java.awt.event.WindowEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public void windowDeiconified(java.awt.event.WindowEvent e) {
	// user code begin {1}
	// user code end
	// user code begin {2}
	// user code end
}
/**
 * Method to handle events for the WindowListener interface.
 * @param e java.awt.event.WindowEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public void windowIconified(java.awt.event.WindowEvent e) {
	// user code begin {1}
	// user code end
	// user code begin {2}
	// user code end
}
/**
 * Method to handle events for the WindowListener interface.
 * @param e java.awt.event.WindowEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public void windowOpened(java.awt.event.WindowEvent e) {
	// user code begin {1}
	// user code end
	// user code begin {2}
	// user code end
}

}



