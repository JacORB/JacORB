package org.jacorb.notification.conf;

import org.jacorb.notification.engine.TaskProcessorRetryStrategyFactory;
import org.jacorb.notification.util.WeakCacheWildcardMap;

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

    /**
     * 0 means no limit
     */
    int DEFAULT_MAX_NUMBER_SUPPLIERS = 0;

    /**
     * 0 means no limit
     */
    int DEFAULT_MAX_NUMBER_CONSUMERS = 0;

    String DEFAULT_START_TIME_SUPPORTED = "on";

    String DEFAULT_STOP_TIME_SUPPORTED = "on";

    /**
     * per default one concurrent pull operation is allowed per PullSupplier.
     */
    int DEFAULT_CONCURRENT_PULL_OPERATIONS_ALLOWED = 1;

    String DEFAULT_DISPOSE_PROXY_CALLS_DISCONNECT = "on";

    String DEFAULT_LAZY_DEFAULT_ADMIN_INIT = "on";

    String DEFAULT_REJECT_NEW_EVENTS = "off";

    /**
     * 0 means no limit
     */
    int DEFAULT_MAX_QUEUE_LENGTH = 0;

    String WILDCARDMAP_DEFAULT = WeakCacheWildcardMap.class.getName();

    String DEFAULT_RETRY_STRATEGY_FACTORY = TaskProcessorRetryStrategyFactory.class.getName();
}
