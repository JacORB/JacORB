package org.jacorb.orb;

/*
 *        JacORB  - a free Java ORB
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

import java.util.*;
import java.io.*;
import java.applet.Applet;
import java.lang.reflect.*;

import org.jacorb.util.*;
import org.jacorb.orb.policies.*;
import org.jacorb.orb.dii.Request;
import org.jacorb.orb.connection.*;
import org.jacorb.orb.portableInterceptor.*;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.Messaging.*;
import org.omg.PortableInterceptor.*;
import org.omg.IOP.*;
import org.omg.IIOP.*;

/**
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 */

public final class ORB
    extends ORBSingleton
    implements org.jacorb.poa.POAListener
{
    private static final String versionString = org.jacorb.util.Version.version;
    private static final String dateString = org.jacorb.util.Version.date;
    private static final String nullIORString =
        "IOR:00000000000000010000000000000000";

    /** "initial" references */
    private Hashtable initial_references = new Hashtable();

    private org.jacorb.poa.POA rootpoa;
    private org.jacorb.poa.Current poaCurrent;
    private BasicAdapter basicAdapter;
    private org.omg.SecurityLevel2.Current securityCurrent = null;

    /** interceptor handling */
    private InterceptorManager interceptor_manager = null;
    private boolean hasClientInterceptors = false;
    private boolean hasServerInterceptors = false;
    private org.omg.PortableInterceptor.Current piCurrent = new PICurrent();

    /** reference caching */
    private Hashtable knownReferences = null;

    /** connection mgmt. */
    private ClientConnectionManager clientConnectionManager;

    /** The transport manager*/
    private TransportManager transport_manager = null;

    private GIOPConnectionManager giop_connection_manager = null;

    /** buffer mgmt. */
    private BufferManager bufferManager = BufferManager.getInstance();

    /**
     * Maps repository ids (strings) to objects that implement
     * org.omg.CORBA.portable.ValueFactory.  This map is used by
     * register/unregister_value_factory() and lookup_value_factory().
     */
    protected Map valueFactories = new HashMap();

    private Map objectKeyMap = new HashMap();

    /** properties */

    private java.util.Properties _props;

    /** command like args */
    public String[] _args;

    public java.applet.Applet applet;

    /* for run() and shutdown()  */
    private Object orb_synch = new java.lang.Object();
    private boolean run = true;
    private boolean wait = true;
    private boolean shutdown_in_progress = false;
    private boolean destroyed = false;
    private Object shutdown_synch = new Object();

    /** do we always add GIOP 1.0 profiles in GIOP 1.2 IORs ? */
    private boolean always_add_1_0_Profile = false;

    /* for registering POAs with the ImR */
    private ImRAccess imr = null;
    private int persistentPOACount;

    public static final String orb_id = "jacorb:" + org.jacorb.util.Version.version;

    /* outstanding dii requests awaiting completion */
    private Set requests = Collections.synchronizedSet( new HashSet() );
    /* most recently completed dii request found during poll */
    private Request request = null;

    /* policy factories, from portable interceptor spec */
    private Hashtable policy_factories = null;

    private static org.omg.CORBA.TCKind kind;

    private static final String [] services  =
        {"RootPOA","POACurrent", "DynAnyFactory", "PICurrent", "CodecFactory"};

    private boolean bidir_giop = false;


    public ORB()
    {
    }

    /**
     * @overwrites id() in org.omg.CORBA_2_5.ORB
     */

    public String id()
    {
        return orb_id;
    }

    public boolean useBiDirGIOP()
    {
        return bidir_giop;
    }

    public void turnOnBiDirGIOP()
    {
        if( ! bidir_giop )
        {
            bidir_giop = true;

            clientConnectionManager.setRequestListener( basicAdapter.getRequestListener() );
        }
    }

    /**
     *  This  version of _getObject  is used for references  that have
     *  arrived over the network and is called from CDRInputStream. It
     *  removes stale cache entries
     */
    public synchronized org.omg.CORBA.Object _getObject( ParsedIOR pior )
    {
        String key = pior.getIORString();
        org.omg.CORBA.portable.ObjectImpl o =
            (org.omg.CORBA.portable.ObjectImpl)knownReferences.get( key );

        if( o != null )
        {
            //            Debug.output( 5, "Found a reference for key " + key + " in cache ");
            org.jacorb.orb.Delegate del = (org.jacorb.orb.Delegate) o._get_delegate();
            if (del != null)
            {
                ParsedIOR delpior= del.getParsedIOR();
                if (delpior == null)
                {
                    knownReferences.remove(key);
                    Debug.output(4,"Removing an invalid reference from cache.");
                }
                else if( pior.getIIOPAddress().equals(delpior.getIIOPAddress()))
                {
                    return o._duplicate();
                }
            }
            else
            {
                Debug.output(3,"remove stale reference  from cache ");
                knownReferences.remove( key );
            }
        }

        org.jacorb.orb.Delegate d = new Delegate(this, pior );
        o = d.getReference( null );

        if( Environment.cacheReferences() )
        {
            // Debug.output(5,"Caching reference for key " + key);
            knownReferences.put( key, o );
        }
        return o;
    }

    /**
     * Find a local POA for a delegate (called from is_local())
     * returns non-null only if a root POA is already activated
     * and all POAs along the path on the poa name are active, i.e.
     * returns null for POAs in the holding state
     */

    org.jacorb.poa.POA findPOA( org.jacorb.orb.Delegate d,
                                org.omg.CORBA.Object ref )
    {
        // if no POAs activated, we don't look further
        if( rootpoa == null || basicAdapter == null )
        {
            // Debug.output(3, "ORB.findPOA: no local root/base adapters");
            return null;
        }

//          if( ! (basicAdapter.getAddress() +":"+ basicAdapter.getPort()).equals( d.get_adport() ))
//          {
//              Debug.output(3, "ORB.findPOA: wrong base adapter address "  + d.get_adport() +
//                           " vs. " + basicAdapter.getAddress() +":"+ basicAdapter.getPort()+ " !");
//              return null;
//          }

        String refImplName = null;
        String orbImplName = Environment.getProperty( "jacorb.implname", "" );

        try
        {
            refImplName =
                org.jacorb.poa.util.POAUtil.extractImplName( d.getObjectKey() );
        }
        catch( org.jacorb.poa.except.POAInternalError pie )
        {
            Debug.output(3, "ORB.findPOA: reference generated by foreign POA");
            return null;
        }

        if( refImplName == null )
        {
            if( orbImplName.length() > 0 )
            {
                Debug.output(3, "ORB.findPOA: impl_name mismatch");
                return null;
            }
        }
        else
        {
            if( !(orbImplName.equals( refImplName ) ))
            {
                Debug.output(3, "ORB.findPOA: impl_name mismatch");
                return null;
            }
        }


        try
        {
            org.jacorb.poa.POA tmp_poa = (org.jacorb.poa.POA)rootpoa;
            String poa_name =
                org.jacorb.poa.util.POAUtil.extractPOAName( d.getObjectKey() );

            /* strip scoped poa name (first part of the object key before "::",
             *  will be empty for the root poa
             */

            java.util.StringTokenizer strtok =
                new java.util.StringTokenizer( poa_name,
                                               org.jacorb.poa.POAConstants.OBJECT_KEY_SEPARATOR );

            String scopes[] = new String[strtok.countTokens()];

            for( int i = 0; strtok.hasMoreTokens(); scopes[i++] = strtok.nextToken() );

            for( int i = 0; i < scopes.length; i++)
            {
                if( scopes[i].equals(""))
                    break;

                /* the following is a  call to a method in the private
                   interface between the ORB  and the POA. It does the
                   necessary    synchronization    between   incoming,
                   potentially concurrent  requests to activate  a POA
                   using its  adapter activator. This  call will block
                   until  the correct  POA is  activated and  ready to
                   service    requests.    Thus,    concurrent   calls
                   originating  from a  single,  multi-threaded client
                   will be serialized  because the thread that accepts
                   incoming  requests  from   the  client  process  is
                   blocked.  Concurrent  calls from other destinations
                   are not  serialized unless they  involve activating
                   the same adapter.  */

                try
                {
                    tmp_poa = tmp_poa._getChildPOA( scopes[i] );
                }
                catch ( org.jacorb.poa.except.ParentIsHolding p )
                {
                    Debug.output(3, "ORB.findPOA: holding adapter");
                    return null;
                }
            }
            byte[] objectId =
                org.jacorb.poa.util.POAUtil.extractOID( ref );

            if( tmp_poa.isSystemId()
               && ! tmp_poa.previouslyGeneratedObjectId( objectId ))
            {
                Debug.output(3, "ORB.findPOA: not a previously generated object key.");
                return null;
            }

            return tmp_poa;
        }
        catch( Exception e )
        {
            Debug.output( 2, e); // TODO
        }

        Debug.output(3, "ORB.findPOA: nothing found");
        return null;
    }


    public ClientConnectionManager getClientConnectionManager()
    {
        return clientConnectionManager;
    }

    public GIOPConnectionManager getGIOPConnectionManager()
    {
        return giop_connection_manager;
    }


    synchronized void _release( org.jacorb.orb.Delegate delegate )
    {
        knownReferences.remove( delegate.getParsedIOR().getIORString() );
    }


    /**
     * This method creates a policy  with the given type and the given
     * value.
     *
     * @param type The policies type.
     * @param value The policies value.
     * @exception org.omg.CORBA.PolicyError There is no PolicyFactory for the
     * given type or the policy creation failed.
     * @see org.omg.PortableInterceptor.PolicyFactory
     */

    public org.omg.CORBA.Policy create_policy( int type, org.omg.CORBA.Any value )
        throws org.omg.CORBA.PolicyError
    {
        switch (type)
        {
            case MAX_HOPS_POLICY_TYPE.value:
                return new
                    org.jacorb.orb.policies.MaxHopsPolicy (value);
            case QUEUE_ORDER_POLICY_TYPE.value:
                return new
                    org.jacorb.orb.policies.QueueOrderPolicy (value);
            case REBIND_POLICY_TYPE.value:
                return new
                    org.jacorb.orb.policies.RebindPolicy (value);
            case RELATIVE_REQ_TIMEOUT_POLICY_TYPE.value:
              return new
                org.jacorb.orb.policies.RelativeRequestTimeoutPolicy (value);
            case RELATIVE_RT_TIMEOUT_POLICY_TYPE.value:
              return new
                org.jacorb.orb.policies.RelativeRoundtripTimeoutPolicy (value);
            case REPLY_END_TIME_POLICY_TYPE.value:
                return new
                    org.jacorb.orb.policies.ReplyEndTimePolicy (value);
            case REPLY_PRIORITY_POLICY_TYPE.value:
                return new
                    org.jacorb.orb.policies.ReplyPriorityPolicy (value);
            case REPLY_START_TIME_POLICY_TYPE.value:
                return new
                    org.jacorb.orb.policies.ReplyStartTimePolicy (value);
            case REQUEST_END_TIME_POLICY_TYPE.value:
                return new
                    org.jacorb.orb.policies.RequestEndTimePolicy (value);
            case REQUEST_PRIORITY_POLICY_TYPE.value:
                return new
                    org.jacorb.orb.policies.RequestPriorityPolicy (value);
            case REQUEST_START_TIME_POLICY_TYPE.value:
                return new
                    org.jacorb.orb.policies.RequestStartTimePolicy (value);
            case ROUTING_POLICY_TYPE.value:
                return new
                    org.jacorb.orb.policies.RoutingPolicy (value);
            case SYNC_SCOPE_POLICY_TYPE.value:
                return new
                    org.jacorb.orb.policies.SyncScopePolicy (value);
            default:
                Integer key = new Integer(type);
                if ( policy_factories == null ||
                     (! policy_factories.containsKey(key)) )
                    throw new org.omg.CORBA.PolicyError();
                PolicyFactory factory =
                    (PolicyFactory)policy_factories.get(key);
                return factory.create_policy(type, value);
        }
    }


    /**
     * Tests if a policy factory is present for the given type.
     */
    public boolean hasPolicyFactoryForType(int type)
    {
        return (policy_factories != null &&
                policy_factories.containsKey( new Integer(type)) );
    }

    public org.omg.CORBA.ContextList create_context_list()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ();
    }

    public org.omg.CORBA.Environment create_environment()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ();
    }

    public org.omg.CORBA.portable.OutputStream create_output_stream()
    {
        return new CDROutputStream(this);
    }

    /**
     * Called from within getReference (below) and set_policy_override
     * (in org.jacorb.orb.Delegate).
     *
     * Generates IOR 1.0 and 1.1, 1.1 contains also 1.0 profile. Only 1.1 and
     * greater profiles contain codeset information about this server. <br>
     * If present, IORInterceptors will be invoked.
     */

    org.omg.IOP.IOR createIOR( String repId,
                               byte[] key,
                               boolean _transient,
                               org.jacorb.poa.POA poa,
                               Hashtable policy_overrides)
    {
        // get the host and port of the server
        String address;
        int port;

        //the ImR is a special case
        if( repId.equals( "IDL:org/jacorb/imr/ImplementationRepository:1.0") )
        {
            address = getIMRAddressForIOR( basicAdapter.getAddress() );
            port = getIMRPortForIOR( basicAdapter.getPort() );
        }
        else
        {
            address = getServerAddress();
            port = getServerPort();
        }

        if( ! _transient &&
            Environment.useImR() &&
            Environment.useImREndpoint() )
        {
            //attempt to override the server IOR address with the ImR address
            try
            {
                if( imr == null )
                {
                    try
                    {
                        imr = (ImRAccess) Class.forName( "org.jacorb.imr.ImRAccessImpl" ).newInstance();
                        imr.connect( this );
                    }
                    catch( Exception e )
                    {
                        if( e instanceof org.omg.CORBA.INTERNAL )
                        {
                            throw (org.omg.CORBA.INTERNAL) e;
                        }
                        else
                        {
                            Debug.output( 1, e );
                            throw new org.omg.CORBA.INTERNAL( e.toString() );
                        }
                    }
                }

                // set host and port to ImR's values
                address = getIMRAddressForIOR( imr.getImRHost() );
                port = getIMRPortForIOR( imr.getImRPort() );
                Debug.output( 2, "New persistent IOR created with ImR at " +
                              address + ":" + port );
            }
            catch (Exception _e)
            {
                // host and port values of this ORB as set above are used
                Debug.output(6, _e);
            }
        }

        //find out GIOP minor version to use. Defaults to 2.

        int giop_minor = 2;
        String gm_str = Environment.getProperty( "jacorb.giop_minor_version", "2" );

        try
        {
            giop_minor = Integer.parseInt( gm_str );
        }
        catch( NumberFormatException nfe )
        {
            throw new Error( "Unable to create int from string >>" +
                             gm_str + "<<. " +
                             "(check property \"jacorb.giop_minor_version\")" );
        }

        // create any components to go in the profiles

        Vector components_iiop_profile = new Vector();
        Vector components_multi_profile = new Vector();

        // set the ORB type ID component to JacORB

        CDROutputStream orbIDComponentDataStream = new CDROutputStream( this );
        orbIDComponentDataStream.beginEncapsulatedArray();
        orbIDComponentDataStream.write_long( ORBConstants.JACORB_ORB_ID );

        TaggedComponent orbIDComponent = new TaggedComponent
        (
            TAG_ORB_TYPE.value,
            orbIDComponentDataStream.getBufferCopy()
        );

        if( giop_minor > 0 )
        {
            components_iiop_profile.addElement( orbIDComponent );
        }
        else
        {
            components_multi_profile.addElement( orbIDComponent );
        }

        //if IORInterceptors are present then invoke them

        if ((interceptor_manager != null) &&
            interceptor_manager.hasIORInterceptors())
        {
            IORInfoImpl info = new IORInfoImpl
            (
                this,
                poa,
                components_iiop_profile,
                components_multi_profile,
                policy_overrides
            );

            try
            {
                interceptor_manager.getIORIterator().iterate( info );
            }
            catch (Exception e)
            {
                Debug.output(2, e);
            }
        }

        //all components for the profiles have to be present by now

        TaggedProfile tp = null;
        Vector taggedProfileVector = new Vector();
        TaggedComponent[] components = null;
        CDROutputStream profileDataStream = null;

        switch( giop_minor )
        {
            case 2 :
            {
                //same as IIOP 1.1
            }
            case 1:
            {
                //create IIOP 1.1 profile

                components = new TaggedComponent[ components_iiop_profile.size() ];
                components_iiop_profile.copyInto( components );

                ProfileBody_1_1 pb1 = new ProfileBody_1_1
                (
                    new org.omg.IIOP.Version( (byte) 1, (byte) giop_minor ),
                    address,
                    (short) port,
                    key,
                    components
                );

                // serialize the profile id 1, leave idx 0 for v.1.0 profile
                profileDataStream = new CDROutputStream( this );
                profileDataStream.beginEncapsulatedArray();
                ProfileBody_1_1Helper.write( profileDataStream, pb1 );

                tp = new TaggedProfile
                (
                    TAG_INTERNET_IOP.value,
                    profileDataStream.getBufferCopy()
                );
                taggedProfileVector.addElement( tp );

                // fall through only if we want to be beckwards compatible
                // with GIOP 1.0 and thus always include an 1.0 profile

                if( ! always_add_1_0_Profile )
                    break;
            }
            case 0:
            {
                // create IIOP 1.0 profile
                ProfileBody_1_0 pb0 = new ProfileBody_1_0
                (
                    new org.omg.IIOP.Version( (byte) 1, (byte) 0 ),
                    address,
                    (short) port,
                    key
                );

                profileDataStream = new CDROutputStream( this );
                profileDataStream.beginEncapsulatedArray();
                ProfileBody_1_0Helper.write( profileDataStream, pb0 );

                tp = new TaggedProfile
                (
                    TAG_INTERNET_IOP.value,
                    profileDataStream.getBufferCopy()
                );
                taggedProfileVector.addElement( tp );
            }
        }

        // now fill the last IOR profile with components (if any)
        if( components_multi_profile.size() > 0 )
        {
            components = new TaggedComponent[ components_multi_profile.size() ];
            components_multi_profile.copyInto( components );

            profileDataStream = new CDROutputStream( this );
            profileDataStream.beginEncapsulatedArray();
            MultipleComponentProfileHelper.write( profileDataStream, components );

            tp = new TaggedProfile
            (
                TAG_MULTIPLE_COMPONENTS.value,
                profileDataStream.getBufferCopy()
            );
            taggedProfileVector.addElement( tp );
        }

        // copy the profiles into the IOR

        TaggedProfile[] tps = new TaggedProfile[ taggedProfileVector.size() ];
        taggedProfileVector.copyInto( tps );

        return new IOR( repId, tps );
    }


    public org.omg.CORBA.Context get_default_context ()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ();
    }


    /**
     * used from the POA
     * @returns the basic adapter used by this ORB instance
     */

    public org.jacorb.orb.BasicAdapter getBasicAdapter()
    {
        if( basicAdapter == null )
            throw new RuntimeException("Adapters not initialized, call POA.init() first");
        return basicAdapter;
    }


    /**
     * getPOACurrent
     */

    public org.jacorb.poa.Current getPOACurrent()
    {
        if (poaCurrent == null)
        {
            poaCurrent = org.jacorb.poa.Current._Current_init();
        }
        return poaCurrent;
    }

    /**
     * called by POA to create an IOR
     *
     * @param poa the calling POA
     * @param object_key
     * @param rep_id
     * @param _transient is the new reference transient or persistent
     * @return a new CORBA Object reference
     */

    public org.omg.CORBA.Object getReference( org.jacorb.poa.POA poa,
                                              byte[] object_key,
                                              String rep_id,
                                              boolean _transient )
    {
        try
        {
            if( rep_id == null )
                rep_id = "IDL:org.omg/CORBA/Object:1.0";

            org.omg.IOP.IOR ior =
                createIOR( rep_id, object_key, _transient, poa, null );

            Delegate d = new Delegate( this, ior );
            return d.getReference( poa );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        return null;
    }

    public org.jacorb.poa.POA getRootPOA()
        throws org.omg.CORBA.INITIALIZE
    {
        if( rootpoa == null )
        {
            rootpoa = org.jacorb.poa.POA._POA_init(this);
            rootpoa._addPOAEventListener( this );

            basicAdapter = new BasicAdapter( this,
                                             rootpoa,
                                             transport_manager,
                                             giop_connection_manager );

        }
        return rootpoa;
    }


    /**
     * @returns - true if ORB is initialized by an applet and
     * appligator use is switched on
     */

    public boolean isApplet()
    {
        return applet != null;
        //return applet != null && Environment.useAppligator();
    }


    public Applet getApplet()
    {
        return applet;
    }

    public String[] list_initial_services()
    {
        Vector v = new Vector();

        for( Enumeration e = initial_references.keys(); e.hasMoreElements(); v.add( e.nextElement() ) );

        String [] initial_services =
            new String[ services.length + v.size()];

        v.copyInto( initial_services );

        System.arraycopy( services, 0, initial_services, v.size(), services.length );
        return initial_services;
    }

    /**
     * An operation from the POAListener interface. Whenever a new POA is
     * created, the ORB is notified.
     */

    public void poaCreated( org.jacorb.poa.POA poa )
    {
        /*
         * Add this orb as the child poa's event listener. This means that the
         * ORB is always a listener to all poa events!
         */
        poa._addPOAEventListener( this );

        /* If the new POA has a persistent lifetime policy, it is registered
         * with the implementation repository if there is one and the
         * use_imr policy is set via the "jacorb.orb.use_imr" property
         */

        if( poa.isPersistent() )
        {
            persistentPOACount++;

            /* Lookup the implementation repository */

            if( imr == null && Environment.useImR() )
            {
                try
                {
                    imr = (ImRAccess) Class.forName( "org.jacorb.imr.ImRAccessImpl" ).newInstance();
                    imr.connect( this );
                }
                catch( Exception e )
                {
                    if( e instanceof org.omg.CORBA.INTERNAL )
                    {
                        throw (org.omg.CORBA.INTERNAL) e;
                    }
                    else
                    {
                        Debug.output( 1, e );
                        throw new org.omg.CORBA.INTERNAL( e.toString() );
                    }
                }
            }

            if( imr != null )
            {
                /* Register the POA */
                String server_name = new String( Environment.implName() );

                imr.registerPOA( server_name + "/" + poa._getQualifiedName(),
                                 server_name, // logical server name
                                 getServerAddress(),
                                 getServerPort() );
            }
        }
    }

    /*
     * Return the address to use to locate the server.  Note that this
     * address will be overwritten by the ImR address in the IOR of
     * persistent servers if the use_imr and use_imr_endpoint properties
     * are switched on.
     *
     * @return the address for the server
     */

    private String getServerAddress()
    {
        String address = Environment.getProperty( "jacorb.ior_proxy_host" );

        if( address == null )
        {
            //property not set
            address = basicAdapter.getAddress();
        }
        else
        {
            Debug.output( Debug.INFORMATION | Debug.ORB_MISC,
                          "Using proxy host " + address + " in IOR" );
        }

        return address;
    }

    /*
     * Return the port to use to locate the server.  Note that this
     * port will be overwritten by the ImR port in the IOR of
     * persistent servers if the use_imr and use_imr_endpoint properties
     * are switched on.
     *
     * @return the port for the server
     */

    private int getServerPort()
    {
        String port_str = Environment.getProperty( "jacorb.ior_proxy_port" );
        int port = -1;

        if( port_str != null )
        {
            try
            {
                port = Integer.parseInt( port_str );
            }
            catch( NumberFormatException nfe )
            {
                throw new Error( "Unable to create integer from string >>" +
                                 port_str + "<<. " +
                                 "(check property \"jacorb.ior_proxy_port\")" );
            }

            if( port < 0 )
            {
                throw new Error( "Negative port numbers are not allowed! " +
                                 "(check property \"jacorb.ior_proxy_port\")" );
            }

            Debug.output( Debug.INFORMATION | Debug.ORB_MISC,
                          "Using proxy port " + port + " in IOR" );
        }
        else
        {
            //property not set
            port = basicAdapter.getPort();
        }

        return port;
    }

    /**
     * Method to check if a Proxy host is configured for the IMR via the
     * <code>jacorb.imr.ior_proxy_host</code> property.
     * @param imrAddress The actual host name or IP of the IMR.
     * @return The proxy host value, if configured, else the supplied actual host.
     */
    private String getIMRAddressForIOR(String imrAddress)
    {
        String imrProxyHost = Environment.getProperty("jacorb.imr.ior_proxy_host");
        return (imrProxyHost == null ? imrAddress : imrProxyHost);
    }

    /**
     * Method to check if a Proxy port is configured for the IMR via the
     * <code>jacorb.imr.ior_proxy_port</code> property.
     * @param imrPort The port number that the IMR is really running on.
     * @return The proxy port number, if configured, else the supplied actual port.
     */
    private int getIMRPortForIOR(int imrPort)
    {
        String imrProxyPort = Environment.getProperty("jacorb.imr.ior_proxy_port");
        if (imrProxyPort != null)
        {
            try
            {
                imrPort = Integer.parseInt(imrProxyPort);
            }
            catch (NumberFormatException nfe)
            {
                Debug.output(2, "IMR Proxy Port is configured to a none integer value. " +
                            "Check the value of property \"jacorb.imr.ior_proxy_port");
            }
        }
        return imrPort;
    }

    public void poaStateChanged(org.jacorb.poa.POA poa, int new_state)
    {
        if( ( new_state == org.jacorb.poa.POAConstants.DESTROYED ||
              new_state == org.jacorb.poa.POAConstants.INACTIVE )  &&
            poa.isPersistent() && imr != null
            )
        {
            /* if all persistent POAs in this server have gone down, unregister
               the server */
            if( --persistentPOACount == 0 )
            {
                imr.setServerDown(new String(Environment.implName()));
            }
        }
    }


    public void referenceCreated(org.omg.CORBA.Object o) {}

    public boolean get_service_information( short service_type,
                                            org.omg.CORBA.ServiceInformationHolder service_information)
    {
        //          if (( service_type == org.omg.CORBA.Security.value ) && Environment.supportSSL ())
        //          {
        //              byte options[] = new byte [5]; // ServiceOption[]
        //              options[0] = (byte)org.omg.Security.SecurityLevel1.value;
        //              options[1] = (byte)org.omg.Security.SecurityLevel2.value;
        //              options[2] = (byte)org.omg.Security.ReplaceORBServices.value;
        //              options[3] = (byte)org.omg.Security.ReplaceSecurityServices.value;
        //              options[4] = (byte)org.omg.Security.CommonInteroperabilityLevel0.value;
        //              org.omg.CORBA.ServiceDetail details[] = new org.omg.CORBA.ServiceDetail [2];
        //              details[0].service_detail_type = org.omg.Security.SecureTransportType.value;
        //              details[0].service_detail = org.jacorb.security.ssl.SSLSetup.getMechanismType().getBytes();
        //              details[1].service_detail_type = org.omg.Security.SecureTransportType.value;
        //              details[1].service_detail = "001010011".getBytes(); // AuditId, _PublicId, AccessId
        //              return true;
        //          }
        //          else return false;
        throw new org.omg.CORBA.NO_IMPLEMENT ();
    }

    /**
     * resolve_initial_references
     */

    public org.omg.CORBA.Object resolve_initial_references(String identifier)
        throws org.omg.CORBA.ORBPackage.InvalidName
    {
        if ( initial_references.containsKey(identifier) )
        {
            return (org.omg.CORBA.Object)initial_references.get(identifier);
        }
        else
        {
            org.omg.CORBA.Object obj = null;
            String url = Environment.getProperty("ORBInitRef." + identifier);

            if( url != null )
            {
      try
      {
          obj = this.string_to_object( url );
      }
      catch( Exception e )
      {
          Debug.output( 1, "ERROR: Could not create initial reference for \"" +
              identifier + '\"' );
          Debug.output( 1, "Please check property \"ORBInitRef." +
              identifier + '\"' );

          Debug.output( 3, e );

          throw new org.omg.CORBA.ORBPackage.InvalidName();
      }
            }
            /* "special" behavior follows */
            else if (identifier.equals ("NameService") && isApplet ())
            {
                // try to get location of URL with ns's IOR from a file
                //     called NameService.ior at the applet's host
                String ior_str =
                    org.jacorb.util.ObjectUtil.readURL("http://"
                                                       + applet.getCodeBase().getHost()
                                                       + "/"
                                                       + "NameService.ior");

                obj = this.string_to_object (ior_str);

                if (obj != null)
                {
                    if (! obj._is_a (org.omg.CosNaming.NamingContextHelper.id ()))
                    {
                        obj = null;
                    }
                }
            }
            else if( identifier.equals("RootPOA") )
            {
                return (org.omg.CORBA.Object)getRootPOA();
            }
            else if( identifier.equals("POACurrent") )
            {
                return (org.omg.CORBA.Object)getPOACurrent();
            }
            else if( identifier.equals("SecurityCurrent") )
            {
                if( securityCurrent == null )
                {
                    try
                    {
                        Class currentClass = Class.forName( "org.jacorb.security.level2.CurrentImpl" );

                        Constructor constr = currentClass.getConstructor( new Class[]{
                            org.omg.CORBA.ORB.class });

                        securityCurrent = (org.omg.SecurityLevel2.Current)
                            constr.newInstance( new Object[]{ this });

                        Method init = currentClass.getDeclaredMethod( "init",
                                                                      new Class[0] );
                        init.invoke( securityCurrent, new Object[0] );
                    }
                    catch (Exception e)
                    {
                        Debug.output( Debug.IMPORTANT | Debug.ORB_MISC,
                                      e );
                    }
                }

                obj = securityCurrent;
            }
            else if( identifier.equals("DynAnyFactory") )
            {
                obj = new org.jacorb.orb.dynany.DynAnyFactoryImpl( this );
            }
            else if( identifier.equals("PICurrent") )
            {
                return piCurrent;
            }
            else if( identifier.equals("CodecFactory") )
            {
                obj = new CodecFactoryImpl(this);
            }
//              else if( identifier.equals("TransactionCurrent") )
//                  obj = new org.jacorb.transaction.TransactionCurrentImpl();
            else
            {
                throw new org.omg.CORBA.ORBPackage.InvalidName ();
            }

            if (obj != null)
            {
                initial_references.put (identifier, obj);
            }

            return obj;
        }
    }

//      private void initialReferencesInit()
//      {
//          Hashtable props = Environment.getProperties( "ORBInitRef." );

//          for( Enumeration names = props.keys(); names.hasMoreElements(); )
//          {
//              String name = (String) names.nextElement();
//              String key = name.substring( name.indexOf('.')+1);
//              try
//              {
//                  register_initial_reference( key,
//                             string_to_object( (String) props.get( name )));

//                  Debug.output(3, "ORBInitRef " + key + " string: " +
//                               (String) props.get( name ));
//              }
//              catch( Exception e )
//              {
//                  Debug.output( 1, "Unable to create initial reference from " +
//                                name + "=" + props.get( name ) );
//                  Debug.output( 3, e );
//              }
//          }
//      }


    /**
     * Register a reference, that will be returned on subsequent calls
     * to resove_initial_references(id). <br>
     * The references "RootPOA", "POACurrent" and "PICurrent" can be set,
     * but will not be resolved with the passed in references.
     *
     * @param id The references human-readable id, e.g. "MyService".
     * @param obj The objects reference.
     * @exception InvalidName A reference with id has already been registered.
     * @overwrites  register_initial_reference() in org.omg.CORBA_2_5.ORB
     */

    public void register_initial_reference( String id, org.omg.CORBA.Object obj )
        throws InvalidName
    {
        if (id == null || id.length() == 0 ||
            initial_references.containsKey(id) )
        {
            throw new InvalidName();
        }
        else
        {
            Debug.output( 4, "Registering initial ref " + id );
            initial_references.put(id, obj);
        }
    }

    public void run()
    {
        Debug.output(4,"ORB run");
        try
        {
            synchronized( orb_synch )
            {
                while( run )
                {
                    orb_synch.wait();
                }
            }
        }
        catch( java.lang.InterruptedException iex )
        {
            iex.printStackTrace();
        }
        Debug.output(4,"ORB run, exit");
    }


    public void send_multiple_requests_oneway( org.omg.CORBA.Request[] req )
    {
        for( int i = 0; i < req.length; i++ )
        {
            req[i].send_oneway();
        }
    }

    public void send_multiple_requests_deferred( org.omg.CORBA.Request[] req )
    {
        for( int i = 0; i < req.length; i++ )
        {
            req[i].send_deferred();
        }
    }

    public boolean poll_next_response()
    {
        if( requests.size () == 0 )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER
                ( 11, org.omg.CORBA.CompletionStatus.COMPLETED_NO );
        }

        synchronized( requests )
        {
            Request req;
            Iterator iter = requests.iterator();
            while( iter.hasNext() )
            {
                req = (Request)iter.next();
                if( req.poll_response() )
                {
                    request = req;
                    return true;
                }
            }
        }
        return false;
    }

    public org.omg.CORBA.Request get_next_response ()
    {
        if( requests.size () == 0 )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER
                ( 11, org.omg.CORBA.CompletionStatus.COMPLETED_NO );
        }

        synchronized( requests )
        {
            Request req = null;
            if( request != null )
            {
                request.get_response();
                req = request;
                request = null;
                return req;
            }

            Iterator iter;
            while( true )
            {
                iter = requests.iterator();
                while( iter.hasNext() )
                {
                    req = (Request)iter.next();
                    if( req.poll_response() )
                    {
                        req.get_response();
                        return req;
                    }
                }
            }
        }
    }

    public void addRequest( org.omg.CORBA.Request req )
    {
        requests.add( req );
    }

       public void removeRequest( org.omg.CORBA.Request req )
    {
        requests.remove( req );
    }


    /**
     * called from ORB.init()
     */

    protected void set_parameters( String[] args, java.util.Properties props )
    {
        if( props != null )
        {
            _props = props;
            Environment.addProperties( props );
        }

        /*
         * find -ORBInitRef args and add them to Environment
         * (overwriting existing props).
         */

        if( args != null )
        {
            _args = args;
            for( int i = 0; i < args.length; i++ )
            {
                String arg = args[ i ].trim();

                if( arg.startsWith( "-ORBInitRef." ))
                {
                    //This is the wrong jacorb form -ORBInitRef.<name>=<val>

                    //get rid of the leading `-'
                    String prop = arg.substring( 1 );

                    //find the equals char that separates prop name from
                    //prop value
                    int equals_pos = prop.indexOf( '=' );

                    //add the property to environment
                    Environment.setProperty( prop.substring( 0, equals_pos ),
                                             prop.substring( equals_pos + 1) );
                }
                else if( arg.equals( "-ORBInitRef" ))
                {
                    //This is the compliant form -ORBInitRef <name>=<val>

                    //Is there a next arg?
                    if( (args.length - 1) < (i + 1) )
                    {
                        Debug.output( 1, "WARNING: -ORBInitRef argument without value" );
                        continue;
                    }

                    String prop = args[ ++i ].trim();

                    //find the equals char that separates prop name from
                    //prop value
                    int equals_pos = prop.indexOf( '=' );

                    //add the property to environment
                    Environment.setProperty( "ORBInitRef." +
                                             prop.substring( 0, equals_pos ),
                                             prop.substring( equals_pos + 1) );
                }
            }
        }

        transport_manager = new TransportManager( this );
        giop_connection_manager = new GIOPConnectionManager();
        clientConnectionManager =
            new ClientConnectionManager( this,
                                         transport_manager,
                                         giop_connection_manager );

        String s = Environment.getProperty( "jacorb.hashtable_class" );
        if( s == null || s.length() == 0 )
        {
            Debug.output( Debug.INFORMATION | Debug.ORB_MISC,
                          "Property \"jacorb.hashtable_class\" not present. Will use default hashtable implementation" );
            knownReferences = new Hashtable();

        }
        else
        {
            try
            {
                knownReferences = (Hashtable) Class.forName( s ).newInstance();
            }
            catch( Exception e )
            {
                Debug.output( Debug.INFORMATION | Debug.ORB_MISC, e );
                knownReferences = new Hashtable();
            }
        }

         objectKeyMap =
            Environment.getProperties("jacorb.orb.objectKeyMap", true );

        String versionProperty =
            Environment.getProperty("jacorb.orb.print_version");

        if( versionProperty != null &&
            versionProperty.equals("on") )
        {
            System.out.println("\tJacORB V " + versionString +
                               ", www.jacorb.org");
            System.out.println("\t(C) Gerald Brose, FU Berlin/XTRADYNE Technologies, " +
                               dateString);
        }

//          Hashtable initrefs = Environment.getProperties("ORBInitRef");

//          for( Enumeration e = initrefs.keys(); e.hasMoreElements(); )
//          {
//              String key = (String)e.nextElement();
//              Object obj = string_to_object( (String)initrefs.get( key ));
//              if( obj != null )
//                  initial_references.put( key.substring( key.indexOf('.')+1), obj);
//          }

        always_add_1_0_Profile =
            Environment.isPropertyOn( "jacorb.giop.add_1_0_profiles" );



        interceptorInit();

//          try
//          {
//              resolve_initial_references("SecurityCurrent");
//          }
//          catch( Exception e )
//          {
//              e.printStackTrace();
//          }

    }

    protected void set_parameters
        ( java.applet.Applet app, java.util.Properties  props )
    {
        applet = app;
        _props = props;

        Environment.addProperties( props );

        transport_manager = new TransportManager( this );
        giop_connection_manager = new GIOPConnectionManager();
        clientConnectionManager =
            new ClientConnectionManager(
                this,
                transport_manager,
                giop_connection_manager);

        String s = Environment.getProperty( "jacorb.hashtable_class" );
        if( s == null || s.length() == 0 )
        {
            Debug.output( Debug.INFORMATION | Debug.ORB_MISC,
                          "Property \"jacorb.hashtable_class\" not present. Will use default hashtable implementation" );
            knownReferences = new Hashtable();

        }
        else
        {
            try
            {
                knownReferences = (Hashtable) Class.forName( s ).newInstance();
            }
            catch( Exception e )
            {
                Debug.output( Debug.INFORMATION | Debug.ORB_MISC, e );
                knownReferences = new Hashtable();
            }
        }


        // unproxyTable = new Hashtable();

        interceptorInit();
    }

    /**
     * This method retrieves the ORBInitializer-Names from the Environment,
     * and runs them.
     */

    private void interceptorInit()
    {
        // get instances from Environment
        Vector orb_initializers = Environment.getORBInitializers ();

        if (orb_initializers.size () > 0)
        {
            ORBInitInfoImpl info = new ORBInitInfoImpl (this);
            ORBInitializer init;

            // call pre_init in ORBInitializers
            for (int i = 0; i < orb_initializers.size(); i++)
            {
                try
                {
                    init = (ORBInitializer) orb_initializers.elementAt (i);
                    init.pre_init (info);
                }
                catch (Exception e)
                {
                    Debug.output (0, e);
                }
            }

            //call post_init on ORBInitializers
            for (int i = 0; i < orb_initializers.size (); i++)
            {
                try
                {
                    init = (ORBInitializer) orb_initializers.elementAt (i);
                    init.post_init (info);
                }
                catch (Exception e)
                {
                    Debug.output (0, e);
                }
            }

            //allow no more access to ORBInitInfo from ORBInitializers
            info.setInvalid ();

            Vector client_interceptors = info.getClientInterceptors ();
            Vector server_interceptors = info.getServerInterceptors ();
            Vector ior_intercept = info.getIORInterceptors ();

            hasClientInterceptors = (client_interceptors.size () > 0);
            hasServerInterceptors = (server_interceptors.size () > 0);

            if (hasClientInterceptors || hasServerInterceptors || (ior_intercept.size () > 0))
            {
                interceptor_manager = new InterceptorManager
                (
                    client_interceptors,
                    server_interceptors,
                    ior_intercept,
                    info.getSlotCount (),
                    this
                );
            }

            // add PolicyFactories to ORB
            policy_factories = info.getPolicyFactories();

            /*
            // add policy factories that are always present
            try
            {
                PolicyFactory factory = (PolicyFactory)new BiDirPolicyFactoryImpl( this );

                policy_factories.put(
                   new Integer(org.omg.BiDirPolicy.BIDIRECTIONAL_POLICY_TYPE.value),
                   factory );
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
            */
        }
    }

    public void shutdown( boolean wait_for_completion )
    {
        Debug.output(2,"prepare ORB for going down...");

        if( ! run )
        {
            return; // ORB already shut down...
        }

        synchronized( shutdown_synch )
        {
            Debug.output(2,"ORB going down...");
            if( shutdown_in_progress && wait_for_completion )
            {
                synchronized( shutdown_synch )
                {
                    try
                    {
                        shutdown_synch.wait();
                    }
                    catch( InterruptedException ie )
                    {}
                    Debug.output(2,"ORB shutdown complete (1)");
                    return;
                }
            }
            else if( shutdown_in_progress && !wait_for_completion )
            {
                Debug.output(2,"ORB shutdown complete (2)");
                return;
            }

            shutdown_in_progress = true;
        }

        if( rootpoa != null )
            rootpoa.destroy(true, wait_for_completion);

        if( basicAdapter != null )
        {
            basicAdapter.stopListeners();
        }

        Debug.output(3,"ORB shutdown (cleaning up ORB...)");

        clientConnectionManager.shutdown();
        knownReferences.clear();
        BufferManager.getInstance().release();

        Debug.output(3,"ORB shutdown (all tables cleared)");

        /* notify all threads waiting for shutdown to complete */

        synchronized( shutdown_synch )
        {
            shutdown_synch.notifyAll();
        }
        Debug.output(3,"ORB shutdown (threads notified)");

        /* notify all threads waiting in orb.run() */
        synchronized( orb_synch )
        {
            run = false;
            orb_synch.notifyAll();
        }
        Debug.output(2,"ORB shutdown complete");
    }

    public void destroy()
    {
        if( destroyed )
            throw new org.omg.CORBA.OBJECT_NOT_EXIST();

        if( run )
        {
            shutdown( true );
        }

        if( interceptor_manager != null )
        {
            interceptor_manager.destroy();
        }

        // other clean up possible here ?
        destroyed = true;
    }

    public org.omg.CORBA.Object string_to_object (String str)
    {
        if (str == null)
        {
            return null;
        }

        try
        {
            ParsedIOR pior = new ParsedIOR( str, this );
            if( pior.isNull() )
            {
                return null;
            }
            else
            {
                return _getObject(pior);
            }
        }
        catch (IllegalArgumentException iae)
        {
            Debug.output (5, iae);
            return null;
        }
    }

    /**
     * always return a ValueDef or throw BAD_PARAM if not repid of a value
     */

    public org.omg.CORBA.Object get_value_def(String repid)
        throws org.omg.CORBA.BAD_PARAM
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * called by org.jacorb.poa.RequestProcessor
     */

    public void set_delegate( java.lang.Object wrapper )
    {
        if( ! (wrapper instanceof org.omg.PortableServer.Servant) )
            throw new org.omg.CORBA.BAD_PARAM("Argument must be of type org.omg.PortableServer.Servant");
        else
        {
            try
            {
                ((org.omg.PortableServer.Servant)wrapper)._get_delegate();
            }
            catch( org.omg.CORBA.BAD_INV_ORDER bio )
            {
                // only set the delegate if it has not been set already
                org.jacorb.orb.ServantDelegate delegate =
                    new org.jacorb.orb.ServantDelegate( this );
                ((org.omg.PortableServer.Servant)wrapper)._set_delegate(delegate);
            }
        }
    }

    /**
     * forces the use of a proxy (by using a dummy applet)
     */

    public void useProxy()
    {
        applet = new java.applet.Applet();
    }

    public String object_to_string( org.omg.CORBA.Object obj)
    {
        if (obj == null)
        {
            return nullIORString;
        }

        if (obj instanceof org.omg.CORBA.LocalObject)
        {
           throw new org.omg.CORBA.MARSHAL ("Attempt to stringify a local object");
        }

        Object delegate =
            ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate();
        if (delegate instanceof org.jacorb.orb.Delegate)
            return delegate.toString();
        else
            throw new Error("Argument has a delegate whose class is "
                            + delegate.getClass().getName()
                            + ", a org.jacorb.orb.Delegate was expected");
    }

    public void perform_work ()
    {
        if (! run)
        {
            throw new org.omg.CORBA.BAD_INV_ORDER
                (4, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
        }
    }

    public boolean work_pending ()
    {
        if (! run)
        {
            throw new org.omg.CORBA.BAD_INV_ORDER
                (4, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
        }

        return false;
    }

    public ValueFactory register_value_factory (String id,
                                                ValueFactory factory)
    {
        return (ValueFactory)valueFactories.put (id, factory);
    }

    public void unregister_value_factory (String id)
    {
        valueFactories.remove (id);
    }

    public ValueFactory lookup_value_factory (String id)
    {
        ValueFactory result = (ValueFactory)valueFactories.get (id);
        if (result == null)
        {
            if (id.startsWith ("IDL"))
            {
                String valueName = org.jacorb.ir.RepositoryID.className (id);
                result = findValueFactory (valueName);
                valueFactories.put (id, result);
            }
        }
        return result;
    }

    /**
     * Finds a ValueFactory for class valueName by trying standard class names.
     */

    private ValueFactory findValueFactory (String valueName)
    {
        Class result = null;
        result = findClass (valueName + "DefaultFactory", true);
        if (result != null)
        {
            return (ValueFactory)instantiate (result);
        }
        else
        {
            // Extension of the standard: Handle the common case
            // when the Impl class is its own factory...
            Class c = findClass (valueName, false);
            result  = findClass (valueName + "Impl", false);

            if (result != null && c.isAssignableFrom (result))
            {
                if (ValueFactory.class.isAssignableFrom (result))
                {
                    return (ValueFactory)instantiate (result);
                }
                else
                {
                    // ... or create a factory on the fly
                    return new JacORBValueFactory (result);
                }
            }
            else
                return null;
        }
    }

    /**
     * Internal value factory class.  This can be used for any value
     * implementation that has a no-arg constructor.
     */
    private class JacORBValueFactory
        implements org.omg.CORBA.portable.ValueFactory
    {
        private Class implementationClass;

        public JacORBValueFactory (Class c)
        {
            implementationClass = c;
        }

        public java.io.Serializable read_value
          (org.omg.CORBA_2_3.portable.InputStream is)
        {
            StreamableValue value =
                (StreamableValue)instantiate (implementationClass);

            // Register the object even before reading its state.
            // This is essential for recursive values.
            ((org.jacorb.orb.CDRInputStream)is).register_value (value);

            value._read (is);
            return value;
        }

    }

    /**
     * Returns the class object for `name', if it exists, otherwise
     * returns null.  If `orgomg' is true, and `name' starts with "org.omg",
     * do a double-take using "omg.org" as the prefix.
     */
    private Class findClass (String name, boolean orgomg)
    {
        Class result = null;
        try
        {
             //#ifjdk 1.2
                result = Thread.currentThread().getContextClassLoader()
                                               .loadClass (name);
             //#else
             //# result = Class.forName (name);
             //#endif
        }
        catch (ClassNotFoundException e)
        {
            if (orgomg && name.startsWith ("org.omg"))
                try
                {
                     //#ifjdk 1.2
                        result = Thread.currentThread().getContextClassLoader()
                                       .loadClass ("omg.org" + name.substring(7));
                     //#else
                     //# result = Class.forName ("omg.org" + name.substring(7));
                     //#endif
                }
                catch (ClassNotFoundException x)
                {
                    // nothing, result is null
                }
        }
        return result;
    }

    /**
     * Instantiates class `c' using its no-arg constructor.  Throws a
     * run-time exception if that fails.
     */
    private Object instantiate (Class c)
    {
        try
        {
            return c.newInstance();
        }
        catch (IllegalAccessException e1)
        {
            throw new RuntimeException ("cannot instantiate class "
                                        + c.getName()
                                        + " (IllegalAccessException)");
        }
        catch (InstantiationException e2)
        {
            throw new RuntimeException ("cannot instantiate class "
                                        + c.getName()
                                        + " (InstantiationException)");
        }
    }

    /**
     * Test, if the ORB has ClientRequestInterceptors <br>
     * Called by Delegate.
     */

    public boolean hasClientRequestInterceptors ()
    {
        return hasClientInterceptors;
    }

    /**
     * Test, if the ORB has ServerRequestInterceptors <br>
     * Called by poa.RequestProcessor.
     */

    public boolean hasServerRequestInterceptors ()
    {
        return hasServerInterceptors;
    }

    /**
     * Test, if the ORB has client or server side interceptors.
     */

    public boolean hasRequestInterceptors ()
    {
        return (hasServerInterceptors || hasClientInterceptors);
    }

    /**
     * Get the InterceptorManager, if present.
     *
     * @return the InterceptorManager, or null, if none is present.
     */

    public org.jacorb.orb.portableInterceptor.InterceptorManager getInterceptorManager()
    {
        return interceptor_manager;
    }


    /* DII helper methods */

    public org.omg.CORBA.ExceptionList create_exception_list ()
    {
        return new org.jacorb.orb.dii.ExceptionList ();
    }

    public org.omg.CORBA.NVList create_list (int count)
    {
        return new org.jacorb.orb.NVList (this, count);
    }

    public org.omg.CORBA.NamedValue create_named_value
        (String name, org.omg.CORBA.Any value, int flags)
    {
        return new org.jacorb.orb.NamedValue (name, value, flags);
    }

    public org.omg.CORBA.NVList create_operation_list (org.omg.CORBA.Object obj)
    {
        org.omg.CORBA.OperationDef oper;

        if (obj instanceof org.omg.CORBA.OperationDef)
        {
            oper = (org.omg.CORBA.OperationDef) obj;
        }
        else
        {
            throw new org.omg.CORBA.BAD_PARAM ("Argument must be of type org.omg.CORBA.OperationDef");
        }
        return (create_operation_list (oper));
    }

    // This operation is under deprecation. To be replaced by one above.

    public org.omg.CORBA.NVList create_operation_list
        (org.omg.CORBA.OperationDef oper)
    {
        int no = 0;
        org.omg.CORBA.Any any;
        org.omg.CORBA.ParameterDescription[] params = null;
        org.omg.CORBA.ParameterDescription param;
        org.omg.CORBA.NVList list;

        params = oper.params ();
        if (params != null)
        {
            no = params.length;
        }
        list = new org.jacorb.orb.NVList (this, no);

        for (int i = 0; i < no; i++)
        {
            param = params[i];
            any = create_any ();
            any.type (param.type);
            list.add_value (param.name, any, param.mode.value ());
        }

        return list;
    }

    /**
     * Map an object key to another, as defined by the value
     * of a corresponding configuration property in the properties
     * file, e.g. map "NameService" to "StandardNS/NameServer-POA/_root"
     *
     * @returns the mapped key, if a mapping is defined, originalKey otherwise
     */

    public byte[] mapObjectKey( byte[] originalKey )
    {
        if( objectKeyMap.size() != 0 )
        {
            String s = new String( originalKey );
            Object o = objectKeyMap.get( s );
            if( o != null )
            {
                return org.jacorb.orb.util.CorbaLoc.parseKey((String)o);
            }
        }
        // else:
        return originalKey;

    }

    /**
     * Inner class that implements org.omg.PortableInterceptor.Current
     * by forwarding each invocation to a thread-dependent target.
     */
    private class PICurrent
        extends org.omg.CORBA.LocalObject
        implements org.omg.PortableInterceptor.Current
    {
        // Helper method that returns the actual
        // target of a PICurrent invocation
        private Current getTarget()
        {
            if (interceptor_manager == null)
                return InterceptorManager.EMPTY_CURRENT;
            else
                return interceptor_manager.getCurrent();
        }

        // org.omg.PortableInterceptor.Current implementation ---

        public org.omg.CORBA.Any get_slot(int id)
            throws InvalidSlot
        {
            return getTarget().get_slot(id);
        }

        public void set_slot(int id, org.omg.CORBA.Any data)
            throws InvalidSlot
        {
            getTarget().set_slot(id, data);
        }

    }

}
