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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CORBA.UNKNOWN;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.QoSAdminOperations;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.ConsumerAdminOperations;
import org.omg.CosNotifyChannelAdmin.ConsumerAdminPOA;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosNotifyChannelAdmin.ProxyNotFound;
import org.omg.CosNotifyChannelAdmin.ProxyPullSupplierPOATie;
import org.omg.CosNotifyChannelAdmin.ProxyPushSupplierHelper;
import org.omg.CosNotifyChannelAdmin.ProxyPushSupplierPOATie;
import org.omg.CosNotifyChannelAdmin.ProxySupplier;
import org.omg.CosNotifyChannelAdmin.ProxySupplierHelper;
import org.omg.CosNotifyChannelAdmin.ProxySupplierPOATie;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushSupplierPOATie;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.NotifySubscribeOperations;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterAdminOperations;
import org.omg.CosNotifyFilter.FilterNotFound;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.omg.CosNotifyComm.StructuredPullSupplierPOATie;
import org.omg.CORBA.BAD_PARAM;
import java.util.List;
import java.util.Vector;
import java.util.Collections;
import org.jacorb.notification.framework.Disposable;
import org.omg.CosNotifyChannelAdmin.ConsumerAdminPOATie;
import org.jacorb.notification.framework.EventDispatcher;
import org.omg.CosNotifyChannelAdmin.ProxyPullSupplierHelper;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullSupplierPOATie;
import org.omg.CosEventChannelAdmin.ProxyPushSupplier;
import org.omg.CosEventChannelAdmin.ProxyPullSupplier;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPullSupplierPOATie;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPushSupplierPOATie;

/**
 * ConsumerAdminImpl.java
 *
 *
 * Created: Sat Oct 12 23:43:18 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class ConsumerAdminTieImpl 
    extends AdminBase 
    implements ConsumerAdminOperations, 
	       Disposable {

    List eventStyleServants_ = new Vector();
    ConsumerAdmin thisRef_;

    public ConsumerAdminTieImpl(ApplicationContext appContext,
				ChannelContext channelContext) {

	super(appContext, channelContext, Logger.getLogger("ConsumerAdmin"));
    }
    
    public ConsumerAdminTieImpl(ApplicationContext appContext,
				ChannelContext channelContext,
				int myId,
				InterFilterGroupOperator filterGroupOperator) {
	super(appContext, 
	      channelContext, 
	      myId, 
	      filterGroupOperator,
	      Logger.getLogger("ConsumerAdmin"));
    }

    public org.omg.CORBA.Object getThisRef() {
	return getConsumerAdmin();
    }

    ConsumerAdmin getConsumerAdmin() {
	if (thisRef_ == null) {
	    thisRef_ = new ConsumerAdminPOATie(this)._this(getOrb());
	}
	return thisRef_;
    }

    public void subscription_change(EventType[] eventType1, EventType[] eventType2) throws InvalidEventType {
    }

    public void destroy() {
	dispose();
    }

    public ProxySupplier get_proxy_supplier(int n) throws ProxyNotFound {
	ProxySupplier _ret = (ProxySupplier)allProxies_.get(new Integer(n));
	if (_ret == null) {
	    throw new ProxyNotFound();
	}
	return _ret;
    }

    public void lifetime_filter(MappingFilter mappingFilter) {
    }

    public MappingFilter lifetime_filter() {
	return null;
    }

    public MappingFilter priority_filter() {
	return null;
    }

    public void priority_filter(MappingFilter mappingFilter) {
    }

    public ProxySupplier obtain_notification_pull_supplier(ClientType clientType, 
							   IntHolder intHolder) throws AdminLimitExceeded {

	intHolder.value = getPullProxyId();
	Integer _key = new Integer(intHolder.value);
	ProxySupplier _pullSupplier = null;

	switch(clientType.value()) {
	case ClientType._ANY_EVENT: {
	    ProxyPullSupplierImpl _pullSupplierServant = new ProxyPullSupplierImpl(applicationContext_,
										   channelContext_,
										   this,
										   thisRef_,
										   _key);

	    pullServants_.put(_key, _pullSupplierServant);

	    ProxyPullSupplierPOATie _pullSupplierPOATie = new ProxyPullSupplierPOATie(_pullSupplierServant);
	    _pullSupplier = _pullSupplierPOATie._this(getOrb());

	    break;
	}
	case ClientType._STRUCTURED_EVENT: {
	    StructuredProxyPullSupplierImpl _pullSupplierServant = 
		new StructuredProxyPullSupplierImpl(applicationContext_,
						    channelContext_,
						    this,
						    thisRef_,
						    _key);

	    _pullSupplierServant.setProxyType(ProxyType.PULL_STRUCTURED);
	    pullServants_.put(_key, _pullSupplierServant);

	    StructuredProxyPullSupplierPOATie _pullSupplierPOATie = 
		new StructuredProxyPullSupplierPOATie(_pullSupplierServant);

	    _pullSupplier = _pullSupplierPOATie._this(getOrb());

	    break;
	}
	case ClientType._SEQUENCE_EVENT: {
	    SequenceProxyPullSupplierImpl _pullSupplierServant = 
		new SequenceProxyPullSupplierImpl(applicationContext_,
						    channelContext_,
						    this,
						    thisRef_,
						    _key);
	    
	    _pullSupplierServant.setProxyType(ProxyType.PULL_SEQUENCE);
	    pullServants_.put(_key, _pullSupplierServant);

	    SequenceProxyPullSupplierPOATie _pullSupplierPOATie = 
		new SequenceProxyPullSupplierPOATie(_pullSupplierServant);

	    _pullSupplier = _pullSupplierPOATie._this(getOrb());

	    break;
	}
	default:
	    throw new BAD_PARAM();
	}

	pullProxies_.put(_key, _pullSupplier);
	allProxies_.put(_key, ProxySupplierHelper.narrow(_pullSupplier));

	return _pullSupplier;
    }

    public void remove(ProxyBase pb) {
	logger_.debug("Remove Proxy: " + pb);

	Integer _key = pb.getKey();
	if (_key != null) {
	    allProxies_.remove(_key);

	    if (pb instanceof StructuredProxyPullSupplierImpl || 
		pb instanceof ProxyPullSupplierImpl ||
		pb instanceof SequenceProxyPullSupplierImpl) {

		pullServants_.remove(_key);
		pullProxies_.remove(_key);
	    } else if (pb instanceof StructuredProxyPushSupplierImpl || 
		       pb instanceof ProxyPushSupplierImpl ||
		       pb instanceof SequenceProxyPushSupplierImpl) {

		pushServants_.remove(_key);
		pushProxies_.remove(_key);
	    }
	} else {
	    eventStyleServants_.remove(pb);
	}
    }

    public int[] push_suppliers() {
	int[] _ret = new int[pushProxies_.size()];
	Iterator _i = pushProxies_.keySet().iterator();
	int x=-1;
	while (_i.hasNext()) {
	    _ret[++x] = ((Integer)_i.next()).intValue();
	}
	return _ret;
    }

    public int[] pull_suppliers() {
	int[] _ret = new int[pullProxies_.size()];
	Iterator _i = pullProxies_.keySet().iterator();
	int x=-1;
	while (_i.hasNext()) {
	    _ret[++x] = ((Integer)_i.next()).intValue();
	}
	return _ret;
    }

    public ProxySupplier obtain_notification_push_supplier(ClientType clientType, 
							   IntHolder intHolder) throws AdminLimitExceeded {

	intHolder.value = getPushProxyId();
	Integer _key = new Integer(intHolder.value);
	ProxySupplier _pushSupplier = null;

	switch(clientType.value()) {
	case ClientType._ANY_EVENT: {
	
	    ProxyPushSupplierImpl _pushSupplierServant = new ProxyPushSupplierImpl(applicationContext_,
										   channelContext_,
										   this,
										   thisRef_);
	
	    pushServants_.put(_key, _pushSupplierServant);

	    ProxyPushSupplierPOATie _pushSupplierPOATie = new ProxyPushSupplierPOATie(_pushSupplierServant);
	    
	    _pushSupplier = _pushSupplierPOATie._this(getOrb());

	    break;
	}
	case ClientType._STRUCTURED_EVENT: {
	    StructuredProxyPushSupplierImpl _pushSupplierServant = 
		new StructuredProxyPushSupplierImpl(applicationContext_,
						    channelContext_,
						    this,
						    thisRef_,
						    _key);
	    _pushSupplierServant.setProxyType(ProxyType.PUSH_STRUCTURED);
	    pushServants_.put(_key, _pushSupplierServant);

	    StructuredProxyPushSupplierPOATie _pushSupplierPOATie = 
		new StructuredProxyPushSupplierPOATie(_pushSupplierServant);

	    _pushSupplier = _pushSupplierPOATie._this(getOrb());

	    break;
	}
	case ClientType._SEQUENCE_EVENT: {
	    SequenceProxyPushSupplierImpl _pushSupplierServant = 
		new SequenceProxyPushSupplierImpl(applicationContext_,
						  channelContext_,
						  this,
						  thisRef_,
						  _key);
	    _pushSupplierServant.setProxyType(ProxyType.PUSH_SEQUENCE);
	    pushServants_.put(_key, _pushSupplierServant);

	    SequenceProxyPushSupplierPOATie _pushSupplierPOATie = 
		new SequenceProxyPushSupplierPOATie(_pushSupplierServant);

	    _pushSupplier = _pushSupplierPOATie._this(getOrb());

	    break;
	}
	default:
	    throw new BAD_PARAM();
	}

	pushProxies_.put(_key, _pushSupplier);
	allProxies_.put(_key, ProxySupplierHelper.narrow(_pushSupplier));

	return _pushSupplier;
    }
    
    public ProxyPullSupplier obtain_pull_supplier() {
	ProxyPullSupplierImpl _servant = 
	    new ProxyPullSupplierImpl(applicationContext_, 
				      channelContext_, 
				      this, 
				      thisRef_);

	_servant.setFilterManager(FilterManager.EMPTY);
	eventStyleServants_.add(_servant);

	org.omg.CosEventChannelAdmin.ProxyPullSupplierPOATie _tie =
	    new org.omg.CosEventChannelAdmin.ProxyPullSupplierPOATie(_servant);

	ProxyPullSupplier _supplier = 
	    _tie._this(getOrb());

	return _supplier;
    }

    public ProxyPushSupplier obtain_push_supplier() {
	ProxyPushSupplierImpl _servant = 
	    new ProxyPushSupplierImpl(applicationContext_, 
				      channelContext_, 
				      this, 
				      thisRef_);

	_servant.setFilterManager(FilterManager.EMPTY);
	eventStyleServants_.add(_servant);

	org.omg.CosEventChannelAdmin.ProxyPushSupplierPOATie _tie =
	    new org.omg.CosEventChannelAdmin.ProxyPushSupplierPOATie(_servant);

	ProxyPushSupplier _supplier =
	    org.omg.CosEventChannelAdmin.ProxyPushSupplierHelper.narrow(_tie._this(getOrb()));

	return _supplier;
    }

    public List getSubsequentDestinations() {
	logger_.debug("getSubsequentDestinations()");

	List _l = new Vector();

	_l.addAll(pullServants_.values());
	_l.addAll(pushServants_.values());
	_l.addAll(eventStyleServants_);

	return _l;
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
}// ConsumerAdminImpl
