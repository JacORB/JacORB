package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.engine.DefaultTaskProcessor;
import org.jacorb.notification.filter.DynamicEvaluator;
import org.jacorb.notification.interfaces.AbstractPoolable;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.filter.EvaluationResult;
import org.jacorb.notification.queue.EventQueue;
import org.jacorb.notification.queue.EventQueueFactory;
import org.jacorb.notification.util.AbstractObjectPool;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNotification.MaximumBatchSize;
import org.omg.CosNotification.PacingInterval;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.DynamicAny.DynAnyFactory;
import org.omg.DynamicAny.DynAnyFactoryHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.TimeBase.TimeTHelper;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.Configurable;

import org.jacorb.notification.filter.EvaluationContext;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ApplicationContext implements Disposable, Configurable
{
    private Configuration configuration_;
    private ORB orb_;
    private POA poa_;
    private TaskProcessor taskProcessor_;
    private AbstractObjectPool evaluationResultPool_;
    private AbstractObjectPool evaluationContextPool_;
    private MessageFactory notificationEventFactory_;
    private DynAnyFactory dynAnyFactory_;
    private DynamicEvaluator dynamicEvaluator_;

    private void setup( ORB orb, POA poa) throws InvalidName
    {
        orb_ = orb;
        poa_ = poa;

        dynAnyFactory_ =
            DynAnyFactoryHelper.narrow( orb_.resolve_initial_references( "DynAnyFactory" ) );

        dynamicEvaluator_ = new DynamicEvaluator(dynAnyFactory_ );

        evaluationContextPool_ =
            new AbstractObjectPool("EvaluationContextPool")
            {
                public Object newInstance()
                {
                    EvaluationContext _e = new EvaluationContext();
                    _e.setDynamicEvaluator( dynamicEvaluator_ );

                    return _e;
                }

                public void activateObject( Object o )
                {
                    AbstractPoolable obj = (AbstractPoolable) o;
                    // todo check if configure can be called from AbstractObjectPool
                    obj.configure(configuration_);
                    obj.reset();
                    obj.setObjectPool( this );
                }
            };

        evaluationResultPool_ =
            new AbstractObjectPool("EvaluationResultPool")
            {
                public Object newInstance()
                {
                    return new EvaluationResult();
                }

                public void activateObject( Object o )
                {
                    AbstractPoolable obj = (AbstractPoolable) o;
                    obj.reset();
                    obj.setObjectPool( this );
                }
            };

        notificationEventFactory_ = new MessageFactory();

        taskProcessor_ = new DefaultTaskProcessor();
    }


    public ApplicationContext( ORB orb, POA poa ) throws InvalidName
    {
        setup( orb, poa);
    }


    public void configure (Configuration conf)
    {
        configuration_ = conf;
        dynamicEvaluator_.configure (conf);
        evaluationContextPool_.configure(conf);
        evaluationResultPool_.configure(conf);
        notificationEventFactory_.configure(conf);
        ((DefaultTaskProcessor)taskProcessor_).configure (conf);
    }

    public void dispose()
    {
        if ( taskProcessor_ != null )
        {
            ((DefaultTaskProcessor)taskProcessor_).dispose();
            taskProcessor_ = null;
        }

        evaluationContextPool_.dispose();
        notificationEventFactory_.dispose();

        orb_.shutdown( true );
    }

    /**
     * Get the Orb value.
     * @return the Orb value.
     */
    public ORB getOrb()
    {
        return orb_;
    }

    /**
     * Set the Orb value.
     * @param newOrb The new Orb value.
     */
    public void setOrb( ORB newOrb )
    {
        orb_ = newOrb;
    }

    /**
     * Get the Poa value.
     * @return the Poa value.
     */
    public POA getPoa()
    {
        return poa_;
    }

    /**
     * Set the Poa value.
     * @param newPoa The new Poa value.
     */
    public void setPoa( POA newPoa )
    {
        poa_ = newPoa;
    }

    private EvaluationResult newEvaluationResult()
    {
        return ( EvaluationResult ) evaluationResultPool_.lendObject();
    }

    public EvaluationContext newEvaluationContext()
    {
        return ( EvaluationContext ) evaluationContextPool_.lendObject();
    }

    public MessageFactory getMessageFactory()
    {
        return notificationEventFactory_;
    }

    public DynAnyFactory getDynAnyFactory()
    {
        return dynAnyFactory_;
    }


    public DynamicEvaluator getDynamicEvaluator()
    {
        return dynamicEvaluator_;
    }


    public TaskProcessor getTaskProcessor()
    {
        return taskProcessor_;
    }
}
