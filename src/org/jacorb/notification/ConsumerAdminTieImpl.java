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
import java.util.List;
import java.util.Vector;

import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.EventConsumer;
import org.jacorb.notification.interfaces.ProxyEvent;
import org.jacorb.notification.interfaces.ProxyEventListener;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.IntHolder;
import org.omg.CosEventChannelAdmin.ProxyPullSupplier;
import org.omg.CosEventChannelAdmin.ProxyPushSupplier;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.ConsumerAdminOperations;
import org.omg.CosNotifyChannelAdmin.ConsumerAdminPOATie;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosNotifyChannelAdmin.ProxyNotFound;
import org.omg.CosNotifyChannelAdmin.ProxySupplier;
import org.omg.CosNotifyChannelAdmin.ProxySupplierHelper;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.PortableServer.Servant;
import org.omg.CosNotification.UnsupportedQoS;

/**
 * ConsumerAdminImpl.java
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ConsumerAdminTieImpl
            extends AbstractAdmin
            implements ConsumerAdminOperations,
            Disposable,
            ProxyEventListener
{

    List eventStyleServants_ = new Vector();
    ConsumerAdmin thisRef_;
    ConsumerAdminPOATie thisServant_;
    private Object thisServantLock_ = new Object();

    List subsequentDestinations_;
    boolean proxyListDirty_ = true;

    public ConsumerAdminTieImpl( ApplicationContext appContext,
                                 ChannelContext channelContext,
                                 PropertyManager adminProperties,
                                 PropertyManager qosProperties )
    {
        super( appContext,
               channelContext,
               adminProperties,
               qosProperties );
    }

    public ConsumerAdminTieImpl( ApplicationContext appContext,
                                 ChannelContext channelContext,
                                 PropertyManager adminProperties,
                                 PropertyManager qosProperties,
                                 int myId,
                                 InterFilterGroupOperator filterGroupOperator )
    {
        super( appContext,
               channelContext,
               adminProperties,
               qosProperties,
               myId,
               filterGroupOperator );
    }


    public Servant getServant()
    {
        if ( thisServant_ == null )
        {
            synchronized ( thisServantLock_ )
            {
                if ( thisServant_ == null )
                {
                    thisServant_ = new ConsumerAdminPOATie( this );
                }
            }
        }

        return thisServant_;
    }

    ConsumerAdmin getConsumerAdmin()
    {
        if ( thisRef_ == null )
        {
            synchronized ( this )
            {
                if ( thisRef_ == null )
                    {
                        // sideeffect of getServant() is that
                        // thisServant_ gets set.
                        getServant();
                        thisRef_ = thisServant_._this( getOrb() );
                    }
            }
        }

        return thisRef_;
    }

    public org.omg.CORBA.Object getThisRef()
    {
        return getConsumerAdmin();
    }

    public void subscription_change( EventType[] eventType1,
                                     EventType[] eventType2 ) throws InvalidEventType
        {}


    public ProxySupplier get_proxy_supplier( int n ) throws ProxyNotFound
    {
        refreshProxyList();

        ProxySupplier _ret = ( ProxySupplier ) allProxies_.get( new Integer( n ) );

        if ( _ret == null )
        {
            throw new ProxyNotFound();
        }

        return _ret;
    }

    public void lifetime_filter( MappingFilter mappingFilter )
    {}

    public MappingFilter lifetime_filter()
    {
        return null;
    }

    public MappingFilter priority_filter()
    {
        return null;
    }

    public void priority_filter( MappingFilter mappingFilter )
    {}

    public ProxySupplier obtain_notification_pull_supplier( ClientType clientType,
                                                            IntHolder intHolder )
        throws AdminLimitExceeded
    {
        try {
            AbstractProxy _servant =
                obtain_notification_pull_supplier_servant( clientType, intHolder );

            Integer _key = _servant.getKey();

            ProxySupplier _proxySupplier =
                ProxySupplierHelper.narrow( _servant.getServant()._this_object( getOrb() ) );

            allProxies_.put( _key, _proxySupplier );

            return _proxySupplier;
        } catch (UnsupportedQoS e) {
            logger_.fatalError("Could not create pull supplier", e);
            throw new RuntimeException();
        }
    }

    public AbstractProxy obtain_notification_pull_supplier_servant( ClientType clientType,
                                                                IntHolder intHolder )
        throws AdminLimitExceeded,
               UnsupportedQoS
    {
        // may throw AdminLimitExceeded
        fireCreateProxyRequestEvent();

        intHolder.value = getPullProxyId();
        Integer _key = new Integer( intHolder.value );

        AbstractProxy _servant;
        ProxySupplier _pullSupplier = null;

        PropertyManager _qosProperties = ( PropertyManager ) qosProperties_.clone();
        PropertyManager _adminProperties = ( PropertyManager ) adminProperties_.clone();

        switch ( clientType.value() )
        {

        case ClientType._ANY_EVENT:
            _servant = new ProxyPullSupplierImpl( this,
                                                  applicationContext_,
                                                  channelContext_,
                                                  _adminProperties,
                                                  _qosProperties,
                                                  _key );
            break;

        case ClientType._STRUCTURED_EVENT:
            _servant =
                new StructuredProxyPullSupplierImpl( this,
                                                     applicationContext_,
                                                     channelContext_,
                                                     _adminProperties,
                                                     _qosProperties,
                                                     _key );
            break;

        case ClientType._SEQUENCE_EVENT:
            _servant =
                new SequenceProxyPullSupplierImpl( this,
                                                   applicationContext_,
                                                   channelContext_,
                                                   _adminProperties,
                                                   _qosProperties,
                                                   _key );

            break;

        default:
            throw new BAD_PARAM();
        }

        if ( filterGroupOperator_.value() == InterFilterGroupOperator._OR_OP )
        {
            _servant.setOrSemantic( true );
        }

        pullServants_.put( _key, _servant );

        _servant.addProxyDisposedEventListener( this );

        if ( channelContext_.getRemoveProxySupplierListener() != null )
        {
            _servant.addProxyDisposedEventListener( channelContext_.getRemoveProxySupplierListener() );
        }

        proxyListDirty_ = true;

        return _servant;
    }

    /**
     *
     */
    public void remove
        ( AbstractProxy pb )
    {
        super.remove( pb );

        Integer _key = pb.getKey();

        if ( _key != null )
        {
            allProxies_.remove( _key );

            if ( pb instanceof StructuredProxyPullSupplierImpl ||
                    pb instanceof ProxyPullSupplierImpl ||
                    pb instanceof SequenceProxyPullSupplierImpl )
            {

                pullServants_.remove( _key );

            }
            else if ( pb instanceof StructuredProxyPushSupplierImpl ||
                      pb instanceof ProxyPushSupplierImpl ||
                      pb instanceof SequenceProxyPushSupplierImpl )
            {

                pushServants_.remove( _key );
            }
        }
        else
        {
            eventStyleServants_.remove( pb );
        }
    }

    public int[] push_suppliers()
    {
        refreshProxyList();

        int[] _ret = new int[ pushServants_.size() ];
        Iterator _i = pushServants_.keySet().iterator();
        int x = -1;

        while ( _i.hasNext() )
        {
            _ret[ ++x ] = ( ( Integer ) _i.next() ).intValue();
        }

        return _ret;
    }

    public int[] pull_suppliers()
    {
        refreshProxyList();

        int[] _ret = new int[ pullServants_.size() ];
        Iterator _i = pullServants_.keySet().iterator();
        int x = -1;

        while ( _i.hasNext() )
        {
            _ret[ ++x ] = ( ( Integer ) _i.next() ).intValue();
        }

        return _ret;
    }

    public ProxySupplier obtain_notification_push_supplier( ClientType clientType,
                                                            IntHolder intHolder )
        throws AdminLimitExceeded
    {

        try {
            AbstractProxy _servant = obtain_notification_push_supplier_servant( clientType,
                                                                        intHolder );

            Integer _key = _servant.getKey();

            if (logger_.isInfoEnabled()) {
                logger_.info("created ProxyPushSupplier with ID: " + _key);
            }

            ProxySupplier _proxySupplier =
                ProxySupplierHelper.narrow( _servant.getServant()._this_object( getOrb() ) );

            allProxies_.put( _key, _proxySupplier );

            return _proxySupplier;
        } catch (UnsupportedQoS e) {
            logger_.fatalError("could not create push_supplier", e);
            throw new RuntimeException();
        }
    }

    public AbstractProxy obtain_notification_push_supplier_servant( ClientType clientType,
                                                                IntHolder intHolder )
        throws AdminLimitExceeded,
               UnsupportedQoS
    {

        // may throw exception if admin limit is exceeded
        fireCreateProxyRequestEvent();

        intHolder.value = getPushProxyId();

        Integer _key = new Integer( intHolder.value );
        AbstractProxy _servantImpl;

        PropertyManager _qosProperties = ( PropertyManager ) qosProperties_.clone();
        PropertyManager _adminProperties = ( PropertyManager ) adminProperties_.clone();

        switch ( clientType.value() )
        {

        case ClientType._ANY_EVENT:
            _servantImpl = new ProxyPushSupplierImpl( this,
                                                      applicationContext_,
                                                      channelContext_,
                                                      _adminProperties,
                                                      _qosProperties,
                                                      _key );
            break;

        case ClientType._STRUCTURED_EVENT:
            _servantImpl = new StructuredProxyPushSupplierImpl( this,
                           applicationContext_,
                           channelContext_,
                           _adminProperties,
                           _qosProperties,
                           _key );
            break;

        case ClientType._SEQUENCE_EVENT:
            _servantImpl =
                new SequenceProxyPushSupplierImpl( this,
                                                   applicationContext_,
                                                   channelContext_,
                                                   _adminProperties,
                                                   _qosProperties,
                                                   _key );
            break;

        default:
            throw new BAD_PARAM();
        }

        pushServants_.put( _key, _servantImpl );

        if ( filterGroupOperator_.value() == InterFilterGroupOperator._OR_OP )
        {
            _servantImpl.setOrSemantic( true );
        }

        _servantImpl.addProxyDisposedEventListener( this );
        _servantImpl.addProxyDisposedEventListener( channelContext_.getRemoveProxySupplierListener() );

        proxyListDirty_ = true;

        return _servantImpl;
    }

    public ProxyPullSupplier obtain_pull_supplier()
    {
        try {
            ProxyPullSupplierImpl _servant =
                new ProxyPullSupplierImpl( this,
                                           applicationContext_,
                                           channelContext_,
                                           ( PropertyManager ) adminProperties_.clone(),
                                           ( PropertyManager ) qosProperties_.clone() );

            _servant.addProxyDisposedEventListener( this );
            // _servant.addProxyDisposedEventListener(channelContext_.getRemoveProxySupplierListener());

            _servant.setFilterManager( FilterManager.EMPTY );
            eventStyleServants_.add( _servant );

            Servant _tie = new org.omg.CosEventChannelAdmin.ProxyPullSupplierPOATie( _servant );
            _servant.setServant( _tie );

            ProxyPullSupplier _supplier =
                org.omg.CosEventChannelAdmin.ProxyPullSupplierHelper.narrow( _tie._this_object( getOrb() ) );

            //servantCache_.put(_servant, _tie);
            proxyListDirty_ = true;

            return _supplier;
        } catch (UnsupportedQoS e) {
            logger_.fatalError("Could not create PullSupplier", e);
            throw new RuntimeException();
        }
    }

    public ProxyPushSupplier obtain_push_supplier()
    {

        try {
        ProxyPushSupplierImpl _servant =
            new ProxyPushSupplierImpl( this,
                                       applicationContext_,
                                       channelContext_,
                                       ( PropertyManager ) adminProperties_.clone(),
                                       ( PropertyManager ) qosProperties_.clone() );

        _servant.addProxyDisposedEventListener( this );
        // _servant.addProxyDisposedEventListener(channelContext_.getRemoveProxySupplierListener());

        _servant.setFilterManager( FilterManager.EMPTY );
        eventStyleServants_.add( _servant );

        Servant _tie = new org.omg.CosEventChannelAdmin.ProxyPushSupplierPOATie( _servant );
        _servant.setServant( _tie );

        ProxyPushSupplier _supplier =
            org.omg.CosEventChannelAdmin.ProxyPushSupplierHelper.narrow( _tie._this_object( getOrb() ) );

        // servantCache_.put(_servant, _tie);
        proxyListDirty_ = true;

        return _supplier;
        } catch (UnsupportedQoS e) {
            logger_.fatalError("Could not create ProxyPushSupplier", e);
            throw new RuntimeException();
        }
    }

    public List getSubsequentFilterStages()
    {
        refreshProxyList();

        return subsequentDestinations_;
    }

    private void refreshProxyList()
    {
        if ( proxyListDirty_ )
        {
            synchronized ( this )
            {
                if ( proxyListDirty_ )
                {
                    List _l = new Vector();

                    EventChannelImpl.checkAddFilterStage( pullServants_.entrySet().iterator(), _l );
                    EventChannelImpl.checkAddFilterStage( pushServants_.entrySet().iterator(), _l );
                    EventChannelImpl.checkAddFilterStage( eventStyleServants_.iterator(), _l );

                    proxyListDirty_ = false;

                    subsequentDestinations_ = _l;
                }
            }
        }
    }


    public EventConsumer getEventConsumer()
    {
        return null;
    }

    public boolean hasEventConsumer()
    {
        return false;
    }

    public void dispose()
    {
        super.dispose();
        Iterator _i = eventStyleServants_.iterator();

        while ( _i.hasNext() )
        {
            ( ( Disposable ) _i.next() ).dispose();
        }

        eventStyleServants_.clear();
    }

    public boolean hasOrSemantic()
    {
        return filterGroupOperator_.value() == InterFilterGroupOperator._OR_OP;
    }

    public void actionProxyDisposed( ProxyEvent event )
    {
        synchronized ( this )
        {
            proxyListDirty_ = true;
        }
    }

    public void actionProxyCreated(ProxyEvent e) {
        // NO Op
    }

}
