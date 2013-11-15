package org.jacorb.test.notification;

import java.util.ArrayList;
import java.util.List;
import org.jacorb.test.notification.common.NotificationTestCase;
import org.junit.Assert;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
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
import org.omg.CosNotifyComm.StructuredPushSupplier;
import org.omg.CosNotifyComm.StructuredPushSupplierOperations;
import org.omg.CosNotifyComm.StructuredPushSupplierPOATie;

public class StructuredPushSender extends Thread implements StructuredPushSupplierOperations,
        TestClientOperations
{
    public StructuredProxyPushConsumer pushConsumer_;

    StructuredEvent[] events_;

    boolean error_ = false;

    boolean connected_;

    boolean eventSent_;

    long interval_ = 0;

    NotificationTestCase testCase_;

    List addedSubscriptions_ = new ArrayList();

    List removedSubscriptions_ = new ArrayList();

    final ORB orb_;

    public StructuredPushSender(ORB orb)
    {
        orb_ = orb;
    }

    public boolean isConnected()
    {
        return connected_;
    }

    public boolean isEventHandled()
    {
        return eventSent_;
    }

    public boolean isError()
    {
        return error_;
    }

    public void run()
    {
        runOnEventArray();

        eventSent_ = true;
    }

    private void runOnEventArray()
    {
        try
        {
            pushEvents(events_);
        } catch (Disconnected e)
        {
            Assert.fail();
        }
    }

    public void pushEvents(final StructuredEvent[] events) throws Disconnected
    {
        for (int x = 0; x < events.length; ++x)
        {
            pushConsumer_.push_structured_event(events[x]);
        }
    }

    public void disconnect_structured_push_supplier()
    {
        connected_ = false;
    }

    public void subscription_change(EventType[] added, EventType[] removed) throws InvalidEventType
    {
        for (int x = 0; x < added.length; ++x)
        {
            addedSubscriptions_.add(added[x]);
        }

        for (int x = 0; x < removed.length; ++x)
        {
            removedSubscriptions_.add(removed[x]);
        }
    }

    public void connect(EventChannel channel, boolean useOrSemantic) throws AdminLimitExceeded,
            AlreadyConnected, AdminNotFound
    {
        Assert.assertNotNull(channel);

        StructuredPushSupplierPOATie senderTie = new StructuredPushSupplierPOATie(this);

        StructuredPushSupplier sender = senderTie._this(orb_);

        SupplierAdmin supplierAdmin = channel.default_supplier_admin();

        Assert.assertNotNull(supplierAdmin);

        Assert.assertEquals(supplierAdmin, channel.get_supplieradmin(supplierAdmin.MyID()));

        IntHolder _proxyIdHolder = new IntHolder();

        pushConsumer_ = StructuredProxyPushConsumerHelper.narrow(supplierAdmin
                .obtain_notification_push_consumer(ClientType.STRUCTURED_EVENT, _proxyIdHolder));

        Assert.assertEquals(pushConsumer_.MyType(), ProxyType.PUSH_STRUCTURED);

        pushConsumer_.connect_structured_push_supplier(sender);

        connected_ = true;
    }

    public void shutdown()
    {
        pushConsumer_.disconnect_structured_push_consumer();
    }

    public void setStructuredEvent(StructuredEvent[] events)
    {
        events_ = events;
    }

    public void setStructuredEvent(StructuredEvent events)
    {
        setStructuredEvent(new StructuredEvent[] { events });
    }

    public void setInterval(long interval)
    {
        interval_ = interval;
    }
}
