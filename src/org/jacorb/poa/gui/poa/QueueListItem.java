package org.jacorb.poa.gui.poa;

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
 
import org.jacorb.poa.gui.beans.DoubleListDialog;

/**
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.0, 05/10/99, RT
 */
public class QueueListItem 
    extends java.awt.Panel 
    implements org.jacorb.poa.gui.beans.DoubleListItem, java.awt.event.ActionListener, java.awt.event.MouseListener 
{
    private DoubleListDialog container;
    private QueueListItemController controller;
    private String ridStr;
    private java.awt.Panel ivjInnerPanel = null;
    private java.awt.Label ivjRequestLabel = null;
    private java.awt.Label ivjObjectLabel = null;
    private java.awt.MenuItem ivjInspectionMenuItem = null;
    private org.jacorb.poa.gui.beans.PopupMenu ivjPopupMenu = null;
    private java.awt.MenuItem ivjRemoveMenuItem = null;
    /**
     * Comment
     */
    private void _actionInspectRequest() {
	if (controller != null) {
            controller._inspectRequest(ridStr);
	}
    }
    /**
     * Comment
     */
    private void _actionMousePressed(java.awt.event.MouseEvent e) {
	if (container != null) {
            container._setSelectedItem(this);
	}
    }
    /**
     * Comment
     */
    private void _actionMouseReleased(java.awt.event.MouseEvent e) {
	int mods = e.getModifiers();
	if ((mods & java.awt.event.MouseEvent.BUTTON3_MASK) != 0) {
            /* Right Mouse Button pressed */
            getPopupMenu()._show(e);
	} else if ((mods & java.awt.event.MouseEvent.BUTTON2_MASK) != 0) {
            /* Middle Mouse Button pressed */
	} else {
            /* Left Mouse Button pressed */
            if (e.getClickCount() > 1) {
                /* doubel click */
            }
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
	if ((e.getSource() == getRemoveMenuItem()) ) {
            connEtoC7(e);
	}
	if ((e.getSource() == getInspectionMenuItem()) ) {
            connEtoC8(e);
	}
	// user code begin {2}
	// user code end
    }
    /**
     * Comment
     */
    private void _actionRemoveRequest() {
	if (controller != null) {
            controller._removeRequest(ridStr);
	}
    }
    /**
     * connEtoC1:  (InnerPanel.mouse.mousePressed(java.awt.event.MouseEvent) --> QueueListItem._actionMousePressed(Ljava.awt.event.MouseEvent;)V)
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
     * connEtoC2:  (RequestLabel.mouse.mousePressed(java.awt.event.MouseEvent) --> QueueListItem._actionMousePressed(Ljava.awt.event.MouseEvent;)V)
     * @param arg1 java.awt.event.MouseEvent
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private void connEtoC2(java.awt.event.MouseEvent arg1) {
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
     * connEtoC3:  (ObjectLabel.mouse.mousePressed(java.awt.event.MouseEvent) --> QueueListItem._actionMousePressed(Ljava.awt.event.MouseEvent;)V)
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
     * connEtoC4:  (InnerPanel.mouse.mouseReleased(java.awt.event.MouseEvent) --> QueueListItem._actionMouseReleased(Ljava.awt.event.MouseEvent;)V)
     * @param arg1 java.awt.event.MouseEvent
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private void connEtoC4(java.awt.event.MouseEvent arg1) {
	try {
            // user code begin {1}
            // user code end
            this._actionMouseReleased(arg1);
            // user code begin {2}
            // user code end
	} catch (java.lang.Throwable ivjExc) {
            // user code begin {3}
            // user code end
            handleException(ivjExc);
	}
    }
    /**
     * connEtoC5:  (RequestLabel.mouse.mouseReleased(java.awt.event.MouseEvent) --> QueueListItem._actionMouseReleased(Ljava.awt.event.MouseEvent;)V)
     * @param arg1 java.awt.event.MouseEvent
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private void connEtoC5(java.awt.event.MouseEvent arg1) {
	try {
            // user code begin {1}
            // user code end
            this._actionMouseReleased(arg1);
            // user code begin {2}
            // user code end
	} catch (java.lang.Throwable ivjExc) {
            // user code begin {3}
            // user code end
            handleException(ivjExc);
	}
    }
    /**
     * connEtoC6:  (ObjectLabel.mouse.mouseReleased(java.awt.event.MouseEvent) --> QueueListItem._actionMouseReleased(Ljava.awt.event.MouseEvent;)V)
     * @param arg1 java.awt.event.MouseEvent
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private void connEtoC6(java.awt.event.MouseEvent arg1) {
	try {
            // user code begin {1}
            // user code end
            this._actionMouseReleased(arg1);
            // user code begin {2}
            // user code end
	} catch (java.lang.Throwable ivjExc) {
            // user code begin {3}
            // user code end
            handleException(ivjExc);
	}
    }
    /**
     * connEtoC7:  (RemoveMenuItem.action.actionPerformed(java.awt.event.ActionEvent) --> QueueListItem._actionRemoveRequest()V)
     * @param arg1 java.awt.event.ActionEvent
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private void connEtoC7(java.awt.event.ActionEvent arg1) {
	try {
            // user code begin {1}
            // user code end
            this._actionRemoveRequest();
            // user code begin {2}
            // user code end
	} catch (java.lang.Throwable ivjExc) {
            // user code begin {3}
            // user code end
            handleException(ivjExc);
	}
    }
    /**
     * connEtoC8:  (InspectionMenuItem.action.actionPerformed(java.awt.event.ActionEvent) --> QueueListItem._actionInspectRequest()V)
     * @param arg1 java.awt.event.ActionEvent
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private void connEtoC8(java.awt.event.ActionEvent arg1) {
	try {
            // user code begin {1}
            // user code end
            this._actionInspectRequest();
            // user code begin {2}
            // user code end
	} catch (java.lang.Throwable ivjExc) {
            // user code begin {3}
            // user code end
            handleException(ivjExc);
	}
    }
    public void _correctWidth(int diff_width) {
    }
    java.awt.Label _getFirstLabel() {
	return getRequestLabel();
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
                ivjInnerPanel.setLayout(null);
                ivjInnerPanel.setBackground(java.awt.Color.lightGray);
                ivjInnerPanel.setBounds(0, 1, 1200, 13);
                getInnerPanel().add(getRequestLabel(), getRequestLabel().getName());
                getInnerPanel().add(getObjectLabel(), getObjectLabel().getName());
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
     * Return the InspectionMenuItem property value.
     * @return java.awt.MenuItem
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private java.awt.MenuItem getInspectionMenuItem() {
	if (ivjInspectionMenuItem == null) {
            try {
                ivjInspectionMenuItem = new java.awt.MenuItem();
                ivjInspectionMenuItem.setEnabled(true);
                ivjInspectionMenuItem.setLabel("Inspect Request Object");
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	};
	return ivjInspectionMenuItem;
    }
    /**
     * Return the Label1 property value.
     * @return java.awt.Label
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private java.awt.Label getObjectLabel() {
	if (ivjObjectLabel == null) {
            try {
                ivjObjectLabel = new java.awt.Label();
                ivjObjectLabel.setName("ObjectLabel");
                ivjObjectLabel.setFont(new java.awt.Font("dialog", 0, 10));
                ivjObjectLabel.setText("Label2");
                ivjObjectLabel.setBounds(200, 0, 990, 13);
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	};
	return ivjObjectLabel;
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
                ivjPopupMenu.setLabel("Request Actions");
                ivjPopupMenu.add(getRemoveMenuItem());
                ivjPopupMenu.add(getInspectionMenuItem());
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
     * Return the RemoveMenuItem property value.
     * @return java.awt.MenuItem
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private java.awt.MenuItem getRemoveMenuItem() {
	if (ivjRemoveMenuItem == null) {
            try {
                ivjRemoveMenuItem = new java.awt.MenuItem();
                ivjRemoveMenuItem.setLabel("Remove Request from Queue");
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	};
	return ivjRemoveMenuItem;
    }
    /**
     * Return the RequestLabel property value.
     * @return java.awt.Label
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private java.awt.Label getRequestLabel() {
	if (ivjRequestLabel == null) {
            try {
                ivjRequestLabel = new java.awt.Label();
                ivjRequestLabel.setName("RequestLabel");
                ivjRequestLabel.setFont(new java.awt.Font("dialog", 0, 10));
                ivjRequestLabel.setText("Label1");
                ivjRequestLabel.setBounds(10, 0, 180, 13);
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	};
	return ivjRequestLabel;
    }
    java.awt.Label _getSecondLabel() {
	return getObjectLabel();
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
	getInnerPanel().addMouseListener(this);
	getRequestLabel().addMouseListener(this);
	getObjectLabel().addMouseListener(this);
	getRemoveMenuItem().addActionListener(this);
	getInspectionMenuItem().addActionListener(this);
    }
    /**
     * Initialize the class.
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private void initialize() {
	// user code begin {1}
	// user code end
	setName("QueueListItem");
	setLayout(null);
	setBackground(java.awt.Color.black);
	setSize(1200, 15);
	add(getInnerPanel(), getInnerPanel().getName());
	initConnections();
	// user code begin {2}
	// user code end
    }
    void _init(QueueListItemController _controller, String _ridStr) {
	controller = _controller;
	ridStr = _ridStr;	
    }
    /**
     * QueueListItem constructor comment.
     * @param layout java.awt.LayoutManager
     */
    public QueueListItem(java.awt.LayoutManager layout) {
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
            QueueListItem aQueueListItem;
            aQueueListItem = new QueueListItem();
            frame.add("Center", aQueueListItem);
            frame.setSize(aQueueListItem.getSize());
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
	if ((e.getSource() == getInnerPanel()) ) {
            connEtoC1(e);
	}
	if ((e.getSource() == getRequestLabel()) ) {
            connEtoC2(e);
	}
	if ((e.getSource() == getObjectLabel()) ) {
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
	if ((e.getSource() == getInnerPanel()) ) {
            connEtoC4(e);
	}
	if ((e.getSource() == getRequestLabel()) ) {
            connEtoC5(e);
	}
	if ((e.getSource() == getObjectLabel()) ) {
            connEtoC6(e);
	}
	// user code begin {2}
	// user code end
    }
    public void _setContainer(DoubleListDialog _container) {
	container = _container;
    }
    public void _setSelected(boolean selected) {
	if (selected) {
            getInnerPanel().setBackground(java.awt.Color.darkGray);		
            getRequestLabel().setBackground(java.awt.Color.darkGray);		
            getObjectLabel().setBackground(java.awt.Color.darkGray);		
            getRequestLabel().setForeground(java.awt.Color.white);		
            getObjectLabel().setForeground(java.awt.Color.white);		

	} else {
            getInnerPanel().setBackground(java.awt.Color.lightGray);		
            getRequestLabel().setBackground(java.awt.Color.lightGray);		
            getObjectLabel().setBackground(java.awt.Color.lightGray);		
            getRequestLabel().setForeground(java.awt.Color.black);		
            getObjectLabel().setForeground(java.awt.Color.black);		
	}
    }
    void _setWidth(int w1, int w2) {
	getRequestLabel().setSize(w1, 13);
	getObjectLabel().setBounds(20+w1, 0, 1200-(w1+20), 13);
    }
    /**
     * Constructor
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    public QueueListItem() {
	super();
	initialize();
    }
}










