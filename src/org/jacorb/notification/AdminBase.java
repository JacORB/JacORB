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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.ProxyCreationRequestEvent;
import org.jacorb.notification.interfaces.ProxyCreationRequestEventListener;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.QoSAdminOperations;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterAdminOperations;
import org.omg.CosNotifyFilter.FilterNotFound;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

/**
 * Abstract Baseclass for Adminobjects.
 *
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AdminBase 
    implements QoSAdminOperations,
	       FilterAdminOperations,
	       FilterStage
{
    protected static final InterFilterGroupOperator DEFAULT_FILTER_GROUP_OPERATOR =
        InterFilterGroupOperator.AND_OP;

    protected static final int NO_ID = Integer.MIN_VALUE;

    protected ChannelContext channelContext_;
    protected ApplicationContext applicationContext_;

    protected int id_ = 0;
    protected int proxyIdPool_ = -1;
    protected Integer key_;
    protected FilterManager filterManager_;

    protected InterFilterGroupOperator filterGroupOperator_;

    protected Map pullServants_;
    protected Map pushServants_;

    protected Map allProxies_;
    private Map servantCache_ = Collections.EMPTY_MAP;

    protected Logger logger_ =
        Hierarchy.getDefaultHierarchy().getLoggerFor( getClass().getName() );

    protected PropertyManager qosProperties_;
    protected PropertyManager adminProperties_;

    protected boolean disposed_ = false;

    protected List seqProxyCreationRequestEventListener_;

    protected NotificationEventFactory getNotificationEventFactory()
    {
        return applicationContext_.getNotificationEventFactory();
    }

    protected EventChannelImpl getChannelServant()
    {
        return channelContext_.getEventChannelServant();
    }

    protected EventChannel getChannel()
    {
        return channelContext_.getEventChannel();
    }

    protected ORB getOrb()
    {
        return applicationContext_.getOrb();
    }

    protected POA getPoa()
    {
        return applicationContext_.getPoa();
    }

    public POA _default_POA()
    {
        return applicationContext_.getPoa();
    }

    protected AdminBase( ApplicationContext appContext,
                         ChannelContext channelContext,
                         PropertyManager adminProperties,
                         PropertyManager qosProperties,
                         int myId,
                         InterFilterGroupOperator filterGroupOperator )
    {

        qosProperties_ = qosProperties;
        adminProperties_ = adminProperties;

        filterGroupOperator_ = filterGroupOperator;

        applicationContext_ = appContext;
        channelContext_ = channelContext;

        filterManager_ = new FilterManager();

        pullServants_ = new Hashtable();
        pushServants_ = new Hashtable();
        allProxies_ = new Hashtable();

        key_ = new Integer( myId );
    }

    protected AdminBase( ApplicationContext appContext,
                         ChannelContext channelContext,
                         PropertyManager adminProps,
                         PropertyManager qosProps )
    {
	
        this( appContext,
              channelContext,
              adminProps,
              qosProps,
              NO_ID,
              DEFAULT_FILTER_GROUP_OPERATOR );
    }

    int getPushProxyId()
    {
        return ++proxyIdPool_;
    }

    int getPullProxyId()
    {
        return ++proxyIdPool_;
    }

    public List getFilters()
    {
        return filterManager_.getFilters();
    }

    // Code for delegation of FilterManager methods to filterManager_

    /**
     * Describe <code>add_filter</code> method here.
     *
     * @param filter a <code>Filter</code> value
     * @return an <code>int</code> value
     */
    public int add_filter( Filter filter )
    {
        return filterManager_.add_filter( filter );
    }

    /**
     * Describe <code>remove_filter</code> method here.
     *
     * @param n an <code>int</code> value
     * @exception FilterNotFound if an error occurs
     */
    public void remove_filter( int n ) throws FilterNotFound
    {
        filterManager_.remove_filter( n );
    }

    /**
     * Describe <code>get_filter</code> method here.
     *
     * @param n an <code>int</code> value
     * @return a <code>Filter</code> value
     * @exception FilterNotFound if an error occurs
     */
    public Filter get_filter( int n ) throws FilterNotFound
    {
        return filterManager_.get_filter( n );
    }

    /**
     * Describe <code>get_all_filters</code> method here.
     *
     * @return an <code>int[]</code> value
     */
    public int[] get_all_filters()
    {
        return filterManager_.get_all_filters();
    }

    /**
     * Describe <code>remove_all_filters</code> method here.
     *
     */
    public void remove_all_filters()
    {
        filterManager_.remove_all_filters();
    }

    /**
     * Describe <code>MyOperator</code> method here.
     *
     * @return an <code>InterFilterGroupOperator</code> value
     */
    public InterFilterGroupOperator MyOperator()
    {
        return filterGroupOperator_;
    }

    /**
     * Describe <code>MyChannel</code> method here.
     *
     * @return an <code>EventChannel</code> value
     */
    public EventChannel MyChannel()
    {
        return getChannel();
    }

    /**
     * Describe <code>MyID</code> method here.
     *
     * @return an <code>int</code> value
     */
    public int MyID()
    {
        return id_;
    }

    // Implementation of org.omg.CosNotification.QoSAdminOperations

    /**
     * Describe <code>get_qos</code> method here.
     *
     * @return a <code>Property[]</code> value
     */
    public Property[] get_qos()
    {
        return null;
    }

    /**
     * Describe <code>set_qos</code> method here.
     *
     * @param property a <code>Property[]</code> value
     * @exception UnsupportedQoS if an error occurs
     */
    public void set_qos( Property[] property ) throws UnsupportedQoS
        {}

    /**
     * Describe <code>validate_qos</code> method here.
     *
     * @param property a <code>Property[]</code> value
     * @param namedPropertyRangeSeqHolder a
     * <code>NamedPropertyRangeSeqHolder</code> value
     * @exception UnsupportedQoS if an error occurs
     */
    public void validate_qos( Property[] property,
                              NamedPropertyRangeSeqHolder namedPropertyRangeSeqHolder )
    throws UnsupportedQoS
    {
    }

    public void destroy()
    {
        dispose();
    }
    

    public synchronized void dispose()
    {
        if ( !disposed_ )
        {
            logger_.debug( "dispose()" );

            getChannelServant().removeAdmin( this );

            try
            {
                byte[] _oid = getPoa().servant_to_id( getServant() );
                getPoa().deactivate_object( _oid );
            }
            catch ( ObjectNotActive e )
            {
                e.printStackTrace();
            }
            catch ( WrongPolicy e )
            {
                e.printStackTrace();
            }
            catch ( ServantNotActive e )
            {
                e.printStackTrace();
            }

            remove_all_filters();

            // dispose all servants which are connected to this admin object
            Iterator _i;

            //pushProxies_.clear();

            _i = pushServants_.values().iterator();

            while ( _i.hasNext() )
            {
                logger_.info( "dispose pushServant" );

		try {
		    ( ( Disposable ) _i.next() ).dispose();
		} catch (Exception e) {
		    logger_.warn("Error disposing a PushServant", e);
		}

		_i.remove();
            }

            pushServants_.clear();

            //pullProxies_.clear();

            _i = pullServants_.values().iterator();

            while ( _i.hasNext() )
            {
                logger_.info( "dispose pullServant" );

		try {
		    ( ( Disposable ) _i.next() ).dispose();
		} catch (Exception e) {
		    logger_.warn("Error disposing a PullServant", e);
		}

                _i.remove();
            }

            pullServants_.clear();

            disposed_ = true;
        }
        else
        {
            throw new OBJECT_NOT_EXIST();
        }
    }

    public Integer getKey()
    {
        return key_;
    }

    /**
     *
     */
    public void remove( ProxyBase proxy )
    {
        Servant _servant = ( Servant ) servantCache_.remove( proxy );

        if ( _servant != null )
        {
            logger_.debug( "remove: " + proxy.getClass().getName() );

            try
            {
                byte[] _oid = getPoa().servant_to_id( _servant );
                getPoa().deactivate_object( _oid );
            }
            catch ( WrongPolicy e )
            {
                e.printStackTrace();
            }
            catch ( ObjectNotActive e )
            {
                e.printStackTrace();
            }
            catch ( ServantNotActive e )
            {
                e.printStackTrace();
            }

            servantCache_.remove( proxy );
        }
    }

    public abstract org.omg.CORBA.Object getThisRef();

    public abstract Servant getServant();

    public boolean isDisposed()
    {
        return disposed_;
    }

    public void addProxyCreationEventListener( ProxyCreationRequestEventListener listener )
    {
        if ( seqProxyCreationRequestEventListener_ == null )
        {
            synchronized ( this )
            {
                if ( seqProxyCreationRequestEventListener_ == null )
                {
                    seqProxyCreationRequestEventListener_ = new Vector();
                }
            }
        }

        seqProxyCreationRequestEventListener_.add( listener );
    }

    public void removeProxyCreationEventListener( ProxyCreationRequestEventListener listener )
    {
        if ( seqProxyCreationRequestEventListener_ != null )
        {
            seqProxyCreationRequestEventListener_.remove( listener );
        }
    }

    protected void fireCreateProxyRequestEvent() throws AdminLimitExceeded
    {
        if ( seqProxyCreationRequestEventListener_ != null )
        {
            ProxyCreationRequestEvent _event = 
		new ProxyCreationRequestEvent( this );

            Iterator _i = seqProxyCreationRequestEventListener_.iterator();

            while ( _i.hasNext() )
            {
                ( ( ProxyCreationRequestEventListener ) _i.next() ).actionProxyCreationRequest( _event );
            }
        }
    }

    public boolean hasLifetimeFilter() {
	return false;
    }

    public boolean hasPriorityFilter() {
	return false;
    }

    public MappingFilter getLifetimeFilter() {
	return null;
    }

    public MappingFilter getPriorityFilter() {
	return null;
    }
}
