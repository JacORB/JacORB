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
