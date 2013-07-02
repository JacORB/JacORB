package org.jacorb.test.notification;

import junit.framework.Assert;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullSupplier;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullSupplierHelper;
import org.omg.CosNotifyComm.StructuredPullConsumerHelper;
import org.omg.CosNotifyComm.StructuredPullConsumerOperations;
import org.omg.CosNotifyComm.StructuredPullConsumerPOATie;
import org.omg.PortableServer.POA;

public class StructuredPullReceiver extends Thread implements StructuredPullConsumerOperations,
        TestClientOperations
{
    StructuredEvent event_ = null;

    ORB orb_;

    POA poa_;

    boolean connected_;

    StructuredProxyPullSupplier pullSupplier_;

    boolean received_;

    long TIMEOUT = 2000;

    boolean error_;

    public StructuredPullReceiver(ORB orb)
    {
        super();
        orb_ = orb;
    }

    public boolean isConnected()
    {
        return connected_;
    }

    public void connect(EventChannel channel, boolean useOrSemantic) throws AdminLimitExceeded,
            AlreadyConnected
    {

        StructuredPullConsumerPOATie _receiverTie = new StructuredPullConsumerPOATie(this);
        ConsumerAdmin _consumerAdmin = channel.default_consumer_admin();
        IntHolder _proxyId = new IntHolder();

        pullSupplier_ = StructuredProxyPullSupplierHelper.narrow(_consumerAdmin
                .obtain_notification_pull_supplier(ClientType.STRUCTURED_EVENT, _proxyId));

        Assert.assertEquals(pullSupplier_.MyType(), ProxyType.PULL_STRUCTURED);

        pullSupplier_.connect_structured_pull_consumer(StructuredPullConsumerHelper
                .narrow(_receiverTie._this(orb_)));
        connected_ = true;
    }

    public void shutdown()
    {
        Assert.assertTrue(!pullSupplier_._non_existent());
        pullSupplier_.disconnect_structured_pull_supplier();
        Assert.assertTrue(pullSupplier_._non_existent());
        pullSupplier_ = null;
    }

    public boolean isEventHandled()
    {
        return event_ != null;
    }

    public boolean isError()
    {
        return false;
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
                event_ = pullSupplier_.try_pull_structured_event(_success);

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
        } catch (Disconnected d)
        {
            d.printStackTrace();
            error_ = true;
        }
    }

    public void push_structured_event(StructuredEvent event)
    {
        synchronized (this)
        {
            event_ = event;
            this.notifyAll();
        }
    }

    public void disconnect_structured_pull_consumer()
    {
        connected_ = false;
    }

    public void offer_change(EventType[] e1, EventType[] e2)
    {
        // ignored
    }
}
