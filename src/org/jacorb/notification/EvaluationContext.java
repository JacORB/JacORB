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
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.DynamicAny.DynAnyFactoryHelper;
import org.omg.DynamicAny.DynAnyFactory;
import org.jacorb.notification.evaluate.DynamicEvaluator;
import org.jacorb.notification.evaluate.ResultExtractor;
import org.jacorb.notification.framework.Poolable;
import java.util.Map;
import java.util.Hashtable;
import org.jacorb.notification.node.EvaluationResult;
import org.omg.CORBA.Any;
import org.jacorb.notification.util.ObjectPoolBase;

/**
 * EvaluationContext.java
 *
 *
 * Created: Sat Nov 30 16:02:34 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class EvaluationContext implements Poolable {

    ObjectPoolBase myPool_;

    ApplicationContext appContext_;
    DynAnyFactory dynAnyFactory_;
    DynamicEvaluator dynamicEvaluator_;
    ResultExtractor resultExtractor_;

    NotificationEvent event_;

    Map resultCache_;    
    Map anyCache_;

    public EvaluationContext() {
	resultCache_ = new Hashtable();
	anyCache_ = new Hashtable();
    }

    public void reset() {
	resultCache_.clear();
	anyCache_.clear();
    }

    public void setDynamicEvaluator(DynamicEvaluator e) {
	dynamicEvaluator_ = e;
    }

    public void setResultExtractor(ResultExtractor r) {
	resultExtractor_ = r;
    }

    public void setDynAnyFactory(DynAnyFactory d) {
	dynAnyFactory_ = d;
    }

    public DynamicEvaluator getDynamicEvaluator() {
	return dynamicEvaluator_;
    }

    public ResultExtractor getResultExtractor() {
	return resultExtractor_;
    }

    public NotificationEvent getEvent() {
	return event_;
    }

    public void setEvent(NotificationEvent event) {
	event_ = event;
    }

    public void storeResult(String name, EvaluationResult value) {
	resultCache_.put(name, value);
    }

    public EvaluationResult lookupResult(String name) {
	return (EvaluationResult)resultCache_.get(name);
    }

    public void eraseResult(String name) {
	resultCache_.remove(name);
    }

    public void storeAny(String name, Any any) {
	anyCache_.put(name, any);
    }

    public Any lookupAny(String name) {
	return (Any)anyCache_.get(name);
    }

    public void eraseAny(String name) {
	anyCache_.remove(name);
    }

    public void release() {
	myPool_.returnObject(this);
    }

    public void setObjectPool(ObjectPoolBase pool) {
	myPool_ = pool;
    }

    public EvaluationResult newEvaluationResult() {
	return appContext_.newEvaluationResult();
    }
}// EvaluationContext
