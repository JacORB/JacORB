package demo.notification.whiteboard;

import java.util.Vector;
import java.util.Enumeration;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushConsumer;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CORBA.ORB;
import org.omg.CosEventComm.Disconnected;

/**
 * ExtendedPixelImage.java
 *
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ExtendedPixelImage extends BrushSizePixelImage implements WhiteboardVars {

    StructuredProxyPushConsumer myConsumer_;
    StructuredEvent event_;
    ORB orb_;

    public void setPixels(int[] pixels) {
	for (int x=0; x<m_pixels.length;x++)
	    m_pixels[x] = pixels[x];
    }

    public ExtendedPixelImage(int w, int h) {
	super(w,h);
    }

    public void setOrb(ORB orb) {
	orb_ = orb;
    }

    public void setEvent(StructuredEvent event) {
	event_ = event;
    }

    // lo"schen mitschreiben und lo"schen
    synchronized public void clearAll() {
	WhiteboardUpdate _update = new WhiteboardUpdate();
	_update.clear(true);
	super.clearAll();
    }

    // lo"schen nur lokal
    public void localClearAll() {
	super.clearAll();
    }

    // dieses drawLine wird von 'remote' aufgerufen
    // und merkt sich u"ber den aufruf die bei uns eingestellte
    // brushSize
    synchronized public void drawLine(int x0,
				      int y0,
				      int x1,
				      int y1,
				      int red,
				      int green,
				      int blue,
				      int brushSize) {
	int x = getBrushSize();
	setBrushSize(brushSize);
	super.drawLine(x0,y0,x1,y1,red,green,blue);
	setBrushSize(x);
    }

    // gezeichnete Linien mitschreiben ansonsten
    // normal zeichnen
    synchronized public void drawLine(int x0,
				      int y0,
				      int x1,
				      int y1,
				      int red,
				      int green,
				      int blue) {

	WhiteboardUpdate _update = new WhiteboardUpdate();

	super.drawLine(x0,y0,x1,y1,red,green,blue);
	_update.line(new LineData(x0, y0, x1, y1, red, green, blue, getBrushSize()));

	try {
	    if (myConsumer_ != null) {
		WhiteboardUpdateHelper.insert(event_.remainder_of_body, _update);
		myConsumer_.push_structured_event(event_);
	    }
	} catch (Disconnected d) {
	    d.printStackTrace();
	    myConsumer_ = null;
	}
    }

    // wird vom ghostWriter aufgerufen ...
    // also A"nderung zeichnen
    // und merken
    synchronized public void drawLine(UpdateLine ul, boolean v) {
	drawLine(ul);
	//lines.addElement(ul);
    }

    // drawLine das auf UpdateLine Objekten arbeitet
    void drawLine(UpdateLine ul) {
	if ( !ul.clearScreen() )
	    drawLine( ul.getX0(),
		      ul.getY0(),
		      ul.getX1(),
		      ul.getY1(),
		      ul.getRed(),
		      ul.getGreen(),
		      ul.getBlue(),
		      ul.getBrushSize() );
	else
	    super.clearAll();
    }

    // UpdateLines auspacken und alle einzeichnen
    public void drawLine(Vector uls) {
	for (Enumeration e = uls.elements(); e.hasMoreElements(); )
	    drawLine((UpdateLine)e.nextElement() );
    }

} // ExtendedPixelImage
