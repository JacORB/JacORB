package demo.notification.whiteboard;

/**
 * GhostPainter.java
 *
 *
 * Created: Wed Feb  9 18:45:20 2000
 *
 * @author Alphonse Bendt
 * @version
 */

// Wenn der GhostPainter einmal gestartet ist,
// schla"ft er eine gewisse Zeit
// und malt dann unvermittelt im u"bergebenen AWTWin 
// Linien ...

public class GhostPainter extends Thread {
    
    int x,y;
    long sleep = 5000;
    IWorkgroupFrame w;
    boolean active_ = true;

    public GhostPainter(IWorkgroupFrame w,int x, int y) {
	this.x = x;
	this.y = y;
	this.w = w;
    }
    
    public void shutdown() {
	active_ = false;
	interrupt();
    }

    public void run() {
	while (active_) {
	    try {
		sleep ((long) (sleep * Math.random()) );
	    } catch (InterruptedException ie) {
		if (!active_) {
		    return;
		}
	    }
	    
	    int x0 = (int) (Math.random() * x);
	    int x1 = (int) (Math.random() * x);
	    int y1 = (int) (Math.random() * y);
	    int y0 = (int) (Math.random() * y);
	}
    }
} // GhostPainter

