package org.jacorb.notification;

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

import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class WrapperMain implements WrapperListener
{
    private static final Runnable CMD_WRAPPERMANAGER_STOP = new Runnable()
    {
        public void run()
        {
            WrapperManager.stop(0);
        }
    };
    
    private static final EventChannelFactoryImpl.ShutdownCallback WRAPPERMANAGER_BEGIN_SHUTDOWN = new EventChannelFactoryImpl.ShutdownCallback()
    {
        public void needTime(int time)
        {
            WrapperManager.signalStopping(time);
        }

        public void shutdownComplete()
        {
            // no operation
        }
    };

    private AbstractChannelFactory application_;

    // //////////////////////////////////////

    private WrapperMain()
    {
        super();
    }

    // //////////////////////////////////////

    // Implementation of org.tanukisoftware.wrapper.WrapperListener

    public Integer start(String[] args)
    {
        try
        {
            application_ = ConsoleMain.newFactory(args);

            application_.setDestroyMethod(CMD_WRAPPERMANAGER_STOP);

            return null;
        } catch (Exception e)
        {
            e.printStackTrace();

            return new Integer(1);
        }
    }

    public int stop(int n)
    {
        application_.shutdown(WRAPPERMANAGER_BEGIN_SHUTDOWN);

        return 0;
    }

    public void controlEvent(int event)
    {
        if (WrapperManager.isControlledByNativeWrapper())
        {
            // The Wrapper will take care of this event
        }
        else
        {
            // We are not being controlled by the Wrapper, so
            // handle the event ourselves.

            if ((event == WrapperManager.WRAPPER_CTRL_C_EVENT)
                    || (event == WrapperManager.WRAPPER_CTRL_CLOSE_EVENT)
                    || (event == WrapperManager.WRAPPER_CTRL_SHUTDOWN_EVENT))
            {
                WrapperManager.stop(0);
            }
        }
    }

    public static void main(String[] args)
    {
        WrapperManager.start(new WrapperMain(), args);
    }
}
