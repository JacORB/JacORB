package org.jacorb.test.notification;

import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.AdminNotFound;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullConsumer;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullConsumerHelper;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.NotifySubscribeOperations;
import org.omg.CosNotifyComm.StructuredPullSupplierHelper;
import org.omg.CosNotifyComm.StructuredPullSupplierOperations;
import org.omg.CosNotifyComm.StructuredPullSupplierPOATie;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class StructuredPullSender
    extends Thread
    implements StructuredPullSupplierOperations, TestClientOperations {

    ORB orb_;
    StructuredEvent event_;
    StructuredProxyPullConsumer pullConsumer_;
    private boolean error_;
    boolean connected_;
    boolean eventHandled_;
    NotificationTestCase testCase_;
    boolean available_;

    public boolean isError() {
        return error_;
    }

    public boolean isEventHandled() {
        return eventHandled_;
    }

    public StructuredPullSender(NotificationTestCase testCase, StructuredEvent event) {
        event_ = event;
        testCase_ = testCase;
    }

    public void run() {
        synchronized(this) {
            available_ = true;
        }
    }

    // Implementation of org.omg.CosNotifyComm.NotifySubscribeOperations

    /**
     * Describe <code>subscription_change</code> method here.
     *
     * @param eventType an <code>EventType[]</code> value
     * @param eventType an <code>EventType[]</code> value
     * @exception InvalidEventType if an error occurs
     */
    public void subscription_change(EventType[] eventType1, EventType[] eventType2) throws InvalidEventType {

    }

    // Implementation of org.omg.CosNotifyComm.StructuredPullSupplierOperations

    /**
     * Describe <code>pull_structured_event</code> method here.
     *
     * @return a <code>StructuredEvent</code> value
     * @exception Disconnected if an error occurs
     */
    public StructuredEvent pull_structured_event() throws Disconnected {
        BooleanHolder _success = new BooleanHolder();
        StructuredEvent _event;
        while(true) {
            _event = try_pull_structured_event(_success);
            if(_success.value) {
                return _event;
            }
            Thread.yield();
        }
    }

    /**
     * Describe <code>try_pull_structured_event</code> method here.
     *
     * @param booleanHolder a <code>BooleanHolder</code> value
     * @return a <code>StructuredEvent</code> value
     * @exception Disconnected if an error occurs
     */
    public StructuredEvent try_pull_structured_event(BooleanHolder booleanHolder) throws Disconnected {
        booleanHolder.value = false;
        StructuredEvent _result = NotificationTestUtils.getInvalidStructuredEvent(orb_);
        if (event_ != null) {
            synchronized(this) {
                if (event_ != null && available_) {
                    _result = event_;
                    event_ = null;
                    booleanHolder.value = true;
                    eventHandled_ = true;
                }
            }
        }
        return _result;
    }

    /**
     * Describe <code>disconnect_structured_pull_supplier</code> method
     * here.
     *
     */
    public void disconnect_structured_pull_supplier() {
        connected_ = false;
    }

    public void connect(EventChannel channel,boolean useOrSemantic) throws AdminLimitExceeded, AlreadyConnected, TypeError, AdminNotFound {

        orb_ = testCase_.getSetup().getORB();

        StructuredPullSupplierPOATie _senderTie = new StructuredPullSupplierPOATie(this);
        SupplierAdmin _supplierAdmin = channel.default_supplier_admin();
        IntHolder _proxyId = new IntHolder();
        pullConsumer_ =
            StructuredProxyPullConsumerHelper.narrow(_supplierAdmin.obtain_notification_pull_consumer(ClientType.STRUCTURED_EVENT, _proxyId));

        testCase_.assertEquals(_supplierAdmin, channel.get_supplieradmin(_supplierAdmin.MyID()));

        testCase_.assertEquals(pullConsumer_.MyType(), ProxyType.PULL_STRUCTURED);


        pullConsumer_.connect_structured_pull_supplier(StructuredPullSupplierHelper.narrow(_senderTie._this(testCase_.getSetup().getORB())));
        connected_ = true;
    }

    public void shutdown() {
        pullConsumer_.disconnect_structured_pull_consumer();
        testCase_.assertTrue(pullConsumer_._non_existent());
    }

    public boolean isConnected() {
        return connected_;
    }

}
