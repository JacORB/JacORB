package org.jacorb.notification.engine;

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

import org.jacorb.notification.interfaces.MessageSupplier;
import org.jacorb.notification.util.TaskExecutor;

import org.omg.CosEventComm.Disconnected;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class PullFromSupplierTask extends AbstractTask
{
    private MessageSupplier target_;

    ////////////////////////////////////////

    PullFromSupplierTask(TaskExecutor te) {
        super(te, null, null);
    }

    ////////////////////////////////////////

    public void setTarget( MessageSupplier target )
    {
        target_ = target;
    }


    public void doWork() throws Disconnected
    {
        target_.runPullMessage();

        dispose();
    }


    public void reset()
    {
        super.reset();

        target_ = null;
    }


    public void handleTaskError(AbstractTask task, Throwable error) {
        logger_.fatalError("Error in Task: " + task, error);

        if (error instanceof Disconnected) {
            target_.dispose();
        }
    }


    public void schedule() throws InterruptedException {
        schedule(false);
    }
}
