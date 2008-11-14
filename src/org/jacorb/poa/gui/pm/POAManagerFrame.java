package org.jacorb.poa.gui.pm;

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

import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.List;
import org.jacorb.poa.gui.POAManagerMonitorController;

/**
 * Implements the org.jacorb.poa.gui.POAManagerView interface
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.03, 06/11/99, RT
 */
public class POAManagerFrame extends java.awt.Frame implements org.jacorb.poa.gui.beans.CloseButtonPanelController, RegisterPanelController, StatePanelController, org.jacorb.poa.gui.POAManagerMonitorView, java.awt.event.WindowListener {
    private POAManagerMonitorController controller;
    private String [] stateChoiceItems = { "Active", "Holding", "Discarding", "Inactive"};
    private String currentStateItem = stateChoiceItems[1];
    private List poaList = null;
    private Choice stateChoice = null;
    private Checkbox waitCheckbox = null;
    private Checkbox etherializeCheckbox = null;
    private java.awt.Panel ivjContentsPane = null;
    private org.jacorb.poa.gui.beans.CloseButtonPanel ivjButtonPanel = null;
    private POAManagerStatePanel ivjPOAManagerStatePanel = null;
    private org.jacorb.poa.gui.beans.ConsolePanel ivjConsolePanel = null;

/**
 * Constructor
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public POAManagerFrame() {
    super();
    initialize();
}
/**
 * This method was created in VisualAge.
 */
public POAManagerFrame(POAManagerMonitorController _controller) {
    super();
    controller = _controller;
    initialize();
}
/**
 * POAManagerFrame constructor comment.
 * @param title java.lang.String
 */
public POAManagerFrame(String title) {
    super(title);
}
public void _actionCloseButtonPressed() {
    if (controller != null) {
        controller.actionCloseView();
    }
}
public void _actionClosePOAMonitor(String name) {
    if (controller != null) {
        controller.actionClosePOAMonitor(name);
    }
}
public void _actionDestroyPOA(String name) {
    if (controller != null) {
        controller.actionDestroyPOA(name);
    }
}
public void _actionOpenPOAMonitor(String name) {
    if (controller != null) {
        controller.actionOpenPOAMonitor(name);
    }
}
public void _addPOA(String name) {
    _getPOAList().add(name);
}
public void _destroy() {
    dispose();
}
private Checkbox _getEtherializeCheckbox() {
    if (etherializeCheckbox == null) {
        etherializeCheckbox = getPOAManagerStatePanel()._getStatePanel()._getEtherializeCheckbox();
    }
    return etherializeCheckbox;
}
private List _getPOAList() {
    if (poaList == null) {
        poaList = getPOAManagerStatePanel()._getRegisterPanel()._getPOAList();
    }
    return poaList;
}
private Choice _getStateChoice() {
    if (stateChoice == null) {
        stateChoice = getPOAManagerStatePanel()._getStatePanel()._getStateChoice();
    }
    return stateChoice;
}
private Checkbox _getWaitCheckbox() {
    if (waitCheckbox == null) {
        waitCheckbox = getPOAManagerStatePanel()._getStatePanel()._getWaitCheckbox();
    }
    return waitCheckbox;
}
public void _printMessage(String str) {
    getConsolePanel()._printMessage(str);
}
public void _removePOA(String name) {
    _getPOAList().remove(name);
}
public void _resetState() {
    _getStateChoice().select(currentStateItem);
}
public void _setToActive() {
    currentStateItem = stateChoiceItems[0];
    _getStateChoice().select(currentStateItem);
    _getWaitCheckbox().setState(false);
    _getEtherializeCheckbox().setState(false);
}
public void _setToDiscarding(boolean wait) {
    currentStateItem = stateChoiceItems[2];
    _getStateChoice().select(currentStateItem);
    _getWaitCheckbox().setState(wait);
    _getEtherializeCheckbox().setState(false);
}
public void _setToHolding(boolean wait) {
    currentStateItem = stateChoiceItems[1];
    _getStateChoice().select(currentStateItem);
    _getWaitCheckbox().setState(wait);
    _getEtherializeCheckbox().setState(false);
}
public void _setToInactive(boolean wait, boolean etherialize) {
    currentStateItem = stateChoiceItems[3];
    _getStateChoice().select(currentStateItem);
    _getWaitCheckbox().setState(wait);
    _getEtherializeCheckbox().setState(etherialize);
}
public void _setVisible(boolean visible) {
    setVisible(visible);
}
public void _stateItemChanged(String item, boolean wait, boolean etherialize) {

    if (item.equals(currentStateItem)) return;

    if (controller != null) {

        if (item.equals(stateChoiceItems[0])) {
            controller.actionSetToActive();

        } else if (item.equals(stateChoiceItems[1])) {
            controller.actionSetToHolding(wait);

        } else if (item.equals(stateChoiceItems[2])) {
            controller.actionSetToDiscarding(wait);

        } else if (item.equals(stateChoiceItems[3])) {
            controller.actionSetToInactive(wait, etherialize);

        }
    }
}
/**
 * connEtoC1:  (POAManagerFrame.window.windowClosing(java.awt.event.WindowEvent) --> POAManagerFrame._closeButtonPressed()V)
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
private org.jacorb.poa.gui.beans.CloseButtonPanel getButtonPanel() {
    if (ivjButtonPanel == null) {
        try {
            ivjButtonPanel = new org.jacorb.poa.gui.beans.CloseButtonPanel();
            ivjButtonPanel.setName("ButtonPanel");
            ivjButtonPanel.setBackground(java.awt.SystemColor.control);
            ivjButtonPanel.setSize(427, 34);
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
 * Return the Console property value.
 * @return org.jacorb.poa.gui.beans.ConsolePanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private org.jacorb.poa.gui.beans.ConsolePanel getConsolePanel() {
    if (ivjConsolePanel == null) {
        try {
            ivjConsolePanel = new org.jacorb.poa.gui.beans.ConsolePanel();
            ivjConsolePanel.setName("ConsolePanel");
            ivjConsolePanel.setBackground(java.awt.SystemColor.control);
            ivjConsolePanel.setSize(426, 240);
            // user code begin {1}
            // user code end
        } catch (java.lang.Throwable ivjExc) {
            // user code begin {2}
            // user code end
            handleException(ivjExc);
        }
    };
    return ivjConsolePanel;
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
            ivjContentsPane.setLayout(getContentsPaneBorderLayout());
            ivjContentsPane.setBackground(java.awt.SystemColor.control);
            getContentsPane().add(getButtonPanel(), "South");
            getContentsPane().add(getConsolePanel(), "Center");
            getContentsPane().add(getPOAManagerStatePanel(), "North");
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
 * Return the ContentsPaneBorderLayout property value.
 * @return java.awt.BorderLayout
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.BorderLayout getContentsPaneBorderLayout() {
    java.awt.BorderLayout ivjContentsPaneBorderLayout = null;
    try {
        /* Create part */
        ivjContentsPaneBorderLayout = new java.awt.BorderLayout();
        ivjContentsPaneBorderLayout.setVgap(0);
        ivjContentsPaneBorderLayout.setHgap(0);
    } catch (java.lang.Throwable ivjExc) {
        handleException(ivjExc);
    };
    return ivjContentsPaneBorderLayout;
}
/**
 * Return the POAManagerFrameBorderLayout property value.
 * @return java.awt.BorderLayout
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.BorderLayout getPOAManagerFrameBorderLayout() {
    java.awt.BorderLayout ivjPOAManagerFrameBorderLayout = null;
    try {
        /* Create part */
        ivjPOAManagerFrameBorderLayout = new java.awt.BorderLayout();
        ivjPOAManagerFrameBorderLayout.setVgap(0);
        ivjPOAManagerFrameBorderLayout.setHgap(0);
    } catch (java.lang.Throwable ivjExc) {
        handleException(ivjExc);
    };
    return ivjPOAManagerFrameBorderLayout;
}
/**
 * Return the POAManagerStatePanel property value.
 * @return org.jacorb.poa.gui.pm.POAManagerStatePanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private POAManagerStatePanel getPOAManagerStatePanel() {
    if (ivjPOAManagerStatePanel == null) {
        try {
            ivjPOAManagerStatePanel = new org.jacorb.poa.gui.pm.POAManagerStatePanel();
            ivjPOAManagerStatePanel.setName("POAManagerStatePanel");
            ivjPOAManagerStatePanel.setBackground(java.awt.SystemColor.control);
            ivjPOAManagerStatePanel.setSize(310, 100);
            // user code begin {1}
            // user code end
        } catch (java.lang.Throwable ivjExc) {
            // user code begin {2}
            // user code end
            handleException(ivjExc);
        }
    };
    return ivjPOAManagerStatePanel;
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
    setName("POAManagerFrame");
    setLayout(getPOAManagerFrameBorderLayout());
    setBackground(java.awt.SystemColor.control);
    setSize(318, 210);
    setTitle("POAManager Monitor");
    add(getContentsPane(), "Center");
    initConnections();
    // user code begin {2}
    for (int i=0; i<stateChoiceItems.length; i++) {
        _getStateChoice().addItem(stateChoiceItems[i]);
    }
    _getStateChoice().select(stateChoiceItems[1]);
    getPOAManagerStatePanel()._getStatePanel()._init(this);
    getPOAManagerStatePanel()._getRegisterPanel()._init(this);
    getButtonPanel()._init(this, "Close");
    // user code end
}
/**
 * main entrypoint - starts the part when it is run as an application
 * @param args java.lang.String[]
 */
public static void main(java.lang.String[] args) {
    try {
        POAManagerFrame aPOAManagerFrame;
        aPOAManagerFrame = new POAManagerFrame();
        try {
            Class aCloserClass = Class.forName("com.ibm.uvm.abt.edit.WindowCloser");
            Class parmTypes[] = { java.awt.Window.class };
            Object parms[] = { aPOAManagerFrame };
            java.lang.reflect.Constructor aCtor = aCloserClass.getConstructor(parmTypes);
            aCtor.newInstance(parms);
        } catch (java.lang.Throwable exc) {};
        aPOAManagerFrame.setVisible(true);
    } catch (Throwable exception) {
        System.err.println("Exception occurred in main() of java.awt.Frame");
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









