package org.jacorb.test.notification;

import junit.framework.Assert;

import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.AdminNotFound;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosNotifyChannelAdmin.ProxyPushSupplier;
import org.omg.CosNotifyChannelAdmin.ProxyPushSupplierHelper;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyComm.PushConsumerPOA;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterNotFound;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class AnyPushReceiver extends PushConsumerPOA implements Runnable, TestClientOperations
{
    ORB orb_;

    long receiveTime_;

    CyclicBarrier barrier_;

    ProxyPushSupplier mySupplier_;

    boolean connected_;

    int numberOfExpectedEvents_ = 1;

    private final AtomicInteger received_ = new AtomicInteger(0);

    long timeout_ = 8000L;

    int filterId_ = Integer.MIN_VALUE;

    ConsumerAdmin myAdmin_;

    private final Object lock_ = new Object();

    public AnyPushReceiver(ORB orb)
    {
        orb_ = orb;
    }

    public void setNumberOfExpectedEvents(int number)
    {
        numberOfExpectedEvents_ = number;
    }

    public void setFilter(Filter filter)
    {
        filterId_ = mySupplier_.add_filter(filter);
    }

    public void addAdminFilter(Filter filter)
    {
        Assert.assertNotNull(myAdmin_);
        myAdmin_.add_filter(filter);
    }

    public void addProxyFilter(Filter filter)
    {
        Assert.assertNotNull(mySupplier_);
        mySupplier_.add_filter(filter);
    }

    public boolean isEventHandled()
    {
        if (numberOfExpectedEvents_ > 0)
        {
            return received_.get() == numberOfExpectedEvents_;
        }

        return received_.get() > 0;
    }

    public void setTimeOut(long timeout)
    {
        timeout_ = timeout;
    }

    public void setBarrier(CyclicBarrier barrier)
    {
        barrier_ = barrier;
    }

    public void shutdown() throws FilterNotFound
    {
        if (filterId_ != Integer.MIN_VALUE)
        {
            mySupplier_.remove_filter(filterId_);
        }
        mySupplier_.disconnect_push_supplier();

        myAdmin_.destroy();
    }

    public void connect(EventChannel channel, boolean useOrSemantic)
        throws AdminLimitExceeded, TypeError, AlreadyConnected, AdminNotFound
    {
        IntHolder _proxyId = new IntHolder();
        IntHolder _adminId = new IntHolder();

        if (useOrSemantic)
        {
            myAdmin_ = channel.new_for_consumers(InterFilterGroupOperator.OR_OP, _adminId);
            Assert.assertEquals(InterFilterGroupOperator.OR_OP, myAdmin_.MyOperator());
        }
        else
        {
            myAdmin_ = channel.new_for_consumers(InterFilterGroupOperator.AND_OP, _adminId);
            Assert.assertEquals(InterFilterGroupOperator.AND_OP, myAdmin_.MyOperator());
        }

        Assert.assertEquals(myAdmin_, channel.get_consumeradmin(_adminId.value));

        mySupplier_ = ProxyPushSupplierHelper.narrow(myAdmin_.obtain_notification_push_supplier(
                ClientType.ANY_EVENT, _proxyId));

        Assert.assertEquals(ProxyType._PUSH_ANY, mySupplier_.MyType().value());

        mySupplier_.connect_any_push_consumer(_this(orb_));

        connected_ = true;
    }

    public int getReceived()
    {
        return received_.get();
    }

    public void run()
    {
        if (!isEventHandled())
        {
            try
            {
                synchronized (lock_)
                {
                    lock_.wait(timeout_);
                }
            } catch (InterruptedException e)
            {
                // ignored
            }
        }

        if (barrier_ != null)
        {
            try
            {
                barrier_.await();
            } catch (Exception e)
            {
                // ignored
            }
        }
    }

    public void push(Any any) throws Disconnected
    {
        received_.incrementAndGet();

        if (numberOfExpectedEvents_ > 0 && (received_.get() == numberOfExpectedEvents_))
        {
            synchronized (lock_)
            {
                lock_.notifyAll();
            }
        }
    }

    public long calcTotalTime(long start)
    {
        return (receiveTime_ - start);
    }

    public boolean isConnected()
    {
        return connected_;
    }

    public boolean isError()
    {
        return false;
    }

    public void disconnect_push_consumer()
    {
        connected_ = false;
    }

    public void offer_change(EventType[] e1, EventType[] e2)
    {
        // ignored
    }
}