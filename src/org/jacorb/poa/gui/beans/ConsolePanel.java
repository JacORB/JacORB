package org.jacorb.poa.gui.beans;

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
 

/**
 * Provides the functionality of a Console.
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.01, 06/11/99, RT
 */
public class ConsolePanel extends java.awt.Panel implements java.awt.event.ActionListener 
{
	private static int ROW_BUFFER = 256;
	private int rowCount = 0;
	private java.awt.Panel ivjButtonPanel = null;
	private java.awt.FlowLayout ivjButtonPanelFlowLayout = null;
	private java.awt.Button ivjClearButton = null;
	private java.awt.TextArea ivjConsole = null;
	private java.awt.Button ivjHideButton = null;
	private java.awt.Button ivjShowButton = null;
	private java.awt.BorderLayout ivjConsolePanelBorderLayout = null;
	private java.awt.Panel ivjLeftPanel = null;
	private java.awt.Panel ivjRightPanel = null;

/**
 * Constructor
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public ConsolePanel() {
	super();
	initialize();
}
/**
 * ConsolePanel constructor comment.
 * @param layout java.awt.LayoutManager
 */
public ConsolePanel(java.awt.LayoutManager layout) {
	super(layout);
}
private void _actionHideConsole() {
	int height = getBounds().height;
	
	remove(getConsole());
	getButtonPanel().remove(getHideButton());
	getButtonPanel().remove(getClearButton());
	getButtonPanel().add(getShowButton(), getShowButton().getName());
	
	java.awt.Component c = this;
	while ((c = c.getParent()) != null) if (c instanceof java.awt.Frame) break;
	c.setSize(c.getBounds().width, c.getBounds().height-height+33);
	c.validate();
}
private void _actionShowConsole() {
	add(getConsole(), "Center");
	getButtonPanel().remove(getShowButton());
	getButtonPanel().add(getHideButton(), getHideButton().getName());
	getButtonPanel().add(getClearButton(), getClearButton().getName());
	
	java.awt.Component c = this;
	while ((c = c.getParent()) != null) if (c instanceof java.awt.Frame) break;
	c.setSize(c.getBounds().width, c.getBounds().height+100);
	c.validate();
}
synchronized public void _printMessage(String text) {
	if (rowCount++ > ROW_BUFFER) {
		getConsole().setText("");
		rowCount = 1;
	}
	getConsole().append(text + "\n");
}
/**
 * Method to handle events for the ActionListener interface.
 * @param e java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public void actionPerformed(java.awt.event.ActionEvent e) {
	// user code begin {1}
	// user code end
	if ((e.getSource() == getClearButton()) ) {
	    connEtoM1(e);
	}
	if ((e.getSource() == getHideButton()) ) {
	    connEtoC1(e);
	}
	if ((e.getSource() == getShowButton()) ) {
	    connEtoC2(e);
	}
	// user code begin {2}
	// user code end
}
/**
 * connEtoC1:  (HideButton.action.actionPerformed(java.awt.event.ActionEvent) --> ConsolePanel._actionHideConsole()V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC1(java.awt.event.ActionEvent arg1) {
	try {
	    // user code begin {1}
	    // user code end
	    this._actionHideConsole();
	    // user code begin {2}
	    // user code end
	} catch (java.lang.Throwable ivjExc) {
	    // user code begin {3}
	    // user code end
	    handleException(ivjExc);
	}
}
/**
 * connEtoC2:  (ShowButton.action.actionPerformed(java.awt.event.ActionEvent) --> ConsolePanel._actionShowConsole()V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC2(java.awt.event.ActionEvent arg1) {
	try {
	    // user code begin {1}
	    // user code end
	    this._actionShowConsole();
	    // user code begin {2}
	    // user code end
	} catch (java.lang.Throwable ivjExc) {
	    // user code begin {3}
	    // user code end
	    handleException(ivjExc);
	}
}
/**
 * connEtoM1:  (ClearButton.action.actionPerformed(java.awt.event.ActionEvent) --> Console.setText(Ljava.lang.String;)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM1(java.awt.event.ActionEvent arg1) {
	try {
	    // user code begin {1}
	    // user code end
	    getConsole().setText("");
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
 * @return java.awt.Panel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Panel getButtonPanel() {
	if (ivjButtonPanel == null) {
	    try {
		ivjButtonPanel = new java.awt.Panel();
		ivjButtonPanel.setName("ButtonPanel");
		ivjButtonPanel.setLayout(getButtonPanelFlowLayout());
		getButtonPanel().add(getHideButton(), getHideButton().getName());
		getButtonPanel().add(getClearButton(), getClearButton().getName());
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
 * Return the ButtonPanelFlowLayout property value.
 * @return java.awt.FlowLayout
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.FlowLayout getButtonPanelFlowLayout() {
	java.awt.FlowLayout ivjButtonPanelFlowLayout = null;
	try {
	    /* Create part */
	    ivjButtonPanelFlowLayout = new java.awt.FlowLayout();
	    ivjButtonPanelFlowLayout.setAlignment(java.awt.FlowLayout.LEFT);
	    ivjButtonPanelFlowLayout.setVgap(5);
	    ivjButtonPanelFlowLayout.setHgap(10);
	} catch (java.lang.Throwable ivjExc) {
	    handleException(ivjExc);
	};
	return ivjButtonPanelFlowLayout;
}
/**
 * Return the ClearButton property value.
 * @return java.awt.Button
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Button getClearButton() {
	if (ivjClearButton == null) {
	    try {
		ivjClearButton = new java.awt.Button();
		ivjClearButton.setName("ClearButton");
		ivjClearButton.setLabel("        Clear        ");
		// user code begin {1}
		// user code end
	    } catch (java.lang.Throwable ivjExc) {
		// user code begin {2}
		// user code end
		handleException(ivjExc);
	    }
	};
	return ivjClearButton;
}
/**
 * Return the Console property value.
 * @return java.awt.TextArea
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.TextArea getConsole() {
	if (ivjConsole == null) {
	    try {
		ivjConsole = new java.awt.TextArea();
		ivjConsole.setName("Console");
		ivjConsole.setFont(new java.awt.Font("dialog", 0, 10));
		ivjConsole.setBackground(java.awt.SystemColor.window);
		// user code begin {1}
		// user code end
	    } catch (java.lang.Throwable ivjExc) {
		// user code begin {2}
		// user code end
		handleException(ivjExc);
	    }
	};
	return ivjConsole;
}
/**
 * Return the ConsolePanelBorderLayout property value.
 * @return java.awt.BorderLayout
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.BorderLayout getConsolePanelBorderLayout() {
	java.awt.BorderLayout ivjConsolePanelBorderLayout = null;
	try {
	    /* Create part */
	    ivjConsolePanelBorderLayout = new java.awt.BorderLayout();
	    ivjConsolePanelBorderLayout.setVgap(0);
	    ivjConsolePanelBorderLayout.setHgap(10);
	} catch (java.lang.Throwable ivjExc) {
	    handleException(ivjExc);
	};
	return ivjConsolePanelBorderLayout;
}
/**
 * Return the HideButton property value.
 * @return java.awt.Button
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Button getHideButton() {
	if (ivjHideButton == null) {
	    try {
		ivjHideButton = new java.awt.Button();
		ivjHideButton.setName("HideButton");
		ivjHideButton.setLabel("Hide Console");
		// user code begin {1}
		// user code end
	    } catch (java.lang.Throwable ivjExc) {
		// user code begin {2}
		// user code end
		handleException(ivjExc);
	    }
	};
	return ivjHideButton;
}
/**
 * Return the LeftPanel property value.
 * @return java.awt.Panel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Panel getLeftPanel() {
	if (ivjLeftPanel == null) {
	    try {
		ivjLeftPanel = new java.awt.Panel();
		ivjLeftPanel.setName("LeftPanel");
		ivjLeftPanel.setLayout(null);
		// user code begin {1}
		// user code end
	    } catch (java.lang.Throwable ivjExc) {
		// user code begin {2}
		// user code end
		handleException(ivjExc);
	    }
	};
	return ivjLeftPanel;
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
 * Return the ShowButton property value.
 * @return java.awt.Button
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Button getShowButton() {
	if (ivjShowButton == null) {
	    try {
		ivjShowButton = new java.awt.Button();
		ivjShowButton.setName("ShowButton");
		ivjShowButton.setBounds(27, 384, 90, 23);
		ivjShowButton.setLabel("Show Console");
		// user code begin {1}
		// user code end
	    } catch (java.lang.Throwable ivjExc) {
		// user code begin {2}
		// user code end
		handleException(ivjExc);
	    }
	};
	return ivjShowButton;
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
	getClearButton().addActionListener(this);
	getHideButton().addActionListener(this);
	getShowButton().addActionListener(this);
}
/**
 * Initialize the class.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initialize() {
	// user code begin {1}
	// user code end
	setName("ConsolePanel");
	setLayout(getConsolePanelBorderLayout());
	setBackground(java.awt.SystemColor.control);
	setSize(426, 240);
	add(getButtonPanel(), "South");
	add(getConsole(), "Center");
	add(getLeftPanel(), "West");
	add(getRightPanel(), "East");
	initConnections();
	// user code begin {2}
	remove(getConsole());
	getButtonPanel().remove(getHideButton());
	getButtonPanel().remove(getClearButton());
	getButtonPanel().add(getShowButton(), getShowButton().getName());	
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
	    ConsolePanel aConsolePanel;
	    aConsolePanel = new ConsolePanel();
	    frame.add("Center", aConsolePanel);
	    frame.setSize(aConsolePanel.getSize());
	    frame.setVisible(true);
	} catch (Throwable exception) {
	    System.err.println("Exception occurred in main() of java.awt.Panel");
	    exception.printStackTrace(System.out);
	}
	}

}









