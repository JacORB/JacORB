package org.jacorb.poa.gui.beans;

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
 
/**
 * A extended PopupMenu
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.0, 05/07/99, RT
 */
public class PopupMenu 
    extends java.awt.PopupMenu 
{
    private java.awt.Component fieldOwnerComponent = null;
    protected transient java.beans.PropertyChangeSupport propertyChange =
        new java.beans.PropertyChangeSupport(this);

    /**
     * Constructor
     */
    public PopupMenu() {
	super();
	initialize();
    }
    /**
     * PopupMenuBean constructor comment.
     * @param label java.lang.String
     */
    public PopupMenu(java.lang.String label) {
	super(label);
    }
    /**
     * Performs the show method.
     * @param mouseEvent java.awt.event.MouseEvent
     */
    public void _show(java.awt.Component c, int x, int y) {
	if (c != null) {
            setOwnerComponent(c);
            c.add(this);
            show(c, x, y);
	}
    }
    /**
     * Performs the show method.
     * @param mouseEvent java.awt.event.MouseEvent
     */
    public void _show(java.awt.event.MouseEvent mouseEvent) {
        //	if (mouseEvent != null && mouseEvent.isPopupTrigger()) {
        java.awt.Component popupOwner = mouseEvent.getComponent();
        if (popupOwner != null) {
            setOwnerComponent(popupOwner);
            popupOwner.add(this);
            show(popupOwner, mouseEvent.getX(), mouseEvent.getY());
        }
        //	}
    }
    /**
     * PopupMenuBean.addPropertyChangeListener method comment.
     */
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
	propertyChange.addPropertyChangeListener(listener);
    }
    /**
     * PopupMenuBean.firePropertyChange method comment.
     */
    public void firePropertyChange(java.lang.String propertyName, java.lang.Object oldValue, java.lang.Object newValue) {
	propertyChange.firePropertyChange(propertyName, oldValue, newValue);
    }
    /**
     * Gets the ownerComponent property (java.awt.Component) value.
     * @return The ownerComponent property value.
     * @see #setOwnerComponent
     */
    public java.awt.Component getOwnerComponent() {
	/* Returns the ownerComponent property value. */
	return fieldOwnerComponent;
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
	// user code begin {2}
	// user code end
    }
    /**
     * main entrypoint - starts the part when it is run as an application
     * @param args java.lang.String[]
     */
    public static void main(java.lang.String[] args) {
	try {
            org.jacorb.poa.gui.beans.PopupMenu aExtendedPopupMenu = 
                new org.jacorb.poa.gui.beans.PopupMenu();
	} catch (Throwable exception) {
            System.err.println("Exception occurred in main() of org.jacorb.poa.gui.beans.PopupMenu");
            exception.printStackTrace(System.out);
	}
    }
    /**
     * PopupMenuBean.removePropertyChangeListener method comment.
     */
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
	propertyChange.removePropertyChangeListener(listener);
    }
    /**
     * Sets the ownerComponent property (java.awt.Component) value.
     * @param ownerComponent The new value for the property.
     * @see #getOwnerComponent
     */
    public void setOwnerComponent(java.awt.Component ownerComponent) {
	/* Get the old property value for fire property change event. */
	java.awt.Component oldValue = fieldOwnerComponent;
	/* Set the ownerComponent property (attribute) to the new value. */
	fieldOwnerComponent = ownerComponent;
	/* Fire (signal/notify) the ownerComponent property change event. */
	firePropertyChange("ownerComponent", oldValue, ownerComponent);
	return;
    }

}






