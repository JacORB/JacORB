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

import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.AbstractPoolable;
import org.jacorb.notification.util.AbstractObjectPool;

import org.omg.CORBA.Any;
import org.omg.CosNotification.StructuredEvent;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class MessageFactory implements Disposable
{
    private AbstractObjectPool anyMessagePool_ =
        new AbstractObjectPool()
        {
            public Object newInstance()
            {
                AbstractPoolable _p =
                    new AnyMessage();
                return _p;
            }

            public void passivateObject( Object o )
            {
            }

            public void activateObject( Object o )
            {
                ( ( AbstractPoolable ) o ).reset();
                ( ( AbstractPoolable ) o ).setObjectPool( this );
            }
        };

    private AbstractObjectPool structuredEventMessagePool_ =
        new AbstractObjectPool()
        {
            public Object newInstance()
            {
                AbstractPoolable _p = new StructuredEventMessage();
                return _p;
            }

            public void passivateObject( Object o )
            {
            }

            public void activateObject( Object o )
            {
                ( ( AbstractPoolable ) o ).reset();
                ( ( AbstractPoolable ) o ).setObjectPool( this );
            }
        };


    public void init()
    {
        anyMessagePool_.init();
        structuredEventMessagePool_.init();
    }

    public void dispose() {
        structuredEventMessagePool_.dispose();
        anyMessagePool_.dispose();
    }

    // Used by the Proxies

    public Message newEvent( Any event,
                                  FilterStage firstStage )
    {
        Message _e = newEvent( event );

        _e.setInitialFilterStage( firstStage );

        return _e;
    }

    public Message newEvent( StructuredEvent event,
                                  FilterStage firstStage )
    {
        Message _e = newEvent( event );

        _e.setInitialFilterStage( firstStage );

        return _e;
    }

    public Message newEvent( Any event )
    {
        AnyMessage _e =
            ( AnyMessage ) anyMessagePool_.lendObject();

        _e.setAny( event );

        return _e.getHandle();
    }

    public Message newEvent( StructuredEvent event )
    {
        StructuredEventMessage _e =
            ( StructuredEventMessage ) structuredEventMessagePool_.lendObject();

        _e.setStructuredEventValue( event );

        return _e.getHandle();
    }

}
