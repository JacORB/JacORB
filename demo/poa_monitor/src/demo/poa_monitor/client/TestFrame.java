package demo.poa_monitor.client;

/**
 * This type was created in VisualAge.
 */
public class TestFrame extends java.awt.Frame implements java.awt.event.ActionListener, java.awt.event.AdjustmentListener, java.awt.event.WindowListener {
	private java.awt.Panel ivjButtonPanel = null;
	private java.awt.FlowLayout ivjButtonPanelFlowLayout = null;
	private java.awt.Label ivjComputationLabel = null;
	private java.awt.Panel ivjContentsPane = null;
	private java.awt.Label ivjInvocationLabel = null;
	private java.awt.Label ivjNumberLabel = null;
	private java.awt.TextField ivjNumberTextField = null;
	private java.awt.Label ivjSpeedLabel = null;
	private java.awt.Scrollbar ivjSpeedScrollbar = null;
	private java.awt.Label ivjThreadsLabel = null;
	private java.awt.TextField ivjThreadsTextField = null;
	private java.awt.Button ivjCancelButton = null;
	private java.awt.Button ivjStartButton = null;
	private java.awt.Button ivjStopButton = null;
	private java.awt.Panel ivjWorkPanel = null;
	private java.awt.Label ivjCostLabel = null;
	private java.awt.Scrollbar ivjCostScrollbar = null;
	private java.awt.Label ivjDescriptionLabel = null;
	private java.awt.Label ivjServerLabel = null;
	private java.awt.BorderLayout ivjContentsPaneBorderLayout = null;
	private java.awt.BorderLayout ivjTestFrameBorderLayout = null;
/**
 * Constructor
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public TestFrame() {
	super();
	initialize();
}
/**
 * TestFrame constructor comment.
 * @param title java.lang.String
 */
public TestFrame(String title) {
	super(title);
}
private void _actionCancelButtonPressed() {
	Client.actionCancel();
}
private void _actionCostbarValueChanged(int value) {
	if (value == 0) {
		getCostLabel().setText("no cost");
		Client.cost = value;

	} else if (value < 11) {
		getCostLabel().setText("0 - "+((value)*100)+" msec");
		Client.cost = value*100;
			
	} else {
		getCostLabel().setText("0 - "+((value)*200)+" msec");
		Client.cost = value*200;
	}
}
private void _actionSpeedbarValueChanged(int value) {
	if (value == 0) {
		getSpeedLabel().setText("no delay");
		Client.speed = value;

	} else if (value < 11) {
		getSpeedLabel().setText("0 - "+((value)*100)+" msec");
		Client.speed = value*100;
			
	} else {
		getSpeedLabel().setText("0 - "+((value)*200)+" msec");
		Client.speed = value*200;
	}
}
private void _actionStartButtonPressed(String objects, String threads) {
	getNumberLabel().setEnabled(false);
	getNumberTextField().setEnabled(false);
	getThreadsLabel().setEnabled(false);
	getThreadsTextField().setEnabled(false);
	getStartButton().setEnabled(false);

	Client.actionStart(Integer.parseInt(objects), Integer.parseInt(threads));
}
private void _actionStopButtonPressed() {
	getNumberLabel().setEnabled(true);
	getNumberTextField().setEnabled(true);
	getThreadsLabel().setEnabled(true);
	getThreadsTextField().setEnabled(true);
	getStartButton().setEnabled(true);

	Client.actionStop();
}
/**
 * Method to handle events for the ActionListener interface.
 * @param e java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public void actionPerformed(java.awt.event.ActionEvent e) {
	// user code begin {1}
	// user code end
	if ((e.getSource() == getStartButton()) ) {
		connEtoC4(e);
	}
	if ((e.getSource() == getStopButton()) ) {
		connEtoC5(e);
	}
	if ((e.getSource() == getCancelButton()) ) {
		connEtoC6(e);
	}
	// user code begin {2}
	// user code end
}
/**
 * Method to handle events for the AdjustmentListener interface.
 * @param e java.awt.event.AdjustmentEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public void adjustmentValueChanged(java.awt.event.AdjustmentEvent e) {
	// user code begin {1}
	// user code end
	if ((e.getSource() == getSpeedScrollbar()) ) {
		connEtoC2(e);
	}
	if ((e.getSource() == getCostScrollbar()) ) {
		connEtoC3(e);
	}
	// user code begin {2}
	// user code end
}
/**
 * connEtoC1:  (TestFrame.window.windowClosing(java.awt.event.WindowEvent) --> TestFrame.dispose()V)
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
 * connEtoC2:  (CancelButton.action.actionPerformed(java.awt.event.ActionEvent) --> TestFrame._actionCancelButtonPressed()V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC2(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this._actionCancelButtonPressed();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC2:  (SpeedScrollbar.adjustment.adjustmentValueChanged(java.awt.event.AdjustmentEvent) --> TestFrame._actionSpeedbarValueChanged(I)V)
 * @param arg1 java.awt.event.AdjustmentEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC2(java.awt.event.AdjustmentEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this._actionSpeedbarValueChanged(getSpeedScrollbar().getValue());
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC3:  (StopButton.action.actionPerformed(java.awt.event.ActionEvent) --> TestFrame._actionStopButtonPressed()V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC3(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this._actionStopButtonPressed();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC3:  (CostScrollbar.adjustment.adjustmentValueChanged(java.awt.event.AdjustmentEvent) --> TestFrame._actionCostbarValueChanged(I)V)
 * @param arg1 java.awt.event.AdjustmentEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC3(java.awt.event.AdjustmentEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this._actionCostbarValueChanged(getCostScrollbar().getValue());
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC4:  (StartButton.action.actionPerformed(java.awt.event.ActionEvent) --> TestFrame._actionStartButtonPressed(Ljava.lang.String;Ljava.lang.String;)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC4(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this._actionStartButtonPressed(getNumberTextField().getText(), getThreadsTextField().getText());
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC5:  (StopButton.action.actionPerformed(java.awt.event.ActionEvent) --> TestFrame._actionStopButtonPressed()V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC5(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this._actionStopButtonPressed();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC5:  (SpeedScrollbar.adjustment.adjustmentValueChanged(java.awt.event.AdjustmentEvent) --> TestFrame._actionSpeedbarValueChanged(I)V)
 * @param arg1 java.awt.event.AdjustmentEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC5(java.awt.event.AdjustmentEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this._actionSpeedbarValueChanged(getSpeedScrollbar().getValue());
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC6:  (CancelButton.action.actionPerformed(java.awt.event.ActionEvent) --> TestFrame._actionCancelButtonPressed()V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC6(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this._actionCancelButtonPressed();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoC6:  (CostsScrollbar.adjustment.adjustmentValueChanged(java.awt.event.AdjustmentEvent) --> TestFrame._actionCostsbarValueChanged(I)V)
 * @param arg1 java.awt.event.AdjustmentEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC6(java.awt.event.AdjustmentEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this._actionCostbarValueChanged(getCostScrollbar().getValue());
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
 * @return java.awt.Panel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Panel getButtonPanel() {
	if (ivjButtonPanel == null) {
		try {
			ivjButtonPanel = new java.awt.Panel();
			ivjButtonPanel.setName("ButtonPanel");
			ivjButtonPanel.setLayout(getButtonPanelFlowLayout());
			ivjButtonPanel.setBackground(java.awt.SystemColor.control);
			getButtonPanel().add(getStartButton(), getStartButton().getName());
			getButtonPanel().add(getStopButton(), getStopButton().getName());
			getButtonPanel().add(getCancelButton(), getCancelButton().getName());
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
 * Return the ButtonPanelFlowLayout property value.
 * @return java.awt.FlowLayout
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.FlowLayout getButtonPanelFlowLayout() {
	java.awt.FlowLayout ivjButtonPanelFlowLayout = null;
	try {
		/* Create part */
		ivjButtonPanelFlowLayout = new java.awt.FlowLayout();
		ivjButtonPanelFlowLayout.setAlignment(java.awt.FlowLayout.RIGHT);
		ivjButtonPanelFlowLayout.setVgap(5);
		ivjButtonPanelFlowLayout.setHgap(10);
	} catch (java.lang.Throwable ivjExc) {
		handleException(ivjExc);
	};
	return ivjButtonPanelFlowLayout;
}
/**
 * Return the Button1 property value.
 * @return java.awt.Button
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Button getCancelButton() {
	if (ivjCancelButton == null) {
		try {
			ivjCancelButton = new java.awt.Button();
			ivjCancelButton.setName("CancelButton");
			ivjCancelButton.setBackground(java.awt.SystemColor.control);
			ivjCancelButton.setActionCommand(" Cancel ");
			ivjCancelButton.setLabel(" Cancel ");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjCancelButton;
}
/**
 * Return the ComputationLabel property value.
 * @return java.awt.Label
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Label getComputationLabel() {
	if (ivjComputationLabel == null) {
		try {
			ivjComputationLabel = new java.awt.Label();
			ivjComputationLabel.setName("ComputationLabel");
			ivjComputationLabel.setLocation(new java.awt.Point(20, 185));
			ivjComputationLabel.setText("Computation cost on server");
			ivjComputationLabel.setBackground(java.awt.SystemColor.control);
			ivjComputationLabel.setSize(new java.awt.Dimension(130, 12));
			ivjComputationLabel.setForeground(java.awt.Color.black);
			ivjComputationLabel.setFont(new java.awt.Font("dialog", 2, 10));
			ivjComputationLabel.setBounds(new java.awt.Rectangle(20, 185, 130, 12));
			ivjComputationLabel.setAlignment(java.awt.Label.LEFT);
			ivjComputationLabel.setBounds(20, 185, 130, 12);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjComputationLabel;
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
			getContentsPane().add(getWorkPanel(), "Center");
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
 * Return the CostsLabel property value.
 * @return java.awt.Label
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Label getCostLabel() {
	if (ivjCostLabel == null) {
		try {
			ivjCostLabel = new java.awt.Label();
			ivjCostLabel.setName("CostLabel");
			ivjCostLabel.setLocation(new java.awt.Point(170, 197));
			ivjCostLabel.setText("no cost");
			ivjCostLabel.setBackground(java.awt.SystemColor.control);
			ivjCostLabel.setSize(new java.awt.Dimension(80, 23));
			ivjCostLabel.setForeground(java.awt.Color.red);
			ivjCostLabel.setFont(new java.awt.Font("dialog", 1, 12));
			ivjCostLabel.setBounds(new java.awt.Rectangle(170, 197, 80, 23));
			ivjCostLabel.setAlignment(2);
			ivjCostLabel.setBounds(150, 197, 100, 23);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjCostLabel;
}
/**
 * Return the CostsScrollbar property value.
 * @return java.awt.Scrollbar
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Scrollbar getCostScrollbar() {
	if (ivjCostScrollbar == null) {
		try {
			ivjCostScrollbar = new java.awt.Scrollbar();
			ivjCostScrollbar.setName("CostScrollbar");
			ivjCostScrollbar.setLocation(new java.awt.Point(20, 200));
			ivjCostScrollbar.setSize(new java.awt.Dimension(130, 18));
			ivjCostScrollbar.setBounds(new java.awt.Rectangle(20, 200, 130, 18));
			ivjCostScrollbar.setBounds(20, 200, 120, 18);
			ivjCostScrollbar.setOrientation(java.awt.Scrollbar.HORIZONTAL);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjCostScrollbar;
}
/**
 * Return the DescriptionLabel property value.
 * @return java.awt.Label
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Label getDescriptionLabel() {
	if (ivjDescriptionLabel == null) {
		try {
			ivjDescriptionLabel = new java.awt.Label();
			ivjDescriptionLabel.setName("DescriptionLabel");
			ivjDescriptionLabel.setLocation(new java.awt.Point(10, 35));
			ivjDescriptionLabel.setText("description not available");
			ivjDescriptionLabel.setBackground(java.awt.SystemColor.control);
			ivjDescriptionLabel.setSize(new java.awt.Dimension(260, 23));
			ivjDescriptionLabel.setForeground(java.awt.Color.black);
			ivjDescriptionLabel.setFont(new java.awt.Font("dialog", 3, 12));
			ivjDescriptionLabel.setBounds(new java.awt.Rectangle(10, 35, 260, 23));
			ivjDescriptionLabel.setAlignment(1);
			ivjDescriptionLabel.setBounds(10, 35, 260, 23);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjDescriptionLabel;
}
/**
 * Return the InvocationLabel property value.
 * @return java.awt.Label
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Label getInvocationLabel() {
	if (ivjInvocationLabel == null) {
		try {
			ivjInvocationLabel = new java.awt.Label();
			ivjInvocationLabel.setName("InvocationLabel");
			ivjInvocationLabel.setLocation(new java.awt.Point(20, 145));
			ivjInvocationLabel.setText("Invocation delay on client");
			ivjInvocationLabel.setBackground(java.awt.SystemColor.control);
			ivjInvocationLabel.setSize(new java.awt.Dimension(130, 12));
			ivjInvocationLabel.setForeground(java.awt.Color.black);
			ivjInvocationLabel.setFont(new java.awt.Font("dialog", 2, 10));
			ivjInvocationLabel.setBounds(new java.awt.Rectangle(20, 145, 130, 12));
			ivjInvocationLabel.setAlignment(java.awt.Label.LEFT);
			ivjInvocationLabel.setBounds(20, 145, 130, 12);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjInvocationLabel;
}
/**
 * Return the NumberLabel property value.
 * @return java.awt.Label
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Label getNumberLabel() {
	if (ivjNumberLabel == null) {
		try {
			ivjNumberLabel = new java.awt.Label();
			ivjNumberLabel.setName("NumberLabel");
			ivjNumberLabel.setLocation(new java.awt.Point(20, 70));
			ivjNumberLabel.setText("Number of objects:");
			ivjNumberLabel.setBackground(java.awt.SystemColor.control);
			ivjNumberLabel.setSize(new java.awt.Dimension(110, 23));
			ivjNumberLabel.setFont(new java.awt.Font("dialog", 2, 12));
			ivjNumberLabel.setBounds(new java.awt.Rectangle(20, 70, 110, 23));
			ivjNumberLabel.setBounds(20, 70, 110, 23);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjNumberLabel;
}
/**
 * Return the NumberTextField property value.
 * @return java.awt.TextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.TextField getNumberTextField() {
	if (ivjNumberTextField == null) {
		try {
			ivjNumberTextField = new java.awt.TextField();
			ivjNumberTextField.setName("NumberTextField");
			ivjNumberTextField.setLocation(new java.awt.Point(170, 70));
			ivjNumberTextField.setText("1");
			ivjNumberTextField.setBackground(java.awt.Color.white);
			ivjNumberTextField.setSize(new java.awt.Dimension(80, 23));
			ivjNumberTextField.setBounds(new java.awt.Rectangle(170, 70, 80, 23));
			ivjNumberTextField.setBounds(170, 66, 80, 30);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjNumberTextField;
}
/**
 * Return the ServerLabel property value.
 * @return java.awt.Label
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Label getServerLabel() {
	if (ivjServerLabel == null) {
		try {
			ivjServerLabel = new java.awt.Label();
			ivjServerLabel.setName("ServerLabel");
			ivjServerLabel.setLocation(new java.awt.Point(20, 20));
			ivjServerLabel.setText("Server description:");
			ivjServerLabel.setBackground(java.awt.SystemColor.control);
			ivjServerLabel.setSize(new java.awt.Dimension(240, 12));
			ivjServerLabel.setFont(new java.awt.Font("dialog", 2, 10));
			ivjServerLabel.setBounds(new java.awt.Rectangle(20, 20, 240, 12));
			ivjServerLabel.setAlignment(1);
			ivjServerLabel.setBounds(20, 20, 240, 12);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjServerLabel;
}
/**
 * Return the SpeedLabel property value.
 * @return java.awt.Label
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Label getSpeedLabel() {
	if (ivjSpeedLabel == null) {
		try {
			ivjSpeedLabel = new java.awt.Label();
			ivjSpeedLabel.setName("SpeedLabel");
			ivjSpeedLabel.setLocation(new java.awt.Point(170, 157));
			ivjSpeedLabel.setText("no delay");
			ivjSpeedLabel.setBackground(java.awt.SystemColor.control);
			ivjSpeedLabel.setSize(new java.awt.Dimension(80, 23));
			ivjSpeedLabel.setForeground(java.awt.Color.red);
			ivjSpeedLabel.setFont(new java.awt.Font("dialog", 1, 12));
			ivjSpeedLabel.setBounds(new java.awt.Rectangle(170, 157, 80, 23));
			ivjSpeedLabel.setAlignment(2);
			ivjSpeedLabel.setBounds(150, 157, 100, 23);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjSpeedLabel;
}
/**
 * Return the SpeedScrollbar property value.
 * @return java.awt.Scrollbar
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Scrollbar getSpeedScrollbar() {
	if (ivjSpeedScrollbar == null) {
		try {
			ivjSpeedScrollbar = new java.awt.Scrollbar();
			ivjSpeedScrollbar.setName("SpeedScrollbar");
			ivjSpeedScrollbar.setLocation(new java.awt.Point(20, 160));
			ivjSpeedScrollbar.setSize(new java.awt.Dimension(130, 18));
			ivjSpeedScrollbar.setBounds(new java.awt.Rectangle(20, 160, 130, 18));
			ivjSpeedScrollbar.setBounds(20, 160, 120, 18);
			ivjSpeedScrollbar.setOrientation(java.awt.Scrollbar.HORIZONTAL);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjSpeedScrollbar;
}
/**
 * Return the Button3 property value.
 * @return java.awt.Button
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Button getStartButton() {
	if (ivjStartButton == null) {
		try {
			ivjStartButton = new java.awt.Button();
			ivjStartButton.setName("StartButton");
			ivjStartButton.setBackground(java.awt.SystemColor.control);
			ivjStartButton.setActionCommand("   Start   ");
			ivjStartButton.setLabel("   Start   ");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjStartButton;
}
/**
 * Return the Button2 property value.
 * @return java.awt.Button
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Button getStopButton() {
	if (ivjStopButton == null) {
		try {
			ivjStopButton = new java.awt.Button();
			ivjStopButton.setName("StopButton");
			ivjStopButton.setBackground(java.awt.SystemColor.control);
			ivjStopButton.setActionCommand("   Stop   ");
			ivjStopButton.setLabel("   Stop   ");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjStopButton;
}
/**
 * Return the TestFrameBorderLayout property value.
 * @return java.awt.BorderLayout
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.BorderLayout getTestFrameBorderLayout() {
	java.awt.BorderLayout ivjTestFrameBorderLayout = null;
	try {
		/* Create part */
		ivjTestFrameBorderLayout = new java.awt.BorderLayout();
		ivjTestFrameBorderLayout.setVgap(0);
		ivjTestFrameBorderLayout.setHgap(0);
	} catch (java.lang.Throwable ivjExc) {
		handleException(ivjExc);
	};
	return ivjTestFrameBorderLayout;
}
/**
 * Return the ThreadsLabel property value.
 * @return java.awt.Label
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Label getThreadsLabel() {
	if (ivjThreadsLabel == null) {
		try {
			ivjThreadsLabel = new java.awt.Label();
			ivjThreadsLabel.setName("ThreadsLabel");
			ivjThreadsLabel.setLocation(new java.awt.Point(20, 105));
			ivjThreadsLabel.setText("Threads per object:");
			ivjThreadsLabel.setBackground(java.awt.SystemColor.control);
			ivjThreadsLabel.setSize(new java.awt.Dimension(110, 23));
			ivjThreadsLabel.setFont(new java.awt.Font("dialog", 2, 12));
			ivjThreadsLabel.setBounds(new java.awt.Rectangle(20, 105, 110, 23));
			ivjThreadsLabel.setBounds(20, 105, 110, 23);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjThreadsLabel;
}
/**
 * Return the ThreadsTextField property value.
 * @return java.awt.TextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.TextField getThreadsTextField() {
	if (ivjThreadsTextField == null) {
		try {
			ivjThreadsTextField = new java.awt.TextField();
			ivjThreadsTextField.setName("ThreadsTextField");
			ivjThreadsTextField.setLocation(new java.awt.Point(170, 105));
			ivjThreadsTextField.setText("1");
			ivjThreadsTextField.setBackground(new java.awt.Color(255,255,255));
			ivjThreadsTextField.setSize(new java.awt.Dimension(80, 23));
			ivjThreadsTextField.setBounds(new java.awt.Rectangle(170, 105, 80, 23));
			ivjThreadsTextField.setBounds(170, 101, 80, 30);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjThreadsTextField;
}
/**
 * Return the ContentsPane property value.
 * @return java.awt.Panel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Panel getWorkPanel() {
	if (ivjWorkPanel == null) {
		try {
			ivjWorkPanel = new java.awt.Panel();
			ivjWorkPanel.setName("WorkPanel");
			ivjWorkPanel.setLayout(null);
			ivjWorkPanel.setBackground(java.awt.SystemColor.control);
			getWorkPanel().add(getNumberLabel(), getNumberLabel().getName());
			getWorkPanel().add(getThreadsLabel(), getThreadsLabel().getName());
			getWorkPanel().add(getNumberTextField(), getNumberTextField().getName());
			getWorkPanel().add(getThreadsTextField(), getThreadsTextField().getName());
			getWorkPanel().add(getSpeedScrollbar(), getSpeedScrollbar().getName());
			getWorkPanel().add(getCostScrollbar(), getCostScrollbar().getName());
			getWorkPanel().add(getInvocationLabel(), getInvocationLabel().getName());
			getWorkPanel().add(getComputationLabel(), getComputationLabel().getName());
			getWorkPanel().add(getSpeedLabel(), getSpeedLabel().getName());
			getWorkPanel().add(getCostLabel(), getCostLabel().getName());
			getWorkPanel().add(getServerLabel(), getServerLabel().getName());
			getWorkPanel().add(getDescriptionLabel(), getDescriptionLabel().getName());
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
	getSpeedScrollbar().addAdjustmentListener(this);
	getCostScrollbar().addAdjustmentListener(this);
	getStartButton().addActionListener(this);
	getStopButton().addActionListener(this);
	getCancelButton().addActionListener(this);
}
/**
 * Initialize the class.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initialize() {
	// user code begin {1}
	// user code end
	setName("TestFrame");
	setLayout(getTestFrameBorderLayout());
	setBackground(java.awt.SystemColor.control);
	setSize(new java.awt.Dimension(280, 310));
	setBounds(new java.awt.Rectangle(0, 0, 280, 310));
	setSize(280, 310);
	setTitle("Test Client");
	add(getContentsPane(), "Center");
	initConnections();
	// user code begin {2}
	getDescriptionLabel().setText(Client.serverDescription);
	// user code end
}
/**
 * main entrypoint - starts the part when it is run as an application
 * @param args java.lang.String[]
 */
public static void main(java.lang.String[] args) {
	try {
		TestFrame aTestFrame;
		aTestFrame = new TestFrame();
		try {
			Class aCloserClass = Class.forName("com.ibm.uvm.abt.edit.WindowCloser");
			Class parmTypes[] = { java.awt.Window.class };
			Object parms[] = { aTestFrame };
			java.lang.reflect.Constructor aCtor = aCloserClass.getConstructor(parmTypes);
			aCtor.newInstance(parms);
		} catch (java.lang.Throwable exc) {};
		aTestFrame.setVisible(true);
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
