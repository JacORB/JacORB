package org.jacorb.test.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.interfaces.ApplicationEvent;
import org.jacorb.notification.interfaces.ProxyEvent;
import org.jacorb.notification.interfaces.ProxyEventListener;
import org.jacorb.notification.servant.ConsumerAdminImpl;
import org.jacorb.notification.servant.IEventChannel;
import org.jacorb.notification.util.QoSPropertySet;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.ConsumerAdminOperations;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.ProxySupplier;
import org.picocontainer.MutablePicoContainer;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class AdminLimitTest extends NotificationTestCase
{
    private ConsumerAdminImpl objectUnderTest_;

    private ConsumerAdminOperations consumerAdmin_;

    public void setUpTest() throws Exception
    {
        QoSPropertySet _qosSettings = new QoSPropertySet(getConfiguration(),
                QoSPropertySet.ADMIN_QOS);

        getPicoContainer().registerComponentImplementation(OfferManager.class);
        getPicoContainer().registerComponentImplementation(SubscriptionManager.class);

        IEventChannel channel = new IEventChannel()
        {
            public MutablePicoContainer getContainer()
            {
                return getPicoContainer();
            }

            public EventChannel getEventChannel()
            {
                return null;
            }

            public int getAdminID()
            {
                return 20;
            }

            public int getChannelID()
            {
                return 10;
            }
            
            public void destroy()
            {
                // nothing to do
            }

            public String getChannelMBean()
            {
                return null;
            }
        };

        objectUnderTest_ = new ConsumerAdminImpl(channel, getORB(), getPOA(),
                getConfiguration(), getMessageFactory(), (OfferManager) getPicoContainer()
                        .getComponentInstance(OfferManager.class), (SubscriptionManager) getPicoContainer()
                        .getComponentInstance(SubscriptionManager.class));

        objectUnderTest_.set_qos(_qosSettings.get_qos());

        consumerAdmin_ = objectUnderTest_;
    }

    public void testBasics() throws Exception
    {
        assertEquals(20, consumerAdmin_.MyID());
    }

    public void testObtainNotificationPullSupplierFiresEvent() throws Exception
    {
        IntHolder _proxyId = new IntHolder();

        final List _events = new ArrayList();

        ProxyEventListener _listener = new ProxyEventListener()
        {
            public void actionProxyCreationRequest(ProxyEvent event) throws AdminLimitExceeded
            {
                _events.add(event);
            }

            public void actionProxyCreated(ProxyEvent event)
            {
                // nothing to do
            }

            public void actionProxyDisposed(ProxyEvent event)
            {
                // ignore
            }
        };

        objectUnderTest_.addProxyEventListener(_listener);

        ProxySupplier _proxySupplier = objectUnderTest_.obtain_notification_pull_supplier(
                ClientType.STRUCTURED_EVENT, _proxyId);

        assertNotNull(_proxySupplier);

        assertEquals(1, _events.size());

        assertEquals(objectUnderTest_, ((ApplicationEvent) _events.get(0)).getSource());
    }

    public void testDenyCreateNotificationPullSupplier() throws Exception
    {
        IntHolder _proxyId = new IntHolder();

        ProxyEventListener _listener = new ProxyEventListener()
        {
            public void actionProxyCreationRequest(ProxyEvent e) throws AdminLimitExceeded
            {
                throw new AdminLimitExceeded();
            }

            public void actionProxyDisposed(ProxyEvent event)
            {
                // ignored
            }

            public void actionProxyCreated(ProxyEvent event)
            {
                // ignored
            }
        };

        objectUnderTest_.addProxyEventListener(_listener);

        try
        {
            objectUnderTest_.obtain_notification_pull_supplier(ClientType.STRUCTURED_EVENT,
                    _proxyId);

            fail();
        } catch (AdminLimitExceeded e)
        {
            // expected
        }
    }

    public void testEvents() throws Exception
    {
        IntHolder _proxyId = new IntHolder();
        final AtomicInteger _counter = new AtomicInteger(0);

        ProxyEventListener _listener = new ProxyEventListener()
        {
            public void actionProxyCreated(ProxyEvent event)
            {
                // ignored
            }

            public void actionProxyDisposed(ProxyEvent event)
            {
                // ignored
            }

            public void actionProxyCreationRequest(ProxyEvent event) throws AdminLimitExceeded
            {
                _counter.incrementAndGet();
            }
        };

        objectUnderTest_.addProxyEventListener(_listener);

        ProxySupplier[] _seqProxySupplier = new ProxySupplier[3];

        _seqProxySupplier[0] = objectUnderTest_.obtain_notification_pull_supplier(
                ClientType.STRUCTURED_EVENT, _proxyId);
        assertEquals(_seqProxySupplier[0], objectUnderTest_.get_proxy_supplier(_proxyId.value));

        _seqProxySupplier[1] = objectUnderTest_.obtain_notification_pull_supplier(
                ClientType.ANY_EVENT, _proxyId);
        assertEquals(_seqProxySupplier[1], objectUnderTest_.get_proxy_supplier(_proxyId.value));

        _seqProxySupplier[2] = objectUnderTest_.obtain_notification_pull_supplier(
                ClientType.SEQUENCE_EVENT, _proxyId);
        assertEquals(_seqProxySupplier[2], objectUnderTest_.get_proxy_supplier(_proxyId.value));

        objectUnderTest_.obtain_pull_supplier();

        assertEquals(3, _counter.get());
    }

    public AdminLimitTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite(AdminLimitTest.class);
    }
}