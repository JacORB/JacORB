package org.jacorb.test.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPushSupplier;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPushSupplierHelper;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.SequencePushConsumerHelper;
import org.omg.CosNotifyComm.SequencePushConsumerOperations;
import org.omg.CosNotifyComm.SequencePushConsumerPOATie;

class SequencePushReceiver extends Thread implements SequencePushConsumerOperations,
        TestClientOperations
{
    SequenceProxyPushSupplier pushSupplier_;

    boolean connected_ = false;

    long timeout_ = 2000;

    final ORB orb_;

    private int expected = -1;
    
    final List received = new ArrayList();
    
    public SequencePushReceiver(ORB orb)
    {
        orb_ = orb;
    }

    public void setExpected(int expected)
    {
        this.expected = expected;
    }
    
    public SequenceProxyPushSupplier getPushSupplier()
    {
        return pushSupplier_;
    }
    
    public void run()
    {
        final long start = System.currentTimeMillis();
        
        synchronized (this)
        {
            while (!isDone() && (System.currentTimeMillis() < start + timeout_))
            {
                try
                {
                    wait(timeout_);
                } catch (InterruptedException e)
                {
                    // ignored
                }
            }
        }
    }

    private boolean isDone()
    {
        boolean done;
        done = (expected != -1) ? received.size() == expected : received.size() > 0;
        return done;
    }

    public void push_structured_events(StructuredEvent[] event) throws Disconnected
    {
        synchronized (this)
        {
            received.addAll(Arrays.asList(event));
            
            notifyAll();
        }
    }

    public void disconnect_sequence_push_consumer()
    {
        connected_ = false;
    }

    public void offer_change(EventType[] type1, EventType[] type2) throws InvalidEventType
    {
        // ignored
    }

    public void connect(EventChannel channel, boolean useOrSemantic) throws AdminLimitExceeded,
            AlreadyConnected, TypeError
    {
        SequencePushConsumerPOATie receiverTie = new SequencePushConsumerPOATie(this);
        ConsumerAdmin _consumerAdmin = channel.default_consumer_admin();
        IntHolder _proxyIdHolder = new IntHolder();

        pushSupplier_ = SequenceProxyPushSupplierHelper.narrow(_consumerAdmin
                .obtain_notification_push_supplier(ClientType.SEQUENCE_EVENT, _proxyIdHolder));

        Assert.assertEquals(ProxyType._PUSH_SEQUENCE, pushSupplier_.MyType().value());

        pushSupplier_.connect_sequence_push_consumer(SequencePushConsumerHelper.narrow(receiverTie
                ._this(orb_)));

        connected_ = true;
    }

    public boolean isEventHandled()
    {
        return isDone();
    }

    public boolean isConnected()
    {
        return connected_;
    }

    public boolean isError()
    {
        return false;
    }

    public void shutdown()
    {
        pushSupplier_.disconnect_sequence_push_supplier();
    }
    
    public List getResult()
    {
        return received;
    }
}
