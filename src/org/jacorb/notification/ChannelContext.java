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

import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.ProxyEventListener;
import org.jacorb.util.Debug;

import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyFilter.FilterFactory;

import org.apache.avalon.framework.logger.Logger;
import org.omg.PortableServer.POA;
import org.omg.CORBA.ORB;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ChannelContext
{
    private POA poa_;

    private Logger logger_ = Debug.getNamedLogger(getClass().getName());
    private EventChannel eventChannel;
    private EventChannelImpl eventChannelServant;
    private EventChannelFactory eventChannelFactory;
    private EventChannelFactoryImpl eventChannelFactoryServant;
    private FilterFactory defaultFilterFactory;
    private TaskProcessor taskProcessor_;

    private ProxyEventListener proxySupplierDisposedListener_;
    private ProxyEventListener proxyConsumerDisposedListener_;

    /**
     * @return the TaskProcessor for this Channel
     */
    public TaskProcessor getTaskProcessor()
    {
        return taskProcessor_;
    }

    /**
     * sets the TaskProcessor for this Channel
     */
    public void setTaskProcessor(TaskProcessor taskProcessor)
    {
        taskProcessor_ = taskProcessor;
    }

    /**
     * Gets the value of eventChannelFactory
     *
     * @return the value of eventChannelFactory
     */
    private EventChannelFactory getEventChannelFactory()
    {
        return eventChannelFactory;
    }

    /**
     * Sets the value of eventChannelFactory
     *
     * @param argEventChannelFactory Value to assign to this.eventChannelFactory
     */
    private void setEventChannelFactory(EventChannelFactory argEventChannelFactory)
    {
        eventChannelFactory = argEventChannelFactory;
    }

    /**
     * Gets the value of eventChannelFactoryServant
     *
     * @return the value of eventChannelFactoryServant
     */
    public EventChannelFactoryImpl getEventChannelFactoryServant()
    {
        return eventChannelFactoryServant;
    }

    /**
     * Sets the value of eventChannelFactoryServant
     *
     * @param argEventChannelFactoryServant Value to assign to
     * this.eventChannelFactoryServant
     */
    public void setEventChannelFactoryServant(EventChannelFactoryImpl argEventChannelFactoryServant)
    {
        eventChannelFactoryServant = argEventChannelFactoryServant;
    }

    /**
     * Gets the value of defaultFilterFactory
     *
     * @return the value of defaultFilterFactory
     */
    public FilterFactory getDefaultFilterFactory()
    {
        return defaultFilterFactory;
    }

    /**
     * Sets the value of defaultFilterFactory
     *
     * @param argDefaultFilterFactory Value to assign to this.defaultFilterFactory
     */
    public void setDefaultFilterFactory(FilterFactory argDefaultFilterFactory)
    {
        defaultFilterFactory = argDefaultFilterFactory;
    }

    /**
     * Gets the value of eventChannelServant
     *
     * @return the value of eventChannelServant
     */
    public EventChannelImpl getEventChannelServant()
    {
        return eventChannelServant;
    }

    /**
     * Sets the value of eventChannelServant
     *
     * @param argEventChannelServant Value to assign to this.eventChannelServant
     */
    public void setEventChannelServant(EventChannelImpl argEventChannelServant)
    {
        logger_.debug("setEventChannelServant(" + argEventChannelServant + ")");
        if (argEventChannelServant == null)
        {
            throw new RuntimeException();
        }
        eventChannelServant = argEventChannelServant;
    }


    public Object clone()
    {
        ChannelContext _copy = new ChannelContext();

        _copy.setEventChannelFactory(eventChannelFactory);
        _copy.setEventChannelFactoryServant(eventChannelFactoryServant);
        _copy.setDefaultFilterFactory(defaultFilterFactory);

        return _copy;
    }

    public void setProxyConsumerDisposedEventListener(ProxyEventListener listener)
    {
        proxyConsumerDisposedListener_ = listener;
    }

    public void setProxySupplierDisposedEventListener(ProxyEventListener listener)
    {
        proxySupplierDisposedListener_ = listener;
    }

    public ProxyEventListener getRemoveProxyConsumerListener()
    {
        return proxyConsumerDisposedListener_;
    }

    public ProxyEventListener getRemoveProxySupplierListener()
    {
        return proxySupplierDisposedListener_;
    }

    public void setPOA(POA poa) {
        poa_ = poa;
    }

    public POA getPOA() {
        return poa_;
    }

    private ORB orb_;

    public void setORB(ORB orb) {
        orb_ = orb;
    }

    public ORB getORB() {
        return orb_;
    }


    private MessageFactory messageFactory_;

    public void setMessageFactory(MessageFactory messageFactory) {
        messageFactory_ = messageFactory;
    }


    public MessageFactory getMessageFactory()
    {
        return messageFactory_;
    }
}
