package demo.notification.whiteboard;

import java.awt.Canvas;
import java.awt.image.ImageProducer;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.Graphics;

public class BrushSizeDrawCanvas extends Canvas {

    WorkgroupController controller_;
    ImageProducer imageProducer_;

    // the last  point
    private int lastX,lastY;

    // the drawing color
    private int drawRed=255;
    private int drawGreen=255;
    private int drawBlue=255;

    public BrushSizeDrawCanvas(WorkgroupController controller, int width, int height) {
	super();
	setSize(width,height);
	controller_ = controller;
	initialize();
    }
    
    public void initialize() {
	imageProducer_ = controller_.getImage().getProducer();
	
	// react on pressed mouse key to start drawing
	addMouseListener(new MouseAdapter() {
		public void mousePressed(MouseEvent e) { 
		    lastX = e.getX(); 
		    lastY = e.getY();
		}
	    });

	// react on dragged mouse
	addMouseMotionListener(new MouseMotionAdapter() {
		public void mouseDragged(MouseEvent e) {
		    controller_.drawLine(lastX,lastY,
				   e.getX(),e.getY(),
				   drawRed,drawGreen,drawBlue);
		    lastX = e.getX();
		    lastY = e.getY();
		    repaint();
		}
	    });
    }

    public void paint(Graphics g){
	g.drawImage(createImage(imageProducer_), 0, 0, null);
    }

    public void update(Graphics g){
	paint(g);
    }
    
    void setDrawColor(int red,int green,int blue) {
	drawRed=red;
	drawGreen=green;
	drawBlue=blue;
    }

    public void clearAll() {
	controller_.clearAll();
	repaint();
    }

    void setBrushSize(int size) {
	controller_.setBrushSize(size);
    }
}
