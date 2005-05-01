package org.jacorb.test.notification;

import junit.framework.Assert;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.EventChannel;
import org.omg.CosEventChannelAdmin.EventChannelHelper;
import org.omg.CosEventChannelAdmin.ProxyPushConsumer;
import org.omg.CosEventChannelAdmin.SupplierAdmin;
import org.omg.CosEventComm.PushSupplierPOA;

/**
 * @author Alphonse Bendt
 */

public class CosEventPushSender extends PushSupplierPOA implements TestClientOperations, Runnable
{
    boolean connected_;

    boolean sent_;

    boolean error_;

    ProxyPushConsumer myConsumer_;

    Any event_;

    ORB orb_;

    public CosEventPushSender(ORB orb, Any event)
    {
        event_ = event;
        orb_ = orb;
    }

    public void disconnect_push_supplier()
    {
        connected_ = false;
    }

    public boolean isConnected()
    {
        return connected_;
    }

    public boolean isEventHandled()
    {
        return sent_;
    }

    public boolean isError()
    {
        return error_;
    }

    public void connect(org.omg.CosNotifyChannelAdmin.EventChannel channel, boolean useOrSemantic)
            throws AlreadyConnected
    {
        Assert.assertNotNull(channel);
        EventChannel _channel = EventChannelHelper.narrow(channel);
        Assert.assertNotNull(_channel);

        SupplierAdmin _admin = _channel.for_suppliers();
        Assert.assertNotNull(_admin);

        myConsumer_ = _admin.obtain_push_consumer();
        Assert.assertNotNull(myConsumer_);

        myConsumer_.connect_push_supplier(_this(orb_));

        connected_ = true;
    }

    public void shutdown()
    {
        myConsumer_.disconnect_push_consumer();
    }

    // Implementation of java.lang.Runnable

    /**
     * Describe <code>run</code> method here.
     * 
     */
    public void run()
    {
        try
        {
            myConsumer_.push(event_);

            event_ = null;
            sent_ = true;
        } catch (Throwable t)
        {
            error_ = true;
        }
    }

}
