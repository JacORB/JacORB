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
package org.jacorb.notification;

import java.util.Map;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import java.util.Hashtable;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterNotFound;
import org.omg.CosNotifyFilter.FilterAdminOperations;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotification.QoSAdminOperations;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.jacorb.notification.engine.Destination;
import java.util.Arrays;
import java.util.List;
import org.jacorb.notification.framework.Disposable;

/*
 *        JacORB - a free Java ORB
 */

/**
 * AdminBase.java
 *
 *
 * Created: Fri Nov 01 18:39:31 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

abstract class AdminBase implements QoSAdminOperations, FilterAdminOperations, Destination {

    protected ChannelContext channelContext_;
    protected ApplicationContext applicationContext_;

    protected Map filters_;
    protected int id_ = 0;
    protected int filterIdPool_ = -1;;
    protected int proxyIdPool_ = 0;

    protected InterFilterGroupOperator myOperator_;
    protected InterFilterGroupOperator filterOperator_;

    protected Map pushProxies_;
    protected Map pullProxies_;
    protected Map pullServants_;
    protected Map pushServants_;

    protected Map allProxies_;

    protected NotificationEventFactory getNotificationEventFactory() {
	return channelContext_.getNotificationEventFactory();
    }
    protected EventChannelImpl getChannelServant() {
	return channelContext_.getEventChannelServant();
    }
    protected EventChannel getChannel() {
	return channelContext_.getEventChannel();
    }
    protected ORB getOrb() {
	return applicationContext_.getOrb();
    }
    protected POA getPoa() {
	return applicationContext_.getPoa();
    }

    protected Logger logger_;

    static InterFilterGroupOperator DEFAULT_FILTER_GROUP_OPERATOR = null;
    static int NO_ID = Integer.MIN_VALUE;

    protected AdminBase(ApplicationContext appContext,
			ChannelContext channelContext,
			int myId,
			InterFilterGroupOperator filterGroupOperator,
			Logger logger) {

	applicationContext_ = appContext;
	channelContext_ = channelContext;
	logger_ = logger;

	pullProxies_ = new Hashtable();
	pushProxies_ = new Hashtable();
	pullServants_ = new Hashtable();
	pushServants_ = new Hashtable();
	allProxies_ = new Hashtable();
	filters_ = getChannelServant().getFilterMap(this);

	if (logger_.isDebugEnabled()) {
	    logger_.debug("NotificationEventFactory = " + getNotificationEventFactory());
	    if (myId == NO_ID) {
		logger_.debug("Admin has no id");
	    } else {
		logger_.debug("My ID = " + myId);
	    }
	}

    }
    
    protected AdminBase(ApplicationContext appContext,
			ChannelContext channelContext,
			Logger logger) {

	this(appContext,
	     channelContext,
	     NO_ID, 
	     DEFAULT_FILTER_GROUP_OPERATOR,
	     logger);
    }

    int getFilterId() {
	return ++filterIdPool_;
    }

    int getPushProxyId() {
	return ++proxyIdPool_;
    }

    int getPullProxyId() {
	return ++proxyIdPool_;
    }
    
    /**
     * Describe <code>get_filter</code> method here.
     *
     * @param n an <code>int</code> value
     * @return a <code>Filter</code> value
     * @exception FilterNotFound if an error occurs
     */
    public Filter get_filter(int n) throws FilterNotFound {
	Integer _key = new Integer(n);

	Filter _filter = (Filter)filters_.get(_key);
	if (_filter == null) {
	    throw new FilterNotFound();
	}
	return _filter;
    }

    /**
     * Describe <code>add_filter</code> method here.
     *
     * @param filter a <code>Filter</code> value
     * @return an <code>int</code> value
     */
    public int add_filter(Filter filter) {
	logger_.info("add_filter(Filter) " + this);

	int _key = getFilterId();

	filters_.put(new Integer(_key), filter);

	return _key;
    }

    /**
     * Describe <code>remove_filter</code> method here.
     *
     * @param n an <code>int</code> value
     * @exception FilterNotFound if an error occurs
     */
    public void remove_filter(int n) throws FilterNotFound {
	Integer _key = new Integer(n);
	if (!filters_.containsKey(_key)) {
	    throw new FilterNotFound();
	}
	filters_.remove(_key);
    }

    /**
     * Describe <code>remove_all_filters</code> method here.
     *
     */
    public void remove_all_filters() {
	filters_.clear();
    }

    public int[] get_all_filters() {
	return null;
    }

    /**
     * Describe <code>MyOperator</code> method here.
     *
     * @return an <code>InterFilterGroupOperator</code> value
     */
    public InterFilterGroupOperator MyOperator() {
	return myOperator_;
    }

    /**
     * Describe <code>MyChannel</code> method here.
     *
     * @return an <code>EventChannel</code> value
     */
    public EventChannel MyChannel() {
	return getChannel();
    }

    /**
     * Describe <code>MyID</code> method here.
     *
     * @return an <code>int</code> value
     */
    public int MyID() {
	return id_;
    }

    
    // Implementation of org.omg.CosNotification.QoSAdminOperations

    /**
     * Describe <code>get_qos</code> method here.
     *
     * @return a <code>Property[]</code> value
     */
    public Property[] get_qos() {
	return null;
    }

    /**
     * Describe <code>set_qos</code> method here.
     *
     * @param property a <code>Property[]</code> value
     * @exception UnsupportedQoS if an error occurs
     */
    public void set_qos(Property[] property1) throws UnsupportedQoS {
	
    }

    /**
     * Describe <code>validate_qos</code> method here.
     *
     * @param property a <code>Property[]</code> value
     * @param namedPropertyRangeSeqHolder a
     * <code>NamedPropertyRangeSeqHolder</code> value
     * @exception UnsupportedQoS if an error occurs
     */
    public void validate_qos(Property[] property1, 
			     NamedPropertyRangeSeqHolder namedPropertyRangeSeqHolder) throws UnsupportedQoS {
	
    }

    public List getFilters() {
	return Arrays.asList(filters_.values().toArray());
    }

    public void dispose() {
	Iterator _i = pushServants_.values().iterator();
	while (_i.hasNext()) {
	    logger_.info("dispose pushServant");
	    ((Disposable)_i.next()).dispose();
	}

	_i = pullServants_.values().iterator();
	while (_i.hasNext()) {
	    logger_.info("dispose pullServant");
	    ((Disposable)_i.next()).dispose();
	}
    }

}// AdminBase
