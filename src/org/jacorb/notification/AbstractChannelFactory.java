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

import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.interfaces.Disposable;
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
import org.omg.CORBA.Object;
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
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.CosNotifyFilter.FilterFactoryHelper;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.Servant;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;
import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.omg.CosNotifyChannelAdmin.EventChannelFactoryHelper;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AbstractChannelFactory
    implements ManageableServant,
               Disposable
{
    interface ShutdownCallback
    {
        void needTime( int time );
        void shutdownComplete();
    }

    ////////////////////////////////////////

    private static final String STANDARD_IMPL_NAME =
        "JacORB-NotificationService";

    private static final long SHUTDOWN_INTERVAL = 1000;

    public static final String EVENTCHANNEL_FACTORY_POA_NAME =
        "EventChannelFactoryPOA";

    ////////////////////////////////////////

    protected org.omg.CORBA.Object thisRef_;

    protected Logger logger_ = null;

    protected ChannelContext defaultChannelContext_ =
        new ChannelContext();

    protected POA rootPOA_;

    protected POA eventChannelFactoryPOA_;

    protected ApplicationContext applicationContext_;

    protected final SynchronizedBoolean filterFactoryStarted_ =
        new SynchronizedBoolean(false);

    ////////////////////////////////////////

    private Servant thisServant_;

    private String ior_;

    private String corbaLoc_;

    private ORB orb_;

    private FilterFactory defaultFilterFactory_;

    private FilterFactoryImpl defaultFilterFactoryServant_;

    private ChannelManager channelManager_ =
        new ChannelManager();

    private Configuration config_;

    private SynchronizedInt eventChannelIDPool_ =
        new SynchronizedInt(-1);

    private NameComponent[] registeredName_ = null;

    private NamingContext namingContext_;

    /**
     * the method that is executed when destroy is invoked.
     */
    private Runnable destroyMethod_ = new Runnable() {
            public void run() {
                dispose();
            }
        };

    ////////////////////////////////////////

    public AbstractChannelFactory()
    {
    }

    ////////////////////////////////////////

    protected abstract AbstractEventChannel newEventChannel();

    protected abstract org.omg.CORBA.Object create_abstract_channel(Property[] admin,
                                                                    Property[] qos,
                                                                    IntHolder id)
        throws UnsupportedAdmin,
               UnsupportedQoS;

    protected abstract String getObjectName();

    protected abstract String getShortcut();

    protected abstract Servant getServant();

    ////////////////////////////////////////

    protected int getLocalPort()
    {
        org.jacorb.orb.ORB jorb =
            (org.jacorb.orb.ORB)getORB();

        return jorb.getBasicAdapter().getPort();
    }


    protected String getLocalAddress()
    {
        org.jacorb.orb.ORB jorb =
            (org.jacorb.orb.ORB)getORB();

        return jorb.getBasicAdapter().getAddress();
    }


    private String createCorbaLoc( String poaName, byte[] id )
    {
        StringBuffer _corbaLoc = new StringBuffer( "corbaloc::" );

        _corbaLoc.append( getLocalAddress() );
        _corbaLoc.append( ":" );
        _corbaLoc.append( getLocalPort() );
        _corbaLoc.append( "/" );
        _corbaLoc.append( getShortcut() );

        return _corbaLoc.toString();
    }


    public synchronized org.omg.CORBA.Object activate() {
        if (thisRef_ == null) {
            try {
                byte[] oid = ( getObjectName().getBytes() );

                eventChannelFactoryPOA_.activate_object_with_id( oid, getServant() );

                thisRef_ = eventChannelFactoryPOA_.id_to_reference( oid );

                if (logger_.isDebugEnabled()) {
                    logger_.debug("activated EventChannelFactory with OID '"
                                  + new String(oid)
                                  + "' on '"
                                  + eventChannelFactoryPOA_.the_name()
                                  + "'" );
                }

                ior_ = getORB().object_to_string( eventChannelFactoryPOA_.id_to_reference( oid ) );

                corbaLoc_ = createCorbaLoc( eventChannelFactoryPOA_.the_name(), oid );

                ((org.jacorb.orb.ORB)getORB()).addObjectKey(getShortcut(),
                                                            ior_);

                defaultChannelContext_.setEventChannelFactory(this);
            } catch (Exception e) {
                e.printStackTrace();

                throw new RuntimeException(e.getMessage());
            }
        }
        return thisRef_;
    }


    public void setDestroyMethod(Runnable destroyMethod) {
        destroyMethod_ = destroyMethod;
    }


    // Implementation of org.jacorb.notification.servant.ManageableServant

    public void setORB( ORB orb)
    {
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


    protected ORB getORB() {
        return orb_;
    }



    public void setPOA( POA POA)
    {

    }


    public final void deactivate()
    {
        try {
            eventChannelFactoryPOA_.deactivate_object(eventChannelFactoryPOA_.servant_to_id(getServant()));
        } catch (Exception e) {
            logger_.fatalError("unable to deactivate object", e);

            throw new RuntimeException();
        }
    }


    public void preActivate() throws Exception
    {
    }


    public final void configure (Configuration conf) throws ConfigurationException
    {
        config_ = conf;

        logger_ = ((org.jacorb.config.Configuration)conf).getNamedLogger(getClass().getName());

        defaultChannelContext_.configure(conf);

        applicationContext_.configure (conf);

        String _filterFactoryConf =
            conf.getAttribute(Attributes.FILTER_FACTORY,
                              Default.DEFAULT_FILTER_FACTORY);

        try {
            setUpDefaultFilterFactory(_filterFactoryConf);
        } catch (InvalidName ex) {
            logger_.error("FilterFactory setup failed", ex);

            throw new ConfigurationException(Attributes.FILTER_FACTORY);
        }
    }


    private void setUpDefaultFilterFactory(String filterFactoryConf)
        throws InvalidName, ConfigurationException
    {
        if (!filterFactoryConf.equals(Default.DEFAULT_FILTER_FACTORY))
        {
            try
            {
                if (logger_.isInfoEnabled()) {
                    logger_.info("try to set default_filter_factory to '" +
                                 filterFactoryConf + "'");
                }

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
            // force Classloader to load Class PatternWrapper.
            // PatternWrapper may cause a ClassNotFoundException if
            // running on < JDK1.4 and gnu.regexp is NOT installed.
            // Therefor the Error should occur as _early_ as possible.
            PatternWrapper.class.getName();

            logger_.info("Create FilterFactory");

            defaultFilterFactoryServant_ =
                new FilterFactoryImpl( applicationContext_ );

            defaultFilterFactoryServant_.configure (config_);

            defaultFilterFactory_ =
                defaultFilterFactoryServant_._this( orb_ );

            filterFactoryStarted_.set(true);
        }
    }


    protected Configuration getConfiguration() {
        return config_;
    }


    protected FilterFactory getDefaultFilterFactory() {
        return defaultFilterFactory_;
    }


    public void dispose()
    {
        try {
            unregisterName();
        } catch (Exception e) {
            logger_.error("unable to unregister NameService registration", e);
        }

        channelManager_.dispose();

        if (defaultFilterFactoryServant_ != null) {
            defaultFilterFactoryServant_.dispose();
        }

        applicationContext_.dispose();

        getORB().shutdown( true );
    }


    protected void addToChannels(int id, AbstractEventChannel channel) {
        channelManager_.add_channel(id, channel);
    }


    protected int[] getAllChannels() {
        return channelManager_.get_all_channels();
    }


    protected AbstractEventChannel get_event_channel_servant( int id )
        throws ChannelNotFound
    {
        return channelManager_.get_channel_servant(id);
    }


    protected Iterator getChannelIterator() {
        return channelManager_.getChannelIterator();
    }


    protected AbstractEventChannel create_channel_servant(IntHolder id,
                                                          Property[] qosProps,
                                                          Property[] adminProps )
        throws UnsupportedAdmin,
               UnsupportedQoS,
               ConfigurationException
    {
        // create identifier
        int _channelID = createChannelIdentifier();
        id.value = _channelID;

        if (logger_.isInfoEnabled() ) {
            logger_.debug( "create channel_servant id=" + _channelID );
        }

        // check QoS and Admin Settings

        AdminPropertySet _adminSettings =
            new AdminPropertySet(getConfiguration());

        _adminSettings.set_admin( adminProps );

        QoSPropertySet _qosSettings =
            new QoSPropertySet( getConfiguration(), QoSPropertySet.ADMIN_QOS);

        _qosSettings.set_qos(qosProps);

        if (logger_.isDebugEnabled() )
        {
            logger_.debug( "uniqueQoSProps: " + _qosSettings );
            logger_.debug( "uniqueAdminProps: " + _adminSettings );
        }

        checkQoSSettings(_qosSettings);

        // create channel context
        ChannelContext _channelContext = (ChannelContext)defaultChannelContext_.clone();

        _channelContext.setORB(getORB());

        _channelContext.setPOA(rootPOA_);

        _channelContext.setMessageFactory(applicationContext_.
                                          getMessageFactory());

        _channelContext.setTaskProcessor(applicationContext_.
                                         getTaskProcessor());


        EventQueueFactory _factory = new EventQueueFactory();


        _factory.configure( ( (org.jacorb.orb.ORB)getORB() ).getConfiguration() );

        _channelContext.setEventQueueFactory(_factory);

        AbstractEventChannel _eventChannelServant = newEventChannel();

        _eventChannelServant.setDefaultFilterFactory( getDefaultFilterFactory() );

        _channelContext.resolveDependencies(_eventChannelServant);

        try {
            org.jacorb.orb.ORB jorb =
                (org.jacorb.orb.ORB)getORB();
            _eventChannelServant.configure (jorb.getConfiguration());
        } catch (Throwable ex) {
            ex.printStackTrace();

            throw new RuntimeException(ex);
        }

        _eventChannelServant.setKey(_channelID);
        _eventChannelServant.set_qos(_qosSettings.toArray());
        _eventChannelServant.set_admin(_adminSettings.toArray());
        _eventChannelServant.setORB(getORB());
        _eventChannelServant.setPOA(rootPOA_);

        return _eventChannelServant;
    }


    private int createChannelIdentifier()
    {
        return eventChannelIDPool_.increment();
    }


    private void checkQoSSettings(PropertySet uniqueQoSProperties)
        throws UnsupportedQoS
    {
        if ( uniqueQoSProperties.containsKey( EventReliability.value ) )
        {
            short _eventReliabilty =
                uniqueQoSProperties.get(EventReliability.value).
                extract_short();

            switch (_eventReliabilty)
            {
            case BestEffort.value:
                logger_.info("EventReliability=BestEffort");
                break;

            case Persistent.value:
                throwPersistentNotSupported( EventReliability.value );

                // fallthrough
            default:
                throwBadValue( EventReliability.value );
            }
        }

        short _connectionReliability = BestEffort.value;

        if ( uniqueQoSProperties.containsKey( ConnectionReliability.value ) )
        {
            _connectionReliability =
                uniqueQoSProperties.get( ConnectionReliability.value ).
                extract_short();

            switch ( _connectionReliability )
            {
            case BestEffort.value:
                logger_.info("ConnectionReliability=BestEffort");
                break;

            case Persistent.value:
                throwPersistentNotSupported( ConnectionReliability.value );

                break; // to satisfy compiler
            default:
                throwBadValue( ConnectionReliability.value );
            }
        }
    }


    private void throwPersistentNotSupported( String property ) throws UnsupportedQoS
    {
        Any _lowVal = getORB().create_any();
        Any _highVal = getORB().create_any();

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
        Any _lowVal = getORB().create_any();
        Any _highVal = getORB().create_any();

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

                    destroyMethod_.run();
                }
            };
        _shutdown.start();
    }


    /**
     * shutdown is called by the Java Wrapper
     */
    public void shutdown( ShutdownCallback cb )
    {
        // estimate shutdown time.
        // during shutdown disconnect must be called on every
        // connected client. in worst case the client is not
        // acccessible anymore and disconnect raises TRANSIENT. as
        // this could take some time request some more time from the
        // WrapperManager who is initiating the shutdown.

        int _numberOfClients = 0;

        Iterator i = getChannelIterator();

        while (i.hasNext()) {
            AbstractEventChannel _channel = (AbstractEventChannel)((Map.Entry)i.next()).getValue();

            _numberOfClients += _channel.getNumberOfConnectedClients();
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


    public String getIOR()
    {
        return ior_;
    }


    public String getCorbaLoc()
    {
        return corbaLoc_;
    }


    private static AbstractChannelFactory newChannelFactory(boolean typed) {
        if (typed) {
            return new TypedEventChannelFactoryImpl();
        } else {
            return new EventChannelFactoryImpl();
        }
    }


    public static AbstractChannelFactory newFactory(final ORB orb,
                                                    boolean startThread,
                                                    Properties props)
        throws Exception {

        AbstractChannelFactory _factory =
            newChannelFactory("on".equals(props.get(Attributes.ENABLE_TYPED_CHANNEL)));

        _factory.setORB(orb);

        _factory.configure(((org.jacorb.orb.ORB)orb).getConfiguration());

        _factory.preActivate();

        // force activation
        _factory.activate();

        _factory.printIOR(props);

        _factory.printCorbaLoc(props);

        _factory.writeFile(props);

        _factory.registerName(props);

        _factory.startChannels(props);

        if (startThread) {
            Thread _orbThread = new Thread(
                                           new Runnable()
                                           {
                                               public void run()
                                               {
                                                   orb.run();
                                               }
                                           }
                                           );

            _orbThread.setName("Notification ORB Runner Thread");

            _orbThread.setDaemon( false );

            _orbThread.start();
        }

        return _factory;
    }


    public static AbstractChannelFactory newFactory(Properties props) throws Exception {
        props.put( "jacorb.implname", STANDARD_IMPL_NAME );

        ORB _orb = ORB.init(new String[] {}, props);

        return newFactory(_orb, true, props);
    }


    private void registerName(Properties props) throws Exception {
        registerName(props.getProperty(Attributes.REGISTER_NAME_ID),
                     props.getProperty(Attributes.REGISTER_NAME_KIND, ""));
    }


    private synchronized void registerName(String nameId,
                                           String nameKind)
        throws Exception
    {
        if ( nameId == null ) {
            throw new ConfigurationException(Attributes.REGISTER_NAME_ID + "is null. This attributes needs to be non null if a reference should be registered in the NameService.");
        }

        namingContext_ =
            NamingContextHelper.narrow( getORB().resolve_initial_references( "NameService" ) );

        if (namingContext_ == null) {
            throw new ConfigurationException("could not resolve initial reference 'NameService'");
        }

        NameComponent[] _name = new NameComponent[] {
            new NameComponent( nameId, nameKind )
        };

        if (logger_.isInfoEnabled()) {
            logger_.info( "namingContext.rebind("
                          + nameId
                          + ((nameKind != null && nameKind.length() > 0 )
                             ? ( "." + nameKind ) : "" )
                          + " => "
                          + getCorbaLoc()
                              + ")" );
        }

        namingContext_.rebind( _name, thisRef_ );

        registeredName_ = _name;
    }


    private synchronized void unregisterName() throws Exception {
        if (namingContext_ != null) {
            if (registeredName_ != null) {
                namingContext_.unbind(registeredName_);

                registeredName_ = null;
            }
        }
    }


    private void startChannels(Properties props) throws UnsupportedQoS, UnsupportedAdmin {
        if (props.containsKey(Attributes.START_CHANNELS)) {
            startChannels(Integer.parseInt((String)props.get(Attributes.START_CHANNELS)));
        }
    }


    private void startChannels(int channels) throws UnsupportedQoS, UnsupportedAdmin {
        for ( int i = 0; i < channels; i++ )
            {
                IntHolder ih = new IntHolder();
                create_abstract_channel( new Property[ 0 ], new Property[ 0 ], ih );
            }
    }


    private void printIOR(Properties props) {
        if ("on".equals(props.get(Attributes.PRINT_IOR))) {
            System.out.println(getIOR());
        }
    }


    private void printCorbaLoc(Properties props) {
        if ("on".equals(props.get(Attributes.PRINT_CORBALOC))) {
            System.out.println(getCorbaLoc());
        }
    }


    private void writeFile(Properties props) {
        String _iorFileName = (String)props.get(Attributes.IOR_FILE);

        if (_iorFileName != null) {
            try
                {
                    PrintWriter out = new PrintWriter( new FileWriter( _iorFileName ) );
                    out.println( getIOR() );
                    out.flush();
                    out.close();
                }
            catch ( Exception e )
                {
                    e.printStackTrace();
                }
        }
    }
}
