package org.jacorb.notification;

import org.omg.CosNotifyChannelAdmin.StructuredProxyPushSupplierPOA;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyComm.StructuredPushConsumer;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosNotifyChannelAdmin.NotConnected;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyInactive;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyActive;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushSupplierOperations;
import org.apache.log4j.Logger;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CosEventComm.Disconnected;
import java.util.Collections;
import java.util.List;

/*
 *        JacORB - a free Java ORB
 */

/**
 * StructuredProxyPushSupplierImpl.java
 *
 *
 * Created: Sun Nov 03 22:41:38 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class StructuredProxyPushSupplierImpl 
    extends ProxyBase 
    implements StructuredProxyPushSupplierOperations, 
	       TransmitEventCapable {

    ConsumerAdminTieImpl myAdminServant_;
    ConsumerAdmin myAdmin_;
    ProxyType myType_ = ProxyType.PUSH_STRUCTURED;

    StructuredPushConsumer pushConsumer_;

    public StructuredProxyPushSupplierImpl(ApplicationContext appContext,
					   ChannelContext channelContext,
					   ConsumerAdminTieImpl myAdminServant,
					   ConsumerAdmin myAdmin) {
	super(appContext, channelContext, Logger.getLogger("Proxy.StructuredProxyPushSupplier"));
	myAdminServant_ = myAdminServant;
	myAdmin_ = myAdmin;
    }

    public void transmit_event(NotificationEvent event) {
	logger_.info("transmit_event()");
	if (connected_) {
	    try {
		pushConsumer_.push_structured_event(event.toStructuredEvent());
	    } catch (Disconnected d) {
		connected_ = false;
		logger_.debug("push failed - Recipient is Disconnected");
	    }
	} else {
	    logger_.debug("Not connected");
	}
    }
    
    public void connect_structured_push_consumer(StructuredPushConsumer consumer) throws AlreadyConnected, TypeError {
	if (connected_) {
	    throw new AlreadyConnected();
	}
	connected_ = true;
	pushConsumer_ = consumer;
    }

    public void disconnect_structured_push_supplier() {
	if (connected_) {
	    if (pushConsumer_ != null) {
		pushConsumer_.disconnect_structured_push_consumer();
		pushConsumer_ = null;
	    }
	    connected_ = false;
	} else {
	    throw new OBJECT_NOT_EXIST();
	}
    }

    public ProxyType MyType() {
	return myType_;
    }

    public ConsumerAdmin MyAdmin() {
	return myAdmin_;
    }

    public List getSubsequentDestinations() {
	return Collections.singletonList(this);
    }

    public TransmitEventCapable getEventSink() {
	return this;
    }

    public void dispose() {
    }
}// StructuredProxyPushSupplierImpl
