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
 
/**
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.0, 05/03/99, RT
 */
public class POAStatePanel extends java.awt.Panel {
    private AOMPanel ivjAOMPanel = null;
    private PolicyPanel ivjPolicyPanel = null;
    private QueuePanel ivjQueuePanel = null;
    private StatePanel ivjStatePanel = null;
    private TMPanel ivjTMPanel = null;
    AOMPanel _getAOMPanel() {
	return getAOMPanel();
    }
    /**
     * Return the AOMPanel property value.
     * @return org.jacorb.poa.gui.poa.AOMPanel
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private AOMPanel getAOMPanel() {
	if (ivjAOMPanel == null) {
            try {
                ivjAOMPanel = new org.jacorb.poa.gui.poa.AOMPanel();
                ivjAOMPanel.setName("AOMPanel");
                ivjAOMPanel.setLocation(10, 10);
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	};
	return ivjAOMPanel;
    }
    PolicyPanel _getPolicyPanel() {
	return getPolicyPanel();
    }
    /**
     * Return the PolicyPanel property value.
     * @return org.jacorb.poa.gui.poa.PolicyPanel
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private PolicyPanel getPolicyPanel() {
	if (ivjPolicyPanel == null) {
            try {
                ivjPolicyPanel = new org.jacorb.poa.gui.poa.PolicyPanel();
                ivjPolicyPanel.setName("PolicyPanel");
                ivjPolicyPanel.setLocation(380, 70);
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	};
	return ivjPolicyPanel;
    }
    QueuePanel _getQueuePanel() {
	return getQueuePanel();
    }
    /**
     * Return the QueuePanel property value.
     * @return org.jacorb.poa.gui.poa.QueuePanel
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private QueuePanel getQueuePanel() {
	if (ivjQueuePanel == null) {
            try {
                ivjQueuePanel = new org.jacorb.poa.gui.poa.QueuePanel();
                ivjQueuePanel.setName("QueuePanel");
                ivjQueuePanel.setLocation(110, 10);
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	};
	return ivjQueuePanel;
    }
    StatePanel _getStatePanel() {
	return getStatePanel();
    }
    /**
     * Return the StatePanel property value.
     * @return org.jacorb.poa.gui.poa.StatePanel
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private StatePanel getStatePanel() {
	if (ivjStatePanel == null) {
            try {
                ivjStatePanel = new org.jacorb.poa.gui.poa.StatePanel();
                ivjStatePanel.setName("StatePanel");
                ivjStatePanel.setLocation(380, 10);
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
    TMPanel _getTMPanel() {
	return getTMPanel();
    }
    /**
     * Return the TMPanel property value.
     * @return org.jacorb.poa.gui.poa.TMPanel
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private TMPanel getTMPanel() {
	if (ivjTMPanel == null) {
            try {
                ivjTMPanel = new org.jacorb.poa.gui.poa.TMPanel();
                ivjTMPanel.setName("TMPanel");
                ivjTMPanel.setLocation(210, 10);
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	};
	return ivjTMPanel;
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
	setName("POAStatePanel");
	setLayout(null);
	setBackground(java.awt.SystemColor.control);
	setSize(690, 220);
	add(getAOMPanel(), getAOMPanel().getName());
	add(getQueuePanel(), getQueuePanel().getName());
	add(getTMPanel(), getTMPanel().getName());
	add(getPolicyPanel(), getPolicyPanel().getName());
	add(getStatePanel(), getStatePanel().getName());
	// user code begin {2}
	// user code end
    }
    /**
     * POAStatePanel constructor comment.
     * @param layout java.awt.LayoutManager
     */
    public POAStatePanel(java.awt.LayoutManager layout) {
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
            POAStatePanel aPOAStatePanel;
            aPOAStatePanel = new POAStatePanel();
            frame.add("Center", aPOAStatePanel);
            frame.setSize(aPOAStatePanel.getSize());
            frame.setVisible(true);
	} catch (Throwable exception) {
            System.err.println("Exception occurred in main() of java.awt.Panel");
            exception.printStackTrace(System.out);
	}
    }
    /**
     * Constructor
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    public POAStatePanel() {
	super();
	initialize();
    }
}







