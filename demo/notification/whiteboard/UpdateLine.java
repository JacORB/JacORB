package demo.notification.whiteboard;

/**
 * UpdateLine.java
 *
 *
 * Created: Sun Feb  6 19:05:46 2000
 *
 * @author Alphonse Bendt
 * @version
 */

// klasse kapselt alle A"nderungen in einem PixelImage
public class UpdateLine implements java.io.Serializable {

    int x0,x1,y1,y0,red,green,blue,brushSize;
    boolean clear = false;

    public UpdateLine(boolean clear) {
	this.clear = true;
    }

    public UpdateLine(int x0, int y0, 
		      int x1, int y1, 
		      int red, int green , int blue,int brushSize) {
	this.x0 = x0;
	this.y0 = y0;
	this.x1 = x1;
	this.y1 = y1;
	this.red = red;
	this.green = green;
	this.blue = blue;
	this.brushSize = brushSize;
    }

    public boolean clearScreen() {
	return clear;
    }
    public int getX0() {
	return x0;
    }
    public int getY0() {
	return y0;
    }
    public int getX1() {
	return x1;
    }
    public int getY1() {
	return y1;
    }
    public int getRed() {
	return red;
    }
    public int getGreen() {
	return green;
    }
    public int getBlue() {
	return blue;
    }	       
    public int getBrushSize() {
	return brushSize;
    }

    public String toString() {
	String s = new String();
	s += "x0:"+x0;
	s += " y0:"+y0;
	s += " x1:"+x1;
	s += " y1:"+y1;
	s += " red:"+red;
	s += " green:"+green;
	s += " blue:"+blue;
	s += " brushSize:"+brushSize+"\n";
	
	return s;
    }
} // UpdateLine





