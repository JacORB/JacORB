package org.jacorb.orb;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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
import java.lang.Object;
import java.net.*;

import org.jacorb.util.*;
import org.jacorb.orb.domain.*;
import org.jacorb.orb.connection.*;
import org.jacorb.poa.POAConstants;
import org.jacorb.orb.portableInterceptor.*;

import org.omg.CORBA.portable.*;
import org.omg.PortableInterceptor.*;
import org.omg.IOP.ServiceContext;
import org.omg.GIOP.*;
import org.omg.CORBA.SystemException;

/**
 * JacORB implementation of CORBA object reference
 *
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 *
 */

public final class Delegate
    extends org.omg.CORBA_2_3.portable.Delegate
{
    // WARNING: DO NOT USE _pior DIRECTLY, BECAUSE THAT IS NOT MT
    // SAFE. USE getParsedIOR() INSTEAD, AND KEEP A METHOD-LOCAL COPY
    // OF THE REFERENCE.
    private ParsedIOR _pior = null;
    private ClientConnection connection = null;

    /** domain service used to implement get_policy and get_domain_managers */
    private static Domain _domainService = null;

    /* save original ior for fall-back */
    private ParsedIOR piorOriginal = null;

    private boolean bound = false;
    private org.jacorb.poa.POA poa;

    private org.omg.CORBA.ORB orb = null;
    
    private boolean use_interceptors = false;

    /** set after the first attempt to determine whether 
        this reference is to a local object */
    private boolean resolved_locality = false;

    private Hashtable pending_replies = new Hashtable();
    private Barrier pending_replies_sync = new Barrier();

    private Object bind_sync = new Object();

    private boolean locate_on_bind_performed = false;

    private ConnectionManager conn_mg = null;

    private Hashtable policy_overrides = new Hashtable();

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
    public Delegate()
    {}

    protected Delegate(org.omg.CORBA.ORB orb, ParsedIOR pior )
    {
        this.orb = orb;
        _pior = pior;

        conn_mg = ((ORB) orb).getConnectionManager();

        initInterceptors();
    }

    protected Delegate(org.omg.CORBA.ORB orb, String object_reference ) 
    {
        this.orb = orb;
        if ( object_reference.indexOf("IOR:") == 0)
        {
            _pior = new ParsedIOR( object_reference );
        }
        else
        {
            throw new org.omg.CORBA.INV_OBJREF( "Not an IOR: " + 
                                                object_reference );
        }

        conn_mg = ((ORB) orb).getConnectionManager();

        initInterceptors();
    }

    protected Delegate(org.omg.CORBA.ORB orb, org.omg.IOP.IOR _ior )
    {
        this.orb = orb;
        _pior = new ParsedIOR( _ior );

        conn_mg = ((ORB) orb).getConnectionManager();

        initInterceptors();
    }


    public int _get_TCKind() 
    {
        return org.omg.CORBA.TCKind._tk_objref;
    }


    /**
     * This bind is a combination of the old _init() and bind()
     * operations. It first inits this delegate with the information
     * supplied by the (parsed) IOR. The it requests a new
     * ClientConnection from the ConnectionsManager. This will *NOT*
     * open up a TCP connection, but the connection is needed for the
     * GIOP message ids. The actual TCP connection is automatically
     * opend up by the ClientConnection, when the first request is
     * sent. This has the advantage, that COMM_FAILURES can only occur
     * inside of _invoke, where they get handled properly (falling
     * back, etc.)
     *  */
    private void bind() 
    { 
        synchronized( bind_sync )
        {
            if( bound )
                return;

            _pior.init();
    
            connection = conn_mg.getConnection( _pior.getAdPort(),
                                                _pior.useSSL() );
            bound = true;
            
            /* The delegate could query the server for the object
             *  location using a GIOP locate request to make sure the
             *  first call will get through without redirections
             *  (provided the server's answer is definite): 
             */
            if( (! locate_on_bind_performed) &&
                Environment.locateOnBind() )
            {  
                //only locate once, because bind is called from the
                //switch statement below again.
                locate_on_bind_performed = true;

                try
                {
                    LocateRequestOutputStream lros = 
                        new LocateRequestOutputStream( _pior.get_object_key(), 
                                                       connection.getId(),
                                                       (int) _pior.getProfileBody().iiop_version.minor );
                    
                    ReplyPlaceholder place_holder = new ReplyPlaceholder();

                    connection.sendRequest( lros,
                                            place_holder,
                                            lros.getRequestId() );
                    
                    
                    LocateReplyInputStream lris =
                        (LocateReplyInputStream) place_holder.getInputStream();
                    
                    switch( lris.rep_hdr.locate_status.value() )
                    {
                        case LocateStatusType_1_2._OBJECT_HERE :
                        {
                            Debug.output(3,"object here");
                            
                            break;
                        }
                        case LocateStatusType_1_2._OBJECT_FORWARD :
                        {
                            Debug.output(3,"Locate Reply: Forward");
                            
                            rebind( orb.object_to_string( lris.read_Object()) );
                            
                            break;
                        }
                        case LocateStatusType_1_2._UNKNOWN_OBJECT :
                        {
                            throw new org.omg.CORBA.UNKNOWN("Could not bind to object, server does not know it!");
                        }
                        default :
                        {
                            throw new RuntimeException("Unknown reply status for LOCATE_REQUEST: " + lris.rep_hdr.locate_status.value());
                        }
                    }
                }
                catch( Exception e )
                {
                    Debug.output( 1, e );
                }
            }

            //wake up threads waiting for the pior
            bind_sync.notifyAll();
        }
    }

    private void rebind( String object_reference ) 
    {
        synchronized( bind_sync )
        {
            if( object_reference.indexOf("IOR:") == 0 )
            {
                rebind( new ParsedIOR( object_reference ));
            }
            else
            {
                throw new org.omg.CORBA.INV_OBJREF( "Not an IOR: " + 
                                                    object_reference );
            }
        }
    }

    private void rebind( ParsedIOR p )
    {
        synchronized( bind_sync )
        {
            if( p.equals( _pior ))
            {
                //already bound to target, so just return
                return;
            }

            //keep "old" pior for fallback
            piorOriginal = _pior;
            
            _pior = p;

            if( connection != null )
            {
                conn_mg.releaseConnection( connection );
                connection = null;
            }

            //to tell bind() that it has to take action
            bound = false;

            bind();
        }
    }    
        
    /**
     * Fallback is a rebind() to the piorOriginal, with nulling that
     * attribute afterwards.  
     * 
     * @return true, if piorOriginal was not null. false, otherwise
     */
    private boolean fallback() 
    {
        synchronized( bind_sync )
        {
            if( piorOriginal != null )
            {
                Debug.output(2, "Delegate: falling back to original IOR");

                rebind( piorOriginal );
                
                //clean up;
                piorOriginal = null;
                
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    public org.omg.CORBA.Request create_request(org.omg.CORBA.Object self,
                                                org.omg.CORBA.Context ctx,
                                                java.lang.String operation ,
                                                org.omg.CORBA.NVList args, 
                                                org.omg.CORBA.NamedValue result)
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

    /** 
     */

    public org.omg.CORBA.Request create_request(org.omg.CORBA.Object self, 
                                                org.omg.CORBA.Context ctx, 
                                                String operation, 
                                                org.omg.CORBA.NVList arg_list, 
                                                org.omg.CORBA.NamedValue result, 
                                                org.omg.CORBA.ExceptionList exceptions, 
                                                org.omg.CORBA.ContextList contexts)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public synchronized org.omg.CORBA.Object duplicate(org.omg.CORBA.Object self)
    {
        return self;
    }

    public boolean equals(java.lang.Object obj)
    {
        return ( obj instanceof org.omg.CORBA.Object && 
                 toString().equals( obj.toString() ));
    }

    public boolean equals(org.omg.CORBA.Object self, java.lang.Object obj)
    {
        return equals(obj);
    }

    /**
     * 
     */

    public void finalize()
    {
        if( connection != null )
        {
            conn_mg.releaseConnection( connection );
        }

        ((org.jacorb.orb.ORB)orb)._release( this );


        Debug.output(3," Delegate gc'ed!");
    }

    public String get_adport()
    {
        return getParsedIOR().getAdPort();
    }

    private org.jacorb.orb.domain.Domain _domainService()
    {
        if (_domainService == null)
        {
            try 
            { 

                Debug.output(Debug.DOMAIN | Debug.DEBUG1, 
                             "Delegate._domainService: fetching "
                             +"global domain service reference from orb");

                _domainService= org.jacorb.orb.domain.DomainHelper.narrow
                    ( this.orb.resolve_initial_references("DomainService") );
            }
            catch (Exception e) 
            {
                Debug.output(Debug.DOMAIN | Debug.IMPORTANT, e);
            }
        }

        return _domainService;
    } // _domainService


    public org.omg.CORBA.DomainManager[] get_domain_managers
        (org.omg.CORBA.Object self)
    {    
        // ask object implementation
        while(true)
        {
            try
            {
                org.omg.CORBA.portable.OutputStream os = 
                    request(self, "_get_domain_managers", true );

                os.write_Object(self);
                org.omg.CORBA.portable.InputStream is = invoke( self, os );

                return org.jacorb.orb.domain.DomainListHelper.read( is );
            }
            catch ( RemarshalException r ){}
            catch( ApplicationException _ax )
            {
                String _id = _ax.getId();
                throw new RuntimeException("Unexpected exception " + _id );
            }

            //   catch (org.omg.CORBA.BAD_OPERATION e)
            //              { // object implementation does not support domain service
            //              // therefore ask global domain service 
            //              Debug.output
            //                  (2,"Delegate: catched BAD_OPERATION exception, resuming "
            //                   + "operation _domain_managers() by getting domains from "
            //                   + "global domain service");
            //              return _domainService().getDomains(self);
            //              }
        }
    } // get_domain_managers


    /**
     * this is get_policy without the call to request(), which would
     * invoke interceptors.
     */
    public org.omg.CORBA.Policy get_policy_no_intercept(org.omg.CORBA.Object self, 
                                                        int policy_type)
    {
        RequestOutputStream _os = null;

        synchronized( bind_sync )
        {
            bind();
            
            ParsedIOR p = getParsedIOR();

            _os = 
                new RequestOutputStream( connection,
                                         connection.getId(),
                                         "_get_policy", 
                                         true, 
                                         p.get_object_key(),
                                         (int) p.getProfileBody().iiop_version.minor );

            //Problem: What about the case where different objects
            //that are accessed by the same connection have different
            //codesets?  Is this possible anyway?
            if( ! connection.isTCSNegotiated() )
            {
                ServiceContext ctx = connection.setCodeSet( p );

                if( ctx != null )
                {
                    _os.addServiceContext( ctx );
                }
            }

            //Setting the codesets not until here results in the
            //header being writtend using the default codesets. On the
            //other hand, the server side must have already read the
            //header to discover the codeset service context.
            _os.setCodeSet( connection.getTCS(), connection.getTCSW() );

        }
        
        return get_policy(self, policy_type, _os);
    }



    public org.omg.CORBA.Policy get_policy(org.omg.CORBA.Object self, 
                                           int policy_type)
    {
        return get_policy( self, 
                           policy_type,  
                           request(self, "_get_policy", true ));
    }



    public org.omg.CORBA.Policy get_policy(org.omg.CORBA.Object self, 
                                           int policy_type,
                                           org.omg.CORBA.portable.OutputStream os)
    {
        // ask object implementation
        while(true)
        {
            try
            {
                os.write_Object(self);
                os.write_long(policy_type);
                org.omg.CORBA.portable.InputStream is = invoke( self, os );
                return org.omg.CORBA.PolicyHelper.narrow( is.read_Object());
            }
            catch ( RemarshalException r ){}
            catch( ApplicationException _ax )
            {
                String _id = _ax.getId();
                throw new RuntimeException("Unexpected exception " + _id );
            }
        }
    } // get_policy


    /**
     * @deprecated Deprecated by CORBA 2.3
     */
    public org.omg.CORBA.InterfaceDef get_interface(org.omg.CORBA.Object self)
    {
        return org.omg.CORBA.InterfaceDefHelper.narrow(get_interface_def(self)) ;
    }


    public org.omg.CORBA.Object get_interface_def(org.omg.CORBA.Object self)
    {
        while(true)
        {
            try
            {       
                org.omg.CORBA.portable.OutputStream os = 
                    request(self, "_interface", true);

                org.omg.CORBA.portable.InputStream is = 
                    invoke( self, os );
                
                return is.read_Object();
            }
            catch ( RemarshalException r ){}
            catch( Exception n )        
            {
                return null;
            }
        }
    }
  
    ClientConnection getConnection()
    {
         synchronized( bind_sync )
        {
            bind();

            return connection;
        }
    }

    public org.omg.IOP.IOR getIOR()
    {
        synchronized( bind_sync )
        {
            if( piorOriginal != null )
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
        synchronized( bind_sync )
        {
            bind();

            return org.jacorb.poa.util.POAUtil.extractOID( getParsedIOR().get_object_key() );
        }
    }

    public byte[] getObjectKey()
    {
        synchronized( bind_sync )
        {
            bind();

            return getParsedIOR().get_object_key();
        }
    }

    public ParsedIOR getParsedIOR()
    {
        synchronized( bind_sync )
        {
            while( _pior == null )
            {
                try
                {
                    bind_sync.wait();
                }
                catch( InterruptedException ie )
                {}
            }

            return _pior;
        }
    }

    public org.jacorb.poa.POA getPOA()
    {
        return (org.jacorb.poa.POA)poa;
    }

    /**
     */

    public org.omg.CORBA.portable.ObjectImpl getReference( org.jacorb.poa.POA _poa )
    {
        Debug.output( 3, "Delegate.getReference with POA <" + 
                      ( _poa != null ? _poa._getQualifiedName() : " empty" ) + ">");
        if( _poa != null ) // && _poa._localStubsSupported())
            poa = _poa;
        org.omg.CORBA.portable.ObjectImpl o = 
            new org.jacorb.orb.Reference( typeId() );
        o._set_delegate(this);
        return o;
    }

    public int hash(org.omg.CORBA.Object self, int x)
    {
        return hashCode();
    }

    public int hashCode()
    {
        return toString().hashCode();
    }

    public int hashCode(org.omg.CORBA.Object self)
    {
        return hashCode();
    }

    /**
     * invoke an operation using this object reference by sending 
     * the request marshalled in the OutputStream
     */

    public org.omg.CORBA.portable.InputStream invoke( org.omg.CORBA.Object self,
                                                      org.omg.CORBA.portable.OutputStream os)
        throws ApplicationException, RemarshalException
    {
        ClientRequestInfoImpl info = null;
        RequestOutputStream ros = null;

        ros = (RequestOutputStream) os;
    
        if ( use_interceptors )
        {
            //set up info object
            info = new ClientRequestInfoImpl();
            info.orb = (org.jacorb.orb.ORB) orb;
            info.operation = ros.operation();
            info.response_expected = ros.response_expected();
            info.received_exception = orb.create_any();
    
            if (ros.getRequest() != null)
                info.setRequest(ros.getRequest());
    
            info.effective_target = self;

            ParsedIOR pior = getParsedIOR();

            if( piorOriginal != null )
                info.target = ((org.jacorb.orb.ORB) orb)._getObject(pior);
            else
                info.target = self;
    
            info.effective_profile = pior.getEffectiveProfile();
                          
            // bnv: simply call pior.getProfileBody()
            org.omg.IIOP.ProfileBody_1_1 _body = pior.getProfileBody();
            if (_body != null)
                info.effective_components = _body.components;

            if ( info.effective_components == null )
            {
                info.effective_components = new org.omg.IOP.TaggedComponent[0];
            }
              
            info.delegate = this;
        
            info.request_id = ros.requestId();
            InterceptorManager manager = 
                ((org.jacorb.orb.ORB) orb).getInterceptorManager();

            info.current = manager.getCurrent();

            //allow interceptors access to request output stream
            info.request_os = ros;

            //allow (BiDir) interceptor to inspect the connection
            info.connection = connection;
    
            invokeInterceptors(info, ClientInterceptorIterator.SEND_REQUEST);

            //add service contexts to message
            Enumeration ctx = info.getRequestServiceContexts();
            while( ctx.hasMoreElements() )
            {
                ros.addServiceContext( (ServiceContext) ctx.nextElement() );
            }
        }

        ReplyPlaceholder placeholder = null;
        try
        {          
            if( ros.response_expected())
            {
                placeholder = new ReplyPlaceholder();

                //store pending replies, so in the case of a
                //LocationForward a RemarshalException can be thrown
                //to *all* waiting threads.

                synchronized( pending_replies )
                {
                    pending_replies.put( placeholder, placeholder );
                }

                synchronized( bind_sync )
                {
                    if( ros.getConnection() == connection )
                    {
                        //RequestOutputStream has been created for
                        //exactly this connection
                        connection.sendRequest( ros,
                                                placeholder,
                                                ros.requestId() );
                    }
                    else
                    {
                        //RequestOutputStream has been created for
                        //other connection, so try again.
                        throw new RemarshalException();
                    }
                }
            }
            else
            {
                connection.sendRequest( ros );
            }
        } 
        catch( org.omg.CORBA.SystemException cfe )
        {
            if (use_interceptors && (info != null)) {
                SystemExceptionHelper.insert(info.received_exception, cfe);

                try 
                {
                    info.received_exception_id = 
                        SystemExceptionHelper.type(cfe).id();
                } 
                catch(org.omg.CORBA.TypeCodePackage.BadKind _bk) 
                {
                    Debug.output(2, _bk);
                }
                
                info.reply_status = SYSTEM_EXCEPTION.value;
                            
                invokeInterceptors(info,
                                   ClientInterceptorIterator.RECEIVE_EXCEPTION);
            }
                
            if( fallback() )
            {
                /* now cause this invocation to be repeated by the
                   caller of invoke(), i.e. the stub 
                */
                throw new RemarshalException();
            } 
            else
            {
                throw cfe;
            }
        }

        /* look at the result stream now */
        
        if( placeholder != null )
        {
            //response is expected

            ReplyInputStream rep = null;

            try
            {
                //this blocks until the reply arrives
                rep = (ReplyInputStream) placeholder.getInputStream();
                
                //this will check the reply status and throw arrived
                //exceptions
                rep.checkExceptions();

                if ( use_interceptors && (info != null) )
                {
                    ReplyHeader_1_2 _header = rep.rep_hdr;
              
                    if (_header.reply_status.value() == ReplyStatusType_1_2._NO_EXCEPTION)
                    { 
                        info.reply_status = SUCCESSFUL.value;
            
                        info.setReplyServiceContexts( _header.service_context );
    
                        //the case that invoke was called from
                        //dii.Request._invoke() will be handled inside
                        //of dii.Request._invoke() itself, because the
                        //result will first be available there
                        if (ros.getRequest() == null) 
                        {
                            InterceptorManager manager = 
                                ((org.jacorb.orb.ORB) orb).getInterceptorManager();
                            info.current = manager.getCurrent();

                            //allow interceptors access to reply input stream
                            info.reply_is = rep;

                            invokeInterceptors(info,
                                               ClientInterceptorIterator.RECEIVE_REPLY);
                        }
                        else
                            ros.getRequest().setInfo(info);            
                    }
                }
    
                return rep;
            }
            catch( RemarshalException re )
            {
                //wait, until the thread that received the actual
                //ForwardRequest rebound this Delegate
                pending_replies_sync.waitOnBarrier();

                throw re;
            }
            catch( org.omg.PortableServer.ForwardRequest f )
            {
                if ( use_interceptors && (info != null) )
                {
                    info.reply_status = LOCATION_FORWARD.value;
                    info.setReplyServiceContexts(rep.rep_hdr.service_context);
            
                    info.forward_reference = f.forward_reference;

                    //allow interceptors access to reply input stream
                    info.reply_is = rep;

                    invokeInterceptors(info,
                                       ClientInterceptorIterator.RECEIVE_OTHER);
                }
              
                /* retrieve the forwarded IOR and bind to it */

                //make other threads, that have unreturned replies, wait
                pending_replies_sync.lockBarrier();

                //tell every pending request to remarshal
                //they will be blocked on the barrier
                synchronized( pending_replies )
                {
                    for( Enumeration e = pending_replies.elements();
                         e.hasMoreElements(); )
                    {
                        ReplyPlaceholder r = 
                               (ReplyPlaceholder) e.nextElement();
                        r.retry();
                    }
                }

                //do the actual rebind
                rebind(orb.object_to_string(f.forward_reference));    

                //now other threads can safely remarshal
                pending_replies_sync.openBarrier();

                throw new RemarshalException();
            }
            catch( SystemException _sys_ex )
            {
                if( use_interceptors && (info != null))
                {
                    info.reply_status = SYSTEM_EXCEPTION.value;
                    
                    info.setReplyServiceContexts(rep.rep_hdr.service_context);
    
                    SystemExceptionHelper.insert(info.received_exception, _sys_ex);
                    try
                    {
                        info.received_exception_id = 
                            SystemExceptionHelper.type(_sys_ex).id();
                    }
                    catch(org.omg.CORBA.TypeCodePackage.BadKind _bk)
                    {
                        Debug.output(2, _bk);
                    }

                    //allow interceptors access to reply input stream
                    info.reply_is = rep;
    
                    invokeInterceptors(info,
                                       ClientInterceptorIterator.RECEIVE_EXCEPTION);
                }
              
                throw _sys_ex;          
            }
            catch(ApplicationException _user_ex)
            {
                if (use_interceptors && (info != null))
                {
                    info.reply_status = USER_EXCEPTION.value;
                    info.setReplyServiceContexts(rep.rep_hdr.service_context);
            
                    info.received_exception_id  = _user_ex.getId();
            
                    rep.mark(0);
                    try
                    {
                        ApplicationExceptionHelper.insert(info.received_exception, _user_ex);
                    }
                    catch(Exception _e)
                    {
                        Debug.output(2, _e);
              
                        SystemExceptionHelper.insert(info.received_exception, 
                                                     new org.omg.CORBA.UNKNOWN(_e.getMessage()));
                    }
                    try
                    {
                        rep.reset();
                    }
                    catch (Exception _e)
                    {
                        //shouldn't happen anyway
                        Debug.output(2, _e);
                    }

                    //allow interceptors access to reply input stream
                    info.reply_is = rep;    

                    invokeInterceptors(info,
                                       ClientInterceptorIterator.RECEIVE_EXCEPTION);
                }    
                throw _user_ex;          
            }
            finally
            {
                //reply returned (with whatever result)
                synchronized( pending_replies )
                {
                    if( placeholder != null )
                    {
                        pending_replies.remove( placeholder );
                    }
                }
            }
        }
        else
        {
            if ( use_interceptors && (info != null) )
            {
                //oneway call
                info.reply_status = SUCCESSFUL.value;
    
                invokeInterceptors(info, ClientInterceptorIterator.RECEIVE_OTHER);
            }
          
            return null; //call was oneway
        }
    }

    public void invokeInterceptors(ClientRequestInfoImpl info, short op)
        throws RemarshalException
    {
        ClientInterceptorIterator intercept_iter = 
            ((org.jacorb.orb.ORB) orb).getInterceptorManager().getClientIterator();
        
        try
        {
            intercept_iter.iterate(info, op);
        }
        catch (org.omg.PortableInterceptor.ForwardRequest fwd)
        {
            rebind(orb.object_to_string(fwd.forward));    
            throw new RemarshalException();            
        }
        catch (org.omg.CORBA.UserException ue)
        {
            Debug.output(Debug.INTERCEPTOR | Debug.IMPORTANT, ue);
        }
    }
    
    /**
     * Determines whether the object denoted by self
     * has type logical_type_id or a subtype of it
     */

    public boolean is_a(org.omg.CORBA.Object self, String logical_type_id )
    {
        /* First, try to find out without a remote invocation. */

        /* check most derived type as defined in the IOR first 
         * (this type might otherwise not be found if the helper 
         * is consulted and the reference was not narrowed to
         * the most derived type. In this case, the ids returned by
         * the helper won't contain the most derived type
         */
         
        ParsedIOR pior = getParsedIOR();

        if( pior.getTypeId().equals( logical_type_id ))
                return true;
        
        /*   The Ids in ObjectImpl will at least contain the type id 
             found in the object reference itself.
        */
        String[] ids = ((org.omg.CORBA.portable.ObjectImpl)self)._ids();
    
        /* the last id will be CORBA.Object, and we know that already... */
        for( int i = 0; i < ids.length - 1; i++ )
        {
            if( ids[i].equals( logical_type_id ))
                return true;
        }
    
        /* ok, we could not affirm by simply looking at the locally available
           type ids, so ask the object itself */
    
        while(true)
        {
            try
            {
                org.omg.CORBA.portable.OutputStream os = request(self, "_is_a", true );
                os.write_string(logical_type_id);
                org.omg.CORBA.portable.InputStream is = invoke( self, os );
                return is.read_boolean();
            }
            catch ( RemarshalException r ){}
            catch( ApplicationException _ax )
            {
                String _id = _ax.getId();
                throw new RuntimeException("Unexpected exception " + _id );
            }
        }
    }

    public boolean is_equivalent(org.omg.CORBA.Object self,
                                 org.omg.CORBA.Object obj )
    {
        return self.toString().equals( obj.toString());
        //    return  hashCode() == obj.hashCode();
    }

    /** 
     * @return true iff this object lives on a local POA
     */

    public boolean is_local(org.omg.CORBA.Object self) 
    {     
        if( ! resolved_locality )
        {
            org.jacorb.poa.POA local_poa = 
                ((org.jacorb.orb.ORB)orb).findPOA( this, self );
            if( local_poa != null ) // && local_poa._localStubsSupported() )
                poa = local_poa;
//              Debug.output( 3, "Delegate.is_local found " + 
//                           ( local_poa != null ? " a " : " no ") + " local POA");
        }
        resolved_locality = true;
        //Debug.output( 5, "Delegate.is_local returns " + (poa != null ));
        return poa != null;
    }

    public boolean is_nil()    
    {
        ParsedIOR pior = getParsedIOR();

        return( pior.getIOR().type_id.equals("") && 
                pior.getIOR().profiles.length == 0 );
    }

    public boolean non_existent(org.omg.CORBA.Object self)
    {
        while(true)
        {
            try
            {       
                org.omg.CORBA.portable.OutputStream os = 
                    request(self, "_non_existent", true);

                org.omg.CORBA.portable.InputStream is = invoke( self, os );

                return is.read_boolean();
            }
            catch ( RemarshalException r )
            {
            }
            catch( Exception n )        
            {
                return true;
            }
        }
    }

    public org.omg.CORBA.ORB orb(org.omg.CORBA.Object self)
    {
        return orb;
    }

    public synchronized void release(org.omg.CORBA.Object self)
    {
    }

    /**
     * releases the InputStream
     */
    public void releaseReply( org.omg.CORBA.Object self, 
                              org.omg.CORBA.portable.InputStream is)
    {
        if( is != null )
        {
            try
            {
                is.close();
            }
            catch ( java.io.IOException io )
            {}
        }
    }

    public synchronized org.omg.CORBA.Request request(org.omg.CORBA.Object self,
                                                      String operation )
    {
        synchronized( bind_sync )
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

    public synchronized org.omg.CORBA.portable.OutputStream request( org.omg.CORBA.Object self,
                                                                     String operation,
                                                                     boolean responseExpected )
    {    
        // NOTE: When making changes to this method which are outside of the 
        // Interceptor-if-statement, please make sure to update 
        // get_poliy_no_intercept as well!
          
        synchronized( bind_sync )
        {
            bind();
     
            ParsedIOR p = getParsedIOR();

            RequestOutputStream ros = 
                new RequestOutputStream( connection,  
                                         connection.getId(),
                                         operation, 
                                         responseExpected, 
                                         p.get_object_key(),
                                         (int) p.getProfileBody().iiop_version.minor );

            //Problem: What about the case where different objects
            //that are accessed by the same connection have different
            //codesets?  Is this possible anyway?
            if( ! connection.isTCSNegotiated() )
            {
                ServiceContext ctx = connection.setCodeSet( p );
                
                if( ctx != null )
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
     * @overrides servant_postinvoke() in org.omg.CORBA.portable.Delegate<BR>
     * called from generated stubs after a local operation
     */


    public void servant_postinvoke(org.omg.CORBA.Object self, ServantObject servant) 
    {
        ((org.jacorb.orb.ORB)orb).getPOACurrent()._removeContext(Thread.currentThread());
    }

    /**
     * @overrides servant_preinvoke() in org.omg.CORBA.portable.Delegate<BR>
     * called from generated stubs before a local operation
     */

    public ServantObject servant_preinvoke( org.omg.CORBA.Object self, 
                                            String operation, 
                                            Class expectedType) 
    {     
        if (poa != null) 
        {
            /* make sure that no proxified IOR is used for local invocations */
            /*
            if( ((org.jacorb.orb.ORB)orb).isApplet())
            {
                Debug.output(1, "Unproxyfying IOR:");
                org.jacorb.orb.Delegate d =
                    (org.jacorb.orb.Delegate)((org.omg.CORBA.portable.ObjectImpl)self)._get_delegate();
        
                //ugly workaround for setting the object key.
                org.jacorb.orb.ParsedIOR divpior =
                    new org.jacorb.orb.ParsedIOR(((org.jacorb.orb.ORB)orb).unproxyfy( d.getIOR() ));
    
                d.setIOR(divpior.getIOR());
                d.set_adport_and_key( divpior.getProfileBody().host+":" +
                                      divpior.getProfileBody().port,
                                      divpior.getProfileBody().object_key );

                ((org.omg.CORBA.portable.ObjectImpl)self)._set_delegate(d);
            }    
            */
            try 
            {
                ServantObject so = new ServantObject();
                if( ( poa.isRetain() && !poa.isUseServantManager() ) || 
                    poa.useDefaultServant() )
                {
                    so.servant = poa.reference_to_servant(self);
                }
                else if( poa.isUseServantManager() )
                {
                    byte [] oid = 
                        org.jacorb.poa.util.POAUtil.extractOID(getParsedIOR().get_object_key());
                    org.omg.PortableServer.ServantManager sm = 
                        poa.get_servant_manager();

                    if( poa.isRetain() )
                    {
                        org.omg.PortableServer.ServantActivator sa = 
                            org.omg.PortableServer.ServantActivatorHelper.narrow( sm );
                        so.servant = sa.incarnate( oid, poa );

                    }
                    else
                    {
                        org.omg.PortableServer.ServantLocator sl = 
                            org.omg.PortableServer.ServantLocatorHelper.narrow( sm );
                        so.servant = 
                            sl.preinvoke( oid, poa, operation, 
                                          new org.omg.PortableServer.ServantLocatorPackage.CookieHolder() );
                    }                   
                }
                else
                {
                    //
                }

                if (!expectedType.isInstance(so.servant)) 
                    return null;
                else 
                {                           
                    ((org.jacorb.orb.ORB)orb).getPOACurrent()._addContext(
                        Thread.currentThread(),
                        new org.jacorb.poa.LocalInvocationContext(
                            orb, 
                            poa, 
                            getObjectId(), 
                            (org.omg.PortableServer.Servant)so.servant
                        )
                    );
                }
                return so;
            }
            catch ( Throwable e ) 
            {
                Debug.output(2,e);
            }
        }
        return null;   
    }

    /**
     * used only by ORB.getConnection ( Delegate ) when diverting
     * connection to the proxy by Delegate.servant_preinvoke 
     */
    /*
    public void set_adport_and_key( String ap, byte[] _key )
    {
        //adport = ap;
        //object_key = _key;
    }

    public void setIOR(org.omg.IOP.IOR _ior)
    {        
        synchronized( bind_sync )
        {
            _pior = new ParsedIOR( _ior );
            piorOriginal = null;

            bind_sync.notifyAll();
        }     
    }
    */
    public String toString()
    {
        synchronized( bind_sync )
        {
            if( piorOriginal != null )
                return piorOriginal.getIORString();
            else
                return getParsedIOR().getIORString();
        }
    }
    
    public String toString(org.omg.CORBA.Object self)
    {
        return toString();
    }
    
    public String typeId()
    {
        return getParsedIOR().getIOR().type_id;
    }

    public boolean useSSL()
    {
        return getParsedIOR().useSSL();
    }
        
    public void initInterceptors()
    {
        use_interceptors = ((org.jacorb.orb.ORB) orb).hasClientRequestInterceptors();
    }

    public org.omg.CORBA.Object set_policy_override(org.omg.CORBA.Object self,
                org.omg.CORBA.Policy[] policies,
                org.omg.CORBA.SetOverrideType set_add) 
    {
	if ( set_add == org.omg.CORBA.SetOverrideType.SET_OVERRIDE )
	{
	    policy_overrides.clear();
	}
	for (int i = 0; i < policies.length; i++)
	{
	    if ( ((org.jacorb.orb.ORB)orb).hasPolicyFactoryForType( policies[i].policy_type() ) )
	    {
		policy_overrides.put(new Integer(policies[i].policy_type()), policies[i]);
	    }
	}
	ParsedIOR pior = getParsedIOR();
	org.omg.IOP.IOR ior = ((org.jacorb.orb.ORB)orb).createIOR( pior.getIOR().type_id, 
								   pior.get_object_key(), 
								   !poa.isPersistent(), 
								   poa,
								   policy_overrides);
        synchronized( bind_sync )
        {
	    _pior = new ParsedIOR( ior );
	    getParsedIOR().init();
	}
	return self;
    }

    public String get_codebase(org.omg.CORBA.Object self)
    {
	return getParsedIOR().getCodebaseComponent();
    }

    private class Barrier
    {        
        private boolean is_open = true;
        
        public synchronized void waitOnBarrier()
        {
            while( ! is_open )
            {
                try
                {
                    this.wait();
                }
                catch( InterruptedException e )
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









