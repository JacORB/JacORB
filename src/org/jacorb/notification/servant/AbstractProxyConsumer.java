package org.jacorb.notification.servant;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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
 */

import org.jacorb.notification.ChannelContext;
import org.jacorb.notification.servant.PropertySet;
import org.jacorb.notification.servant.PropertySetListener;
import org.jacorb.notification.conf.Configuration;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.TimerEventSupplier;
import org.jacorb.util.Environment;

import org.omg.CORBA.BAD_QOS;
import org.omg.CosNotification.Priority;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StartTimeSupported;
import org.omg.CosNotification.StopTimeSupported;
import org.omg.CosNotification.Timeout;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;

import java.util.List;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;
import org.jacorb.notification.CollectionsWrapper;
import org.jacorb.notification.interfaces.MessageConsumer;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

abstract class AbstractProxyConsumer
            extends AbstractProxy
            implements AbstractProxyConsumerI
{
    private TaskProcessor taskProcessor_;

    private SynchronizedBoolean isStartTimeSupported_ = new SynchronizedBoolean(true);

    private SynchronizedBoolean isStopTimeSupported_ = new SynchronizedBoolean(true);

    private List subsequentDestinations_;

    ////////////////////////////////////////

    AbstractProxyConsumer( AbstractAdmin adminServant,
                           ChannelContext channelContext)
    {

        super( adminServant,
               channelContext);

        subsequentDestinations_ = CollectionsWrapper.singletonList(myAdmin_);

        taskProcessor_ = channelContext.getTaskProcessor();
    }

    ////////////////////////////////////////

    public final List getSubsequentFilterStages() {
        return subsequentDestinations_;
    }


    public void preActivate()
    {
        logger_.debug("AbstractProxyConsumer.initialize");

        configureStartTimeSupported();

        configureStopTimeSupported();

        qosSettings_.addPropertySetListener(new String[] {Priority.value,
                                                          Timeout.value,
                                                          StartTimeSupported.value,
                                                          StopTimeSupported.value},
                                            reconfigureCB);
    }


    private PropertySetListener reconfigureCB =
        new PropertySetListener()
        {
            public void validateProperty(Property[] props, List errors)
            {}
            public void actionPropertySetChanged(PropertySet source)
            {
                configureStartTimeSupported();
                configureStopTimeSupported();
            }
        };


    private void configureStartTimeSupported()
    {
        if (qosSettings_.containsKey(StartTimeSupported.value))
        {
            isStartTimeSupported_.set(qosSettings_.get(StartTimeSupported.value).extract_boolean());
        }
        else
        {
            isStartTimeSupported_.set(Environment.isPropertyOn(Configuration.START_TIME_SUPPORTED,
                                      Default.DEFAULT_START_TIME_SUPPORTED));
        }

        if (logger_.isInfoEnabled())
        {
            logger_.info("set QoS: StartTimeSupported=" + isStartTimeSupported_);
        }
    }


    private void configureStopTimeSupported()
    {
        if (qosSettings_.containsKey(StopTimeSupported.value))
        {
            isStopTimeSupported_.set(qosSettings_.get(StopTimeSupported.value).extract_boolean());
        }
        else
        {
            isStopTimeSupported_.set(Environment.isPropertyOn(Configuration.STOP_TIME_SUPPORTED,
                                     Default.DEFAULT_STOP_TIME_SUPPORTED));
        }

        if (logger_.isInfoEnabled())
        {
            logger_.info("set QoS: StopTimeSupported=" + isStopTimeSupported_);
        }
    }


    public void scheduleTimedPullTask( TimerEventSupplier target )
    {
        try
        {
            taskProcessor_.scheduleTimedPullTask(target);
        }
        catch (InterruptedException e)
        {
            logger_.fatalError("interrupted", e);
        }
    }


    protected void checkMessageProperties(Message mesg)
    {
        if (mesg.hasStartTime() && !isStartTimeSupported_.get() )
        {
            logger_.error("StartTime NOT allowed");

            throw new BAD_QOS("property StartTime is not allowed");
        }

        if (mesg.hasStopTime() && !isStopTimeSupported_.get() )
        {
            logger_.error("StopTime NOT allowed");

            throw new BAD_QOS("property StopTime is not allowed");
        }
    }


    public FilterStage getFirstStage()
    {
        return this;
    }


    public boolean isTimeOutSupported()
    {
        return isStopTimeSupported_.get();
    }


    public boolean isStartTimeSupported()
    {
        return isStartTimeSupported_.get();
    }


    public final SupplierAdmin MyAdmin()
    {
        return ( SupplierAdmin ) myAdmin_.activate();
    }


    public final MessageConsumer getMessageConsumer()
    {
        throw new UnsupportedOperationException();
    }


    public final boolean hasMessageConsumer()
    {
        return false;
    }
}

