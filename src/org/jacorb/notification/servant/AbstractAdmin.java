package org.jacorb.notification.servant;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.jacorb.notification.ChannelContext;
import org.jacorb.notification.EventChannelImpl;
import org.jacorb.notification.FilterManager;
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.PropertyManager;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.ProxyCreationRequestEvent;
import org.jacorb.notification.interfaces.ProxyCreationRequestEventListener;
import org.jacorb.util.Debug;

import org.omg.CORBA.NO_IMPLEMENT;
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
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.omg.PortableServer.Servant;

import org.apache.avalon.framework.logger.Logger;
import java.util.HashMap;

/**
 * Abstract Baseclass for Adminobjects.
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AbstractAdmin
    implements QoSAdminOperations,
               FilterAdminOperations,
               FilterStage,
               ManageableServant
{
    /**
     * the default InterFilterGroupOperator used.
     */
    protected static final InterFilterGroupOperator
        DEFAULT_FILTER_GROUP_OPERATOR = InterFilterGroupOperator.AND_OP;

    protected static final int NO_ID = Integer.MIN_VALUE;

    protected ChannelContext channelContext_;

    ////////////////////////////////////////
    // Implementation of IF ManageableServant

    private POA poa_;

    protected POA getPOA() {
        return poa_;
    }

    public void setPOA(POA poa) {
        poa_ = poa;
    }

    private ORB orb_;

    protected ORB getORB() {
        return orb_;
    }

    public void setORB(ORB orb) {
        orb_ = orb;
    }

    ////////////////////////////////////////

    protected int id_ = 0;

    protected int proxyIdPool_ = -1;

    protected Integer key_;

    protected FilterManager filterManager_;

    protected InterFilterGroupOperator filterGroupOperator_;

    protected Map pullServants_ = new HashMap();

    protected Map pushServants_ = new HashMap();

    protected Map allProxies_ = new HashMap();

    private Map servantCache_ = Collections.EMPTY_MAP;

    protected Logger logger_ =
        Debug.getNamedLogger( getClass().getName() );

    protected PropertyManager qosProperties_;

    protected PropertyManager adminProperties_;

    protected boolean disposed_ = false;

    protected List seqProxyCreationRequestEventListener_ = new Vector();

    ////////////////////////////////////////

    protected MessageFactory getMessageFactory()
    {
        return channelContext_.getMessageFactory();
    }


    /**
     * @deprecated
     */
    protected EventChannelImpl getChannelServant()
    {
        return channelContext_.getEventChannelServant();
    }


    protected EventChannel getChannel()
    {
        return channelContext_.getEventChannel();
    }


    public POA _default_POA()
    {
        return getPOA();
    }


    protected AbstractAdmin(ChannelContext aChannelContext,
                            PropertyManager aAdminPropertyManager,
                            PropertyManager aQoSPropertyManager,
                            int aId,
                            InterFilterGroupOperator aInterFilterGroupOperator )
    {
        qosProperties_ = aQoSPropertyManager;
        adminProperties_ = aAdminPropertyManager;

        filterGroupOperator_ = aInterFilterGroupOperator;

        channelContext_ = aChannelContext;

        filterManager_ =
            new FilterManager(channelContext_);

        key_ = new Integer( aId );

        setPOA(channelContext_.getPOA());
        setORB(channelContext_.getORB());
    }


    protected AbstractAdmin(ChannelContext aChannelContext,
                            PropertyManager aAdminPropertyManager,
                            PropertyManager aQoSPropertyManager )
    {
        this(aChannelContext,
             aAdminPropertyManager,
             aQoSPropertyManager,
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


    public int add_filter( Filter aFilter )
    {
        return filterManager_.add_filter( aFilter );
    }


    public void remove_filter( int aFilterId ) throws FilterNotFound
    {
        filterManager_.remove_filter( aFilterId );
    }


    public Filter get_filter( int aFilterId ) throws FilterNotFound
    {
        return filterManager_.get_filter( aFilterId );
    }


    public int[] get_all_filters()
    {
        return filterManager_.get_all_filters();
    }


    public void remove_all_filters()
    {
        filterManager_.remove_all_filters();
    }


    public InterFilterGroupOperator MyOperator()
    {
        return filterGroupOperator_;
    }


    public EventChannel MyChannel()
    {
        return getChannel();
    }


    public int MyID()
    {
        return id_;
    }


    public Property[] get_qos()
    {
        return qosProperties_.toArray();
    }


    public void set_qos( Property[] aPropertySeq ) throws UnsupportedQoS
    {
        throw new NO_IMPLEMENT("The method set_qos is not supported yet");
    }


    public void validate_qos( Property[] aPropertySeq,
                              NamedPropertyRangeSeqHolder propertyRangeSeqHolder )
        throws UnsupportedQoS
    {
        throw new NO_IMPLEMENT("The method validate_qos is not supported yet");
    }


    public void destroy()
    {
        dispose();
    }


    public void dispose()
    {
        if ( !disposed_ )
        {
            disposed_ = true;

            logger_.debug( "dispose AbstractAdmin" );

            getChannelServant().removeAdmin( this );

            try
            {
                byte[] _oid = getPOA().servant_to_id( getServant() );
                getPOA().deactivate_object( _oid );
            }
            catch ( ObjectNotActive e )
            {
                logger_.fatalError("Couldnt deactivate Object", e);
            }
            catch ( WrongPolicy e )
            {
                logger_.fatalError("Couldnt deactivate Object", e);
            }
            catch ( ServantNotActive e )
            {
                logger_.fatalError("Couldnt deactivate Object", e);
            }

            remove_all_filters();

            // dispose all servants which are connected to this admin object
            Iterator _i;

            //pushProxies_.clear();

            _i = pushServants_.values().iterator();

            while ( _i.hasNext() )
            {
                logger_.info( "dispose pushServant" );

                try
                {
                    ( ( Disposable ) _i.next() ).dispose();
                }
                catch ( Exception e )
                {
                    logger_.warn( "Error disposing a PushServant", e );
                }

                _i.remove();
            }

            pushServants_.clear();

            //pullProxies_.clear();

            _i = pullServants_.values().iterator();

            while ( _i.hasNext() )
            {
                logger_.info( "dispose pullServant" );

                try
                {
                    ( ( Disposable ) _i.next() ).dispose();
                }
                catch ( Exception e )
                {
                    logger_.warn( "Error disposing a PullServant", e );
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


    public void remove
        ( AbstractProxy aProxy )
    {
        Servant _servant = ( Servant ) servantCache_.remove( aProxy );

        if ( _servant != null )
        {
            logger_.debug( "remove: " + aProxy.getClass().getName() );

            try
            {
                byte[] _oid = getPOA().servant_to_id( _servant );
                getPOA().deactivate_object( _oid );
            }
            catch ( WrongPolicy e )
            {
                logger_.fatalError( "Error removing AdminBase", e );
            }
            catch ( ObjectNotActive e )
            {
                logger_.fatalError( "Error removing AdminBase", e );
            }
            catch ( ServantNotActive e )
            {
                logger_.fatalError( "Error removing AdminBase", e );
            }

            servantCache_.remove( aProxy );
        }
    }


    public boolean isDisposed()
    {
        return disposed_;
    }


    public void addProxyCreationEventListener( ProxyCreationRequestEventListener listener )
    {
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
                ProxyCreationRequestEventListener _pcrListener;
                _pcrListener = ( ProxyCreationRequestEventListener ) _i.next();
                _pcrListener.actionProxyCreationRequest( _event );
            }
        }
    }


    /**
     * Admin never has a Lifetime Filter
     */
    public boolean hasLifetimeFilter()
    {
        return false;
    }


    /**
     * Admin never has a Priority Filter
     */
    public boolean hasPriorityFilter()
    {
        return false;
    }


    /**
     * Admin never has a Lifetime Filter
     */
    public MappingFilter getLifetimeFilter()
    {
        throw new UnsupportedOperationException();
    }


    /**
     * Admin never has a Priority Filter
     */
    public MappingFilter getPriorityFilter()
    {
        throw new UnsupportedOperationException();
    }
}
