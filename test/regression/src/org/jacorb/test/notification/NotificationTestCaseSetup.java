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

import org.jacorb.notification.ChannelContext;
import org.jacorb.notification.EventChannelFactoryImpl;
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.engine.DefaultTaskProcessor;
import org.jacorb.notification.queue.EventQueueFactory;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import junit.extensions.TestSetup;
import junit.framework.Test;
import org.apache.avalon.framework.configuration.Configuration;

/**
 * @author Alphonse Bendt
 */

public class NotificationTestCaseSetup extends TestSetup {

    private ORB orb_;
    private POA poa_;
    private Thread orbThread_;
    private NotificationTestUtils testUtils_;
    private EventChannelFactoryImpl eventChannelFactory_;

    private ChannelContext channelContext_;

    private ORB clientORB_;

    ////////////////////////////////////////

    public NotificationTestCaseSetup(Test suite) throws Exception {
        super(suite);
    }

    ////////////////////////////////////////

    public NotificationTestUtils getTestUtils() {
        return(testUtils_);
    }


    public void setUp() throws Exception {
        super.setUp();

        orb_ = ORB.init(new String[0], null);
        poa_ = POAHelper.narrow(orb_.resolve_initial_references("RootPOA"));

        testUtils_ = new NotificationTestUtils(orb_);

        channelContext_ = new ChannelContext();

        channelContext_.setORB(orb_);

        channelContext_.setPOA(poa_);

        channelContext_.setTaskProcessor(new DefaultTaskProcessor());

        channelContext_.setEventQueueFactory(new EventQueueFactory());

        channelContext_.setMessageFactory(new MessageFactory());

        channelContext_.configure(getConfiguration());

        poa_.the_POAManager().activate();

        orbThread_ = new Thread(
                   new Runnable() {
                       public void run() {
                           orb_.run();
                       }
                   });

        orbThread_.setDaemon(true);

        orbThread_.start();

        clientORB_ = ORB.init(new String[] {}, null);

//         new Thread(new Runnable() {
//                 public void run() {
//                     clientORB_.run();
//                 }
//             }).start();

    }


    public void tearDown() throws Exception {
        super.tearDown();

        if (eventChannelFactory_ != null) {
            eventChannelFactory_.dispose();
        }

        channelContext_.dispose();

        orb_.shutdown(true);

        clientORB_.shutdown(true);
    }


    public ORB getClientORB() {
        return clientORB_;
    }

    public EventChannelFactoryImpl getFactoryServant() throws Exception {
        if (eventChannelFactory_ == null) {
            eventChannelFactory_ = EventChannelFactoryImpl.newFactory();
        }

        return eventChannelFactory_;
    }


    public ORB getORB() {
        return orb_;
    }


    public POA getPOA() {
        return poa_;
    }

    public Configuration getConfiguration() {
        return (((org.jacorb.orb.ORB)getORB()).getConfiguration());
    }

    public ChannelContext getChannelContext() {
        return channelContext_;
    }
}
