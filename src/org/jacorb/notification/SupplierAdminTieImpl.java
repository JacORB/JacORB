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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.EventConsumer;
import org.jacorb.notification.interfaces.ProxyEvent;
import org.jacorb.notification.interfaces.ProxyEventListener;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.IntHolder;
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
import org.omg.CosNotifyChannelAdmin.SupplierAdminPOATie;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.PortableServer.Servant;

/**
 * SupplierAdminImpl.java
 *
 *
 * Created: Sun Oct 13 01:39:12 2002
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class SupplierAdminTieImpl
            extends AdminBase
            implements SupplierAdminOperations,
            Disposable
		       //            ProxyEventListener
{

    private SupplierAdminPOATie thisServant_;
    private SupplierAdmin thisRef_;
    private List eventStyleServants_ = new Vector();

    private List listProxyEventListener_ = new ArrayList();

    SupplierAdminTieImpl( ApplicationContext appContext,
                          ChannelContext channelContext,
                          PropertyManager adminProperties,
                          PropertyManager qosProperties )
    {

        super( appContext,
               channelContext,
               adminProperties,
               qosProperties );
    }

    SupplierAdminTieImpl( ApplicationContext appContext,
                          ChannelContext channelContext,
                          PropertyManager adminProperties,
                          PropertyManager qosProperties,
                          int myId,
                          InterFilterGroupOperator myOperator )
    {

        super( appContext,
               channelContext,
               adminProperties,
               qosProperties,
               myId,
               myOperator );
    }

    public Servant getServant()
    {
        if ( thisServant_ == null )
        {
            synchronized ( this )
            {
                if ( thisServant_ == null )
                {
                    thisServant_ = new SupplierAdminPOATie( this );
                }
            }
        }

        return thisServant_;
    }

    SupplierAdmin getSupplierAdmin()
    {
        if ( thisRef_ == null )
        {
            synchronized ( this )
            {
                if ( thisRef_ == null )
                {
                    getServant();
                    thisRef_ = thisServant_._this( getOrb() );
                }
            }
        }

        return thisRef_;
    }

    public org.omg.CORBA.Object getThisRef()
    {
        return getSupplierAdmin();
    }

    public void offer_change( EventType[] eventType1,
                              EventType[] eventType2 ) throws InvalidEventType
        {}

    // Implementation of org.omg.CosNotifyChannelAdmin.SupplierAdminOperations

    /**
     * Describe <code>pull_consumers</code> method here.
     *
     * @return an <code>int[]</code> value
     */
    public int[] pull_consumers()
    {
        int[] _ret = new int[ pullServants_.size() ];
        Iterator _i = pullServants_.keySet().iterator();
        int x = -1;

        while ( _i.hasNext() )
        {
            _ret[ ++x ] = ( ( Integer ) _i.next() ).intValue();
        }

        return _ret;
    }

    /**
     * Describe <code>push_consumers</code> method here.
     *
     * @return an <code>int[]</code> value
     */
    public int[] push_consumers()
    {
        int[] _ret = new int[ pushServants_.size() ];
        Iterator _i = pushServants_.keySet().iterator();
        int x = -1;

        while ( _i.hasNext() )
        {
            _ret[ ++x ] = ( ( Integer ) _i.next() ).intValue();
        }

        return _ret;
    }

    public ProxyConsumer obtain_notification_pull_consumer( ClientType clientType,
            IntHolder intHolder )
    throws AdminLimitExceeded
    {

        ProxyBase _servant = obtain_notification_pull_consumer_servant( clientType, intHolder );

        Integer _key = _servant.getKey();

        ProxyConsumer _proxyConsumer =
            ProxyConsumerHelper.narrow( _servant.getServant()._this_object( getOrb() ) );

        allProxies_.put( _key, _proxyConsumer );

	fireProxyCreated(_servant);

        return _proxyConsumer;
    }

    public ProxyBase obtain_notification_pull_consumer_servant( ClientType clientType,
            IntHolder intHolder ) throws AdminLimitExceeded
    {

        fireCreateProxyRequestEvent();

        intHolder.value = getPullProxyId();
        Integer _key = new Integer( intHolder.value );
        ProxyBase _servant;

        PropertyManager _adminProperties = ( PropertyManager ) adminProperties_.clone();
        PropertyManager _qosProperties = ( PropertyManager ) qosProperties_.clone();

        switch ( clientType.value() )
        {

        case ClientType._ANY_EVENT:

            _servant = new ProxyPullConsumerImpl( this,
                                                  applicationContext_,
                                                  channelContext_,
                                                  adminProperties_,
                                                  qosProperties_,
                                                  _key );

            break;

        case ClientType._STRUCTURED_EVENT:

            _servant =
                new StructuredProxyPullConsumerImpl( this,
                                                     applicationContext_,
                                                     channelContext_,
                                                     _adminProperties,
                                                     _qosProperties,
                                                     _key );

            break;

        case ClientType._SEQUENCE_EVENT:

            _servant =
                new SequenceProxyPullConsumerImpl( this,
                                                   applicationContext_,
                                                   channelContext_,
                                                   _adminProperties,
                                                   _qosProperties,
                                                   _key );

            break;

        default:
            throw new BAD_PARAM();
        }

        pullServants_.put( _key, _servant );

        if ( filterGroupOperator_.value() == InterFilterGroupOperator._OR_OP )
        {
            _servant.setOrSemantic( true );
        }

	//        _servant.addProxyDisposedEventListener( this );
        _servant.addProxyDisposedEventListener( channelContext_.getRemoveProxyConsumerListener() );

        return _servant;
    }

    /**
     * Describe <code>get_proxy_consumer</code> method here.
     *
     * @param n an <code>int</code> value
     * @return a <code>ProxyConsumer</code> value
     * @exception ProxyNotFound if an error occurs
     */
    public ProxyConsumer get_proxy_consumer( int n ) throws ProxyNotFound
    {
        ProxyConsumer _ret = ( ProxyConsumer ) allProxies_.get( new Integer( n ) );

        if ( _ret == null )
        {
            throw new ProxyNotFound();
        }

        return _ret;
    }


    public ProxyConsumer obtain_notification_push_consumer( ClientType clienttype,
							    IntHolder intholder )
	throws AdminLimitExceeded
    {

        ProxyBase _servant = obtain_notification_push_consumer_servant( clienttype, intholder );
        Integer _key = _servant.getKey();

        ProxyConsumer _proxyConsumer =
            ProxyConsumerHelper.narrow( _servant.getServant()._this_object( getOrb() ) );

        allProxies_.put( _key, _proxyConsumer );

	fireProxyCreated(_servant);

        return _proxyConsumer;
    }

    public ProxyBase obtain_notification_push_consumer_servant( ClientType clientType,
            IntHolder intHolder )
    throws AdminLimitExceeded
    {

        logger_.debug( "obtain_notification_push_consumer()" );

        // may throws AdminLimitExceeded
        fireCreateProxyRequestEvent();

        intHolder.value = getPushProxyId();
        Integer _key = new Integer( intHolder.value );
        ProxyBase _servant;

        PropertyManager _adminProperties = ( PropertyManager ) adminProperties_.clone();
        PropertyManager _qosProperties = ( PropertyManager ) qosProperties_.clone();

        switch ( clientType.value() )
        {

        case ClientType._ANY_EVENT:
            _servant = new ProxyPushConsumerImpl( this,
                                                  applicationContext_,
                                                  channelContext_,
                                                  _adminProperties,
                                                  _qosProperties,
                                                  _key );
            break;

        case ClientType._STRUCTURED_EVENT:
            _servant =
                new StructuredProxyPushConsumerImpl( this,
                                                     applicationContext_,
                                                     channelContext_,
                                                     _adminProperties,
                                                     _qosProperties,
                                                     _key );
            break;

        case ClientType._SEQUENCE_EVENT:
            _servant =
                new SequenceProxyPushConsumerImpl( this,
                                                   applicationContext_,
                                                   channelContext_,
                                                   _adminProperties,
                                                   _qosProperties,
                                                   _key );
            break;

        default:
            throw new BAD_PARAM();
        }

        pushServants_.put( _key, _servant );

        if ( filterGroupOperator_.value() == InterFilterGroupOperator._OR_OP )
        {
            _servant.setOrSemantic( true );
        }

	//        _servant.addProxyDisposedEventListener( this );
        _servant.addProxyDisposedEventListener( channelContext_.getRemoveProxyConsumerListener() );

        logger_.debug( "obtain_notification_push_consumer() => " + _servant );

        return _servant;
    }

    // Implementation of org.omg.CosEventChannelAdmin.SupplierAdminOperations

    /**
     * Describe <code>obtain_pull_consumer</code> method here.
     *
     * @return a <code>ProxyPullConsumer</code> value
     */
    public ProxyPullConsumer obtain_pull_consumer()
    {

        ProxyPullConsumerImpl _servant =
            new ProxyPullConsumerImpl( this,
                                       applicationContext_,
                                       channelContext_,
                                       adminProperties_,
                                       qosProperties_ );

        _servant.setFilterManager( FilterManager.EMPTY );
        eventStyleServants_.add( _servant );
	//        _servant.addProxyDisposedEventListener( this );

        Servant _tie = new org.omg.CosEventChannelAdmin.ProxyPullConsumerPOATie( _servant );
        _servant.setServant( _tie );

        ProxyPullConsumer _ret =
            org.omg.CosEventChannelAdmin.ProxyPullConsumerHelper.narrow( _tie._this_object( getOrb() ) );

	fireProxyCreated(_servant);

        return _ret;
    }

    /**
     * Return a ProxyPushConsumer reference to be used to connect to a
     * PushSupplier.
     */
    public ProxyPushConsumer obtain_push_consumer()
    {

        ProxyPushConsumerImpl _servant =
            new ProxyPushConsumerImpl( this,
                                       applicationContext_,
                                       channelContext_,
                                       adminProperties_,
                                       qosProperties_ );

        _servant.setFilterManager( FilterManager.EMPTY );
        eventStyleServants_.add( _servant );
	//        _servant.addProxyDisposedEventListener( this );

        Servant _tie = new org.omg.CosEventChannelAdmin.ProxyPushConsumerPOATie( _servant );
        _servant.setServant( _tie );

        ProxyPushConsumer _ret =
            org.omg.CosEventChannelAdmin.ProxyPushConsumerHelper.narrow( _tie._this_object( getOrb() ) );

	fireProxyCreated(_servant);

        return _ret;
    }

    public List getSubsequentFilterStages()
    {
        return getChannelServant().getAllConsumerAdmins();
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

	listProxyEventListener_.clear();
    }

    void fireProxyRemoved(ProxyBase b) {
	Iterator i = listProxyEventListener_.iterator();
	ProxyEvent e = new ProxyEvent(b);
	while (i.hasNext()) {
	    ((ProxyEventListener)i.next()).actionProxyDisposed(e);
	}
    }

    void fireProxyCreated(ProxyBase b) {
	Iterator i = listProxyEventListener_.iterator();
	ProxyEvent e = new ProxyEvent(b);
	while (i.hasNext()) {
	    ((ProxyEventListener)i.next()).actionProxyCreated(e);
	}
    }

    /**
     *
     */
    public void remove
        ( ProxyBase pb )
    {
        super.remove( pb );

        Integer _key = pb.getKey();

        if ( _key != null )
        {
            allProxies_.remove( _key );

            if ( pb instanceof StructuredProxyPullConsumerImpl
                    || pb instanceof ProxyPullConsumerImpl
                    || pb instanceof SequenceProxyPullConsumerImpl )
            {

                pullServants_.remove( _key );

            }
            else if ( pb instanceof StructuredProxyPushConsumerImpl
                      || pb instanceof ProxyPushConsumerImpl
                      || pb instanceof SequenceProxyPushConsumerImpl )
            {

                pushServants_.remove( _key );
            }
        }
        else
        {
            eventStyleServants_.remove( pb );
        }

	fireProxyRemoved(pb);
    }

    public boolean hasOrSemantic()
    {
        return false;
    }

    public void addProxyEventListener(ProxyEventListener l) {
	listProxyEventListener_.add(l);
    }

    public void removeProxyEventListener(ProxyEventListener l) {
	listProxyEventListener_.remove(l);
    }

//     public void actionProxyDisposed( ProxyEvent e )
//     {}

}
