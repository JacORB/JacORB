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

import java.util.Iterator;
import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.ProxyPullConsumer;
import org.omg.CosEventChannelAdmin.ProxyPushConsumer;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.QoSAdminOperations;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosNotifyChannelAdmin.ProxyConsumer;
import org.omg.CosNotifyChannelAdmin.ProxyConsumerHelper;
import org.omg.CosNotifyChannelAdmin.ProxyNotFound;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyChannelAdmin.SupplierAdminOperations;
import org.omg.CosNotifyChannelAdmin.SupplierAdminPOA;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.NotifyPublishOperations;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterAdminOperations;
import org.omg.CosNotifyFilter.FilterNotFound;
import org.omg.CosNotifyFilter.UnsupportedFilterableData;
import org.omg.PortableServer.POA;
import org.omg.CosNotifyChannelAdmin.ProxyPullConsumerPOATie;
import org.omg.CosNotifyChannelAdmin.ProxyPushConsumerPOATie;
import org.apache.log4j.Logger;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullConsumerPOATie;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushConsumerPOATie;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.ProxyPushConsumerHelper;
import org.omg.CORBA.BAD_PARAM;
import java.util.List;
import java.util.Vector;
import java.util.Collections;
import org.jacorb.notification.framework.Disposable;
import org.omg.CosNotifyChannelAdmin.SupplierAdminPOATie;

/*
 *        JacORB - a free Java ORB
 */

/**
 * SupplierAdminImpl.java
 *
 *
 * Created: Sun Oct 13 01:39:12 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class SupplierAdminTieImpl 
    extends AdminBase 
    implements SupplierAdminOperations,
	       Disposable {

    SupplierAdminTieImpl(ApplicationContext appContext,
			 ChannelContext channelContext) {
        super(appContext, channelContext, Logger.getLogger("SupplierAdmin"));
    }

    SupplierAdminTieImpl(ApplicationContext appContext, 
			 ChannelContext channelContext, 
			 int myId,
                         InterFilterGroupOperator myOperator) {

        super(appContext, 
	      channelContext, 
	      myId, 
	      myOperator, 
	      Logger.getLogger("SupplierAdmin"));
    }

    SupplierAdmin thisRef_;
    List eventStyleProxies_ = new Vector();

    SupplierAdmin getSupplierAdmin() {
	if (thisRef_ == null) {
	    thisRef_ = new SupplierAdminPOATie(this)._this(getOrb());
	}
        return thisRef_;
    }

    // Implementation of org.omg.CosNotifyComm.NotifyPublishOperations

    /**
     * Describe <code>offer_change</code> method here.
     *
     * @param eventType an <code>EventType[]</code> value
     * @param eventType an <code>EventType[]</code> value
     * @exception InvalidEventType if an error occurs
     */
    public void offer_change(EventType[] eventType1, EventType[] eventType2) throws InvalidEventType {
    }

    // Implementation of org.omg.CosNotifyChannelAdmin.SupplierAdminOperations

    /**
     * Describe <code>destroy</code> method here.
     *
     */
    public void destroy() {
	dispose();
    }

    /**
     * Describe <code>pull_consumers</code> method here.
     *
     * @return an <code>int[]</code> value
     */
    public int[] pull_consumers() {
        int[] _ret = new int[pullProxies_.size()];
        Iterator _i = pullProxies_.keySet().iterator();
        int x=-1;
        while (_i.hasNext()) {
            _ret[++x] = ((Integer)_i.next()).intValue();
        }
        return _ret;
    }

    /**
     * Describe <code>push_consumers</code> method here.
     *
     * @return an <code>int[]</code> value
     */
    public int[] push_consumers() {
        int[] _ret = new int[pushProxies_.size()];
        Iterator _i = pushProxies_.keySet().iterator();
        int x=-1;
        while (_i.hasNext()) {
            _ret[++x] = ((Integer)_i.next()).intValue();
        }
        return _ret;
    }

    /**
     * Describe <code>obtain_notification_pull_consumer</code> method here.
     *
     * @param clientType a <code>ClientType</code> value
     * @param intHolder an <code>IntHolder</code> value
     * @return a <code>ProxyConsumer</code> value
     * @exception AdminLimitExceeded if an error occurs
     */
    public ProxyConsumer obtain_notification_pull_consumer(ClientType clientType,
							   IntHolder intHolder) throws AdminLimitExceeded {

	logger_.info("obtain_notification_pull_consumer()");

	ProxyConsumer _pullConsumer = null;
	intHolder.value = getPullProxyId();
	Integer _key = new Integer(intHolder.value);
	Thread _pullThread;

	switch(clientType.value()) {
	case ClientType._ANY_EVENT: {
	    ProxyPullConsumerImpl _proxyImpl = new ProxyPullConsumerImpl(applicationContext_,
									 channelContext_,
									 this,
									 thisRef_);
	    pullServants_.put(_key, _proxyImpl);

	    ProxyPullConsumerPOATie _pullConsumerPOATie = new ProxyPullConsumerPOATie(_proxyImpl);


	    
	    _proxyImpl.setFilterMap(getChannelServant().getFilterMap(_proxyImpl));
	    
	    _pullThread = new Thread(_proxyImpl);
	    _pullConsumer = _pullConsumerPOATie._this(getOrb()); 
	    
	    break;
	}
	case ClientType._STRUCTURED_EVENT: {
	    StructuredProxyPullConsumerImpl _proxyImpl = 
		new StructuredProxyPullConsumerImpl(applicationContext_, 
						    channelContext_,
						    this,
						    thisRef_);
	    
	    pullServants_.put(_key, _proxyImpl);

	    _proxyImpl.setFilterMap(getChannelServant().getFilterMap(_proxyImpl));

	    StructuredProxyPullConsumerPOATie _pullConsumerPOATie = new StructuredProxyPullConsumerPOATie(_proxyImpl);
	    _pullThread = new Thread(_proxyImpl);
	    _pullConsumer = _pullConsumerPOATie._this(getOrb());

	    break;
	}
	default:
	    throw new BAD_PARAM();
	}
        pullProxies_.put(_key, _pullConsumer);
        allProxies_.put(_key, ProxyConsumerHelper.narrow(_pullConsumer));

        return _pullConsumer;
    }

    /**
     * Describe <code>get_proxy_consumer</code> method here.
     *
     * @param n an <code>int</code> value
     * @return a <code>ProxyConsumer</code> value
     * @exception ProxyNotFound if an error occurs
     */
    public ProxyConsumer get_proxy_consumer(int n) throws ProxyNotFound {
        ProxyConsumer _ret = (ProxyConsumer)allProxies_.get(new Integer(n));
        if (_ret == null) {
            throw new ProxyNotFound();
        }
        return _ret;
    }

    /**
     * Describe <code>obtain_notification_push_consumer</code> method here.
     *
     * @param clientType a <code>ClientType</code> value
     * @param intHolder an <code>IntHolder</code> value
     * @return a <code>ProxyConsumer</code> value
     * @exception AdminLimitExceeded if an error occurs
     */
    public ProxyConsumer obtain_notification_push_consumer(ClientType clientType,
							   IntHolder intHolder) throws AdminLimitExceeded {
	logger_.info("obtain_notification_push_consumer()");

	intHolder.value = getPushProxyId();
	Integer _key = new Integer(intHolder.value);

	logger_.info("Id is: " + _key);

	ProxyConsumer _proxyConsumer = null;

	switch (clientType.value()) {
	case ClientType._ANY_EVENT: {
	    logger_.debug("PushConsumer for ANY Events requested");

	    logger_.debug("AppContext: "+ applicationContext_);
	    logger_.debug("ChannelContext: " +channelContext_);
	    logger_.debug(thisRef_);

	    ProxyPushConsumerImpl _proxyConsumerServant = new ProxyPushConsumerImpl(applicationContext_,
										    channelContext_,
										    this,
										    thisRef_);

	    pushServants_.put(_key, _proxyConsumerServant);

	    _proxyConsumerServant.setFilterMap(getChannelServant().getFilterMap(_proxyConsumerServant));

	    ProxyPushConsumerPOATie _proxyPushConsumerPOATie = new ProxyPushConsumerPOATie(_proxyConsumerServant);

	    _proxyConsumer = _proxyPushConsumerPOATie._this(getOrb());

	    break;
	}

	case ClientType._STRUCTURED_EVENT: {
	    logger_.debug("PushConsumer for Structured Events requested");

	    StructuredProxyPushConsumerImpl _proxyConsumerServant = 
		new StructuredProxyPushConsumerImpl(applicationContext_,
						    channelContext_,
						    this,
						    thisRef_);

	    pushServants_.put(_key, _proxyConsumerServant);

	    _proxyConsumerServant.setFilterMap(getChannelServant().getFilterMap(_proxyConsumerServant));

	    logger_.debug("Created Servant: " + _proxyConsumerServant);
	    logger_.debug("Added Servant to Event Channel");

	    StructuredProxyPushConsumerPOATie _proxyPushConsumerPOATie = 
		new StructuredProxyPushConsumerPOATie(_proxyConsumerServant);

	    _proxyConsumer = ProxyConsumerHelper.narrow(_proxyPushConsumerPOATie._this(getOrb()));

	    break;
	}
	default:
	    throw new BAD_PARAM();
	}


        pushProxies_.put(_key, _proxyConsumer);
	logger_.debug("Added to PushProxies list");

        allProxies_.put(_key, ProxyConsumerHelper.narrow(_proxyConsumer));
	logger_.debug("Added to Total list");

        return _proxyConsumer;
    }

    // Implementation of org.omg.CosEventChannelAdmin.SupplierAdminOperations

    /**
     * Describe <code>obtain_pull_consumer</code> method here.
     *
     * @return a <code>ProxyPullConsumer</code> value
     */
    public ProxyPullConsumer obtain_pull_consumer() {
        ProxyPullConsumerImpl p =  new ProxyPullConsumerImpl(applicationContext_,
							     channelContext_,
							     this, 
							     thisRef_);
	p.setFilterMap(Collections.EMPTY_MAP);
	eventStyleProxies_.add(p);
        return org.omg.CosEventChannelAdmin.ProxyPullConsumerHelper.narrow(new org.omg.CosEventChannelAdmin.ProxyPullConsumerPOATie(p)._this(getOrb()));
    }

    /**
     * Return a ProxyPushConsumer reference to be used to connect to a
     * PushSupplier.
     */
    public ProxyPushConsumer obtain_push_consumer() {
        ProxyPushConsumerImpl p = new ProxyPushConsumerImpl(applicationContext_, 
							    channelContext_,
							    this, 
							    thisRef_);
	p.setFilterMap(Collections.EMPTY_MAP);
	eventStyleProxies_.add(p);
        return org.omg.CosEventChannelAdmin.ProxyPushConsumerHelper.narrow(new org.omg.CosEventChannelAdmin.ProxyPushConsumerPOATie(p)._this(getOrb()));
    }

    public List getSubsequentDestinations() {
	return getChannelServant().getAllConsumerAdmins();
    }

    public TransmitEventCapable getEventSink() {
	return null;
    }

    public void dispose() {
	super.dispose();
	Iterator _i = eventStyleProxies_.iterator();
	while(_i.hasNext()) {
	    ((Disposable)_i.next()).dispose();
	}
    }

}// SupplierAdminImpl
