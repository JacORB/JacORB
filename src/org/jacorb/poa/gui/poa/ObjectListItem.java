package org.jacorb.poa.gui.poa;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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
public class ObjectListItem 
    extends java.awt.Panel 
    implements org.jacorb.poa.gui.beans.DoubleListItem, java.awt.event.ActionListener, java.awt.event.MouseListener 
{
    public DoubleListDialog container;
    private ObjectListItemController controller;
    private String oidStr;
    private java.awt.MenuItem ivjDeactivateMenuItem = null;
    private java.awt.MenuItem ivjInspectionMenuItem = null;
    private org.jacorb.poa.gui.beans.PopupMenu ivjPopupMenu = null;
    private java.awt.Panel ivjInnerPanel = null;
    private java.awt.Label ivjObjectLabel = null;
    private java.awt.Label ivjServantLabel = null;
    /**
     * Comment
     */
    private void _actionDeactivateObject() {
	if (controller != null) {
            controller._actionDeactivateObject(oidStr);
	}
    }
    /**
     * Comment
     */
    private void _actionInspectServantClass() {
	if (controller != null) {
            controller._inspectServantClass(oidStr);
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
	if ((e.getSource() == getDeactivateMenuItem()) ) {
            connEtoC4(e);
	}
	if ((e.getSource() == getInspectionMenuItem()) ) {
            connEtoC5(e);
	}
	// user code begin {2}
	// user code end
    }
    /**
     * connEtoC1:  (Label2.mouse.mousePressed(java.awt.event.MouseEvent) --> ObjectListItem._actionMousePressed(Ljava.awt.event.MouseEvent;)V)
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
     * connEtoC2:  (Label1.mouse.mousePressed(java.awt.event.MouseEvent) --> ObjectListItem._actionMousePressed(Ljava.awt.event.MouseEvent;)V)
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
     * connEtoC3:  (Panel1.mouse.mousePressed(java.awt.event.MouseEvent) --> ObjectListItem._actionMousePressed(Ljava.awt.event.MouseEvent;)V)
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
     * connEtoC4:  (DeactivateMenuItem.action.actionPerformed(java.awt.event.ActionEvent) --> ObjectListItem._actionDeactivateObject()V)
     * @param arg1 java.awt.event.ActionEvent
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private void connEtoC4(java.awt.event.ActionEvent arg1) {
	try {
            // user code begin {1}
            // user code end
            this._actionDeactivateObject();
            // user code begin {2}
            // user code end
	} catch (java.lang.Throwable ivjExc) {
            // user code begin {3}
            // user code end
            handleException(ivjExc);
	}
    }
    /**
     * connEtoC5:  (InspectionMenuItem.action.actionPerformed(java.awt.event.ActionEvent) --> ObjectListItem._actionInspectServantClass()V)
     * @param arg1 java.awt.event.ActionEvent
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private void connEtoC5(java.awt.event.ActionEvent arg1) {
	try {
            // user code begin {1}
            // user code end
            this._actionInspectServantClass();
            // user code begin {2}
            // user code end
	} catch (java.lang.Throwable ivjExc) {
            // user code begin {3}
            // user code end
            handleException(ivjExc);
	}
    }
    /**
     * connEtoC6:  (ObjectLabel.mouse.mouseReleased(java.awt.event.MouseEvent) --> ObjectListItem._actionMouseReleased(Ljava.awt.event.MouseEvent;)V)
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
     * connEtoC7:  (InnerPanel.mouse.mouseReleased(java.awt.event.MouseEvent) --> ObjectListItem._actionMouseReleased(Ljava.awt.event.MouseEvent;)V)
     * @param arg1 java.awt.event.MouseEvent
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private void connEtoC7(java.awt.event.MouseEvent arg1) {
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
     * connEtoC8:  (ServantLabel.mouse.mouseReleased(java.awt.event.MouseEvent) --> ObjectListItem._actionMouseReleased(Ljava.awt.event.MouseEvent;)V)
     * @param arg1 java.awt.event.MouseEvent
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private void connEtoC8(java.awt.event.MouseEvent arg1) {
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
    public void _correctWidth(int diff_width) {
        /*	
                if (curr_width + diff_width < init_width) {
		curr_width = init_width;
		curr_width2 = init_width2;
                } else {
		curr_width = curr_width + diff_width;
		curr_width2 = curr_width2 + diff_width;
                }
                setSize(curr_width, 15);
                getInnerPanel().setSize(curr_width, 13);
                getServantLabel().setSize(curr_width2, 13);
        */	
    }
    /**
     * Return the DeactivateMenuItem property value.
     * @return java.awt.MenuItem
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private java.awt.MenuItem getDeactivateMenuItem() {
	if (ivjDeactivateMenuItem == null) {
            try {
                ivjDeactivateMenuItem = new java.awt.MenuItem();
                ivjDeactivateMenuItem.setLabel("Deactivate Object");
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	};
	return ivjDeactivateMenuItem;
    }
    java.awt.Label _getFirstLabel() {
	return getObjectLabel();
    }
    /**
     * Return the Panel1 property value.
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
                getInnerPanel().add(getObjectLabel(), getObjectLabel().getName());
                getInnerPanel().add(getServantLabel(), getServantLabel().getName());
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
                ivjInspectionMenuItem.setLabel("Inspect Servant Class");
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
                ivjObjectLabel.setText("Label1");
                ivjObjectLabel.setBounds(10, 0, 180, 13);
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
                ivjPopupMenu.setLabel("Object Actions");
                ivjPopupMenu.add(getDeactivateMenuItem());
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
    java.awt.Label _getSecondLabel() {
	return getServantLabel();
    }
    /**
     * Return the Label2 property value.
     * @return java.awt.Label
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private java.awt.Label getServantLabel() {
	if (ivjServantLabel == null) {
            try {
                ivjServantLabel = new java.awt.Label();
                ivjServantLabel.setName("ServantLabel");
                ivjServantLabel.setFont(new java.awt.Font("dialog", 0, 10));
                ivjServantLabel.setText("Label2");
                ivjServantLabel.setBounds(200, 0, 990, 13);
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	};
	return ivjServantLabel;
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
	getServantLabel().addMouseListener(this);
	getObjectLabel().addMouseListener(this);
	getInnerPanel().addMouseListener(this);
	getDeactivateMenuItem().addActionListener(this);
	getInspectionMenuItem().addActionListener(this);
    }
    /**
     * Initialize the class.
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private void initialize() {
	// user code begin {1}
	// user code end
	setName("ObjectListItem");
	setLayout(null);
	setBackground(java.awt.Color.black);
	setSize(1200, 15);
	add(getInnerPanel(), getInnerPanel().getName());
	initConnections();
	// user code begin {2}
	// user code end
    }
    void _init(ObjectListItemController _controller, String _oidStr) {
	controller = _controller;
	oidStr = _oidStr;	
    }
    /**
     * ObjectListItem constructor comment.
     * @param layout java.awt.LayoutManager
     */
    public ObjectListItem(java.awt.LayoutManager layout) {
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
            ObjectListItem aObjectListItem;
            aObjectListItem = new ObjectListItem();
            frame.add("Center", aObjectListItem);
            frame.setSize(aObjectListItem.getSize());
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
	if ((e.getSource() == getServantLabel()) ) {
            connEtoC1(e);
	}
	if ((e.getSource() == getObjectLabel()) ) {
            connEtoC2(e);
	}
	if ((e.getSource() == getInnerPanel()) ) {
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
	if ((e.getSource() == getObjectLabel()) ) {
            connEtoC6(e);
	}
	if ((e.getSource() == getInnerPanel()) ) {
            connEtoC7(e);
	}
	if ((e.getSource() == getServantLabel()) ) {
            connEtoC8(e);
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
            getObjectLabel().setBackground(java.awt.Color.darkGray);		
            getServantLabel().setBackground(java.awt.Color.darkGray);		
            getObjectLabel().setForeground(java.awt.Color.white);		
            getServantLabel().setForeground(java.awt.Color.white);		

	} else {
            getInnerPanel().setBackground(java.awt.Color.lightGray);		
            getObjectLabel().setBackground(java.awt.Color.lightGray);		
            getServantLabel().setBackground(java.awt.Color.lightGray);		
            getObjectLabel().setForeground(java.awt.Color.black);		
            getServantLabel().setForeground(java.awt.Color.black);		
	}
    }
    void _setWidth(int w1, int w2) {
	getObjectLabel().setSize(w1, 13);
	getServantLabel().setBounds(20+w1, 0, 1200-(w1+20), 13);
        /*	
                init_width2 = curr_width2 = w2;
                init_width = curr_width = 10+w1+10+w2+10	+500;
                setSize(init_width, 15);
                getInnerPanel().setSize(init_width, 13);
                getObjectLabel().setSize(w1, 13);	
                getServantLabel().setBounds(20+w1, 0, w2	+500, 13);
        */	
    }
    /**
     * Constructor
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    public ObjectListItem() {
	super();
	initialize();
    }
}







