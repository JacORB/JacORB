package org.jacorb.test.notification;

import org.omg.CosNotifyComm.PullConsumerPOA;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyChannelAdmin.ProxyPullSupplier;
import org.omg.CORBA.BooleanHolder;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.ProxyPullSupplierHelper;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import junit.framework.TestCase;
import org.omg.CosNotifyChannelAdmin.AdminNotFound;
import org.apache.log.Logger;
import org.apache.log.Hierarchy;

/*
 *        JacORB - a free Java ORB
 */

/**
 * AnyPullReceiver.java
 *
 *
 * Created: Tue Nov 12 14:31:04 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class AnyPullReceiver extends PullConsumerPOA implements Runnable, TestClientOperations {

    Logger logger_ = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());

    Any event_ = null;
    boolean received_ = false;
    boolean error_ = false;
    ORB orb_;
    POA poa_;
    ProxyPullSupplier mySupplier_;
    long TIMEOUT = 5000;
    boolean connected_;
    IntHolder adminId_;
    ConsumerAdmin myAdmin_;
    TestCase testCase_;

    public AnyPullReceiver(TestCase testCase) {
	testCase_ = testCase;
    }

    public void connect(NotificationTestCaseSetup setup,
			EventChannel channel,
			boolean useOrSemantic) throws AdminNotFound, AlreadyConnected, AdminLimitExceeded {

	orb_ = setup.getClientOrb();
	poa_ = setup.getClientRootPOA();

	IntHolder _proxyId = new IntHolder();
	IntHolder _adminId = new IntHolder();

	if (useOrSemantic) {
	    adminId_ = new IntHolder();
	    myAdmin_ = channel.new_for_consumers(InterFilterGroupOperator.OR_OP, _adminId);
	    testCase_.assertEquals(InterFilterGroupOperator.OR_OP, myAdmin_.MyOperator());
	} else {
	    myAdmin_ = channel.new_for_consumers(InterFilterGroupOperator.AND_OP, _adminId);
	    testCase_.assertEquals(InterFilterGroupOperator.AND_OP, myAdmin_.MyOperator());
	}
	testCase_.assertEquals(myAdmin_, channel.get_consumeradmin(_adminId.value));

	mySupplier_ = 
	    ProxyPullSupplierHelper.narrow(myAdmin_.
					   obtain_notification_pull_supplier(ClientType.ANY_EVENT, _proxyId));

	mySupplier_.connect_any_pull_consumer(_this(orb_));
	connected_ = true;
    }

    public void shutdown() {
	mySupplier_.disconnect_pull_supplier();
	mySupplier_ = null;

	if (myAdmin_ != null) {
	    myAdmin_.destroy();
	    myAdmin_ = null;
	}
    }

    void reset() {
	error_ = false;
	received_ = false;
	event_ = null;
    }
    
    public boolean isConnected() {
	return connected_;
    }

    public boolean isEventHandled() {
	return received_;
    }

    public boolean isError() {
	return error_;
    }

    public void run() {
	BooleanHolder _success = new BooleanHolder();
	_success.value = false;
	long _startTime = System.currentTimeMillis();
	logger_.info("start receiver");

	try {
	    while (true) {
		event_ = mySupplier_.try_pull(_success);
	    
		if (_success.value) {
		    logger_.debug("Received Event");
		    received_ = true;
		    break;
		} 

		if (System.currentTimeMillis() < _startTime + TIMEOUT) {
		    Thread.yield();
		} else {
		    logger_.debug("Timeout");
		    received_ = false;
		    break;
		}
	    }
	} catch (Disconnected d) {
	    d.printStackTrace();
	    error_ = true;
	}
    }

    public void offer_change(EventType[] e1, EventType[] e2) {
    }

    public void disconnect_pull_consumer() {
	connected_ = false;
    }

}// AnyPullReceiver
