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
import org.jacorb.notification.interfaces.Poolable;
import org.jacorb.notification.node.EvaluationResult;
import org.omg.DynamicAny.DynAnyFactory;
import org.jacorb.notification.evaluate.ResultExtractor;
import org.jacorb.notification.evaluate.DynamicEvaluator;
import org.omg.DynamicAny.DynAnyFactoryHelper;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNotification.MaximumBatchSize;
import org.omg.CORBA.Any;
import org.omg.TimeBase.TimeTHelper;
import org.omg.CosNotification.PacingInterval;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Disposable;

/**
 * ApplicationContext.java
 *
 *
 * Created: Sat Nov 30 16:02:04 2002
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ApplicationContext implements Disposable {

    private ORB orb_;
    private POA poa_;
    private TaskProcessor taskProcessor_;
    private ObjectPoolBase evaluationResultPool_;
    private ObjectPoolBase evaluationContextPool_;
    private NotificationEventFactory notificationEventFactory_;
    private DynAnyFactory dynAnyFactory_;
    private ResultExtractor resultExtractor_;
    private DynamicEvaluator dynamicEvaluator_;
    private PropertyValidator propertyValidator_;
    private PropertyManager defaultAdminProperties_;
    private PropertyManager defaultQoSProperties_;

    public ApplicationContext(ORB orb, POA poa) throws InvalidName {
	this(orb, poa, false);
    }

    public ApplicationContext(ORB orb, POA poa, boolean init) throws InvalidName {
	orb_ = orb;
	poa_ = poa;

	dynAnyFactory_ = 
	    DynAnyFactoryHelper.narrow(orb_.resolve_initial_references("DynAnyFactory"));

	resultExtractor_ = new ResultExtractor(dynAnyFactory_);
	dynamicEvaluator_ = new DynamicEvaluator(orb_, dynAnyFactory_);

	evaluationContextPool_ = new ObjectPoolBase() {
		public Object newInstance() {
		    EvaluationContext _e = new EvaluationContext();
		    _e.setDynamicEvaluator(dynamicEvaluator_);
		    _e.setResultExtractor(resultExtractor_);
		    _e.setDynAnyFactory(dynAnyFactory_);

		    return _e;
		}
		public void activateObject(Object o) {
		    ((Poolable) o).setObjectPool(this);
		}
		public void passivateObject(Object o) {
		    ((Poolable) o).reset();
		}
	    };
	evaluationContextPool_.init();

	evaluationResultPool_ = new ObjectPoolBase() {
		public Object newInstance() {
		    return new EvaluationResult();
		}
		public void activateObject(Object o) {
		    ((Poolable) o).setObjectPool(this);
		}
		public void passivateObject(Object o) {
		    ((Poolable) o).reset();
		}
	    };
	evaluationResultPool_.init();
	
	notificationEventFactory_ = new NotificationEventFactory(this);
	notificationEventFactory_.init();

	propertyValidator_ = new PropertyValidator(orb_);

	defaultQoSProperties_ = new PropertyManager(this);
	defaultAdminProperties_ = new PropertyManager(this);

	Any _maxBatchSize = orb_.create_any();
	_maxBatchSize.insert_long(1);
	defaultQoSProperties_.setProperty(MaximumBatchSize.value, _maxBatchSize);

	Any _pacingInterval = orb_.create_any();
	TimeTHelper.insert(_pacingInterval, 0);
	defaultQoSProperties_.setProperty(PacingInterval.value, _pacingInterval);

	if (init) {
	    init();
	}
    }

    public void init() {
	taskProcessor_ = new TaskProcessor();
    }

    public void dispose() {
	if (taskProcessor_ != null) {
	    taskProcessor_.dispose();
	    taskProcessor_ = null;
	}

	evaluationResultPool_.dispose();
	evaluationContextPool_.dispose();
	notificationEventFactory_.dispose();
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

    public EvaluationResult newEvaluationResult() {
	return (EvaluationResult) evaluationResultPool_.lendObject();
    }

    public EvaluationContext newEvaluationContext() {
	return (EvaluationContext) evaluationContextPool_.lendObject();
    }

    public NotificationEventFactory getNotificationEventFactory() {
	return notificationEventFactory_;
    }

    public PropertyValidator getPropertyValidator() {
	return propertyValidator_;
    }

    public DynAnyFactory getDynAnyFactory() {
	return dynAnyFactory_;
    }

    public PropertyManager getDefaultAdminProperties() {
	return defaultAdminProperties_;
    }

    public PropertyManager getDefaultQoSProperties() {
	return defaultQoSProperties_;
    }

    public DynamicEvaluator getDynamicEvaluator() {
	return dynamicEvaluator_;
    }

    public ResultExtractor getResultExtractor() {
	return resultExtractor_;
    }

    public TaskProcessor getTaskProcessor() {
	return taskProcessor_;
    }

}// ApplicationContext
