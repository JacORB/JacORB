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
 
import java.awt.Label;

/**
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.0, 05/03/99, RT
 */
public class PolicyPanel extends java.awt.Panel {
    private java.awt.Label ivjIdAssignmentNameLabel = null;
    private java.awt.Label ivjIdAssignmentValueLabel = null;
    private java.awt.Label ivjIdUniquenessNameLabel = null;
    private java.awt.Label ivjIdUniquenessValueLabel = null;
    private java.awt.Label ivjImplicitActivationNameLabel = null;
    private java.awt.Label ivjImplicitActivationValueLabel = null;
    private java.awt.Label ivjLifespanNameLabel = null;
    private java.awt.Label ivjLifespanValueLabel = null;
    private java.awt.Label ivjRequestProcessingNameLabel = null;
    private java.awt.Label ivjRequestProcessingValueLabel = null;
    private java.awt.Label ivjServantRetentionNameLabel = null;
    private java.awt.Label ivjServantRetentionValueLabel = null;
    private java.awt.Panel ivjTablePanel = null;
    private java.awt.Label ivjThreadNameLabel = null;
    private java.awt.Label ivjThreadValueLabel = null;
    private java.awt.Label ivjTitleLabel = null;
    Label _getIdAssignmentLabel() {
	return getIdAssignmentValueLabel();
    }
    /**
     * Return the IdAssignmentNameLabel property value.
     * @return java.awt.Label
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private java.awt.Label getIdAssignmentNameLabel() {
	if (ivjIdAssignmentNameLabel == null) {
            try {
                ivjIdAssignmentNameLabel = new java.awt.Label();
                ivjIdAssignmentNameLabel.setName("IdAssignmentNameLabel");
                ivjIdAssignmentNameLabel.setLocation(new java.awt.Point(1, 40));
                ivjIdAssignmentNameLabel.setText(" ID Assignment");
                ivjIdAssignmentNameLabel.setBackground(java.awt.SystemColor.control);
                ivjIdAssignmentNameLabel.setFont(new java.awt.Font("dialog", 2, 10));
                ivjIdAssignmentNameLabel.setBounds(new java.awt.Rectangle(1, 40, 100, 12));
                ivjIdAssignmentNameLabel.setBounds(1, 40, 100, 12);
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	}
	return ivjIdAssignmentNameLabel;
    }
    /**
     * Return the IdAssignmentValueLabel property value.
     * @return java.awt.Label
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private java.awt.Label getIdAssignmentValueLabel() {
	if (ivjIdAssignmentValueLabel == null) {
            try {
                ivjIdAssignmentValueLabel = new java.awt.Label();
                ivjIdAssignmentValueLabel.setName("IdAssignmentValueLabel");
                ivjIdAssignmentValueLabel.setLocation(new java.awt.Point(102, 40));
                ivjIdAssignmentValueLabel.setText(" SYSTEM_ID");
                ivjIdAssignmentValueLabel.setBackground(java.awt.SystemColor.control);
                ivjIdAssignmentValueLabel.setFont(new java.awt.Font("dialog", 0, 10));
                ivjIdAssignmentValueLabel.setBounds(new java.awt.Rectangle(102, 40, 179, 12));
                ivjIdAssignmentValueLabel.setBounds(102, 40, 179, 12);
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	}
	return ivjIdAssignmentValueLabel;
    }
    Label _getIdUniquenessLabel() {
	return getIdUniquenessValueLabel();
    }
    /**
     * Return the IdUniquenessNameLabel property value.
     * @return java.awt.Label
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private java.awt.Label getIdUniquenessNameLabel() {
	if (ivjIdUniquenessNameLabel == null) {
            try {
                ivjIdUniquenessNameLabel = new java.awt.Label();
                ivjIdUniquenessNameLabel.setName("IdUniquenessNameLabel");
                ivjIdUniquenessNameLabel.setLocation(new java.awt.Point(1, 27));
                ivjIdUniquenessNameLabel.setText(" ID Uniqueness");
                ivjIdUniquenessNameLabel.setBackground(java.awt.SystemColor.control);
                ivjIdUniquenessNameLabel.setFont(new java.awt.Font("dialog", 2, 10));
                ivjIdUniquenessNameLabel.setBounds(new java.awt.Rectangle(1, 27, 100, 12));
                ivjIdUniquenessNameLabel.setBounds(1, 27, 100, 12);
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	}
	return ivjIdUniquenessNameLabel;
    }
    /**
     * Return the IdUniquenessValueLabel property value.
     * @return java.awt.Label
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private java.awt.Label getIdUniquenessValueLabel() {
	if (ivjIdUniquenessValueLabel == null) {
            try {
                ivjIdUniquenessValueLabel = new java.awt.Label();
                ivjIdUniquenessValueLabel.setName("IdUniquenessValueLabel");
                ivjIdUniquenessValueLabel.setLocation(new java.awt.Point(102, 27));
                ivjIdUniquenessValueLabel.setText(" UNIQUE_ID");
                ivjIdUniquenessValueLabel.setBackground(java.awt.SystemColor.control);
                ivjIdUniquenessValueLabel.setFont(new java.awt.Font("dialog", 0, 10));
                ivjIdUniquenessValueLabel.setBounds(new java.awt.Rectangle(102, 27, 179, 12));
                ivjIdUniquenessValueLabel.setBounds(102, 27, 179, 12);
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	}
	return ivjIdUniquenessValueLabel;
    }
    Label _getImplicitActivationLabel() {
	return getImplicitActivationValueLabel();
    }
    /**
     * Return the ImplicitActivationNameLabel property value.
     * @return java.awt.Label
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private java.awt.Label getImplicitActivationNameLabel() {
	if (ivjImplicitActivationNameLabel == null) {
            try {
                ivjImplicitActivationNameLabel = new java.awt.Label();
                ivjImplicitActivationNameLabel.setName("ImplicitActivationNameLabel");
                ivjImplicitActivationNameLabel.setLocation(new java.awt.Point(1, 79));
                ivjImplicitActivationNameLabel.setText(" Implicit Activation");
                ivjImplicitActivationNameLabel.setBackground(java.awt.SystemColor.control);
                ivjImplicitActivationNameLabel.setFont(new java.awt.Font("dialog", 2, 10));
                ivjImplicitActivationNameLabel.setBounds(new java.awt.Rectangle(1, 79, 100, 12));
                ivjImplicitActivationNameLabel.setBounds(1, 79, 100, 12);
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	}
	return ivjImplicitActivationNameLabel;
    }
    /**
     * Return the ImplicitActivationValueLabel property value.
     * @return java.awt.Label
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private java.awt.Label getImplicitActivationValueLabel() {
	if (ivjImplicitActivationValueLabel == null) {
            try {
                ivjImplicitActivationValueLabel = new java.awt.Label();
                ivjImplicitActivationValueLabel.setName("ImplicitActivationValueLabel");
                ivjImplicitActivationValueLabel.setLocation(new java.awt.Point(102, 79));
                ivjImplicitActivationValueLabel.setText(" IMPLICIT_ACTIVATION");
                ivjImplicitActivationValueLabel.setBackground(java.awt.SystemColor.control);
                ivjImplicitActivationValueLabel.setFont(new java.awt.Font("dialog", 0, 10));
                ivjImplicitActivationValueLabel.setBounds(new java.awt.Rectangle(102, 79, 179, 12));
                ivjImplicitActivationValueLabel.setBounds(102, 79, 179, 12);
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	}
	return ivjImplicitActivationValueLabel;
    }
    Label _getLifespanLabel() {
	return getLifespanValueLabel();
    }
    /**
     * Return the LifespanNameLabel property value.
     * @return java.awt.Label
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private java.awt.Label getLifespanNameLabel() {
	if (ivjLifespanNameLabel == null) {
            try {
                ivjLifespanNameLabel = new java.awt.Label();
                ivjLifespanNameLabel.setName("LifespanNameLabel");
                ivjLifespanNameLabel.setLocation(new java.awt.Point(1, 14));
                ivjLifespanNameLabel.setText(" Lifespan");
                ivjLifespanNameLabel.setBackground(java.awt.SystemColor.control);
                ivjLifespanNameLabel.setFont(new java.awt.Font("dialog", 2, 10));
                ivjLifespanNameLabel.setBounds(new java.awt.Rectangle(1, 14, 100, 12));
                ivjLifespanNameLabel.setBounds(1, 14, 100, 12);
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	}
	return ivjLifespanNameLabel;
    }
    /**
     * Return the LifespanValueLabel property value.
     * @return java.awt.Label
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private java.awt.Label getLifespanValueLabel() {
	if (ivjLifespanValueLabel == null) {
            try {
                ivjLifespanValueLabel = new java.awt.Label();
                ivjLifespanValueLabel.setName("LifespanValueLabel");
                ivjLifespanValueLabel.setLocation(new java.awt.Point(102, 14));
                ivjLifespanValueLabel.setText(" TRANSIENT");
                ivjLifespanValueLabel.setBackground(java.awt.SystemColor.control);
                ivjLifespanValueLabel.setFont(new java.awt.Font("dialog", 0, 10));
                ivjLifespanValueLabel.setBounds(new java.awt.Rectangle(102, 14, 179, 12));
                ivjLifespanValueLabel.setBounds(102, 14, 179, 12);
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	}
	return ivjLifespanValueLabel;
    }
    Label _getRequestProcessingLabel() {
	return getRequestProcessingValueLabel();
    }
    /**
     * Return the RequestProcessingNameLabel property value.
     * @return java.awt.Label
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private java.awt.Label getRequestProcessingNameLabel() {
	if (ivjRequestProcessingNameLabel == null) {
            try {
                ivjRequestProcessingNameLabel = new java.awt.Label();
                ivjRequestProcessingNameLabel.setName("RequestProcessingNameLabel");
                ivjRequestProcessingNameLabel.setLocation(new java.awt.Point(1, 66));
                ivjRequestProcessingNameLabel.setText(" Request Processing");
                ivjRequestProcessingNameLabel.setBackground(java.awt.SystemColor.control);
                ivjRequestProcessingNameLabel.setFont(new java.awt.Font("dialog", 2, 10));
                ivjRequestProcessingNameLabel.setBounds(new java.awt.Rectangle(1, 66, 100, 12));
                ivjRequestProcessingNameLabel.setBounds(1, 66, 100, 12);
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	}
	return ivjRequestProcessingNameLabel;
    }
    /**
     * Return the RequestProcessingValueLabel property value.
     * @return java.awt.Label
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private java.awt.Label getRequestProcessingValueLabel() {
	if (ivjRequestProcessingValueLabel == null) {
            try {
                ivjRequestProcessingValueLabel = new java.awt.Label();
                ivjRequestProcessingValueLabel.setName("RequestProcessingValueLabel");
                ivjRequestProcessingValueLabel.setLocation(new java.awt.Point(102, 66));
                ivjRequestProcessingValueLabel.setText(" USE_ACTIVE_OBJECT_MAP_ONLY");
                ivjRequestProcessingValueLabel.setBackground(java.awt.SystemColor.control);
                ivjRequestProcessingValueLabel.setFont(new java.awt.Font("dialog", 0, 10));
                ivjRequestProcessingValueLabel.setBounds(new java.awt.Rectangle(102, 66, 179, 12));
                ivjRequestProcessingValueLabel.setBounds(102, 66, 179, 12);
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	}
	return ivjRequestProcessingValueLabel;
    }
    Label _getServantRetentionLabel() {
	return getServantRetentionValueLabel();
    }
    /**
     * Return the ServantRetentionNameLabel property value.
     * @return java.awt.Label
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private java.awt.Label getServantRetentionNameLabel() {
	if (ivjServantRetentionNameLabel == null) {
            try {
                ivjServantRetentionNameLabel = new java.awt.Label();
                ivjServantRetentionNameLabel.setName("ServantRetentionNameLabel");
                ivjServantRetentionNameLabel.setLocation(new java.awt.Point(1, 53));
                ivjServantRetentionNameLabel.setText(" Servant Retention");
                ivjServantRetentionNameLabel.setBackground(java.awt.SystemColor.control);
                ivjServantRetentionNameLabel.setFont(new java.awt.Font("dialog", 2, 10));
                ivjServantRetentionNameLabel.setBounds(new java.awt.Rectangle(1, 53, 100, 12));
                ivjServantRetentionNameLabel.setBounds(1, 53, 100, 12);
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	}
	return ivjServantRetentionNameLabel;
    }
    /**
     * Return the ServantRetentionValueLabel property value.
     * @return java.awt.Label
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private java.awt.Label getServantRetentionValueLabel() {
	if (ivjServantRetentionValueLabel == null) {
            try {
                ivjServantRetentionValueLabel = new java.awt.Label();
                ivjServantRetentionValueLabel.setName("ServantRetentionValueLabel");
                ivjServantRetentionValueLabel.setLocation(new java.awt.Point(102, 53));
                ivjServantRetentionValueLabel.setText(" RETAIN");
                ivjServantRetentionValueLabel.setBackground(java.awt.SystemColor.control);
                ivjServantRetentionValueLabel.setFont(new java.awt.Font("dialog", 0, 10));
                ivjServantRetentionValueLabel.setBounds(new java.awt.Rectangle(102, 53, 179, 12));
                ivjServantRetentionValueLabel.setBounds(102, 53, 179, 12);
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	}
	return ivjServantRetentionValueLabel;
    }
    /**
     * Return the TablePanel property value.
     * @return java.awt.Panel
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private java.awt.Panel getTablePanel() {
	if (ivjTablePanel == null) {
            try {
                ivjTablePanel = new java.awt.Panel();
                ivjTablePanel.setName("TablePanel");
                ivjTablePanel.setLocation(new java.awt.Point(9, 35));
                ivjTablePanel.setLayout(null);
                ivjTablePanel.setBackground(java.awt.SystemColor.controlShadow);
                ivjTablePanel.setBounds(new java.awt.Rectangle(9, 35, 282, 92));
                ivjTablePanel.setBounds(9, 35, 282, 92);
                getTablePanel().add(getIdUniquenessNameLabel(), getIdUniquenessNameLabel().getName());
                getTablePanel().add(getIdUniquenessValueLabel(), getIdUniquenessValueLabel().getName());
                getTablePanel().add(getLifespanValueLabel(), getLifespanValueLabel().getName());
                getTablePanel().add(getThreadValueLabel(), getThreadValueLabel().getName());
                getTablePanel().add(getThreadNameLabel(), getThreadNameLabel().getName());
                getTablePanel().add(getLifespanNameLabel(), getLifespanNameLabel().getName());
                getTablePanel().add(getRequestProcessingNameLabel(), getRequestProcessingNameLabel().getName());
                getTablePanel().add(getImplicitActivationNameLabel(), getImplicitActivationNameLabel().getName());
                getTablePanel().add(getIdAssignmentNameLabel(), getIdAssignmentNameLabel().getName());
                getTablePanel().add(getIdAssignmentValueLabel(), getIdAssignmentValueLabel().getName());
                getTablePanel().add(getImplicitActivationValueLabel(), getImplicitActivationValueLabel().getName());
                getTablePanel().add(getRequestProcessingValueLabel(), getRequestProcessingValueLabel().getName());
                getTablePanel().add(getServantRetentionValueLabel(), getServantRetentionValueLabel().getName());
                getTablePanel().add(getServantRetentionNameLabel(), getServantRetentionNameLabel().getName());
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	}
	return ivjTablePanel;
    }
    Label _getThreadLabel() {
	return getThreadValueLabel();
    }
    /**
     * Return the ThreadNameLabel property value.
     * @return java.awt.Label
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private java.awt.Label getThreadNameLabel() {
	if (ivjThreadNameLabel == null) {
            try {
                ivjThreadNameLabel = new java.awt.Label();
                ivjThreadNameLabel.setName("ThreadNameLabel");
                ivjThreadNameLabel.setLocation(new java.awt.Point(1, 1));
                ivjThreadNameLabel.setText(" Thread");
                ivjThreadNameLabel.setBackground(java.awt.SystemColor.control);
                ivjThreadNameLabel.setFont(new java.awt.Font("dialog", 2, 10));
                ivjThreadNameLabel.setBounds(new java.awt.Rectangle(1, 1, 100, 12));
                ivjThreadNameLabel.setBounds(1, 1, 100, 12);
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	}
	return ivjThreadNameLabel;
    }
    /**
     * Return the ThreadValueLabel property value.
     * @return java.awt.Label
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private java.awt.Label getThreadValueLabel() {
	if (ivjThreadValueLabel == null) {
            try {
                ivjThreadValueLabel = new java.awt.Label();
                ivjThreadValueLabel.setName("ThreadValueLabel");
                ivjThreadValueLabel.setLocation(new java.awt.Point(102, 1));
                ivjThreadValueLabel.setText(" ORB_CTRL_MODEL");
                ivjThreadValueLabel.setBackground(java.awt.SystemColor.control);
                ivjThreadValueLabel.setFont(new java.awt.Font("dialog", 0, 10));
                ivjThreadValueLabel.setBounds(new java.awt.Rectangle(102, 1, 179, 12));
                ivjThreadValueLabel.setBounds(102, 1, 179, 12);
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	}
	return ivjThreadValueLabel;
    }
    /**
     * Return the TitleLabel property value.
     * @return java.awt.Label
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private java.awt.Label getTitleLabel() {
	if (ivjTitleLabel == null) {
            try {
                ivjTitleLabel = new java.awt.Label();
                ivjTitleLabel.setName("TitleLabel");
                ivjTitleLabel.setLocation(new java.awt.Point(12, 8));
                ivjTitleLabel.setText("Policies:");
                ivjTitleLabel.setBackground(java.awt.SystemColor.control);
                ivjTitleLabel.setForeground(new java.awt.Color(0,0,0));
                ivjTitleLabel.setBounds(new java.awt.Rectangle(12, 8, 52, 23));
                ivjTitleLabel.setBounds(12, 8, 52, 23);
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
	}
	return ivjTitleLabel;
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
	try {
            // user code begin {1}
            // user code end
            setName("PolicyPanel");
            setBounds(new java.awt.Rectangle(0, 0, 300, 140));
            setLayout(null);
            setBackground(java.awt.SystemColor.control);
            setSize(300, 140);
            add(getTitleLabel(), getTitleLabel().getName());
            add(getTablePanel(), getTablePanel().getName());
	} catch (java.lang.Throwable ivjExc) {
            handleException(ivjExc);
	}
	// user code begin {2}
	// user code end
    }
    /**
     * PolicyPanel constructor comment.
     * @param layout java.awt.LayoutManager
     */
    public PolicyPanel(java.awt.LayoutManager layout) {
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
            PolicyPanel aPolicyPanel;
            aPolicyPanel = new PolicyPanel();
            frame.add("Center", aPolicyPanel);
            frame.setSize(aPolicyPanel.getSize());
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
    public PolicyPanel() {
	super();
	initialize();
    }
}






