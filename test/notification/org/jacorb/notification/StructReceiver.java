import org.omg.CosNotifyComm.StructuredPushConsumerPOA;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.EventType;

class StructReceiver extends StructuredPushConsumerPOA implements Runnable {

    StructuredEvent event_ = null;
    ORB orb_;
    POA poa_;

    StructReceiver(ORB orb, POA poa) {
	super();
	orb_ = orb;
	poa_ = poa;
    }

    boolean received() {
	return event_ != null;
    }

    public void run() {
	if (event_ == null) {
	    synchronized(this) {
		try {
		    wait(1000);
		} catch (InterruptedException ie) {
		}
	    }
	}
    }

    public void push_structured_event(StructuredEvent event) {
	event_ = event;
	synchronized(this) {
	    notifyAll();
	}
    }

    public void disconnect_structured_push_consumer() {
    }

    public void offer_change(EventType[] e1, EventType[] e2) {
    }
}
