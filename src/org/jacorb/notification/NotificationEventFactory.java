package org.jacorb.notification;

import org.omg.CORBA.ORB;
import org.jacorb.notification.evaluate.DynamicEvaluator;
import org.jacorb.notification.evaluate.ResultExtractor;
import org.jacorb.notification.engine.Destination;
import org.omg.CORBA.Any;
import org.apache.log4j.Logger;
import org.omg.CosNotification.StructuredEvent;

/*
 *        JacORB - a free Java ORB
 */

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
    DynamicEvaluator dynamicEvaluator_;
    ResultExtractor resultExtractor_;
    ORB orb_;

    public NotificationEventFactory(ORB orb, DynamicEvaluator dynamicEvaluator, ResultExtractor resultExtractor) {
	orb_ = orb;
	dynamicEvaluator_ = dynamicEvaluator;
	resultExtractor_ = resultExtractor;
    }
    
    public NotificationEvent newEvent(Any event) {
	NotificationEvent _e = new NotificationAnyEvent(orb_, 
							resultExtractor_, 
							dynamicEvaluator_, 
							event, 
							Logger.getLogger("NotificationEvent.Any"));
	return _e;
    }

    public NotificationEvent newEvent(Any event, Destination firstHop) {
	NotificationEvent _e = newEvent(event);
	_e.hops_[0] = firstHop;

	return _e;
    }

    public NotificationEvent newEvent(StructuredEvent event, Destination firstHop) {
	NotificationEvent _e = newEvent(event);
	_e.hops_[0] = firstHop;
	return _e;
    }

    public NotificationEvent newEvent(StructuredEvent event) {
	NotificationEvent _e = new NotificationStructuredEvent(orb_, 
							       resultExtractor_, 
							       dynamicEvaluator_, 
							       event,
							       Logger.getLogger("NotificationEvent.Structured"));
	return _e;
    }

}// NotificationEventFactory
