package org.jacorb.test.notification;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.omg.CORBA.IntHolder;
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
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushSupplier;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushSupplierHelper;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.StructuredPushConsumerHelper;
import org.omg.CosNotifyComm.StructuredPushConsumerOperations;
import org.omg.CosNotifyComm.StructuredPushConsumerPOATie;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterNotFound;

import EDU.oswego.cs.dl.util.concurrent.CyclicBarrier;

public class StructuredPushReceiver extends Thread implements StructuredPushConsumerOperations,
        TestClientOperations
{

    StructuredProxyPushSupplier pushSupplier_;

    int received_ = 0;

    int expected_ = 1;

    int filterId_ = Integer.MIN_VALUE;

    long timeout_ = 2000;

    Filter filter_;

    CyclicBarrier barrier_;

    boolean connected_ = false;

    PerformanceListener perfListener_;

    NotificationTestCase testCase_;

    List receivedEvents = new ArrayList();

    List addedOffers = new ArrayList();

    List removedOffers = new ArrayList();

    public StructuredPushReceiver(NotificationTestCase testCase)
    {
        testCase_ = testCase;
    }

    public StructuredPushReceiver(NotificationTestCase testCase, PerformanceListener perfListener,
            int expected)
    {
        perfListener_ = perfListener;
        expected_ = expected;
        testCase_ = testCase;
    }

    public StructuredPushReceiver(NotificationTestCase testCase, int expected)
    {
        this(testCase, null, expected);
    }

    public void setBarrier(CyclicBarrier barrier)
    {
        barrier_ = barrier;
    }

    public void setFilter(Filter filter)
    {
        filter_ = filter;
        filterId_ = pushSupplier_.add_filter(filter);
    }

    public void setTimeOut(long timeout)
    {
        timeout_ = timeout;
    }

    public void run()
    {
        if (!isEventHandled())
        {
            synchronized (this)
            {
                try
                {
                    wait(timeout_);
                } catch (InterruptedException e)
                {
                }
            }
        }

        if (barrier_ != null)
        {
            try
            {
                barrier_.barrier();
            } catch (InterruptedException ie)
            {
            }
        }
    }

    public void push_structured_event(StructuredEvent event) throws Disconnected
    {
        received_++;

        if (perfListener_ != null)
        {
            perfListener_.eventReceived(event, System.currentTimeMillis());
        }

        receivedEvents.add(event);

        if (received_ == expected_)
        {
            synchronized (this)
            {
                notifyAll();
            }
        }
    }

    public void disconnect_structured_push_consumer()
    {
        connected_ = false;
    }

    public void offer_change(EventType[] added, EventType[] removed) throws InvalidEventType
    {

        for (int x = 0; x < added.length; ++x)
        {
            addedOffers.add(added[x]);
        }

        for (int x = 0; x < removed.length; ++x)
        {
            removedOffers.add(removed[x]);
        }
    }

    public void connect(EventChannel channel, boolean useOrSemantic) throws AdminLimitExceeded,
            AlreadyConnected, TypeError
    {

        StructuredPushConsumerPOATie receiverTie = new StructuredPushConsumerPOATie(this);

        ConsumerAdmin _consumerAdmin = channel.default_consumer_admin();

        IntHolder _proxyIdHolder = new IntHolder();
        pushSupplier_ = StructuredProxyPushSupplierHelper.narrow(_consumerAdmin
                .obtain_notification_push_supplier(ClientType.STRUCTURED_EVENT, _proxyIdHolder));

        Assert.assertNotNull(pushSupplier_);
        Assert.assertNotNull(pushSupplier_.MyType());
        Assert.assertEquals(pushSupplier_.MyType(), ProxyType.PUSH_STRUCTURED);

        Assert.assertEquals(_consumerAdmin, pushSupplier_.MyAdmin());
        pushSupplier_.connect_structured_push_consumer(StructuredPushConsumerHelper
                .narrow(receiverTie._this(testCase_.getORB())));

        connected_ = true;
    }

    public boolean isEventHandled()
    {
        if (expected_ > 0)
        {
            return received_ == expected_;
        }
        return received_ > 0;

    }

    public boolean isConnected()
    {
        return connected_;
    }

    public boolean isError()
    {
        return false;
    }

    public void shutdown() throws FilterNotFound
    {
        if (filterId_ != Integer.MIN_VALUE)
        {
            pushSupplier_.remove_filter(filterId_);
        }
        Assert.assertTrue(!pushSupplier_._non_existent());
        pushSupplier_.disconnect_structured_push_supplier();
        //      testCase_.assertTrue(pushSupplier_._non_existent());

        if (filter_ != null)
        {
            filter_.destroy();
        }
    }
}