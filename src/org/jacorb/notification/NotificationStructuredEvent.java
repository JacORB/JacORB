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
package org.jacorb.notification;

import org.omg.CORBA.ORB;
import org.jacorb.notification.evaluate.ResultExtractor;
import org.jacorb.notification.evaluate.DynamicEvaluator;
import org.omg.CosNotification.StructuredEvent;
import org.apache.log4j.Logger;
import org.omg.CORBA.Any;
import org.omg.CosNotification.StructuredEventHelper;
import org.jacorb.notification.node.EvaluationResult;
import org.jacorb.notification.node.ComponentOperator;

class NotificationStructuredEvent extends NotificationEvent {

    NotificationStructuredEvent(ORB orb, 
				ResultExtractor extractor, 
				DynamicEvaluator evaluator, 
				StructuredEvent event, 
				Logger logger) {
	super(orb, extractor, evaluator, logger);
	structuredEventValue_ = event;
    }

    Any anyValue_;
    StructuredEvent structuredEventValue_;

    public int getType() {
	return TYPE_STRUCTURED;
    }

    public Any toAny() {
	if (anyValue_ == null) {
	    synchronized(this) {
		if (anyValue_ == null) {
		    anyValue_ = orb_.create_any();
		    StructuredEventHelper.insert(anyValue_, structuredEventValue_);
		}
	    }
	}
	return anyValue_;
    }

    public StructuredEvent toStructuredEvent() {
	return structuredEventValue_;
    }

    public EvaluationResult evaluate(ComponentOperator op) {
	return null;
    }

    public EvaluationResult testExists(ComponentOperator op) {
	return null;
    }

    public String getConstraintKey() {
	return FilterUtils.calcConstraintKey(structuredEventValue_.header.fixed_header.event_type.domain_name,
					     structuredEventValue_.header.fixed_header.event_type.type_name);
    }
    
    public EvaluationResult hasDefault(ComponentOperator op) {
	return null;
    }
}
