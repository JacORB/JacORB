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

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.conf.Default;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.UnsupportedAdmin;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.ChannelNotFound;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyChannelAdmin.EventChannelFactoryHelper;
import org.omg.CosNotifyChannelAdmin.EventChannelHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

/**
 * <code>EventChannelFactoryImpl</code> is a implementation of
 * the  <code>EventChannelFactory</code> interface which defines operations
 * for creating and managing new Notification Service style event
 * channels. It supports a routine that creates new instances of
 * Notification Service event channels and assigns unique numeric
 * identifiers to them. In addition the
 * <code>EventChannelFactory</code> interface supports a routing,
 * which can return the unique identifiers assigned to all event
 * channels created by a given instance of
 * <code>EventChannelFactory</code>, and another routine which, given
 * the unique identifier of an event channel created by a target
 * <code>EventChannelFactory</code> instance, returns the object
 * reference of that event channel.<br>
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class EventChannelFactoryImpl
    extends AbstractChannelFactory
    implements JacORBEventChannelFactoryOperations
{
    private StaticEventChannelFactoryInfo staticInfo_;

    ////////////////////////////////////////

    protected String getShortcut() {
            return "NotificationService";
        }

    protected String getObjectName() {
        return "_ECFactory";
    }

    ////////////////////////////////////////

    /**
     * The <code>create_channel</code> operation is invoked to create
     * a new instance of the Notification Service style event
     * channel. This operation accepts two input parameters. The first
     * input parameter is a list of name-value pairs, which specify the
     * initial QoS property settings for the new channel. The second
     * input parameter is a list of name-value pairs, which specify
     * the initial administrative property settings for the new
     * channel. <br> If no implementation of the
     * <code>EventChannel</code> Interface exists that can support all
     * of the requested administrative property settings, the
     * <code>UnsupportedAdmin</code> exception is raised This
     * exception contains as data a sequence of data structures, each
     * identifies the name of an administrative property in the input
     * list whose requested setting could not be satisfied, along with
     * an error code and a range of settings for the property which
     * could be satisfied. The meanings of the error codes that might
     * be returned are described in <a
     * href="%%%NOTIFICATION_SPEC_URL%%%">Notification Service
     * Specification</a> Table 2-5 on page 2-46.<br>
     * If neither of these exceptions is raised, the
     * <code>create_channel</code> operation will return a reference
     * to a new Notification Service style event channel. In addition,
     * the operation assigns to this new event channel a numeric
     * identifier, which is unique among all event channels created by
     * the target object. This numeric identifier is returned as an
     * output parameter.
     *
     * @param qualitiyOfServiceProperties a list of name-value pairs,
     * which specify the initial QoS property settings for the new channel
     * @param administrativeProperties a list of name-value pairs,
     * which specify the initial administrative property settings for
     * the new channel
     * @param channelIdentifier, a reference to the new event channel
     * @return a newly created event channel
     * @exception UnsupportedAdmin if no implementation supports the
     * requested administrative settings
     * @exception UnsupportedQoS if no implementation supports the
     * requested QoS settings
     */
    public EventChannel create_channel( Property[] qualitiyOfServiceProperties,
                                        Property[] administrativeProperties,
                                        IntHolder channelIdentifier )
        throws UnsupportedAdmin,
               UnsupportedQoS
    {
        try
        {
            AbstractEventChannel _channelServant =
                create_channel_servant(channelIdentifier,
                                       qualitiyOfServiceProperties,
                                       administrativeProperties );

            addToChannels(channelIdentifier.value, _channelServant);

            return EventChannelHelper.narrow(_channelServant.activate());
        } catch (UnsupportedQoS e) {
            throw e;
        } catch (UnsupportedAdmin e) {
            throw e;
        } catch ( Exception e ) {
            logger_.fatalError( "create_channel", e );

            throw new RuntimeException();
        }
    }


    protected AbstractEventChannel newEventChannel() {
        EventChannelImpl _eventChannelServant =
            new EventChannelImpl();

        return _eventChannelServant;
    }


    /**
     * The <code>get_all_channels</code> operation returns a sequence
     * of all of the unique numeric identifiers corresponding to
     * Notification Service event channels, which have been created by
     * the target object.
     *
     * @return an <code>int[]</code> value
     */
    public int[] get_all_channels()
    {
        return getAllChannels();
    }


    /**
     * The <code>get_event_channel</code> operation accepts as input
     * a numeric value that is supposed to be the unique identifier of
     * a Notification Service event channel, which has been created by
     * the target object. If this input value does not correspond to
     * such a unique identifier, the <code>ChannelNotFound</code>
     * exception is raised. Otherwise, the operation returns the
     * object reference of the Notification Service event channel
     * corresponding to the input identifier.
     *
     * @param id an <code>int</code> the unique identifier of a
     * Notification Service event channel
     * @return an <code>EventChannel</code> corresponding to the input identifier
     * @exception ChannelNotFound if the input value does not
     * correspond to a Notification Service event channel
     */
    public EventChannel get_event_channel( int id ) throws ChannelNotFound
    {
        return EventChannelHelper.narrow(get_event_channel_servant( id ).activate());
    }


    public void preActivate() throws ConfigurationException
    {

    }


    public EventChannelFactory getEventChannelFactory()
    {
        return EventChannelFactoryHelper.narrow(thisRef_);
    }


    private ApplicationContext getApplicationContext2()
    {
        return applicationContext_;
    }


    public Servant getServant() {
        return new JacORBEventChannelFactoryPOATie(this);
    }


    public POA _default_POA()
    {
        return eventChannelFactoryPOA_;
    }


    public synchronized StaticEventChannelFactoryInfo get_static_info() {
        if (staticInfo_ == null) {
            staticInfo_ = new StaticEventChannelFactoryInfo();

            staticInfo_.corbaloc = getCorbaLoc();

            staticInfo_.filterfactory_running =
                (filterFactoryStarted_.get());

            staticInfo_.filterfactory_url =
                getConfiguration().getAttribute(Attributes.FILTER_FACTORY,
                                                Default.DEFAULT_FILTER_FACTORY);

            staticInfo_.hostname = getLocalAddress();

            staticInfo_.port = getLocalPort();
        }
        return staticInfo_;
    }


    protected org.omg.CORBA.Object create_abstract_channel(Property[] admin,
                                                           Property[] qos,
                                                           IntHolder id)
        throws UnsupportedAdmin,
               UnsupportedQoS
    {
        return create_channel(admin, qos, id);
    }
}
