package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.EventChannelEvent;
import org.jacorb.notification.interfaces.EventChannelEventListener;
import org.jacorb.notification.queue.EventQueueFactory;
import org.jacorb.notification.servant.ManageableServant;
import org.jacorb.notification.util.AdminPropertySet;
import org.jacorb.notification.util.PatternWrapper;
import org.jacorb.notification.util.PropertySet;
import org.jacorb.notification.util.QoSPropertySet;

import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.CosNotification.BestEffort;
import org.omg.CosNotification.ConnectionReliability;
import org.omg.CosNotification.EventReliability;
import org.omg.CosNotification.Persistent;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.PropertyError;
import org.omg.CosNotification.PropertyRange;
import org.omg.CosNotification.QoSError_code;
import org.omg.CosNotification.UnsupportedAdmin;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.ChannelNotFound;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyChannelAdmin.EventChannelFactoryHelper;
import org.omg.CosNotifyChannelAdmin.EventChannelFactoryPOA;
import org.omg.CosNotifyChannelAdmin.EventChannelHelper;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.CosNotifyFilter.FilterFactoryHelper;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.omg.PortableServer.Servant;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;

/**
 * <code>EventChannelFactoryImpl</code> is a implementation of
 * the  <code>EventChannelFactory</code> interface which defines operations
 * for creating and managing new Notification Service style event
 * channels. It supports a routine that creates new instances of
 * Notification Service event channels and assigns unique numeric
 * identifiers to them. In addition the
 * <code>EventChannelFactory</code> interface supports a routing,
 * which can return the unique identifiers assigned to all event
 * channels created by a given instance of
 * <code>EventChannelFactory</code>, and another routine which, given
 * the unique identifier of an event channel created by a target
 * <code>EventChannelFactory</code> instance, returns the object
 * reference of that event channel.<br>
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class EventChannelFactoryImpl
    extends JacORBEventChannelFactoryPOA
    implements Disposable,
               ManageableServant,
               Configurable
{
    interface ShutdownCallback
    {
        void needTime( int time );
        void shutdownComplete();
    }

    ////////////////////////////////////////

    private static final long SHUTDOWN_INTERVAL = 1000;

    private static final Object[] INTEGER_ARRAY_TEMPLATE = new Integer[ 0 ];

    private static final String STANDARD_IMPL_NAME =
        "JacORB-NotificationService";

    private static final String NOTIFICATION_SERVICE_SHORTCUT =
        "NotificationService";

    private static final String EVENTCHANNEL_FACTORY_POA_NAME =
        "EventChannelFactoryPOA";

    private static final String OBJECT_NAME = "_ECFactory";

    ////////////////////////////////////////

    private Logger logger_ = null;
    private Configuration config_;
    private ORB orb_;
    private POA eventChannelFactoryPOA_;
    private POA rootPOA_;
    private EventChannelFactory thisFactory_;
    private FilterFactory defaultFilterFactory_;
    private FilterFactoryImpl defaultFilterFactoryServant_;
    private ApplicationContext applicationContext_;
    private SynchronizedInt eventChannelIDPool_;
    private Map allChannels_ = new HashMap();
    private Object allChannelsLock_ = allChannels_;
    private String ior_;
    private String corbaLoc_;
    private List listEventChannelEventListener_ = new ArrayList();
    private StaticEventChannelFactoryInfo staticInfo_;
    private String staticURL_;
    private Runnable destroyMethod_;

    private ChannelContext defaultChannelContext_;

    ////////////////////////////////////////

    private EventChannelFactoryImpl() {
        initialize();
    }

    ////////////////////////////////////////

    public void configure (Configuration conf)
    {
        config_ = conf;

        logger_ = ((org.jacorb.config.Configuration)conf).getNamedLogger(getClass().getName());;

        defaultChannelContext_.configure(conf);

        applicationContext_.configure (conf);

        String _filterFactoryConf =
            conf.getAttribute(Attributes.FILTER_FACTORY,
                              Default.DEFAULT_FILTER_FACTORY);

        try {
            setUpDefaultFilterFactory(_filterFactoryConf);
        } catch (InvalidName ex) {
            logger_.error("FilterFactory setup failed", ex);
        }

        staticURL_ = conf.getAttribute(Attributes.FILTER_FACTORY,
                                       Default.DEFAULT_FILTER_FACTORY);
    }


    public void setDestroyMethod(Runnable destroyMethod) {
        destroyMethod_ = destroyMethod;
    }


    public void setORB(ORB orb) {
        orb_ = orb;
        org.jacorb.orb.ORB jorb = (org.jacorb.orb.ORB)orb_;

        try {
            org.omg.CORBA.Object obj =
                orb.resolve_initial_references( "RootPOA" );
            rootPOA_ = POAHelper.narrow(obj);

            applicationContext_ = new ApplicationContext(orb, rootPOA_);

            org.omg.CORBA.Policy[] _policies = new org.omg.CORBA.Policy []
            {
                rootPOA_.create_id_assignment_policy
                ( IdAssignmentPolicyValue.USER_ID )
            };

            eventChannelFactoryPOA_ =
                rootPOA_.create_POA(EVENTCHANNEL_FACTORY_POA_NAME,
                                    rootPOA_.the_POAManager(),
                                    _policies );

            for ( int x = 0; x < _policies.length; ++x )
                _policies[ x ].destroy();

            rootPOA_.the_POAManager().activate();
        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException(e.getMessage());
        }
    }


    public void setPOA(POA poa) {
        // ignore
    }


    protected ORB getORB() {
        return orb_;
    }

    /**
     * The <code>create_channel</code> operation is invoked to create
     * a new instance of the Notification Service style event
     * channel. This operation accepts two input parameters. The first
     * input parameter is a list of name-value pairs, which specify the
     * initial QoS property settings for the new channel. The second
     * input parameter is a list of name-value pairs, which specify
     * the initial administrative property settings for the new
     * channel. <br> If no implementation of the
     * <code>EventChannel</code> Interface exists that can support all
     * of the requested administrative property settings, the
     * <code>UnsupportedAdmin</code> exception is raised This
     * exception contains as data a sequence of data structures, each
     * identifies the name of an administrative property in the input
     * list whose requested setting could not be satisfied, along with
     * an error code and a range of settings for the property which
     * could be satisfied. The meanings of the error codes that might
     * be returned are described in <a
     * href="%%%NOTIFICATION_SPEC_URL%%%">Notification Service
     * Specification</a> Table 2-5 on page 2-46.<br>
     * If neither of these exceptions is raised, the
     * <code>create_channel</code> operation will return a reference
     * to a new Notification Service style event channel. In addition,
     * the operation assigns to this new event channel a numeric
     * identifier, which is unique among all event channels created by
     * the target object. This numeric identifier is returned as an
     * output parameter.
     *
     * @param qualitiyOfServiceProperties a list of name-value pairs,
     * which specify the initial QoS property settings for the new channel
     * @param administrativeProperties a list of name-value pairs,
     * which specify the initial administrative property settings for
     * the new channel
     * @param channelIdentifier, a reference to the new event channel
     * @return a newly created event channel
     * @exception UnsupportedAdmin if no implementation supports the
     * requested administrative settings
     * @exception UnsupportedQoS if no implementation supports the
     * requested QoS settings
     */
    public EventChannel create_channel( Property[] qualitiyOfServiceProperties,
                                        Property[] administrativeProperties,
                                        IntHolder channelIdentifier )
        throws UnsupportedAdmin,
               UnsupportedQoS
    {
        try
        {
            // create identifier
            int _identifier = createChannelIdentifier();
            channelIdentifier.value = _identifier;

            final Integer _key = new Integer( _identifier );

            final EventChannelImpl _channelServant =
                create_channel_servant( _identifier,
                                        qualitiyOfServiceProperties,
                                        administrativeProperties );

            eventChannelServantCreated( _channelServant );

            if (logger_.isInfoEnabled()) {
                logger_.info( "created EventChannel with ID: " + _identifier );
            }

            synchronized(allChannelsLock_) {
                allChannels_.put( _key, _channelServant );
            }

            _channelServant.setDisposeHook(new Runnable() {
                    public void run() {
                        synchronized(allChannelsLock_) {
                            allChannels_.remove( _key );
                        }

                        fireEventChannelDestroyed(_channelServant);
                }
            });

            return EventChannelHelper.narrow(_channelServant.activate());
        } catch (UnsupportedQoS e) {
            throw e;
        } catch (UnsupportedAdmin e) {
            throw e;
        } catch ( Exception e ) {
            logger_.fatalError( "create_channel", e );
            throw new RuntimeException();
        }
    }


    protected void eventChannelServantCreated( EventChannelImpl servant )
    {
        EventChannelEvent _event = new EventChannelEvent( servant );

        Iterator _i = listEventChannelEventListener_.iterator();

        while ( _i.hasNext() )
        {
            ( ( EventChannelEventListener ) _i.next() ).actionEventChannelCreated( _event );
        }
    }


    private void checkQoSSettings(PropertySet _uniqueQoSProperties)
        throws UnsupportedQoS
    {
        if ( _uniqueQoSProperties.containsKey( EventReliability.value ) )
        {
            short _eventReliabilty =
                _uniqueQoSProperties.get(EventReliability.value).
                extract_short();

            switch (_eventReliabilty)
            {
            case BestEffort.value:
                logger_.info("EventReliability=BestEffort");
                break;

            case Persistent.value:
                throwPersistentNotSupported( EventReliability.value );

            default:
                throwBadValue( EventReliability.value );
            }
        }

        short _connectionReliability = BestEffort.value;

        if ( _uniqueQoSProperties.containsKey( ConnectionReliability.value ) )
        {
            _connectionReliability =
                _uniqueQoSProperties.get( ConnectionReliability.value ).
                extract_short();

            switch ( _connectionReliability )
            {
            case BestEffort.value:
                logger_.info("ConnectionReliability=BestEffort");
                break;

            case Persistent.value:
                //break;
                throwPersistentNotSupported( ConnectionReliability.value );

            default:
                throwBadValue( ConnectionReliability.value );
            }
        }
    }


    public EventChannelImpl create_channel_servant( final int channelID,
                                                    Property[] qualitiyOfServiceProperties,
                                                    Property[] administrativeProperties )
        throws UnsupportedAdmin,
               UnsupportedQoS,
               ObjectNotActive,
               WrongPolicy,
               ServantAlreadyActive,
               ConfigurationException
    {
        if (logger_.isInfoEnabled() ) {
            logger_.debug( "create channel_servant id=" + channelID );
        }

        // check QoS and Admin Settings

        AdminPropertySet _adminSettings =
            new AdminPropertySet(config_);

        _adminSettings.set_admin( administrativeProperties );

        QoSPropertySet _qosSettings =
            new QoSPropertySet( config_, QoSPropertySet.ADMIN_QOS);

        _qosSettings.set_qos(qualitiyOfServiceProperties);

        if (logger_.isDebugEnabled() )
        {
            logger_.debug( "uniqueQoSProps: " + _qosSettings );
            logger_.debug( "uniqueAdminProps: " + _adminSettings );
        }

        checkQoSSettings(_qosSettings);

        // create channel context
        ChannelContext _channelContext = (ChannelContext)defaultChannelContext_.clone();

        _channelContext.setORB(applicationContext_.getOrb());

        _channelContext.setPOA(applicationContext_.getPoa());

        _channelContext.setMessageFactory(applicationContext_.
                                          getMessageFactory());

        _channelContext.setTaskProcessor(applicationContext_.
                                         getTaskProcessor());


        EventQueueFactory _factory = new EventQueueFactory();


        _factory.configure( ( (org.jacorb.orb.ORB)applicationContext_.getOrb() ).getConfiguration() );


        _channelContext.setEventQueueFactory(_factory);

        // create new servant
        final EventChannelImpl _eventChannelServant =
            new EventChannelImpl();

        _eventChannelServant.setDefaultFilterFactory( defaultFilterFactory_ );


        _channelContext.resolveDependencies(_eventChannelServant);

        try {
            org.jacorb.orb.ORB jorb =
                (org.jacorb.orb.ORB)applicationContext_.getOrb();
            _eventChannelServant.configure (jorb.getConfiguration());
        } catch (Throwable ex) {
            ex.printStackTrace();

            throw new RuntimeException(ex);
        }

        _eventChannelServant.setKey(channelID);
        _eventChannelServant.set_qos(_qosSettings.toArray());
        _eventChannelServant.set_admin(_adminSettings.toArray());
        _eventChannelServant.setORB(applicationContext_.getOrb());
        _eventChannelServant.setPOA(applicationContext_.getPoa());
        return _eventChannelServant;
    }


    private void fireEventChannelDestroyed(EventChannelImpl channel) {
        if (!listEventChannelEventListener_.isEmpty())
            {
                EventChannelEvent _event =
                    new EventChannelEvent( channel );

                Iterator i = listEventChannelEventListener_.iterator();

                while ( i.hasNext() )
                    {
                        ( ( EventChannelEventListener ) i.next() ).actionEventChannelDestroyed( _event );
                    }
            }
    }


    void removeEventChannelServant( int id )
    {
        EventChannelImpl _channel;

    }


    private void initializeEventChannelIDPool() {
        eventChannelIDPool_ = new SynchronizedInt(-1);
    }


    private void initialize() {
        initializeEventChannelIDPool();
    }


    private int createChannelIdentifier()
    {
        return eventChannelIDPool_.increment();
    }


    /**
     * The <code>get_all_channels</code> operation returns a sequence
     * of all of the unique numeric identifiers corresponding to
     * Notification Service event channels, which have been created by
     * the target object.
     *
     * @return an <code>int[]</code> value
     */
    public int[] get_all_channels()
    {
        Integer[] _keys;

        synchronized(allChannelsLock_) {
            _keys = ( Integer[] ) allChannels_.keySet().toArray( INTEGER_ARRAY_TEMPLATE );
        }

        int[] _ret = new int[ _keys.length ];

        for ( int x = _keys.length - 1; x >= 0; --x )
        {
            _ret[ x ] = _keys[ x ].intValue();
        }

        return _ret;
    }


    /**
     * The <code>get_event_channel</code> operation accepts as input
     * a numeric value that is supposed to be the unique identifier of
     * a Notification Service event channel, which has been created by
     * the target object. If this input value does not correspond to
     * such a unique identifier, the <code>ChannelNotFound</code>
     * exception is raised. Otherwise, the operation returns the
     * object reference of the Notification Service event channel
     * corresponding to the input identifier.
     *
     * @param n an <code>int</code> the unique identifier of a
     * Notification Service event channel
     * @return an <code>EventChannel</code> corresponding to the input identifier
     * @exception ChannelNotFound if the input value does not
     * correspond to a Notification Service event channel
     */
    public EventChannel get_event_channel( int id ) throws ChannelNotFound
    {
        return EventChannelHelper.narrow(get_event_channel_servant( id ).activate());
    }


    public EventChannelImpl get_event_channel_servant( int id )
        throws ChannelNotFound
    {
        Integer _key = new Integer(id);

        synchronized(allChannelsLock_) {
            if (allChannels_.containsKey(_key)) {
                return ( EventChannelImpl ) allChannels_.get( _key );
            } else {
                logger_.error("channel: " + id + " not found " + allChannels_);
                throw new ChannelNotFound("The Channel " + id + " does not exist");
            }
        }
    }


    public void addEventChannelEventListener( EventChannelEventListener listener )
    {
        listEventChannelEventListener_.add( listener );
    }


    public void removeEventChannelEventListener( EventChannelEventListener listener )
    {
        listEventChannelEventListener_.remove( listener );
    }


    public void shutdown( ShutdownCallback cb )
    {
        // estimate shutdown time.
        // during shutdown disconnect must be called on every
        // connected client. in worst case the client is not
        // acccessible anymore and disconnect raises TRANSIENT. as
        // this could take some time request some more time from the
        // WrapperManager who is initiating the shutdown.

        int _numberOfClients = 0;

        synchronized(allChannelsLock_) {
            Iterator i = allChannels_.entrySet().iterator();
            while (i.hasNext()) {
                EventChannelImpl _channel = (EventChannelImpl)((Map.Entry)i.next()).getValue();

                _numberOfClients += _channel.getNumberOfConnectedClients();
            }
        }

        // TODO fetch this from somewhere?
        int _connectionTimeout = 4000;

        int _estimatedShutdowntime = _numberOfClients * _connectionTimeout;

        if (logger_.isInfoEnabled()) {
            logger_.info("Connected Clients: " + _numberOfClients );
            logger_.info("Connection Timeout: " + _connectionTimeout + " ms");
            logger_.info("Estimated Shutdowntime: " +
                         _estimatedShutdowntime + " ms");
        }

        // estimate 4000ms shutdowntime per channel
        cb.needTime( _estimatedShutdowntime );

        logger_.info( "NotificationService is going down" );

        dispose();

        logger_.info( "NotificationService down" );

        cb.shutdownComplete();
    }


    public void deactivate()
    {
        try {
            eventChannelFactoryPOA_.deactivate_object(eventChannelFactoryPOA_.servant_to_id(getServant()));
        } catch (Exception e) {
            logger_.fatalError("unable to deactivate object", e);

            throw new RuntimeException();
        }
    }


    public void destroy()
    {
        // start extra thread to
        // shut down the Notification Service.
        // otherwise ORB.shutdown() would be called inside
        // a remote invocation which causes an exception.
        Thread _shutdown =
            new Thread() {
                public void run() {
                    try {
                        logger_.info("Notification Service is going down in " +
                                     SHUTDOWN_INTERVAL + " ms");
                        Thread.sleep(SHUTDOWN_INTERVAL);
                    } catch (InterruptedException e) {}

                    if (destroyMethod_ != null) {
                        destroyMethod_.run();
                    } else {
                        dispose();
                    }
                }
            };
        _shutdown.start();
    }


    public void dispose()
    {
        listEventChannelEventListener_.clear();

        synchronized(allChannelsLock_) {
            Iterator _i = allChannels_.entrySet().iterator();

            while ( _i.hasNext() )
                {
                    EventChannelImpl _ec = ( EventChannelImpl ) ( ( Map.Entry ) _i.next() ).getValue();
                    _i.remove();
                    _ec.dispose();
                }
        }

        if (defaultFilterFactoryServant_ != null) {
            defaultFilterFactoryServant_.dispose();
        }

        applicationContext_.dispose();

        applicationContext_.getOrb().shutdown( true );
    }


    private void setUpDefaultFilterFactory(String filterFactoryConf)
        throws InvalidName
    {
        if (!filterFactoryConf.equals(Default.DEFAULT_FILTER_FACTORY))
        {
            try
            {
                if (logger_.isInfoEnabled())
                    logger_.info("try to set default_filter_factory to '" +
                                 filterFactoryConf + "'");

                defaultFilterFactory_ = FilterFactoryHelper.narrow
                    (getORB().string_to_object(filterFactoryConf));
            }
            catch (Throwable e)
            {
                logger_.error("Could not resolve FilterFactory: '"
                              + filterFactoryConf +
                              "'. Will default to builtin FilterFactory.", e);
            }
        }

        if (defaultFilterFactory_ == null)
        {
            if (logger_.isInfoEnabled())
                logger_.info("Create FilterFactory");

            defaultFilterFactoryServant_ =
                new FilterFactoryImpl( applicationContext_ );
            defaultFilterFactoryServant_.configure (config_);

            defaultFilterFactoryServant_.configure (config_);

            defaultFilterFactory_ =
                defaultFilterFactoryServant_._this( orb_ );
        }
    }


    public void preActivate()
    {
        defaultChannelContext_ = new ChannelContext();

        configure (((org.jacorb.orb.ORB)orb_).getConfiguration());

        defaultChannelContext_.setEventChannelFactory(EventChannelFactoryHelper.narrow(activate()));
    }


    public String getCorbaLoc()
    {
        return corbaLoc_;
    }


    private String createCorbaLoc( String poaName, byte[] id )
    {
        StringBuffer _corbaLoc = new StringBuffer( "corbaloc::" );

        _corbaLoc.append( getLocalAddress() );
        _corbaLoc.append( ":" );
        _corbaLoc.append( getLocalPort() );
        _corbaLoc.append( "/" );
        _corbaLoc.append( NOTIFICATION_SERVICE_SHORTCUT );

        return _corbaLoc.toString();
    }


    private int getLocalPort()
    {
        org.jacorb.orb.ORB jorb =
            (org.jacorb.orb.ORB)applicationContext_.getOrb();
        return jorb.getBasicAdapter().getPort();
    }


    private String getLocalAddress()
    {
        org.jacorb.orb.ORB jorb =
            (org.jacorb.orb.ORB)applicationContext_.getOrb();
        return jorb.getBasicAdapter().getAddress();
    }


    private String objectIdToHexString( byte[] objectId )
    {
        StringBuffer buffer = new StringBuffer();

        for ( int x = 0; x < objectId.length; ++x )
        {
            buffer.append( "%" );
            String hex = Integer.toHexString(objectId[ x ]).toUpperCase();

            if ( hex.length() == 1 )
                buffer.append( "0" );

            buffer.append( hex );
        }

        return buffer.toString();
    }


    public String getIOR()
    {
        return ior_;
    }


    public EventChannelFactory getEventChannelFactory()
    {
        return thisFactory_;
    }


    public ApplicationContext getApplicationContext()
    {
        return applicationContext_;
    }


    public Servant getServant() {
        return this;
    }


    public synchronized org.omg.CORBA.Object activate() {
        if (thisFactory_ == null) {
            try {
                byte[] oid = ( OBJECT_NAME.getBytes() );

                eventChannelFactoryPOA_.activate_object_with_id( oid, this );

                thisFactory_ =
                    JacORBEventChannelFactoryHelper.narrow( eventChannelFactoryPOA_.id_to_reference( oid ) );

                if (logger_.isDebugEnabled()) {
                    logger_.debug("activated EventChannelFactory with OID '"
                                  + new String(oid)
                                  + "' on '"
                                  + eventChannelFactoryPOA_.the_name()
                                  + "'" );
                }

                ior_ = orb_.object_to_string( eventChannelFactoryPOA_.id_to_reference( oid ) );

                corbaLoc_ = createCorbaLoc( eventChannelFactoryPOA_.the_name(), oid );

                ((org.jacorb.orb.ORB)orb_).addObjectKey(NOTIFICATION_SERVICE_SHORTCUT,
                                                        ior_);
            } catch (Exception e) {
                e.printStackTrace();

                throw new RuntimeException(e.getMessage());
            }
        }
        return thisFactory_;
    }


    private void throwPersistentNotSupported( String property ) throws UnsupportedQoS
    {
        Any _lowVal = applicationContext_.getOrb().create_any();
        Any _highVal = applicationContext_.getOrb().create_any();

        _lowVal.insert_short( BestEffort.value );
        _highVal.insert_short( BestEffort.value );

        UnsupportedQoS _e =
            new UnsupportedQoS( new PropertyError[] {
                new PropertyError( QoSError_code.UNSUPPORTED_VALUE,
                                   property,
                                   new PropertyRange( _lowVal, _highVal ) )
            } );

        throw _e;
    }


    private void throwBadValue( String property ) throws UnsupportedQoS
    {
        Any _lowVal = applicationContext_.getOrb().create_any();
        Any _highVal = applicationContext_.getOrb().create_any();

        _lowVal.insert_short( BestEffort.value );
        _highVal.insert_short( BestEffort.value );

        UnsupportedQoS _e =
            new UnsupportedQoS( "The specified Property Value is not supported",
                                new PropertyError[] {
                                    new PropertyError( QoSError_code.BAD_VALUE,
                                                       property,
                                                       new PropertyRange( _lowVal, _highVal ) ) } );
        throw _e;
    }


    public POA _default_POA()
    {
        return eventChannelFactoryPOA_;
    }


    public synchronized StaticEventChannelFactoryInfo get_static_info() {
        if (staticInfo_ == null) {
            staticInfo_ = new StaticEventChannelFactoryInfo();
            staticInfo_.corbaloc = getCorbaLoc();
            staticInfo_.filterfactory_running =
                (defaultFilterFactoryServant_ != null);
            staticInfo_.filterfactory_url = staticURL_;
            staticInfo_.hostname = getLocalAddress();
            staticInfo_.port = getLocalPort();
        }
        return staticInfo_;
    }


    private static void help()
    {
        System.out.println( "Usage: ntfy [-printIOR] [-printCorbaloc] " +
                            "[-writeIOR <filename>] " +
                            "[-registerName <nameId>[.<nameKind>]] " +
                            "[-port <oaPort>] [-channels <channels>] [-help]");
        System.exit( 0 );
    }


    public static EventChannelFactoryImpl newFactory() throws Exception
    {
        return newFactory( new String[ 0 ] );
    }


    public static EventChannelFactoryImpl newFactory( String[] args ) throws Exception
    {
        boolean doHelp = false;
        boolean doPrintIOR = false;
        boolean doPrintCorbaloc = false;
        String iorFileName = null;
        String oaPort = null;
        int channels = 0;
        String nameId = null;
        String nameKind = "";
        boolean doStartMemoryProfiler = false;

        // force Classloader to load Class PatternWrapper.
        // PatternWrapper may cause a ClassNotFoundException if
        // running on < JDK1.4 and gnu.regexp is NOT installed.
        // Therefor the Error should occur as _early_ as possible.
        //        PatternWrapper.class.getName();

        try
        {
            // process arguments
            for ( int i = 0; i < args.length; i++ )
            {
                if ( args[ i ].equals( "-printIOR" ) )
                {
                    doPrintIOR = true;
                }
                else if ( args[ i ].equals( "-printCorbaloc" ) )
                {
                    doPrintCorbaloc = true;
                }
                else if ( args[ i ].equals( "-help" ) )
                {
                    doHelp = true;
                }
                else if ( args[ i ].equals( "-port" ) )
                {
                    oaPort = args[ ++i ];
                }
                else if ( args[ i ].equals( "-channels" ) )
                {
                    channels = Integer.parseInt( args[ ++i ] );
                }
                else if ( args[ i ].equals( "-writeIOR" ) )
                {
                    iorFileName = args[ ++i ];
                }
                else if ( args[ i ].equals( "-registerName" ) )
                {
                    String name = args[ ++i ];

                    int index = name.indexOf( "." );

                    if ( name.lastIndexOf( "." ) != index )
                    {
                        throw new IllegalArgumentException
                            ( name +
                              ": argument to -registerName should be " +
                              "<nameId> or <nameId>.<nameKind>" );
                    }

                    if ( index != -1 )
                    {
                        nameId = name.substring( 0, index );
                        nameKind = name.substring( index + 1 );
                    }
                    else
                    {
                        nameId = name;
                    }
                }
                else if ( args[ i ].equals( "-memoryInfo" ) ) {
                    doStartMemoryProfiler = true;
                }
                else
                {
                    System.out.println( "Unknown argument: " + args[ i ] );
                    help();
                }
            }
        }
        catch ( ArrayIndexOutOfBoundsException e )
        {
            doHelp = true;
        }

        if ( doHelp )
        {
            help();
            System.exit( 0 );
        }

        // set ORB properties

        Properties props = new Properties();

        props.put( "jacorb.implname", STANDARD_IMPL_NAME );

        if ( oaPort != null )
        {
            props.put ( "OAPort", oaPort );
        }

        final ORB _orb = ORB.init( new String[ 0 ], props );

        Logger _logger =
            ((org.jacorb.orb.ORB)_orb).getConfiguration().
            getNamedLogger(EventChannelFactoryImpl.class.getName() + ".init");

        if (_logger.isDebugEnabled())
            _logger.debug("Starting ORB with Properties: " + props);

        EventChannelFactoryImpl _factory = new EventChannelFactoryImpl();

        _factory.setORB(_orb);

        _factory.preActivate();

        // force activation
        _factory.activate();

        for ( int i = 0; i < channels; i++ )
        {
            IntHolder ih = new IntHolder();
            _factory.create_channel( new Property[ 0 ], new Property[ 0 ], ih );
        }

        if ( doPrintIOR )
        {
            System.out.println( _factory.getIOR() );
        }

        if ( iorFileName != null )
        {
            _logger.info( "Writing IOR to file:" + iorFileName );

            try
            {
                PrintWriter out = new PrintWriter( new FileWriter( iorFileName ) );
                out.println( _factory.getIOR() );
                out.flush();
                out.close();
            }
            catch ( Exception e )
            {
                _logger.error("Could not write the IOR to file:" +
                              iorFileName, e );
            }
        }

        if ( nameId != null )
        {
            NamingContext namingContext =
                NamingContextHelper.narrow( _orb.resolve_initial_references( "NameService" ) );

            NameComponent[] name = new NameComponent[] {
                                       new NameComponent( nameId, nameKind )
                                   };

            if (_logger.isInfoEnabled()) {
                _logger.info( "namingContext.rebind("
                              + nameId
                              + ((nameKind != null && nameKind.length() > 0 )
                                 ? ( "." + nameKind ) : "" )
                              + " => "
                              + _factory.getCorbaLoc()
                              + ")" );
            }
            namingContext.rebind( name, _factory.getEventChannelFactory() );
        }

        if ( doPrintCorbaloc )
        {
            System.out.println( _factory.getCorbaLoc() );
        }


        Thread _orbThread = new Thread(
                       new Runnable()
                       {
                           public void run()
                           {
                               _orb.run();
                           }
                       }
                   );

        _orbThread.setName("Notification ORB Runner Thread");

        _orbThread.setDaemon( false );

        _orbThread.start();

//         if (doStartMemoryProfiler) {
//             MemoryProfiler.startProfiler();
//         }

        _logger.info("NotificationService up");

        return _factory;
    }


    public static void main( String[] args ) throws Exception
    {
        newFactory( args );
    }
}
