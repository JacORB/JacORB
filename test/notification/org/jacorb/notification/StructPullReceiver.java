import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyComm.StructuredPullConsumerPOA;
import org.omg.CosNotifyChannelAdmin.ProxyPullSupplier;

class StructPullReceiver extends StructuredPullConsumerPOA implements Runnable {

    StructuredEvent event_ = null;
    ORB orb_;
    POA poa_;
    boolean disonnectCalled_;

    StructPullReceiver(ORB orb, POA poa, ProxyPullSupplier supplier) {
	super();
	orb_ = orb;
	poa_ = poa;
	//	supplier.
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

    public void disconnect_structured_pull_consumer() {
	disonnectCalled_ = true;
    }

    public void offer_change(EventType[] e1, EventType[] e2) {
    }
}
