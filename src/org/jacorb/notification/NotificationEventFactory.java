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
import org.jacorb.notification.interfaces.Poolable;
import org.jacorb.notification.util.ObjectPoolBase;
import org.omg.CORBA.Any;
import org.omg.CosNotification.StructuredEvent;

/**
 * NotificationEventFactory.java
 *
 *
 * Created: Tue Nov 05 18:53:27 2002
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class NotificationEventFactory implements Disposable
{
    ApplicationContext appContext_;

    private ObjectPoolBase notificationAnyEventPool_ =
        new ObjectPoolBase()
        {
            public Object newInstance()
            {
                Poolable _p =
                    new NotificationAnyEvent( appContext_ );
                return _p;
            }

            public void passivateObject( Object o )
            {
            }

            public void activateObject( Object o )
            {
                ( ( Poolable ) o ).reset();
                ( ( Poolable ) o ).setObjectPool( this );
            }
        };

    private ObjectPoolBase notificationStructuredEventPool_ =
        new ObjectPoolBase()
        {
            public Object newInstance()
            {
                Poolable _p = new NotificationStructuredEvent( appContext_ );
                return _p;
            }

            public void passivateObject( Object o )
            {
            }

            public void activateObject( Object o )
            {
                ( ( Poolable ) o ).reset();
                ( ( Poolable ) o ).setObjectPool( this );
            }
        };

    public NotificationEventFactory( ApplicationContext appContext )
    {
        appContext_ = appContext;
    }

    public void init()
    {
        notificationAnyEventPool_.init();
        notificationStructuredEventPool_.init();
    }

    public void dispose() {
	notificationStructuredEventPool_.dispose();
	notificationAnyEventPool_.dispose();
    }

    // Used by the Proxies

    public NotificationEvent newEvent( Any event, FilterStage firstStage )
    {
        NotificationEvent _e = newEvent( event );

        _e.setFilterStage( firstStage );

        return _e;
    }

    public NotificationEvent newEvent( StructuredEvent event, 
				       FilterStage firstStage )
    {
        NotificationEvent _e = newEvent( event );

        _e.setFilterStage( firstStage );

        return _e;
    }

    public NotificationEvent newEvent( Any event )
    {
        NotificationAnyEvent _e =
            ( NotificationAnyEvent ) notificationAnyEventPool_.lendObject();

        _e.setAny( event );
        return _e;
    }

    public NotificationEvent newEvent( StructuredEvent event )
    {
        NotificationStructuredEvent _e =
            ( NotificationStructuredEvent ) notificationStructuredEventPool_.lendObject();

        _e.setStructuredEventValue( event );
        return _e;
    }

    public NotificationEvent newEvent(NotificationEvent event) {
	NotificationEvent _newEvent = null;

	switch(event.getType()) {

	case NotificationEvent.TYPE_ANY:
	    _newEvent = newEvent(event.toAny());
	case NotificationEvent.TYPE_STRUCTURED:
	    _newEvent = newEvent(event.toStructuredEvent());
	}
	
	return _newEvent;
    }

} // NotificationEventFactory
