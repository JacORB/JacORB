package org.jacorb.orb;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2000  Gerald Brose.
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
    extends org.omg.CORBA.portable.Delegate
{
    // these need to be accessible to the ORB
    public ParsedIOR pior;
    public ClientConnection connection;
    
    private byte[] object_key;
    private byte[] oid;
    private String adport;
    private org.omg.IOP.IOR ior;

    /** code set service Context */
    org.omg.IOP.ServiceContext [] ctx = new org.omg.IOP.ServiceContext[0];

    /** SSL tagged component */
    private org.omg.SSLIOP.SSL ssl;

    /** domain service used to implement get_policy and get_domain_managers */
    private static Domain _domainService= null;

    private boolean uses_ssl = false; // bnv

    /* save original ior for fall-back */
    private org.omg.IOP.IOR iorOriginal = null;
    private ParsedIOR piorOriginal = null;

    private boolean bound = false;
    private org.jacorb.poa.POA poa;

    private int client_count = 1;
    protected org.omg.CORBA.ORB orb;
    private org.jacorb.poa.InvocationContext context;

    private boolean use_interceptors = false;
    private boolean location_forward_permanent = true;


    /* constructors: */
    public Delegate()
    {}

    protected Delegate(org.omg.CORBA.ORB orb, org.jacorb.orb.ParsedIOR _pior )
    {
        this.orb = orb;
        ior = _pior.getIOR();
        pior = _pior;
        _init();
    }

    protected Delegate(org.omg.CORBA.ORB orb, String object_reference ) 
    {
        this.orb = orb;
        if ( object_reference.indexOf("IOR:") == 0)
        {
            pior = new ParsedIOR( object_reference );
            ior =  pior.getIOR();
        }
        else
            throw new org.omg.CORBA.INV_OBJREF("Not an IOR: "+object_reference);
        _init();
    }

    protected Delegate(org.omg.CORBA.ORB orb, org.omg.IOP.IOR _ior )
    {
        this.orb = orb;
        ior = _ior;
        pior = new ParsedIOR( ior );
        _init();
    }


    public int _get_TCKind() 
    {
        return org.omg.CORBA.TCKind._tk_objref;
    }

    /*
     * bnv: client-side policy enforcement
     */

    private boolean useSSL ( org.omg.SSLIOP.SSL ssl )
    {
        if (ssl == null)
        {
            return false;
        }
        else
        {   
            // if  need it do it, or, if I support it and the others want it, ditto
            return( Environment.enforceSSL() ||
                    ( Environment.supportSSL() && ( ssl.target_requires > 1 )) );
        }
    } 

    private void _init()
    {
        org.omg.IIOP.ProfileBody_1_1 pb = pior.getProfileBody ();
        int port = pb.port;

        // bnv: consults SSL tagged component
        ssl = ParsedIOR.getSSLTaggedComponent ( pb );    
    
        if( useSSL( ssl ) ) 
        {
            //      for policy expected serverside
            uses_ssl = true; 
            port = ssl.port; 
        }                
        else 
        { 
            uses_ssl = false; 
        } 

        if( port < 0 ) 
            port += 65536;

        object_key = pb.object_key;
        adport = pb.host + ":" + port;

        if( uses_ssl )
            org.jacorb.util.Debug.output( 3, "Delegate bound to SSL " + adport );
        else
            org.jacorb.util.Debug.output( 3, "Delegate bound to " + adport );

        initInterceptors();
    }

    public synchronized void bind() 
    { 
        if( bound )
            return;
    
        if( noMoreClients() )
            throw new org.omg.CORBA.INV_OBJREF("This reference has already been released!");
    
        connection = ((jacorb.orb.ORB)orb).getConnectionManager().getConnection( this );
        bound = true;
    
        /* The delegate could query the server for the object location using
           a GIOP locate request to make sure the first call will get through
           without redirections (provided the server's answer is definite):
        */
    
        if( org.jacorb.util.Environment.locateOnBind())
        {        
            LocateRequestOutputStream lros = 
                new LocateRequestOutputStream( connection, object_key );
            LocateReplyInputStream lris = 
                connection.sendLocateRequest( lros );
    
            switch ( lris.status().value() )
            {
            case org.omg.GIOP.LocateStatusType_1_0._OBJECT_HERE:
                org.jacorb.util.Debug.output(3,"object here");
                break;
            case org.omg.GIOP.LocateStatusType_1_0._OBJECT_FORWARD:
                org.jacorb.util.Debug.output(3,"Locate Reply: Forward");
                unbind();
                bind( orb.object_to_string( lris.read_Object()) );    
                // ignore this for the moment...
                break;
            case org.omg.GIOP.LocateStatusType_1_0._UNKNOWN_OBJECT :
                throw new org.omg.CORBA.UNKNOWN("Could not bind to object, server does not know it!");
            default:
                throw new RuntimeException("Unknown reply status for LOCATE_REQUEST: " + lris.status().value());
            }
        }
    }

    public synchronized void bind( String object_reference) 
    { 
        if( bound )
            return;
        if (object_reference.indexOf("IOR:") == 0)
        {
            pior = new ParsedIOR( object_reference );
            ior =  pior.getIOR();
        }
        _init();
        bind();
    }


    /**
     * Unbind this reference
     */ 
    
    public synchronized void unbind()
    {
        if( ! bound )
            return;
        pior = null;
        adport = null;
        if( connection != null )
            connection.releaseConnection();
        bound = false;
    }

    public org.omg.CORBA.Request create_request(org.omg.CORBA.Object self,
                                                             org.omg.CORBA.Context ctx,
                                                             java.lang.String operation ,
                                                             org.omg.CORBA.NVList args, 
                                                             org.omg.CORBA.NamedValue result)
    {
        if( !bound)
            bind();
        return new org.jacorb.orb.dii.Request( self, orb, 
                                           connection, object_key, 
                                           operation, args, ctx, result);
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
        if( true )
            throw new java.lang.RuntimeException("Not yet implemented!");
        return null;
    }

    private synchronized void incrementClientCount()
    {
        client_count++;
    }

    private synchronized void decrementClientCount()
    {
        client_count--;
    }

    private synchronized boolean noMoreClients()
    {
        return (client_count <= 0 );
    }


    public synchronized org.omg.CORBA.Object duplicate(org.omg.CORBA.Object self)
    {
        if( !bound )
            bind();
        try
        {
            incrementClientCount();
            if( connection == null )
            {
                bound = false;
                throw new org.omg.CORBA.COMM_FAILURE();
            }
            connection.duplicate();
            org.jacorb.util.Debug.output(4,"Reference count for " + adport + " : " + client_count );
            return self;
        } 
        catch ( Exception e )
        {
            org.jacorb.util.Debug.output(3,e);
            return null;
        }
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
        release( null );
        Debug.output(3," Delegate gc'ed!");
    }

    public String get_adport()
    {
        return adport;
    }

    private org.jacorb.orb.domain.Domain _domainService()
    {
        if (_domainService == null)
        {
            try 
            { 

                org.jacorb.util.Debug.output
                    (Debug.DOMAIN | Debug.DEBUG1, "Delegate._domainService: fetching "
                     +"global domain service reference from orb");
                _domainService= org.jacorb.orb.domain.DomainHelper.narrow
                    ( this.orb.resolve_initial_references("DomainService") );
            }
            catch (Exception e) 
            {

                org.jacorb.util.Debug.output(Debug.DOMAIN | Debug.IMPORTANT, e);
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
                org.omg.CORBA.portable.OutputStream os = request
                    (self, "_get_domain_managers", true );
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
            //              org.jacorb.util.Debug.output
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

        if( !bound ) 
            bind();
        
        // devik: if connection's tcs was not negotiated yet, mark all requests
        // with codeset servicecontext.
        ctx = connection.addCodeSetContext(ctx,pior);
    
        RequestOutputStream _os = 
            new RequestOutputStream( connection, 
                                     orb, 
                                     "_get_policy", 
                                     true, 
                                     object_key, 
                                     ctx);
    
        return get_policy(self, policy_type, _os);
    }



    public org.omg.CORBA.Policy get_policy(org.omg.CORBA.Object self, 
                                           int policy_type)
    {
        return get_policy(self, policy_type,  request(self, "_get_policy", true ));
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
                org.omg.CORBA.portable.OutputStream os = request(self, "_interface", true);
                org.omg.CORBA.portable.InputStream is = invoke( self, os );
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
        if( noMoreClients() )
            throw new org.omg.CORBA.INV_OBJREF("This reference has already been released!");
        return connection;
    }

    public org.omg.IOP.IOR getIOR()
    {
        /*        if( ior == null )
                  return org.jacorb.orb.CDROutputStream.null_ior;
                  else
        */
        if( iorOriginal != null )
            return iorOriginal;
        else
            return ior;
    }

    public byte[] getObjectId()
    {
        if( oid == null )
            oid = org.jacorb.poa.util.POAUtil.extractOID( object_key );
        return oid;
    }

    public byte[] getObjectKey()
    {
        return object_key;
    }

    public ParsedIOR getParsedIOR()
    {
        return pior;
    }

    public org.jacorb.poa.POA getPOA()
    {
        return (jacorb.poa.POA)poa;
    }

    public boolean port_is_ssl()
    {
        // check invariant
        if ( uses_ssl && 
             (connection != null) &&
             ! connection.isSSL() )
        {
            // invariant violated: this a fatal error!
            org.jacorb.util.Debug.output( 1, "SSL socket expected. FATAL ERROR." );
            // return org.omg.Security.AssociationStatus.SecAssocFailure;
            System.exit (0);
        }
        return uses_ssl;
    }

    public org.omg.SSLIOP.SSL ssl()
    {
        return ssl;
    }

    public org.omg.CORBA.portable.ObjectImpl getReference(jacorb.poa.POA _poa)
    {
        if( _poa != null && _poa._localStubsSupported())
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
        ReplyInputStream rep = null;
        ClientRequestInfoImpl info = null;
        RequestOutputStream ros = null;
    
        if (! location_forward_permanent)
        {
            org.jacorb.util.Debug.output(2, "Delegate: falling back to original IOR");
            // falling back to original target,
            // if location forward was only one-time
            unbind();
            ior = iorOriginal;
            pior = new ParsedIOR( ior );
            _init();
            bind();
          
            iorOriginal = null;
            piorOriginal = null;
            location_forward_permanent = true;
        }
    
        if( !bound )
            bind();
    
        ros = (RequestOutputStream) os;
    
        if ( use_interceptors && ros.separateHeader() )
        {
            //set up info object
            info = new ClientRequestInfoImpl();
            info.orb = (jacorb.orb.ORB) orb;
            info.operation = ros.operation();
            info.response_expected = ros.response_expected();
            info.setRequestServiceContexts(ros.getServiceContexts());
            info.received_exception = orb.create_any();
    
            if (ros.getRequest() != null)
                info.setRequest(ros.getRequest());
    
            info.effective_target = self;
    
            if (iorOriginal != null)
                info.target = ((jacorb.orb.ORB) orb)._getObject(pior);
            else
                info.target = self;
    
            info.effective_profile = pior.getEffectiveProfile();
                          
            // bnv: simply call pior.getProfileBody()
            org.omg.IIOP.ProfileBody_1_1 _body = pior.getProfileBody();
            if (_body != null)
                info.effective_components = _body.components;

            if ( info.effective_components == null )
            {
                Debug.output(3, "no effective components");
                info.effective_components = new org.omg.IOP.TaggedComponent[0];
            }
            else
            {
                org.omg.SSLIOP.SSL _ssl = 
                    ParsedIOR.getSSLTaggedComponent(info.effective_components);
                if ( _ssl == null )
                    Debug.output(3, "no SSL in effective components");
            }

              
            info.delegate = this;
        
            info.request_id = ros.requestId();
            InterceptorManager manager = ((jacorb.orb.ORB) orb).getInterceptorManager();
            info.current = manager.getCurrent();
    
            invokeInterceptors(info, ClientInterceptorIterator.SEND_REQUEST);
              
            ros.setServiceContexts(info.getRequestServiceContexts());
        }
    
        try
        {          
            os.close(); 
            rep = (ReplyInputStream)connection.sendRequest(self,ros);
            
            // devik: if tcs was not negotiated yet, in every context we will send
            // tcs wanted. After first such request was sent (and it is here) we can
            // mark connection tcs as negotiated
            connection.markTcsNegotiated();
        } 
        catch (org.omg.CORBA.SystemException cfe)
        {
            if (use_interceptors && (info != null)) {
                SystemExceptionHelper.insert(info.received_exception, cfe);

                try 
                {
                    info.received_exception_id = SystemExceptionHelper.type(cfe).id();
                } 
                catch(org.omg.CORBA.TypeCodePackage.BadKind _bk) 
                {
                    org.jacorb.util.Debug.output(2, _bk);
                }
                
                info.reply_status = SYSTEM_EXCEPTION.value;
                info.setReplyServiceContexts(new ServiceContext[0]);
            
                invokeInterceptors(info,
                                   ClientInterceptorIterator.RECEIVE_EXCEPTION);
            }
                
            // suggested by Markus Lindermeier: Catch COMM_FAILURE and fall-back to 
            // the original ior if the current one was forwarded, else throw exception.
            if( iorOriginal != null ) 
            {
                unbind();
                ior = iorOriginal;
                /* retry only once */
                iorOriginal = null; 
                piorOriginal = null; 
                pior = new ParsedIOR( ior );
                _init();
                bind();    
                /* now cause this invocation to be repeated by the caller 
                   of invoke(), i.e. the stub */
                throw new RemarshalException();
            } 
            else
                throw cfe;
        }
        catch (java.io.IOException ioe)
        {
            org.jacorb.util.Debug.output(0, ioe);
            ApplicationException _e = new ApplicationException(ioe.getMessage(), null);
    
            if (use_interceptors && (info != null))
            {
                SystemExceptionHelper.insert(info.received_exception, 
                                             new org.omg.CORBA.UNKNOWN(ioe.getMessage(),
                                                                       1, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE));
    
                info.received_exception_id = _e.getId();
              
                info.reply_status = SYSTEM_EXCEPTION.value;
                info.setReplyServiceContexts(new ServiceContext[0]);
              
                invokeInterceptors(info,
                                   ClientInterceptorIterator.RECEIVE_EXCEPTION);
            }
            throw _e;
        }
        catch (Exception e)
        {        
            org.jacorb.util.Debug.output(0, e);
            ApplicationException _e = new ApplicationException(e.getMessage(), null);
          
            if ( use_interceptors && (info != null) )
            {
                SystemExceptionHelper.insert(info.received_exception, 
                                             new org.omg.CORBA.UNKNOWN(e.getMessage(),
                                                                       1, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE));
            
                info.received_exception_id = _e.getId();
              
                info.reply_status = SYSTEM_EXCEPTION.value;
                info.setReplyServiceContexts(new ServiceContext[0]);
    
                invokeInterceptors(info,
                                   ClientInterceptorIterator.RECEIVE_EXCEPTION);
            }
          
            throw _e;
        }         

        /* look at the result stream now */
        
        if( rep != null )
        {
            try
            {
                org.omg.CORBA.portable.InputStream result = rep.result();
    
                if ( use_interceptors && (info != null) )
                {
                    ReplyHeader_1_0 _header = rep.getHeader();
              
                    //exceptions are thrown by result()
                    if (_header.reply_status.value() == ReplyStatusType_1_0._NO_EXCEPTION)
                    { 
                        info.reply_status = SUCCESSFUL.value;
            
                        info.setReplyServiceContexts(_header.service_context);
    
                        //the case that invoke was called from dii.Request._invoke()
                        //will be handled inside of dii.Request._invoke() itself,
                        //because the result will first be available there
                        if (ros.getRequest() == null) 
                        {
                            InterceptorManager manager = 
                                ((jacorb.orb.ORB) orb).getInterceptorManager();
                            info.current = manager.getCurrent();
    
                            invokeInterceptors(info,
                                               ClientInterceptorIterator.RECEIVE_REPLY);
                        }
                        else
                            ros.getRequest().setInfo(info);            
                    }
                }
    
                return result;
            }
            catch ( org.omg.PortableServer.ForwardRequest f )
            {
                if ( use_interceptors && (info != null) )
                {
                    //assuming "permanent", util new GIOP version is implemented
                    info.reply_status = LOCATION_FORWARD_PERMANENT.value;
                    info.setReplyServiceContexts(rep.getHeader().service_context);
            
                    info.forward_reference = f.forward_reference;
    
                    invokeInterceptors(info,
                                       ClientInterceptorIterator.RECEIVE_OTHER);
                }
              
                if( iorOriginal == null ) // suggested by Markus Lindermeier:Save the 
                {
                    iorOriginal = ior;  // original IOR.
                    piorOriginal = pior;
                }
    
                /* retrieve the forwarded IOR and bind to it */
                org.jacorb.util.Debug.output(3,"LocationForward");
                unbind();
                bind(orb.object_to_string(f.forward_reference));    
                throw new RemarshalException();
            }
            catch (SystemException _sys_ex)
            {
                if (use_interceptors && (info != null))
                {
                    info.reply_status = SYSTEM_EXCEPTION.value;
                    info.setReplyServiceContexts(rep.getHeader().service_context);
    
                    SystemExceptionHelper.insert(info.received_exception, _sys_ex);
                    try
                    {
                        info.received_exception_id = 
                            SystemExceptionHelper.type(_sys_ex).id();
                    }
                    catch(org.omg.CORBA.TypeCodePackage.BadKind _bk)
                    {
                        org.jacorb.util.Debug.output(2, _bk);
                    }
    
                    invokeInterceptors(info,
                                       ClientInterceptorIterator.RECEIVE_EXCEPTION);
                }
              
                throw _sys_ex;          
            }
            catch(ApplicationException _user_ex){
                if (use_interceptors && (info != null)){
                    info.reply_status = USER_EXCEPTION.value;
                    info.setReplyServiceContexts(rep.getHeader().service_context);
            
                    info.received_exception_id  = _user_ex.getId();
            
                    rep.mark(0);
                    try{
                        ApplicationExceptionHelper.insert(info.received_exception, _user_ex);
                    }catch(Exception _e){
                        org.jacorb.util.Debug.output(2, _e);
              
                        SystemExceptionHelper.insert(info.received_exception, 
                                                     new org.omg.CORBA.UNKNOWN(_e.getMessage()));
                    }
                    try{
                        rep.reset();
                    }catch (Exception _e){
                        //shouldn't happen anyway
                        org.jacorb.util.Debug.output(2, _e);
                    }
    
                    invokeInterceptors(info,
                                       ClientInterceptorIterator.RECEIVE_EXCEPTION);
                }
    
                throw _user_ex;          
            }
        }
        else
        {
            if ( use_interceptors && (info != null) )
            {
                //oneway call
                info.reply_status = SUCCESSFUL.value;
                info.setReplyServiceContexts(new ServiceContext[0]);
    
                invokeInterceptors(info, ClientInterceptorIterator.RECEIVE_OTHER);
            }
          
            return null; // if call was oneway
        }
    }

    public void invokeInterceptors(ClientRequestInfoImpl info, short op)
        throws RemarshalException
    {
        ClientInterceptorIterator intercept_iter = 
            ((jacorb.orb.ORB) orb).getInterceptorManager().getClientIterator();
        
        try{
            intercept_iter.iterate(info, op);
        }
        catch (org.omg.PortableInterceptor.ForwardRequest fwd)
        {
            // suggested by Markus Lindermeier: Save the 
            iorOriginal = ior;  // original IOR.
            piorOriginal = pior;
            location_forward_permanent = fwd.permanent;
            unbind();
            bind(orb.object_to_string(fwd.forward));    
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

    public boolean is_local(org.omg.CORBA.Object self) 
    {     
        return poa != null;
    }

    public boolean is_nil()    
    {
        if( noMoreClients() )
            throw new org.omg.CORBA.INV_OBJREF("This reference has already been released!");
        return( ior.type_id.equals("") && ior.profiles.length == 0 );
    }

    public boolean non_existent(org.omg.CORBA.Object self)
    {
        while(true)
        {
            try
            {       
                org.omg.CORBA.portable.OutputStream os = request(self, "_non_existent", true);
                org.omg.CORBA.portable.InputStream is = invoke( self, os );
                return is.read_boolean();
            }
            catch ( RemarshalException r ){}
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
        decrementClientCount();
        if( noMoreClients() )
        {
            org.jacorb.util.Debug.output(2, "releasing connection to " + adport );
            ((jacorb.orb.ORB)orb)._release( this );

            if( bound )
                unbind();
        }
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
        if( !bound)
            bind();
    
        return new org.jacorb.orb.dii.Request( self, orb, 
                                           connection, 
                                           object_key, 
                                           operation );
    }

    /**
     */

    public synchronized org.omg.CORBA.portable.OutputStream request(
                                                                    org.omg.CORBA.Object self,
                                                                    String operation,
                                                                    boolean responseExpected )
    {    
        // NOTE: When making changes to this method which are outside of the 
        // Interceptor-if-statement, please make shure to update 
        // get_poliy_no_intercept as well!
          
        // Delegate d = (jacorb.orb.Delegate)((org.omg.CORBA.portable.ObjectImpl)self)._get_delegate();

        if( !bound ) 
            bind();
        
        // devik: if connection's tcs was not negotiated yet, mark all requests
        // with codeset servicecontext.

        ctx = connection.addCodeSetContext( ctx, pior );
        
        return new RequestOutputStream( connection, 
                                        orb, 
                                        operation, 
                                        responseExpected, 
                                        object_key,
                                        ctx, 
                                        use_interceptors);
    }

    /**
     */


    public void servant_postinvoke(org.omg.CORBA.Object self, ServantObject servant) 
    {
        ((jacorb.orb.ORB)orb).getPOACurrent()._removeContext(context);
    }

    /**
     */

    public ServantObject servant_preinvoke(org.omg.CORBA.Object self, 
                                           String operation, 
                                           Class expectedType) 
    {     
        if (poa != null) 
        {
            /* make sure that no proxified IOR is used for local invocations */
    
            if( ((jacorb.orb.ORB)orb).isApplet())
            {
                org.jacorb.util.Debug.output(1, "Unproxyfying IOR:");
                org.jacorb.orb.Delegate d =
                    (jacorb.orb.Delegate)((org.omg.CORBA.portable.ObjectImpl)self)._get_delegate();
        
                //ugly workaround for setting the object key.
                org.jacorb.orb.ParsedIOR divpior =
                    new org.jacorb.orb.ParsedIOR(((jacorb.orb.ORB)orb).unproxyfy( d.getIOR() ));
    
                d.setIOR(divpior.getIOR());
                d.set_adport_and_key(divpior.getProfileBody().host+":"+divpior.getProfileBody().port,divpior.getProfileBody().object_key);
                ((org.omg.CORBA.portable.ObjectImpl)self)._set_delegate(d);
            }    
    
            try 
            {
                ServantObject so = new ServantObject();
                so.servant = poa.reference_to_servant(self);
                if (!expectedType.isInstance(so.servant)) 
                    return null;
                else 
                {
                    context = 
                        new org.jacorb.poa.LocalInvocationContext(
                                      orb, 
                                      poa, 
                                      getObjectId(), 
                                      (org.omg.PortableServer.Servant) so.servant);
                    ((jacorb.orb.ORB)orb).getPOACurrent()._addContext(
                                      context, 
                                      Thread.currentThread());
                }
                return so;
            }
            catch ( Throwable e ) 
            {
                org.jacorb.util.Debug.output(2,e);
            }
        }
        return null;   
    }

    /**
     * used only by ORB.getConnection ( Delegate ) when diverting
     * connection to the proxy by Delegate.servant_preinvoke 
     */

    public void set_adport_and_key( String ap, byte[] _key )
    {
        adport = ap;
        object_key = _key;
    }

    public void setIOR(org.omg.IOP.IOR _ior)
    {
        ior=_ior;
        pior=new org.jacorb.orb.ParsedIOR(ior);
    
        iorOriginal = null;
        piorOriginal = null;
    }

    public String toString()
    {
        //    if( client_count == 0 )
        //        throw new org.omg.CORBA.INV_OBJREF("This reference has already been released!");
        if( piorOriginal != null )
            return piorOriginal.ior_str;
        else
            return pior.ior_str;
    }
    
    public String toString(org.omg.CORBA.Object self)
    {
        return toString();
        //        return self.getClass().getName() + ":" + this.toString();
    }
    
    public String typeId()
    {
        if( noMoreClients() )
            throw new org.omg.CORBA.INV_OBJREF("This reference has already been released!");
        return ior.type_id;
    }
    
    
    public void initInterceptors()
    {
        use_interceptors = ((jacorb.orb.ORB) orb).hasClientRequestInterceptors();
    }


}

