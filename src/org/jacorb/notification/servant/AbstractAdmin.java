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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.jacorb.notification.servant.AdminPropertySet;
import org.jacorb.notification.ChannelContext;
import org.jacorb.notification.EventChannelImpl;
import org.jacorb.notification.FilterManager;
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.servant.QoSPropertySet;
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
import org.omg.CosNotifyChannelAdmin.EventChannelHelper;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosNotifyChannelAdmin.ProxyNotFound;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterAdminOperations;
import org.omg.CosNotifyFilter.FilterNotFound;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;
import org.apache.avalon.framework.logger.Logger;

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

    protected static final int NO_ID = 0;

    protected ChannelContext channelContext_;

    ////////////////////////////////////////

    protected Logger logger_ =
        Debug.getNamedLogger( getClass().getName() );

    private Integer key_;

    private boolean isKeyPublic_;

    private POA poa_;

    private ORB orb_;

    private EventChannelImpl eventChannelServant_;

    protected InterFilterGroupOperator filterGroupOperator_;

    protected FilterManager filterManager_;

    private SynchronizedInt proxyIdPool_ = new SynchronizedInt(0);

    protected Object modifyProxiesLock_ = new Object();

    protected Map pullServants_ = new HashMap();

    protected Map pushServants_ = new HashMap();

    protected QoSPropertySet qosSettings_ = new QoSPropertySet(QoSPropertySet.ADMIN_QOS);

    protected AdminPropertySet adminSettings_ = new AdminPropertySet();

    protected boolean disposed_ = false;

    protected List seqProxyCreationRequestEventListener_ = new Vector();

    ////////////////////////////////////////

    protected AbstractAdmin(ChannelContext channelContext)
    {
        channelContext_ = channelContext;

        eventChannelServant_ = channelContext_.getEventChannelServant();

        filterManager_ =
            new FilterManager(channelContext_);

        setPOA(channelContext_.getPOA());

        setORB(channelContext_.getORB());
    }

    ////////////////////////////////////////

    public void setInterFilterGroupOperator(InterFilterGroupOperator op)
    {
        filterGroupOperator_ = op;
    }


    public void setKey(Integer key)
    {
        key_ = key;
    }


    public void setIsKeyPublic(boolean isKeyPublic)
    {
        isKeyPublic_ = isKeyPublic;
    }


    protected POA getPOA()
    {
        return poa_;
    }


    public void setPOA(POA poa)
    {
        poa_ = poa;
    }


    protected ORB getORB()
    {
        return orb_;
    }


    public void setORB(ORB orb)
    {
        orb_ = orb;
    }


    protected MessageFactory getMessageFactory()
    {
        return channelContext_.getMessageFactory();
    }


    protected EventChannelImpl getChannelServant()
    {
        return eventChannelServant_;
    }


    private EventChannel getChannel()
    {
        return EventChannelHelper.narrow(eventChannelServant_.activate());
    }


    public POA _default_POA()
    {
        return getPOA();
    }


    int getProxyID()
    {
        return proxyIdPool_.increment();
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
        return key_.intValue();
    }


    public Property[] get_qos()
    {
        return qosSettings_.get_qos();
    }


    public void set_qos( Property[] props ) throws UnsupportedQoS
    {
        qosSettings_.validate_qos(props, new NamedPropertyRangeSeqHolder());

        qosSettings_.set_qos(props);
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
        synchronized (this) {
            if (!disposed_) {
                disposed_ = true;
            } else {
                throw new OBJECT_NOT_EXIST();
            }
        }

        //////////////////////////////

        getChannelServant().removeAdmin( this );

        //////////////////////////////

        remove_all_filters();

        //////////////////////////////

        logger_.debug("dispose PushServants");
        Iterator _i;

        synchronized(modifyProxiesLock_) {
            _i = pushServants_.values().iterator();

            while ( _i.hasNext() )
                {
                    try
                        {
                            Disposable _d = (Disposable)_i.next();

                            _i.remove();

                            _d.dispose();
                        }
                    catch ( Exception e )
                        {
                            logger_.warn( "Error disposing a PushServant", e );
                        }
                }

            pushServants_.clear();

            //////////////////////////////

            logger_.debug("dispose PullServants");

            _i = pullServants_.values().iterator();

            while ( _i.hasNext() )
                {
                    try
                        {
                            Disposable _d = (Disposable)_i.next();

                            _i.remove();

                            _d.dispose();
                        }
                    catch ( Exception e )
                        {
                            logger_.warn( "Error disposing a PullServant", e );
                        }
                }

            pullServants_.clear();
        }
    }


    public void deactivate() {
        logger_.debug( "deactivate Object" );

        try
            {
                byte[] _oid = getPOA().servant_to_id( getServant() );
                getPOA().deactivate_object( _oid );
            }
        catch ( Exception e )
            {
                logger_.fatalError("Couldn't deactivate Object", e);
                throw new RuntimeException();
            }
    }


    abstract Servant getServant();


    public Integer getKey()
    {
        return key_;
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


    public boolean hasLifetimeFilter()
    {
        return false;
    }


    public boolean hasPriorityFilter()
    {
        return false;
    }


    public MappingFilter getLifetimeFilter()
    {
        throw new UnsupportedOperationException();
    }


    public MappingFilter getPriorityFilter()
    {
        throw new UnsupportedOperationException();
    }


    protected AbstractProxy getProxy(int key) throws ProxyNotFound
    {
        Integer _key = new Integer(key);
        AbstractProxy _servant = null;

        synchronized (modifyProxiesLock_)
        {
            _servant = (AbstractProxy)pullServants_.get(_key);

            if (_servant == null)
            {
                _servant = (AbstractProxy)pullServants_.get(_key);
            }
        }

        if (_servant == null || !_servant.isKeyPublic() )
        {
            throw new ProxyNotFound("The ProxyConsumer with ID=" + key + " does not exist");
        }

        return _servant;
    }


    protected int[] get_all_notify_proxies(Map map, Object lock)
    {
        List _allKeys = new ArrayList();

        synchronized (lock)
        {
            Iterator _i = map.entrySet().iterator();

            while ( _i.hasNext() )
            {
                Map.Entry _entry = (Map.Entry)_i.next();

                if ( ( (AbstractProxy)_entry.getValue() ).isKeyPublic() ) {
                    _allKeys.add(_entry.getKey());
                }

            }
        }

        int[] _allKeysArray = new int[_allKeys.size()];
        for (int x=0; x<_allKeysArray.length; ++x) {
            _allKeysArray[x] = ((Integer)_allKeys.get(x)).intValue();
        }
        return _allKeysArray;
    }


    protected void configureEventStyleID(AbstractProxy servant) {
        servant.setKey(new Integer(getProxyID()), false);

        servant.setFilterManager( FilterManager.EMPTY_FILTER_MANAGER );
    }


    protected void configureQoS(AbstractProxy servant)  {
        try {
            servant.set_qos(qosSettings_.get_qos());
        } catch (UnsupportedQoS e) {
            logger_.fatalError("unexpected exception", e);

            throw new RuntimeException(e.getMessage());
        }
    }


    protected void configureNotifyStyleID(AbstractProxy servant) {
        servant.setKey(new Integer(getProxyID()), true);
    }


    protected void configureInterFilterGroupOperator(AbstractProxy servant) {
        if ( filterGroupOperator_ != null &&
             (filterGroupOperator_.value() == InterFilterGroupOperator._OR_OP ) )
            {
                servant.setInterFilterGroupOperatorOR( true );
            }
    }

}
