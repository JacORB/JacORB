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
    private ORB orb_;
    private POA poa_;
    private MessageFactory messageFactory_;
    private EventChannelImpl eventChannelServant_;
    private EventChannelFactory eventChannelFactory_;
    private EventChannelFactoryImpl eventChannelFactoryServant_;
    private FilterFactory defaultFilterFactory_;
    private TaskProcessor taskProcessor_;

    ////////////////////////////////////////

    public TaskProcessor getTaskProcessor()
    {
        return taskProcessor_;
    }


    public void setTaskProcessor(TaskProcessor taskProcessor)
    {
        taskProcessor_ = taskProcessor;
    }


    private EventChannelFactory getEventChannelFactory()
    {
        return eventChannelFactory_;
    }


    private void setEventChannelFactory(EventChannelFactory argEventChannelFactory)
    {
        eventChannelFactory_ = argEventChannelFactory;
    }


    public EventChannelFactoryImpl getEventChannelFactoryServant()
    {
        return eventChannelFactoryServant_;
    }


    public void setEventChannelFactoryServant(EventChannelFactoryImpl argEventChannelFactoryServant)
    {
        eventChannelFactoryServant_ = argEventChannelFactoryServant;
    }


    public FilterFactory getDefaultFilterFactory()
    {
        return defaultFilterFactory_;
    }


    public void setDefaultFilterFactory(FilterFactory argDefaultFilterFactory)
    {
        defaultFilterFactory_ = argDefaultFilterFactory;
    }


    public EventChannelImpl getEventChannelServant()
    {
        return eventChannelServant_;
    }


    public void setEventChannelServant(EventChannelImpl argEventChannelServant)
    {
        if (argEventChannelServant == null)
        {
            throw new RuntimeException();
        }
        eventChannelServant_ = argEventChannelServant;
    }


    public Object clone()
    {
        ChannelContext _copy = new ChannelContext();

        _copy.setEventChannelFactory(eventChannelFactory_);
        _copy.setEventChannelFactoryServant(eventChannelFactoryServant_);
        _copy.setDefaultFilterFactory(defaultFilterFactory_);

        return _copy;
    }


    public void setPOA(POA poa) {
        poa_ = poa;
    }


    public POA getPOA() {
        return poa_;
    }


    public void setORB(ORB orb) {
        orb_ = orb;
    }


    public ORB getORB() {
        return orb_;
    }


    public void setMessageFactory(MessageFactory messageFactory) {
        messageFactory_ = messageFactory;
    }


    public MessageFactory getMessageFactory()
    {
        return messageFactory_;
    }
}
