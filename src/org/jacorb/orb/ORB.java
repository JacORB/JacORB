package org.jacorb.orb;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

import org.jacorb.imr.ImRAccessImpl;
import org.jacorb.util.*;
import org.jacorb.orb.iiop.*;
import org.jacorb.orb.policies.*;
import org.jacorb.orb.dii.Request;
import org.jacorb.orb.giop.*;
import org.jacorb.orb.portableInterceptor.*;
import org.jacorb.poa.util.POAUtil;

import org.apache.avalon.framework.logger.*;
import org.apache.avalon.framework.configuration.*;

import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.BAD_QOS;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA.portable.BoxedValueHelper;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.Messaging.*;
import org.omg.PortableInterceptor.*;
import org.omg.IOP.*;
import org.omg.IIOP.*;
import org.omg.ETF.*;

/**
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 */

public final class ORB
    extends ORBSingleton
    implements org.jacorb.poa.POAListener, Configurable
{
    private static final String versionString = org.jacorb.util.Version.version;
    private static final String dateString = org.jacorb.util.Version.date;
    private static final String nullIORString =
        "IOR:00000000000000010000000000000000";

    /** the configuration object for this ORB instance */
    private org.jacorb.config.Configuration configuration = null;

    /** configuration properties */
    private boolean cacheReferences;
    private String implName;
    private int giopMinorVersion;
    private boolean giopAdd_1_0_Profiles;
    private String hashTableClassName;
    private boolean useIMR;
    private boolean useIMREndpoint;
    private String imrProxyHost = null;
    private int imrProxyPort = -1;
    private String iorProxyHost;
    private int iorProxyPort = -1;
    private boolean printVersion = true;

    /** "initial" references */
    private Map initial_references = new HashMap();

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
    private Map knownReferences = null;

    /** connection mgmt. */
    private ClientConnectionManager clientConnectionManager;

    /** The transport manager*/
    private TransportManager transport_manager = null;

    private GIOPConnectionManager giop_connection_manager = null;

    /** buffer mgmt. */
    private BufferManager bufferManager;

    /**
     * Maps repository ids (strings) to objects that implement
     * org.omg.CORBA.portable.ValueFactory.  This map is used by
     * register/unregister_value_factory() and lookup_value_factory().
     */
    protected Map valueFactories = new HashMap();

    /**
     * Maps repository ids (strings) of boxed value types to
     * BoxedValueHelper instances for those types.
     */
    protected Map boxedValueHelpers = new HashMap();

    private Map objectKeyMap = new HashMap();

    /** properties */
    private java.util.Properties _props;

    /** the ORB object's logger */
    private Logger logger;

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

    /* for registering POAs with the ImR */
    private ImRAccess imr = null;
    private int persistentPOACount;

    public static final String orb_id = "jacorb:" + org.jacorb.util.Version.version;

    /* outstanding dii requests awaiting completion */
    private Set requests = Collections.synchronizedSet( new HashSet() );
    /* most recently completed dii request found during poll */
    private Request request = null;

    /* PolicyManagement */
    private org.jacorb.orb.policies.PolicyManager policyManager = null;

    /* policy factories, from portable interceptor spec */
    private Map policy_factories = null;

    private static org.omg.CORBA.TCKind kind;

    private static final String [] services  =
        {"RootPOA","POACurrent", "DynAnyFactory", "PICurrent", "CodecFactory"};

    private boolean bidir_giop = false;


    public ORB()
    {
    }

    /**
     * configure the ORB
     */

    public void configure(Configuration myConfiguration)
        throws ConfigurationException
    {
        super.configure(myConfiguration);
        this.configuration =
            (org.jacorb.config.Configuration)myConfiguration;
        logger =
            configuration.getNamedLogger("jacorb.orb");

        cacheReferences =
            configuration.getAttribute("jacorb.reference_caching", "off").equals("on");

        implName =
            configuration.getAttribute("jacorb.implname", "" );

        giopMinorVersion =
            configuration.getAttributeAsInteger("jacorb.giop_minor_version", 2);

        giopAdd_1_0_Profiles =
            configuration.getAttribute("jacorb.giop.add_1_0_profiles", "off").equals("on");

        hashTableClassName =
            configuration.getAttribute( "jacorb.hashtable_class", "" );

        useIMR =
            configuration.getAttribute("jacorb.use_imr","off").equals("on");

        imrProxyHost =
            configuration.getAttribute("jacorb.imr.ior_proxy_host",null);

        imrProxyPort =
            configuration.getAttributeAsInteger("jacorb.imr.ior_proxy_port",-1);

        useIMREndpoint =
            configuration.getAttribute("jacorb.use_imr", "on").equals("on");

        iorProxyHost =
            configuration.getAttribute("jacorb.ior_proxy_host", null);

        iorProxyPort =
            configuration.getAttributeAsInteger("jacorb.ior_proxy_port",-1);

        printVersion =
            configuration.getAttribute("jacorb.orb.print_version", "on").equals("on");

        if( printVersion && logger.isInfoEnabled())
        {
            logger.info("\n\t~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                        "\tJacORB V " + versionString + ", www.jacorb.org\n" +
                        "\t(C) Gerald Brose, XTRADYNE Technologies/FU Berlin, " +
                        dateString + "\n" +
                        "\t~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        }

        BufferManager.configure( configuration);
        try
        {
            bufferManager = BufferManager.getInstance();
        }
        catch( BAD_INV_ORDER b)
        {
            b.printStackTrace(); // cannot happen!
        }

        configureObjectKeyMap(configuration);
    }

    /**
     * Some parts of JacORB cannot be elegantly configured from the outside
     * and need access to the ORB's configuration retrieve config settings.
     * This method should only be used in those restricted cases!
     */

    public org.jacorb.config.Configuration getConfiguration()
    {
        return configuration;
    }

    /**
     * Overrides id() in org.omg.CORBA_2_5.ORB
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
            org.jacorb.orb.Delegate del = (org.jacorb.orb.Delegate)o._get_delegate();
            if (del != null)
            {
                ParsedIOR delpior= del.getParsedIOR();
                if (delpior == null)
                {
                    knownReferences.remove(key);
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Removing an invalid reference from cache.");
                    }
                }
                else if( pior.getEffectiveProfile()
                           .is_match(delpior.getEffectiveProfile()))
                {
                    return o._duplicate();
                }
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Remove stale reference from cache ");
                }
                knownReferences.remove( key );
            }
        }

        if (pior == null)
        {
            if (logger.isErrorEnabled())
                logger.error("Internal error, pior is null");
        }

        org.jacorb.orb.Delegate d = new Delegate(this, pior );
        try
        {
            d.configure(configuration);
        }
        catch(ConfigurationException ce)
        {
            if (logger.isErrorEnabled())
                logger.error("ConfigurationException", ce);
        }

        o = d.getReference( null );

        if( cacheReferences )
        {
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
        List scopes;
        String res;
        String refImplName = null;

        // if no POAs activated, we don't look further
        if( rootpoa == null || basicAdapter == null )
        {
            return null;
        }

        // String orbImplName = Environment.getProperty( "jacorb.implname", "" );

        try
        {
            refImplName =
                org.jacorb.poa.util.POAUtil.extractImplName( d.getObjectKey() );
        }
        catch( org.jacorb.poa.except.POAInternalError pie )
        {
            if( logger.isDebugEnabled() )
            {
                logger.debug("findPOA: reference generated by foreign POA");
            }
            return null;
        }

        if( refImplName == null )
        {
            if( implName.length() > 0 )
            {
                if( logger.isDebugEnabled() )
                {
                    logger.debug("findPOA: impl_name mismatch");
                }
                return null;
            }
        }
        else
        {
            if( !(implName.equals( refImplName ) ))
            {
                if( logger.isDebugEnabled() )
                {
                    logger.debug("findPOA: impl_name mismatch");
                }
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
            scopes = POAUtil.extractScopedPOANames (poa_name);

            for( int i = 0; i < scopes.size(); i++)
            {
                res = ((String)scopes.get( i ));

                if( res.equals (""))
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
                    tmp_poa = tmp_poa._getChildPOA( res );
                }
                catch ( org.jacorb.poa.except.ParentIsHolding p )
                {
                    if( logger.isDebugEnabled() )
                    {
                        logger.debug("findPOA: holding adapter");
                    }
                    return null;
                }
            }
            byte[] objectId =
                org.jacorb.poa.util.POAUtil.extractOID( ref );

            if( tmp_poa.isSystemId()
               && ! tmp_poa.previouslyGeneratedObjectId(objectId))
            {
                if( logger.isDebugEnabled() )
                {
                    logger.debug("findPOA: not a previously generated object key.");
                }
                return null;
            }

            return tmp_poa;
        }
        catch( Exception e )
        {
            if( logger.isErrorEnabled() )
            {
                logger.error(e.getMessage());
            }
        }

        if( logger.isDebugEnabled() )
        {
            logger.debug("findPOA: nothing found");
        }
        return null;
    }


    public ClientConnectionManager getClientConnectionManager()
    {
        return clientConnectionManager;
    }

    public GIOPConnectionManager getGIOPConnectionManager()
    {
        if (giop_connection_manager == null)
        {
            giop_connection_manager =
                new GIOPConnectionManager();
            try
            {
                giop_connection_manager.configure(configuration);
            }
            catch( ConfigurationException ce )
            {
                throw new INTERNAL(ce.getMessage());
            }
        }
        return giop_connection_manager;
    }


    /**
     * Take a string rather then a Delegate object to prevent data race
     * warning.
     */
    synchronized void _release( String iorString )
    {
        knownReferences.remove( iorString );
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

    org.omg.IOP.IOR createIOR(String repId,
                              byte[] objectKey,
                              boolean _transient,
                              org.jacorb.poa.POA poa,
                              Map policy_overrides)
    {
        List profiles     = new ArrayList();
        Map  componentMap = new HashMap();

        for (Iterator i = basicAdapter.getEndpointProfiles().iterator();
             i.hasNext();)
        {
            Profile profile = (Profile)i.next();
            profile.set_object_key (objectKey);
            profiles.add (profile);

            TaggedComponentList profileComponents = new TaggedComponentList();
            profileComponents.addComponent(create_ORB_TYPE_ID());
            componentMap.put(new Integer(profile.tag()), profileComponents);

            // use proxy or ImR address if necessary
            if (profile instanceof IIOPProfile)
            {
                patchAddress((IIOPProfile)profile, repId, _transient);
            }
        }

        TaggedComponentList multipleComponents = new TaggedComponentList();
        componentMap.put (new Integer (TAG_MULTIPLE_COMPONENTS.value),
                          multipleComponents);

        // invoke IOR interceptors
        if ((interceptor_manager != null) &&
            interceptor_manager.hasIORInterceptors())
        {
            IORInfoImpl info = new IORInfoImpl(this, poa,
                                               componentMap,
                                               policy_overrides,
                                               profiles);
            try
            {
                interceptor_manager.getIORIterator().iterate( info );
            }
            catch (Exception e)
            {
                if (logger.isErrorEnabled())
                    logger.error(e.getMessage());
            }
        }

        // add GIOP 1.0 profile if necessary
        IIOPProfile iiopProfile = findIIOPProfile(profiles);
        if ( (iiopProfile != null)
             && ( this.giopMinorVersion == 0 || this.giopAdd_1_0_Profiles ))
            //            && (   Environment.giopMinorVersion() == 0
            //    || Environment.giopAdd_1_0_Profiles()))
        {
            Profile profile_1_0 = iiopProfile.to_GIOP_1_0();
            profiles.add(profile_1_0);

            // shuffle all components over into the multiple components profile
            TaggedComponentList iiopComponents =
                (TaggedComponentList)componentMap.get(new Integer(TAG_INTERNET_IOP.value));

            multipleComponents.addAll(iiopProfile.getComponents());
            multipleComponents.addAll(iiopComponents);

            // if we only want GIOP 1.0, remove the other profile
            if (giopMinorVersion == 0)
            {
                profiles.remove(iiopProfile);
            }
        }

        // marshal the profiles into the IOR and return
        TaggedProfile[] tps = null;
        if (multipleComponents.isEmpty())
        {
            tps = new TaggedProfile [profiles.size()];
        }
        else
        {
            tps = new TaggedProfile [profiles.size() + 1];
            tps[tps.length-1] =
                createMultipleComponentsProfile(multipleComponents);
        }

        TaggedProfileHolder      tp = new TaggedProfileHolder();
        TaggedComponentSeqHolder tc = new TaggedComponentSeqHolder();
        for (int i=0; i<profiles.size(); i++)
        {
            Profile p = (Profile)profiles.get(i);
            TaggedComponentList c =
                (TaggedComponentList)componentMap.get (new Integer (p.tag()));
            tc.value = c.asArray();
            p.marshal (tp, tc);
            tps[i] = tp.value;
        }

        return new IOR(repId, tps);
    }

    private TaggedProfile createMultipleComponentsProfile
                                  (TaggedComponentList components)
    {
        CDROutputStream out = new CDROutputStream(this);
        out.beginEncapsulatedArray();
        MultipleComponentProfileHelper.write(out, components.asArray());
        return new TaggedProfile
        (
            TAG_MULTIPLE_COMPONENTS.value,
            out.getBufferCopy()
        );
    }

    /**
     * Finds the first IIOPProfile in the given List of Profiles,
     * and returns it.  If no such profile is found, this method
     * returns null.
     */
    private IIOPProfile findIIOPProfile (List profiles)
    {
        for (Iterator i = profiles.iterator(); i.hasNext();)
        {
            Profile p = (Profile)i.next();
            if (p instanceof IIOPProfile)
                return (IIOPProfile)p;
        }
        return null;
    }

    public org.omg.CORBA.Context get_default_context ()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ();
    }


    /**
     * used from the POA
     * @return the basic adapter used by this ORB instance
     */
    public org.jacorb.orb.BasicAdapter getBasicAdapter()
    {
        if( basicAdapter == null )
            throw new INITIALIZE("Adapters not initialized; resolve RootPOA.");
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
        if( rep_id == null )
            rep_id = "IDL:omg.org/CORBA/Object:1.0";

        org.omg.IOP.IOR ior =
            createIOR( rep_id, object_key, _transient, poa, null );

        if (ior == null)
        {
            if (logger.isErrorEnabled())
                logger.error("Interal error: createIOR returns null");
        }

        Delegate d = new Delegate( this, ior );
        try
        {
            d.configure(configuration);
        }
        catch(ConfigurationException ce)
        {
            if (logger.isErrorEnabled())
                logger.error(ce.getMessage(), ce);
        }
        return d.getReference( poa );
    }

    public org.jacorb.poa.POA getRootPOA()
        throws org.omg.CORBA.INITIALIZE
    {
        if( rootpoa == null )
        {
            rootpoa = org.jacorb.poa.POA._POA_init(this);

            basicAdapter = new BasicAdapter( this,
                                             rootpoa,
                                             getTransportManager(),
                                             getGIOPConnectionManager() );

            try
            {
                basicAdapter.configure(configuration);
                rootpoa.configure(configuration);
            }
            catch( ConfigurationException ce )
            {
                throw new org.omg.CORBA.INITIALIZE("ConfigurationException: " +
                                                   ce.getMessage() );
            }
            rootpoa._addPOAEventListener( this );

        }
        return rootpoa;
    }


    /**
     * @return - true if ORB is initialized by an applet and
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
        List l = new ArrayList();

        for( Iterator e = initial_references.keySet().iterator();
             e.hasNext(); l.add( e.next() ) );

        String [] initial_services =
            new String[ services.length + l.size()];

        l.toArray( initial_services );

        System.arraycopy( services, 0, initial_services, l.size(), services.length );
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

            getImR ();

            if( imr != null )
            {
                /* Register the POA */
                String server_name = implName;

                imr.registerPOA( server_name + "/" + poa._getQualifiedName(),
                                 server_name, // logical server name
                                 getServerAddress(),
                                 getServerPort() );
            }
        }
    }


    private void getImR ()
    {
        /* Lookup the implementation repository */
        if( imr == null && useIMR )
        {
            try
            {
                imr = ImRAccessImpl.connect (this);
            }
            catch( Exception e )
            {
                // If we failed to resolve the IMR set the reference to null.
                if (logger.isWarnEnabled())
                {
                    logger.warn("Error: No connection to ImplementationRepository");
                }
                if (logger.isDebugEnabled())
                {
                    logger.debug(e.getMessage());
                }

                if( e instanceof org.omg.CORBA.INTERNAL )
                {
                    throw new org.omg.CORBA.OBJ_ADAPTER ("Unable to resolve ImR");
                }
                else if (e instanceof org.omg.CORBA.TRANSIENT)
                {
                    throw (org.omg.CORBA.TRANSIENT)e;
                }
                else
                {
                    throw new org.omg.CORBA.OBJ_ADAPTER( e.toString() );
                }
            }
        }
    }

    /**
     * Replace the server address in profile with a proxy address if necessary.
     */
    private void patchAddress(IIOPProfile profile,
                              String repId,
                              boolean _transient)
    {
        if (repId.equals ("IDL:org/jacorb/imr/ImplementationRepository:1.0"))
        {
            profile.patchPrimaryAddress(imrProxyHost,imrProxyPort);
        }
        else if (!_transient
                 && useIMR
                 && useIMREndpoint )
        {
            getImR();

            // The double call to patchPrimaryAddress ensures that either the
            // actual imr address or the environment values are patched into the
            // address, giving precedence to the latter.
            profile.patchPrimaryAddress(imr.getImRHost(), imr.getImRPort());
            profile.patchPrimaryAddress(imrProxyHost, imrProxyPort);
        }
        else
        {
            profile.patchPrimaryAddress(iorProxyHost, iorProxyPort);
        }
    }

    /**
     * Creates an ORB_TYPE_ID tagged component for JacORB.
     */
    private TaggedComponent create_ORB_TYPE_ID()
    {
        CDROutputStream orbIDComponentDataStream = new CDROutputStream( this );
        orbIDComponentDataStream.beginEncapsulatedArray();
        orbIDComponentDataStream.write_long( ORBConstants.JACORB_ORB_ID );

        return new TaggedComponent
        (
            TAG_ORB_TYPE.value,
            orbIDComponentDataStream.getBufferCopy()
        );
    }


    /**
     * <code>getServerAddress</code> returns the address to use to locate the
     * server.  Note that this address will be overwritten by the ImR address in
     * the IOR of persistent servers if the use_imr and use_imr_endpoint
     * properties are switched on
     *
     * @return a <code>String</code>, the address for the server.
     */
    private String getServerAddress()
    {
        String address = iorProxyHost;

        if( address == null )
        {
            //property not set
            address = getBasicAdapter().getAddress();
        }
        else
        {
            if (logger.isInfoEnabled())
            {
                logger.info("Using proxy host " + address + " in IOR" );
            }
        }

        return address;
    }


    /**
     * <code>getServerPort</code> returns the port to use to locate the server.
     * Note that this port will be overwritten by the ImR port in the IOR of
     * persistent servers if the use_imr and use_imr_endpoint properties are
     * switched on.
     *
     * @return an <code>int</code>, the port for the server.
     */
    private int getServerPort()
    {
        int port = -1;

        if( iorProxyPort != -1 )
        {
            port = iorProxyPort;

            if( port < 0 )
            {
                throw new BAD_QOS( "Negative port numbers are not allowed! " +
                                   "(check property \"jacorb.ior_proxy_port\")" );
            }

            if (logger.isInfoEnabled())
            {
                logger.info("Using proxy port " + port + " in IOR" );
            }
        }
        else
        {
            //property not set
            port = getBasicAdapter().getPort();
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
        return imrProxyPort;
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
                imr.setServerDown(implName);
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
            String url = null;

            try
            {
                url =
                    configuration.getAttribute("ORBInitRef." + identifier);
            }
            catch( Exception e )
            {
                // ignore
            }

            if( url != null )
            {
                try
                {
                    obj = this.string_to_object( url );
                }
                catch( Exception e )
                {
                    if (logger.isErrorEnabled())
                    {
                        logger.error( "Could not create initial reference for \"" +
                                      identifier + "\"\n" +
                                      "Please check property \"ORBInitRef." +
                                      identifier + '\"' );
                    }
                    if (logger.isDebugEnabled())
                    {
                        logger.debug( e.getMessage() );
                    }

                    throw new org.omg.CORBA.ORBPackage.InvalidName();
                }
            }
            /* "special" behavior follows */
            else if (identifier.equals("NameService") && isApplet())
            {
                try
                {
                    // try to get location of URL with ns's IOR from a file
                    //     called NameService.ior at the applet's host
                    String ior_str =
                        org.jacorb.util.ObjectUtil.readURL("http://"
                                                           + applet.getCodeBase().getHost()
                                                           + "/"
                                                           + "NameService.ior");
                    obj = this.string_to_object (ior_str);
                }
                catch( IOException io )
                {
                    if (logger.isWarnEnabled())
                        logger.warn(io.getMessage());
                }

                if (obj != null)
                {
                    if (! obj._is_a(org.omg.CosNaming.NamingContextHelper.id()))
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
                        Class currentClass =
                            ObjectUtil.classForName( "org.jacorb.security.level2.CurrentImpl" );

                        Constructor constr =
                            currentClass.getConstructor( new Class[]{ org.omg.CORBA.ORB.class });

                        securityCurrent =
                            (org.omg.SecurityLevel2.Current)constr.newInstance( new Object[]{ this });

                        Method configureMethod =
                            currentClass.getDeclaredMethod( "configure",
                                                            new Class[]{ Configuration.class } );

                        configureMethod.invoke( securityCurrent, new Object[]{ configuration });

                        Method init =
                            currentClass.getDeclaredMethod( "init", new Class[0] );

                        init.invoke( securityCurrent, new Object[0] );
                    }
                    catch (Exception e)
                    {
                        if (logger.isWarnEnabled())
                        {
                            logger.warn("Exception",e);
                        }
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
            else if( identifier.equals("ORBPolicyManager") )
            {
                return getPolicyManager();
            }
            else if( identifier.equals("CodecFactory") )
            {
                obj = new CodecFactoryImpl(this);
            }
            else
            {
                throw new org.omg.CORBA.ORBPackage.InvalidName();
            }

            if (obj != null)
            {
                initial_references.put (identifier, obj);
            }

            return obj;
        }
    }

    PolicyManager getPolicyManager()
    {
        return policyManager;
    }

    /**
     * Register a reference, that will be returned on subsequent calls
     * to resove_initial_references(id). <br>
     * The references "RootPOA", "POACurrent" and "PICurrent" can be set,
     * but will not be resolved with the passed in references.
     * <p>
     * Overrides  register_initial_reference() in org.omg.CORBA_2_5.ORB
     *
     * @param id The references human-readable id, e.g. "MyService".
     * @param obj The objects reference.
     * @exception InvalidName A reference with id has already been registered.
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
            if (logger.isDebugEnabled())
            {
                logger.debug( "Registering initial ref " + id );
            }
            initial_references.put(id, obj);
        }
    }

    public void run()
    {
        if (logger.isInfoEnabled())
        {
            logger.info("ORB run");
        }

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
        catch (InterruptedException ex)
        {
        }
        if (logger.isInfoEnabled())
        {
            logger.info("ORB run, exit");
        }
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
     * called from ORB.init(), entry point for initialization.
     */

    protected void set_parameters(String[] args, java.util.Properties props)
    {
        _props = props;        

        try
        {
            configure( org.jacorb.config.Configuration.getConfiguration(props, this));
        }
        catch( ConfigurationException ce )
        {
            if ( logger != null && logger.isErrorEnabled())
            {
                logger.error( ce.getMessage());
            }
            else
                ce.printStackTrace();

            throw new org.omg.CORBA.INITIALIZE( ce.getMessage() );
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
                    if ( equals_pos == -1 )
                    {
                        throw new org.omg.CORBA.BAD_PARAM( "InitRef format invalid for " + prop );
                    }

                    //add the property to environment
                    ((DefaultConfiguration)configuration).setAttribute( prop.substring( 0, equals_pos ),
                                                                        prop.substring( equals_pos + 1) );
                }
                else if( arg.equals( "-ORBInitRef" ))
                {
                    //This is the compliant form -ORBInitRef <name>=<val>

                    //Is there a next arg?
                    if( (args.length - 1) < (i + 1) )
                    {
                        if (logger.isWarnEnabled())
                        {
                            logger.warn( "WARNING: -ORBInitRef argument without value" );
                        }
                        continue;
                    }

                    String prop = args[ ++i ].trim();

                    //find the equals char that separates prop name from
                    //prop value
                    int equals_pos = prop.indexOf( '=' );
                    if ( equals_pos == -1 )
                    {
                        throw new org.omg.CORBA.BAD_PARAM( "InitRef format invalid for " + prop );
                    }

                    //add the property to environment
                     ((DefaultConfiguration)configuration).setAttribute( "ORBInitRef." +
                                                                         prop.substring( 0, equals_pos ),
                                                                         prop.substring( equals_pos + 1) );
                }
            }
        }
        policyManager = new PolicyManager( this );

        try
        {
            clientConnectionManager =
                new ClientConnectionManager( this,
                                             getTransportManager(),
                                             getGIOPConnectionManager());
            clientConnectionManager.configure(configuration);
        }
        catch( ConfigurationException ce )
        {
            if (logger.isErrorEnabled())
                logger.error(ce.getMessage());
        }


        //String s = Environment.getProperty( "jacorb.hashtable_class" );
        if( hashTableClassName == null || hashTableClassName.length() == 0 )
        {
            if (logger.isInfoEnabled())
            {
                logger.info("Property \"jacorb.hashtable_class\" not present. Will use default hashtable implementation" );
            }
            knownReferences = new HashMap();
        }
        else
        {
            try
            {
                knownReferences =
                    (Map)ObjectUtil.classForName( hashTableClassName ).newInstance();
            }
            catch( Exception e )
            {
                if (logger.isInfoEnabled())
                {
                    logger.info(e.getMessage());
                }
                knownReferences = new HashMap();
            }
        }

        interceptorInit();
    }

    /**
     * Initialization method, called from within the super class
     * org.omg.CORBA.ORB
     */

    protected void set_parameters(java.applet.Applet app, java.util.Properties props)
    {
        applet = app;
        _props = props;

        try
        {
            configure( org.jacorb.config.Configuration.getConfiguration(props, this));
        }
        catch( ConfigurationException ce )
        {
            if (logger.isErrorEnabled())
            {
                logger.error( ce.getMessage());
            }
            throw new org.omg.CORBA.INITIALIZE( ce.getMessage() );
        }


        try
        {
            clientConnectionManager =
                new ClientConnectionManager( this,
                                             getTransportManager(),
                                             getGIOPConnectionManager() );
            clientConnectionManager.configure(configuration);
        }
        catch( ConfigurationException ce )
        {
            if (logger.isErrorEnabled())
                logger.error(ce.getMessage());
        }

        if( hashTableClassName == null || hashTableClassName.length() == 0 )
        {
            if (logger.isInfoEnabled())
            {
                logger.info("Property \"jacorb.hashtable_class\" not present. Will use default hashtable implementation" );
            }
            knownReferences = new HashMap();
        }
        else
        {
            try
            {
                knownReferences =
                    (Map)ObjectUtil.classForName( hashTableClassName ).newInstance();
            }
            catch( Exception e )
            {
                if (logger.isInfoEnabled())
                {
                    logger.info(e.getMessage());
                }
                knownReferences = new HashMap();
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
        Vector orb_initializers = getORBInitializers();

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
                    if (logger.isWarnEnabled())
                    {
                        logger.warn( e.getMessage());
                    }
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
                    if (logger.isWarnEnabled())
                    {
                        logger.warn( e.getMessage());
                    }
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
        }
    }


    /**
     * Collects all properties with prefix "org.omg.PortableInterceptor.ORBInitializerClass."
     * and try to instantiate their values as ORBInitializer-Classes.
     *
     * @return a Vector containing ORBInitializer instances
     */

    private Vector getORBInitializers()
    {
        String[] prop_names = configuration.getAttributeNames();
        Vector orb_initializers = new Vector();

        String initializer_prefix =
            "org.omg.PortableInterceptor.ORBInitializerClass.";

        //Test EVERY property if prefix matches.
        for(int i = 0; i < prop_names.length; i++)
        {
            if ( prop_names[i].startsWith( initializer_prefix ))
            {
                String name = null;
//                 try
//                 {
                    name = configuration.getAttribute( prop_names[i], "" );
 //                }
//                 catch( ConfigurationException ce )
//                 {
//                     ce.printStackTrace(); // debug
//                 }

                if( name.length() == 0 )
                {
                    if( prop_names[i].length() > initializer_prefix.length() )
                    {
                        name =
                            prop_names[i].substring( initializer_prefix.length() );
                    }
                }

                if( name == null )
                {
                    continue;
                }

                try
                {
                    orb_initializers.addElement(ObjectUtil.classForName(name).newInstance());
                    if( logger.isDebugEnabled())
                        logger.debug("Build: " + name);
                }
                catch (Exception e)
                {
                    if( logger.isDebugEnabled())
                        logger.debug( "Unable to build ORBInitializer from >>" +
                                      name + "<<" );
                }
            }
        }

        return orb_initializers;
    }




    public void shutdown( boolean wait_for_completion )
    {
        if (logger.isInfoEnabled())
        {
            logger.info("prepare ORB for shutdown...");
        }

        if( ! run )
        {
            return; // ORB already shut down...
        }

        synchronized( shutdown_synch )
        {
            if (logger.isInfoEnabled())
            {
                logger.info("ORB going down...");
            }

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

                    if (logger.isDebugEnabled())
                    {
                        logger.debug("ORB going shutdown complete (1)");
                    }

                    return;
                }
            }
            else if( shutdown_in_progress && !wait_for_completion )
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("ORB going shutdown complete (2)");
                }
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

        if (logger.isDebugEnabled())
        {
            logger.debug("ORB going shutdown (cleaning up ORB...)");
        }

        clientConnectionManager.shutdown();
        knownReferences.clear();
        bufferManager.release();

        /* notify all threads waiting for shutdown to complete */

        synchronized( shutdown_synch )
        {
            shutdown_synch.notifyAll();
        }

        /* notify all threads waiting in orb.run() */
        synchronized( orb_synch )
        {
            run = false;
            orb_synch.notifyAll();
        }
        if (logger.isInfoEnabled())
        {
            logger.info("ORB shutdown complete");
        }
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
            ParsedIOR pior = new ParsedIOR( str, this, logger );
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
            if (logger.isDebugEnabled())
            {
                logger.debug(iae.getMessage());
            }
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
            throw new BAD_PARAM("Argument has a delegate whose class is "
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

    public ValueFactory register_value_factory(String id,
                                                ValueFactory factory)
    {
        return (ValueFactory)valueFactories.put (id, factory);
    }

    public void unregister_value_factory(String id)
    {
        valueFactories.remove (id);
    }

    public ValueFactory lookup_value_factory(String id)
    {
        ValueFactory result = (ValueFactory)valueFactories.get (id);

        if (result == null)
        {
            if (id.startsWith("IDL"))
            {
                String valueName = org.jacorb.ir.RepositoryID.className(id, null);
                result = findValueFactory(valueName);
                valueFactories.put(id, result);
            }
        }
        return result;
    }

    /**
     * Finds a ValueFactory for class valueName by trying standard class names.
     */

    private ValueFactory findValueFactory(String valueName)
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
            return is.read_value(value);
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
            result = ObjectUtil.classForName(name);
        }
        catch (ClassNotFoundException e)
        {
            if (orgomg && name.startsWith ("org.omg"))
                try
                {
                    result = ObjectUtil.classForName("omg.org" + name.substring(7));
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
     * Returns a BoxedValueHelper for the type specified by repId, or
     * null if no such BoxedValueHelper can be found.  This method uses an
     * internal cache of BoxedValueHelpers so that each class needs only
     * be looked up once.
     *
     * @param repId the repository id of the type for which a BoxedValueHelper
     * should be returned.  It is assumed that repId is the repository id of a
     * boxed value type.  Otherwise, the result will be null.
     * @return an instance of the BoxedValueHelper class that corresponds
     * to repId.
     */

    public BoxedValueHelper getBoxedValueHelper(String repId)
    {
        BoxedValueHelper result = (BoxedValueHelper)boxedValueHelpers.get(repId);
        if (result == null)
        {
            if (boxedValueHelpers.containsKey(repId))
                return null;
            else
            {
                result = org.jacorb.ir.RepositoryID.createBoxedValueHelper(repId, null);
                boxedValueHelpers.put(repId, result);
            }
        }
        return result;
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

    public TransportManager getTransportManager()
    {
        if (transport_manager == null)
        {
            transport_manager = new TransportManager(this);
            try
            {
                transport_manager.configure(configuration);
            }
            catch( ConfigurationException ce )
            {
                throw new INTERNAL(ce.getMessage());
            }
        }
        return transport_manager;
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
     * a helper method supplied to initialize the object key map. This
     * replaces functionality from the defunct Environment class to populate
     * a hash map based on the names starting with "jacorb.orb.ObjectKeyMap"
     */

    private void configureObjectKeyMap (Configuration config)
    {
        String[] names = config.getAttributeNames();
        String prefix = "jacorb.orb.objectKeyMap.";
        for (int i = 0; i < names.length; i++)
            if (names[i].startsWith (prefix))
                objectKeyMap.put(names[i].substring (prefix.length()),
                                 config.getAttribute (names[i],""));
    }

    /**
     * <code>addObjectKey </code> is a proprietary method that allows the
     * internal objectKeyMap to be altered programmatically. The objectKeyMap
     * allows more readable corbaloc URLs by mapping the actual object key to
     * an arbitary string. See the jacorb.properties file for more information.
     *
     * @param key_name a <code>String</code> value e.g. NameService
     * @param full_path an <code>String</code> value e.g. file:/home/rnc/NameSingleton.ior
     */
    public void addObjectKey(String key_name, String full_path)
    {
        objectKeyMap.put(key_name, full_path);
    }


    /**
     * Map an object key to another, as defined by the value
     * of a corresponding configuration property in the properties
     * file, e.g. map "NameService" to "StandardNS/NameServer-POA/_root"
     *
     * @param originalKey a <code>byte[]</code> value containing the original
     * key.
     * @return a <code>byte[]</code> value containing the mapped key, if a
     * mapping is defined, originalKey otherwise.
     */
    public byte[] mapObjectKey( byte[] originalKey )
    {
        BufferedReader br        = null;
        File           iorFile   = null;
        ParsedIOR      pIOR      = null;
        String         found     = null;
        String         original  = null;

        if( objectKeyMap.size() != 0 )
        {
            original = new String( originalKey );
            found    = (String)objectKeyMap.get( original );

            if( found != null )
            {
                if ( ParsedIOR.isParsableProtocol ( found ) )
                {
                    // We have found a file reference. Use ParsedIOR to get
                    // the byte key.
                    try
                    {
                        pIOR = new ParsedIOR( found, this, logger );
                        return pIOR.get_object_key();
                    }
                    catch ( IllegalArgumentException e )
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Error - could not read protocol " + found );
                        }
                        return originalKey;
                    }
                }
                else
                {
                    return org.jacorb.orb.util.CorbaLoc.parseKey(found);
                }
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
