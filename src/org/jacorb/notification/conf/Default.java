package org.jacorb.notification.conf;

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
 * @author Alphonse Bendt
 * @version $Id$
 */

public interface Default
{
    long DEFAULT_PROXY_POLL_INTERVALL = 1000L;

    String DEFAULT_ORDER_POLICY = "PriorityOrder";

    String DEFAULT_DISCARD_POLICY = "PriorityOrder";

    int DEFAULT_MAX_EVENTS_PER_CONSUMER = 100;

    int DEFAULT_MAX_BATCH_SIZE = 1;

    int DEFAULT_FILTER_POOL_SIZE = 2;

    int DEFAULT_DELIVER_POOL_SIZE = 4;

    int DEFAULT_BACKOUT_INTERVAL = 2000;

    int DEFAULT_EVENTCONSUMER_ERROR_THRESHOLD = 3;

    int DEFAULT_PULL_POOL_SIZE = 2;

    String DEFAULT_THREADPOLICY = "ThreadPool";

    String DEFAULT_FILTER_FACTORY = "builtin";

    int DEFAULT_MAX_NUMBER_SUPPLIERS = Integer.MAX_VALUE;

    int DEFAULT_MAX_NUMBER_CONSUMERS = Integer.MAX_VALUE;

    String DEFAULT_START_TIME_SUPPORTED = "on";

    String DEFAULT_STOP_TIME_SUPPORTED = "on";

    /**
     * per default one concurrent pull operation is allowed per PullSupplier.
     */
    int DEFAULT_CONCURRENT_PULL_OPERATIONS_ALLOWED = 1;

    String DEFAULT_DISPOSE_PROXY_CALLS_DISCONNECT = "on";
}
