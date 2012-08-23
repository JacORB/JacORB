package org.jacorb.orb;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.imr.ImRAccessImpl;
import org.jacorb.ir.RepositoryID;
import org.jacorb.orb.giop.ClientConnection;
import org.jacorb.orb.giop.ClientConnectionManager;
import org.jacorb.orb.giop.LocateReplyInputStream;
import org.jacorb.orb.giop.LocateRequestOutputStream;
import org.jacorb.orb.giop.ReplyInputStream;
import org.jacorb.orb.giop.RequestOutputStream;
import org.jacorb.orb.iiop.IIOPProfile;
import org.jacorb.orb.miop.MIOPProfile;
import org.jacorb.orb.policies.PolicyManager;
import org.jacorb.orb.portableInterceptor.ClientInterceptorIterator;
import org.jacorb.orb.portableInterceptor.ClientRequestInfoImpl;
import org.jacorb.orb.portableInterceptor.InterceptorManager;
import org.jacorb.orb.portableInterceptor.ServerInterceptorIterator;
import org.jacorb.orb.portableInterceptor.ServerRequestInfoImpl;
import org.jacorb.orb.util.CorbaLoc;
import org.jacorb.poa.util.POAUtil;
import org.jacorb.util.ObjectUtil;
import org.jacorb.util.SelectorManager;
import org.jacorb.util.Time;
import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.INV_OBJREF;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.OBJ_ADAPTER;
import org.omg.CORBA.Policy;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TIMEOUT;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.ObjectImpl;
import org.omg.CORBA.portable.RemarshalException;
import org.omg.CORBA.portable.ServantObject;
import org.omg.ETF.Profile;
import org.omg.GIOP.LocateStatusType_1_2;
import org.omg.IOP.IOR;
import org.omg.IOP.ServiceContext;
import org.omg.Messaging.RELATIVE_REQ_TIMEOUT_POLICY_TYPE;
import org.omg.Messaging.RELATIVE_RT_TIMEOUT_POLICY_TYPE;
import org.omg.Messaging.REPLY_END_TIME_POLICY_TYPE;
import org.omg.Messaging.REPLY_START_TIME_POLICY_TYPE;
import org.omg.Messaging.REQUEST_END_TIME_POLICY_TYPE;
import org.omg.Messaging.REQUEST_START_TIME_POLICY_TYPE;
import org.omg.Messaging.SYNC_NONE;
import org.omg.Messaging.SYNC_SCOPE_POLICY_TYPE;
import org.omg.Messaging.SYNC_WITH_SERVER;
import org.omg.Messaging.SYNC_WITH_TARGET;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.LOCATION_FORWARD;
import org.omg.PortableInterceptor.SUCCESSFUL;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.omg.PortableInterceptor.USER_EXCEPTION;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantActivator;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.WrongAdapter;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;
import org.omg.SSLIOP.SSL;
import org.omg.SSLIOP.SSLHelper;
import org.omg.TimeBase.UtcT;
import org.slf4j.Logger;

/**
 * JacORB implementation of CORBA object reference
 *
 * @author Gerald Brose
 *
 */

public final class Delegate
    extends org.omg.CORBA_2_3.portable.Delegate
{
    // WARNING: DO NOT USE _pior DIRECTLY, BECAUSE THAT IS NOT MT
    // SAFE. USE getParsedIOR() INSTEAD, AND KEEP A METHOD-LOCAL COPY
    // OF THE REFERENCE.
    private ParsedIOR _pior = null;
    private IOR ior = null;

    /**
     * Normally a delegate only has one connection open at a time. With the addition
     * of MIOP it is possible for the user to request a two-way operation (via e.g. IIOP)
     * and then a one-way operation via MIOP. Rather than repeatedly calling
     * ConnectionManager.getConnection the additional MIOP connection will also be cached
     * here.
     */
    private ClientConnection connections[] = new ClientConnection[2];

    private enum TransportType { IIOP, MIOP };

    /**
     * The current connection; this should be either IIOP or MIOP where
     * 0 = IIOP
     * 1 = MIOP
     */
    private TransportType currentConnection = TransportType.IIOP;

    /* save original ior for fallback */
    private ParsedIOR piorOriginal = null;

    /* save iors to detect and prevent locate forward loop */
    private ParsedIOR piorLastFailed = null;

    /* flag to indicate if this is the delegate for the ImR */
    private boolean isImR = false;

    private boolean bound = false;
    private org.jacorb.poa.POA poa;

    private final org.jacorb.orb.ORB orb;
    private final Logger logger;

    /** set after the first attempt to determine whether
        this reference is to a local object */
    private boolean resolved_locality = false;

    private final ConcurrentHashMap<org.omg.ETF.Profile, ReplyGroup> groups;

    private final java.lang.Object bind_sync = new java.lang.Object();

    private boolean locate_on_bind_performed = false;

    private final ClientConnectionManager conn_mg;

    private final Map<Integer, Policy> policy_overrides;

    private CookieHolder cookie = null;

    private boolean clearCurrentContext = true;

    private String invokedOperation = null;

    private final SelectorManager selectorManager;

    /**
     * <code>localInterceptors</code> stores the ClientInterceptorHandler that is
     * currently in use during an interceptor invocation. It is held within a
     * thread local to prevent thread interaction issues.
     */
    private static final ThreadLocal<ClientInterceptorHandler> localInterceptors = new ThreadLocal<ClientInterceptorHandler>();

    /** the configuration object for this delegate */
    private final Configuration configuration;

    /** configuration properties */
    private final boolean useIMR;
    private boolean locateOnBind;

    /** mitigate the symptom of an infinite loop in some imr failure cases, see bug 810 */
    private int maxBuiltinRetries = 0;

   /**
    * This is the default GIOP minor version configured by JacORB. Most of the time
    * JacORB will use whatever is within the effective profile. In the case of MIOP this
    * information may not be available to it will use this to set the GIOP version.
    */
    private int defaultGiopMinor;

    /**
     * specify if this Delegate should drop its connection to the remote ORB after
     * a non-recoverable SystemException occured. non-recoverable SystemException
     * couldn't be handled by rebind.
     */
    private boolean disconnectAfterNonRecoverableSystemException;

    /**
     * Always attempt to locate is_a information locally rather than remotely.
     */
    private boolean avoidIsARemoteCall=true;

    /**
     * Preserve JacORB 2.3.x series behaviour with interceptors which restores the
     * 2.3.x behaviour of returning false from an is_local call when interceptors are
     * enabled and installed.
     *
     * This should NOT be required in general and has only been added for certain scenarios
     * with legacy applications. Do not remove without checking with Nick Cross (13/April/2012)
     */
    private boolean isLocalHistoricalInterceptors = false;


    /**
     *  Denote whether to allow orbPolicies or do optimised version
     */
    private boolean disableClientOrbPolicies;



    public static enum INVOCATION_KEY
    {
       REQUEST_END_TIME,
       REPLY_END_TIME,
       /**
        * This is used to indicate that a current context popped from the Delegates
        * invocationContext stack was pushed there prior to an interceptor call.  We
        * need this to ensure that the context is not popped if a CORBA call is made
        * by the interceptor.  The context must be popped on return from the
        * interceptor.
        */
       INTERCEPTOR_CALL,
       SERVANT_PREINVOKE
    };

    /**
     * 03-09-04: 1.5.2.2
     *
     * boolean threadlocal to ensure that
     * after servant_preinvoke has returned null the
     * next call to is_local will return false
     * so that the stub will choose the non-optimized path
     */
    private static final ThreadLocal<Boolean> ignoreNextCallToIsLocal = new ThreadLocal<Boolean>()
    {
        protected Boolean initialValue()
        {
            return Boolean.FALSE;
        }
    };


    /**
     * @see #getInvocationContext()
     * @see #clearInvocationContext()
     * This is a <code>Stack</code> containing HashMaps.  The HashMap will
     * contain values that apply to the current invocation.  A Stack of
     * HashMaps is needed to cater for the fact that invocations can be
     * made within invocations and the values may differ for each invocation.
     * We need to retain the values for the original invocation as well as
     * apply the correct values for any internal invocation.
     */
    private static final ThreadLocal<Stack<Map<INVOCATION_KEY, UtcT>>> invocationContext = new ThreadLocal<Stack<Map<INVOCATION_KEY, UtcT>>>()
    {
        protected Stack<Map<INVOCATION_KEY, UtcT>> initialValue()
        {
            return new Stack<Map<INVOCATION_KEY, UtcT>> ();
        };
    };

    /**
     * access the current invocation context (its a Stack of Maps).
     * this context lives as long as the invocation is active.
     * especially it outlives RemarshalExceptions and thus
     * can be used to share information between mutiple requests
     * that are done as part of an invocation.
     */
    public static final Stack<Map<INVOCATION_KEY, UtcT>> getInvocationContext()
    {
        return invocationContext.get();
    }

    /**
     * clear the current invocation context. must be invoked
     * before control is returned to the client
     * (exceptions, orderly termination).
     * mustn't be reset in case of RemarshalExceptions.
     */
    public static void clearInvocationContext()
    {
        if ( ! ( getInvocationContext()).empty())
        {
            ( getInvocationContext()).pop();
        }
    }

    /**
     * A general note on the synchronization concept
     *
     * The main problem that has to be addressed by synchronization
     * means is the case when an object reference is shared by
     * threads, and LocationForwards (e.g. thrown by the ImR) or
     * ForwardRequest (thrown by ClientInterceptors) involved. In
     * these cases, the rebinding to another target can occur while
     * there are still other requests active. Therefore, the act of
     * rebinding must be synchronized, so every thread sees a
     * consistent state.
     *
     * Synchronization is done via the bind_sync object. Please also
     * have a look at the comment for operation bind().
     */


    /* constructors: */
    private Delegate(ORB orb, Configuration config, boolean parseIORLazy)
    {
        super();

        this.orb = orb;
        configuration = config;

        conn_mg = orb.getClientConnectionManager();

        selectorManager = orb.getSelectorManager ();

        logger = ((Configuration)config).getLogger("jacorb.orb.delegate");
        useIMR =
            config.getAttributeAsBoolean("jacorb.use_imr", false);
        locateOnBind =
            config.getAttributeAsBoolean("jacorb.locate_on_bind", false);
        avoidIsARemoteCall =
            config.getAttributeAsBoolean("jacorb.avoidIsARemoteCall", true);
        isLocalHistoricalInterceptors =
            config.getAttributeAsBoolean("jacorb.isLocalHistoricalInterceptors", false);
        try
        {
            maxBuiltinRetries =
                config.getAttributeAsInteger ("jacorb.maxBuiltinRetries", 0);
            if (maxBuiltinRetries < 0)
            {
                logger.error ("Configuration error - max builtin retries < 0");
                throw new INTERNAL ("Configuration error - max builtin retries < 0");
            }
        }
        catch (ConfigurationException ex)
        {
            logger.error ("Configuration exception retrieving max builtin retries", ex);
            throw new INTERNAL ("Configuration exception retrieving max builtin retries" + ex);
        }
        disconnectAfterNonRecoverableSystemException = config.getAttributeAsBoolean
            ("jacorb.delegate.disconnect_after_systemexception", true);

        disableClientOrbPolicies = config.getAttributeAsBoolean("jacorb.disableClientOrbPolicies", false);
        try
        {
           defaultGiopMinor = configuration.getAttributeAsInteger ("jacorb.giop_minor_version", 2);
        }
        catch (ConfigurationException ex)
        {
            logger.error ("Configuration exception retrieving giop minor version", ex);
            throw new INTERNAL ("Configuration exception retrieving giop minor version" + ex);
        }

        // standard initialization
        groups = new ConcurrentHashMap<org.omg.ETF.Profile, ReplyGroup>();
        if (parseIORLazy)
        {
            // reference received across the net

            if (disableClientOrbPolicies)
            {
                policy_overrides = Collections.emptyMap ();
            }
            else
            {
                policy_overrides = new HashMap<Integer, Policy>(0);
            }
        }
        else
        {
            // reference locally created

            if (disableClientOrbPolicies)
            {
                policy_overrides = Collections.emptyMap ();
            }
            else
            {
                policy_overrides = new HashMap<Integer, Policy>();
            }
        }
    }

    private Delegate(ORB orb, boolean parseIORLazy)
    {
        this(orb, orb.getConfiguration(), parseIORLazy);
    }

    public Delegate ( org.jacorb.orb.ORB orb, ParsedIOR pior )
    {
        this(orb, false);
        _pior = pior;
        checkIfImR( _pior.getTypeId() );
    }

    public Delegate(org.jacorb.orb.ORB orb, IOR ior, boolean parseIORLazy)
    {
        this(orb, parseIORLazy);
        this.ior = ior;

        if (parseIORLazy)
        {
            // postpone parsing of IOR.
            // see getParsedIOR
        }
        else
        {
            getParsedIOR();
        }
        checkIfImR( ior.type_id );
    }

    public Delegate(org.jacorb.orb.ORB orb, org.omg.IOP.IOR ior)
    {
        this(orb, ior, false);
    }

    /**
     * Method to determine if this delegate is the delegate for the ImR.
     * This information is needed when trying to determine if the ImR has
     * gone down and come back up at a different addresss.  All delegates
     * except the delegate of the ImR itself will try to determine if the
     * ImR has gone down and come back up at a new address if a connection
     * to the ImR can't be made.  If the delegate of the ImR itself has
     * failed to connect then the ImR hasn't come back up!
     */
    private void checkIfImR( String typeId )
    {
        if ("IDL:org/jacorb/imr/ImplementationRepository:1.0".equals (typeId))
        {
            isImR = true;
        }
    }


    public int _get_TCKind()
    {
        return org.omg.CORBA.TCKind._tk_objref;
    }

    /**
     * This bind is a combination of the old _init() and bind()
     * operations. It first inits this delegate with the information
     * supplied by the (parsed) IOR. Then it requests a new
     * ClientConnection from the ConnectionsManager. This will *NOT*
     * open up a TCP connection, but the connection is needed for the
     * GIOP message ids. The actual TCP connection is automatically
     * opened up by the ClientConnection, when the first request is
     * sent. This has the advantage, that COMM_FAILURES can only occur
     * inside of _invoke, where they get handled properly (falling
     * back, etc.)
     */
    private void bind()
    {
        synchronized (bind_sync)
        {
            if ( bound )
            {
                return;
            }

            final ParsedIOR ior = getParsedIOR();

            // Check if ClientProtocolPolicy set, if so, set profile
            // selector for IOR that selects effective profile for protocol
            org.omg.RTCORBA.Protocol[] protocols = getClientProtocols();
            if (protocols != null)
            {
                ior.setProfileSelector(new SpecificProfileSelector(protocols));
            }

            org.omg.ETF.Profile profile = ior.getEffectiveProfile();

            if (profile == null)
            {
                throw new org.omg.CORBA.COMM_FAILURE ("no effective profile");
            }

            patchSSL(profile, ior);

            //MIOP
            if(profile instanceof MIOPProfile)
            {
                connections[TransportType.MIOP.ordinal ()] = conn_mg.getConnection(profile);
                profile = ((MIOPProfile)profile).getGroupIIOPProfile();
            }

            if (profile != null)
            {
                connections[TransportType.IIOP.ordinal ()] = conn_mg.getConnection(profile);
            }

            bound = true;

            /* The delegate could query the server for the object
             *  location using a GIOP locate request to make sure the
             *  first call will get through without redirections
             *  (provided the server's answer is definite):
             */
            if (( ! locate_on_bind_performed ) &&
                    locateOnBind )
            {
                //only locate once, because bind is called from the
                //switch statement below again.
                locate_on_bind_performed = true;

                try
                {
                    LocateRequestOutputStream lros =
                        new LocateRequestOutputStream( orb,
                                                       ior.get_object_key(),
                                                       connections[currentConnection.ordinal ()].getId(),
                                                       ior.getEffectiveProfile().version().minor );

                    LocateReplyReceiver receiver = new LocateReplyReceiver();
                    receiver.configure (configuration);

                    connections[currentConnection.ordinal ()].sendRequest( lros,
                                            receiver,
                                            lros.getRequestId(),
                                            true ); //response expected

                    LocateReplyInputStream lris = receiver.getReply();

                    switch ( lris.rep_hdr.locate_status.value() )
                    {

                    case LocateStatusType_1_2._UNKNOWN_OBJECT :
                        {
                            throw new org.omg.CORBA.UNKNOWN( "Could not bind to object, server does not know it!" );
                        }

                    case LocateStatusType_1_2._OBJECT_HERE :
                        {
                            break;
                        }

                    case LocateStatusType_1_2._OBJECT_FORWARD :
                        {
                            //fall through
                        }

                    case LocateStatusType_1_2._OBJECT_FORWARD_PERM :
                        {
                            //_OBJECT_FORWARD_PERM is actually more or
                            //less deprecated
                            rebind(lris.read_Object());
                            break;
                        }

                    case LocateStatusType_1_2._LOC_SYSTEM_EXCEPTION :
                        {
                            throw SystemExceptionHelper.read( lris );
                        }

                    case LocateStatusType_1_2._LOC_NEEDS_ADDRESSING_MODE :
                        {
                            throw new org.omg.CORBA.NO_IMPLEMENT( "Server responded to LocateRequest with a status of LOC_NEEDS_ADDRESSING_MODE, but this isn't yet implemented by JacORB" );
                        }

                    default :
                        {
                            throw new IllegalArgumentException( "Unknown reply status for LOCATE_REQUEST: " + lris.rep_hdr.locate_status.value() );
                        }

                    }

                }
                catch ( org.omg.CORBA.SystemException se )
                {
                    //rethrow
                    throw se;
                }
                catch ( Exception e )
                {
                    if (logger.isWarnEnabled())
                    {
                        logger.warn( e.getMessage() );
                    }
                }
            }
        }
    }

    private void patchSSL(Profile profile, ParsedIOR ior)
    {
        if (!(profile instanceof IIOPProfile))
        {
            return;
        }

        IIOPProfile iiopProfile = (IIOPProfile) profile;

        if (iiopProfile.version().minor != 0)
        {
            return;
        }

        TaggedComponentList multipleComponents = ior.getMultipleComponents();

        SSL ssl = (SSL)multipleComponents.getComponent( org.omg.SSLIOP.TAG_SSL_SEC_TRANS.value, SSLHelper.class );

        if (ssl != null)
        {
            logger.debug("patching GIOP 1.0 profile to contain SSL information from the multiple components profile");
            iiopProfile.addComponent(org.omg.SSLIOP.TAG_SSL_SEC_TRANS.value, ssl, SSLHelper.class);
        }
    }

    public void rebind(org.omg.CORBA.Object obj)
    {
        String object_reference = orb.object_to_string(obj);

        if (object_reference != null && object_reference.indexOf( "IOR:" ) == 0)
        {
            rebind(new ParsedIOR( orb, object_reference));
        }
        else
        {
            throw new INV_OBJREF ("Not an IOR: " + object_reference);
        }
    }

    public void rebind(ParsedIOR pior)
    {
        synchronized ( bind_sync )
        {
            // Do the ParsedIORs currently match.
            final ParsedIOR originalIOR = getParsedIOR();
            boolean originalMatch = originalIOR.equals(pior);

            // Check if ClientProtocolPolicy set, if so, set profile
            // selector for IOR that selects effective profile for protocol
            org.omg.RTCORBA.Protocol[] protocols = getClientProtocols();

            if (protocols != null)
            {
                pior.setProfileSelector(new SpecificProfileSelector(protocols));
            }

            // While the target override may have altered the effective profile so that
            // the IORs are now equal if the original ones do not match we still have to
            // disconnect so that the connection is made with the correct effective profile.
            if (originalMatch && pior.equals(originalIOR))
            {
                //already bound to target so just return
                return ;
            }

            if (piorLastFailed != null && piorLastFailed.equals(pior))
            {
                //we've already failed to bind to the ior
                throw new org.omg.CORBA.TRANSIENT();
            }

            if (piorOriginal == null)
            {
                //keep original pior for fallback
                piorOriginal = _pior;
            }

            _pior = pior;

             if (connections[TransportType.IIOP.ordinal ()] != null)
             {
                 conn_mg.releaseConnection( connections[TransportType.IIOP.ordinal ()] );
                 connections[TransportType.IIOP.ordinal ()] = null;
             }
             if ( connections[TransportType.MIOP.ordinal ()] != null )
             {
                 conn_mg.releaseConnection( connections[TransportType.MIOP.ordinal ()] );
                 connections[TransportType.MIOP.ordinal ()] = null;
             }

            //to tell bind() that it has to take action
            bound = false;

            bind();
        }
    }

    public org.omg.CORBA.Request create_request( org.omg.CORBA.Object self,
            org.omg.CORBA.Context ctx,
            String operation,
            org.omg.CORBA.NVList args,
            org.omg.CORBA.NamedValue result )
    {
        checkORB();

        bind();

        return new org.jacorb.orb.dii.Request( self,
                                               orb,
                                               connections[currentConnection.ordinal ()],
                                               getParsedIOR().get_object_key(),
                                               operation,
                                               args,
                                               ctx,
                                               result );
    }

    public org.omg.CORBA.Request create_request( org.omg.CORBA.Object self,
            org.omg.CORBA.Context ctx,
            String operation,
            org.omg.CORBA.NVList args,
            org.omg.CORBA.NamedValue result,
            org.omg.CORBA.ExceptionList exceptions,
            org.omg.CORBA.ContextList contexts )
    {
        checkORB();

        bind();

        return new org.jacorb.orb.dii.Request( self,
                                               orb,
                                               connections[currentConnection.ordinal ()],
                                               getParsedIOR().get_object_key(),
                                               operation,
                                               args,
                                               ctx,
                                               result,
                                               exceptions,
                                               contexts);
    }

    public org.omg.CORBA.Object duplicate( org.omg.CORBA.Object self )
    {
        return orb._getDelegate (new ParsedIOR( orb, toString()));
    }

    public boolean equals(java.lang.Object obj)
    {
        return ( obj instanceof org.omg.CORBA.Object &&
                 toString().equals( obj.toString() ) );
    }

    public boolean equals( org.omg.CORBA.Object self, java.lang.Object obj )
    {
        return equals( obj );
    }

    // The finalize method has been removed due to performance and scalability
    //  issues.
    //
    // This method was called when a client-side stub was garbage-collected.  In
    // effect it caused the Delegate to unregister itself from the underlying
    // GIOPConnection and if there were no other Delegates using that
    // connection, it was be closed and disposed of altogether.
    //
    // This therefore moves the responsibility to the client code to call _release.
    //

    public org.omg.CORBA.DomainManager[] get_domain_managers( org.omg.CORBA.Object self )
    {
        return null;
    }

    /**
     * The get_policy operation returns the policy object of the
     * specified type, which applies to this object. It returns the
     * effective Policy for the object reference. The effective Policy
     * is the one that would be used if a request were made.  This
     * Policy is determined first by obtaining the effective override
     * for the PolicyType as returned by get_client_policy. The
     * effective override is then compared with the Policy as
     * specified in the IOR.
     * <p>
     * The effective Policy is determined by reconciling the effective
     * override and the IOR-specified Policy. If the two policies
     * cannot be reconciled, the standard system exception INV_POLICY
     * is raised with standard minor code 1. The absence of a Policy
     * value in the IOR implies that any legal value may be used.
     */

    public org.omg.CORBA.Policy get_policy( org.omg.CORBA.Object self,
                                            int policy_type )
    {
        Policy result = get_client_policy(policy_type);
        if (result != null)
        {
            // TODO: "reconcile" with server-side policy
            return result;
        }
        // if not locally overridden, ask the server
        return get_policy( self,
                           policy_type,
                           request( self, "_get_policy", true ) );
    }

    /**
     * Gets the effective overriding policy with the given type from
     * the client-side, or null if this policy type is unset.
     *
     * (Implementation is incomplete, we don't check PolicyCurrent, i.e.
     * at the thread-level)
     */

    public org.omg.CORBA.Policy get_client_policy(int policy_type)
    {
        Policy result = null;

        if (disableClientOrbPolicies)
        {
            return null;
        }

        synchronized(policy_overrides)
        {
            final Integer key = Integer.valueOf(policy_type);
            result = policy_overrides.get(key);
        }

        if ( result == null )
        {
            // no override at the object level for this type, now
            // check at the thread level, ie PolicyCurrent.
            // TODO: currently not implemented

            // check at the ORB-level
            final PolicyManager policyManager = orb.getPolicyManager();
            if (policyManager != null)
            {
                Policy[] orbPolicies = policyManager.get_policy_overrides (new int[] {policy_type});
                if (orbPolicies!= null && orbPolicies.length == 1)
                {
                    result = orbPolicies[0];
                }
            }
        }

        return result;
    }


    public org.omg.CORBA.Policy get_policy( org.omg.CORBA.Object self,
                                            int policy_type,
                                            org.omg.CORBA.portable.OutputStream os )
    {
        // ask object implementation
        while ( true )
        {
            try
            {
                os.write_Object( self );
                os.write_long( policy_type );
                org.omg.CORBA.portable.InputStream is = invoke( self, os );
                return org.omg.CORBA.PolicyHelper.narrow( is.read_Object() );
            }
            catch ( RemarshalException r ) // NOPMD
            {
                // Ignored
            }
            catch ( ApplicationException _ax )
            {
                String _id = _ax.getId();
                throw new INTERNAL( "Unexpected exception " + _id );
            }
        }
    } // get_policy

    public UtcT getRequestEndTime()
    {
        Policy policy = get_client_policy(REQUEST_END_TIME_POLICY_TYPE.value);
        if (policy != null)
        {
            return ((org.omg.Messaging.RequestEndTimePolicy)policy).end_time();
        }
        return null;
    }

    public UtcT getReplyEndTime()
    {
        Policy policy = get_client_policy (REPLY_END_TIME_POLICY_TYPE.value);
        if (policy != null)
        {
            return ((org.omg.Messaging.ReplyEndTimePolicy)policy).end_time();
        }
        return null;
    }

    public UtcT getRequestStartTime()
    {
        Policy policy = get_client_policy (REQUEST_START_TIME_POLICY_TYPE.value);
        if (policy != null)
        {
            return ((org.omg.Messaging.RequestStartTimePolicy)policy).start_time();
        }
        return null;
    }

    public UtcT getReplyStartTime()
    {
        Policy policy = get_client_policy (REPLY_START_TIME_POLICY_TYPE.value);
        if (policy != null)
        {
            return ((org.omg.Messaging.ReplyStartTimePolicy)policy).start_time();
        }
        return null;
    }

    public long getRelativeRoundtripTimeout()
    {
        Policy policy = get_client_policy (RELATIVE_RT_TIMEOUT_POLICY_TYPE.value);
        if (policy != null)
        {
            return ((org.omg.Messaging.RelativeRoundtripTimeoutPolicy)policy)
                                                            .relative_expiry();
        }
        return -1;
    }

    public long getRelativeRequestTimeout()
    {
        Policy policy = get_client_policy (RELATIVE_REQ_TIMEOUT_POLICY_TYPE.value);
        if (policy != null)
        {
            return ((org.omg.Messaging.RelativeRequestTimeoutPolicy)policy)
                                                            .relative_expiry();
        }
        return -1;
    }

    public short getSyncScope()
    {
        Policy policy = get_client_policy (SYNC_SCOPE_POLICY_TYPE.value);
        if (policy != null)
        {
            return ((org.omg.Messaging.SyncScopePolicy)policy).synchronization();
        }
        return org.omg.Messaging.SYNC_WITH_TRANSPORT.value;
    }

    public org.omg.RTCORBA.Protocol[] getClientProtocols ()
    {
        Policy policy = get_client_policy(org.omg.RTCORBA.CLIENT_PROTOCOL_POLICY_TYPE.value);
        if (policy != null)
        {
            return ((org.omg.RTCORBA.ClientProtocolPolicy)policy).protocols ();
        }
        return null;
    }

    /**
     * Deprecated by CORBA 2.3
     */
    @Deprecated
    public org.omg.CORBA.InterfaceDef get_interface( org.omg.CORBA.Object self )
    {
        return org.omg.CORBA.InterfaceDefHelper.narrow( get_interface_def( self ) ) ;
    }

    public org.omg.CORBA.Object get_interface_def (org.omg.CORBA.Object self)
    {
        checkORB();

        // If local object call _interface directly

        if (is_really_local (self))
        {
            org.omg.PortableServer.Servant servant;
            org.omg.CORBA.portable.ServantObject so;

            so = servant_preinvoke (self, "_interface", java.lang.Object.class);

            // If preinvoke returns null POA spec, 11.3.4 states OBJ_ADAPTER
            // should be thrown.
            if (so == null )
            {
                throw new OBJ_ADAPTER ( "Servant from pre_invoke was null" );
            }
            try
            {
                servant = (org.omg.PortableServer.Servant) so.servant;
                orb.set_delegate (servant);
                return servant._get_interface_def();
            }
            finally
            {
                servant_postinvoke (self, so);
            }
        }

        org.omg.CORBA.portable.InputStream is = invokeBuiltin (self, "_interface", null);
        return (is == null) ? null : is.read_Object();
    }

    ClientConnection getConnection()
    {
        synchronized ( bind_sync )
        {
            bind();

            return connections[currentConnection.ordinal ()];
        }
    }

    public org.omg.IOP.IOR getIOR()
    {
        synchronized ( bind_sync )
        {
            if ( piorOriginal != null )
            {
                return piorOriginal.getIOR();
            }
            return getParsedIOR().getIOR();
        }
    }

    public byte[] getObjectId()
    {
        synchronized ( bind_sync )
        {
            bind();

            return POAUtil.extractOID( getParsedIOR().get_object_key() );
        }
    }

    public byte[] getObjectKey()
    {
        synchronized ( bind_sync )
        {
            bind();

            return getParsedIOR().get_object_key();
        }
    }

    public ParsedIOR getParsedIOR()
    {
        synchronized ( bind_sync )
        {
            // If the _pior has not been initialised due to the lazy
            // initialisation use the ior to create one.
            if (_pior == null)
            {
                if (ior == null)
                {
                    // should never happen due to the checks in configure
                    throw new INTERNAL ("Internal error - unable to initialise ParsedIOR as IOR is null");
                }
                else
                {
                    _pior = new ParsedIOR (orb, ior);
                    ior = null;
                }
            }

            return _pior;
        }
    }

    public void resolvePOA (org.omg.CORBA.Object self)
    {
        if (! resolved_locality)
        {
            resolved_locality = true;
            org.jacorb.poa.POA local_poa = orb.findPOA (this, self);

            if (local_poa != null)
            {
                poa = local_poa;
            }
        }
    }

    public org.jacorb.poa.POA getPOA()
    {
        return poa;
    }

    /**
     */

    public org.omg.CORBA.portable.ObjectImpl getReference( org.jacorb.poa.POA _poa )
    {
        if ( _poa != null )
        {
            poa = _poa;
        }

        final String typeId = ior == null ? typeId() : ior.type_id;

        org.omg.CORBA.portable.ObjectImpl reference =
            new org.jacorb.orb.Reference( typeId );

        reference._set_delegate( this );

        return reference;
    }

    public int hash( org.omg.CORBA.Object self, int x )
    {
        checkORB();

        return hashCode();
    }

    public int hashCode()
    {
        return getIDString().hashCode();
    }

    public int hashCode( org.omg.CORBA.Object self )
    {
        return hashCode();
    }

    /**
     * Invokes an asynchronous operation using this object reference by
     * sending the request marshalled in the OutputStream.  The reply
     * will be directed to the supplied ReplyHandler.
     */
    public void invoke( org.omg.CORBA.Object self,
                        org.omg.CORBA.portable.OutputStream os,
                        org.omg.Messaging.ReplyHandler replyHandler )
      throws ApplicationException, RemarshalException
    {
        // discard result, it is always null
        invoke_internal (self, os, replyHandler, true);
    }

    /**
     * Invokes a synchronous operation using this object reference
     * by sending the request marshalled in the OutputStream.
     * @return the reply, if a reply is expected for this request.
     * If no reply is expected, returns null.
     */
    public org.omg.CORBA.portable.InputStream invoke
                                       ( org.omg.CORBA.Object self,
                                         org.omg.CORBA.portable.OutputStream os )
      throws ApplicationException, RemarshalException
    {
        return invoke_internal (self, os, null, false);
    }

    private org.omg.CORBA.portable.InputStream invoke_internal
                                        ( org.omg.CORBA.Object self,
                                          org.omg.CORBA.portable.OutputStream os,
                                          org.omg.Messaging.ReplyHandler replyHandler,
                                          boolean async )
        throws ApplicationException, RemarshalException
    {
        try
        {
            final org.omg.CORBA.portable.InputStream in =
                _invoke_internal(self, os, replyHandler, async);

            if (clearCurrentContext)
            {
                clearInvocationContext();
            }

            return in;
        }
        catch(ApplicationException e)
        {
            if (clearCurrentContext)
            {
                clearInvocationContext();
            }

            throw e;
        }
        catch(SystemException t)
        {
            if (clearCurrentContext)
            {
                clearInvocationContext();
            }

            throw t;
        }
    }

    /**
     * Internal implementation of both invoke() methods.  Note that
     * the boolean argument <code>async</code> is necessary to differentiate
     * between synchronous and asynchronous calls, because the ReplyHandler
     * can be null even for an asynchronous call.
     */
    private org.omg.CORBA.portable.InputStream _invoke_internal
                               ( org.omg.CORBA.Object self,
                                 org.omg.CORBA.portable.OutputStream os,
                                 org.omg.Messaging.ReplyHandler replyHandler,
                                 boolean async )
        throws ApplicationException, RemarshalException
    {
        checkORB();

        RequestOutputStream ros      = (RequestOutputStream)os;

        Stack<Map<INVOCATION_KEY, UtcT>> invocationStack = invocationContext.get ();

        /**
         * We must just peek as we do not want to remove the context from
         * the Stack
         */
        Map<INVOCATION_KEY, UtcT> currentCtxt = invocationStack.peek();
        UtcT reqET = null;
        UtcT repET = null;

        if (currentCtxt != null)
        {
            reqET = (UtcT) currentCtxt.get (INVOCATION_KEY.REQUEST_END_TIME);
            repET = (UtcT) currentCtxt.get (INVOCATION_KEY.REPLY_END_TIME);

            checkTimeout (reqET, repET);
        }

        ReplyReceiver       receiver = null;
        final ClientInterceptorHandler interceptors;

        if (orb.hasClientRequestInterceptors())
        {
            interceptors = new DefaultClientInterceptorHandler
            (
                    (DefaultClientInterceptorHandler)localInterceptors.get(),
                    orb,
                    ros,
                    self,
                    this,
                    piorOriginal,
                    connections[currentConnection.ordinal ()]
            );
        }
        else
        {
            interceptors = NullClientInterceptorHandler.getInstance();
        }

        if ( connections[currentConnection.ordinal ()] != null )
        {
           orb.notifyTransportListeners (connections[currentConnection.ordinal ()].getGIOPConnection());
        }

        if (orb.hasRequestInterceptors())
        {
            localInterceptors.set(interceptors);

            try
            {
                interceptors.handle_send_request();
            }
            catch (ForwardRequest fwd)
            {
                // Should not happen for remote requests
            }
            catch (RemarshalException re)
            {
                // RemarshalExceptions explicitely caught, because in
                // that case, localInterceptors must stay set
                throw re;
            }
            catch (RuntimeException e)
            {
                // If we are throwing a system exception then this will disrupt the call path.
                // Therefore nullify localInterceptors so it doesn't appear we are still in an
                // interceptor call.
                localInterceptors.set(null);

                throw e;
            }
        }

        ClientConnection connectionToUse = null;

        ReplyGroup group = null;
        try
        {
            synchronized (bind_sync)
            {
               if ( ! bound )
               {
                  // Somehow the connection got closed under us
                  throw new COMM_FAILURE("Connection closed");
               }
               else if (ros.getConnection() == connections[currentConnection.ordinal ()])
               {
                  // RequestOutputStream has been created for
                  // exactly this connection
                  connectionToUse = connections[currentConnection.ordinal ()];
               }
               else
               {
                    logger.debug("invoke: RemarshalException");

                    // RequestOutputStream has been created for
                    // another connection, so try again
                    throw new RemarshalException();
                }
            }

            group = getReplyGroup (connectionToUse);
            if ( !ros.response_expected() )  // oneway op
            {
                invoke_oneway (ros, connectionToUse, interceptors, group);
            }
            else
            {
                // response expected, synchronous or asynchronous
                receiver = new ReplyReceiver(this, group,
                                             ros.operation(),
                                             ros.getReplyEndTime(),
                                            interceptors, replyHandler, selectorManager);

                try
                {
                   receiver.configure(configuration);
                }
                catch (ConfigurationException ex)
                {
                   logger.error ("Configuration problem with ReplyReceiver", ex);
                   throw new INTERNAL ("Caught configuration exception setting up ReplyReceiver.");
                }

                group.addHolder (receiver);

                // Use the local copy of the client connection to avoid trouble
                // with something else affecting the real connections[currentConnection].
                connectionToUse.sendRequest(ros, receiver, ros.requestId(), true);
            }
        }
        catch ( org.omg.CORBA.SystemException cfe )
        {
            logger.debug("invoke[-->]: SystemException", cfe);

            if( !async )
            {
                // Remove ReplyReceiver to break up reference cycle
                // Otherwise gc will not detect this Delegate and
                // will never finalize it.
                if (group != null)
                    group.removeHolder(receiver);
            }

            try
            {
                interceptors.handle_receive_exception ( cfe );
            }
            catch (ForwardRequest fwd)
            {
                // Should not happen for remote requests
            }

            // The exception is a TRANSIENT, so try rebinding.
            if ( cfe instanceof org.omg.CORBA.TRANSIENT && try_rebind() )
            {
                throw new RemarshalException();
            }

            if (!(cfe instanceof org.omg.CORBA.TIMEOUT)) {
              if (logger.isDebugEnabled()) {
                logger.debug (this.toString() + ":invoke_internal: closing connection due to " + cfe.getMessage());
              }
              disconnect(connectionToUse);
            }

            throw cfe;
        }
        finally
        {
            if (orb.hasRequestInterceptors())
            {
                localInterceptors.set(null);
            }
        }

        try
        {
            if ( !async && receiver != null )
            {
                // Synchronous invocation, response expected.
                // This call blocks until the reply arrives.
                org.omg.CORBA.portable.InputStream is = receiver.getReply();

                ((CDRInputStream)is).updateMutatorConnection (connectionToUse.getGIOPConnection());

                if (clearCurrentContext)
                {
                    clearInvocationContext();
                }

                return is;
            }
        }
        catch(SystemException e)
        {
            logger.debug("invoke[<--]: SystemException", e);

            // If the attempt to read the reply throws a system exception its
            // possible that the pending_replies will not get cleaned up.
            if (group != null)
                group.removeHolder(receiver);

            disconnect(connectionToUse);

            throw e;
        }

        return null;
    }

    /**
     * Internal method to check whether the timeouts have been exceeded.
     * @param reqET the request end time
     * @param repET the reply end time param
     */
    private void checkTimeout (UtcT reqET, UtcT repET)
    {
        if (reqET != null)
        {
            if (Time.hasPassed(reqET))
            {
                throw new TIMEOUT("Request End Time exceeded",
                                  2,
                                  CompletionStatus.COMPLETED_NO);
            }
        }

        if (repET != null)
        {
            if (Time.hasPassed(repET))
            {
                throw new TIMEOUT("Reply End Time exceeded",
                                  3,
                                  CompletionStatus.COMPLETED_NO);
            }
        }
    }

    private void disconnect(ClientConnection connectionInUse)
    {
        if (connectionInUse == null)
        {
            return;
        }

        if (disconnectAfterNonRecoverableSystemException)
        {
            return;
        }

        synchronized(bind_sync)
        {
            if (connections[currentConnection.ordinal ()] == null)
            {
                return;
            }

            if (connections[currentConnection.ordinal ()] != connectionInUse)
            {
                return;
            }

            logger.debug("release the connection");

            conn_mg.releaseConnection( connections[currentConnection.ordinal ()] );
            connections[currentConnection.ordinal ()] = null;
            bound = false;
        }
    }

    private void invoke_oneway (RequestOutputStream ros,
                                ClientConnection connectionToUse,
                                ClientInterceptorHandler interceptors,
                                ReplyGroup group)
        throws RemarshalException, ApplicationException
    {
        switch (ros.syncScope())
        {
            case SYNC_NONE.value:
                RequestOutputStream copy = new RequestOutputStream(ros);
                passToTransport (connectionToUse, copy);
                try
                {
                    interceptors.handle_receive_other (SUCCESSFUL.value);
                }
                catch (ForwardRequest fwd)
                {
                    // Should not happen for remote requests
                }
                break;

            case SYNC_WITH_TRANSPORT.value:
                connectionToUse.sendRequest (ros, false);
                try
                {
                    interceptors.handle_receive_other (SUCCESSFUL.value);
                }
                catch (ForwardRequest fwd)
                {
                    /// Should not happen for remote requests
                }
                break;

            case SYNC_WITH_SERVER.value:
            case SYNC_WITH_TARGET.value:

                ReplyReceiver rcv = new ReplyReceiver (this,
                                                       group,
                                                       ros.operation(),
                                                       ros.getReplyEndTime(),
                                                       interceptors,
                                                       null, selectorManager);
                try
                {
                   rcv.configure(configuration);
                }
                catch (ConfigurationException ex)
                {
                   logger.error ("Configuration problem with ReplyReceiver", ex);
                   throw new INTERNAL ("Caught configuration exception setting up ReplyReceiver.");
                }

                if (connections[TransportType.MIOP.ordinal ()] != null)
                {
                    connections[TransportType.MIOP.ordinal ()].sendRequest(ros,false);
                }
                else
                {
                    connectionToUse.sendRequest (ros, rcv, ros.requestId(), true);
                    // connections[TransportType.IIOP.ordinal ()].sendRequest (ros, rcv, ros.requestId(), true);
                }

                ReplyInputStream in = rcv.getReply();
                try
                {
                    interceptors.handle_receive_reply (in);
                }
                catch (ForwardRequest fwd)
                {
                    /// Should not happen for remote requests
                }
                break;

            default:
                throw new org.omg.CORBA.MARSHAL
                    ("Illegal SYNC_SCOPE: " + ros.syncScope(),
                     0, CompletionStatus.COMPLETED_MAYBE);
        }
    }

    private ReplyGroup getReplyGroup (ClientConnection connectionToUse)
    {
        // The ReplyGroup collects pending replies for a specific target.  This
        // allows separation between requests that may have been sent to an IMR
        // and those that were sent to the final target, as could be the case
        // with massively threaded clients.
        org.omg.ETF.Profile profile = connectionToUse.getRegisteredProfile();
        ReplyGroup group = groups.get (profile);
        if (group == null)
        {
            if (logger.isInfoEnabled())
            {
                logger.info ("Adding new retry group for " + profile);
            }
            ReplyGroup g = new ReplyGroup (this, profile);
            group = groups.putIfAbsent (profile, g);
            if (group == null)
            {
                group = g;
                group.postInit();
            }
        }
        return group;
    }

    private void passToTransport (final ClientConnection connectionToUse, final RequestOutputStream ros)
    {
        new Thread (new Runnable()
        {
            public void run()
            {
                try
                {
                    connectionToUse.sendRequest (ros, false);
                }
                catch (Throwable e)
                {
                    if (logger.isWarnEnabled())
                    {
                        logger.warn ("Caught CORBA SystemException ", e);
                    }
                }
            }
        },
        "PassToTransport").start();
    }

    private boolean try_rebind()
    {
        synchronized ( bind_sync )
        {
            if( logger.isDebugEnabled())
            {
                logger.debug("Delegate.try_rebind" );
            }

            if ( piorOriginal != null )
            {
                if( logger.isDebugEnabled())
                {
                    logger.debug("Delegate: falling back to original IOR");
                }

                //keep last failed ior to detect forwarding loops
                piorLastFailed = getParsedIOR();

                //rebind to the original ior
                rebind( piorOriginal );

                //clean up and start fresh
                piorOriginal = null;
                piorLastFailed = null; // supplied byte Kevin Heifner, OCI

                return true;
            }
            else if ( useIMR && ! isImR )
            {
                Integer orbTypeId = getParsedIOR().getORBTypeId();

                // only lookup ImR if IOR is generated by JacORB
                if ( orbTypeId == null ||
                     orbTypeId.intValue() != ORBConstants.JACORB_ORB_ID )
                {
                    if( logger.isDebugEnabled())
                    {
                        logger.debug("Delegate: foreign IOR detected" );
                    }
                    return false;
                }

                if( logger.isDebugEnabled())
                {
                    logger.debug("Delegate: JacORB IOR detected" );
                }

                byte[] object_key = getParsedIOR().get_object_key();

                // No backup IOR so it may be that the ImR is down
                // Attempt to resolve the ImR again to see if it has
                // come back up at a different address

                if( logger.isDebugEnabled())
                {
                    logger.debug("Delegate: attempting to contact ImR" );
                }

                ImRAccess imr = null;

                try
                {
                    imr = ImRAccessImpl.connect(orb);
                }
                catch ( Exception e )
                {
                    if( logger.isDebugEnabled())
                    {
                        logger.debug("Delegate: failed to contact ImR" );
                    }
                    return false;
                }

                //create a corbaloc URL to use to contact the server
                StringBuffer corbaloc = new StringBuffer( "corbaloc:iiop:" );

                corbaloc.append( imr.getImRHost() );
                corbaloc.append( ':' );
                corbaloc.append( imr.getImRPort() );
                corbaloc.append( '/' );
                corbaloc.append( CorbaLoc.parseKey( object_key ) );

                //rebind to the new IOR
                rebind( new ParsedIOR( orb, corbaloc.toString()));

                //clean up and start fresh
                piorOriginal = null;
                piorLastFailed = null; //***

                return true;
            }
            else
            {
                return false;
            }
        }
    }

    public void invokeInterceptors( ClientRequestInfoImpl info, short op )
        throws RemarshalException
    {
        ClientInterceptorIterator intercept_iter =
            orb.getInterceptorManager().getClientIterator();

        try
        {
            intercept_iter.iterate( info, op );
        }
        catch (ForwardRequest fwd )
        {
            rebind(fwd.forward);
            throw new RemarshalException();
        }
        catch ( org.omg.CORBA.UserException ue )
        {
            if (logger.isErrorEnabled())
            {
                logger.error( ue.getMessage() );
            }
        }
    }



   /**
    * <code>repository_id</code> returns a repository ID.
    *
    * @return a <code>String</code> value
    */
   public String repository_id (org.omg.CORBA.Object self)
   {
       return getParsedIOR().getTypeId();
   }


    /**
     * Determines whether the object denoted by self
     * has type logical_type_id or a subtype of it
     */

    public boolean is_a( org.omg.CORBA.Object self, String logical_type_id )
    {
        /* First, try to find out without a remote invocation. */

        /* check most derived type as defined in the IOR first
         * (this type might otherwise not be found if the helper
         * is consulted and the reference was not narrowed to
         * the most derived type. In this case, the ids returned by
         * the helper won't contain the most derived type
         */

        if (ior != null && ior.type_id.equals(logical_type_id))
        {
            return true;
        }

        ParsedIOR pior = getParsedIOR();

        if ( pior.getTypeId().equals( logical_type_id ) )
        {
            return true;
        }

        /*   The Ids in ObjectImpl will at least contain the type id
             found in the object reference itself.
        */
        String[] ids = ( ( org.omg.CORBA.portable.ObjectImpl ) self )._ids();

        /* the last id will be CORBA.Object, and we know that already... */
        for ( int i = 0; i < ids.length - 1; i++ )
        {
            if ( ids[ i ].equals( logical_type_id ) )
            {
                return true;
            }
        }

        /* ok, we could not affirm by simply looking at the locally available
           type ids, so ask the object itself */

        // If local object call _is_a directly
        if (is_really_local(self))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Located " + self + " on local POA; assuming local.");
            }

            org.omg.PortableServer.Servant servant;
            org.omg.CORBA.portable.ServantObject so;

            so = servant_preinvoke (self, "_is_a", java.lang.Object.class);

            // If preinvoke returns null POA spec, 11.3.4 states OBJ_ADAPTER
            // should be thrown.
            if (so == null )
            {
                throw new OBJ_ADAPTER( "Servant from pre_invoke was null" );
            }

            try
            {
                servant = (org.omg.PortableServer.Servant)so.servant;
                orb.set_delegate(servant);
                return servant._is_a(logical_type_id);
            }
            finally
            {
                servant_postinvoke(self, so);
            }
        }
        // The check below avoids trying to load a stub for CORBA.Object.
        // (It would be faster to check that ids.length > 1, but Sun's
        // CosNaming JNDI provider calls _is_a() on some weird ObjectImpl
        // instances whose _ids() method returns an array of length two,
        // containing two Strings equal to "IDL:omg.org/CORBA/Object:1.0".)
        if (avoidIsARemoteCall &&
            !ids[0].equals("IDL:omg.org/CORBA/Object:1.0"))
        {
            // Try to avoid remote call - is it a derived type?
            try
            {
                // Retrieve the local stub for the object in question. Then call the _ids method
                // and see if any match the logical_type_id otherwise fall back to remote.

                final String classname = RepositoryID.className( ids[0], "Stub", null );

                int lastDot = classname.lastIndexOf( '.' );
                StringBuffer buffer = new StringBuffer( classname.substring( 0, lastDot + 1) );
                buffer.append( '_' );
                buffer.append( classname.substring( lastDot + 1 ) );

                // This will only work if there is a correspondence between the Java class
                // name and the Repository ID. If prefixes have been using then this mapping
                // may have been lost.

                // First, search with stub name
                // if not found, try with the 'org.omg.stub' prefix to support package
                // with javax prefix
                Class stub=null;
                try
                {
                    stub = ObjectUtil.classForName( buffer.toString());
                }
                catch (ClassNotFoundException e)
                {
                    stub = ObjectUtil.classForName("org.omg.stub."+buffer.toString());
                }

                Method idm = stub.getMethod ( "_ids", (Class[]) null );
                String newids[] = (String[] )idm.invoke( stub.newInstance(), (java.lang.Object[]) null );

                for ( int i = 0; i < newids.length ; i++ )
                {
                    if (newids[i].equals( logical_type_id ) )
                    {
                        return true;
                    }
                }
            }
            // If it fails fall back to a remote call.
            catch (ClassNotFoundException e) // NOPMD
            {
                // ignore
            }
            catch (IllegalArgumentException e) // NOPMD
            {
                // ignore
            }
            catch (SecurityException e) // NOPMD
            {
                // ignore
            }
            catch (NoSuchMethodException e) // NOPMD
            {
                // ignore
            }
            catch (IllegalAccessException e) // NOPMD
            {
                // ignore
            }
            catch (InvocationTargetException e) // NOPMD
            {
                // ignore
            }
            catch (InstantiationException e) // NOPMD
            {
                // ignore
            }
            catch (SystemException e) // NOPMD
            {
                // ignore
            }
            logger.debug("trying is_a remotely");
        }

        org.omg.CORBA.portable.InputStream is = invokeBuiltin (self, "_is_a", logical_type_id);
        return (is == null) ? false : is.read_boolean();
    }

    public boolean is_equivalent(org.omg.CORBA.Object self,
                                 org.omg.CORBA.Object obj)
    {
        checkORB();

        boolean result = true;

        if (self != obj)
        {
            ParsedIOR pior1 = new ParsedIOR( orb, obj.toString() );
            ParsedIOR pior2 = new ParsedIOR( orb, self.toString() );
            result = pior2.getIDString().equals( pior1.getIDString() );
        }

        return result;
    }

    public String getIDString()
    {
        return getParsedIOR().getIDString();
    }

    /**
     * @return true if this object lives on a local POA and
     * interceptors are not installed. When interceptors are
     * installed this returns false so that stubs do not call
     * direct to implementation, avoiding installed interceptors.
     */

    public boolean is_local(org.omg.CORBA.Object self)
    {
        if (ignoreNextCallToIsLocal.get() == Boolean.TRUE)
        {
            ignoreNextCallToIsLocal.set(Boolean.FALSE);
            return false;
        }

        if (isLocalHistoricalInterceptors && localInterceptors.get() == null && orb.hasRequestInterceptors())
        {
            return false;
        }

        return is_really_local(self);
    }

    /**
     * @return true if this object lives on a local POA
     */

    private boolean is_really_local(org.omg.CORBA.Object self)
    {
        if (poa == null)
        {
            resolvePOA(self);
        }

        return poa != null;
    }

    public boolean is_nil()
    {
        ParsedIOR pior = getParsedIOR();

        return ( pior.getIOR().type_id.equals( "" ) &&
                 pior.getIOR().profiles.length == 0 );
    }

    public boolean non_existent (org.omg.CORBA.Object self)
    {
        // If local object call _non_existent directly

        if (is_really_local(self))
        {
            org.omg.PortableServer.Servant servant;
            org.omg.CORBA.portable.ServantObject so;

            so = servant_preinvoke(self, "_non_existent", java.lang.Object.class);

            try
            {
                servant = (org.omg.PortableServer.Servant)so.servant;
                orb.set_delegate(servant);
                return servant._non_existent();
            }
            finally
            {
                servant_postinvoke(self, so);
            }
        }

        org.omg.CORBA.portable.InputStream is = invokeBuiltin (self, "_non_existent", null);
        return (is == null) ? false : is.read_boolean();
    }


    public org.omg.CORBA.Object get_component (org.omg.CORBA.Object self)
    {
        // If local object call _get_component directly

        if (is_really_local(self))
        {
            org.omg.PortableServer.Servant servant;
            org.omg.CORBA.portable.ServantObject so;

            so = servant_preinvoke(self, "_get_component", java.lang.Object.class);

            try
            {
                servant = (org.omg.PortableServer.Servant)so.servant;
                orb.set_delegate(servant);
                return servant._get_component();
            }
            finally
            {
                servant_postinvoke(self, so);
            }
        }

        org.omg.CORBA.portable.InputStream is = invokeBuiltin (self, "_get_component", null);
        return (is == null) ? null : is.read_Object();
    }

    private org.omg.CORBA.portable.InputStream invokeBuiltin (org.omg.CORBA.Object self, String op, String arg)
    {
        org.omg.CORBA.portable.OutputStream os;
        // The old behavior was to loop forever, which will happen here if maxBuiltinRetries is 0.
        for (int retries = 0; maxBuiltinRetries == 0 || retries < maxBuiltinRetries; retries ++)
        {
            try
            {
                os = request(self, op, true);
                if (arg != null)
                    os.write_string(arg);
                return invoke(self, os);
            }
            catch (RemarshalException re) // NOPMD
            {
                // Ignored
            }
            catch (ApplicationException ax)
            {
                throw new INTERNAL("Unexpected exception " + ax.getId());
            }
        }
        return null;
    }


    public org.omg.CORBA.ORB orb( org.omg.CORBA.Object self )
    {
        return orb;
    }

    /**
     * Called to indicate that this Delegate will no longer be used by
     * the client.  The Delegate unregisters itself from the underlying
     * GIOPConnection.  If there are no other Delegates using that
     * connection, it will be closed and disposed of altogether.
     */
    public void release( org.omg.CORBA.Object self )
    {
        synchronized ( bind_sync )
        {
            if (!bound)
            {
                return;
            }


            if ( connections[currentConnection.ordinal ()] != null )
            {
                conn_mg.releaseConnection( connections[currentConnection.ordinal ()] );
                connections[currentConnection.ordinal ()] = null;
            }
            bound = false;

            // Call using string rather than this to prevent data race
            // warning.
            orb._release( getParsedIOR().getIORString() );

            if ( logger.isDebugEnabled() )
            {
                logger.debug("Delegate released!");
            }
        }
    }

    /**
     * releases the InputStream
     */
    public void releaseReply( org.omg.CORBA.Object self,
                              org.omg.CORBA.portable.InputStream is )
    {
        if ( is != null )
        {
            try
            {
                is.close();
            }
            catch ( java.io.IOException io )
            {
                // ignored
            }
        }
        Time.waitFor (getReplyStartTime());
    }

    public org.omg.CORBA.Request request( org.omg.CORBA.Object self,
                                                       String operation )
    {
        orb.perform_work();

        synchronized ( bind_sync )
        {
            bind();

            return new org.jacorb.orb.dii.Request( self,
                                                   orb,
                                                   connections[currentConnection.ordinal ()],
                                                   getParsedIOR().get_object_key(),
                                                   operation );
        }
    }

    /**
     */

    public org.omg.CORBA.portable.OutputStream request(org.omg.CORBA.Object self,
                                                   String operation,
                                                   boolean responseExpected )
    {
        orb.perform_work();

        Stack<Map<INVOCATION_KEY, UtcT>> invocationStack = invocationContext.get ();
        Map<INVOCATION_KEY, UtcT> currentCtxt = null;

        if (! invocationStack.empty())
        {
            currentCtxt = invocationStack.peek();

            /**
             * If the context was created as an interceptor call was
             * being made in servant_preinvoke then don't clear it as part of this
             * request. It will be cleared on return from the
             * interceptor call. This caters for situations where embedded requests are made
             */
            if (currentCtxt.containsKey (INVOCATION_KEY.INTERCEPTOR_CALL) ||
                currentCtxt.containsKey (INVOCATION_KEY.SERVANT_PREINVOKE))
            {
                clearCurrentContext = false;
            }
        }

        if (currentCtxt == null)
        {
            currentCtxt = new HashMap<INVOCATION_KEY, UtcT>();

            invocationStack.push (currentCtxt);
        }

        UtcT requestEndTime = currentCtxt.get (INVOCATION_KEY.REQUEST_END_TIME);
        UtcT replyEndTime = currentCtxt.get (INVOCATION_KEY.REPLY_END_TIME);

        if (!disableClientOrbPolicies)
        {
            // Compute the deadlines for this request based on any absolute or
            // relative timing policies that have been specified.  Compute this
            // now, because it is the earliest possible time, and therefore any
            // relative timeouts will cover the entire invocation.

            if (requestEndTime == null)
            {
                requestEndTime = getRequestEndTime();
                final long requestTimeout = getRelativeRequestTimeout();

                if ((requestTimeout > 0) || (requestEndTime != null))
                {
                    requestEndTime = Time.earliest(Time.corbaFuture (requestTimeout), requestEndTime);

                    if (Time.hasPassed(requestEndTime))
                    {
                        if (clearCurrentContext)
                        {
                           clearInvocationContext();
                        }

                        throw new TIMEOUT("Request End Time exceeded prior to invocation",
                                          2,
                                          CompletionStatus.COMPLETED_NO);
                    }
                }

                currentCtxt.put (INVOCATION_KEY.REQUEST_END_TIME, requestEndTime);
            }
            else
            {
                if (Time.hasPassed (requestEndTime))
                {
                    if (clearCurrentContext)
                    {
                       clearInvocationContext();
                    }

                    throw new TIMEOUT("Request End Time exceeded",
                                      2,
                                      CompletionStatus.COMPLETED_NO);
                }
            }

            if (replyEndTime == null)
            {
                replyEndTime = getReplyEndTime();

                final long roundtripTimeout = getRelativeRoundtripTimeout();

                if ((roundtripTimeout > 0) || (replyEndTime != null))
                {
                    replyEndTime = Time.earliest(Time.corbaFuture (roundtripTimeout), replyEndTime);

                    if (Time.hasPassed(replyEndTime))
                    {
                        if (clearCurrentContext)
                        {
                           clearInvocationContext();
                        }

                        throw new TIMEOUT("Reply End Time exceeded prior to invocation",
                                          3,
                                          CompletionStatus.COMPLETED_NO);
                    }
                }

                currentCtxt.put (INVOCATION_KEY.REPLY_END_TIME, replyEndTime);
            }
            else
            {
                if (Time.hasPassed(replyEndTime))
                {
                    if (clearCurrentContext)
                    {
                        clearInvocationContext();
                    }

                    throw new TIMEOUT("Reply End Time exceeded",
                                      3,
                                      CompletionStatus.COMPLETED_NO);
                }
            }
        }

        synchronized ( bind_sync )
        {
            bind();

            ParsedIOR ior = getParsedIOR();

            //MIOP
            //the object key must be the representant objectKey if the reply is
            //expected and the request is a group request
            org.omg.ETF.Profile profile = ior.getEffectiveProfile();
            byte[] objectKey = profile.get_object_key();

            // Default to the version within the EffectiveProfile
            int giopMinor = profile.version().minor;

            if(profile instanceof MIOPProfile)
            {
                if(responseExpected)
                {
                    IIOPProfile ip = ((MIOPProfile)profile).getGroupIIOPProfile();
                    if (ip == null)
                    {
                        throw new INV_OBJREF ("No Group IIOP Profile so unable to send a two-way request.");
                    }
                    objectKey = ip.get_object_key();
                    currentConnection = TransportType.IIOP;
                }
                else
                {
                    currentConnection = TransportType.MIOP;
                }
                // If we are using MIOP then it encodes its own version. So at this point we set
                // the version for GIOP to be the one configured.
                giopMinor = defaultGiopMinor;
            }
            else
            {
                currentConnection = TransportType.IIOP;
            }

            RequestOutputStream out =
                new RequestOutputStream( orb,
                                         connections[currentConnection.ordinal ()],
                                         connections[currentConnection.ordinal ()].getId(),
                                         operation,
                                         responseExpected,
                                         getSyncScope(),
                                         getRequestStartTime(),
                                         requestEndTime,
                                         replyEndTime,
                                         objectKey,
                                         giopMinor);

            // CodeSets are only negotiated once per connection,
            // not for each individual request
            // (CORBA 3.0, 13.10.2.6, second paragraph).
            if (!connections[currentConnection.ordinal ()].isTCSNegotiated())
            {
                connections[currentConnection.ordinal ()].setCodeSet(ior);
            }

            //Setting the codesets not until here results in the
            //header being written using the default codesets. On the
            //other hand, the server side must have already read the
            //header to discover the codeset service context.
            out.setCodeSets( connections[currentConnection.ordinal ()].getTCS(), connections[currentConnection.ordinal ()].getTCSW() );

            out.updateMutatorConnection (connections[currentConnection.ordinal ()].getGIOPConnection());

            return out;
        }
    }

    /**
     * Overrides servant_postinvoke() in org.omg.CORBA.portable.Delegate<BR>
     * called from generated stubs after a local operation
     */


    public void servant_postinvoke( org.omg.CORBA.Object self, ServantObject servant )
    {
        try
        {
            if (orb.hasRequestInterceptors())
            {
                ServerRequestInfoImpl sinfo = ( (ServantObjectImpl) servant).getServerRequestInfo();

                DefaultClientInterceptorHandler interceptors =
                    ( (ServantObjectImpl) servant).getClientInterceptorHandler();

                if (sinfo != null && interceptors != null)
                {
                    Collection<ServiceContext> ctx = sinfo.getReplyServiceContexts();
                    interceptors.getInfo ().setReplyServiceContexts (ctx.toArray (new ServiceContext[ctx.size ()]));

                    try
                    {
                        if (sinfo.reply_status() == SUCCESSFUL.value)
                        {
                            interceptors.handle_receive_reply (null);
                        }
                        else if (sinfo.reply_status() == SYSTEM_EXCEPTION.value)
                        {
                            interceptors.handle_receive_exception (
                                SystemExceptionHelper.read (sinfo.sending_exception().create_input_stream()));
                        }
                        else if (sinfo.reply_status() == LOCATION_FORWARD.value)
                        {
                            /**
                             * If the ForwardRequest was thrown at the send_exception interception
                             * point we will not be able to get the forward_reference from the
                             * server info and a BAD_INV_ORDER will be thrown.  In that case handle
                             * as a simple receive_other.
                             */
                            try
                            {
                                interceptors.handle_location_forward (null, sinfo.forward_reference());
                            }
                            catch (BAD_INV_ORDER bio)
                            {
                                interceptors.handle_receive_other(sinfo.reply_status());
                            }
                        }
                        else if (sinfo.reply_status() == USER_EXCEPTION.value)
                        {
                            interceptors.handle_receive_other (sinfo.reply_status());
                        }
                    }
                    catch (ForwardRequest fwd)
                    {
                        throw new RuntimeException (fwd);
                    }
                    catch (RemarshalException re)
                    {
                        // Should not happen for a local invocation
                    }
                }
            }

            if (poa != null)
            {
                if ( poa.isUseServantManager() &&
                        ! poa.isRetain() &&
                        cookie != null &&
                        invokedOperation != null )
                {
                    // ServantManager is a ServantLocator:
                    // call postinvoke
                    try
                    {
                        byte [] oid =
                            POAUtil.extractOID( getParsedIOR().get_object_key() );
                        org.omg.PortableServer.ServantLocator sl =
                            ( org.omg.PortableServer.ServantLocator ) poa.get_servant_manager();

                        sl.postinvoke( oid, poa, invokedOperation, cookie.value, (Servant)servant.servant );

                        // delete stored values
                        cookie = null;
                        invokedOperation = null;
                    }
                    catch ( Throwable e )
                    {
                        if (logger.isWarnEnabled())
                        {
                            logger.warn( e.getMessage() );
                        }
                    }
                }
            }
        }
        finally
        {
            if (poa != null)
            {
                poa.removeLocalRequest();
            }

            orb.getPOACurrent()._removeContext( Thread.currentThread() );

             if (orb.getInterceptorManager() != null)
             {
                 orb.getInterceptorManager().removeLocalPICurrent ();
             }
        }
    }

    /**
     * Overrides servant_preinvoke() in org.omg.CORBA.portable.Delegate<BR>
     * called from generated stubs before a local operation
     */

    public ServantObject servant_preinvoke( org.omg.CORBA.Object self,
                                            String operation,
                                            Class expectedType )
    {
        InterceptorManager manager = null;
        ServerInterceptorIterator interceptorIterator = null;
        ServerRequestInfoImpl sinfo = null;
        Collection<ServiceContext> contexts = null;
        DefaultClientInterceptorHandler interceptors = null;

        if (poa == null)
        {
            resolvePOA(self);
        }

        if (poa == null)
        {
            if (logger.isWarnEnabled())
            {
                logger.warn("No POA! servant_preinvoke returns null");
            }

            return null;
        }

        Map<INVOCATION_KEY, UtcT> currentContext = new HashMap<INVOCATION_KEY, UtcT>();
        currentContext.put (INVOCATION_KEY.SERVANT_PREINVOKE, null);

        invocationContext.get().push (currentContext);

        // remember that a local request is outstanding. On
        //  any exit through an exception, this must be cleared again,
        // otherwise the POA will hangon destruction (bug #400).
        poa.addLocalRequest();

        final ServantObject servantObject = (ServantObject) new ServantObjectImpl();

        ( (ServantObjectImpl) servantObject).setORB (orb);

        try
        {
            if (orb.hasClientRequestInterceptors())
            {
                interceptors = new DefaultClientInterceptorHandler(orb,
                                                                   operation,
                                                                   true,
                                                                   SYNC_WITH_TARGET.value,
                                                                   self,
                                                                   this,
                                                                   piorOriginal);
            }

            if (orb.hasRequestInterceptors() && interceptors != null)
            {
                try
                {
                    interceptors.handle_send_request();
                }
                catch (ForwardRequest fwd)
                {
                    ( (ObjectImpl) self)._set_delegate ( ( (ObjectImpl) fwd.forward)._get_delegate());
                    return null;
                }
                catch (RemarshalException re)
                {
                    // Should not happen for a local server invocation.
                }
            }

            if (interceptors != null)
            {
                contexts = interceptors.getInfo().getRequestServiceContexts();
            }

            if (orb.hasServerRequestInterceptors())
            {
                sinfo = new ServerRequestInfoImpl
                (
                    orb,
                    contexts,
                    (org.omg.PortableServer.Servant) servantObject.servant,
                    getObjectId(),
                    operation,
                    true,
                    SYNC_WITH_TARGET.value
                );

                manager = orb.getInterceptorManager();
                sinfo.setCurrent (manager.getEmptyCurrent());
                interceptorIterator = manager.getServerIterator();

                ( (org.jacorb.orb.ServantObjectImpl ) servantObject).setServerRequestInfo (sinfo);

                // Note: this code is very similar to the below hasServerRequestInterceptors try/catch
                try
                {
                   manager.setLocalPICurrent (sinfo.current ());

                   interceptorIterator.iterate
                   (
                       sinfo,
                       ServerInterceptorIterator.RECEIVE_REQUEST_SERVICE_CONTEXTS
                   );
                }
                catch (ForwardRequest fwd)
                {
                    if (interceptors != null)
                    {
                        interceptors.handle_location_forward (null, fwd.forward);
                    }

                    ( (ObjectImpl) self)._set_delegate ( ( (ObjectImpl) fwd.forward)._get_delegate());
                    return null;
                }
                catch (Exception ex)
                {
                    if (interceptors != null && orb.hasRequestInterceptors())
                    {
                        try
                        {
                            if (ex instanceof SystemException)
                            {
                                interceptors.handle_receive_exception ( (SystemException) ex);
                            }
                            else if (ex instanceof ApplicationException)
                            {
                                interceptors.handle_receive_exception ( (ApplicationException) ex, null);
                            }
                        }
                        catch (ForwardRequest fwd)
                        {
                            ( (ObjectImpl) self)._set_delegate ( ( (ObjectImpl) fwd.forward)._get_delegate());
                            return null;
                        }
                    }

                    throw ex;
                }
            }


            try
            {
                if ( ( poa.isRetain() && !poa.isUseServantManager() ) ||
                     poa.useDefaultServant() )
                {
                    // no ServantManagers, but AOM use
                    try
                    {
                       servantObject.servant = poa.reference_to_servant( self );
                    }
                    catch( WrongAdapter e )
                    {
                        //  exit on an error condition, but need to clean up first (added to fix bug #400)
                        poa.removeLocalRequest();
                        throw new OBJ_ADAPTER( "WrongAdapter caught when converting servant to reference. " + e );
                    }
                    catch( WrongPolicy e )
                    {
                        //  exit on an error condition, but need to clean up first (added to fix bug #400)
                        poa.removeLocalRequest();
                        throw new OBJ_ADAPTER("WrongPolicy caught" + e );
                    }
                    catch( ObjectNotActive e )
                    {
                        //  exit on an error condition, but need to clean up first (added to fix bug #400)
                        poa.removeLocalRequest();
                        throw new org.omg.CORBA.OBJECT_NOT_EXIST();
                    }
                }
                else if ( poa.isUseServantManager() )
                {
                    byte [] oid =
                    POAUtil.extractOID( getParsedIOR().get_object_key() );
                    org.omg.PortableServer.ServantManager sm =
                    poa.get_servant_manager();

                    if ( poa.isRetain() )
                    {
                        // ServantManager is a ServantActivator. Use the AOM to
                        // incarnate this or return the servant. It will correctly
                        // synchrnoize the requests.
                        servantObject.servant = poa._incarnateServant(oid, (ServantActivator)sm);
                    }
                    else
                    {
                        // ServantManager is a ServantLocator:
                        // locate a servant

                        org.omg.PortableServer.ServantLocator sl =
                            ( org.omg.PortableServer.ServantLocator ) sm;

                        // store this for postinvoke

                        cookie = new org.omg.PortableServer.ServantLocatorPackage.CookieHolder();

                        invokedOperation = operation;

                        boolean ok = false;

                        try
                        {
                            servantObject.servant = sl.preinvoke( oid, poa, operation, cookie );
                            ok = true;
                        }
                        finally
                        {
                            if (!ok)
                            {
                                // error condition: need to clean up before
                                // propagating the exception (added to fix
                                // bug #400)
                                poa.removeLocalRequest();
                            }
                        }
                    }
                    ((Servant)servantObject.servant)._this_object ((org.omg.CORBA.ORB)orb);
                }
                else
                {
                    throw new INTERNAL("Internal error: we should not have got to this piece of code!");
                }
            }
            catch( WrongPolicy e )
            {
                //  exit on an error condition, but need to clean up first (added to fix bug #400)
                poa.removeLocalRequest();
                throw new OBJ_ADAPTER( "WrongPolicy caught" + e );
            }
            catch( org.omg.PortableServer.ForwardRequest e )
            {
                if( logger.isDebugEnabled() )
                {
                    logger.debug("Caught forwardrequest to " + e.forward_reference + " from " + self );
                }
                ( (ObjectImpl) self)._set_delegate ( ( (ObjectImpl) e.forward_reference)._get_delegate());

                return null;
            }

            if ( !expectedType.isInstance( servantObject.servant ) )
            {
                if( logger.isWarnEnabled() )
                {
                    logger.warn("Expected " + expectedType +
                                " got " + servantObject.servant.getClass() );
                }

                ignoreNextCallToIsLocal.set(Boolean.TRUE);

                poa.removeLocalRequest();
                return null;
            }


            orb.getPOACurrent()._addContext
            (
                Thread.currentThread(),
                new org.jacorb.poa.LocalInvocationContext
                (
                    orb,
                    poa,
                    getObjectId(),
                    ( org.omg.PortableServer.Servant ) servantObject.servant
                )
            );

            ( (org.jacorb.orb.ServantObjectImpl )servantObject).setClientInterceptorHandler (interceptors);

            if (orb.hasServerRequestInterceptors())
            {
                sinfo =  ( (org.jacorb.orb.ServantObjectImpl ) servantObject).getServerRequestInfo();
                sinfo.setServant((org.omg.PortableServer.Servant) servantObject.servant);

                interceptorIterator = manager.getServerIterator();

                ( (org.jacorb.orb.ServantObjectImpl ) servantObject).setServerRequestInfo (sinfo);

                // Note: this code is very similar to the above hasServerRequestInterceptors try/catch
                try
                {
                   manager.setLocalPICurrent (sinfo.current ());

                   interceptorIterator.iterate
                   (
                       sinfo,
                       ServerInterceptorIterator.RECEIVE_REQUEST
                   );
                }
                catch (ForwardRequest fwd)
                {
                    if (interceptors != null)
                    {
                        interceptors.handle_location_forward (null, fwd.forward);
                    }

                    ( (ObjectImpl) self)._set_delegate ( ( (ObjectImpl) fwd.forward)._get_delegate());
                    return null;
                }
                catch (Exception ex)
                {
                    if (interceptors != null && orb.hasRequestInterceptors())
                    {
                        try
                        {
                            if (ex instanceof SystemException)
                            {
                                interceptors.handle_receive_exception ( (SystemException) ex);
                            }
                            else if (ex instanceof ApplicationException)
                            {
                                interceptors.handle_receive_exception ( (ApplicationException) ex, null);
                            }
                        }
                        catch (ForwardRequest fwd)
                        {
                            ( (ObjectImpl) self)._set_delegate ( ( (ObjectImpl) fwd.forward)._get_delegate());
                            return null;
                        }
                    }

                    throw ex;
                }

                /**
                 * If this is an internal ORB call from is_a or non_existent then we need
                 * to complete the interception point SEND_REPLY.  In other situations this will
                 * happen in the stub via the normalCompletion call but this will not be
                 * the case for internal ORB Calls
                 */
                if (operation.equals ("_is_a") || operation.equals ("_non_existent") ||
                    operation.equals("_interface") || operation.equals ("_get_component"))
                {
                    interceptorIterator.iterate
                    (
                        sinfo,
                        ServerInterceptorIterator.SEND_REPLY
                    );
                }
            }

            return servantObject;
        }
        catch (Exception e)
        {
            poa.removeLocalRequest();

            logger.error("unexpected exception during servant_preinvoke", e);

            orb.getPOACurrent()._removeContext( Thread.currentThread() );

            if (orb.getInterceptorManager() != null)
            {
               orb.getInterceptorManager().removeLocalPICurrent ();
            }

            if (e instanceof OBJECT_NOT_EXIST)
            {
                throw (OBJECT_NOT_EXIST)e;
            }

            if (e instanceof ObjectNotActive)
            {
                throw new OBJECT_NOT_EXIST();
            }

            if (e instanceof RuntimeException)
            {
                throw (RuntimeException) e;
            }

            throw new OBJ_ADAPTER("unexpected exception: " + e);
        }
        finally
        {
            clearInvocationContext();
        }
    }


    public String toString()
    {
        synchronized ( bind_sync )
        {
            if ( piorOriginal != null )
            {
                return piorOriginal.getIORString();
            }
            return getParsedIOR().getIORString();
        }
    }

    public String toString( org.omg.CORBA.Object self )
    {
        return toString();
    }

    public String typeId()
    {
        return getParsedIOR().getIOR().type_id;
    }

    public org.omg.CORBA.Object set_policy_override( org.omg.CORBA.Object self,
                                                     org.omg.CORBA.Policy[] policies,
                                                     org.omg.CORBA.SetOverrideType set_add )
    {
        return set_policy_overrides (self, policies, set_add);
    }

    public org.omg.CORBA.Object set_policy_overrides( org.omg.CORBA.Object self,
                                                     org.omg.CORBA.Policy[] policies,
                                                     org.omg.CORBA.SetOverrideType set_add )
    {
        if (disableClientOrbPolicies)
        {
            throw new BAD_PARAM("policy override is disabled per configuration");
        }

        // According to CORBA 3, 4.3.9.1 this should return a new Object with
        // the policies applied to that.
        org.omg.CORBA.Object result = duplicate(self);
        Delegate delResult = (Delegate)((ObjectImpl)result)._get_delegate();

        synchronized(policy_overrides)
        {
            if ( set_add == org.omg.CORBA.SetOverrideType.ADD_OVERRIDE)
            {
                // Need to add the overrides within this object to the new one.
                delResult.policy_overrides.putAll (policy_overrides);
            }

            for ( int i = 0; i < policies.length; i++ )
            {
                delResult.policy_overrides.put(Integer.valueOf(policies[ i ].policy_type()), policies[ i ] );
            }
        }

        return result;
    }

    public String get_codebase( org.omg.CORBA.Object self )
    {
        return getParsedIOR().getCodebaseComponent();
    }

    /**
     * Call work_pending as that does a simple boolean check to establish
     * if the ORB has been shutdown - otherwise it throws BAD_INV_ORDER.
     */
    private void checkORB()
    {
        orb.work_pending();
    }

}
