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

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public interface Configuration
{
    String FILTER_POOL_WORKERS =
        "jacorb.notification.filter.thread_pool_size";

    String DELIVER_POOL_WORKERS =
        "jacorb.notification.proxysupplier.thread_pool_size";

    String PULL_POOL_WORKERS =
        "jacorb.notification.proxyconsumer.thread_pool_size";

    String PULL_CONSUMER_POLLINTERVALL =
        "jacorb.notification.supplier.poll_intervall";

    String MAX_BATCH_SIZE =
        "jacorb.notification.max_batch_size";

    String MAX_EVENTS_PER_CONSUMER =
        "jacorb.notification.max_events_per_consumer";

    String ORDER_POLICY =
        "jacorb.notification.order_policy";

    String DISCARD_POLICY =
        "jacorb.notification.discard_policy";

    String BACKOUT_INTERVAL =
        "jacorb.notification.consumer.backout_interval";

    String EVENTCONSUMER_ERROR_THRESHOLD =
        "jacorb.notification.consumer.error_threshold";

    String THREADPOLICY =
        "jacorb.notification.proxysupplier.threadpolicy";

    String FILTER_FACTORY =
        "jacorb.notification.default_filter_factory";

    String MAX_NUMBER_CONSUMERS =
        "jacorb.notification.consumer.max_number";

    String MAX_NUMBER_SUPPLIERS =
        "jacorb.notification.supplier.max_number";

    String START_TIME_SUPPORTED =
        "jacorb.notification.start_time_supported";

    String STOP_TIME_SUPPORTED =
        "jacorb.notification.stop_time_supported";

    String DISPOSE_PROXY_CALLS_DISCONNECT =
        "jacorb.notification.proxy.destroy_causes_disconnect";

}

