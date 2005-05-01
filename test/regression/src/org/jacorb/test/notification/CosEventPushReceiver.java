package org.jacorb.test.notification;

import junit.framework.Assert;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.ConsumerAdmin;
import org.omg.CosEventChannelAdmin.EventChannel;
import org.omg.CosEventChannelAdmin.EventChannelHelper;
import org.omg.CosEventChannelAdmin.ProxyPushSupplier;
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosEventComm.PushConsumerPOA;

/**
 * @author Alphonse Bendt
 */

public class CosEventPushReceiver extends PushConsumerPOA implements Runnable, TestClientOperations
{
    Any event_;

    long timeout_ = 2000;

    boolean received_ = false;

    boolean connected_;

    ProxyPushSupplier mySupplier_;

    final ORB orb_;

    public CosEventPushReceiver(ORB orb)
    {
        orb_ = orb;
    }

    public void setTimeOut(long t)
    {
        timeout_ = t;
    }

    public void push(Any event) throws Disconnected
    {
        synchronized (this)
        {
            event_ = event;
            notifyAll();
        }
    }

    public void disconnect_push_consumer()
    {
        connected_ = false;
    }

    public void run()
    {
        if (event_ == null)
        {
            synchronized (this)
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

        if (event_ != null)
        {
            received_ = true;
        }
    }

    public boolean isEventHandled()
    {
        return received_;
    }

    public boolean isConnected()
    {
        return connected_;
    }

    public boolean isError()
    {
        return false;
    }

    public void connect(org.omg.CosNotifyChannelAdmin.EventChannel channel, boolean useOrSemantic)
            throws AlreadyConnected, TypeError
    {

        EventChannel _channel = EventChannelHelper.narrow(channel);
        Assert.assertNotNull(_channel);

        ConsumerAdmin _admin = _channel.for_consumers();
        Assert.assertNotNull(_admin);

        mySupplier_ = _admin.obtain_push_supplier();
        Assert.assertNotNull(mySupplier_);

        mySupplier_.connect_push_consumer(_this(orb_));
        connected_ = true;
    }

    public void shutdown()
    {
        mySupplier_.disconnect_push_supplier();
    }
}
