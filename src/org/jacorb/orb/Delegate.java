package org.jacorb.orb;

/*
 *        JacORB - a free Java ORB
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

import java.lang.reflect.Method;
import java.util.*;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.jacorb.ir.RepositoryID;
import org.jacorb.imr.ImRAccessImpl;
import org.jacorb.orb.giop.*;
import org.jacorb.orb.iiop.*;
import org.jacorb.orb.portableInterceptor.*;
import org.jacorb.orb.util.*;
import org.jacorb.poa.util.POAUtil;
import org.jacorb.util.Time;
import org.jacorb.util.ObjectUtil;
import org.jacorb.config.Configuration;

import org.omg.CORBA.*;
import org.omg.CORBA.Object;
import org.omg.CORBA.portable.*;
import org.omg.GIOP.LocateStatusType_1_2;
import org.omg.IOP.ServiceContext;
import org.omg.Messaging.*;
import org.omg.PortableInterceptor.SUCCESSFUL;
import org.omg.PortableServer.POAPackage.*;
import org.omg.TimeBase.UtcT;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;
import org.omg.PortableServer.Servant;

/**
 * JacORB implementation of CORBA object reference
 *
 * @author Gerald Brose
 * @version $Id$
 *
 */

public final class Delegate
    extends org.omg.CORBA_2_3.portable.Delegate
    implements Configurable
{
    // WARNING: DO NOT USE _pior DIRECTLY, BECAUSE THAT IS NOT MT
    // SAFE. USE getParsedIOR() INSTEAD, AND KEEP A METHOD-LOCAL COPY
    // OF THE REFERENCE.
    private ParsedIOR _pior = null;
    private org.omg.IOP.IOR ior = null;
    private ClientConnection connection = null;
    private String objectReference = null;

    /* save original ior for fallback */
    private ParsedIOR piorOriginal = null;

    /* save iors to detect and prevent locate forward loop */
    private ParsedIOR piorLastFailed = null;

    /* flag to indicate if this is the delegate for the ImR */
    private boolean isImR = false;

    private boolean bound = false;
    private org.jacorb.poa.POA poa;

    private org.jacorb.orb.ORB orb = null;
    private Logger logger = null;

    /** set after the first attempt to determine whether
        this reference is to a local object */
    private boolean resolved_locality = false;

    private Set     pending_replies      = new HashSet();
    private Barrier pending_replies_sync = new Barrier();

    private java.lang.Object bind_sync = new java.lang.Object();

    private boolean locate_on_bind_performed = false;

    private ClientConnectionManager conn_mg = null;

    private Map policy_overrides;

    private boolean doNotCheckExceptions = false; //Setting for Appligator

    private CookieHolder cookie = null;

    private String invokedOperation = null;

    /** the configuration object for this delegate */
    private org.apache.avalon.framework.configuration.Configuration configuration = null;

    /** configuration properties */
    private boolean useIMR;
    private boolean locateOnBind;

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
     * have a look at the comment for opration bind().
     */


    /* constructors: */

    private Delegate()
    {
    }

    public Delegate ( org.jacorb.orb.ORB orb, ParsedIOR pior )
    {
        this.orb = orb;
        _pior = pior;

        checkIfImR( _pior.getTypeId() );
        conn_mg = orb.getClientConnectionManager();
    }

    public Delegate( org.jacorb.orb.ORB orb, String object_reference )
    {
        if ( object_reference.indexOf( "IOR:" ) != 0 )
        {
            throw new org.omg.CORBA.INV_OBJREF( "Not an IOR: " +
                                                object_reference );
        }

        this.orb = orb;
        this.objectReference = object_reference;
        conn_mg = orb.getClientConnectionManager();
    }

    public Delegate(org.jacorb.orb.ORB orb, org.omg.IOP.IOR ior)
    {
        this.orb = orb;
        this.ior = ior;
        conn_mg = orb.getClientConnectionManager();
    }

    /**
     * special constructor for appligator
     */
    public Delegate( org.jacorb.orb.ORB orb,
                     String object_reference,
                     boolean _donotcheckexceptions )
    {
        this( orb, object_reference );
        doNotCheckExceptions = _donotcheckexceptions;
    }


    public void configure(org.apache.avalon.framework.configuration.Configuration configuration)
        throws org.apache.avalon.framework.configuration.ConfigurationException
    {
        this.configuration = configuration;
        logger = 
            ((Configuration)configuration).getNamedLogger("jacorb.orb.delegate");
        useIMR = 
            configuration.getAttribute("jacorb.use_imr","off").equals("on");
        locateOnBind =
            configuration.getAttribute("jacorb.locate_on_bind","off").equals("on");

        if (objectReference != null)
            _pior = new ParsedIOR( objectReference, orb, logger);
        else if (ior!=null)
            _pior = new ParsedIOR( ior, orb, logger);
        else if (_pior == null )
            throw new ConfigurationException("Neither objectReference nor IOR set!");
        checkIfImR( _pior.getTypeId() );
   }


    public boolean doNotCheckExceptions()
    {
        return doNotCheckExceptions;
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
        if ( typeId.equals( "IDL:org/jacorb/imr/ImplementationRepository:1.0" ) )
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
        // ? why was that int synchronized block?
        if (bound)
            return ;

        synchronized (bind_sync)
        {
            if ( bound )
                return ;

            org.omg.ETF.Profile p = _pior.getEffectiveProfile();
            if (p == null)
                throw new org.omg.CORBA.COMM_FAILURE ("no effective profile");

            connection = conn_mg.getConnection(p);
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
                        new LocateRequestOutputStream
                            ( _pior.get_object_key(),
                              connection.getId(),
                              ( int ) _pior.getEffectiveProfile().version().minor );

                    LocateReplyReceiver receiver =
                        new LocateReplyReceiver(orb);

                    connection.sendRequest( lros,
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
                            rebind( orb.object_to_string( lris.read_Object() ) );
                            break;
                        }

                    case LocateStatusType_1_2._LOC_SYSTEM_EXCEPTION :
                        {
                            throw SystemExceptionHelper.read( lris );

                            //break;
                        }

                    case LocateStatusType_1_2._LOC_NEEDS_ADDRESSING_MODE :
                        {
                            throw new org.omg.CORBA.NO_IMPLEMENT( "Server responded to LocateRequest with a status of LOC_NEEDS_ADDRESSING_MODE, but this isn't yet implemented by JacORB" );

                            //break;
                        }

                    default :
                        {
                            throw new RuntimeException( "Unknown reply status for LOCATE_REQUEST: " + lris.rep_hdr.locate_status.value() );
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
                        logger.warn( e.getMessage() );
                }

            }

            //wake up threads waiting for the pior
            bind_sync.notifyAll();
        }
    }

    public void rebind(String object_reference)
    {
 //        synchronized (bind_sync)
//         {
            if (object_reference.indexOf( "IOR:" ) == 0)
            {
                rebind( new ParsedIOR( object_reference, orb, logger ) );
            }
            else
            {
                throw new org.omg.CORBA.INV_OBJREF( "Not an IOR: " +
                                                    object_reference );
            }
            //        }
    }

    public void rebind( org.omg.CORBA.Object o )
    {
        rebind(orb.object_to_string(o));
    }

    public void rebind( ParsedIOR p )
    {
        synchronized ( bind_sync )
        {
            if ( p.equals( _pior ) )
            {
                //already bound to target so just return
                return ;
            }

            if ( piorLastFailed != null && piorLastFailed.equals( p ) )
            {
                //we've already failed to bind to the ior
                throw new org.omg.CORBA.TRANSIENT();
            }

            if ( piorOriginal == null )
            {
                //keep original pior for fallback
                piorOriginal = _pior;
            }

            _pior = p;

            if ( connection != null )
            {
                conn_mg.releaseConnection( connection );
                connection = null;
            }

            //to tell bind() that it has to take action
            bound = false;

            bind();
        }
    }

    public org.omg.CORBA.Request create_request( org.omg.CORBA.Object self,
            org.omg.CORBA.Context ctx,
            java.lang.String operation ,
            org.omg.CORBA.NVList args,
            org.omg.CORBA.NamedValue result )
    {
        bind();

        return new org.jacorb.orb.dii.Request( self,
                                               orb,
                                               connection,
                                               getParsedIOR().get_object_key(),
                                               operation,
                                               args,
                                               ctx,
                                               result );
    }

    public org.omg.CORBA.Request create_request( org.omg.CORBA.Object self,
            org.omg.CORBA.Context ctx,
            String operation,
            org.omg.CORBA.NVList arg_list,
            org.omg.CORBA.NamedValue result,
            org.omg.CORBA.ExceptionList exceptions,
            org.omg.CORBA.ContextList contexts )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public synchronized org.omg.CORBA.Object duplicate( org.omg.CORBA.Object self )
    {
        return self;
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

    protected void finalize() throws Throwable
    {
        try
        {
            if ( connection != null )
            {
                // Synchronization for inc/dec of clients is handled inside
                // releaseConnection.
                conn_mg.releaseConnection( connection );
            }

            // Call using string rather than this to prevent data race warning.
            orb._release( getParsedIOR().getIORString() );

            if ( logger.isDebugEnabled() )
            {
                logger.debug("Delegate gc'ed!");
            }
        }
        finally
        {
            super.finalize();
        }
    }

    public org.omg.CORBA.DomainManager[] get_domain_managers
    ( org.omg.CORBA.Object self )
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
        else
        {
            // if not locally overridden, ask the server
            return get_policy( self,
                               policy_type,
                               request( self, "_get_policy", true ) );
        }
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
        Integer key = new Integer(policy_type);
        Policy result = null;

        if (policy_overrides != null)
        {
            result = (Policy)policy_overrides.get(key);
        }
        
        if ( result == null )
        {
            // no override at the object level for this type, now
            // check at the thread level, ie PolicyCurrent.
            // TODO: currently not implemented

            // check at the ORB-level
            org.omg.CORBA.Policy[] orbPolicies = 
                orb.getPolicyManager().get_policy_overrides(new int[]{policy_type});            
            if ( orbPolicies!= null && orbPolicies.length == 1)
                result =  orbPolicies[0];
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
            catch ( RemarshalException r )
            {
            }
            catch ( ApplicationException _ax )
            {
                String _id = _ax.getId();
                throw new RuntimeException( "Unexpected exception " + _id );
            }

        }
    } // get_policy

    public UtcT getRequestEndTime()
    {
        Policy p = get_client_policy (REQUEST_END_TIME_POLICY_TYPE.value);
        if (p != null)
            return ((org.omg.Messaging.RequestEndTimePolicy)p).end_time();
        else
            return null;
    }

    public UtcT getReplyEndTime()
    {
        Policy p = get_client_policy (REPLY_END_TIME_POLICY_TYPE.value);
        if (p != null)
            return ((org.omg.Messaging.ReplyEndTimePolicy)p).end_time();
        else
            return null;
    }

    public UtcT getRequestStartTime()
    {
        Policy p = get_client_policy (REQUEST_START_TIME_POLICY_TYPE.value);
        if (p != null)
            return ((org.omg.Messaging.RequestStartTimePolicy)p).start_time();
        else
            return null;
    }

    public UtcT getReplyStartTime()
    {
        Policy p = get_client_policy (REPLY_START_TIME_POLICY_TYPE.value);
        if (p != null)
            return ((org.omg.Messaging.ReplyStartTimePolicy)p).start_time();
        else
            return null;
    }

    public long getRelativeRoundtripTimeout()
    {
        Policy p = get_client_policy (RELATIVE_RT_TIMEOUT_POLICY_TYPE.value);
        if (p != null)
            return ((org.omg.Messaging.RelativeRoundtripTimeoutPolicy)p)
                                                            .relative_expiry();
        else
            return -1;
    }

    public long getRelativeRequestTimeout()
    {
        Policy p = get_client_policy (RELATIVE_REQ_TIMEOUT_POLICY_TYPE.value);
        if (p != null)
            return ((org.omg.Messaging.RelativeRequestTimeoutPolicy)p)
                                                            .relative_expiry();
        else
            return -1;
    }

    public short getSyncScope()
    {
        Policy p = get_client_policy (SYNC_SCOPE_POLICY_TYPE.value);
        if (p != null)
            return ((org.omg.Messaging.SyncScopePolicy)p).synchronization();
        else
            return ((org.omg.Messaging.SYNC_WITH_TRANSPORT.value));
    }

    /**
     * @deprecated Deprecated by CORBA 2.3
     */

    public org.omg.CORBA.InterfaceDef get_interface( org.omg.CORBA.Object self )
    {
        return org.omg.CORBA.InterfaceDefHelper.narrow( get_interface_def( self ) ) ;
    }

    public org.omg.CORBA.Object get_interface_def (org.omg.CORBA.Object self)
    {
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
                return (servant._get_interface_def());
            }
            finally
            {
                servant_postinvoke (self, so);
            }
        }
        else
        {
            org.omg.CORBA.portable.OutputStream os;
            org.omg.CORBA.portable.InputStream is;

            while (true)
            {
                try
                {
                    os = request (self, "_interface", true);
                    is = invoke (self, os);
                    return is.read_Object();
                }
                catch (RemarshalException re)
                {
                }
                catch (Exception ex)
                {
                    return null;
                }
            }
        }
    }

    ClientConnection getConnection()
    {
        synchronized ( bind_sync )
        {
            bind();
            return connection;
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
            else
            {
                return getParsedIOR().getIOR();
            }
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
            while ( _pior == null )
            {
                try
                {
                    bind_sync.wait();
                }
                catch ( InterruptedException ie )
                {
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
        return ( org.jacorb.poa.POA ) poa;
    }

    /**
     */

    public org.omg.CORBA.portable.ObjectImpl getReference( org.jacorb.poa.POA _poa )
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug("Delegate.getReference with POA <" +
                          ( _poa != null ? _poa._getQualifiedName() : " empty" ) + ">" );
        }

        if ( _poa != null )   // && _poa._localStubsSupported())
            poa = _poa;

        org.omg.CORBA.portable.ObjectImpl o =
            new org.jacorb.orb.Reference( typeId() );

        o._set_delegate( this );

        return o;
    }

    public int hash( org.omg.CORBA.Object self, int x )
    {
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

    /**
     * Internal implementation of both invoke() methods.  Note that
     * the boolean argument <code>async</code> is necessary to differentiate
     * between synchronous and asynchronous calls, because the ReplyHandler
     * can be null even for an asynchronous call.
     */
    private org.omg.CORBA.portable.InputStream invoke_internal
                               ( org.omg.CORBA.Object self,
                                 org.omg.CORBA.portable.OutputStream os,
                                 org.omg.Messaging.ReplyHandler replyHandler,
                                 boolean async )
        throws ApplicationException, RemarshalException
    {
        RequestOutputStream ros      = (RequestOutputStream)os;
        ReplyReceiver       receiver = null;

        ClientInterceptorHandler interceptors =
            new ClientInterceptorHandler( orb, ros, self, this,
                                          piorOriginal, connection );

        interceptors.handle_send_request();

        try
        {
            if ( !ros.response_expected() )  // oneway op
            {
                invoke_oneway (ros, interceptors);
                return null;
            }
            else  // response expected, synchronous or asynchronous
            {
                receiver = new ReplyReceiver(this,
                                             ros.operation(),
                                             ros.getReplyEndTime(),
                                             interceptors,
                                             replyHandler );

                // Store the receiver in pending_replies, so in the
                // case of a LocationForward a RemarshalException can
                // be thrown to *all* waiting threads.

                synchronized ( pending_replies )
                {
                    pending_replies.add ( receiver );
                }

                synchronized ( bind_sync )
                {
                    if ( ros.getConnection() == connection )
                    {
                        //RequestOutputStream has been created for
                        //exactly this connection
                        connection.sendRequest( ros,
                                                receiver,
                                                ros.requestId(),
                                                true ); // response expected
                    }
                    else
                    {
                        if (logger.isDebugEnabled())
                            logger.debug("invoke: RemarshalException");

                        // RequestOutputStream has been created for
                        // another connection, so try again
                        throw new RemarshalException();
                    }
                }
            }
        }
        catch ( org.omg.CORBA.SystemException cfe )
        {
            if (logger.isDebugEnabled())
                logger.debug("invoke: SystemException");

            if( !async )
            {
               // Remove ReplyReceiver to break up reference cycle
               // Otherwise gc will not detect this Delegate and
               // will never finalize it.
               synchronized (pending_replies)
               {
                   pending_replies.remove (receiver);
               }
            }

            interceptors.handle_receive_exception ( cfe );

            if ( cfe instanceof org.omg.CORBA.TRANSIENT )
            {
                // The exception is a TRANSIENT, so try rebinding.
                if ( try_rebind() )
                {
                    throw new RemarshalException();
                }
            }

            throw cfe;
        }

        if ( !async && receiver != null )
        {
            // Synchronous invocation, response expected.
            // This call blocks until the reply arrives.
            org.omg.CORBA.portable.InputStream is = receiver.getReply();

            return is;
        }
        else
        {
            return null;
        }
    }

    private void invoke_oneway (RequestOutputStream ros,
                                ClientInterceptorHandler interceptors)
        throws RemarshalException, ApplicationException
    {
        switch (ros.syncScope())
        {
            case SYNC_NONE.value:
                passToTransport (ros);
                interceptors.handle_receive_other (SUCCESSFUL.value);
                break;

            case SYNC_WITH_TRANSPORT.value:
                connection.sendRequest (ros, false);
                interceptors.handle_receive_other (SUCCESSFUL.value);
                break;

            case SYNC_WITH_SERVER.value:
            case SYNC_WITH_TARGET.value:
                ReplyReceiver rcv = new ReplyReceiver (this,
                                                       ros.operation(),
                                                       ros.getReplyEndTime(),
                                                       interceptors,
                                                       null);
                connection.sendRequest (ros, rcv, ros.requestId(), true);
                ReplyInputStream in = rcv.getReply();
                interceptors.handle_receive_reply (in);
                break;

            default:
                throw new org.omg.CORBA.MARSHAL
                    ("Illegal SYNC_SCOPE: " + ros.syncScope(),
                     0, CompletionStatus.COMPLETED_MAYBE);
        }
    }

    private void passToTransport (final RequestOutputStream ros)
    {
        new Thread (new Runnable()
        {
            public void run()
            {
                connection.sendRequest (ros, false);
            }
        }).start();
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
                corbaloc.append( ":" );
                corbaloc.append( imr.getImRPort() );
                corbaloc.append( "/" );
                corbaloc.append( CorbaLoc.parseKey( object_key ) );

                //rebind to the new IOR
                rebind( new ParsedIOR( corbaloc.toString(), orb, logger));

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
        catch ( org.omg.PortableInterceptor.ForwardRequest fwd )
        {
            rebind( orb.object_to_string( fwd.forward ) );
            throw new RemarshalException();
        }
        catch ( org.omg.CORBA.UserException ue )
        {
            if (logger.isErrorEnabled())
                logger.error( ue.getMessage() );
        }
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

        ParsedIOR pior = getParsedIOR();

        if ( pior.getTypeId().equals( logical_type_id ) )
            return true;

        /*   The Ids in ObjectImpl will at least contain the type id
             found in the object reference itself.
        */
        String[] ids = ( ( org.omg.CORBA.portable.ObjectImpl ) self )._ids();

        /* the last id will be CORBA.Object, and we know that already... */
        for ( int i = 0; i < ids.length - 1; i++ )
        {
            if ( ids[ i ].equals( logical_type_id ) )
                return true;
        }

        /* ok, we could not affirm by simply looking at the locally available
           type ids, so ask the object itself */

        // If local object call _is_a directly
        if (is_really_local (self))
        {
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
        else
        {
            // Try to avoid remote call - is it a derived type?
            try
            {
                // Retrieve the local stub for the object in question. Then call the _ids method
                // and see if any match the logical_type_id otherwise fall back to remote.

                String classname = RepositoryID.className( ids[0], "_Stub", null );
                
                int lastDot = classname.lastIndexOf( '.' );
                StringBuffer scn = new StringBuffer( classname.substring( 0, lastDot + 1) );
                scn.append( '_' );
                scn.append( classname.substring( lastDot + 1 ) );

                // This will only work if there is a correspondence between the Java class
                // name and the Repository ID. If prefixes have been using then this mapping
                // may have been lost.

                // First, search with stub name
                // if not found, try with the 'org.omg.stub' prefix to support package
                // with javax prefix
                Class stub=null;
                try {
                    stub = ObjectUtil.classForName( scn.toString());
                } catch (ClassNotFoundException e) {
                    stub = ObjectUtil.classForName("org.omg.stub."+scn.toString());
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
            catch (Throwable e)
            {
                if (logger.isDebugEnabled())
                    logger.debug("trying is_a remotely");
            }

            org.omg.CORBA.portable.OutputStream os;
            org.omg.CORBA.portable.InputStream is;

            while (true)
            {
                try
                {
                    os = request(self, "_is_a", true);
                    os.write_string(logical_type_id);
                    is = invoke(self, os);
                    return is.read_boolean();
                }
                catch (RemarshalException re)
                {
                }
                catch (ApplicationException ax)
                {
                    throw new RuntimeException ("Unexpected exception " + ax.getId());
                }
            }
        }
    }

    public boolean is_equivalent(org.omg.CORBA.Object self,
                                 org.omg.CORBA.Object obj)
    {
        boolean result = true;

        if (self != obj)
        {
            ParsedIOR pior1 = new ParsedIOR( obj.toString(), orb, logger );
            ParsedIOR pior2 = new ParsedIOR( self.toString(), orb, logger );
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
        if (orb.hasRequestInterceptors())
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
        else
        {
            org.omg.CORBA.portable.OutputStream os;
            org.omg.CORBA.portable.InputStream is;

            while (true)
            {
                try
                {
                    os = request(self, "_non_existent", true);
                    is = invoke(self, os);
                    return is.read_boolean();
                }
                catch (RemarshalException re)
                {
                }
                catch (ApplicationException e)
                {
                    throw new RuntimeException( "Unexpected exception " + e.getId() );
                }
            }
        }
    }

    public org.omg.CORBA.ORB orb( org.omg.CORBA.Object self )
    {
        return orb;
    }

    public synchronized void release( org.omg.CORBA.Object self )
    {
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
            }
        }
        Time.waitFor (getReplyStartTime());
    }

    public synchronized org.omg.CORBA.Request request( org.omg.CORBA.Object self,
                                                       String operation )
    {
        synchronized ( bind_sync )
        {
            bind();
            return new org.jacorb.orb.dii.Request( self,
                                                   orb,
                                                   connection,
                                                   getParsedIOR().get_object_key(),
                                                   operation );
        }

    }

    /**
     */

    public synchronized org.omg.CORBA.portable.OutputStream request
                                                 ( org.omg.CORBA.Object self,
                                                   String operation,
                                                   boolean responseExpected )
    {
        // Compute the deadlines for this request based on any absolute or
        // relative timing policies that have been specified.  Compute this
        // now, because it is the earliest possible time, and therefore any
        // relative timeouts will cover the entire invocation.

        UtcT requestEndTime = getRequestEndTime();
        long requestTimeout = getRelativeRequestTimeout();

        if ((requestTimeout != 0) || (requestEndTime != null)) {
            requestEndTime = Time.earliest(Time.corbaFuture (requestTimeout),
                                           requestEndTime);
            if (Time.hasPassed(requestEndTime))
                throw new TIMEOUT("Request End Time exceeded prior to invocation",
                                  0, CompletionStatus.COMPLETED_NO);
        }

        UtcT replyEndTime     = getReplyEndTime();
        long roundtripTimeout = getRelativeRoundtripTimeout();

        if ((roundtripTimeout != 0) || (replyEndTime != null)) {
            replyEndTime = Time.earliest(Time.corbaFuture (roundtripTimeout),
                                         replyEndTime);
            if (Time.hasPassed(replyEndTime))
                throw new TIMEOUT("Reply End Time exceeded prior to invocation",
                                  0, CompletionStatus.COMPLETED_NO);
        }

        synchronized ( bind_sync )
        {
            bind();

            ParsedIOR p = getParsedIOR();

            RequestOutputStream ros =
                new RequestOutputStream( connection,
                                         connection.getId(),
                                         operation,
                                         responseExpected,
                                         getSyncScope(),
                                         getRequestStartTime(),
                                         requestEndTime,
                                         replyEndTime,
                                         p.get_object_key(),
                                         ( int ) p.getEffectiveProfile().version().minor );

            // CodeSets are only negotiated once per connection,
            // not for each individual request
            // (CORBA 3.0, 13.10.2.6, second paragraph).
            if ( ! connection.isTCSNegotiated() )
            {
                ServiceContext ctx = connection.setCodeSet( p );

                if ( ctx != null )
                {
                    ros.addServiceContext( ctx );
                }

            }

            //Setting the codesets not until here results in the
            //header being writtend using the default codesets. On the
            //other hand, the server side must have already read the
            //header to discover the codeset service context.
            ros.setCodeSet( connection.getTCS(), connection.getTCSW() );

            return ros;
        }

    }

    /**
     * Overrides servant_postinvoke() in org.omg.CORBA.portable.Delegate<BR>
     * called from generated stubs after a local operation
     */


    public void servant_postinvoke( org.omg.CORBA.Object self, ServantObject servant )
    {
        if (poa != null)
        {
            if ( poa.isUseServantManager() )
            {
               if (! poa.isRetain() &&
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
                          logger.warn( e.getMessage() );
                  }
               }
            }
           poa.removeLocalRequest();
        }
        orb.getPOACurrent()._removeContext( Thread.currentThread() );
    }

    /**
     * Overrides servant_preinvoke() in org.omg.CORBA.portable.Delegate<BR>
     * called from generated stubs before a local operation
     */

    public ServantObject servant_preinvoke( org.omg.CORBA.Object self,
                                            String operation,
                                            Class expectedType )
    {
        if (poa == null)
        {
            resolvePOA(self);
        }

        if (poa != null)
        {
            // remember that a local request is outstanding. On
            //  any exit through an exception, this must be cleared again,
            // otherwise the POA will hangon destruction (bug #400).
            poa.addLocalRequest();

            ServantObject so = new ServantObject();

            try
            {
                if ( ( poa.isRetain() && !poa.isUseServantManager() ) ||
                     poa.useDefaultServant() )
                {
                    // no ServantManagers, but AOM use
                    try
                    {
                        so.servant = poa.reference_to_servant( self );
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
                        // ServantManager is a ServantActivator:
                        // see if the servant is already activated
                        try
                        {
                            so.servant = poa.id_to_servant( oid );
                        }
                        catch( ObjectNotActive ona )
                        {
                            // okay, we need to activate it
                            org.omg.PortableServer.ServantActivator sa =
                                ( org.omg.PortableServer.ServantActivator ) sm ;
                            org.omg.PortableServer.ServantActivatorHelper.narrow( sm );
                            so.servant = sa.incarnate( oid, poa );
                            orb.set_delegate(so.servant);
                        }
                    }
                    else
                    {
                        // ServantManager is a ServantLocator:
                        // locate a servant

                        org.omg.PortableServer.ServantLocator sl =
                            ( org.omg.PortableServer.ServantLocator ) sm;

                        // store this for postinvoke

                        cookie =
                           new org.omg.PortableServer.ServantLocatorPackage.CookieHolder();

                        invokedOperation = operation;

                        so.servant =
                            sl.preinvoke( oid, poa, operation, cookie );
                    }
                }
                else
                {
                    throw new INTERNAL("Internal error: we should not have gotten to this piece of code!");
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
                return servant_preinvoke(e.forward_reference, operation, expectedType);
            }

            if ( !expectedType.isInstance( so.servant ) )
            {
                if( logger.isWarnEnabled() )
                {
                    logger.warn("Expected " + expectedType +
                                " got " + so.servant.getClass() );
                }

                poa.removeLocalRequest();
                return null;
            }
            else
            {
                orb.getPOACurrent()._addContext(
                    Thread.currentThread(),
                    new org.jacorb.poa.LocalInvocationContext(
                        orb,
                        poa,
                        getObjectId(),
                        ( org.omg.PortableServer.Servant ) so.servant
                                                             )
                                               );
            }
            return so;
        }
        if (logger.isWarnEnabled())
        {
            logger.warn("No POA! servant_preinvoke returns null");
        }
        return null;
    }

    public String toString()
    {
        synchronized ( bind_sync )
        {
            if ( piorOriginal != null )
                return piorOriginal.getIORString();
            else
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
        if (policy_overrides == null)
        {
            policy_overrides = new HashMap();
        }
        if ( set_add == org.omg.CORBA.SetOverrideType.SET_OVERRIDE )
        {
            policy_overrides.clear();
        }

        for ( int i = 0; i < policies.length; i++ )
        {
            policy_overrides.put( new Integer( policies[ i ].policy_type() ), policies[ i ] );
        }

        return self;
    }

    public String get_codebase( org.omg.CORBA.Object self )
    {
        return getParsedIOR().getCodebaseComponent();
    }

    public Set get_pending_replies()
    {
        return pending_replies;
    }

    public void replyDone (ReplyPlaceholder placeholder)
    {
        synchronized (pending_replies)
        {
            pending_replies.remove (placeholder);
        }
    }

    public void lockBarrier()
    {
        pending_replies_sync.lockBarrier();
    }

    public void waitOnBarrier()
    {
        pending_replies_sync.waitOnBarrier();
    }

    public void openBarrier()
    {
        pending_replies_sync.openBarrier();
    }

    private static class Barrier
    {
        private boolean is_open = true;

        public synchronized void waitOnBarrier()
        {
            while (! is_open)
            {
                try
                {
                    this.wait();
                }
                catch ( InterruptedException e )
                {
                    //ignore
                }
            }
        }

        public synchronized void lockBarrier()
        {
            is_open = false;
        }

        public synchronized void openBarrier()
        {
            is_open = true;

            this.notifyAll();
        }
    }
}
