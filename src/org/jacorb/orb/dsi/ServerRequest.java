package org.jacorb.orb.dsi;

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

import java.io.*;
import java.lang.*;
import org.jacorb.orb.*;
import org.jacorb.orb.connection.*;
import org.jacorb.orb.portableInterceptor.*;

/**
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 */

public class ServerRequest 
    extends org.omg.CORBA.ServerRequest 
    implements org.omg.CORBA.portable.ResponseHandler
{
    protected RequestInputStream in;
    protected ReplyOutputStream out;	
    protected ServerConnection connection;
    
    protected int status = org.omg.GIOP.ReplyStatusType_1_0._NO_EXCEPTION;
    protected byte[] oid;
    protected org.omg.CORBA.Object reference = null;
    protected String[] rest_of_name = null;

    /* is this request stream or DSI-based ? */
    protected boolean stream_based; 
    protected org.omg.CORBA.SystemException sys_ex;
    protected org.omg.PortableServer.ForwardRequest location_forward;
    protected org.omg.CORBA.Any  ex;
    protected org.omg.CORBA.Any result;
    protected org.jacorb.orb.NVList args;

    private org.jacorb.orb.ORB orb;

    private ServerRequestInfoImpl info = null;

    /** only to be called implicitly by subclasses */
    protected ServerRequest(){}

    /** only to be called implicitly by subclasses (LocateRequest) */

    protected ServerRequest( org.jacorb.orb.ORB orb, ServerConnection _connection )
    {
	this.orb = orb;
	connection = _connection;
    }

    public ServerRequest( org.jacorb.orb.ORB orb, byte[] _buf, ServerConnection _connection )
    {
	this.orb = orb;
	in = new RequestInputStream(_connection,_buf);
	connection = _connection;

	oid = org.jacorb.poa.util.POAUtil.extractOID( in.req_hdr.object_key);
    }

    /** if this request could not be delivered directly to the
	correct POA because the POA's adapter activator could
	not be called when the parent POA was in holding state,
	the parent will queue the request and later return it
	to the adapter layer. In order to be able to find the right
	POA when trying to deliver again, we have to remember the
	target POA's name 
    */

    public void setRemainingPOAName(String [] r_o_n)
    {
	rest_of_name = r_o_n;
    }

    public String[] remainingPOAName()
    {
	return rest_of_name;
    }
	
    public java.lang.String operation()
    {
	return in.req_hdr.operation;
    }

    public org.omg.CORBA.Any result()
    {
	if( stream_based )
	    throw new RuntimeException("This ServerRequest is stream-based!");
	return result;
    }

    public org.omg.CORBA.NVList arguments()
    {
	if( stream_based )
	    throw new RuntimeException("This ServerRequest is stream-based!");
	return args;
    }

    public org.omg.CORBA.Any except()
    {
	if( stream_based )
	    throw new RuntimeException("This ServerRequest is stream-based!");
	return ex;
    }

    public org.omg.GIOP.ReplyStatusType_1_0 status()
    {
	return org.omg.GIOP.ReplyStatusType_1_0.from_int( status );
    }

    public org.omg.CORBA.Context ctx()
    {
	return null;
    }

    public void arguments(org.omg.CORBA.NVList p)
    {
	if( stream_based )
	    throw new RuntimeException("This ServerRequest is stream-based!");
	args = (jacorb.orb.NVList)p;
	// unmarshal

	if( args != null )
	{
	    in.mark(0);
	    for( java.util.Enumeration e = args.enumerate(); 
		 e.hasMoreElements(); )
	    {
		org.omg.CORBA.NamedValue nv = 
		    (org.omg.CORBA.NamedValue)e.nextElement();
			
		if( nv.flags() != org.omg.CORBA.ARG_OUT.value )
		{ 
		    // out parameters are not received
		    try
		    { 
			nv.value().read_value( in, nv.value().type() );
		    } 
		    catch (Exception ex)
		    {
			throw new org.omg.CORBA.MARSHAL("Couldn't unmarshal object of type "
							+ nv.value().type() + " in ServerRequest.");
		    }
		}
	    }
	    try
	    { 
		in.reset();
	    }
	    catch (Exception ex)
	    {
		throw new org.omg.CORBA.UNKNOWN("Could not reset input stream");
	    }
	    
	    if (info != null){
		//invoke interceptors
		org.omg.Dynamic.Parameter[] params = new org.omg.Dynamic.Parameter[args.count()];
		for (int i = 0; i < params.length; i++){
		    try{
			org.omg.CORBA.NamedValue value = args.item(i);

			org.omg.CORBA.ParameterMode mode = null;
			if (value.flags() == org.omg.CORBA.ARG_IN.value)
			    mode = org.omg.CORBA.ParameterMode.PARAM_IN;
			else if (value.flags() == org.omg.CORBA.ARG_OUT.value)
			    mode = org.omg.CORBA.ParameterMode.PARAM_OUT;
			else if (value.flags() == org.omg.CORBA.ARG_INOUT.value)
			    mode = org.omg.CORBA.ParameterMode.PARAM_INOUT;
		  
			params[i] = new org.omg.Dynamic.Parameter(value.value(), mode);
		    }catch (Exception e){
			jacorb.util.Debug.output(2, e);
		    }
		}

		info.arguments = params;

		ServerInterceptorIterator intercept_iter = 
		    orb.getInterceptorManager().getServerIterator();      
	      
		try{
		    intercept_iter.iterate(info, ServerInterceptorIterator.RECEIVE_REQUEST);
		} catch(org.omg.CORBA.UserException ue){
		  if (ue instanceof org.omg.PortableInterceptor.
		      ForwardRequest){
		    
		    org.omg.PortableInterceptor.ForwardRequest fwd =
		      (org.omg.PortableInterceptor.ForwardRequest) ue;

		    setLocationForward(new org.omg.PortableServer.
				       ForwardRequest(fwd.forward));
		  }    
		} catch (org.omg.CORBA.SystemException _sys_ex) {
		    setSystemException(_sys_ex);
		}
	      
		info = null;
	    }	
	}
    }

    public void set_result(org.omg.CORBA.Any res)
    {
	if( stream_based )
	    throw new RuntimeException("This ServerRequest is stream-based!");
	result = res;
    }

    public void set_exception(org.omg.CORBA.Any ex)
    {
	if( stream_based )
	    throw new RuntimeException("This ServerRequest is stream-based!");
	this.ex = ex;
	status = org.omg.GIOP.ReplyStatusType_1_0._USER_EXCEPTION;
    }


    public void reply()
    {
	if( in.req_hdr.response_expected )
	{
	    org.jacorb.util.Debug.output(6,"ServerRequest: reply to " + operation());
	    try 
	    { 
		if( out == null )
		{ 
		    out = 
                        new ReplyOutputStream(
                                 connection,
                                 new org.omg.IOP.ServiceContext[0],
                                 requestId(), 
                                 org.omg.GIOP.ReplyStatusType_1_0.from_int(status),
                                 orb.hasServerRequestInterceptors());		       
		}

		/* DSI-based servers set results and user exceptions using anys, so 
		   we have to treat this differently */

		if( !stream_based )
		{
		    if( status == org.omg.GIOP.ReplyStatusType_1_0._USER_EXCEPTION )
		    {
			out.write_string( ex.type().id() );
			ex.write_value( out );
		    }
		    else if( status == org.omg.GIOP.ReplyStatusType_1_0._NO_EXCEPTION )
		    {
			result.write_value( out );
			if( args != null )
			{
			    for( java.util.Enumeration e = args.enumerate(); 
				 e.hasMoreElements(); )
			    {
				jacorb.orb.NamedValue nv = 
				    (jacorb.orb.NamedValue)e.nextElement();
				
				if( nv.flags() != org.omg.CORBA.ARG_IN.value )
				{ 
				// in parameters are not returnd
				    try
				    { 
					nv.send( out );
				    } 
				    catch (Exception ex)
				    {
					throw new org.omg.CORBA.MARSHAL("Couldn't return (in)out arg of type "
									+ nv.value().type() + " in ServerRequest.");
				    }
				}
			    }
			}
			//result.write_value( out );
		    }
		}

		/* these two exceptions are set in the same way for both stream-based and
		   DSI-based servers */

		if( status == org.omg.GIOP.ReplyStatusType_1_0._LOCATION_FORWARD )
		{
		    out.write_Object( location_forward.forward_reference );
		}
		else if( status == org.omg.GIOP.ReplyStatusType_1_0._SYSTEM_EXCEPTION )
		{
		    org.jacorb.orb.SystemExceptionHelper.write( out, sys_ex );
		}

		/* everything is written to out by now, be it results or exceptions */

		out.close();
		connection.sendReply( out );
		    
	    }
	    catch ( Exception ioe )
	    {
		jacorb.util.Debug.output(2,ioe);
		System.err.println("ServerRequest: Error replying to request!");
	    }
	}
        else
        {
            if ( connection instanceof org.jacorb.orb.connection.http.ServerConnection)
            { 
                //always reply in this case
                try
                {
                    connection.sendReply( null );
                }
                catch ( Exception ioe )
                {
                    org.jacorb.util.Debug.output(2,ioe);
                    System.err.println("ServerRequest: Error replying to oneway HTTP request!");
                }
            }
	}
    }

    /* ResponseHandler */

    public org.omg.CORBA.portable.OutputStream createReply()
    {
	stream_based = true;
	if( out != null )
	    throw new Error("Internal: reply already created!");
	if( !stream_based )
	    throw new Error("Internal: ServerRequest not stream-based!");

	out = 
            new ReplyOutputStream(
                                  connection,
                                  new org.omg.IOP.ServiceContext[0],
                                  requestId(),
                                  org.omg.GIOP.ReplyStatusType_1_0.NO_EXCEPTION,
                                  orb.hasServerRequestInterceptors());
	return out;
    }

    public org.omg.CORBA.portable.OutputStream createExceptionReply()
    {
	stream_based = true;

	status = org.omg.GIOP.ReplyStatusType_1_0._USER_EXCEPTION;

	//keep service contexts
	org.omg.IOP.ServiceContext[] ctx = null;
	if (out != null)
	  ctx = out.getServiceContexts();
	else
	  ctx = new org.omg.IOP.ServiceContext[0];

	out = 
            new ReplyOutputStream(connection, ctx,
                                  requestId(),
                                  org.omg.GIOP.ReplyStatusType_1_0.USER_EXCEPTION,
                                  orb.hasServerRequestInterceptors());
	return out;
    }

    /** our own: */

    public void setSystemException(org.omg.CORBA.SystemException s)
    {
	jacorb.util.Debug.output(2, s);
	status = org.omg.GIOP.ReplyStatusType_1_0._SYSTEM_EXCEPTION;

	/* we need to create a new output stream here because a system exception may
	   have occurred *after* a no_exception request header was written onto the
	   original output stream*/

	//keep service contexts
	org.omg.IOP.ServiceContext[] ctx = null;
	if (out != null)
            ctx = out.getServiceContexts();
	else
            ctx = new org.omg.IOP.ServiceContext[0];

	out = new ReplyOutputStream(connection, ctx,
                                    requestId(),
                                    org.omg.GIOP.ReplyStatusType_1_0.SYSTEM_EXCEPTION,
                                    orb.hasServerRequestInterceptors());
	sys_ex = s;
    }

    public void setLocationForward(org.omg.PortableServer.ForwardRequest r)
    {
	jacorb.util.Debug.output(2,"Location Forward");
	status = org.omg.GIOP.ReplyStatusType_1_0._LOCATION_FORWARD;

	//keep service contexts
	org.omg.IOP.ServiceContext[] ctx = null;
	if (out != null)
	  ctx = out.getServiceContexts();
	else
	  ctx = new org.omg.IOP.ServiceContext[0];

	out = new ReplyOutputStream(connection, ctx,
                                    requestId(),
                                    org.omg.GIOP.ReplyStatusType_1_0.LOCATION_FORWARD,
                                    orb.hasServerRequestInterceptors());
	location_forward = r;
    }

    /**
     * @returns the InputStream. This operation sets the
     * request be stream-based, ie. all attempts to extract
     * data using DII-based operations will throw exceptions
     * For internal access to the stream use get_in()
     *
     */

    public org.jacorb.orb.CDRInputStream getInputStream()
    {
	stream_based = true;
	return in;
    }
  
    public ReplyOutputStream getReplyOutputStream()
    {
	if (out == null)
	    createReply();
        
	stream_based = true;
	return out;
    }

    public boolean responseExpected()
    {
	return in.req_hdr.response_expected;
    }

    public org.omg.CORBA.SystemException getSystemException()
    {
	return sys_ex;
    }

    public int requestId()
    {
	return in.req_hdr.request_id;
    }

    public byte[] objectKey()
    {
	return in.req_hdr.object_key;
    }

    public org.omg.IOP.ServiceContext[] getServiceContext()
    {
	return in.req_hdr.service_context;
    }

    public byte[] objectId()
    {
	return oid;
    }

    public boolean streamBased()
    {
	return stream_based;
    }


    public void setReference(org.omg.CORBA.Object o)
    {
	reference = o;
    }

    public org.omg.CORBA.Object getReference()
    {
	return reference;
    }

    public byte [] getBuffer()
    {
	return in.getBuffer();
    }

    public void updateBuffer( byte[] _buf )
    {
	RequestInputStream rin = 
            new RequestInputStream(connection,_buf);
	//  	byte[] n_oid = org.jacorb.poa.util.POAUtil.extractOID( in.req_hdr.object_key);
	//  	if( oid != n_oid )
	//  	    throw new org.omg.CORBA.UNKNOWN("Invalid message buffer update");
	in = rin;
    }

    public RequestInputStream get_in()
    {
	return in;
    }

    /**
     * If a new output stream has to be created, the request itself isn't fixed
     * to stream-based.
     */

    public ReplyOutputStream get_out()
    {
        if (out == null)
            out = 
                new ReplyOutputStream(connection,
                                      new org.omg.IOP.ServiceContext[0],
                                      requestId(),
                                      org.omg.GIOP.ReplyStatusType_1_0.NO_EXCEPTION,
                                      orb.hasServerRequestInterceptors());
      return out;
    }

    public void setServerRequestInfo(ServerRequestInfoImpl info)
    {
	this.info = info;
    }

    public org.omg.CORBA.Object getForwardReference()
    {
	if (location_forward != null)
	    return location_forward.forward_reference;
	else
	    return null;
    }

    public void reply(byte[] buf)
    {
	reply(buf, buf.length);
    }

    public void reply(byte[] buf,int len)
    {
	if( out == null )
	    out = new ReplyOutputStream(connection, 
                                        new org.omg.IOP.ServiceContext[0],
                                        requestId(), 
                                        org.omg.GIOP.ReplyStatusType_1_0.from_int(status));
    

	out.setBuffer(buf);
	//correct the requestId (unsigned long)
	out.setGIOPRequestId(requestId());
	out.setSize(len);
	//	out.insertMsgSize(); stream copied.. not needed
	try
        {
	    connection.sendReply( out );
	}
        catch (Exception ioe)
        {
	    org.jacorb.util.Debug.output(2,ioe);
	    ioe.printStackTrace();
	    System.out.println("ServerRequest: Error replying to request!");
	}
	in.req_hdr.response_expected = false; 
        // make sure that no
	// other reply is send
    }    

    public ServerConnection getConnection()
    {
	return connection;
    }

}





