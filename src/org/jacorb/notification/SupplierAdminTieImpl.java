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

import java.util.Iterator;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.ProxyPullConsumer;
import org.omg.CosEventChannelAdmin.ProxyPushConsumer;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosNotifyChannelAdmin.ProxyConsumer;
import org.omg.CosNotifyChannelAdmin.ProxyConsumerHelper;
import org.omg.CosNotifyChannelAdmin.ProxyNotFound;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyChannelAdmin.SupplierAdminOperations;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.NotifyPublishOperations;
import org.omg.CosNotifyChannelAdmin.ProxyPullConsumerPOATie;
import org.omg.CosNotifyChannelAdmin.ProxyPushConsumerPOATie;
import org.apache.log4j.Logger;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullConsumerPOATie;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushConsumerPOATie;
import org.omg.CosNotifyChannelAdmin.ProxyPushConsumerHelper;
import org.omg.CORBA.BAD_PARAM;
import java.util.List;
import java.util.Vector;
import org.jacorb.notification.framework.Disposable;
import org.omg.CosNotifyChannelAdmin.SupplierAdminPOATie;
import org.jacorb.notification.framework.EventDispatcher;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPushConsumerPOATie;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPullConsumerPOATie;

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

    SupplierAdmin thisRef_;
    List eventStyleServants_ = new Vector();

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

    public org.omg.CORBA.Object getThisRef() {
	return getSupplierAdmin();
    }

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

	switch(clientType.value()) {
	case ClientType._ANY_EVENT: {
	    ProxyPullConsumerImpl _proxyImpl = new ProxyPullConsumerImpl(applicationContext_,
									 channelContext_,
									 this,
									 thisRef_,
									 _key);
	    pullServants_.put(_key, _proxyImpl);

	    ProxyPullConsumerPOATie _pullConsumerPOATie = new ProxyPullConsumerPOATie(_proxyImpl);

	    _pullConsumer = _pullConsumerPOATie._this(getOrb()); 
	    
	    break;
	}
	case ClientType._STRUCTURED_EVENT: {
	    StructuredProxyPullConsumerImpl _proxyImpl = 
		new StructuredProxyPullConsumerImpl(applicationContext_, 
						    channelContext_,
						    this,
						    thisRef_,
						    _key);
	    
	    _proxyImpl.setProxyType(ProxyType.PULL_STRUCTURED);
	    pullServants_.put(_key, _proxyImpl);
	    StructuredProxyPullConsumerPOATie _pullConsumerPOATie = new StructuredProxyPullConsumerPOATie(_proxyImpl);
	    _pullConsumer = _pullConsumerPOATie._this(getOrb());

	    break;
	}
	case ClientType._SEQUENCE_EVENT: {
	    SequenceProxyPullConsumerImpl _proxyImpl = 
		new SequenceProxyPullConsumerImpl(applicationContext_, 
						  channelContext_,
						  this,
						  thisRef_,
						  _key);
	    
	    _proxyImpl.setProxyType(ProxyType.PULL_SEQUENCE);
	    pullServants_.put(_key, _proxyImpl);

	    SequenceProxyPullConsumerPOATie _pullConsumerPOATie = 
		new SequenceProxyPullConsumerPOATie(_proxyImpl);

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

	ProxyConsumer _proxyConsumer = null;

	switch (clientType.value()) {
	case ClientType._ANY_EVENT: {

	    ProxyPushConsumerImpl _proxyConsumerServant = new ProxyPushConsumerImpl(applicationContext_,
										    channelContext_,
										    this,
										    thisRef_,
										    _key);

	    pushServants_.put(_key, _proxyConsumerServant);

	    ProxyPushConsumerPOATie _proxyPushConsumerPOATie = 
		new ProxyPushConsumerPOATie(_proxyConsumerServant);

	    _proxyConsumer = _proxyPushConsumerPOATie._this(getOrb());

	    break;
	}

	case ClientType._STRUCTURED_EVENT: {

	    StructuredProxyPushConsumerImpl _proxyConsumerServant = 
		new StructuredProxyPushConsumerImpl(applicationContext_,
						    channelContext_,
						    this,
						    thisRef_,
						    _key);
	    
	    _proxyConsumerServant.setProxyType(ProxyType.PUSH_STRUCTURED);
	    pushServants_.put(_key, _proxyConsumerServant);

	    StructuredProxyPushConsumerPOATie _proxyPushConsumerPOATie = 
		new StructuredProxyPushConsumerPOATie(_proxyConsumerServant);

	    _proxyConsumer = ProxyConsumerHelper.narrow(_proxyPushConsumerPOATie._this(getOrb()));

	    break;
	}
	case ClientType._SEQUENCE_EVENT: {

	    logger_.debug("Create sequence style");

	    SequenceProxyPushConsumerImpl _proxyConsumerServant = 
		new SequenceProxyPushConsumerImpl(applicationContext_,
						    channelContext_,
						    this,
						    thisRef_,
						    _key);
	    
	    _proxyConsumerServant.setProxyType(ProxyType.PUSH_SEQUENCE);
	    pushServants_.put(_key, _proxyConsumerServant);

	    SequenceProxyPushConsumerPOATie _proxyPushConsumerPOATie = 
		new SequenceProxyPushConsumerPOATie(_proxyConsumerServant);

	    _proxyConsumer = ProxyConsumerHelper.narrow(_proxyPushConsumerPOATie._this(getOrb()));

	    break;
	}
	default:
	    throw new BAD_PARAM();
	}

        pushProxies_.put(_key, _proxyConsumer);
        allProxies_.put(_key, ProxyConsumerHelper.narrow(_proxyConsumer));

        return _proxyConsumer;
    }

    // Implementation of org.omg.CosEventChannelAdmin.SupplierAdminOperations

    /**
     * Describe <code>obtain_pull_consumer</code> method here.
     *
     * @return a <code>ProxyPullConsumer</code> value
     */
    public ProxyPullConsumer obtain_pull_consumer() {
        ProxyPullConsumerImpl _servant =  new ProxyPullConsumerImpl(applicationContext_,
								    channelContext_,
								    this, 
								    thisRef_);
	_servant.setFilterManager(FilterManager.EMPTY);
	eventStyleServants_.add(_servant);

	org.omg.CosEventChannelAdmin.ProxyPullConsumerPOATie _tie = 
	    new org.omg.CosEventChannelAdmin.ProxyPullConsumerPOATie(_servant);

	ProxyPullConsumer _ret = org.omg.CosEventChannelAdmin.ProxyPullConsumerHelper.narrow(_tie._this(getOrb()));

        return _ret;
    }

    /**
     * Return a ProxyPushConsumer reference to be used to connect to a
     * PushSupplier.
     */
    public ProxyPushConsumer obtain_push_consumer() {
        ProxyPushConsumerImpl _servant = new ProxyPushConsumerImpl(applicationContext_, 
								   channelContext_,
								   this, 
								   thisRef_);
	_servant.setFilterManager(FilterManager.EMPTY);
	eventStyleServants_.add(_servant);

	org.omg.CosEventChannelAdmin.ProxyPushConsumerPOATie _tie = 
	    new org.omg.CosEventChannelAdmin.ProxyPushConsumerPOATie(_servant);
	
	ProxyPushConsumer _ret = org.omg.CosEventChannelAdmin.ProxyPushConsumerHelper.narrow(_tie._this(getOrb()));

        return _ret;
    }

    public List getSubsequentDestinations() {
	return getChannelServant().getAllConsumerAdmins();
    }

    public EventDispatcher getEventDispatcher() {
	return null;
    }

    public boolean hasEventDispatcher() {
	return false;
    }

    public void dispose() {
	super.dispose();
	Iterator _i = eventStyleServants_.iterator();
	while(_i.hasNext()) {
	    ((Disposable)_i.next()).dispose();
	}
	eventStyleServants_.clear();
    }

    public void remove(ProxyBase pb) {
	Integer _key = pb.getKey();

	if (_key != null) {
	    allProxies_.remove(_key);
	    if (pb instanceof StructuredProxyPullConsumerImpl || 
		pb instanceof ProxyPullConsumerImpl ||
		pb instanceof SequenceProxyPullConsumerImpl) {
		
		pullServants_.remove(_key);
		pullProxies_.remove(_key);
		
	    } else if (pb instanceof StructuredProxyPushConsumerImpl || 
		       pb instanceof ProxyPushConsumerImpl ||
		       pb instanceof SequenceProxyPushConsumerImpl) {
		
		pushServants_.remove(_key);
		pushProxies_.remove(_key);
	    }
	} else {
	    eventStyleServants_.remove(pb);
	}
    }
}// SupplierAdminImpl
