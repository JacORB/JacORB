package org.jacorb.poa.gui.beans;

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
 * Provides the functionality of a fill level statebar.
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.0, 05/03/99
 * @see		jacorb.poa.gui.beans.FillLevelCanvas
 */
public class FillLevelBar extends java.awt.Panel {
	private boolean useAvg;
	private boolean isVariable;
	private int max;
	private int avg;
	private int min;
	private int cur;
	private java.awt.Label ivjAvgLabel = null;
	private java.awt.Label ivjCurLabel = null;
	private java.awt.Label ivjMaxLabel = null;
	private java.awt.Label ivjMinLabel = null;
	private FillLevelCanvas ivjFillLevelCanvas = null;
/**
 * Constructor
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public FillLevelBar() {
	super();
	initialize();
}
/**
 * FillLevelBar constructor comment.
 * @param layout java.awt.LayoutManager
 */
public FillLevelBar(java.awt.LayoutManager layout) {
	super(layout);
}
/**
 * Return the Label22 property value.
 * @return java.awt.Label
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Label getAvgLabel() {
	if (ivjAvgLabel == null) {
		try {
			ivjAvgLabel = new java.awt.Label();
			ivjAvgLabel.setName("AvgLabel");
			ivjAvgLabel.setText("");
			ivjAvgLabel.setBackground(java.awt.SystemColor.control);
			ivjAvgLabel.setFont(new java.awt.Font("dialog", 0, 10));
			ivjAvgLabel.setBounds(23, 54, 25, 10);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjAvgLabel;
}
/**
 * Return the Label1 property value.
 * @return java.awt.Label
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Label getCurLabel() {
	if (ivjCurLabel == null) {
		try {
			ivjCurLabel = new java.awt.Label();
			ivjCurLabel.setName("CurLabel");
			ivjCurLabel.setText("");
			ivjCurLabel.setBackground(java.awt.SystemColor.control);
			ivjCurLabel.setFont(new java.awt.Font("dialog", 0, 10));
			ivjCurLabel.setAlignment(1);
			ivjCurLabel.setBounds(0, 115, 25, 12);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjCurLabel;
}
/**
 * Return the FillLevelCanvas property value.
 * @return org.jacorb.poa.gui.beans.FillLevelCanvas
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private FillLevelCanvas getFillLevelCanvas() {
	if (ivjFillLevelCanvas == null) {
		try {
			ivjFillLevelCanvas = new org.jacorb.poa.gui.beans.FillLevelCanvas();
			ivjFillLevelCanvas.setName("FillLevelCanvas");
			ivjFillLevelCanvas.setBounds(5, 10, 15, 100);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjFillLevelCanvas;
}
/**
 * Return the Label21 property value.
 * @return java.awt.Label
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Label getMaxLabel() {
	if (ivjMaxLabel == null) {
		try {
			ivjMaxLabel = new java.awt.Label();
			ivjMaxLabel.setName("MaxLabel");
			ivjMaxLabel.setText("");
			ivjMaxLabel.setBackground(java.awt.SystemColor.control);
			ivjMaxLabel.setFont(new java.awt.Font("dialog", 0, 10));
			ivjMaxLabel.setBounds(23, 7, 25, 10);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjMaxLabel;
}
/**
 * Return the Label2 property value.
 * @return java.awt.Label
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Label getMinLabel() {
	if (ivjMinLabel == null) {
		try {
			ivjMinLabel = new java.awt.Label();
			ivjMinLabel.setName("MinLabel");
			ivjMinLabel.setText("");
			ivjMinLabel.setBackground(java.awt.SystemColor.control);
			ivjMinLabel.setFont(new java.awt.Font("dialog", 0, 10));
			ivjMinLabel.setBounds(23, 104, 25, 10);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	return ivjMinLabel;
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
public void init(int _min, int _avg, int _max, java.awt.Color _color1, java.awt.Color _color2, boolean _useAvg, boolean _isVariable) {
	min = _min;
	avg = _avg;
	max = _max;
	useAvg = _useAvg;
	getFillLevelCanvas().init(min, avg, max, _color1, _color2, useAvg);
	isVariable = _isVariable;
	if (useAvg) {
		getAvgLabel().setText(""+_avg);
		getAvgLabel().setLocation(23, getFillLevelCanvas().getYAvg()+6);		
	}
	getMaxLabel().setText(""+_max);
	getMinLabel().setText(""+_min);
	getCurLabel().setText(""+_min);	
}
/**
 * Initialize the class.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initialize() {
	// user code begin {1}
	// user code end
	setName("FillLevelBar");
	setLayout(null);
	setBackground(java.awt.SystemColor.control);
	setSize(50, 130);
	add(getCurLabel(), getCurLabel().getName());
	add(getMinLabel(), getMinLabel().getName());
	add(getMaxLabel(), getMaxLabel().getName());
	add(getAvgLabel(), getAvgLabel().getName());
	add(getFillLevelCanvas(), getFillLevelCanvas().getName());
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
		FillLevelBar aFillLevelBar;
		aFillLevelBar = new FillLevelBar();
		frame.add("Center", aFillLevelBar);
		frame.setSize(aFillLevelBar.getSize());
		frame.setVisible(true);
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of java.awt.Panel");
		exception.printStackTrace(System.out);
	}
}
private int maxCompute(int current) {
	
	int tenPerCent = max/10;
	
	if (current < tenPerCent) {
			
		while (max > current*2 && max > 10) {
			max = max/2 > 10 ? max/2 : 10;
		}
		
	} else if (current > max-tenPerCent) {
	
		while (max < current+tenPerCent) {
			max = max*2;
		}
	}
	return max;
}
public void paint(java.awt.Graphics g) {
	g.setColor(getBackground());
	g.fill3DRect(3, 8, 19, 104, false);
}
public void setAvgValue(int value) {
	if (avg == value) return;
	avg = value;
	if (useAvg) {
		getFillLevelCanvas().setAvg(avg);
		getAvgLabel().setLocation(23, getFillLevelCanvas().getYAvg()+6);
		getAvgLabel().setText(""+avg);
	}
}
public void setCurrentValue(int value) {
	if (cur == value) return;
	cur = value;
	if (isVariable) {
		if (max != maxCompute(cur)) {
			getFillLevelCanvas().setMax(max);
			getMaxLabel().setText(""+max);
		}
	}
	getFillLevelCanvas().setCurrent(cur > min ? cur-min : 0);
	getCurLabel().setText(""+cur);
}
public void setMaxValue(int value) {
	if (max == value) return;
	max = value;
	getFillLevelCanvas().setMax(max);
	if (useAvg) {
		getAvgLabel().setLocation(23, getFillLevelCanvas().getYAvg()+6);
	}
	getMaxLabel().setText(""+max);	
}
public void setMinValue(int value) {
	if (min == value) return;
	min = value;
	getFillLevelCanvas().setMin(min);
	if (useAvg) {
		getAvgLabel().setLocation(23, getFillLevelCanvas().getYAvg()+6);
	}
	getFillLevelCanvas().setCurrent(cur > min ? cur-min : 0);	
	getMinLabel().setText(""+min);
}

}









