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

package org.jacorb.test.notification.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jacorb.notification.EventTypeWrapper;
import org.omg.CosNotification.EventType;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class EventTypeUtilTest extends TestCase
{
    public void testToString()
    {
        EventType et = new EventType();
        et.domain_name = "domain";
        et.type_name = "type";
        
        String str = EventTypeWrapper.toString(et);
        assertNotNull(str);
        
        assertEquals("domain/type", str);
    }
    
    public void testArrayToString()
    {
        EventType et = new EventType();
        et.domain_name = "domain";
        et.type_name = "type";
        
        EventType et2 = new EventType();
        et2.domain_name = "domain2";
        et2.type_name = "type2";
        
        String str = EventTypeWrapper.toString(new EventType[]{et, et2});
        
        assertNotNull(str);
    }
    
    public static Test suite()
    {
        return new TestSuite(EventTypeUtilTest.class);
    }
}