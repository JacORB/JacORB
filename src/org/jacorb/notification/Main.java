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

import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class Main implements WrapperListener
{

    private EventChannelFactoryImpl application_;

    private Main()
    {
        super();
    }

    // Implementation of org.tanukisoftware.wrapper.WrapperListener

    public Integer start( String[] stringArray )
    {
        try
        {
            application_ = EventChannelFactoryImpl.newFactory( stringArray );

            return null;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return new Integer( 1 );
        }
    }


    public int stop( int n )
    {
        EventChannelFactoryImpl.ShutdownCallback cb =
            new EventChannelFactoryImpl.ShutdownCallback()
            {
                public void needTime( int time )
                {
                    WrapperManager.signalStopping( time );
                }

                public void shutdownComplete()
                {}

            };

        application_.shutdown( cb );

        return 0;
    }

    public void controlEvent( int event )
    {
        if ( WrapperManager.isControlledByNativeWrapper() )
        {
            // The Wrapper will take care of this event
        }
        else
        {
            // We are not being controlled by the Wrapper, so
            //  handle the event ourselves.

            if ( ( event == WrapperManager.WRAPPER_CTRL_C_EVENT ) ||
                 ( event == WrapperManager.WRAPPER_CTRL_CLOSE_EVENT ) ||
                 ( event == WrapperManager.WRAPPER_CTRL_SHUTDOWN_EVENT ) )
            {
                WrapperManager.stop( 0 );
            }
        }
    }


    public static void main( String[] args )
    {
        WrapperManager.start( new Main(), args );
    }
}
