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

import org.omg.CORBA.IntHolder;
import org.omg.CosNotifyChannelAdmin.ChannelNotFound;
import org.omg.CosNotification.UnsupportedAdmin;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import java.util.Map;
import java.util.Hashtable;
import org.omg.CosNotifyChannelAdmin.EventChannelFactoryPOA;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.apache.log.Logger;
import org.omg.CosNotification.EventReliability;
import org.omg.CORBA.Any;
import org.omg.CosNotification.BestEffort;
import org.omg.CosNotification.Persistent;
import org.omg.CosNotification.PropertyError;
import org.omg.CosNotification.QoSError_code;
import org.omg.CosNotification.PropertyRange;
import org.omg.CosNotification.ConnectionReliability;
import org.apache.log.Hierarchy;
import org.apache.log.Priority;
import org.apache.log.format.PatternFormatter;
import org.apache.log.output.io.StreamTarget;
import org.apache.log.LogTarget;
import java.io.File;
import org.apache.log.output.io.FileTarget;
import java.io.IOException;
import org.apache.log.output.io.SafeFileTarget;

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
 *
 * Created: Thu Oct 03 23:54:41 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class EventChannelFactoryImpl extends EventChannelFactoryPOA
{

    private static Object[] INTEGER_ARRAY_TEMPLATE = new Integer[ 0 ];

    protected POA poa_;
    protected ORB orb_;
    protected EventChannelFactory thisFactory_;
    protected FilterFactory defaultFilterFactory_;
    protected ApplicationContext applicationContext_;
    protected ChannelContext channelContextTemplate_;
    protected int counter_ = 0;
    protected Map allChannels_;
    protected PropertyValidator propertyValidator_;
    protected Logger logger_;

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
	throws UnsupportedAdmin, UnsupportedQoS
    {

        logger_.info( "create_channel()" );

        EventChannel _channel = null;

        EventChannelImpl _channelServant = create_channel_servant( qualitiyOfServiceProperties,
                                           administrativeProperties );


        _channel = _channelServant._this( applicationContext_.getOrb() );

        // create identifier
        int _identifier = createChannelIdentifier();
        channelIdentifier.value = _identifier;
        Integer _key = new Integer( _identifier );

        logger_.info( "created new Channel with id: " + _identifier );

        allChannels_.put( _key, _channel );

        return _channel;
    }

    EventChannelImpl create_channel_servant( Property[] qualitiyOfServiceProperties,
            Property[] administrativeProperties )

    throws UnsupportedAdmin,
                UnsupportedQoS
    {

        logger_.info( "create_channel_servant()" );

        propertyValidator_.checkAdminPropertySeq( administrativeProperties );
        propertyValidator_.checkQoSPropertySeq( qualitiyOfServiceProperties );

        Map _uniqueAdminProperties = propertyValidator_.getUniqueProperties( administrativeProperties );
        Map _uniqueQoSProperties = propertyValidator_.getUniqueProperties( qualitiyOfServiceProperties );

        if ( _uniqueQoSProperties.containsKey( EventReliability.value ) )
        {

            short _eventReliabilty =
                ( ( Any ) _uniqueQoSProperties.get( EventReliability.value ) ).extract_short();

            switch ( _eventReliabilty )
            {

            case BestEffort.value:
                break;

            case Persistent.value:
                throwPersistentNotSupported( EventReliability.value );

            default:
                throwBadValue( EventReliability.value );

            }
        }

        if ( _uniqueQoSProperties.containsKey( ConnectionReliability.value ) )
        {

            short _connectionReliability =
                ( ( Any ) _uniqueQoSProperties.get( ConnectionReliability.value ) ).extract_short();

            switch ( _connectionReliability )
            {

            case BestEffort.value:
                break;

            case Persistent.value:
                throwPersistentNotSupported( ConnectionReliability.value );

            default:
                throwBadValue( ConnectionReliability.value );

            }
        }

        ChannelContext _channelContext;

        _channelContext = ( ChannelContext ) channelContextTemplate_.clone();

        // create new servant
        EventChannelImpl _channelServant = new EventChannelImpl( applicationContext_,
                                           _channelContext,
                                           _uniqueQoSProperties,
                                           _uniqueAdminProperties );

        return _channelServant;
    }

    protected int createChannelIdentifier()
    {
        return ++counter_;
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
        Integer[] _keys = ( Integer[] ) allChannels_.keySet().toArray( INTEGER_ARRAY_TEMPLATE );

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
    public EventChannel get_event_channel( int n ) throws ChannelNotFound
    {
        return ( EventChannel ) allChannels_.get( new Integer( n ) );
    }

    ////////////////////////////////////////

    private void initialize() throws InvalidName {
        logger_ = Hierarchy.getDefaultHierarchy().getLoggerFor( getClass().getName() );

        allChannels_ = new Hashtable();
        thisFactory_ = this._this( applicationContext_.getOrb() );

        FilterFactoryImpl _filterFactoryServant = new FilterFactoryImpl( applicationContext_ );

        defaultFilterFactory_ = _filterFactoryServant._this( applicationContext_.getOrb() );

        channelContextTemplate_ = new ChannelContext();
        channelContextTemplate_.setDefaultFilterFactory( defaultFilterFactory_ );
        channelContextTemplate_.setEventChannelFactoryServant( this );
        channelContextTemplate_.setEventChannelFactory( thisFactory_ );

        propertyValidator_ = applicationContext_.getPropertyValidator();
    }

    public EventChannelFactoryImpl() throws Exception {
        ORB _orb = ORB.init( new String[0], null );
        POA _poa = POAHelper.narrow( _orb.resolve_initial_references( "RootPOA" ) );

	setLogLevel( "org.jacorb.notification", Priority.DEBUG );

        applicationContext_ = new ApplicationContext( _orb, _poa );

	initialize();
    }

    public EventChannelFactoryImpl( ApplicationContext applicationContext ) throws InvalidName
    {
        applicationContext_ = applicationContext;

	initialize();
    }

    void throwPersistentNotSupported( String property ) throws UnsupportedQoS
    {
        Any _lowVal = orb_.create_any();
        Any _highVal = orb_.create_any();

        _lowVal.insert_short( BestEffort.value );
        _highVal.insert_short( BestEffort.value );

        UnsupportedQoS _e =
            new UnsupportedQoS( new PropertyError[] {new PropertyError( QoSError_code.UNSUPPORTED_VALUE,
                                property,
                                new PropertyRange( _lowVal, _highVal ) ) } );
    }

    void throwBadValue( String property ) throws UnsupportedQoS
    {
        Any _lowVal = orb_.create_any();
        Any _highVal = orb_.create_any();

        _lowVal.insert_short( BestEffort.value );
        _highVal.insert_short( BestEffort.value );

        UnsupportedQoS _e =
            new UnsupportedQoS( "The specified Property Value is not supported",
                                new PropertyError[] {new PropertyError( QoSError_code.BAD_VALUE,
                                                                        property,
                                                                        new PropertyRange( _lowVal, _highVal ) ) } );
    }

    static void setLogLevel( String logger, Priority priority ) throws IOException 
    {
	String path = "/home/alf/notify.log";
	File _file = new File(path);

        Logger _logger =
            Hierarchy.
            getDefaultHierarchy().
            getLoggerFor( logger );

        String _pattern = "%7.7{priority} [%-.25{category}] "
                          + "(%{context}): %{message}\\n%{throwable}";

        PatternFormatter _formatter = new PatternFormatter( _pattern );

	FileTarget _fileTarget = new SafeFileTarget(_file, true, _formatter);
        StreamTarget _consoleTarget = new StreamTarget( System.out, _formatter );

	LogTarget _target = _consoleTarget;

        _logger.setLogTargets( new LogTarget[] {_target} );

        _logger.setPriority( priority );
    }

    public static void main( String[] args ) throws Exception
    {
        ORB _orb = ORB.init( args, null );
        POA _poa = POAHelper.narrow( _orb.resolve_initial_references( "RootPOA" ) );

	//	setLogLevel( "org.jacorb.notification", Priority.NONE );
	setLogLevel("org.jacorb.notification", Priority.INFO);

        ApplicationContext _appContext = new ApplicationContext( _orb, _poa );

        EventChannelFactoryImpl _factory =
            new EventChannelFactoryImpl( _appContext );

        _poa.the_POAManager().activate();

        org.omg.CORBA.Object _o = _poa.servant_to_reference( _factory );

        NamingContextExt _nc =
            NamingContextExtHelper.narrow( _orb.resolve_initial_references( "NameService" ) );

        String _factoryName = "NotificationService";

        _nc.rebind( _nc.to_name( _factoryName ), _o );

        _orb.run();
    }



} // NotificationChannelFactoryImpl
