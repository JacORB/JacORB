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

import org.omg.CORBA.ORB;
import org.jacorb.notification.evaluate.DynamicEvaluator;
import org.jacorb.notification.evaluate.ResultExtractor;
import org.jacorb.notification.framework.DistributorNode;
import org.jacorb.notification.util.ObjectPoolBase;
import org.jacorb.notification.framework.Poolable;
import org.omg.CORBA.Any;
import org.apache.log4j.Logger;
import org.omg.CosNotification.StructuredEvent;

/**
 * NotificationEventFactory.java
 *
 *
 * Created: Tue Nov 05 18:53:27 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class NotificationEventFactory {
    ApplicationContext appContext_;

    ObjectPoolBase notificationAnyEventPool_ = new ObjectPoolBase() {
	    public Object newInstance() {
		Poolable _p = 
		    new NotificationAnyEvent(appContext_,
					     Logger.getLogger("NotificationEvent.Any"));
		return _p;
	    }
	    
	    public void passivateObject(Object o) {

	    }
	    
	    public void activateObject(Object o) {
		((Poolable)o).reset();
		((Poolable)o).setObjectPool(this);
	    }
	};
    
    ObjectPoolBase notificationStructuredEventPool_ = new ObjectPoolBase() {
	    public Object newInstance() {
		Poolable _p = new NotificationStructuredEvent(appContext_,
							      Logger.getLogger("NotificationEvent.Structured"));
		return _p;
	    }

	    public void passivateObject(Object o) {

	    }

	    public void activateObject(Object o) {
		((Poolable)o).reset();
		((Poolable)o).setObjectPool(this);
	    }
	};

    public NotificationEventFactory(ApplicationContext appContext) {
	appContext_ = appContext;
    }

    public void init() {
	notificationAnyEventPool_.init();
	notificationStructuredEventPool_.init();
    }

    // Used by the Proxies

    public NotificationEvent newEvent(Any event, DistributorNode firstHop) {
	NotificationEvent _e = newEvent(event);

	_e.setDistributorNode(firstHop);

	return _e;
    }

    public NotificationEvent newEvent(StructuredEvent event, DistributorNode firstHop) {
	NotificationEvent _e = newEvent(event);

	_e.setDistributorNode(firstHop);

	return _e;
    }

    // Used by the Filter

    public NotificationEvent newEvent(StructuredEvent event, EvaluationContext context) {
	NotificationEvent _event = newEvent(event);

	_event.setEvaluationContext(context);

	return _event;
    }

    public NotificationEvent newEvent(Any event, EvaluationContext context) {
	NotificationEvent _e = newEvent(event);

	_e.setEvaluationContext(context);

	return _e;
    }

    // internal use
    // fetch from object pool

    protected NotificationEvent newEvent(Any event) {
	NotificationAnyEvent _e = 
	    (NotificationAnyEvent)notificationAnyEventPool_.lendObject();

	_e.setAny(event);
	return _e;
    }

    protected NotificationEvent newEvent(StructuredEvent event) {
	NotificationStructuredEvent _e = 
	    (NotificationStructuredEvent)notificationStructuredEventPool_.lendObject();

	_e.setStructuredEventValue(event);
	return _e;
    }

}// NotificationEventFactory
