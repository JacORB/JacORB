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
 * @version 1.01, 05/07/99, RT
 */
public class AOMPanel extends java.awt.Panel implements java.awt.event.ActionListener {
    private DetailsButtonController controller;
	
    private java.awt.Button ivjDetailsButton = null;
    private org.jacorb.poa.gui.beans.FillLevelBar ivjFillLevelBar = null;
    private java.awt.Label ivjLabel1 = null;
    private java.awt.Label ivjLabel2 = null;
    /**
     * Comment
     */
    private void _actionDetailsButtonPressed() {
	if (controller != null) {
            controller._actionDetailsButtonPressed("aom");
	}
    }
    /**
     * Method to handle events for the ActionListener interface.
     * @param e java.awt.event.ActionEvent
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    public void actionPerformed(java.awt.event.ActionEvent e) {
	// user code begin {1}
	// user code end
	if ((e.getSource() == getDetailsButton()) ) {
            connEtoC1(e);
	}
	// user code begin {2}
	// user code end
    }
    /**
     * connEtoC1:  (DetailsButton.action.actionPerformed(java.awt.event.ActionEvent) --> AOMPanel.detailsButtonPressed()V)
     * @param arg1 java.awt.event.ActionEvent
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private void connEtoC1(java.awt.event.ActionEvent arg1) {
	try {
            // user code begin {1}
            // user code end
            this._actionDetailsButtonPressed();
            // user code begin {2}
            // user code end
	} catch (java.lang.Throwable ivjExc) {
            // user code begin {3}
            // user code end
            handleException(ivjExc);
	}
    }
    FillLevelBar _getAOMBar() {
	return getFillLevelBar();
    }
    /**
     * Return the DetailsButton property value.
     * @return java.awt.Button
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private java.awt.Button getDetailsButton() {
	if (ivjDetailsButton == null) {
            try {
                ivjDetailsButton = new java.awt.Button();
                ivjDetailsButton.setName("DetailsButton");
                ivjDetailsButton.setLocation(33, 170);
                ivjDetailsButton.setBackground(java.awt.SystemColor.control);
                ivjDetailsButton.setSize(23, 23);
                ivjDetailsButton.setFont(new java.awt.Font("dialog", 0, 8));			
                ivjDetailsButton.setActionCommand("...");
                ivjDetailsButton.setLabel("...");
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	};
	return ivjDetailsButton;
    }
    /**
     * Return the FillLevelBar property value.
     * @return org.jacorb.poa.gui.beans.FillLevelBar
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private org.jacorb.poa.gui.beans.FillLevelBar getFillLevelBar() {
	if (ivjFillLevelBar == null) {
            try {
                ivjFillLevelBar = new org.jacorb.poa.gui.beans.FillLevelBar();
                ivjFillLevelBar.setName("FillLevelBar");
                ivjFillLevelBar.setLocation(32, 35);
                ivjFillLevelBar.setBackground(java.awt.SystemColor.control);
                ivjFillLevelBar.setSize(50, 130);
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	};
	return ivjFillLevelBar;
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
                ivjLabel1.setLocation(10, 10);
                ivjLabel1.setText("Active Object");
                ivjLabel1.setBackground(java.awt.SystemColor.control);
                ivjLabel1.setSize(70, 12);
                ivjLabel1.setForeground(java.awt.Color.black);
                ivjLabel1.setFont(new java.awt.Font("dialog", 2, 10));
                ivjLabel1.setAlignment(1);
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
                ivjLabel2.setLocation(10, 22);
                ivjLabel2.setText("Map");
                ivjLabel2.setBackground(java.awt.SystemColor.control);
                ivjLabel2.setSize(70, 12);
                ivjLabel2.setForeground(java.awt.Color.black);
                ivjLabel2.setFont(new java.awt.Font("dialog", 2, 10));
                ivjLabel2.setAlignment(1);
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
	getDetailsButton().addActionListener(this);
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
	setSize(90, 200);
	add(getLabel1(), getLabel1().getName());
	add(getLabel2(), getLabel2().getName());
	add(getFillLevelBar(), getFillLevelBar().getName());
	add(getDetailsButton(), getDetailsButton().getName());
	initConnections();
	// user code begin {2}
	// user code end
    }
    void _init(DetailsButtonController _controller) {
	controller = _controller;
    }
    /**
     * AOMPanel constructor comment.
     * @param layout java.awt.LayoutManager
     */
    public AOMPanel(java.awt.LayoutManager layout) {
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
            AOMPanel aAOMPanel;
            aAOMPanel = new AOMPanel();
            frame.add("Center", aAOMPanel);
            frame.setSize(aAOMPanel.getSize());
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
    public AOMPanel() {
	super();
	initialize();
    }
}







