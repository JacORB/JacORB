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

import org.omg.CORBA.Any;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.Property;
import org.jacorb.notification.node.ComponentOperator;
import org.jacorb.notification.evaluate.DynamicEvaluator;
import org.jacorb.notification.evaluate.ResultExtractor;
import org.jacorb.notification.node.TCLNode;
import org.jacorb.notification.node.EvaluationResult;
import org.jacorb.notification.node.IdentValue;
import org.jacorb.notification.node.DotOperator;
import org.jacorb.notification.node.UnionPositionOperator;
import org.jacorb.notification.node.ComponentPositionOperator;
import org.jacorb.notification.node.ImplicitOperator;
import org.jacorb.notification.node.ImplicitOperatorNode;
import org.jacorb.notification.framework.DistributorNode;
import org.jacorb.notification.framework.Poolable;
import org.jacorb.notification.evaluate.EvaluationException;
import org.jacorb.notification.util.ObjectPoolBase;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.jacorb.notification.node.ArrayOperator;
import org.jacorb.notification.node.AssocOperator;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.EventHeader;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.StructuredEventHelper;
import org.apache.log4j.Logger;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;

/**
 * NotificationEvent.java
 *
 *
 * Created: Tue Oct 22 20:16:33 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public abstract class NotificationEvent implements Poolable {

    public static final int TYPE_ANY = 0;
    public static final int TYPE_STRUCTURED = 1;
    public static final int TYPE_TYPED = 2;

    protected ApplicationContext applicationContext_;
    protected Logger logger_;

    protected DistributorNode currentDistributor_;
    protected EvaluationContext evaluationContext_;
    protected ObjectPoolBase myPool_;

    public abstract EventTypeIdentifier getEventTypeIdentifier();
    public abstract EvaluationResult evaluate(ComponentOperator c) throws EvaluationException;
    public abstract EvaluationResult hasDefault(ComponentOperator c) throws EvaluationException;
    public abstract EvaluationResult testExists(ComponentOperator c) throws EvaluationException;

    public abstract String getConstraintKey();
    public abstract Any toAny();
    public abstract StructuredEvent toStructuredEvent();
    public abstract int getType();
    
    private int referenced_ = 0;

    static boolean DEBUG = false;

    int finalReleaseCalled = 0;

    protected NotificationEvent(ApplicationContext appContext,
				Logger logger) {

	applicationContext_ = appContext;
	logger_ = logger;
    }

    public void reset() {
	finalReleaseCalled = 0;
	currentDistributor_ = null;
	myPool_ = null;
	evaluationContext_ = null;
    }


    synchronized public void addReference() {
	++referenced_;
    }

    synchronized public void release() {
	if (referenced_ > 0) {
	    --referenced_;
	}
	if (referenced_ == 0) {
	    myPool_.returnObject(this);
	}
    }

    public void setDistributorNode(DistributorNode node) {
	currentDistributor_ = node;
    }

    public DistributorNode getDistributorNode() {
	return currentDistributor_;
    }

    public EvaluationContext getEvaluationContext() {
	return evaluationContext_;
    }

    public void setEvaluationContext(EvaluationContext context) {
	evaluationContext_ = context;
    }	

    public void setObjectPool(ObjectPoolBase pool) {
	myPool_ = pool;
    }

}// NotificationEvent

