package org.jacorb.test.notification;

import org.omg.CORBA.Any;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.ConsumerAdmin;
import org.omg.CosEventChannelAdmin.EventChannel;
import org.omg.CosEventChannelAdmin.EventChannelHelper;
import org.omg.CosEventChannelAdmin.ProxyPushSupplier;
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosEventComm.PushConsumerPOA;

import org.jacorb.util.Debug;

import org.apache.avalon.framework.logger.Logger;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class CosEventPushReceiver extends PushConsumerPOA implements Runnable, TestClientOperations {

    Any event_;
    long timeout_ = 2000;
    boolean received_ = false;
    boolean connected_;
    ProxyPushSupplier mySupplier_;
    Logger logger_ = Debug.getNamedLogger(getClass().getName());
    NotificationTestCase currentTest_;

    public CosEventPushReceiver(NotificationTestCase testCase) {
        currentTest_ = testCase;
    }

    public void setTimeOut(long t) {
        timeout_ = t;
    }

    public void push(Any event) throws Disconnected {
        synchronized(this) {
            event_ = event;
            notifyAll();
        }
    }

    public void disconnect_push_consumer() {
        connected_ = false;
    }

    public void run() {
        if (event_ == null) {
            synchronized(this) {
                try {
                    wait(timeout_);
                } catch (InterruptedException e) {}
            }
        }

        if (event_ != null) {
            received_ = true;
        } else {
            logger_.info("Timout !!!");
        }
    }

    public boolean isEventHandled() {
        return received_;
    }

    public boolean isConnected() {
        return connected_;
    }

    public boolean isError() {
        return false;
    }

    public void connect(org.omg.CosNotifyChannelAdmin.EventChannel channel,
                        boolean useOrSemantic) throws AlreadyConnected, TypeError {

        EventChannel _channel = EventChannelHelper.narrow(channel);
        currentTest_.assertNotNull(_channel);

        ConsumerAdmin _admin = _channel.for_consumers();
        currentTest_.assertNotNull(_admin);

        mySupplier_ = _admin.obtain_push_supplier();
        currentTest_.assertNotNull(mySupplier_);

        mySupplier_.connect_push_consumer(_this(currentTest_.getORB()));
        connected_ = true;
    }

    public void shutdown() {
        mySupplier_.disconnect_push_supplier();
    }
}
