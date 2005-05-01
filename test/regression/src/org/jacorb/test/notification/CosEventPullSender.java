package org.jacorb.test.notification;

import org.omg.CORBA.Any;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.EventChannel;
import org.omg.CosEventChannelAdmin.EventChannelHelper;
import org.omg.CosEventChannelAdmin.ProxyPullConsumer;
import org.omg.CosEventChannelAdmin.SupplierAdmin;
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosEventComm.PullSupplierPOA;

import EDU.oswego.cs.dl.util.concurrent.Latch;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class CosEventPullSender extends PullSupplierPOA implements TestClientOperations, Runnable
{
    Any event_;

    Any invalidAny_;

    ProxyPullConsumer myConsumer_;

    Latch latch_ = new Latch();

    private boolean connected_;

    private boolean error_;

    private boolean sent_;

    private final ORB orb_;

    CosEventPullSender(ORB orb, Any event)
    {
        event_ = event;
        orb_ = orb;
    }

    public void run()
    {
        try
        {
            latch_.acquire();
        } catch (InterruptedException ie)
        {
            // ignored
        }
    }

    public Any pull() throws Disconnected
    {
        BooleanHolder _b = new BooleanHolder();
        Any _event;
        while (true)
        {
            _event = try_pull(_b);
            if (_b.value)
            {
                return _event;
            }
            Thread.yield();
        }
    }

    public Any try_pull(BooleanHolder success) throws Disconnected
    {
        Any _event = invalidAny_;
        success.value = false;
        if (event_ != null)
        {
            synchronized (this)
            {
                if (event_ != null)
                {
                    _event = event_;
                    event_ = null;
                    success.value = true;
                    sent_ = true;
                    latch_.release();
                }
            }
        }
        return _event;
    }

    public void disconnect_pull_supplier()
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

    public void shutdown()
    {
        myConsumer_.disconnect_pull_consumer();
    }

    public void connect(org.omg.CosNotifyChannelAdmin.EventChannel channel, boolean useOrSemantic)
            throws AlreadyConnected, TypeError
    {
        invalidAny_ = orb_.create_any();
        EventChannel _channel = EventChannelHelper.narrow(channel);
        SupplierAdmin _admin = _channel.for_suppliers();
        myConsumer_ = _admin.obtain_pull_consumer();

        myConsumer_.connect_pull_supplier(_this(orb_));
        connected_ = true;
    }

}
