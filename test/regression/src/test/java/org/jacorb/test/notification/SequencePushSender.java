package org.jacorb.test.notification;

import org.junit.Assert;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPushConsumer;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPushConsumerHelper;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.SequencePushSupplierHelper;
import org.omg.CosNotifyComm.SequencePushSupplierOperations;
import org.omg.CosNotifyComm.SequencePushSupplierPOATie;

class SequencePushSender extends Thread implements SequencePushSupplierOperations,
        TestClientOperations
{
    SequenceProxyPushConsumer pushConsumer_;

    StructuredEvent[] event_;

    int times_ = 1;

    boolean error_ = false;

    boolean connected_;

    boolean eventSent_;

    ORB orb_;

    public SequencePushSender(ORB orb, StructuredEvent[] event)
    {
        orb_ = orb;
        event_ = event;
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
        for (int x = 0; x < times_; ++x)
        {
            try
            {
                pushConsumer_.push_structured_events(event_);
            } catch (Exception e)
            {
                error_ = true;
            }
        }
        eventSent_ = true;
    }

    public void disconnect_sequence_push_supplier()
    {
        connected_ = false;
    }

    public void subscription_change(EventType[] eventType, EventType[] eventType2)
            throws InvalidEventType
    {
        // ignored
    }

    public void connect(EventChannel channel, boolean useOrSemantic) throws AdminLimitExceeded,
            AlreadyConnected
    {
        SequencePushSupplierPOATie senderTie = new SequencePushSupplierPOATie(this);
        SupplierAdmin supplierAdmin = channel.default_supplier_admin();
        IntHolder _proxyIdHolder = new IntHolder();

        pushConsumer_ = SequenceProxyPushConsumerHelper.narrow(supplierAdmin
                .obtain_notification_push_consumer(ClientType.SEQUENCE_EVENT, _proxyIdHolder));

        Assert.assertEquals(ProxyType._PUSH_SEQUENCE, pushConsumer_.MyType().value());

        pushConsumer_.connect_sequence_push_supplier(SequencePushSupplierHelper.narrow(senderTie
                ._this(orb_)));

        connected_ = true;
    }

    public void shutdown()
    {
        pushConsumer_.disconnect_sequence_push_consumer();
    }
}
