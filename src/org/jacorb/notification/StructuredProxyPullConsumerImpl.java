package org.jacorb.notification;

import org.jacorb.notification.ProxyBase;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyComm.StructuredPullConsumerOperations;
import org.omg.CosNotifyFilter.FilterNotFound;
import org.omg.CosNotifyChannelAdmin.NotConnected;
import org.omg.CosNotifyComm.NotifyPublishOperations;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyChannelAdmin.ObtainInfoMode;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyActive;
import org.omg.CosNotifyComm.StructuredPullSupplier;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotification.QoSAdminOperations;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyInactive;
import org.omg.CosNotification.Property;
import org.omg.CosNotifyFilter.FilterAdminOperations;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullConsumerOperations;
import org.omg.CosNotifyChannelAdmin.ProxyConsumerOperations;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.apache.log4j.Logger;
import java.util.List;

/*
 *        JacORB - a free Java ORB
 */

/**
 * StructuredProxyPullConsumerImpl.java
 *
 *
 * Created: Mon Nov 04 01:27:01 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class StructuredProxyPullConsumerImpl extends ProxyBase 
    implements StructuredProxyPullConsumerOperations, Runnable {

    SupplierAdminTieImpl myAdminServant_;
    SupplierAdmin myAdmin_;
    ProxyType myType_ = ProxyType.PUSH_STRUCTURED;

    public StructuredProxyPullConsumerImpl(ApplicationContext appContext,
					   ChannelContext channelContext, 
					   SupplierAdminTieImpl supplierAdminServant, 
					   SupplierAdmin supplierAdmin) {
	super(appContext, channelContext, Logger.getLogger("Proxy.StructuredPullConsumer"));
	myAdmin_ = supplierAdmin;
	myAdminServant_ = supplierAdminServant;
    }
    
    // Implementation of org.omg.CosNotifyComm.StructuredPullConsumerOperations

    /**
     * Describe <code>disconnect_structured_pull_consumer</code> method
     * here.
     *
     */
    public void disconnect_structured_pull_consumer() {
	
    }
    
    // Implementation of org.omg.CosNotifyChannelAdmin.StructuredProxyPullConsumerOperations

    /**
     * Describe <code>connect_structured_pull_supplier</code> method here.
     *
     * @param structuredPullSupplier a <code>StructuredPullSupplier</code>
     * value
     * @exception AlreadyConnected if an error occurs
     * @exception TypeError if an error occurs
     */
    public void connect_structured_pull_supplier(StructuredPullSupplier structuredPullSupplier) throws AlreadyConnected, TypeError {
	
    }

    /**
     * Describe <code>suspend_connection</code> method here.
     *
     * @exception NotConnected if an error occurs
     * @exception ConnectionAlreadyInactive if an error occurs
     */
    public void suspend_connection() throws NotConnected, ConnectionAlreadyInactive {
	
    }

    /**
     * Describe <code>resume_connection</code> method here.
     *
     * @exception ConnectionAlreadyActive if an error occurs
     * @exception NotConnected if an error occurs
     */
    public void resume_connection() throws ConnectionAlreadyActive, NotConnected {
	
    }
    
    // Implementation of org.omg.CosNotifyChannelAdmin.ProxyConsumerOperations

    /**
     * Describe <code>MyType</code> method here.
     *
     * @return a <code>ProxyType</code> value
     */
    public ProxyType MyType() {
	return myType_;
    }

    /**
     * Describe <code>MyAdmin</code> method here.
     *
     * @return a <code>SupplierAdmin</code> value
     */
    public SupplierAdmin MyAdmin() {
	return myAdmin_;
    }

    /**
     * Describe <code>obtain_subscription_types</code> method here.
     *
     * @param obtainInfoMode an <code>ObtainInfoMode</code> value
     * @return an <code>EventType[]</code> value
     */
    public EventType[] obtain_subscription_types(ObtainInfoMode obtainInfoMode) {
	return null;
    }

    /**
     * Describe <code>validate_event_qos</code> method here.
     *
     * @param property a <code>Property[]</code> value
     * @param namedPropertyRangeSeqHolder a
     * <code>NamedPropertyRangeSeqHolder</code> value
     * @exception UnsupportedQoS if an error occurs
     */
    public void validate_event_qos(Property[] property1, NamedPropertyRangeSeqHolder namedPropertyRangeSeqHolder) throws UnsupportedQoS {
	
    }

    public void run() {
    }

    public List getSubsequentDestinations() {
	return null;
    }
    
    public TransmitEventCapable getEventSink() {
	return null;
    }


    private void disconnect() {
    }

    public void dispose() {
	disconnect();
    }
}// StructuredProxyPullConsumerImpl
