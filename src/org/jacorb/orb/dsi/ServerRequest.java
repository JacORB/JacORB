package org.jacorb.orb.dsi;

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

import java.util.*;
import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.giop.*;
import org.jacorb.orb.portableInterceptor.ServerInterceptorIterator;
import org.jacorb.orb.portableInterceptor.ServerRequestInfoImpl;
import org.jacorb.poa.util.POAUtil;
import org.jacorb.util.Debug;
import org.jacorb.util.Environment;
import org.jacorb.util.Time;
import org.omg.CORBA.INTERNAL;
import org.omg.GIOP.ReplyStatusType_1_2;
import org.omg.IOP.INVOCATION_POLICIES;
import org.omg.IOP.ServiceContext;
import org.omg.Messaging.*;
import org.omg.TimeBase.UtcT;

/**
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 */

public class ServerRequest
    extends org.omg.CORBA.ServerRequest
    implements org.omg.CORBA.portable.ResponseHandler
{
    private RequestInputStream in;
    private ReplyOutputStream out;
    private GIOPConnection connection;

    private UtcT requestStartTime = null,
                 requestEndTime   = null,
                 replyEndTime     = null;

    /**
     * <code>scopes</code> caches the scoped poa names.
     */
    private List scopes;
    private static boolean cachePoaNames;
    private int status = ReplyStatusType_1_2._NO_EXCEPTION;
    private byte[] oid;
    private byte[] object_key;
    private org.omg.CORBA.Object reference = null;
    /**
     * <code>rest_of_name</code> is target poa's name in relation to parent.
     */
    private String[] rest_of_name = null;

    /* is this request stream or DSI-based ? */
    private boolean stream_based;

    private org.omg.CORBA.SystemException sys_ex;
    private org.omg.PortableServer.ForwardRequest location_forward;
    private org.omg.CORBA.Any  ex;
    private org.omg.CORBA.Any result;
    private org.jacorb.orb.NVList args;

    private org.jacorb.orb.ORB orb;

    private boolean usePreconstructedReply = false; //for appligator

    private ServerRequestInfoImpl info = null;

    static
    {
        cachePoaNames = Environment.isPropertyOn ("jacorb.cachePoaNames");
    }

    public ServerRequest( org.jacorb.orb.ORB orb,
                          RequestInputStream in,
                          GIOPConnection _connection )
    {
        this.orb = orb;
        this.in = in;
        connection = _connection;

        getTimingPolicies();

        object_key = orb.mapObjectKey( in.req_hdr.target.object_key() );

        oid = org.jacorb.poa.util.POAUtil.extractOID( object_key );
    }

    /*
     * if this request could not be delivered directly to the correct
     * POA because the POA's adapter activator could not be called
     * when the parent POA was in holding state, the parent will queue
     * the request and later return it to the adapter layer. In order
     * to be able to find the right POA when trying to deliver again,
     * we have to remember the target POA's name
     */

    public void setRemainingPOAName(String [] rest_of_name)
    {
        this.rest_of_name = rest_of_name;
    }

    /**
     * <code>remainingPOAName</code> retrieves (if any) the target poa's
     * name in relation to parent.
     * @return a <code>String[]</code> value
     */
    public String[] remainingPOAName()
    {
        return rest_of_name;
    }

    public String operation()
    {
        return in.req_hdr.operation;
    }

    /**
     * The resulting any must be used to create an input stream from
     * which the result value can be read.
     */

    public org.omg.CORBA.Any result()
    {
        if( stream_based )
        {
            org.omg.CORBA.Any any = orb.create_any();

            // create the output stream for the result

            CDROutputStream _out = ((CDROutputStream)any.create_output_stream());

            // get a copy of the content of this reply
            byte[] result_buf = out.getBody();

            // ... and insert it
            _out.setBuffer( result_buf  );
            // important: set the _out buffer's position to the end of the contents!
            _out.skip( result_buf.length );
            return any;
        }
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

    public ReplyStatusType_1_2 status()
    {
        return ReplyStatusType_1_2.from_int( status );
    }

    public org.omg.CORBA.Context ctx()
    {
        return null;
    }

    public void arguments(org.omg.CORBA.NVList p)
    {
        args = (org.jacorb.orb.NVList)p;
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

            if (info != null)
            {
                //invoke interceptors
                org.omg.Dynamic.Parameter[] params = new org.omg.Dynamic.Parameter[args.count()];
                for (int i = 0; i < params.length; i++)
                {
                    try
                    {
                        org.omg.CORBA.NamedValue value = args.item(i);

                        org.omg.CORBA.ParameterMode mode = null;
                        if (value.flags() == org.omg.CORBA.ARG_IN.value)
                            mode = org.omg.CORBA.ParameterMode.PARAM_IN;
                        else if (value.flags() == org.omg.CORBA.ARG_OUT.value)
                            mode = org.omg.CORBA.ParameterMode.PARAM_OUT;
                        else if (value.flags() == org.omg.CORBA.ARG_INOUT.value)
                            mode = org.omg.CORBA.ParameterMode.PARAM_INOUT;

                        params[i] = new org.omg.Dynamic.Parameter(value.value(), mode);
                    }
                    catch (Exception e)
                    {
                        Debug.output(2, e);
                    }
                }

                info.arguments = params;

                ServerInterceptorIterator intercept_iter =
                    orb.getInterceptorManager().getServerIterator();

                try
                {
                    intercept_iter.iterate(info, ServerInterceptorIterator.RECEIVE_REQUEST);
                }
                catch(org.omg.CORBA.UserException ue)
                {
                    if (ue instanceof org.omg.PortableInterceptor.
                        ForwardRequest)
                    {

                        org.omg.PortableInterceptor.ForwardRequest fwd =
                            (org.omg.PortableInterceptor.ForwardRequest) ue;

                        setLocationForward(new org.omg.PortableServer.
                            ForwardRequest(fwd.forward));
                    }
                }
                catch (org.omg.CORBA.SystemException _sys_ex)
                {
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
        status = ReplyStatusType_1_2._USER_EXCEPTION;
    }


    public void reply()
    {
        if( responseExpected() )
        {
            //shortcut for appligator
            if (usePreconstructedReply)
            {
                try
                {
                    connection.sendReply( out );
                }
                catch( Exception ioe )
                {
                    Debug.output(2,ioe);
                    Debug.output( 2, "ServerRequest: Error replying to request!" );
                }

                return;
            }

            if( Debug.isDebugEnabled() )
            {
                Debug.output( "ServerRequest: reply to " + operation() );
            }

            try
            {
                if( out == null )
                {
                    out =
                        new ReplyOutputStream(
                                 requestId(),
                                 ReplyStatusType_1_2.from_int(status),
                                 in.getGIOPMinor(),
                                 in.isLocateRequest());
                }

                /*
                 * DSI-based servers set results and user exceptions
                 * using anys, so we have to treat this differently
                 */
                if( !stream_based )
                {
                    if( status == ReplyStatusType_1_2._USER_EXCEPTION )
                    {
                        out.write_string( ex.type().id() );
                        ex.write_value( out );
                    }
                    else if( status == ReplyStatusType_1_2._NO_EXCEPTION )
                    {
                        if( result != null )
                            result.write_value( out );

                        if( args != null )
                        {
                            for( java.util.Enumeration e = args.enumerate();
                                 e.hasMoreElements(); )
                            {
                                org.jacorb.orb.NamedValue nv =
                                    (org.jacorb.orb.NamedValue)e.nextElement();

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
                    }
                }

                /*
                 * these two exceptions are set in the same way for
                 * both stream-based and DSI-based servers
                 */
                if( status == ReplyStatusType_1_2._LOCATION_FORWARD )
                {
                    out.write_Object( location_forward.forward_reference );
                }
                else if( status == ReplyStatusType_1_2._SYSTEM_EXCEPTION )
                {
                    org.jacorb.orb.SystemExceptionHelper.write( out, sys_ex );
                }

                /*
                 * everything is written to out by now, be it results
                 * or exceptions.
                 */

                connection.sendReply( out );
            }
            catch ( Exception ioe )
            {
                Debug.output(2,ioe);
                Debug.output( 2, "ServerRequest: Error replying to request!" );
            }
        }
    }

    /* ResponseHandler */

    public org.omg.CORBA.portable.OutputStream createReply()
    {
        stream_based = true;

        if( out != null )
            // The reply was already created.  This happens in oneway
            // operations using SyncScope SYNC_WITH_SERVER, and does
            // not do any harm.
            return out;

        if( !stream_based )
            throw new INTERNAL("ServerRequest not stream-based!");

        out =
            new ReplyOutputStream(requestId(),
                                  ReplyStatusType_1_2.NO_EXCEPTION,
                                  in.getGIOPMinor(),
                                  in.isLocateRequest() );

        return out;
    }

    public org.omg.CORBA.portable.OutputStream createExceptionReply()
    {
        stream_based = true;

        status = ReplyStatusType_1_2._USER_EXCEPTION;

        out =
            new ReplyOutputStream(requestId(),
                                  ReplyStatusType_1_2.USER_EXCEPTION,
                                  in.getGIOPMinor(),
                                  in.isLocateRequest() );

        return out;
    }

    /** our own: */

    public void setSystemException(org.omg.CORBA.SystemException s)
    {
        Debug.output(2, s);

        status = ReplyStatusType_1_2._SYSTEM_EXCEPTION;

        /* we need to create a new output stream here because a system exception may
           have occurred *after* a no_exception request header was written onto the
           original output stream*/


        out = new ReplyOutputStream(requestId(),
                                    ReplyStatusType_1_2.SYSTEM_EXCEPTION,
                                    in.getGIOPMinor(),
                                    in.isLocateRequest() );
        sys_ex = s;
    }

    public void setLocationForward(org.omg.PortableServer.ForwardRequest r)
    {
        Debug.output(2,"Location Forward");

        status = ReplyStatusType_1_2._LOCATION_FORWARD;

        out = new ReplyOutputStream(requestId(),
                                    ReplyStatusType_1_2.LOCATION_FORWARD,
                                    in.getGIOPMinor(),
                                    in.isLocateRequest() );
        location_forward = r;
    }

    /**
     * @return the InputStream. This operation sets the
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
        return Messages.responseExpected(in.req_hdr.response_flags);
    }

    /**
     * Returns the SyncScope of this request, as expressed in the
     * header's response_flags.  Note that here, on the server side,
     * this no longer differentiates between SYNC_NONE and SYNC_WITH_TRANSPORT.
     * The former is returned in both cases.
     */
    public short syncScope()
    {
        switch (in.req_hdr.response_flags)
        {
            case 0x00:
                return org.omg.Messaging.SYNC_NONE.value;
            case 0x01:
                return org.omg.Messaging.SYNC_WITH_SERVER.value;
            case 0x03:
                return org.omg.Messaging.SYNC_WITH_TARGET.value;
            default:
                throw new RuntimeException ("Illegal SYNC_SCOPE: "
                                            + in.req_hdr.response_flags);
        }
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
        return object_key;
    }

    /**
     * <code>getScopes</code> returns the cached list of poa_names.
     *
     * @return a <code>List</code> value containing Strings separated by
     * {@link org.jacorb.poa.POAConstants#OBJECT_KEY_SEPARATOR OBJECT_KEY_SEPARATOR}
     */
    public List getScopes ()
    {
        if (scopes == null || ( cachePoaNames == false ) )
        {
            scopes = POAUtil.extractScopedPOANames
                (POAUtil.extractPOAName (object_key));
        }
        return scopes;
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
                new ReplyOutputStream(requestId(),
                                      ReplyStatusType_1_2.NO_EXCEPTION,
                                      in.getGIOPMinor(),
                                      in.isLocateRequest() );

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

    public GIOPConnection getConnection()
    {
        return connection;
    }

    public void setUsePreconstructedReply(boolean use)
    {
        usePreconstructedReply = use;
    }

    /**
     * If this request has a service context with timing policies,
     * this method decodes them and puts them into the corresponding
     * instance variables (requestStartTime, requestEndTime, replyEndTime).
     */
    private void getTimingPolicies()
    {
        ServiceContext ctx = in.getServiceContext(INVOCATION_POLICIES.value);
        if (ctx != null)
        {
            CDRInputStream input = new CDRInputStream (null, ctx.context_data);
            input.openEncapsulatedArray();
            PolicyValue[] p = PolicyValueSeqHelper.read (input);
            for (int i=0; i < p.length; i++)
            {
                if (p[i].ptype == REQUEST_START_TIME_POLICY_TYPE.value)
                    requestStartTime = Time.fromCDR (p[i].pvalue);
                else if (p[i].ptype == REQUEST_END_TIME_POLICY_TYPE.value)
                    requestEndTime = Time.fromCDR (p[i].pvalue);
                else if (p[i].ptype == REPLY_END_TIME_POLICY_TYPE.value)
                    replyEndTime = Time.fromCDR (p[i].pvalue);
            }
        }
    }

    /**
     * Returns the time after which a reply to this request may no longer
     * be obtained or returned to the client; null if no such time has
     * been specified.
     */
    public UtcT getReplyEndTime()
    {
        return replyEndTime;
    }

    /**
     * Returns the time after which this request may no longer be
     * delivered to its target; null if no such time has been specified.
     */
    public UtcT getRequestEndTime()
    {
        return requestEndTime;
    }

    /**
     * Returns the time after which this request may be delivered to
     * its target; null if no such time has been specified.
     */
    public UtcT getRequestStartTime()
    {
        return requestStartTime;
    }

}
