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

import java.awt.Color;
import java.awt.Label;
import org.jacorb.poa.gui.POAMonitorController;
import org.jacorb.poa.gui.beans.DoubleListDialog;
import org.jacorb.poa.gui.beans.FillLevelBar;
import org.jacorb.poa.gui.beans.MessageDialog;
import org.jacorb.poa.util.POAUtil;
import org.jacorb.poa.util.StringPair;

/**
 * Implements the org.jacorb.poa.gui.POAView interface
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.01, 06/11/99, RT
 */
public class POAFrame
    extends java.awt.Frame
    implements 	org.jacorb.poa.gui.beans.CloseButtonPanelController,
                DetailsButtonController, ObjectListItemController,
                QueueListItemController,
                org.jacorb.poa.gui.POAMonitorView, java.awt.event.WindowListener
{

    private POAMonitorController controller = null;
    private static Color aomBarColor = new java.awt.Color(196,196,0);
    private static Color queueBarColor = new java.awt.Color(0,128,128);
    private static Color activeRequestsBarColor1 = new java.awt.Color(0,128,0);
    private static Color activeRequestsBarColor2 = new java.awt.Color(128,0,0);
    private static Color threadPoolBarColor = new java.awt.Color(0,128,0);
    private String poaName = "";
    private FillLevelBar aomBar = null;
    private FillLevelBar queueBar = null;
    private FillLevelBar activeRequestsBar = null;
    private FillLevelBar threadPoolBar = null;
    private Label stateLabel = null;
    private Label threadLabel = null;
    private Label lifespanLabel = null;
    private Label idUniquenessLabel = null;
    private Label idAssignmentLabel = null;
    private Label servantRetentionLabel = null;
    private Label requestProcessingLabel = null;
    private Label implicitActivationLabel = null;
    private java.awt.FontMetrics fontMetrics1 = null;
    private org.jacorb.poa.gui.beans.CloseButtonPanel ivjButtonPanel = null;
    private org.jacorb.poa.gui.beans.ConsolePanel ivjConsolePanel = null;
    private java.awt.Panel ivjContentsPane = null;
    private POAStatePanel ivjPOAStatePanel = null;
    public void _actionCloseButtonPressed() {
	if (controller != null) {
            controller.actionCloseView();
	}
    }
    public void _actionDeactivateObject(String oidStr) {
	if (controller != null) {
            controller.actionDeactivateObject(oidStr);
	}
    }
    public void _actionDetailsButtonPressed(String source) {
	if (source.equals("aom")) {
            _showAOMDialog();
	} else if (source.equals("queue")) {
            _showQueueDialog();
	} else {
            System.err.println("details unknown source: "+source);
	}
    }
    /**
     * connEtoC1:  (POAFrame.window.windowClosing(java.awt.event.WindowEvent) --> POAFrame.dispose()V)
     * @param arg1 java.awt.event.WindowEvent
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private void connEtoC1(java.awt.event.WindowEvent arg1) {
	try {
            // user code begin {1}
            // user code end
            this._actionCloseButtonPressed();
            // user code begin {2}
            // user code end
	} catch (java.lang.Throwable ivjExc) {
            // user code begin {3}
            // user code end
            handleException(ivjExc);
	}
    }
    public void _destroy() {
	dispose();
    }
    private java.awt.FontMetrics _fontMetrics1() {
	if (fontMetrics1 == null) {
            fontMetrics1 = getPOAStatePanel()._getPolicyPanel()._getThreadLabel().getGraphics().getFontMetrics();
	}
	return fontMetrics1;
    }
    private FillLevelBar _getActiveRequestsBar() {
	if (activeRequestsBar == null) {
            activeRequestsBar = getPOAStatePanel()._getTMPanel()._getActiveRequestsBar();
	}
	return activeRequestsBar;
    }
    private FillLevelBar _getAOMBar() {
	if (aomBar == null) {
            aomBar = getPOAStatePanel()._getAOMPanel()._getAOMBar();
	}
	return aomBar;
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
     * Return the ConsolePanel property value.
     * @return org.jacorb.poa.gui.beans.ConsolePanel
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private org.jacorb.poa.gui.beans.ConsolePanel getConsolePanel() {
	if (ivjConsolePanel == null) {
            try {
                ivjConsolePanel = new org.jacorb.poa.gui.beans.ConsolePanel();
                ivjConsolePanel.setName("ConsolePanel");
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
                ivjContentsPane.setLayout(new java.awt.BorderLayout());
                ivjContentsPane.setBackground(java.awt.SystemColor.control);
                getContentsPane().add(getButtonPanel(), "South");
                getContentsPane().add(getConsolePanel(), "Center");
                getContentsPane().add(getPOAStatePanel(), "North");
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
    private Label _getIdAssignmentLabel() {
	if (idAssignmentLabel == null) {
            idAssignmentLabel = getPOAStatePanel()._getPolicyPanel()._getIdAssignmentLabel();
	}
	return idAssignmentLabel;
    }
    private Label _getIdUniquenessLabel() {
	if (idUniquenessLabel == null) {
            idUniquenessLabel = getPOAStatePanel()._getPolicyPanel()._getIdUniquenessLabel();
	}
	return idUniquenessLabel;
    }
    private Label _getImplicitActivationLabel() {
	if (implicitActivationLabel == null) {
            implicitActivationLabel = getPOAStatePanel()._getPolicyPanel()._getImplicitActivationLabel();
	}
	return implicitActivationLabel;
    }
    private Label _getLifespanLabel() {
	if (lifespanLabel == null) {
            lifespanLabel = getPOAStatePanel()._getPolicyPanel()._getLifespanLabel();
	}
	return lifespanLabel;
    }
    /**
     * Return the POAStatePanel property value.
     * @return org.jacorb.poa.gui.poa.POAStatePanel
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private POAStatePanel getPOAStatePanel() {
	if (ivjPOAStatePanel == null) {
            try {
                ivjPOAStatePanel = new org.jacorb.poa.gui.poa.POAStatePanel();
                ivjPOAStatePanel.setName("POAStatePanel");
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	};
	return ivjPOAStatePanel;
    }
    private FillLevelBar _getQueueBar() {
	if (queueBar == null) {
            queueBar = getPOAStatePanel()._getQueuePanel()._getQueueBar();
	}
	return queueBar;
    }
    private Label _getRequestProcessingLabel() {
	if (requestProcessingLabel == null) {
            requestProcessingLabel = getPOAStatePanel()._getPolicyPanel()._getRequestProcessingLabel();
	}
	return requestProcessingLabel;
    }
    private Label _getServantRetentionLabel() {
	if (servantRetentionLabel == null) {
            servantRetentionLabel = getPOAStatePanel()._getPolicyPanel()._getServantRetentionLabel();
	}
	return servantRetentionLabel;
    }
    private Label _getStateLabel() {
	if (stateLabel == null) {
            stateLabel = getPOAStatePanel()._getStatePanel()._getStateLabel();
	}
	return stateLabel;
    }
    private Label _getThreadLabel() {
	if (threadLabel == null) {
            threadLabel = getPOAStatePanel()._getPolicyPanel()._getThreadLabel();
	}
	return threadLabel;
    }
    private FillLevelBar _getThreadPoolBar() {
	if (threadPoolBar == null) {
            threadPoolBar = getPOAStatePanel()._getTMPanel()._getThreadPoolBar();
	}
	return threadPoolBar;
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
    public void _initActiveRequestsBar(int avg, int max) {
	_getActiveRequestsBar().init(0, avg, max, activeRequestsBarColor1, activeRequestsBarColor2, true, false);
    }
    public void _initAOMBar(int max, boolean isVariable) {
	_getAOMBar().init(0, 0, max, aomBarColor, null, false, isVariable);
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
	setName("POAFrame");
	setLayout(new java.awt.BorderLayout());
	setSize(698, 310);
	setTitle("POA Monitor");
	add(getContentsPane(), "Center");
	initConnections();
	// user code begin {2}
	getButtonPanel()._init(this, "Close");
	getPOAStatePanel()._getAOMPanel()._init(this);
	getPOAStatePanel()._getQueuePanel()._init(this);
	// user code end
    }
    public void _initQueueBar(int max, boolean isVariable) {
	_getQueueBar().init(0, 0, max, queueBarColor, null, false, isVariable);
    }
    public void _initThreadPoolBar(int max) {
	_getThreadPoolBar().init(0, 0, max, threadPoolBarColor, null, false, false);
    }
    public void _inspectRequest(String oidStr) {
	new MessageDialog(this, "Message", "This function is not yet implemented!").setVisible(true);
    }
    public void _inspectServantClass(String oidStr) {
	new MessageDialog(this, "Message", "This function is not yet implemented!").setVisible(true);
    }
    public POAFrame(POAMonitorController _controller) {
	super();
	controller = _controller;
	initialize();
    }
    /**
     * POAFrame constructor comment.
     * @param title java.lang.String
     */
    public POAFrame(String title) {
	super(title);
    }
    /**
     * main entrypoint - starts the part when it is run as an application
     * @param args java.lang.String[]
     */
    public static void main(java.lang.String[] args) {
	try {
            POAFrame aPOAFrame;
            aPOAFrame = new POAFrame();
            try {
                Class aCloserClass = Class.forName("com.ibm.uvm.abt.edit.WindowCloser");
                Class parmTypes[] = { java.awt.Window.class };
                Object parms[] = { aPOAFrame };
                java.lang.reflect.Constructor aCtor = aCloserClass.getConstructor(parmTypes);
                aCtor.newInstance(parms);
            } catch (java.lang.Throwable exc) {};
            aPOAFrame.setVisible(true);
	} catch (Throwable exception) {
            System.err.println("Exception occurred in main() of java.awt.Frame");
            exception.printStackTrace(System.out);
	}
    }
    public void _printMessage(String str) {
	getConsolePanel()._printMessage(str);
    }
    public void _removeRequest(String ridStr) {
	if (controller != null) {
            controller.actionRemoveRequestFromQueue(ridStr);
	}
    }
    public void _setMaxThreadPoolBar(int value) {
	_getThreadPoolBar().setMaxValue(value);
    }
    public void _setName(String str) {
	poaName = str;
	setTitle(poaName+" Monitor ("+_getStateLabel().getText()+")");
    }
    public void _setPolicyIdAssignment(String str) {
	_getIdAssignmentLabel().setText(" "+str);
    }
    public void _setPolicyIdUniqueness(String str) {
	_getIdUniquenessLabel().setText(" "+str);
    }
    public void _setPolicyImplicitActivation(String str) {
	_getImplicitActivationLabel().setText(" "+str);
    }
    public void _setPolicyLifespan(String str) {
	_getLifespanLabel().setText(" "+str);
    }
    public void _setPolicyRequestProcessing(String str) {
	_getRequestProcessingLabel().setText(" "+str);
    }
    public void _setPolicyServantRetention(String str) {
	_getServantRetentionLabel().setText(" "+str);
    }
    public void _setPolicyThread(String str) {
	_getThreadLabel().setText(" "+str);
    }
    public void _setState(String str) {
	_getStateLabel().setText(str);
	setTitle(poaName+" Monitor ("+str+")");
    }
    public void _setValueActiveRequestsBar(int value) {
	_getActiveRequestsBar().setCurrentValue(value);
    }
    public void _setValueAOMBar(int value) {
	_getAOMBar().setCurrentValue(value);
    }
    public void _setValueQueueBar(int value) {
	_getQueueBar().setCurrentValue(value);
    }
    public void _setValueThreadPoolBar(int value) {
	_getThreadPoolBar().setCurrentValue(value);
    }
    public void _setVisible(boolean visible) {
	setVisible(visible);
    }
    private void _showAOMDialog() {

	if (controller == null) return;

	StringPair[] data = controller.actionRetrieveAOMContent();

	DoubleListDialog showDialog = new DoubleListDialog(this, "Active Object Map Snapshot");
	showDialog._setHeaderLabel1("Object ID");
	showDialog._setHeaderLabel2("Servant Class");

	if (data != null) {

            ObjectListItem[] items = new ObjectListItem[data.length];
            String helpStr;
            int firstMax = 100;
            int secondMax = 100;
            int helpInt;
            for (int i=0; i<data.length; i++) {
                items[i] = new ObjectListItem();
                items[i]._init(this, data[i].first);
                helpStr = POAUtil.convert(data[i].first.getBytes());
                items[i]._getFirstLabel().setText(helpStr);
                items[i]._getSecondLabel().setText(data[i].second);

                helpInt = _fontMetrics1().stringWidth(items[i]._getFirstLabel().getText());
                if (helpInt > firstMax) firstMax = helpInt;

                helpInt = _fontMetrics1().stringWidth(items[i]._getSecondLabel().getText());
                if (helpInt > secondMax) secondMax = helpInt;
            }

            showDialog._setSize(firstMax+20, secondMax+20);

            for (int i=0; i<data.length; i++) {
                items[i]._setWidth(firstMax+20, secondMax+20);
                showDialog._addItem(items[i]);
            }
	}

	showDialog.setVisible(true);
    }
    private void _showQueueDialog() {
	if (controller == null) return;

	StringPair[] data = controller.actionRetrieveQueueContent();

	DoubleListDialog showDialog = new DoubleListDialog(this, "Queue Snapshot");
	showDialog._setHeaderLabel1("Request ID");
	showDialog._setHeaderLabel2("Object ID");

	if (data != null) {

            QueueListItem[] items = new QueueListItem[data.length];
            String helpStr;
            int firstMax = 100;
            int secondMax = 100;
            int helpInt;
            for (int i=0; i<data.length; i++) {
                items[i] = new QueueListItem();
                items[i]._init(this, data[i].first);
                items[i]._getFirstLabel().setText(data[i].first);
                helpStr = POAUtil.convert(data[i].second.getBytes());
                items[i]._getSecondLabel().setText(helpStr);

                helpInt = _fontMetrics1().stringWidth(items[i]._getFirstLabel().getText());
                if (helpInt > firstMax) firstMax = helpInt;

                helpInt = _fontMetrics1().stringWidth(items[i]._getSecondLabel().getText());
                if (helpInt > secondMax) secondMax = helpInt;
            }

            showDialog._setSize(firstMax+20, secondMax+20);

            for (int i=0; i<data.length; i++) {
                items[i]._setWidth(firstMax+20, secondMax+20);
                showDialog._addItem(items[i]);
            }
	}

	showDialog.setVisible(true);
    }
    /**
     * Constructor
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    public POAFrame() {
	super();
	initialize();
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
    public void windowDeactivated(java.awt.event.WindowEvent e) {
    }
    /**
     * Method to handle events for the WindowListener interface.
     * @param e java.awt.event.WindowEvent
     */
    public void windowDeiconified(java.awt.event.WindowEvent e) {
    }
    /**
     * Method to handle events for the WindowListener interface.
     * @param e java.awt.event.WindowEvent
     */
    public void windowIconified(java.awt.event.WindowEvent e) {
    }
    /**
     * Method to handle events for the WindowListener interface.
     * @param e java.awt.event.WindowEvent
     */
    public void windowOpened(java.awt.event.WindowEvent e) {
    }
}







