package org.jacorb.test.notification;

import org.omg.CosEventComm.PullConsumerPOA;
import org.omg.CosEventChannelAdmin.ProxyPullSupplier;
import org.omg.CORBA.Any;
import org.omg.CORBA.BooleanHolder;
import org.omg.CosEventComm.Disconnected;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.EventChannelHelper;
import org.omg.CosEventChannelAdmin.EventChannel;
import org.omg.PortableServer.POA;
import org.omg.CosEventChannelAdmin.ConsumerAdmin;

/**
 * CosEventPullReceiver.java
 *
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class CosEventPullReceiver extends PullConsumerPOA implements Runnable, TestClientOperations {

    ProxyPullSupplier mySupplier_;
    Any event_ = null;
    long timeout_ = 1000;
    boolean error_ = false;
    boolean connected_;
    
    public void disconnect_pull_consumer() {
	connected_ = false;
    }

    public boolean isError() {
	return error_;
    }

    public boolean isConnected() {
	return connected_;
    }

    public boolean isEventHandled() {
	return (event_ != null);
    }

    public void run() {
	long _startTime = System.currentTimeMillis();
	long _stopTime = _startTime + timeout_;

	BooleanHolder _success = new BooleanHolder();

	try {
	    while (connected_ && System.currentTimeMillis() < _stopTime) {
		event_ = mySupplier_.try_pull(_success);
		if (_success.value) {
		    break;
		}
		Thread.yield();
	    }
	} catch (Disconnected d) {
	    d.printStackTrace();
	    error_ = true;
	}
    }

    public void connect(NotificationTestCaseSetup setup,
			org.omg.CosNotifyChannelAdmin.EventChannel channel,
			boolean useOrSemantic) throws AlreadyConnected {

	EventChannel _channel = EventChannelHelper.narrow(channel);
	ConsumerAdmin _admin = _channel.for_consumers();
	mySupplier_ = _admin.obtain_pull_supplier();
	mySupplier_.connect_pull_consumer(_this(setup.getClientOrb()));
	connected_ = true;
    }

    public void shutdown() {
	mySupplier_.disconnect_pull_supplier();
    }

}
