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

import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import org.jacorb.notification.framework.DistributorNode;
import org.jacorb.notification.framework.Disposable;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.QoSAdminOperations;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyActive;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyInactive;
import org.omg.CosNotifyChannelAdmin.NotConnected;
import org.omg.CosNotifyChannelAdmin.ObtainInfoMode;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.NotifyPublishOperations;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterAdminOperations;
import org.omg.CosNotifyFilter.FilterNotFound;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.PortableServer.POA;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import org.omg.CosNotifyFilter.FilterNotFound;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CosNotifyChannelAdmin.ProxyType;

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
				    DistributorNode,
				    Disposable {

    static Integer NO_KEY = null;

    protected NotificationEventFactory notificationEventFactory_;
    protected EventChannelImpl eventChannel_;
    protected POA poa_;
    protected ORB orb_;
    protected Logger logger_;
    protected boolean connected_;
    protected ChannelContext channelContext_;
    protected ApplicationContext applicationContext_;
    protected Integer key_;
    protected AdminBase myAdmin_;
    protected FilterManager filterManager_;
    protected boolean disposed_ = false;
    protected ProxyType proxyType_;

    protected ProxyBase(AdminBase admin,
			ApplicationContext appContext,
			ChannelContext channelContext,
			Logger logger) {

	this(admin,
	     appContext,
	     channelContext,
	     NO_KEY,
	     logger);
    }

    protected ProxyBase(AdminBase admin,
			ApplicationContext appContext,
			ChannelContext channelContext,
			Integer key,
			Logger logger) {
	
	myAdmin_ = admin;
	key_ = key;
	applicationContext_ = appContext;
	channelContext_ = channelContext;
	poa_ = appContext.getPoa();
	orb_ = appContext.getOrb();
	logger_ = logger;
	eventChannel_ = channelContext.getEventChannelServant();
	connected_ = false;
	notificationEventFactory_ = applicationContext_.getNotificationEventFactory();
	filterManager_ = new FilterManager();
    }
    
    public int add_filter(Filter filter) {
	return filterManager_.add_filter(filter);
    }

    public void remove_filter(int n) throws FilterNotFound {
	filterManager_.remove_filter(n);
    }

    public Filter get_filter(int n) throws FilterNotFound {
	return filterManager_.get_filter(n);
    }

    public int[] get_all_filters() {
	return filterManager_.get_all_filters();
    }

    public void remove_all_filters() {
	filterManager_.remove_all_filters();
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

    Integer getKey() {
	return key_;
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

    void setFilterManager(FilterManager manager) {
	filterManager_ = manager;
    }

    public List getFilters() {
	return filterManager_.getFilters();
    }

    public void dispose() {
	if (!disposed_) {
	    remove_all_filters();
	    disposed_ = true;
	} else {
	    throw new OBJECT_NOT_EXIST();
	}
    }

    void setProxyType(ProxyType pt) {
	proxyType_ = pt;
    }

    public ProxyType MyType() {
	return proxyType_;
    }
}// ProxyBase
