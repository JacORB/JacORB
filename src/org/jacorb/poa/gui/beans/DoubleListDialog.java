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
 * Provides the functionality of a double list dialog.
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.0, 05/10/99
 * @see		org.jacorb.poa.gui.beans.DoubleListItem
 */
public class DoubleListDialog extends java.awt.Dialog implements CloseButtonPanelController, java.awt.event.ComponentListener, java.awt.event.MouseListener, java.awt.event.WindowListener {
	private DoubleListItem selectedItem = null;
	private int curr_width = 0;
	private int init_width = 0;
	private int curr_width_wp = 0;
	private int init_width_wp = 0;
	private int itemCount = 0;
	private CloseButtonPanel ivjButtonPanel = null;
	private java.awt.Panel ivjContentsPane = null;
	private java.awt.Panel ivjInnerPanel = null;
	private java.awt.ScrollPane ivjScrollPane = null;
	private java.awt.Panel ivjWorkPanel = null;
	private DoubleListHeaderPanel ivjHeaderPanel = null;
/**
 * Constructor
 * @param parent Symbol
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public DoubleListDialog(java.awt.Frame parent) {
	super(parent);
	initialize();
}
/**
 * DoubleListDialog constructor comment.
 * @param parent java.awt.Frame
 * @param title java.lang.String
 */
public DoubleListDialog(java.awt.Frame parent, String title) {
	super(parent, title);
	initialize();
}
/**
 * DoubleListDialog constructor comment.
 * @param parent java.awt.Frame
 * @param title java.lang.String
 * @param modal boolean
 */
public DoubleListDialog(java.awt.Frame parent, String title, boolean modal) {
	super(parent, title, modal);
	initialize();
}
/**
 * DoubleListDialog constructor comment.
 * @param parent java.awt.Frame
 * @param modal boolean
 */
public DoubleListDialog(java.awt.Frame parent, boolean modal) {
	super(parent, modal);
	initialize();
}
public void _actionCloseButtonPressed() {
	dispose();
}
/**
 * Comment
 */
public void _actionMousePressed(java.awt.event.MouseEvent mouseEvent) {
	_setSelectedItem(null);
}
/**
 * Comment
 */
private void _actionWindowResized(java.awt.event.ComponentEvent componentEvent) {
	int width = getBounds().width;
		
	int diff = (width-12) - curr_width_wp;

	if (width > init_width) {
		curr_width_wp = curr_width_wp + diff;
	} else {
		curr_width_wp = init_width_wp;
	}
	
	getWorkPanel().setSize(curr_width_wp, getWorkPanel().getComponentCount()*14);

/*	
	if (width > init_width || curr_width > init_width) {
		
		java.awt.Component[] components = getWorkPanel().getComponents();
		System.out.println("Have got all components");		
		for (int i=0; i<components.length; i++) {
			((DoubleListItem) components[i])._correctWidth(diff);
		}		
	}
*/	
	getScrollPane().validate();
}
public void _addItem(DoubleListItem item) {
	java.awt.Component c = (java.awt.Component) item;
	c.setLocation(0, itemCount*14);
	getWorkPanel().add(c);
	item._setContainer(this);
	itemCount++;
}
public void _setHeaderLabel1(String str) {
	getHeaderPanel()._getHeaderLabel1().setText(str);
}
public void _setHeaderLabel2(String str) {
	getHeaderPanel()._getHeaderLabel2().setText(str);
}
public void _setSelectedItem(DoubleListItem item) {
	if (selectedItem != null) selectedItem._setSelected(false);
	selectedItem = item;
	if (selectedItem != null) selectedItem._setSelected(true);
}
public void _setSize(int w1, int w2) {
	int width1 = w1 < 100 ? 100 : w1;
	int width2 = w2 < 100 ? 100 : w2;
	
	init_width_wp = curr_width_wp = 10+width1+10+width2+10;
	init_width = curr_width = init_width_wp +12;
	
	setSize(init_width, 400);
	getWorkPanel().setSize(init_width - 12, 0);
	getHeaderPanel()._getHeaderLabel1().setSize(width1, 20);
	getHeaderPanel()._getHeaderLabel2().setBounds(20+width1, 0, width2, 20);
}
/**
 * Method to handle events for the ComponentListener interface.
 * @param e java.awt.event.ComponentEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public void componentHidden(java.awt.event.ComponentEvent e) {
	// user code begin {1}
	// user code end
	// user code begin {2}
	// user code end
}
/**
 * Method to handle events for the ComponentListener interface.
 * @param e java.awt.event.ComponentEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public void componentMoved(java.awt.event.ComponentEvent e) {
	// user code begin {1}
	// user code end
	// user code begin {2}
	// user code end
}
/**
 * Method to handle events for the ComponentListener interface.
 * @param e java.awt.event.ComponentEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public void componentResized(java.awt.event.ComponentEvent e) {
	// user code begin {1}
	// user code end
	if ((e.getSource() == this) ) {
		connEtoC2(e);
	}
	// user code begin {2}
	// user code end
}
/**
 * Method to handle events for the ComponentListener interface.
 * @param e java.awt.event.ComponentEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public void componentShown(java.awt.event.ComponentEvent e) {
	// user code begin {1}
	// user code end
	// user code begin {2}
	// user code end
}
/**
 * connEtoC1:  (DoubleListDialog.window.windowClosing(java.awt.event.WindowEvent) --> DoubleListDialog.dispose()V)
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
 * connEtoC2:  (DoubleListDialog.component.componentResized(java.awt.event.ComponentEvent) --> DoubleListDialog._actionWindowResized(Ljava.awt.event.ComponentEvent;)V)
 * @param arg1 java.awt.event.ComponentEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC2(java.awt.event.ComponentEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this._actionWindowResized(arg1);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC3:  (WorkPanel.mouse.mousePressed(java.awt.event.MouseEvent) --> DoubleListDialog._actionMousePressed(Ljava.awt.event.MouseEvent;)V)
 * @param arg1 java.awt.event.MouseEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC3(java.awt.event.MouseEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this._actionMousePressed(arg1);
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
			ivjContentsPane.setBackground(java.awt.SystemColor.control);
			getContentsPane().add(getButtonPanel(), "South");
			getContentsPane().add(getInnerPanel(), "Center");
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
 * Return the TitlePanel property value.
 * @return org.jacorb.poa.gui.beans.DoubleListHeaderPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private DoubleListHeaderPanel getHeaderPanel() {
	if (ivjHeaderPanel == null) {
		try {
			ivjHeaderPanel = new org.jacorb.poa.gui.beans.DoubleListHeaderPanel();
			ivjHeaderPanel.setName("HeaderPanel");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjHeaderPanel;
}
/**
 * Return the InnerPanel property value.
 * @return java.awt.Panel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Panel getInnerPanel() {
	if (ivjInnerPanel == null) {
		try {
			ivjInnerPanel = new java.awt.Panel();
			ivjInnerPanel.setName("InnerPanel");
			ivjInnerPanel.setLayout(new java.awt.BorderLayout());
			ivjInnerPanel.setBackground(java.awt.SystemColor.control);
			getInnerPanel().add(getHeaderPanel(), "North");
			getInnerPanel().add(getScrollPane(), "Center");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjInnerPanel;
}
/**
 * Return the ScrollPane property value.
 * @return java.awt.ScrollPane
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.ScrollPane getScrollPane() {
	if (ivjScrollPane == null) {
		try {
			ivjScrollPane = new java.awt.ScrollPane();
			ivjScrollPane.setName("ScrollPane");
			getScrollPane().add(getWorkPanel(), getWorkPanel().getName());
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjScrollPane;
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
			ivjWorkPanel.setLayout(null);
			ivjWorkPanel.setBackground(java.awt.Color.lightGray);
			ivjWorkPanel.setLocation(0, 0);
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
	this.addComponentListener(this);
	getWorkPanel().addMouseListener(this);
}
/**
 * Initialize the class.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initialize() {
	// user code begin {1}
	// user code end
	setName("DoubleListDialog");
	setLayout(new java.awt.BorderLayout());
	setSize(400, 400);
	setResizable(true);
	add(getContentsPane(), "Center");
	initConnections();
	// user code begin {2}
	getButtonPanel()._init(this, "Cancel");
	// user code end
}
/**
 * main entrypoint - starts the part when it is run as an application
 * @param args java.lang.String[]
 */
public static void main(java.lang.String[] args) {
	try {
		DoubleListDialog aDoubleListDialog = new org.jacorb.poa.gui.beans.DoubleListDialog(new java.awt.Frame());
		aDoubleListDialog.setModal(true);
		try {
			Class aCloserClass = Class.forName("com.ibm.uvm.abt.edit.WindowCloser");
			Class parmTypes[] = { java.awt.Window.class };
			Object parms[] = { aDoubleListDialog };
			java.lang.reflect.Constructor aCtor = aCloserClass.getConstructor(parmTypes);
			aCtor.newInstance(parms);
		} catch (java.lang.Throwable exc) {};
		aDoubleListDialog.setVisible(true);
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of java.awt.Dialog");
		exception.printStackTrace(System.out);
	}
}
/**
 * Method to handle events for the MouseListener interface.
 * @param e java.awt.event.MouseEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public void mouseClicked(java.awt.event.MouseEvent e) {
	// user code begin {1}
	// user code end
	// user code begin {2}
	// user code end
}
/**
 * Method to handle events for the MouseListener interface.
 * @param e java.awt.event.MouseEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public void mouseEntered(java.awt.event.MouseEvent e) {
	// user code begin {1}
	// user code end
	// user code begin {2}
	// user code end
}
/**
 * Method to handle events for the MouseListener interface.
 * @param e java.awt.event.MouseEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public void mouseExited(java.awt.event.MouseEvent e) {
	// user code begin {1}
	// user code end
	// user code begin {2}
	// user code end
}
/**
 * Method to handle events for the MouseListener interface.
 * @param e java.awt.event.MouseEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public void mousePressed(java.awt.event.MouseEvent e) {
	// user code begin {1}
	// user code end
	if ((e.getSource() == getWorkPanel()) ) {
		connEtoC3(e);
	}
	// user code begin {2}
	// user code end
}
/**
 * Method to handle events for the MouseListener interface.
 * @param e java.awt.event.MouseEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public void mouseReleased(java.awt.event.MouseEvent e) {
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









