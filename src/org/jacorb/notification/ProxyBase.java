/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2002 Gerald Brose
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
package org.jacorb.notification;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jacorb.notification.engine.Destination;
import org.jacorb.notification.framework.Disposable;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.QoSAdminOperations;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyActive;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyInactive;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.NotConnected;
import org.omg.CosNotifyChannelAdmin.ObtainInfoMode;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.NotifyPublishOperations;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterAdminOperations;
import org.omg.CosNotifyFilter.FilterNotFound;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.PortableServer.POA;

/*
 *        JacORB - a free Java ORB
 */

/**
 * ProxyBase.java
 *
 *
 * Created: Sun Nov 03 22:49:01 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

abstract class ProxyBase implements FilterAdminOperations, 
				    NotifyPublishOperations, 
				    QoSAdminOperations, 
				    Destination,
				    Disposable {

    protected NotificationEventFactory notificationEventFactory_;
    protected EventChannelImpl eventChannel_;
    protected POA poa_;
    protected ORB orb_;
    protected Logger logger_;
    protected boolean connected_;
    protected Map filters_;
    protected int filterIdPool_ = 0;
    protected ChannelContext channelContext_;
    protected ApplicationContext applicationContext_;

    int getFilterId() {
	return ++filterIdPool_;
    }

    protected ProxyBase(ApplicationContext appContext,
			ChannelContext channelContext,
			Logger logger) {

	this(appContext.getOrb(), 
	     appContext.getPoa(), 
	     channelContext.getEventChannelServant(), 
	     logger,
	     channelContext.getNotificationEventFactory());

	applicationContext_ = appContext;
	channelContext_ = channelContext;
    }

    protected ProxyBase(ORB orb, 
			POA poa,
			EventChannelImpl myChannelServant,
			Logger logger,
			NotificationEventFactory notificationEventFactory) {
	this(orb, poa, myChannelServant, logger);
	notificationEventFactory_ = notificationEventFactory;
    }
    
    protected ProxyBase(ORB orb, 
			POA poa, 
			EventChannelImpl myChannelServant,
			Logger logger) {
	poa_ = poa;
	orb_ = orb;
	logger_ = logger;
	eventChannel_ = myChannelServant;
	connected_ = false;
    }

    void setFilterMap(Map filters) {
	filters_ = filters;
    }

    public int add_filter(Filter filter) {
	logger_.info("add_filter(Filter)");

	int _key = getFilterId();

	filters_.put(new Integer(_key), filter);

	return _key;
    }

    public void remove_filter(int filterId) throws FilterNotFound {
    }

    public Filter get_filter(int filterId) throws FilterNotFound {
	return null;
    }

    public void remove_all_filters() {
    }
    
    public int[] get_all_filters() {
	return null;
    }

    public EventType[] obtain_subscription_types(ObtainInfoMode obtainInfoMode) {
        return null;
    }

    public void validate_event_qos(Property[] qosProps, NamedPropertyRangeSeqHolder propSeqHolder)
    throws UnsupportedQoS {}

    public void validate_qos(Property[] qosProps, NamedPropertyRangeSeqHolder propSeqHolder)
    throws UnsupportedQoS {}

    public void set_qos(Property[] qosProps) throws UnsupportedQoS {}

    public Property[] get_qos() {
        return null;
    }

    public void offer_change(EventType[] eventTypes, EventType[] eventTypes2) throws InvalidEventType {}

    public void suspend_connection()
	throws NotConnected, ConnectionAlreadyInactive {}

    public void resume_connection()
	throws ConnectionAlreadyActive, NotConnected {}

    public void subscription_change(EventType[] eventType, EventType[] eventType2) throws InvalidEventType {}

    public void priority_filter(MappingFilter filter) {
    }

    public MappingFilter priority_filter() {
	return null;
    }

    public MappingFilter lifetime_filter() {
	return null;
    }

    public void lifetime_filter(MappingFilter filter) {
    }

    public EventType[] obtain_offered_types(ObtainInfoMode obtaininfomode) {
	return null;
    }

    /**
      * Override this method from the Servant baseclass.  Fintan Bolton
      * in his book "Pure CORBA" suggests that you override this method to
      * avoid the risk that a servant object (like this one) could be
      * activated by the <b>wrong</b> POA object.
      */
     public POA _default_POA() {
         return applicationContext_.getPoa();
     }

    protected void debug(String msg) {
	logger_.debug(msg);
    }

    public List getFilters() {
	Collection _c = filters_.values();

	return Arrays.asList(_c.toArray());
    }

}// ProxyBase

