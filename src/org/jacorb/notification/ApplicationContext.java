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
import org.omg.PortableServer.POA;
import org.jacorb.notification.util.ObjectPoolBase;
import org.jacorb.notification.framework.Poolable;
import org.jacorb.notification.node.EvaluationResult;
import org.omg.DynamicAny.DynAnyFactory;
import org.jacorb.notification.evaluate.ResultExtractor;
import org.jacorb.notification.evaluate.DynamicEvaluator;
import org.omg.DynamicAny.DynAnyFactoryHelper;
import org.omg.CORBA.ORBPackage.InvalidName;

/**
 * ApplicationContext.java
 *
 *
 * Created: Sat Nov 30 16:02:04 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class ApplicationContext {

    ORB orb_;
    POA poa_;
    ObjectPoolBase evaluationResultPool_;
    ObjectPoolBase evaluationContextPool_;
    NotificationEventFactory notificationEventFactory_;
    DynAnyFactory dynAnyFactory_;
    ResultExtractor resultExtractor_;
    DynamicEvaluator dynamicEvaluator_;

    public ApplicationContext(ORB orb, POA poa) throws InvalidName {
	orb_ = orb;
	poa_ = poa;

	dynAnyFactory_ = DynAnyFactoryHelper.narrow(orb_.resolve_initial_references("DynAnyFactory"));
	resultExtractor_ = new ResultExtractor(dynAnyFactory_);
	dynamicEvaluator_ = new DynamicEvaluator(orb_, dynAnyFactory_);

	evaluationContextPool_ = new ObjectPoolBase() {
		public Object newInstance() {
		    EvaluationContext e = new EvaluationContext();
		    e.setDynamicEvaluator(dynamicEvaluator_);
		    e.setResultExtractor(resultExtractor_);
		    e.setDynAnyFactory(dynAnyFactory_);

		    return e;
		}
		public void activateObject(Object o) {
		    ((Poolable)o).setObjectPool(this);
		}
		public void passivateObject(Object o) {
		    ((Poolable)o).reset();
		}
	    };
	evaluationContextPool_.init();

	evaluationResultPool_ = new ObjectPoolBase() {
		public Object newInstance() {
		    return new EvaluationResult();
		}
		public void activateObject(Object o) {
		    ((Poolable)o).setObjectPool(this);
		}
		public void passivateObject(Object o) {
		    ((Poolable)o).reset();
		}
	    };
	evaluationResultPool_.init();
	
	notificationEventFactory_ = new NotificationEventFactory(this);
	notificationEventFactory_.init();
    }

    /**
     * Get the Orb value.
     * @return the Orb value.
     */
    public ORB getOrb() {
	return orb_;
    }

    /**
     * Set the Orb value.
     * @param newOrb The new Orb value.
     */
    public void setOrb(ORB newOrb) {
	orb_ = newOrb;
    }

    /**
     * Get the Poa value.
     * @return the Poa value.
     */
    public POA getPoa() {
	return poa_;
    }

    /**
     * Set the Poa value.
     * @param newPoa The new Poa value.
     */
    public void setPoa(POA newPoa) {
	poa_ = newPoa;
    }

    public String toString() {
	StringBuffer _b = new StringBuffer();
	_b.append("orb: " + orb_ + "\n");
	_b.append("poa: " + poa_ + "\n");
	return _b.toString();
    }

    public EvaluationResult newEvaluationResult() {
	return (EvaluationResult)evaluationResultPool_.lendObject();
    }

    public EvaluationContext newEvaluationContext() {
	return (EvaluationContext)evaluationContextPool_.lendObject();
    }

    public NotificationEventFactory getNotificationEventFactory() {
	return notificationEventFactory_;
    }
    
}// ApplicationContext
