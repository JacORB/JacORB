package org.jacorb.test.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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
 */

import org.jacorb.notification.OfferManager;

import org.omg.CosNotification.EventType;
import org.omg.CosNotifyComm.NotifyPublishOperations;

import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.ArrayList;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class OfferManagerTest extends TestCase {

    public static final EventType[] EMPTY_EVENT_TYPE_ARRAY = new EventType[0];

    OfferManager offerManager_;
    List added_;
    List removed_;
    NotifyPublishOperations listener_;

    ////////////////////////////////////////

    public OfferManagerTest (String name){
        super(name);
    }

    ////////////////////////////////////////

    public void setUp() throws Exception {
        offerManager_ = new OfferManager();
        added_ = new ArrayList();
        removed_ = new ArrayList();
        listener_ = new NotifyPublishOperations() {
                public void offer_change(EventType[] added, EventType[] removed) {
                    for (int x=0; x<added.length; ++x) {
                        added_.add(added[x]);
                    }

                    for (int x=0; x<removed.length; ++x) {
                        removed_.add(removed[x]);
                    }
                }
            };
    }


    public void testRemoveNotifies() throws Exception {
        EventType[] _toBeAdded = new EventType[] {new EventType("domain1", "type1")};

        offerManager_.offer_change(_toBeAdded, EMPTY_EVENT_TYPE_ARRAY);

        offerManager_.addListener(listener_);

        offerManager_.offer_change(EMPTY_EVENT_TYPE_ARRAY,
                                   new EventType[] { new EventType("domain1", "type1") });

        assertEquals(0, added_.size());
        assertEquals(1, removed_.size());
        assertEquals("domain1", ((EventType)removed_.get(0)).domain_name);
        assertEquals("type1", ((EventType)removed_.get(0)).type_name);

        // shouldn't cause a problem
        offerManager_.offer_change(EMPTY_EVENT_TYPE_ARRAY,
                                   new EventType[] { new EventType("domain1", "type1") });
    }


    public void testAddNotifies() throws Exception {
        EventType[] _toBeAdded = new EventType[] {new EventType("domain1", "type1")};

        offerManager_.offer_change(_toBeAdded, EMPTY_EVENT_TYPE_ARRAY);

        offerManager_.addListener(listener_);

        _toBeAdded = new EventType[] {new EventType("domain2", "type2")};

        offerManager_.offer_change(_toBeAdded, EMPTY_EVENT_TYPE_ARRAY);

        assertEquals(1, added_.size());

        assertEquals("domain2", ((EventType)added_.get(0)).domain_name);
        assertEquals("type2", ((EventType)added_.get(0)).type_name);

        assertEquals(0, removed_.size());

        // another offer with known event types should cause a notification
        offerManager_.offer_change(_toBeAdded, EMPTY_EVENT_TYPE_ARRAY);

        assertEquals(1, added_.size());
    }


    public static TestSuite suite(){
        TestSuite suite = new TestSuite(OfferManagerTest.class);

        return suite;
    }


    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
