package org.jacorb.poa.gui.pm;

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
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.02, 05/31/99, RT
 */
public class RegisterPanel extends java.awt.Panel implements java.awt.event.ActionListener, java.awt.event.MouseListener {
	private RegisterPanelController controller;
	private java.awt.List ivjPOAList = null;
	private java.awt.Label ivjRegisterLabel = null;
	private java.awt.BorderLayout ivjRegisterPanelBorderLayout = null;
	private java.awt.MenuItem ivjCloseMenuItem = null;
	private java.awt.MenuItem ivjDestroyMenuItem = null;
	private java.awt.MenuItem ivjOpenMenuItem = null;
	private org.jacorb.poa.gui.beans.PopupMenu ivjPopupMenu = null;
/**
 * Constructor
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public RegisterPanel() {
	super();
	initialize();
}
/**
 * RegisterPanel constructor comment.
 * @param layout java.awt.LayoutManager
 */
public RegisterPanel(java.awt.LayoutManager layout) {
	super(layout);
}
/**
 * Comment
 */
private void _actionClosePOAMonitor(String item) {
	if (controller != null) {
		controller._actionClosePOAMonitor(item);
	}
}
/**
 * Comment
 */
private void _actionDestroyPOA(String item) {
	if (controller != null) {
		controller._actionDestroyPOA(item);
	}
}
/**
 * Comment
 */
private void _actionMousePressed(java.awt.event.MouseEvent e) {
	if (getPOAList().getSelectedItem() != null) {
		int mods = e.getModifiers();
		if ((mods & java.awt.event.MouseEvent.BUTTON3_MASK) != 0) {
			/* Right Mouse Button pressed */
			getPopupMenu()._show(this, getBounds().width, e.getY()+10);
		} else if ((mods & java.awt.event.MouseEvent.BUTTON2_MASK) != 0) {
			/* Middle Mouse Button pressed */
		} else {
			/* Left Mouse Button pressed */
			if (e.getClickCount() > 1) {
				/* doubel click */
				_actionOpenPOAMonitor(getPOAList().getSelectedItem());
			}
		}
	}
}
/**
 * Comment
 */
private void _actionOpenPOAMonitor(String item) {
	if (controller != null) {
		controller._actionOpenPOAMonitor(item);
	}
}
java.awt.List _getPOAList() {
	return getPOAList();
}
void _init(RegisterPanelController _controller) {
	controller = _controller;
}
/**
 * Method to handle events for the ActionListener interface.
 * @param e java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public void actionPerformed(java.awt.event.ActionEvent e) {
	// user code begin {1}
	// user code end
	if ((e.getSource() == getOpenMenuItem()) ) {
		connEtoC2(e);
	}
	if ((e.getSource() == getCloseMenuItem()) ) {
		connEtoC3(e);
	}
	if ((e.getSource() == getDestroyMenuItem()) ) {
		connEtoC4(e);
	}
	// user code begin {2}
	// user code end
}
/**
 * connEtoC1:  (POAList.mouse.mousePressed(java.awt.event.MouseEvent) --> RegisterPanel._actionMousePressed(Ljava.awt.event.MouseEvent;)V)
 * @param arg1 java.awt.event.MouseEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC1(java.awt.event.MouseEvent arg1) {
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
 * connEtoC2:  (OpenMenuItem.action.actionPerformed(java.awt.event.ActionEvent) --> RegisterPanel._actionOpenPOAMonitor(Ljava.lang.String;)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC2(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this._actionOpenPOAMonitor(getPOAList().getSelectedItem());
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC3:  (CloseMenuItem.action.actionPerformed(java.awt.event.ActionEvent) --> RegisterPanel._actionClosePOAMonitor(Ljava.lang.String;)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC3(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this._actionClosePOAMonitor(getPOAList().getSelectedItem());
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC4:  (DestroyMenuItem.action.actionPerformed(java.awt.event.ActionEvent) --> RegisterPanel._actionDestroyPOA(Ljava.lang.String;)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC4(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this._actionDestroyPOA(getPOAList().getSelectedItem());
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * Return the CloseMenuItem property value.
 * @return java.awt.MenuItem
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.MenuItem getCloseMenuItem() {
	if (ivjCloseMenuItem == null) {
		try {
			ivjCloseMenuItem = new java.awt.MenuItem();
			ivjCloseMenuItem.setLabel("Close Monitor");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjCloseMenuItem;
}
/**
 * Return the DestroyMenuItem property value.
 * @return java.awt.MenuItem
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.MenuItem getDestroyMenuItem() {
	if (ivjDestroyMenuItem == null) {
		try {
			ivjDestroyMenuItem = new java.awt.MenuItem();
			ivjDestroyMenuItem.setLabel("Destroy POA");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjDestroyMenuItem;
}
/**
 * Return the OpenMenuItem property value.
 * @return java.awt.MenuItem
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.MenuItem getOpenMenuItem() {
	if (ivjOpenMenuItem == null) {
		try {
			ivjOpenMenuItem = new java.awt.MenuItem();
			ivjOpenMenuItem.setLabel("Open Monitor");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjOpenMenuItem;
}
/**
 * Return the POAList property value.
 * @return java.awt.List
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.List getPOAList() {
	if (ivjPOAList == null) {
		try {
			ivjPOAList = new java.awt.List();
			ivjPOAList.setName("POAList");
			ivjPOAList.setFont(new java.awt.Font("dialog", 0, 10));
			ivjPOAList.setBackground(java.awt.SystemColor.activeCaptionText);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjPOAList;
}
/**
 * Return the PopupMenu property value.
 * @return org.jacorb.poa.gui.beans.PopupMenu
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private org.jacorb.poa.gui.beans.PopupMenu getPopupMenu() {
	if (ivjPopupMenu == null) {
		try {
			ivjPopupMenu = new org.jacorb.poa.gui.beans.PopupMenu();
			ivjPopupMenu.setLabel("POA Actions");
			ivjPopupMenu.add(getOpenMenuItem());
			ivjPopupMenu.add(getCloseMenuItem());
			ivjPopupMenu.add(getDestroyMenuItem());
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjPopupMenu;
}
/**
 * Return the Label1 property value.
 * @return java.awt.Label
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Label getRegisterLabel() {
	if (ivjRegisterLabel == null) {
		try {
			ivjRegisterLabel = new java.awt.Label();
			ivjRegisterLabel.setName("RegisterLabel");
			ivjRegisterLabel.setText("Registered POA's:");
			ivjRegisterLabel.setBackground(java.awt.SystemColor.control);
			ivjRegisterLabel.setForeground(java.awt.Color.black);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjRegisterLabel;
}
/**
 * Return the RegisterPanelBorderLayout property value.
 * @return java.awt.BorderLayout
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.BorderLayout getRegisterPanelBorderLayout() {
	java.awt.BorderLayout ivjRegisterPanelBorderLayout = null;
	try {
		/* Create part */
		ivjRegisterPanelBorderLayout = new java.awt.BorderLayout();
		ivjRegisterPanelBorderLayout.setVgap(0);
		ivjRegisterPanelBorderLayout.setHgap(0);
	} catch (java.lang.Throwable ivjExc) {
		handleException(ivjExc);
	};
	return ivjRegisterPanelBorderLayout;
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
	getPOAList().addMouseListener(this);
	getOpenMenuItem().addActionListener(this);
	getCloseMenuItem().addActionListener(this);
	getDestroyMenuItem().addActionListener(this);
}
/**
 * Initialize the class.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initialize() {
	// user code begin {1}
	// user code end
	setName("RegisterPanel");
	setLayout(getRegisterPanelBorderLayout());
	setBackground(java.awt.SystemColor.control);
	setSize(150, 90);
	add(getRegisterLabel(), "North");
	add(getPOAList(), "Center");
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
		RegisterPanel aRegisterPanel;
		aRegisterPanel = new RegisterPanel();
		frame.add("Center", aRegisterPanel);
		frame.setSize(aRegisterPanel.getSize());
		frame.setVisible(true);
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of java.awt.Panel");
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
	if ((e.getSource() == getPOAList()) ) {
		connEtoC1(e);
	}
	// user code begin {2}
	// user code end
}

}



