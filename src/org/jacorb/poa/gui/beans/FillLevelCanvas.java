package org.jacorb.poa.gui.beans;

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
 
/**
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.0, 05/03/99
 * @see		org.jacorb.poa.gui.beans.FillLevelBar
 */
public class FillLevelCanvas extends java.awt.Canvas {
	private java.awt.Graphics buf = null;
	private java.awt.Image img = null;
	private java.awt.Color color1 = java.awt.Color.orange;
	private java.awt.Color color2 = java.awt.Color.red;
	private int width = 0;
	private int height = 0;
	private int max = 0;
	private int avg = 0;
	private int min = 0;
	private int cur = 0;
	private int yAvg = 0;
	private int yCur = 0;
	private boolean useAvg;
/**
 * Constructor
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public FillLevelCanvas() {
	super();
	initialize();
}
public int getYAvg() {
	if (useAvg) return yAvg;
	return 0;
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
public void init(int _min, int _avg, int _max, java.awt.Color _color1, java.awt.Color _color2, boolean _useAvg) {
	min = _min;
	avg = _avg;
	max = _max;
	useAvg = _useAvg;
	yCompute();
	if (_color1 != null) color1 = _color1;
	if (_color2 != null) color2 = _color2;
}
/**
 * Initialize the class.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initialize() {
	// user code begin {1}
	// user code end
	setName("FillLevelCanvas");
	setBackground(java.awt.Color.white);
	setSize(15, 100);
	// user code begin {2}
	width = getBounds().width;
	height = getBounds().height;
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
		FillLevelCanvas aFillLevelCanvas;
		aFillLevelCanvas = new FillLevelCanvas();
		frame.add("Center", aFillLevelCanvas);
		frame.setSize(aFillLevelCanvas.getSize());
		frame.setVisible(true);
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of org.jacorb.poa.gui.FillLevelCanvas");
		exception.printStackTrace(System.out);
	}
}
public void paint(java.awt.Graphics g) {
	if (buf == null) {
		img = createImage(getBounds().width, getBounds().height);
		buf = img.getGraphics();
	} else {	
		buf.setColor(getBackground());
		buf.fillRect(0, 0, width, height);
		buf.setColor(getForeground());
	}
	paintUnbuffered(buf);
	g.drawImage(img, 0, 0, this);
}
public void paintUnbuffered(java.awt.Graphics g) {
	if (useAvg) {
		if (cur <= avg) {
			g.setColor(color1);
			g.fillRect(0, yCur, width, height - yCur);

		} else {
			g.setColor(color2);
			g.fillRect(0, yCur, width, height - yCur);
			g.setColor(color1);
			g.fillRect(0, yAvg, width, height - yAvg);
		}
		if (avg < max) {
			g.setColor(java.awt.Color.black);
			g.drawLine(0, yAvg, width, yAvg);
		}

	} else {
		g.setColor(color1);
		g.fillRect(0, yCur, width, height - yCur);
	}
}
public void setAvg(int value) {
	avg = value;
	yCompute();
	repaint();
}
public void setCurrent(int value) {
	cur = value;
	yCompute();
	repaint();
}
public void setMax(int value) {
	max = value;
	yCompute();
	repaint();
}
public void setMin(int value) {
	min = value;
	yCompute();
	repaint();
}
private void yCompute() {
	float helpF;
	if (useAvg) {
		helpF = ((float)avg)/((float)max-min) * ((float)height);
		yAvg = yTransform((int) helpF);
	}
	helpF = ((float)cur)/((float)max-min) * ((float)height);
	yCur = yTransform((int) helpF);
}
private int yTransform(int y) {
	return height - y;
}

}









