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

package org.jacorb.notification.jmx;

import java.util.Properties;

import org.jacorb.notification.AbstractChannelFactory;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class EventChannelFactoryControl implements EventChannelFactoryMBean
{
    private AbstractChannelFactory factory_;

    private final static String STARTED = "Started";

    private final static String RUNNING = "Already Running";

    private final static String NOT_RUNNING = "Not Running";

    private final static String STOPPED = "Stopped";

    public String start()
    {
        if (factory_ != null)
        {
            return RUNNING;
        }

        try
        {
            Properties props = new Properties();

            factory_ = AbstractChannelFactory.newFactory(props);

            return STARTED;
        } catch (Exception e)
        {
            throw new RuntimeException("Start failed");
        }
    }

    public String stop()
    {
        if (factory_ != null)
        {
            factory_.dispose();
            factory_ = null;

            return STOPPED;
        }
        return NOT_RUNNING;
    }
}