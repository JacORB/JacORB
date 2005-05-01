package org.jacorb.test.notification;

import junit.framework.Assert;

import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPullConsumer;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPullConsumerHelper;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.SequencePullSupplierHelper;
import org.omg.CosNotifyComm.SequencePullSupplierOperations;
import org.omg.CosNotifyComm.SequencePullSupplierPOATie;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class SequencePullSender extends Thread implements SequencePullSupplierOperations,
        TestClientOperations
{
    ORB orb_;

    StructuredEvent[] event_;

    SequenceProxyPullConsumer pullConsumer_;

    boolean error_;

    boolean connected_;

    boolean eventHandled_;

    boolean available_ = false;

    public void run()
    {
        synchronized (this)
        {
            available_ = true;
        }
    }

    public boolean isError()
    {
        return error_;
    }

    public boolean isEventHandled()
    {
        return eventHandled_;
    }

    public SequencePullSender(ORB orb, StructuredEvent[] event)
    {
        event_ = event;
        orb_ = orb;
    }

    public void subscription_change(EventType[] eventType1, EventType[] eventType2)
            throws InvalidEventType
    {
        // ignored
    }

    public StructuredEvent[] pull_structured_events(int number) throws Disconnected
    {
        BooleanHolder _success = new BooleanHolder();
        StructuredEvent[] _event;
        while (true)
        {
            _event = try_pull_structured_events(number, _success);
            if (_success.value)
            {
                return _event;
            }
            Thread.yield();
        }
    }

    public StructuredEvent[] try_pull_structured_events(int number, BooleanHolder booleanHolder)
            throws Disconnected
    {

        booleanHolder.value = false;
        StructuredEvent[] _result = new StructuredEvent[] { NotificationTestUtils
                .getInvalidStructuredEvent(orb_) };

        if (event_ != null)
        {
            synchronized (this)
            {
                if (event_ != null && available_)
                {
                    _result = event_;
                    event_ = null;
                    booleanHolder.value = true;
                    eventHandled_ = true;
                }
            }
        }
        return _result;
    }

    public void disconnect_sequence_pull_supplier()
    {
        connected_ = false;
    }

    public void connect(EventChannel channel, boolean useOrSemantic) throws AdminLimitExceeded,
            AlreadyConnected, TypeError
    {
        SequencePullSupplierPOATie _senderTie = new SequencePullSupplierPOATie(this);
        SupplierAdmin _supplierAdmin = channel.default_supplier_admin();
        IntHolder _proxyId = new IntHolder();
        pullConsumer_ = SequenceProxyPullConsumerHelper.narrow(_supplierAdmin
                .obtain_notification_pull_consumer(ClientType.SEQUENCE_EVENT, _proxyId));

        Assert.assertEquals(ProxyType._PULL_SEQUENCE, pullConsumer_.MyType().value());

        pullConsumer_.connect_sequence_pull_supplier(SequencePullSupplierHelper.narrow(_senderTie
                ._this(orb_)));
        connected_ = true;
    }

    public void shutdown()
    {
        pullConsumer_.disconnect_sequence_pull_consumer();
    }

    public boolean isConnected()
    {
        return connected_;
    }

}
