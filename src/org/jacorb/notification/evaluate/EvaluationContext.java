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
package org.jacorb.notification.evaluate;

import org.omg.CORBA.Any;
import org.omg.DynamicAny.DynAnyFactory;
import org.omg.DynamicAny.DynAnyFactoryHelper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.jacorb.notification.NotificationEvent;

/**
 * EvaluationContext.java
 *
 *
 * Created: Sat Sep 07 21:25:33 2002
 *
 * @author <a href="mailto:a.bendt@berlin.de">Alphonse Bendt</a>
 * @version
 */

public class EvaluationContext {

    ORB orb_;
    DynAnyFactory dynAnyFactory_;
    DynamicEvaluator dynamicEvaluator_;
    ResultExtractor resultExtractor_;

    NotificationEvent myEvent_;

    public EvaluationContext() {
    }

    public EvaluationContext(ORB orb) throws InvalidName {
	this();
	orb_ = orb;
	dynAnyFactory_ = DynAnyFactoryHelper.narrow(orb.resolve_initial_references("DynAnyFactory"));
	resultExtractor_ = new ResultExtractor(dynAnyFactory_);
	dynamicEvaluator_ = new DynamicEvaluator(orb_, dynAnyFactory_);
    }

    public EvaluationContext(ORB orb, 
			     DynAnyFactory dynAnyFactory, 
			     DynamicEvaluator dynamicEvaluator, 
			     ResultExtractor resultExtractor) {

	this();
	dynAnyFactory_ = dynAnyFactory;
	orb_ = orb;
	dynamicEvaluator_ = dynamicEvaluator;
	resultExtractor_ = resultExtractor;
    }

    public DynamicEvaluator getDynamicEvaluator() {
	return dynamicEvaluator_;
    }

    public ResultExtractor getResultExtractor() {
	return resultExtractor_;
    }

    public void setEvent(NotificationEvent event) {
	myEvent_ = event;
    }

    public NotificationEvent getEvent() {
	return myEvent_;
    }

    void debug(String msg) {
	System.err.println("[EvaluationContext] " + msg);
    }
}// EvaluationContext
