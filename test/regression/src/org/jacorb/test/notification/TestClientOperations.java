package org.jacorb.test.notification;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyFilter.FilterNotFound;
import org.omg.CosNotifyChannelAdmin.AdminNotFound;

/**
 * TestClientOperations.java
 *
 *
 * Created: Mon Dec 09 18:43:07 2002
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public interface TestClientOperations {
    
    public boolean isConnected();
    public boolean isEventHandled();
    public boolean isError();
    public void connect(NotificationTestCaseSetup setup, EventChannel eventChannel, boolean useOrSemantic) throws AlreadyConnected, TypeError, AdminLimitExceeded, AdminNotFound;

    public void shutdown() throws FilterNotFound;

}// TestClientOperations
