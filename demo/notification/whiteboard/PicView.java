package demo.notification.whiteboard;

import java.io.File;
import java.awt.Canvas;
import java.awt.Toolkit;
import java.awt.MediaTracker;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Graphics;

// das im konstruktor u"bergebene bild laden
// und darstellen ...

public class PicView extends Canvas {
    private Toolkit toolkit;
    private MediaTracker mediaTracker;
    private Image image;

    public PicView(String filename) {
	super();
	toolkit = Toolkit.getDefaultToolkit();
	mediaTracker = new MediaTracker(this);
	image = loadImage(filename);
	setSize(getDim() );
    }

    private Dimension getDim() {
	Dimension d = new Dimension (0, 0);
	if (image != null) {
	    d.width = image.getWidth (this);
	    d.height = image.getHeight (this);
	}
	return d;
    }

    Image loadImage(String filename) {
	if ( !new File(filename).exists ()) { 
	    System.out.println("File "+filename+" not Found !");
	    System.exit(1);
	}

	Image imageData = toolkit.getImage(filename);
	mediaTracker.addImage(imageData,0);
	
	try {
	    mediaTracker.waitForID(0);
	} catch (InterruptedException ie) {}

	return imageData;
    }
        
    public void paint(Graphics g) {
	g.clearRect (0, 0, getDim().width, getDim().height);
	g.drawImage(image,0,0,null);
    }
}














