package org.jacorb.notification;

import org.jacorb.notification.engine.Engine;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.CosNotifyChannelAdmin.EventChannel;



/*
 *        JacORB - a free Java ORB
 */

/**
 * ChannelContext.java
 *
 *
 * Created: Sat Nov 30 16:02:18 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class ChannelContext {

    EventChannel eventChannel;
    EventChannelImpl eventChannelServant;
    EventChannelFactory eventChannelFactory;
    EventChannelFactoryImpl eventChannelFactoryServant;
    FilterFactory defaultFilterFactory;
    Engine engine;
    NotificationEventFactory notificationEventFactory;
    

    /**
     * Gets the value of notificationEventFactory
     *
     * @return the value of notificationEventFactory
     */
    public NotificationEventFactory getNotificationEventFactory()  {
	return this.notificationEventFactory;
    }

    /**
     * Sets the value of notificationEventFactory
     *
     * @param argNotificationEventFactory Value to assign to this.notificationEventFactory
     */
    public void setNotificationEventFactory(NotificationEventFactory argNotificationEventFactory) {
	this.notificationEventFactory = argNotificationEventFactory;
    }


    /**
     * Gets the value of engine
     *
     * @return the value of engine
     */
    public Engine getEngine()  {
	return this.engine;
    }

    /**
     * Sets the value of engine
     *
     * @param argEngine Value to assign to this.engine
     */
    public void setEngine(Engine argEngine) {
	this.engine = argEngine;
    }

    /**
     * Gets the value of eventChannelFactory
     *
     * @return the value of eventChannelFactory
     */
    public EventChannelFactory getEventChannelFactory()  {
	return this.eventChannelFactory;
    }

    /**
     * Sets the value of eventChannelFactory
     *
     * @param argEventChannelFactory Value to assign to this.eventChannelFactory
     */
    public void setEventChannelFactory(EventChannelFactory argEventChannelFactory) {
	this.eventChannelFactory = argEventChannelFactory;
    }

    /**
     * Gets the value of eventChannelFactoryServant
     *
     * @return the value of eventChannelFactoryServant
     */
    public EventChannelFactoryImpl getEventChannelFactoryServant()  {
	return this.eventChannelFactoryServant;
    }

    /**
     * Sets the value of eventChannelFactoryServant
     *
     * @param argEventChannelFactoryServant Value to assign to this.eventChannelFactoryServant
     */
    public void setEventChannelFactoryServant(EventChannelFactoryImpl argEventChannelFactoryServant) {
	this.eventChannelFactoryServant = argEventChannelFactoryServant;
    }

    /**
     * Gets the value of defaultFilterFactory
     *
     * @return the value of defaultFilterFactory
     */
    public FilterFactory getDefaultFilterFactory()  {
	return this.defaultFilterFactory;
    }

    /**
     * Sets the value of defaultFilterFactory
     *
     * @param argDefaultFilterFactory Value to assign to this.defaultFilterFactory
     */
    public void setDefaultFilterFactory(FilterFactory argDefaultFilterFactory) {
	this.defaultFilterFactory = argDefaultFilterFactory;
    }



    /**
     * Gets the value of eventChannelServant
     *
     * @return the value of eventChannelServant
     */
    public EventChannelImpl getEventChannelServant()  {
	return eventChannelServant;
    }

    /**
     * Sets the value of eventChannelServant
     *
     * @param argEventChannelServant Value to assign to this.eventChannelServant
     */
    public void setEventChannelServant(EventChannelImpl argEventChannelServant) {
	eventChannelServant = argEventChannelServant;
    }

    /**
     * Get the EventChannel value.
     * @return the EventChannel value.
     */
    public EventChannel getEventChannel() {
	return eventChannel;
    }

    /**
     * Set the EventChannel value.
     * @param newEventChannel The new EventChannel value.
     */
    public void setEventChannel(EventChannel newEventChannel) {
	this.eventChannel = newEventChannel;
    }
    
    public Object clone() {
	ChannelContext _copy = new ChannelContext();

	_copy.setEventChannel(eventChannel);
	_copy.setEventChannelServant(eventChannelServant);
	_copy.setEventChannelFactory(eventChannelFactory);
	_copy.setEngine(engine);
	_copy.setEventChannelFactoryServant(eventChannelFactoryServant);
	_copy.setNotificationEventFactory(notificationEventFactory);
	_copy.setDefaultFilterFactory(defaultFilterFactory);

	return _copy;
    }

}// ChannelContext
