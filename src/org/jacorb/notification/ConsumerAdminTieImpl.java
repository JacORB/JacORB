 package org.jacorb.notification;

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

/*
 *        JacORB - a free Java ORB
 */

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

    ConsumerAdmin getConsumerAdmin() {
	if (thisRef_ == null) {
	    thisRef_ = new ConsumerAdminPOATie(this)._this(getOrb());
	}
	return thisRef_;
    }

    // Implementation of org.omg.CosNotifyComm.NotifySubscribeOperations

    /**
     * Describe <code>subscription_change</code> method here.
     *
     * @param eventType an <code>EventType[]</code> value
     * @param eventType an <code>EventType[]</code> value
     * @exception InvalidEventType if an error occurs
     */
    public void subscription_change(EventType[] eventType1, EventType[] eventType2) throws InvalidEventType {
    }
    
    // Implementation of org.omg.CosNotifyChannelAdmin.ConsumerAdminOperations

    /**
     * Describe <code>destroy</code> method here.
     *
     */
    public void destroy() {
	logger_.info("Destroy");
	dispose();
    }

    /**
     * Describe <code>get_proxy_supplier</code> method here.
     *
     * @param n an <code>int</code> value
     * @return a <code>ProxySupplier</code> value
     * @exception ProxyNotFound if an error occurs
     */
    public ProxySupplier get_proxy_supplier(int n) throws ProxyNotFound {
	ProxySupplier _ret = (ProxySupplier)allProxies_.get(new Integer(n));
	if (_ret == null) {
	    throw new ProxyNotFound();
	}
	return _ret;
    }

    /**
     * Describe <code>lifetime_filter</code> method here.
     *
     * @param mappingFilter a <code>MappingFilter</code> value
     */
    public void lifetime_filter(MappingFilter mappingFilter) {
    }

    /**
     * Describe <code>lifetime_filter</code> method here.
     *
     * @return a <code>MappingFilter</code> value
     */
    public MappingFilter lifetime_filter() {
	return null;
    }

    /**
     * Describe <code>priority_filter</code> method here.
     *
     * @return a <code>MappingFilter</code> value
     */
    public MappingFilter priority_filter() {
	return null;
    }

    /**
     * Describe <code>priority_filter</code> method here.
     *
     * @param mappingFilter a <code>MappingFilter</code> value
     */
    public void priority_filter(MappingFilter mappingFilter) {
    }

    /**
     * Describe <code>obtain_notification_pull_supplier</code> method here.
     *
     * @param clientType a <code>ClientType</code> value
     * @param intHolder an <code>IntHolder</code> value
     * @return a <code>ProxySupplier</code> value
     * @exception AdminLimitExceeded if an error occurs
     */
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
										   thisRef_);

	    _pullSupplierServant.setFilterMap(getChannelServant().getFilterMap(_pullSupplierServant));

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
						    thisRef_);

	    pullServants_.put(_key, _pullSupplierServant);

	    _pullSupplierServant.setFilterMap(getChannelServant().getFilterMap(_pullSupplierServant));

	    StructuredPullSupplierPOATie _pullSupplierPOATie = new StructuredPullSupplierPOATie(_pullSupplierServant);
	    _pullSupplier = ProxySupplierHelper.narrow(_pullSupplierPOATie._this(getOrb()));

	    break;
	}
	default:
	    throw new BAD_PARAM();
	}

	pullProxies_.put(_key, _pullSupplier);
	allProxies_.put(_key, ProxySupplierHelper.narrow(_pullSupplier));

	return _pullSupplier;
    }

    /**
     * Describe <code>push_suppliers</code> method here.
     *
     * @return an <code>int[]</code> value
     */
    public int[] push_suppliers() {
	int[] _ret = new int[pushProxies_.size()];
	Iterator _i = pushProxies_.keySet().iterator();
	int x=-1;
	while (_i.hasNext()) {
	    _ret[++x] = ((Integer)_i.next()).intValue();
	}
	return _ret;
    }

    /**
     * Describe <code>pull_suppliers</code> method here.
     *
     * @return an <code>int[]</code> value
     */
    public int[] pull_suppliers() {
	int[] _ret = new int[pullProxies_.size()];
	Iterator _i = pullProxies_.keySet().iterator();
	int x=-1;
	while (_i.hasNext()) {
	    _ret[++x] = ((Integer)_i.next()).intValue();
	}
	return _ret;
    }

    /**
     * Describe <code>obtain_notification_push_supplier</code> method here.
     *
     * @param clientType a <code>ClientType</code> value
     * @param intHolder an <code>IntHolder</code> value
     * @return a <code>ProxySupplier</code> value
     * @exception AdminLimitExceeded if an error occurs
     */
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

	    _pushSupplierServant.setFilterMap(getChannelServant().getFilterMap(_pushSupplierServant));

	    ProxyPushSupplierPOATie _pushSupplierPOATie = new ProxyPushSupplierPOATie(_pushSupplierServant);
	    
	    _pushSupplier = _pushSupplierPOATie._this(getOrb());

	    break;
	}
	case ClientType._STRUCTURED_EVENT: {
	    StructuredProxyPushSupplierImpl _pushSupplierServant = new StructuredProxyPushSupplierImpl(applicationContext_,
												       channelContext_,
												       this,
												       thisRef_);

	    pushServants_.put(_key, _pushSupplierServant);

	    _pushSupplierServant.setFilterMap(getChannelServant().getFilterMap(_pushSupplierServant));

	    StructuredProxyPushSupplierPOATie _pushSupplierPOATie = 
		new StructuredProxyPushSupplierPOATie(_pushSupplierServant);

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

    
    // Implementation of org.omg.CosEventChannelAdmin.ConsumerAdminOperations

    /**
     * Describe <code>obtain_pull_supplier</code> method here.
     *
     * @return a <code>ProxyPullSupplier</code> value
     */
    public org.omg.CosEventChannelAdmin.ProxyPullSupplier obtain_pull_supplier() {
	ProxyPullSupplierImpl p = new ProxyPullSupplierImpl(applicationContext_, 
							    channelContext_, 
							    this, 
							    thisRef_);
	p.setFilterMap(Collections.EMPTY_MAP);
	eventStyleServants_.add(p);
	org.omg.CosEventChannelAdmin.ProxyPullSupplier _supplier = 
	    org.omg.CosEventChannelAdmin.ProxyPullSupplierHelper.narrow(new org.omg.CosEventChannelAdmin.ProxyPullSupplierPOATie(p)._this(getOrb()));

	return _supplier;
    }

    /**
     * Describe <code>obtain_push_supplier</code> method here.
     *
     * @return a <code>ProxyPushSupplier</code> value
     */
    public org.omg.CosEventChannelAdmin.ProxyPushSupplier obtain_push_supplier() {
	ProxyPushSupplierImpl p = new ProxyPushSupplierImpl(applicationContext_, 
							    channelContext_, 
							    this, 
							    thisRef_);
	p.setFilterMap(Collections.EMPTY_MAP);
	eventStyleServants_.add(p);
	org.omg.CosEventChannelAdmin.ProxyPushSupplier _supplier =
	    org.omg.CosEventChannelAdmin.ProxyPushSupplierHelper.narrow(new org.omg.CosEventChannelAdmin.ProxyPushSupplierPOATie(p)._this(getOrb()));

	return _supplier;
    }
    
    public void transmit_event(NotificationEvent event) {
	Iterator _proxies = pullServants_.values().iterator();
	TransmitEventCapable _forwarder;

	while (_proxies.hasNext()) {
	    _forwarder = (TransmitEventCapable)_proxies.next();
	    _forwarder.transmit_event(event);
	}

	_proxies = pushServants_.values().iterator();
	while (_proxies.hasNext()) {
	    _forwarder = (TransmitEventCapable)_proxies.next();
	    _forwarder.transmit_event(event);
	}

	_proxies = eventStyleServants_.iterator();
	while (_proxies.hasNext()) {
	    _forwarder = (TransmitEventCapable)_proxies.next();
	    _forwarder.transmit_event(event);
	}
    }

    public List getSubsequentDestinations() {
	logger_.debug("getSubsequentDestinations()");

	List _l = new Vector();

	_l.addAll(pullServants_.values());
	_l.addAll(pushServants_.values());
	_l.addAll(eventStyleServants_);

	return _l;
    }

    public TransmitEventCapable getEventSink() {
	return null;
    }

    public void dispose() {
	super.dispose();
	Iterator _i = eventStyleServants_.iterator();
	while(_i.hasNext()) {
	    ((Disposable)_i.next()).dispose();
	}
    }
}// ConsumerAdminImpl
