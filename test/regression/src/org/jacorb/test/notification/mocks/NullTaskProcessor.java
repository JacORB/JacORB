package org.jacorb.test.notification.mocks;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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
 */

import org.jacorb.notification.interfaces.MessageSupplier;
import org.jacorb.notification.interfaces.MessageConsumer;
import org.jacorb.notification.servant.AbstractProxySupplier;
import org.jacorb.notification.engine.TaskFactory;
import org.jacorb.notification.engine.TaskExecutor;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.engine.TaskProcessor;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class NullTaskProcessor implements TaskProcessor {

    public TaskFactory getTaskFactory() {
        return null;
    }

    public long getBackoutInterval() {
        return 0;
    }

    public void configureTaskExecutor(AbstractProxySupplier abstractProxySupplier) {

    }

    public TaskExecutor getFilterTaskExecutor() {
        return null;
    }

    public void processMessage(Message message) {

    }

    public void scheduleTimedPullTask(MessageSupplier messageSupplier) throws InterruptedException {

    }

    public void scheduleTimedPushTask(MessageConsumer messageConsumer) throws InterruptedException {

    }

    public Object executeTaskPeriodically(long l, Runnable runnable, boolean flag) {
        return null;
    }

    public void cancelTask(Object object) {

    }

    public Object executeTaskAfterDelay(long l, Runnable runnable) {
        return null;
    }
}
