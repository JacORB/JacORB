package org.jacorb.test.notification;

import junit.framework.TestCase;

import org.omg.CORBA.IntHolder;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.AdminNotFound;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushConsumer;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushConsumerHelper;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.StructuredPushSupplierHelper;
import org.omg.CosNotifyComm.StructuredPushSupplierOperations;
import org.omg.CosNotifyComm.StructuredPushSupplierPOATie;
import org.omg.CosNotifyComm.StructuredPushSupplier;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POA;
import org.omg.CORBA.ORB;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.util.Debug;

public class StructuredPushSender
    extends Thread
    implements StructuredPushSupplierOperations, TestClientOperations {

    Logger logger_ = Debug.getNamedLogger(getClass().getName());
    PerformanceListener perfListener_;
    StructuredGenerator generator_;
    StructuredProxyPushConsumer pushConsumer_;
    StructuredEvent event_;
    StructuredEvent[] events_;
    int times_ = 1;
    boolean error_ = false;
    boolean connected_;
    boolean eventSent_;
    long interval_ = 0;
    TestCase testCase_;

    StructuredPushSender(TestCase testCase, StructuredEvent event) {
        event_ = event;
        testCase_ = testCase;
    }

    public StructuredPushSender(TestCase testCase,StructuredEvent event, int times) {
        testCase_ = testCase;
        event_ = event;
        times_ = times;
    }

    public StructuredPushSender(TestCase testCase,
                                StructuredEvent[] events,
                                long interval) {

        testCase_ = testCase;
        events_ = events;
        interval_ = interval;

    }


    public StructuredPushSender(TestCase testCase,
                                PerformanceListener logger,
                                StructuredGenerator generator,
                                int times,
                                long interval) {

        perfListener_ = logger;
        generator_ = generator;
        times_ = times;
        interval_ = interval;
        testCase_ = testCase;
    }

    public boolean isConnected() {
        return connected_;
    }

    public boolean isEventHandled() {
        return eventSent_;
    }

    public boolean isError() {
        return error_;
    }

    public void run() {
        if (events_ != null) {
            runOnEventArray();
        } else {
            runOnSingleEvent();
        }
        eventSent_ = true;
    }

    private void runOnEventArray()  {
        for (int x=0; x<events_.length; ++x) {
            try {
                pushConsumer_.push_structured_event(events_[x]);
            } catch (Exception e) {
                logger_.error("", e);
                testCase_.fail();
            }
        }
    }

    private void runOnSingleEvent() {
        for (int x=0; x<times_; ++x) {
            try {
                if (generator_ != null) {
                    pushConsumer_.push_structured_event(generator_.getNextEvent());
                } else {
                    pushConsumer_.push_structured_event(event_);
                }
            } catch (Exception e) {
                logger_.info("error while push", e);
                error_ = true;
            }
            try {
                Thread.sleep(interval_);
            } catch (InterruptedException ie) {}
        }
    }


    public void disconnect_structured_push_supplier() {
        connected_ = false;
    }

    public void subscription_change(EventType[] eventType,
                                    EventType[] eventType2) throws InvalidEventType {
    }

    public void connect(NotificationTestCaseSetup setup,
                        EventChannel channel,
                        boolean useOrSemantic)
        throws AdminLimitExceeded,
               AlreadyConnected,
               AdminNotFound {

        testCase_.assertNotNull(channel);

        StructuredPushSupplierPOATie senderTie =
            new StructuredPushSupplierPOATie(this);

        StructuredPushSupplier sender = senderTie._this(setup.getClientOrb());

        SupplierAdmin supplierAdmin =
            channel.default_supplier_admin();

        testCase_.assertNotNull(supplierAdmin);

        testCase_.assertEquals(supplierAdmin,
                               channel.get_supplieradmin(supplierAdmin.MyID()));

        IntHolder _proxyIdHolder = new IntHolder();

        pushConsumer_ =
            StructuredProxyPushConsumerHelper.narrow(supplierAdmin.obtain_notification_push_consumer(ClientType.STRUCTURED_EVENT,_proxyIdHolder));

        testCase_.assertEquals(pushConsumer_.MyType(),
                               ProxyType.PUSH_STRUCTURED);

        pushConsumer_.connect_structured_push_supplier(sender);

        connected_ = true;
    }

    public void shutdown() {
        pushConsumer_.disconnect_structured_push_consumer();
        //      testCase_.assertTrue(pushConsumer_._non_existent());
    }
}
