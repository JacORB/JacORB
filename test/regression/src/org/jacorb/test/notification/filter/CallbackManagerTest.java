/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

package org.jacorb.test.notification.filter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.easymock.MockControl;
import org.jacorb.notification.filter.CallbackManager;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyComm.NotifySubscribe;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class CallbackManagerTest extends TestCase
{
    private CallbackManager objectUnderTest_;

    private MockControl controlSubscription_;

    private NotifySubscribe mockSubscription_;

    private final static EventType[] EMPTY = new EventType[0];

    protected void setUp() throws Exception
    {
        super.setUp();

        objectUnderTest_ = new CallbackManager();
        controlSubscription_ = MockControl.createControl(NotifySubscribe.class);
        mockSubscription_ = (NotifySubscribe) controlSubscription_.getMock();
    }

    public CallbackManagerTest(String name)
    {
        super(name);
    }

    public void testAttach_callback()
    {
        int id = objectUnderTest_.attach_callback(mockSubscription_);

        int[] ids = objectUnderTest_.get_callbacks();
        assertEquals(1, ids.length);
        assertEquals(id, ids[0]);
    }

    public void testDetach_callback()
    {
        assertEquals(0, objectUnderTest_.get_callbacks().length);
        objectUnderTest_.detach_callback(0);
        int id = objectUnderTest_.attach_callback(mockSubscription_);

        assertEquals(1, objectUnderTest_.get_callbacks().length);

        objectUnderTest_.detach_callback(id);

        assertEquals(0, objectUnderTest_.get_callbacks().length);
    }

    public void testGet_callbacks()
    {
        int[] ids = objectUnderTest_.get_callbacks();

        assertNotNull(ids);
        assertEquals(0, ids.length);
    }

    public void testNoChangeDoesNotNotify() throws Exception
    {
        objectUnderTest_.attach_callback(mockSubscription_);

        controlSubscription_.replay();

        objectUnderTest_.changeSet(EMPTY, EMPTY);

        controlSubscription_.verify();
    }

    public void testAddNonExistingDoesNotify() throws Exception
    {
        EventType[] added = new EventType[] { new EventType("domain", "type") };

        mockSubscription_.subscription_change(added, EMPTY);
        controlSubscription_.setMatcher(MockControl.ARRAY_MATCHER);

        controlSubscription_.replay();

        objectUnderTest_.attach_callback(mockSubscription_);

        objectUnderTest_.changeSet(added, EMPTY);

        controlSubscription_.verify();
    }

    public void testDeleteNonExistingDoesNotNotify() throws Exception
    {
        EventType[] removed = new EventType[] { new EventType("domain", "type") };

        controlSubscription_.replay();

        objectUnderTest_.attach_callback(mockSubscription_);

        objectUnderTest_.changeSet(EMPTY, removed);

        controlSubscription_.verify();
    }

    public void testAddMultipleDoesNotifyOnce() throws Exception
    {
        final EventType eventType = new EventType("domain", "type");

        EventType[] added = new EventType[] { eventType, eventType };

        mockSubscription_.subscription_change(new EventType[] { eventType }, EMPTY);
        controlSubscription_.setMatcher(MockControl.ARRAY_MATCHER);

        controlSubscription_.replay();

        objectUnderTest_.attach_callback(mockSubscription_);

        objectUnderTest_.changeSet(added, EMPTY);

        controlSubscription_.verify();
    }

    public void testAddExistingDoesNotNotify() throws Exception
    {
        EventType[] added = new EventType[] { new EventType("domain", "type") };

        mockSubscription_.subscription_change(added, EMPTY);
        controlSubscription_.setMatcher(MockControl.ARRAY_MATCHER);

        controlSubscription_.replay();

        objectUnderTest_.attach_callback(mockSubscription_);

        objectUnderTest_.changeSet(added, EMPTY);
        objectUnderTest_.changeSet(added, EMPTY);

        controlSubscription_.verify();
    }

    public void testReplaceWithEqualSetDoesNotNotify() throws Exception
    {
        EventType[] content = new EventType[] { new EventType("domain", "type") };

        controlSubscription_.replay();

        objectUnderTest_.changeSet(content, EMPTY);

        objectUnderTest_.attach_callback(mockSubscription_);

        objectUnderTest_.replaceWith(content);

        controlSubscription_.verify();
    }

    public void testReplaceNotifiesAboutAdded() throws Exception
    {
        EventType[] content = new EventType[] { new EventType("domain", "type") };

        EventType eventType = new EventType("domain2", "type2");
        EventType[] replace = new EventType[] { new EventType("domain", "type"), eventType };

        mockSubscription_.subscription_change(new EventType[] { eventType }, EMPTY);
        controlSubscription_.setMatcher(MockControl.ARRAY_MATCHER);

        controlSubscription_.replay();

        objectUnderTest_.changeSet(content, EMPTY);

        objectUnderTest_.attach_callback(mockSubscription_);

        objectUnderTest_.replaceWith(replace);

        controlSubscription_.verify();
    }

    public void testReplaceNotifiesAboutRemoved() throws Exception
    {
        EventType[] content = new EventType[] { new EventType("domain", "type") };

        mockSubscription_.subscription_change(EMPTY, content);
        controlSubscription_.setMatcher(MockControl.ARRAY_MATCHER);

        controlSubscription_.replay();

        objectUnderTest_.changeSet(content, EMPTY);

        objectUnderTest_.attach_callback(mockSubscription_);

        objectUnderTest_.replaceWith(EMPTY);

        controlSubscription_.verify();
    }

    public void testDispose()
    {
        objectUnderTest_.attach_callback(mockSubscription_);

        assertEquals(1, objectUnderTest_.get_callbacks().length);

        objectUnderTest_.dispose();

        assertEquals(0, objectUnderTest_.get_callbacks().length);
    }

    public static Test suite()
    {
        return new TestSuite(CallbackManagerTest.class);
    }
}