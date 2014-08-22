/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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

package org.jacorb.notification.engine;

import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.interfaces.IProxyPushSupplier;

/**
 * @author Alphonse Bendt
 */
public class TaskProcessorRetryStrategyFactory implements RetryStrategyFactory
{
    private final TaskProcessor taskProcessor_;
    private final int backoutInterval_;

    public TaskProcessorRetryStrategyFactory(Configuration config, TaskProcessor taskProcessor) throws ConfigurationException
    {
        super();

        backoutInterval_ = config.getAttributeAsInteger(Attributes.BACKOUT_INTERVAL,
                Default.DEFAULT_BACKOUT_INTERVAL);

        taskProcessor_ = taskProcessor;
    }

    public RetryStrategy newRetryStrategy(IProxyPushSupplier pushSupplier,
            PushOperation pushOperation)
    {
        return new TaskProcessorRetryStrategy(pushSupplier, pushOperation, taskProcessor_, backoutInterval_);
    }
}
