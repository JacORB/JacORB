package org.jacorb.test.notification;

import junit.framework.Assert;

import org.omg.CORBA.Any;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.AdminNotFound;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosNotifyChannelAdmin.ProxyPullSupplier;
import org.omg.CosNotifyChannelAdmin.ProxyPullSupplierHelper;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyComm.PullConsumerPOA;
import org.omg.PortableServer.POA;

/**
 * @author Alphonse Bendt
 */

public class AnyPullReceiver
            extends PullConsumerPOA
            implements Runnable,
            TestClientOperations
{
    Any event_ = null;
    boolean received_ = false;
    boolean error_ = false;
    ORB orb_;
    POA poa_;
    ProxyPullSupplier mySupplier_;
    long TIMEOUT = 5000;
    boolean connected_;
    IntHolder adminId_;
    ConsumerAdmin myAdmin_;
    NotificationTestCase testCase_;

    public AnyPullReceiver(NotificationTestCase testCase)
    {
        testCase_ = testCase;
    }

    public void connect(EventChannel channel,
                        boolean useOrSemantic) throws AdminNotFound, AlreadyConnected, AdminLimitExceeded
    {

        orb_ = testCase_.getORB();
        poa_ = testCase_.getPOA();

        IntHolder _proxyId = new IntHolder();
        IntHolder _adminId = new IntHolder();

        if (useOrSemantic)
        {
            adminId_ = new IntHolder();
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
            ProxyPullSupplierHelper.narrow(myAdmin_.
                                           obtain_notification_pull_supplier(ClientType.ANY_EVENT, _proxyId));

        Assert.assertEquals(ProxyType._PULL_ANY,
                               mySupplier_.MyType().value());


        mySupplier_.connect_any_pull_consumer(_this(orb_));
        connected_ = true;
    }

    public void shutdown()
    {
        mySupplier_.disconnect_pull_supplier();
        mySupplier_ = null;

        if (myAdmin_ != null)
        {
            myAdmin_.destroy();
            myAdmin_ = null;
        }
    }

    void reset()
    {
        error_ = false;
        received_ = false;
        event_ = null;
    }

    public boolean isConnected()
    {
        return connected_;
    }

    public boolean isEventHandled()
    {
        return received_;
    }

    public boolean isError()
    {
        return error_;
    }

    public void run()
    {
        BooleanHolder _success = new BooleanHolder();
        _success.value = false;
        long _startTime = System.currentTimeMillis();

        try
        {
            while (true)
            {
                event_ = mySupplier_.try_pull(_success);

                if (_success.value)
                {
                    received_ = true;
                    break;
                }

                if (System.currentTimeMillis() < _startTime + TIMEOUT)
                {
                    Thread.yield();
                }
                else
                {
                    received_ = false;
                    break;
                }
            }
        }
        catch (Disconnected d)
        {
            d.printStackTrace();
            error_ = true;
        }
    }

    public void offer_change(EventType[] e1, EventType[] e2)
    {}

    public void disconnect_pull_consumer()
    {
        connected_ = false;
    }

} // AnyPullReceiver
