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

import org.jacorb.notification.engine.TaskProcessorDependency;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.queue.EventQueueFactory;
import org.jacorb.notification.queue.EventQueueFactoryDependency;
import org.jacorb.notification.servant.ManageableServant;

import org.omg.CORBA.ORB;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.PortableServer.POA;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;

/**
 * @author Alphonse Bendt
 * @version $Id$ 
 */

public class ChannelContext implements Configurable, Disposable
{
    class DependencyNotSatisfied extends RuntimeException {
        DependencyNotSatisfied(String name) {
            super("The Dependency for: " + name + " could not be satisfied");
        }
    }

    private Map map_;
    private Logger logger_;

    ////////////////////////////////////////

    public ChannelContext() {
        map_ = new HashMap();
    }

    private void ensureDependency(String name) throws DependencyNotSatisfied {
        if (!map_.containsKey(name)) {
            throw new DependencyNotSatisfied(name);
        }
    }

    public Object clone() {
        ChannelContext context = new ChannelContext();

        context.map_ = new HashMap(map_);
        logger_ = ((org.jacorb.config.Configuration)context.getConfiguration()).getNamedLogger(getClass().getName());

        return context;
    }

    public void configure(Configuration config) {
        map_.put(Configuration.class.getName(), config);

        logger_ = ((org.jacorb.config.Configuration)config).getNamedLogger(getClass().getName());

        Iterator i = map_.values().iterator();

        while (i.hasNext()) {
            try {
                ((Configurable)i.next()).configure(config);
            } catch (ClassCastException e) {
            } catch (ConfigurationException e) {
                logger_.error("failed to configure element", e);
            }
        }
    }


    public void dispose() {
        Iterator i = map_.values().iterator();


        while (i.hasNext()) {
            try {
                ((Disposable)i.next()).dispose();
            } catch (ClassCastException e) {
            }
        }
        map_.clear();
    }


    private Object get(String name) {
        ensureDependency(name);

        return map_.get(name);
    }


    private EventQueueFactory getEventQueueFactory() {
        return (EventQueueFactory)get(EventQueueFactory.class.getName());
    }

    public void setEventQueueFactory(EventQueueFactory factory) {
        map_.put(EventQueueFactory.class.getName(), factory);
    }


    private TaskProcessor getTaskProcessor()
    {
        return (TaskProcessor)get(TaskProcessor.class.getName());
    }


    private Configuration getConfiguration() {
        return (Configuration)get(Configuration.class.getName());
    }

    public void setTaskProcessor(TaskProcessor taskProcessor)
    {
        map_.put(TaskProcessor.class.getName(), taskProcessor);
    }


    public void setPOA(POA poa) {
        map_.put(POA.class.getName(), poa);
    }


    private POA getPOA() {
        return (POA)get(POA.class.getName());
    }


    public void setORB(ORB orb) {
        map_.put(ORB.class.getName(), orb);
    }


    private ORB getORB() {
        return (ORB)get(ORB.class.getName());
    }


    public void setMessageFactory(MessageFactory messageFactory) {
        map_.put(MessageFactory.class.getName(), messageFactory);
    }


    private MessageFactory getMessageFactory()
    {
        return (MessageFactory)get(MessageFactory.class.getName());
    }


    public void setEventChannel(EventChannel eventChannel) {
        map_.put(EventChannel.class.getName(), eventChannel);
    }


    private EventChannel getEventChannel() {
        return (EventChannel)get(EventChannel.class.getName());
    }

    public void setEventChannelFactory(EventChannelFactory factory) {
        map_.put(EventChannelFactory.class.getName(), factory);
    }


    private EventChannelFactory getEventChannelFactory() {
        return (EventChannelFactory)get(EventChannelFactory.class.getName());
    }

    public void resolveDependencies(Dependant o) {
        try {
            ManageableServant _manageableServant = (ManageableServant)o;

            _manageableServant.setORB(getORB());
            _manageableServant.setPOA(getPOA());
        } catch (ClassCastException e) {}

        try {
            EventQueueFactoryDependency _eventQueueFactoryUser = (EventQueueFactoryDependency)o;

            _eventQueueFactoryUser.setEventQueueFactory(getEventQueueFactory());
        } catch (ClassCastException e) {
        }

        try {
            TaskProcessorDependency _taskProcessorUser = (TaskProcessorDependency)o;

            _taskProcessorUser.setTaskProcessor(getTaskProcessor());
        } catch (ClassCastException e) {}

        try {
            MessageFactoryDependency _messageFactoryUser = (MessageFactoryDependency)o;

            _messageFactoryUser.setMessageFactory(getMessageFactory());
        } catch (ClassCastException e) {}

        try {
            EventChannelDependency ec = (EventChannelDependency)o;

            ec.setEventChannel(getEventChannel());
        } catch (ClassCastException e) {}

        try {
            EventChannelFactoryDependency ef = (EventChannelFactoryDependency)o;

            ef.setEventChannelFactory(getEventChannelFactory());
        } catch (ClassCastException e) {}


        try {
            Configurable _configurable = (Configurable)o;

            _configurable.configure(getConfiguration());
        } catch (ClassCastException e) {
        } catch (ConfigurationException e) {
            logger_.fatalError("configuration failed", e);
        }

        try {
            ChannelContextDependency _container = (ChannelContextDependency)o;

            _container.setChannelContext((ChannelContext)clone());
        } catch (ClassCastException e) {
        }
    }
}
