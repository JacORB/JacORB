/*
 * JacORB - a free Java ORB
 * 
 * Copyright (C) 1999-2004 Gerald Brose
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Library General Public License as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139,
 * USA.
 *  
 */

package org.jacorb.notification.container;

import org.apache.avalon.framework.configuration.Configuration;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.engine.DefaultTaskExecutor;
import org.jacorb.notification.engine.TaskExecutor;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.AbstractComponentAdapter;

public class ThreadPoolTaskExecutorComponentAdapter extends AbstractComponentAdapter
{
    public ThreadPoolTaskExecutorComponentAdapter()
    {
        super(TaskExecutor.class, DefaultTaskExecutor.class);
    }

    public Object getComponentInstance(PicoContainer container)
    {
        final Configuration config = (Configuration) container
                .getComponentInstance(Configuration.class);

        final int value = config.getAttributeAsInteger(Attributes.DELIVER_POOL_WORKERS,
                Default.DEFAULT_DELIVER_POOL_SIZE);

        return new DefaultTaskExecutor("DeliverThread", value);
    }

    public void verify(PicoContainer container)
    {
    }
}