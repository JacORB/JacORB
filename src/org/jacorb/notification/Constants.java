package org.jacorb.notification;

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

import org.omg.CosNotification.FifoOrder;

/**
 * Constants.java
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public interface Constants
{
    public final static long DEFAULT_PROXY_POLL_INTERVALL = 1000L;
    public final static String DEFAULT_ORDER_POLICY = "PriorityOrder";
    public final static String DEFAULT_DISCARD_POLICY = "PriorityOrder";
    public final static int DEFAULT_MAX_EVENTS_PER_CONSUMER = 100;
    public final static int DEFAULT_MAX_BATCH_SIZE = 1;
    public final static int DEFAULT_FILTER_POOL_SIZE = 2;
    public final static int DEFAULT_DELIVER_POOL_SIZE = 4;
    public static final int DEFAULT_BACKOUT_INTERVAL = 2000;
    public static final int DEFAULT_EVENTCONSUMER_ERROR_THRESHOLD = 3;
}
