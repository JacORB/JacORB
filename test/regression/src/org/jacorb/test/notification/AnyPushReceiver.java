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
import org.omg.PortableServer.POA;

import EDU.oswego.cs.dl.util.concurrent.CyclicBarrier;

public class AnyPushReceiver
    extends PushConsumerPOA
    implements Runnable,
               TestClientOperations
{
    Any event_ = null;
    ORB orb_;
    POA poa_;
    long receiveTime_;
    CyclicBarrier barrier_;

    ProxyPushSupplier mySupplier_;
    PerformanceListener perfListener_;

    boolean connected_;
    int expected_ = 1;
    int received_ = 0;
    long TIMEOUT = 3000L;
    long TIMEOUT_OFF = 0;
    int filterId_ = Integer.MIN_VALUE;
    NotificationTestCase testCase_;
    ConsumerAdmin myAdmin_;

    private Object lock_ = new Object();

    public AnyPushReceiver(NotificationTestCase testCase)
    {
        testCase_ = testCase;
    }


    public AnyPushReceiver(NotificationTestCase testCase,
                           PerformanceListener listener,
                           int expected)
    {
        perfListener_ = listener;
        expected_ = expected;
        testCase_ = testCase;
    }


    public void setExpected(int e)
    {
        expected_ = e;
    }


    public void setPerformanceListener(PerformanceListener listener)
    {
        perfListener_ = listener;
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
        if (expected_ > 0)
        {
            return received_ == expected_;
        }
        else
        {
            return received_ > 0;
        }
    }


    public void setTimeOut(long timeout)
    {
        TIMEOUT = timeout;
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


    public void connect(EventChannel channel,
                        boolean useOrSemantic)

    throws AdminLimitExceeded,
                TypeError,
                AlreadyConnected,
                AdminNotFound
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

        mySupplier_ =
            ProxyPushSupplierHelper.narrow(myAdmin_.obtain_notification_push_supplier(ClientType.ANY_EVENT, _proxyId));

        Assert.assertEquals(ProxyType._PUSH_ANY,
                               mySupplier_.MyType().value());


        mySupplier_.connect_any_push_consumer(_this(testCase_.getORB()));

        connected_ = true;
    }

    public int getReceived()
    {
        return received_;
    }

    public void run()
    {

        if (!isEventHandled())
        {
            try
            {
                synchronized (lock_)
                {
                    lock_.wait(TIMEOUT);
                }

            }
            catch (InterruptedException e)
            {}
        }

        if (barrier_ != null)
        {
            try
            {
                barrier_.barrier();
            }
            catch (InterruptedException ie)
            {}
        }
    }

    public void push(Any any) throws Disconnected
    {
        received_++;

        if (perfListener_ != null)
        {
            perfListener_.eventReceived(any, System.currentTimeMillis());
        }

        if (expected_ > 0 && (received_ == expected_))
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
    {}
}
