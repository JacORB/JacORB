package org.jacorb.notification;

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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.jacorb.notification.interfaces.AdminEvent;
import org.jacorb.notification.interfaces.AdminEventListener;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.ProxyCreationRequestEvent;
import org.jacorb.notification.interfaces.ProxyCreationRequestEventListener;
import org.jacorb.notification.interfaces.ProxyEvent;
import org.jacorb.notification.interfaces.ProxyEventListener;
import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.MaxConsumers;
import org.omg.CosNotification.MaxSuppliers;
import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.UnsupportedAdmin;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyChannelAdmin.EventChannelPOA;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.PortableServer.POA;
import org.omg.CosNotifyChannelAdmin.EventChannelHelper;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.omg.PortableServer.POAPackage.ObjectNotActive;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class EventChannelImpl extends EventChannelPOA implements Disposable
{
    private Logger logger_ = Hierarchy.getDefaultHierarchy().getLoggerFor( getClass().getName() );;

    private ORB myOrb_ = null;
    private POA myPoa_ = null;

    private String ior_;

    private int myKey_;

    private EventChannel thisRef_;
    private FilterFactory defaultFilterFactory_;
    private EventChannelFactoryImpl myFactoryServant_;
    private EventChannelFactory myFactory_;

    private List allConsumerAdmins_ = new Vector();
    private List subsequentDestinations_;

    private Object refreshAdminListLock_ = new Object();

    private boolean adminListDirty_ = true;

    private Map consumerAdminServants_;
    private Map supplierAdminServants_;

    private ConsumerAdmin defaultConsumerAdmin_;
    private SupplierAdmin defaultSupplierAdmin_;

    private int consumerIdPool_ = 0;
    private int numberOfConsumers_;
    private int numberOfSuppliers_;
    private int maxNumberOfConsumers_ = Integer.MAX_VALUE;
    private int maxNumberOfSuppliers_ = Integer.MAX_VALUE;

    protected ApplicationContext applicationContext_;
    protected ChannelContext channelContext_;

    protected ConsumerAdminTieImpl defaultConsumerAdminImpl_;
    protected SupplierAdminTieImpl defaultSupplierAdminImpl_;
    protected PropertyManager adminProperties_;
    protected PropertyManager qosProperties_;

    protected List listAdminEventListeners_ = new ArrayList();

    ProxyCreationRequestEventListener getCreateProxyEventListener()
    {
        return proxyCreationEventListener_;
    }

    ProxyEventListener getRemoveProxyConsumerListener()
    {
        return proxyConsumerDisposedListener_;
    }

    ProxyEventListener getRemoveProxySupplierListener()
    {
        return proxySupplierDisposedListener_;
    }

    /**
     * ProxyCreationRequestEventListener as an anonymous inner class.
     */
    private ProxyCreationRequestEventListener proxyCreationEventListener_ =
        new ProxyCreationRequestEventListener()
        {
            public void actionProxyCreationRequest( ProxyCreationRequestEvent event )
            throws AdminLimitExceeded
            {

                if ( event.getSource() instanceof ConsumerAdminTieImpl )
                {
                    addConsumer();
                }
                else if ( event.getSource() instanceof SupplierAdminTieImpl )
                {
                    addSupplier();
                }
                else
                {
                    throw new RuntimeException();
                }
            }
        };

    /**
     * ProxyDisposedEventListener as an anonymous inner class.
     */
    private ProxyEventListener proxyConsumerDisposedListener_ =
        new ProxyEventListener()
        {
	    public void actionProxyCreated( ProxyEvent e) {
		// No Op
	    }

            public void actionProxyDisposed( ProxyEvent e )
            {
                --numberOfConsumers_;
            }
        };

    /**
     * ProxyDisposedEventListener as an anonymous inner class.
     */
    private ProxyEventListener proxySupplierDisposedListener_ =
        new ProxyEventListener()
        {

	    public void actionProxyCreated(ProxyEvent e) {
		// No OP
	    }

            public void actionProxyDisposed( ProxyEvent e )
            {
                --numberOfSuppliers_;
            }
        };


    /**
     * Callback to help keep track of the number of Consumers.
     *
     * @exception AdminLimitExceeded if creation of another Consumer
     * is prohibited.
     */

    synchronized void addConsumer()
    throws AdminLimitExceeded
    {
        if ( numberOfConsumers_ < maxNumberOfConsumers_ )
        {
            numberOfConsumers_++;
        }
        else
        {
            throw new AdminLimitExceeded();
        }
    }


    /**
     * Callback to keep track of the number of Suppliers
     *
     * @exception AdminLimitExceeded if no Suppliers may be created
     */
    synchronized void addSupplier()
    throws AdminLimitExceeded
    {
        if ( numberOfSuppliers_ < maxNumberOfSuppliers_ )
        {
            numberOfSuppliers_++;
        }
        else
        {
            throw new AdminLimitExceeded();
        }
    }

    int getAdminId()
    {
        return ++consumerIdPool_;
    }

    private void fireNewAdmin(AdminBase b) {
	Iterator i = listAdminEventListeners_.iterator();
	AdminEvent e = new AdminEvent(b);
	while (i.hasNext()) {
	    ((AdminEventListener)i.next()).actionAdminCreated(e);
	}
    }

    private void fireDestroyAdmin(AdminBase b) {
	Iterator i = listAdminEventListeners_.iterator();
	AdminEvent e = new AdminEvent(b);
	while (i.hasNext()) {
	    ((AdminEventListener)i.next()).actionAdminDestroyed(e);
	}
    }

    public void addAdminEventListener(AdminEventListener l) {
	listAdminEventListeners_.add(l);
    }

    public void removeAdminEventListener(AdminEventListener l) {
	listAdminEventListeners_.remove(l);
    }

    ConsumerAdminTieImpl getDefaultConsumerAdminServant()
    {
        if ( defaultConsumerAdminImpl_ == null )
        {
            synchronized ( this )
            {
                if ( defaultConsumerAdminImpl_ == null )
                {
                    defaultConsumerAdminImpl_ =
                        new ConsumerAdminTieImpl( applicationContext_,
                                                  channelContext_,
                                                  adminProperties_,
                                                  qosProperties_ );

		    fireNewAdmin(defaultConsumerAdminImpl_);

		    synchronized(refreshAdminListLock_) {
			allConsumerAdmins_.add( defaultConsumerAdminImpl_ );

			adminListDirty_ = true;
		    }
                }
            }
        }

        return defaultConsumerAdminImpl_;
    }

    SupplierAdminTieImpl getDefaultSupplierAdminServant()
    {
        if ( defaultSupplierAdminImpl_ == null )
        {
            synchronized ( this )
            {
                if ( defaultSupplierAdminImpl_ == null )
                {
                    defaultSupplierAdminImpl_ =
                        new SupplierAdminTieImpl( applicationContext_,
                                                  channelContext_,
                                                  adminProperties_,
                                                  qosProperties_ );

		    fireNewAdmin(defaultSupplierAdminImpl_);

                    if ( logger_.isDebugEnabled() )
                    {
                        logger_.debug( "getDefaultSupplierAdminServant(): "
                                       + defaultSupplierAdminImpl_ );
                    }
                }
            }
        }

        return defaultSupplierAdminImpl_;
    }

    /**
     * The MyFactory attribute is a readonly attribute that maintains
     * the object reference of the event channel factory, which
     * created a given Notification Service EventChannel instance.
     */
    public EventChannelFactory MyFactory()
    {
        return myFactory_;
    }

    /**
     * The default_consumer_admin attribute is a readonly attribute
     * that maintains a reference to the default ConsumerAdmin
     * instance associated with the target EventChannel instance. Each
     * EventChannel instance has an associated default ConsumerAdmin
     * instance, which exists upon creation of the channel and is
     * assigned the unique identifier of zero. Subsequently, clients
     * can create additional Event Service style ConsumerAdmin
     * instances by invoking the inherited  operation, and additional
     * Notification Service style ConsumerAdmin instances by invoking
     * the new_for_consumers operation defined by the EventChannel
     * interface.
     */
    public ConsumerAdmin default_consumer_admin()
    {
        if ( defaultConsumerAdmin_ == null )
        {
            synchronized ( this )
            {
                if ( defaultConsumerAdmin_ == null )
                {

                    defaultConsumerAdmin_ =
                        getDefaultConsumerAdminServant().getConsumerAdmin();

                }
            }
        }

        return defaultConsumerAdmin_;
    }

    /**
     * The default_supplier_admin attribute is a readonly attribute
     * that maintains a reference to the default SupplierAdmin
     * instance associated with the target EventChannel instance. Each
     * EventChannel instance has an associated default SupplierAdmin
     * instance, which exists upon creation of the channel and is
     * assigned the unique identifier of zero. Subsequently, clients
     * can create additional Event Service style SupplierAdmin
     * instances by invoking the inherited for_suppliers operation,
     * and additional Notification Service style SupplierAdmin
     * instances by invoking the new_for_suppliers operation defined
     * by the EventChannel interface. 
     */
    public SupplierAdmin default_supplier_admin()
    {
        if ( defaultSupplierAdmin_ == null )
        {
            synchronized ( this )
            {
                if ( defaultSupplierAdmin_ == null )
                {

                    defaultSupplierAdmin_ =
                        getDefaultSupplierAdminServant().getSupplierAdmin();

                }
            }
        }
        return defaultSupplierAdmin_;
    }

    /**
     * The default_filter_factory attribute is a readonly attribute
     * that maintains an object reference to the default factory to be
     * used by the EventChannel instance with which it is associated for
     * creating filter objects. If the target channel does not support
     * a default filter factory, the attribute will maintain the value
     * of OBJECT_NIL.
     */
    public FilterFactory default_filter_factory()
    {
        return defaultFilterFactory_;
    }

    /**
     * The new_for_consumers operation is invoked to create a new
     * Notification Service style ConsumerAdmin instance. The
     * operation accepts as an input parameter a boolean flag, which
     * indicates whether AND or OR semantics will be used when
     * combining the filter objects associated with the newly created
     * ConsumerAdmin instance with those associated with a supplier
     * proxy, which was created by the ConsumerAdmin during the
     * evaluation of each event against a set of filter objects. The
     * new instance is assigned a unique identifier by the target
     * EventChannel instance that is unique among all ConsumerAdmin
     * instances currently associated with the channel. Upon
     * completion, the operation returns the reference to the new
     * ConsumerAdmin instance as the result of the operation, and the
     * unique identifier assigned to the new ConsumerAdmin instance as
     * the output parameter. 
     */
    public ConsumerAdmin new_for_consumers( InterFilterGroupOperator filterGroupOperator,
                                            IntHolder intHolder )
    {
	ConsumerAdminTieImpl _consumerAdminTieImpl = 
	    new_for_consumers_servant( filterGroupOperator, intHolder );

	fireNewAdmin(_consumerAdminTieImpl);
	
	return _consumerAdminTieImpl.getConsumerAdmin();
    }

    ConsumerAdminTieImpl new_for_consumers_servant( InterFilterGroupOperator filterGroupOperator,
            IntHolder intHolder )
    {

        intHolder.value = getAdminId();

        PropertyManager _adminProperties = ( PropertyManager ) adminProperties_.clone();
        PropertyManager _qosProperties = ( PropertyManager ) qosProperties_.clone();

        ConsumerAdminTieImpl _consumerAdminServant = 
	    new ConsumerAdminTieImpl( applicationContext_,
				      channelContext_,
				      _adminProperties,
				      _qosProperties,
				      intHolder.value,
				      filterGroupOperator );

        Integer _key = new Integer( intHolder.value );

        _consumerAdminServant.addProxyCreationEventListener( getCreateProxyEventListener() );

        consumerAdminServants_.put( _key, _consumerAdminServant );

        allConsumerAdmins_.add( _consumerAdminServant );

	adminListDirty_ = true;

        return _consumerAdminServant;
    }

    public SupplierAdmin new_for_suppliers( InterFilterGroupOperator filterGroupOperator,
                                            IntHolder intHolder )
    {


        SupplierAdminTieImpl _supplierAdmin =
            new_for_suppliers_servant( filterGroupOperator, intHolder );

	fireNewAdmin(_supplierAdmin);

        return _supplierAdmin.getSupplierAdmin();

    }

    SupplierAdminTieImpl new_for_suppliers_servant( InterFilterGroupOperator filterGroupOperator,
            IntHolder intHolder )
    {

        intHolder.value = getAdminId();
        Integer _key = new Integer( intHolder.value );


        PropertyManager _adminProperties = ( PropertyManager ) adminProperties_.clone();
        PropertyManager _qosProperties = ( PropertyManager ) qosProperties_.clone();

        SupplierAdminTieImpl _supplierAdminServant = 
	    new SupplierAdminTieImpl( applicationContext_,
				      channelContext_,
				      _adminProperties,
				      _qosProperties,
				      intHolder.value,
				      filterGroupOperator );

        _supplierAdminServant.addProxyCreationEventListener( getCreateProxyEventListener() );

        supplierAdminServants_.put( _key, _supplierAdminServant );

        return _supplierAdminServant;
    }

    public void removeAdmin( AdminBase admin )
    {
        Integer _key = admin.getKey();

        if ( _key != null )
        {
            logger_.debug( "removeAdmin(" + _key + ")" );
            logger_.debug( "admin: " + admin );

            if ( admin instanceof SupplierAdminTieImpl )
            {
                supplierAdminServants_.remove( _key );
            }
            else if ( admin instanceof ConsumerAdminTieImpl )
            {
                consumerAdminServants_.remove( _key );
            }
        }

        if ( admin instanceof ConsumerAdminTieImpl 
	     && allConsumerAdmins_.contains( admin ) )
        {
            allConsumerAdmins_.remove( admin );
	    adminListDirty_ = true;
        }
	
	fireDestroyAdmin(admin);
    }

    public ConsumerAdmin get_consumeradmin( int identifier )
    {
        if ( identifier == 0 )
        {
            return default_consumer_admin();
        }

        Integer _key = new Integer( identifier );
        return ( ( ConsumerAdminTieImpl ) consumerAdminServants_.get( _key ) ).getConsumerAdmin();
    }

    public SupplierAdmin get_supplieradmin( int identifier )
    {
        if ( identifier == 0 )
        {
            return default_supplier_admin();
        }

        Integer _key = new Integer( identifier );
        return ( ( SupplierAdminTieImpl ) supplierAdminServants_.get( _key ) ).getSupplierAdmin();
    }

    public int[] get_all_consumeradmins()
    {
        return null;
    }

    public int[] get_all_supplieradmins()
    {
        return null;
    }

    public Property[] get_admin()
    {
        return adminProperties_.toArray();
    }

    public Property[] get_qos()
    {
        return qosProperties_.toArray();
    }

    public void set_qos( Property[] qos ) throws UnsupportedQoS
        {}

    public void validate_qos( Property[] qos,
                              NamedPropertyRangeSeqHolder namedPropertySeqHolder ) throws UnsupportedQoS
        {}

    public void set_admin( Property[] adminProps ) throws UnsupportedAdmin
        {}

    /**
     * EventChannel constructor.
     */
    EventChannelImpl( int key,  
		      ApplicationContext appContext,
                      ChannelContext channelContext,
                      Map qosProperties,
                      Map adminProperties ) throws ServantAlreadyActive,
						   WrongPolicy,
						   ObjectNotActive
    {
	super();

	myKey_ = key;

        if ( adminProperties.containsKey( MaxConsumers.value ) )
        {
            Any _maxConsumers = ( Any ) adminProperties.get( MaxConsumers.value );
            maxNumberOfConsumers_ = _maxConsumers.extract_long();
        }

        if ( adminProperties.containsKey( MaxSuppliers.value ) )
        {
            Any _maxSuppliers = ( Any ) adminProperties.get( MaxSuppliers.value );
            maxNumberOfSuppliers_ = _maxSuppliers.extract_long();
        }

        channelContext_ = channelContext;
        applicationContext_ = appContext;

        adminProperties_ = new PropertyManager( applicationContext_, adminProperties );
        qosProperties_ = new PropertyManager( applicationContext_, qosProperties );

        adminProperties_.setDefaultManager( applicationContext_.getDefaultAdminProperties() );
        qosProperties_.setDefaultManager( applicationContext_.getDefaultQoSProperties() );

        myOrb_ = appContext.getOrb();
        myPoa_ = appContext.getPoa();
        myFactory_ = channelContext.getEventChannelFactory();
        myFactoryServant_ = channelContext.getEventChannelFactoryServant();
        defaultFilterFactory_ = channelContext.getDefaultFilterFactory();

        channelContext.setEventChannelServant( this );

	byte[] oid = myPoa_.activate_object(this);	
	thisRef_ = EventChannelHelper.narrow(myPoa_.id_to_reference(oid));
	ior_ = myOrb_.object_to_string(myPoa_.id_to_reference(oid));
        channelContext_.setEventChannel( thisRef_ );

        channelContext_.setProxySupplierDisposedEventListener( proxySupplierDisposedListener_ );
        channelContext_.setProxyConsumerDisposedEventListener( proxyConsumerDisposedListener_ );

        supplierAdminServants_ = new Hashtable();
        consumerAdminServants_ = new Hashtable();

        channelContext.setTaskProcessor( appContext.getTaskProcessor() );
    }

    /**
     * send the ConsumerAdmin vectors off for destrcution.
     */
    private void consumerAdminDestroy()
    {}

    /**
     * send the SupplierAdmin vectors off for destrcution.
     */
    private void supplierAdminDestroy()
    {}

    /**
     * Iterate a list and send the servant off to be destroyed.
     */
    private void releaseList( Vector list )
    {
        for ( Enumeration _e = list.elements(); _e.hasMoreElements(); )
        {
            org.omg.PortableServer.Servant _servant =
                ( org.omg.PortableServer.Servant ) _e.nextElement();
            releaseServant( _servant );
        }
    }

    /**
     * Destroy / deactivate the servant.
     */
    private void releaseServant( org.omg.PortableServer.Servant servant )
    {
        try
        {
            servant._poa().deactivate_object( servant._object_id() );
        }
        catch ( org.omg.PortableServer.POAPackage.WrongPolicy wpEx )
        {
            wpEx.printStackTrace();
        }
        catch ( org.omg.PortableServer.POAPackage.ObjectNotActive onaEx )
        {
            onaEx.printStackTrace();
        }
    }

    /**
     * Destroy all objects which are managed by the POA.
     */
    public void destroy()
    {
        dispose();
    }

    public void dispose()
    {
	myFactoryServant_.removeEventChannelServant(getKey());

        logger_.info( "dispose default consumer admin" );
        if ( defaultConsumerAdmin_ != null )
        {
            defaultConsumerAdminImpl_.dispose();
            defaultConsumerAdminImpl_ = null;
            defaultConsumerAdmin_ = null;
        }


        logger_.info( "dispose default supplier admin" );
        if ( defaultSupplierAdmin_ != null )
        {
            defaultSupplierAdminImpl_.dispose();
            defaultSupplierAdminImpl_ = null;
            defaultSupplierAdmin_ = null;
        }

        Iterator _i = consumerAdminServants_.values().iterator();

        while ( _i.hasNext() )
        {
	    Disposable _d = (Disposable) _i.next();
	    _i.remove();
	    _d.dispose();
        }

        _i = supplierAdminServants_.values().iterator();

        while ( _i.hasNext() )
        {
            Disposable _d = ( Disposable ) _i.next();
            _i.remove();
            _d.dispose();
        }

        consumerAdminDestroy();
        supplierAdminDestroy();

	listAdminEventListeners_.clear();
        //        releaseServant(this);
    }


    /**
     * Return the consumerAdmin interface (event style)
     */
    public org.omg.CosEventChannelAdmin.ConsumerAdmin for_consumers()
    {
        try
        {
            return org.omg.CosEventChannelAdmin.ConsumerAdminHelper.narrow( default_consumer_admin() );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return the supplierAdmin interface (event style)
     */
    public org.omg.CosEventChannelAdmin.SupplierAdmin for_suppliers()
    {
        try
        {
            return org.omg.CosEventChannelAdmin.SupplierAdminHelper.narrow( default_supplier_admin() );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Override this method from the Servant baseclass.  Fintan Bolton
     * in his book "Pure CORBA" suggests that you override this method to
     * avoid the risk that a servant object (like this one) could be
     * activated by the <b>wrong</b> POA object.
     */
    public POA _default_POA()
    {
	logger_.debug("DefaultPOA: " + myPoa_.the_name());
        return myPoa_;
    }

    private void refreshConsumerAdminList() {
	logger_.debug("refresh ?");

	if (adminListDirty_) {
	    synchronized(refreshAdminListLock_) {
		if (adminListDirty_) {
		    
		    logger_.debug("yes");

		    List _l = new Vector();

		    checkAddFilterStage(allConsumerAdmins_.iterator(), _l);

		    subsequentDestinations_ = _l;

		    adminListDirty_ = false;
		}
	    }
	}
    }


    List getAllConsumerAdmins()
    {
	logger_.debug("getAllConsumerAdmins()");

	refreshConsumerAdminList();

        return subsequentDestinations_;
    }

    static void checkAddFilterStage( Iterator in, List out )
    {
        while ( in.hasNext() )
        {
            Object _next = in.next();
            FilterStage _node = null;

            if ( _next instanceof FilterStage )
            {
                _node = ( FilterStage ) _next;
            }
            else if ( _next instanceof Map.Entry )
            {
                _node = ( FilterStage ) ( ( Map.Entry ) _next ).getValue();
            }
            else
            {
                throw new RuntimeException( "unexpected: " + _node.getClass().getName() );
            }

            if ( !_node.isDisposed() )
            {
                out.add( _node );
            }
            else
            {
                in.remove();
            }
        }
    }

    public int getKey() {
	return myKey_;
    }

    EventChannel getEventChannel() {
	if (thisRef_ == null) {
	    synchronized(this) {
		if (thisRef_ == null) {
		    thisRef_ = _this( applicationContext_.getOrb() );
		}
	    }
	}
	return thisRef_;
    }

    public String getIOR() {
	return ior_;
    }
}
