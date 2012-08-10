package org.jacorb.notification.engine;

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

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class FilterSupplierAdminTask extends AbstractFilterTask
{
    private static int sCount = 0;

    private int id_ = ++sCount;

    private boolean skip_ = false;

    ////////////////////////////////////////

    public FilterSupplierAdminTask(TaskFactory taskFactory, TaskExecutor taskExecutor)
    {
        super(taskFactory, taskExecutor);
    }

    ////////////////////////////////////////

    public String toString()
    {
        return "[FilterSupplierAdminTask#" + id_ + "]";
    }

    public void setSkip(boolean skip)
    {
        skip_ = skip;
    }

    public void reset()
    {
        super.reset();

        skip_ = false;
    }

    public void doFilter() throws InterruptedException
    {
        final boolean _forward = filter();

        if (_forward)
        {
            getTaskFactory().newFilterConsumerAdminTask(this).schedule();
        }
    }

    private boolean filter()
    {
        final boolean _forward;

        // process attached filters. as an Event passes only 1
        // SupplierAdmin we can assume constant array size here

        if (!skip_)
        {
            _forward = getMessage().match(arrayCurrentFilterStage_[0]);
        }
        else
        {
            _forward = true;
        }

        if (_forward)
        {
            addFilterStage(arrayCurrentFilterStage_[0].getSubsequentFilterStages());
        }

        return _forward;
    }
}