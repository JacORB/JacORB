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

package org.jacorb.test.notification.jmx;

import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.JMXManageable;

public class TestService implements TestServiceMBean, JMXManageable
{
    private int value_;
    private int invoked_;
    private JMXCallback callback_;
    
    public TestService()
    {
        super();
    }

    public void setValue(int v)
    {
        value_ = v;
    }

    public int getValue()
    {
        return value_;
    }

    public void invokeMethod()
    {
        ++invoked_;
        
        if (callback_ != null)
        {
            callback_.sendJMXNotification("invoked", "Count: " + invoked_);
        }
    }
    
    public int getInvokeCount()
    {
        return invoked_;
    }

    public String getJMXObjectName()
    {
        return null;
    }

    public void registerDisposable(Disposable disposable)
    {
        // no op
    }

    public void dispose()
    {
        // no op
    }
    
    public String[] getJMXNotificationTypes()
    {
        return new String[] {"invoked"};
    }
    
    public void setJMXCallback(JMXCallback callback)
    {
        callback_ = callback;
    }
}
